/* 
 *  Copyright (c) 2010-2011, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.h2gis.h2spatialext.jai;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import java.awt.image.RenderedImage;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.media.jai.ROI;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

/**
 * Vectorize regions of uniform value in an image.
 *
 * @author Michael Bedward
 * @author Simone Giannecchini, GeoSolutions
 * @author Erwan Bocher, LAB-STICC CNRS
 * @since 1.1
 * @version $Id$
 */
public class VectorizeOpImage extends AttributeOpImage {
    
    // positions in the 2x2 sample window
    private static final int TL = 0;
    private static final int TR = 1;
    private static final int BL = 2;
    private static final int BR = 3;
    
    private static final GeometryFactory PACKED_FACTORY = new GeometryFactory(new PackedCoordinateSequenceFactory());

    /*
     * Possible configurations of values in the 2x2 sample window: 
     *
     * <pre>
     *  0) AB   1) AA   2) AA   3) AA
     *     AB      AB      BB      BA
     *
     *  4) AB   5) AB   6) AB   7) AA
     *     BB      AA      CC      BC
     *
     *  8) AB   9) AB  10) AB  11) AB
     *     CB      AC      BC      CA
     * 
     * 12) AB  13) AB  14) AA
     *     BA      CD      AA
     * </pre>
     * 
     * These patterns are adapted from those used in the GRASS raster to 
     * vector routine.
     * 
     * The following map is a lookup table for the sample window pattern
     * where the key is constructed as follows (bit 6 is left-most):
     * bit 6 = TR != TL
     * bit 5 = BL != TL
     * bit 4 = BL != TR
     * bit 3 = BR != TL
     * bit 2 = BR != TR
     * bit 1 = BR != BL
     */
    private static final SortedMap<Integer, Integer> NBR_CONFIG_LOOKUP = new TreeMap<Integer, Integer>();
    static {
        NBR_CONFIG_LOOKUP.put( 0x2d, 0 );  // 101101
        NBR_CONFIG_LOOKUP.put( 0x07, 1 );  // 000111
        NBR_CONFIG_LOOKUP.put( 0x1e, 2 );  // 011110
        NBR_CONFIG_LOOKUP.put( 0x19, 3 );  // 011001
        NBR_CONFIG_LOOKUP.put( 0x34, 4 );  // 110100
        NBR_CONFIG_LOOKUP.put( 0x2a, 5 );  // 101010
        NBR_CONFIG_LOOKUP.put( 0x3e, 6 );  // 111110
        NBR_CONFIG_LOOKUP.put( 0x1f, 7 );  // 011111
        NBR_CONFIG_LOOKUP.put( 0x3d, 8 );  // 111101
        NBR_CONFIG_LOOKUP.put( 0x2f, 9 );  // 101111
        NBR_CONFIG_LOOKUP.put( 0x37, 10 ); // 110111
        NBR_CONFIG_LOOKUP.put( 0x3b, 11 ); // 111011
        NBR_CONFIG_LOOKUP.put( 0x33, 12 ); // 110011
        NBR_CONFIG_LOOKUP.put( 0x3f, 13 ); // 111111
        NBR_CONFIG_LOOKUP.put( 0x00, 14 ); // 000000
    }
    

    // Precision of comparison in the function different(a, b)
    // TODO: enable this to be set by the user
    private static final double EPSILON = 1.0e-8d;
    
    // Default values used for "inside" when not vectorizing
    // boundaries between adjacent inside regions
    private static final int INSIDE_FLAG_VALUE = 1;

    // Source image band being processed
    private final int band;

    // Set of values that indicate 'outside' or 'no data' areas in the raster
    private SortedSet<Double> outsideValues;
    
    // Flag indicating whether the boundaries between adjacent inside regions
    // should be vectorized
    private final boolean insideEdges;

    // Proxy value used when inside edges are not being vectorized
    // (ie. insideEdges == false)
    private Double inside = null;

