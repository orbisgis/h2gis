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

import java.awt.image.renderable.ParameterBlock;
import java.util.Collection;
import java.util.Collections;
import javax.media.jai.JAI;

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ROI;
import javax.media.jai.registry.RIFRegistry;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Traces the boundaries of regions with uniform data and returns them as
 * vector polygons. The source image passes through to thedestination unchanged, 
 * similar to a JAI statistics operator, while the vectors are returned as
 * an image property.
 * <pre><code>
 * // Vectorize regions using default parameter settings
 * RenderedImage image = ...
 * ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
 * pb.setSource("source0", image);
 * RenderedOp dest = JAI.create("Vectorize", pb);
 * 
 * // retrieve the vectors
 * Collection&lt;Polygon&gt; polys = (Collection&lt;Polygon&gt;) dest.getProperty(
 *         VectorizeDescriptor.VECTOR_PROPERTY_NAME);
 * </code></pre>
 * 
 * The vectors are JTS Polygon objects. Each polygon holds the value of its source image
 * region as a Double (regardless of the source image data type) as a <i>user data</i>
 * attribute.
 * 
 * <pre><code>
 * // report source image region value and area (expressed as pixel units)
 * Collection&lt;Polygon&gt; polys = (Collection&lt;Polygon&gt;) dest.getProperty(
 *         VectorizeDescriptor.VECTOR_PROPERTY_NAME);
 * 
 * System.out.println("Region value  Perimeter       Area");
 * for (Polygon poly : polys) {
 *     Double value = (Double) poly.getUserData();
 *     double perimeter = poly.getLength();
 *     double area = poly.getArea();
 *     System.out.printf("%12.2f %10.2f %10.2f\n", value, perimeter, area);
 * }
 * </code></pre>
 * 
 * Optionally, polygons below a threshold area can be filtered from the output
 * by simple deletion, or by merging with a neighbour (where possible).
 * A neighbouring polygon is one that shares one or more boundary segments
 * with the target polygon (ie. lineal intersection). Two polygons that only
 * touch at a single vertex are not considered neighbours.
 * 
 * <pre><code>
 * ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
 * pb.setSource("source0", myImage);
 * 
 * // Filter polygons with area up to 5 pixels by merging
 * // them with the largest neighbouring polygon. Where no neighbour
 * // exists (e.g. small region surrounded by NODATA) the polygon
 * // will be discarded.
 * pb.setParameter("filterThreshold", 5.1);
 * pb.setParameter("filterMethod", VectorizeDescriptor.FILTER_MERGE_LARGEST);
 * </code></pre>
 *
 * While the Vectorize parameters allow substantial control over the 
 * polygons generated from a source image, sometimes it is not possible to
 * avoid generating unwanted polygons. An example is where the same pixel
 * value is used for a target region in one part of the image, but is 
 * treated as an outside value in other parts of the image. Generally it will
 * be straightforward to identify such unwanted polygons and filter them from
 * the result set.
 * <p>
 * 
 * The following parameters control the vectorizing process:
 * <table border="1" cellpadding="3">
 * <tr>
 * <th>Name</th>
 * <th>Class</th>
 * <th>Default</th>
 * <th>Description</th>
 * </tr>
 * 
 * <tr>
 * <td>roi</td>
 * <td>ROI</td>
 * <td>null</td>
 * <td>An optional ROI to define the vectorizing area.</td>
 * </tr>
 * 
 * <tr>
 * <td>band</td>
 * <td>Integer</td>
 * <td>0</td>
 * <td>The source image band to process.</td>
 * </tr>
 * 
 * <tr>
 * <td>outsideValues</td>
 * <td>Collection</td>
 * <td>empty</td>
 * <td>Values to treat as NODATA.</td>
 * </tr>
 * 
 * <tr>
 * <td>insideEdges</td>
 * <td>Boolean</td>
 * <td>Boolean.TRUE</td>
 * <td>
 * Whether to vectorize boundaries between data regions.
 * Setting this to false results in only the boundaries between NODATA
 * and data regions being returned.
 * </td>
 * </tr>
 * 
 * <tr>
 * <td>removeCollinear</td>
 * <td>Boolean</td>
 * <td>Boolean.TRUE</td>
 * <td>Whether to simplify polygons by removing collinear vertices.</td>
 * </tr>
 * 
 * <tr>
 * <td>filterThreshold</td>
 * <td>Double</td>
 * <td>0</td>
 * <td>
 * The area (pixel units) below which a polygon will be filtered from 
 * the output by merging or deletion. 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td>filterMethod</td>
 * <td>Integer</td>
 * <td>{@link #FILTER_MERGE_LARGEST}</td>
 * <td>The method used to filter small polygons when filterThreshold &gt; 0.
 * Must be one of:<br>
 * {@link #FILTER_MERGE_LARGEST}<br>
 * {@link #FILTER_MERGE_RANDOM}<br>
 * {@link #FILTER_DELETE}<br></td>
 * </tr>
 * </table>
 * 
 * @see com.vividsolutions.jts.geom.Polygon
 * @see org.jaitools.media.jai.regionalizeRegionalizeDescriptor
 * @see org.jaitools.media.jai.rangelookup.RangeLookupDescriptor
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class VectorizeDescriptor extends OperationDescriptorImpl {
    
    // A flag that indicates whether the operator is already registered.
    private static boolean registered = false;
    
    /**
     * Name used to access the vectorized region boundaries as
     * a destination image property.
     */
    public static final String VECTOR_PROPERTY_NAME = "vectors";
    
    /**
     * Filter small polygons by merging each with its largest (area) neighbour.
     * This is the default.
     */
    public static final int FILTER_MERGE_LARGEST = 0;
    /**
     * Filter small polygons by merging each with a randomly chosen neighbour.
     */
    public static final int FILTER_MERGE_RANDOM = 1;
    /**
     * Filter small polygons by simple deletion.
     */
    public static final int FILTER_DELETE = 2;

    static final int ROI_ARG = 0;
    static final int BAND_ARG = 1;
    static final int OUTSIDE_VALUES_ARG = 2;
    static final int INSIDE_EDGES_ARG = 3;
    static final int REMOVE_COLLINEAR_ARG = 4;
    static final int FILTER_SMALL_POLYS_ARG = 5;
    static final int FILTER_METHOD_ARG = 6;

    private static final String[] paramNames = {
        "roi",
        "band",
        "outsideValues",
        "insideEdges",
        "removeCollinear",
        "filterThreshold",
        "filterMethod"
    };

    private static final Class[] paramClasses = {
         javax.media.jai.ROI.class,
         Integer.class,
         Collection.class,
         Boolean.class,
         Boolean.class,
         Double.class,
         Integer.class
    };

    private static final Object[] paramDefaults = {
         (ROI) null,
         Integer.valueOf(0),
         Collections.EMPTY_LIST,
         Boolean.TRUE,
         Boolean.TRUE,
         Double.valueOf(0.0),
         FILTER_MERGE_LARGEST
    };

    /** Constructor. */
    public VectorizeDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Vectorize"},
                    {"LocalName", "Vectorize"},
                    {"Vendor", "org.jaitools.media.jai"},
                    {"Description", "Vecotirze boundaries of regions of uniform value"},
                    {"DocURL", "http://code.google.com/p/jaitools/"},
                    {"Version", "1.1.0"},
                    
                    {"arg0Desc", paramNames[0] + " an optional ROI"},
                    
                    {"arg1Desc", paramNames[1] + " (Integer, default=0) " +
                              "the source image band to process"},
                    
                    {"arg2Desc", paramNames[2] + " (Collection, default=null) " +
                              "optional set of values to treat as outside"},
                    
                    {"arg3Desc", paramNames[3] + " (Boolean, default=true) " +
                              "whether to vectorize boundaries between adjacent" +
                              "regions with non-outside values"},
                    {"arg4Desc", paramNames[4] + " (Boolean, default=false) " +
                              "whether to reduce collinear points in the resulting polygons"},
                    {"arg5Desc", paramNames[5] + " (Double, default=0) " +
                              "area (fractional pixels) below which polygons will be filtered"},
                    {"arg6Desc", paramNames[6] + " (Integer, default=FILTER_MERGE_LARGEST) " +
                              "filter method to use for polygons smaller than threshold area"}
                },
                new String[]{RenderedRegistryMode.MODE_NAME},   // supported modes
                
                1,                                              // number of sources
                
                paramNames,
                paramClasses,
                paramDefaults,
                    
                null                                            // valid values (none defined)
                );
    }

    /**
     * Validates supplied parameters.
     * 
     * @param modeName the rendering mode
     * @param pb the parameter block
     * @param msg a {@code StringBuffer} to receive error messages
     * 
     * @return {@code true} if parameters are valid; {@code false} otherwise
     */
    @Override
    protected boolean validateParameters(String modeName, ParameterBlock pb, StringBuffer msg) {

        boolean ok = super.validateParameters(modeName, pb, msg);
        if (ok) {
            int filterMethod = pb.getIntParameter(FILTER_METHOD_ARG);
            if ( !(filterMethod == FILTER_MERGE_LARGEST ||
                   filterMethod == FILTER_MERGE_RANDOM ||
                   filterMethod == FILTER_DELETE) ) {
                ok = false;
                msg.append("Invalid filter method: ").append(filterMethod);
            }
        }
        return ok;
    }
    
    /**
     * A method to register this operator with the OperationRegistry and
     * RIFRegistry.
     */
    public static void register()
    {
        if (!registered)
        {
            // Get the OperationRegistry.
            OperationRegistry op = JAI.getDefaultInstance().getOperationRegistry();
            // Register the operator's descriptor.
            VectorizeDescriptor desc =
                    new VectorizeDescriptor();
            op.registerDescriptor(desc);
            // Register the operators's RIF.
            SlopeRIF rif = new SlopeRIF();
            RIFRegistry.register(op, "Vectorize", "JAITOOLS", rif);
            registered = true;
        }
    }


}

