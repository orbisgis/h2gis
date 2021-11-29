package org.h2gis.functions.spatial.split;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.*;

import java.util.ArrayDeque;
import java.util.HashSet;

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
     * @param geom
     * @return
     */
    public static Geometry divide(Geometry geom) {
        return divideOnePass(geom);
    }

    /**
     * @param geom
     * @param maxvertices
     * @return
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
     * @return
     */
    public static HashSet<Geometry> subdivide_recursive(Geometry geom, int maxvertices) {
        if(geom ==null){
            return null;
        }
        maxvertices = Math.max(0, maxvertices);
        ArrayDeque<Geometry> stack = new ArrayDeque<>();
        int size = geom.getNumGeometries();
        for (int i = 0; i < size; i++) {
            stack.add(geom.getGeometryN(i));
        }
        HashSet<Geometry> results = new HashSet<>();
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
                Envelope llEnv = new Envelope(minX, midX, minY, midY);
                Envelope lrEnv = new Envelope(midX, maxX, minY, midY);
                Envelope ulEnv = new Envelope(minX, midX, midY, maxY);
                Envelope urEnv = new Envelope(midX, maxX, midY, maxY);
                filterGeom(FACTORY.toGeometry(llEnv).intersection(slice), maxvertices, stack, results);
                filterGeom(FACTORY.toGeometry(lrEnv).intersection(slice), maxvertices, stack, results);
                filterGeom(FACTORY.toGeometry(ulEnv).intersection(slice), maxvertices, stack, results);
                filterGeom(FACTORY.toGeometry(urEnv).intersection(slice), maxvertices, stack, results);
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
     * @return
     */
    private static Geometry divideOnePass(Geometry geom) {
        if(geom ==null){
            return null;
        }
        HashSet<Geometry> results = new HashSet();
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
                Envelope llEnv = new Envelope(minX, midX, minY, midY);
                Envelope lrEnv = new Envelope(midX, maxX, minY, midY);
                Envelope ulEnv = new Envelope(minX, midX, midY, maxY);
                Envelope urEnv = new Envelope(midX, maxX, midY, maxY);
                Geometry ll = FACTORY.toGeometry(llEnv).intersection(subGeom);
                Geometry lr = FACTORY.toGeometry(lrEnv).intersection(subGeom);
                Geometry ul = FACTORY.toGeometry(ulEnv).intersection(subGeom);
                Geometry ur = FACTORY.toGeometry(urEnv).intersection(subGeom);
                results.add(ll);
                results.add(lr);
                results.add(ul);
                results.add(ur);
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
    public static void filterGeom(Geometry geom, int maxvertices, ArrayDeque<Geometry> stack, HashSet ret) {
        int size = geom.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Geometry subGeom = geom.getGeometryN(i);
            int nbPts = 0;
            if (geom instanceof Polygon) {
                nbPts = subGeom.getNumPoints() - 1;
                if (nbPts <= maxvertices) {
                    ret.add(subGeom);
                } else {
                    stack.add(subGeom);
                }
            } else if (geom instanceof LineString) {
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