    // Segments of vertical boundary under construction
    private Map<Integer, LineSegment> vertLines;

    // Segments of horizontal boundary under construction
    private LineSegment horizLine;

    // Holds lines, constructed from boundary segments, to be polygonized
    private List<LineString> lines;
    
    // Factory for construction of JTS Geometry objects
    private final static GeometryFactory GEOMETRY_FACTORY= new GeometryFactory(new PrecisionModel(10));

    // Polygons cached for subsequent requests
    SoftReference<List<Geometry>> cachedVectors;
    
    // Whether to remove collinear points from polygons.
    private final  boolean removeCollinear;
    
    // Threshold area (fractional pixels) below which polygons will 
    // filtered from output
    private final double filterThreshold;

    // Filtering method for small polygons when filterThreshold > 0
    private final int filterMethod;
    
    private Random rr;
    

    /**
     * Creates a new instance of the operator.
     * 
     * @param source the source image to be vectorized
     * 
     * @param roi an optional {@code ROI} defining the region to be vectorized
     * 
     * @param band the source image band to examine
     * 
     * @param outsideValues values representing "outside" areas (ie. regions that
     *        will not be vectorized); may be null or empty
     * 
     * @param insideEdges flag controlling whether boundaries between adjacent
     *        "inside" regions should be vectorized
     *
     * @param removeCollinear whether to remove collinear points from polygons
     * 
     * @param filterThreshold the area (factional pixels) below which polygons will
     *        be filtered from the output
     * 
     * @param filterMethod filtering method used if {@code filterThreshold > 0};
     *        must be one of 
     *        {@link VectorizeDescriptor#FILTER_MERGE_LARGEST},
     *        {@link VectorizeDescriptor#FILTER_MERGE_RANDOM}, or
     *        {@link VectorizeDescriptor#FILTER_DELETE}
     */
    public VectorizeOpImage(RenderedImage source,
            ROI roi,
            int band,
            List<Double> outsideValues,
            boolean insideEdges,
            boolean removeCollinear,
            double filterThreshold,
            int filterMethod) {
            
        super(source, roi);
                
        this.band = band;
        
        this.outsideValues = new TreeSet<Double>();
        if (outsideValues == null || outsideValues.isEmpty()) {
            this.outsideValues.add(Double.NaN);
        } else {
            this.outsideValues.addAll(outsideValues);
        }
        
        this.insideEdges = insideEdges;
        this.removeCollinear=removeCollinear;
        this.filterThreshold = filterThreshold;
        this.filterMethod = filterMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Geometry> getAttribute(String name) {
        if (cachedVectors == null || cachedVectors.get() == null) {
            synchronized(this) {
                doVectorize();
            }
        }
        
        return cachedVectors.get();
    }
        
    /**
     * {@inheritDoc}
     * @return 
     */
    @Override
    protected String[] getAttributeNames() {
        return new String[] {VectorizeDescriptor.VECTOR_PROPERTY_NAME};
    }
    

    /**
     * Runs the polygon creation and filtering steps.
     */
    private void doVectorize() {
        lines = new ArrayList<LineString>();
        vertLines = new HashMap<Integer, LineSegment>();
        
        vectorizeBoundaries();
        List<Geometry> polys = assemblePolygons();
        
        if (filterThreshold > 0) {
            filterSmallPolygons(polys);
        }
        cachedVectors = new SoftReference<List<Geometry>>(polys);
    }
        
    /**
     * Polygonizess the boundary segments that have been collected by the 
     * vectorizing algorithm and, if the field {@code insideEdges} is TRUE,
     * assigns the value of the source image band to each polygon's user data
     * field.
     */
    private List<Geometry> assemblePolygons() {

        List<Geometry> polygons = new ArrayList<Geometry>();
        RandomIter imgIter = RandomIterFactory.create(getSourceImage(0), null);
        Polygonizer polygonizer = new Polygonizer();
        
        try {
            polygonizer.add(lines);
            Collection<Geometry> rawPolys = polygonizer.getPolygons();

            for (Iterator it = rawPolys.iterator(); it.hasNext();) {
                Polygon poly = (Polygon) it.next();
                
                // Remove the geometry and free some memory
                it.remove();

                if (removeCollinear) {
                    //poly = Utils.removeCollinearVertices(poly);
                    poly = (Polygon) DouglasPeuckerSimplifier.simplify(poly, 0);
                }
                
                // Get interior point by going to the minimum boundary
                // coordinate and then addign half cell width to X and Y
                // ordinates. Since we are vectorizing around the edges of
                // raster cells this should always work.
                Coordinate[] coords = poly.getExteriorRing().getCoordinates();
                Coordinate minCoord = CoordinateArrays.minCoordinate(coords);
                Coordinate insideCoord = new Coordinate(
                        minCoord.x + 0.5, minCoord.y + 0.5);

                Point insidePt = GEOMETRY_FACTORY.createPoint(insideCoord);
                if (!poly.contains(insidePt)) {
                    throw new RuntimeException("Can't locate interior point for polygon");
                }

                //
                // now get the value and save it for future usage
                //
                double val = imgIter.getSampleDouble(
                        (int) insideCoord.x, (int) insideCoord.y, band);
                
                if ((roi == null || roi.contains(insideCoord.x, insideCoord.y)) && !isOutside(val)) {
                    // if we don't clone the polygon the results will share coordinate objects
                    // which will backfire if any c.s. visitor is used later
                    // since all geometries end up in the heap also better use packed c.s.
                    poly = (Polygon) PACKED_FACTORY.createGeometry(poly);
                    if (insideEdges) {
                        poly.setUserData(val);
                    } else {
                        poly.setUserData(inside);
                    }
                    polygons.add(poly);
                }
            }
            return polygons;
        } finally {
            // release resources
            imgIter.done();
        }
    }


    /**
     * Vectorizes the boundaries of regions of uniform value in the source image.
     */
    private void vectorizeBoundaries() {
        // array treated as a 2x2 matrix of double values used as a moving window
        double[] sample = new double[4];

        // array treated as a 2x2 matrix of boolean flags used to indicate which
        // sampling window pixels are within the source image and ROI (if used)
        boolean[] flag = new boolean[4];

        RandomIter imageIter = RandomIterFactory.create(getSourceImage(0), null);
        if (!insideEdges) {
            setInsideValue();
        }
        final Double OUT = outsideValues.first();

        try {
            // NOTE: the for-loop indices are set to emulate a one pixel width border
            // around the source image area
            for (int y = srcBounds.y - 1; y < srcBounds.y + srcBounds.height; y++) {
                sample[TR] = sample[BR] = OUT;
                flag[TR] = flag[BR] = false;

                boolean yFlag = srcBounds.contains(srcBounds.x, y);
                boolean yNextFlag = srcBounds.contains(srcBounds.x, y + 1);

                for (int x = srcBounds.x - 1; x < srcBounds.x + srcBounds.width; x++) {
                    sample[TL] = sample[TR];
                    flag[TL] = flag[TR];
                    sample[BL] = sample[BR];
                    flag[BL] = flag[BR];

                    flag[TR] = yFlag && srcBounds.contains(x + 1, y) && 
                            (roi == null || roi.contains(x + 1, y));
                    
                    flag[BR] = yNextFlag && srcBounds.contains(x + 1, y + 1) && 
                            (roi == null || roi.contains(x + 1, y + 1));

                    sample[TR] = (flag[TR] ? imageIter.getSampleDouble(x + 1, y, band) : OUT);
                    if (isOutside(sample[TR])) {
                        sample[TR] = OUT;
                    } else if (!insideEdges) {
                        sample[TR] = inside;
                    }

                    sample[BR] = (flag[BR] ? imageIter.getSampleDouble(x + 1, y + 1, band) : OUT);
                    if (isOutside(sample[BR])) {
                        sample[BR] = OUT;
                    } else if (!insideEdges) {
                        sample[BR] = inside;
                    }
                    updateCoordList(x, y, sample);
                }
            }
        } finally {
            imageIter.done();
        }
    }
    

    /**
     * Sets the proxy value used for "inside" cells when inside edges
     * are not being vectorized.
     */
    private void setInsideValue() {
        Double maxFinite = null;

        for (Double d : outsideValues) {
            if (!(d.isInfinite() || d.isNaN())) {
                maxFinite = d;
            }
        }

        if (maxFinite != null) {
            inside = maxFinite + 1;
        } else {
            inside = (double) INSIDE_FLAG_VALUE;
        }
    }


    /**
     * Controls the construction of line segments that border regions of uniform data
     * in the raster. See the {@linkplain #nbrConfig} method for more details.
     *
     * @param xpixel index of the image col in the top left cell of the 2x2 data window
     * @param ypixel index of the image row in the top left cell of the 2x2 data window
     * @param sample current sampling window data
     */
    private void updateCoordList(int xpixel, int ypixel, double[] sample) {
        LineSegment seg;
        int xvec = xpixel + 1;
        int yvec = ypixel + 1;

        int configIndex = nbrConfig(sample);
        switch (configIndex) {
            case 0:
                /*
                 * Vertical edge:
                 * 
                 *   AB
                 *   AB
                 * 
                 * No update required.
                 */
                break;

            case 1:
                /*
                 * Corner:
                 * 
                 *   AA
                 *   AB
                 * 
                 * Begin new horizontal.
                 * Begin new vertical.
                 */
                horizLine = new LineSegment();
                horizLine.p0.x = xvec;

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 2:
                /*
                 * Horizontal edge:
                 * 
                 *   AA
                 *   BB
                 * 
                 * No update required.
                 */
                break;

            case 3:
                /*
                 * Corner:
                 * 
                 *   AA
                 *   BA
                 * 
                 * End current horizontal. 
                 * Begin new vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);
                horizLine = null;

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 4:
                /*
                 * Corner:
                 * 
                 *   AB
                 *   BB
                 * 
                 * End current horizontal. 
                 * End current vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);
                horizLine = null;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);
                vertLines.remove(xvec);
                break;

            case 5:
                /*
                 * Corner:
                 * 
                 *   AB
                 *   AA
                 * 
                 * Begin new horizontal. 
                 * End current vertical.
                 */
                horizLine = new LineSegment();
                horizLine.p0.x = xvec;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);
                vertLines.remove(xvec);
                break;

            case 6:
                /*
                 * T-junction:
                 * 
                 *   AB
                 *   CC
                 * 
                 * End current horizontal. 
                 * Begin new horizontal. 
                 * End current vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);

                horizLine.p0.x = xvec;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);
                vertLines.remove(xvec);
                break;

            case 7:
                /*
                 * T-junction:
                 * 
                 *   AA
                 *   BC
                 * 
                 * End current horizontal. 
                 * Begin new horizontal. 
                 * Begin new vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);

                horizLine.p0.x = xvec;

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 8:
                /*
                 * T-junction:
                 * 
                 *   AB
                 *   CB
                 * 
                 * End current horizontal.
                 * End current vertical.
                 * Begin new vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);
                horizLine = null;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 9:
                /*
                 * T-junction:
                 * 
                 *   AB
                 *   AC
                 * 
                 * Begin new horizontal.
                 * End current vertical.
                 * Begin new vertical.
                 */
                horizLine = new LineSegment();
                horizLine.p0.x = xvec;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 10:
            case 11:
            case 12:
            case 13:
                /*
                 * Cross:
                 * 
                 *   AB  AB  AB  AB
                 *   BC  CA  BA  CD
                 * 
                 * End current horizontal.
                 * Begin new horizontal.
                 * End current vertical.
                 * Begin new vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);

                horizLine.p0.x = xvec;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 14:
                /*
                 * Uniform:
                 * 
                 *   AA
                 *   AA
                 * 
                 * No update required.
                 */
                break;
        }
    }

