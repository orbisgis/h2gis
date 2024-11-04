package org.h2gis.functions.spatial.split;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.*;

import java.util.*;

public class ST_SubDivide extends DeterministicScalarFunction {

    static GeometryFactory FACTORY = new GeometryFactory();

    public ST_SubDivide() {
        addProperty(PROP_REMARKS, "Divides geometry into parts using its internal envelope, " +
                "until each part can be represented using no more than max_vertices.\n If no vertices apply a single recurve");
    }

    @Override
    public String getJavaStaticMethod() {
        return "divide";
    }

    /**
     * Divide the geometry into quadrants
     *
     * @param geom {@link Geometry}
     * @return geometry
     */
    public static Geometry divide(Geometry geom) {
        return divideOnePass(geom);
    }

    /**
     * @param geom {@link Geometry}
     * @param maxvertices max number of vertices
     * @return Geometry
     */
    public static Geometry divide(Geometry geom, int maxvertices) {
        Geometry res = FACTORY.buildGeometry(subdivide_recursive(geom, maxvertices));
        res.setSRID(geom.getSRID());
        return res;
    }


    /**
     * Divide a geometry in quadrant recursively
     *
     * @param geom        input geometry
     * @param maxvertices number of vertices in the final geometry
     * @return geometry
     */
    public static List<Geometry> subdivide_recursive(Geometry geom, int maxvertices) {
        if(geom ==null){
            return null;
        }
        maxvertices = Math.max(0, maxvertices);
        Stack<Geometry> stack = new Stack<>();
        int size = geom.getNumGeometries();
        for (int i = 0; i < size; i++) {
            stack.add(geom.getGeometryN(i));
        }
        List<Geometry> results = new ArrayList<>();
        while (!stack.isEmpty()) {
            final Geometry slice = stack.pop();
            int nbPts = 0;
            if (geom instanceof Polygon) {
                nbPts = slice.getNumPoints() - 1;
            } else if (geom instanceof LineString) {
                nbPts = slice.getNumPoints();
            }
            if (nbPts > maxvertices) {
                final Envelope envelope = slice.getEnvelopeInternal();
                final double minX = envelope.getMinX();
                final double maxX = envelope.getMaxX();
                final double midX = minX + (maxX - minX) / 2.0;
                final double minY = envelope.getMinY();
                final double maxY = envelope.getMaxY();
                final double midY = minY + (maxY - minY) / 2.0;
                if(envelope.getHeight()==0){
                    Envelope ulEnv = new Envelope(minX, midX, midY, maxY);
                    Envelope urEnv = new Envelope(midX, maxX, midY, maxY);
                    filterGeom(FACTORY.toGeometry(ulEnv).intersection(slice), maxvertices, stack, results);
                    filterGeom(FACTORY.toGeometry(urEnv).intersection(slice), maxvertices, stack, results);
                }
                else if(envelope.getWidth()==0){
                    Envelope llEnv = new Envelope(minX, midX, minY, midY);
                    filterGeom(FACTORY.toGeometry(llEnv).intersection(slice), maxvertices, stack, results);
                    Envelope lrEnv = new Envelope(midX, maxX, minY, midY);
                    filterGeom(FACTORY.toGeometry(lrEnv).intersection(slice), maxvertices, stack, results);
                }
                else{
                    Envelope ulEnv = new Envelope(minX, midX, midY, maxY);
                    Envelope urEnv = new Envelope(midX, maxX, midY, maxY);
                    Envelope llEnv = new Envelope(minX, midX, minY, midY);
                    Envelope lrEnv = new Envelope(midX, maxX, minY, midY);
                    filterGeom(FACTORY.toGeometry(ulEnv).intersection(slice), maxvertices, stack, results);
                    filterGeom(FACTORY.toGeometry(urEnv).intersection(slice), maxvertices, stack, results);
                    filterGeom(FACTORY.toGeometry(llEnv).intersection(slice), maxvertices, stack, results);
                    filterGeom(FACTORY.toGeometry(lrEnv).intersection(slice), maxvertices, stack, results);
                }
            } else {
                results.add(slice);
            }
        }
        return results;
    }

    /**
     * Divide the geometry in quadrants
     *
     * @param geom input geometry
     * @return geometry
     */
    private static Geometry divideOnePass(Geometry geom) {
        if(geom ==null){
            return null;
        }
        List<Geometry> results = new ArrayList();
        int size = geom.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Geometry subGeom = geom.getGeometryN(i);
            if (subGeom instanceof Polygon || subGeom instanceof LineString) {
                final Envelope envelope = subGeom.getEnvelopeInternal();
                double minX = envelope.getMinX();
                double maxX = envelope.getMaxX();
                double midX = minX + (maxX - minX) / 2.0;
                double minY = envelope.getMinY();
                double maxY = envelope.getMaxY();
                double midY = minY + (maxY - minY) / 2.0;
                if(envelope.getHeight()==0){
                    Envelope ulEnv = new Envelope(minX, midX, midY, maxY);
                    Envelope urEnv = new Envelope(midX, maxX, midY, maxY);
                    Geometry ul = FACTORY.toGeometry(ulEnv).intersection(subGeom);
                    Geometry ur = FACTORY.toGeometry(urEnv).intersection(subGeom);
                    results.add(ul);
                    results.add(ur);
                }
                else if(envelope.getWidth()==0){
                    Envelope llEnv = new Envelope(minX, midX, minY, midY);
                    Geometry ll = FACTORY.toGeometry(llEnv).intersection(subGeom);
                    Envelope lrEnv = new Envelope(midX, maxX, minY, midY);
                    Geometry lr = FACTORY.toGeometry(lrEnv).intersection(subGeom);
                    results.add(ll);
                    results.add(lr);
                } else{
                    Envelope ulEnv = new Envelope(minX, midX, midY, maxY);
                    Envelope urEnv = new Envelope(midX, maxX, midY, maxY);
                    Geometry ul = FACTORY.toGeometry(ulEnv).intersection(subGeom);
                    Geometry ur = FACTORY.toGeometry(urEnv).intersection(subGeom);
                    results.add(ul);
                    results.add(ur);
                    Envelope llEnv = new Envelope(minX, midX, minY, midY);
                    Geometry ll = FACTORY.toGeometry(llEnv).intersection(subGeom);
                    Envelope lrEnv = new Envelope(midX, maxX, minY, midY);
                    Geometry lr = FACTORY.toGeometry(lrEnv).intersection(subGeom);
                    results.add(ll);
                    results.add(lr);
                }
            } else {
                results.add(subGeom);
            }
        }
        Geometry res = FACTORY.buildGeometry(results);
        res.setSRID(geom.getSRID());
        return res;
    }


    /**
     * Extract unique geometry and check if the geometry must be divided
     *
     * @param geom
     * @param maxvertices
     * @param stack
     * @param ret
     */
    public static void filterGeom(Geometry geom, int maxvertices, Stack stack, List ret) {
        int size = geom.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Geometry subGeom = geom.getGeometryN(i);
            int nbPts = 0;
            if (subGeom.getDimension()==2) {
                nbPts = subGeom.getNumPoints() - 1;
                if (nbPts <= maxvertices) {
                    ret.add(subGeom);
                } else {
                    stack.add(subGeom);
                }
            } else if (subGeom.getDimension()==1) {
                nbPts = subGeom.getNumPoints();
                if (nbPts <= maxvertices) {
                    ret.add(subGeom);
                } else {
                    stack.add(subGeom);
                }
            } else {
                ret.add(subGeom);
            }
        }
    }

}