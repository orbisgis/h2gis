package org.h2gis.functions.spatial.edit;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ST_ForcePolygonCCW extends DeterministicScalarFunction {


    public ST_ForcePolygonCCW() {
        addProperty(PROP_REMARKS, "Forces (Multi)Polygons to use a counter-clockwise orientation for their exterior ring, and a clockwise orientation for their interior rings.\n" +
                " Non-polygonal geometries are returned unchanged.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }


    /**
     *
     * @param geom
     * @return
     */
    public static Geometry execute(Geometry geom) throws SQLException {
        if (geom != null) {
            final List<Geometry> geometries = new LinkedList<Geometry>();
            forcePolygonCCW(geom, geometries);
            return geom.getFactory().buildGeometry(geometries);
        }
        return null;
    }


    private static void forcePolygonCCW(final Geometry geometry,
                                       final List<Geometry> geometries) throws SQLException {
        if (geometry instanceof Polygon) {
            forcePolygonCCW((Polygon) geometry, geometries);
        } else if (geometry instanceof GeometryCollection) {
            forcePolygonCCW((GeometryCollection) geometry, geometries);
        }
        else{
            geometries.add(geometry);
        }
    }

    private static void forcePolygonCCW(final Polygon polygon,
                                       final List<Geometry> geometries) {
        if(Orientation.isCCW(polygon.getExteriorRing().getCoordinateSequence())){
            geometries.add(polygon);
        }
        else {
            geometries.add( polygon.reverse());
        }
    }

    private static void forcePolygonCCW(final GeometryCollection geometryCollection,
                                        final List<Geometry> geometries) throws SQLException {
        int size = geometryCollection.getNumGeometries();
        for (int i = 0; i < size; i++) {
            forcePolygonCCW(geometryCollection.getGeometryN(i), geometries);
        }
    }

}