    /**
     * Examines the values in the 2x2 sample window and returns
     * the integer id of the configuration (0 - 14) based on
     * the NBR_CONFIG_LOOKUP {@code Map}.
     * 
     * @param sample sample window values
     * @return configuration id
     */
    private static int nbrConfig(double[] sample) {
        int flag = 0;
        
        flag |= (isDifferent(sample[TR], sample[TL]) << 5);
        
        flag |= (isDifferent(sample[BL], sample[TL]) << 4);
        flag |= (isDifferent(sample[BL], sample[TR]) << 3);
        
        flag |= (isDifferent(sample[BR], sample[TL]) << 2);
        flag |= (isDifferent(sample[BR], sample[TR]) << 1);
        flag |=  isDifferent(sample[BR], sample[BL]);
        
        return NBR_CONFIG_LOOKUP.get(flag);
    }

    /**
     * Creates a LineString for a newly constructed horizontal border segment
     * @param y y ordinate of the line
     */
    private void addHorizLine(int y) {
        Coordinate[] coords = new Coordinate[] { 
            new Coordinate(horizLine.p0.x, y),
            new Coordinate(horizLine.p1.x, y) 
        };

        lines.add(GEOMETRY_FACTORY.createLineString(coords));
    }

    /**
     * Creates a LineString for a newly constructed vertical border segment
     * @param x x ordinate of the line
     */
    private void addVertLine(int x) {
        
        Coordinate[] coords = new Coordinate[] {
            new Coordinate(x, vertLines.get(x).p0.y),
            new Coordinate(x, vertLines.get(x).p1.y)
        };
        
        lines.add(GEOMETRY_FACTORY.createLineString(coords));
    }

    private boolean isOutside(double value) {
        for (Double d : outsideValues) {
            if (isDifferent(d, value) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if two double values are different. Uses an absolute tolerance and
     * checks for NaN values.
     *
     * @param a first value
     * @param b second value
     * @return 1 if the values are different; 0 otherwise
     */
    private static int isDifferent(double a, double b) {
        if (Double.isNaN(a) ^ Double.isNaN(b)) {
            return 1;
        } else if (Double.isNaN(a) && Double.isNaN(b)) {
            return 0;
        }

        if (Math.abs(a - b) > EPSILON) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Filters small polygons from the list of initial polygons using either
     * deletion or merging as specified by the filterMethod parameter.
     * <p>
     * The list of small polygons is processed repeatedly until empty or
     * no more can be removed by merging to neighbouring polygons.
     * 
     * @param polys initial polygons
     */
    private void filterSmallPolygons(List<Geometry> polys) {
        List<Geometry> toFilter = new ArrayList<Geometry>();
        List<Geometry> holdOver = new ArrayList<Geometry>();
        
        ListIterator<Geometry> polysIter = polys.listIterator();
        while (polysIter.hasNext()) {
            Geometry poly = polysIter.next();
            if (poly.getArea() < filterThreshold) {
                polysIter.remove();
                if (filterMethod != VectorizeDescriptor.FILTER_DELETE) {
                    toFilter.add(poly);
                }
            }
        }
        
        if (toFilter.isEmpty()) {
            return;
        }
        
        /*
         * TODO: set some threshold number of small polygons to 
         * create a spatial index ?
         */
        
        Quadtree spIndex = new Quadtree();
        for (Geometry poly : polys) {
            spIndex.insert(poly.getEnvelopeInternal(), poly);
        }

        boolean foundMergers;
        do {
            foundMergers = false;
            
            ListIterator<Geometry> filterIter = toFilter.listIterator();
            while (filterIter.hasNext()) {
                Geometry smallPoly = filterIter.next();
                filterIter.remove();

                List nbrs = spIndex.query(smallPoly.getEnvelopeInternal());
                Geometry selectedNbr = null;
                if (!nbrs.isEmpty()) {
                    switch (filterMethod) {
                        case VectorizeDescriptor.FILTER_MERGE_LARGEST:
                            selectedNbr = getLargestNbr(smallPoly, nbrs);
                            break;

                        case VectorizeDescriptor.FILTER_MERGE_RANDOM:
                            selectedNbr = getRandomNbr(smallPoly, nbrs);
                            break;

                        default:
                            throw new IllegalArgumentException("Invalid filterMethod value");
                    }
                }

                if (selectedNbr != null) {
                    foundMergers = true;
                    spIndex.remove(selectedNbr.getEnvelopeInternal(), selectedNbr);
                    removePolygon(polys, selectedNbr);
                    
                    Geometry merged = selectedNbr.union(smallPoly);
                    merged.setUserData(selectedNbr.getUserData());
                    spIndex.insert(merged.getEnvelopeInternal(), merged);
                    polys.add(merged);

                } else {
                    // no merger was possible but it might be later when other
                    // polys have been merged, so hold over
                    holdOver.add(smallPoly);
                }
            }
            
            toFilter.addAll(holdOver);
            holdOver.clear();
            
        } while (foundMergers && !toFilter.isEmpty());
    }

    /**
     * Gets the largest neighbour of the given small polygon.
     * 
     * @param smallPoly the small polygon
     * @param nbrs list of potential neighbours
     * 
     * @return the largest neighbouring polygon; or {@code null} if
     *         no neighbour could be found
     */
    private Geometry getLargestNbr(Geometry smallPoly, List nbrs) {
        Geometry largestNbr = null;
        ListIterator nbrsIter = nbrs.listIterator();
        double maxArea = 0;

        while (nbrsIter.hasNext()) {
            Geometry g = (Geometry) nbrsIter.next();
            // Check that boundaries have lineal intersection
            if (smallPoly.relate(g, "****1****")) {
                double area = g.getArea();
                if (area > maxArea) {
                    maxArea = area;
                    largestNbr = g;
                }
            } else {
                nbrsIter.remove();
            }
        }

        return largestNbr;
    }

    /**
     * Selects a random neighbour of the given small polygon.
     * 
     * @param smallPoly the small polygon
     * @param nbrs list of potential neighbours
     * 
     * @return the selected neighbouring polygon; or {@code null} if
     *         no neighbour could be found
     */
    private Geometry getRandomNbr(Geometry smallPoly, List nbrs) {
        Geometry selected = null;
        ListIterator nbrsIter = nbrs.listIterator();

        while (nbrsIter.hasNext()) {
            Geometry g = (Geometry) nbrsIter.next();
            // Check that boundaries have lineal intersection
            if (!smallPoly.relate(g, "****1****")) {
                nbrsIter.remove();
            }
        }

        if (!nbrs.isEmpty()) {
            if (rr == null) rr = new Random();
            int index = rr.nextInt(nbrs.size());
            selected = (Geometry) nbrs.get(index);
        }
        
        return selected;
    }

    
    /**
     * Remove a polygon from the list of polygons. 
     * 
     * TODO: remove this method when JTS 1.12 is released with its fix for
     * Geometry.equals(Object o)
     * 
     * @param polys list of current polygons
     * @param toRemove polygon to remove
     * 
     * @throws RuntimeException if the polygon is not found
     */
    private void removePolygon(List<Geometry> polys, Geometry toRemove) {
        int k = 0;
        for (Geometry p : polys) {
            if (p.equalsExact(toRemove, EPSILON)) {
                polys.remove(k);
                return;
            }
            k++ ;
        }
        
        throw new RuntimeException("Failed to remove polygon");
    }

}
