package org.h2gis.functions.spatial.edit;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ST_ForcePolygonCW extends DeterministicScalarFunction {


    public ST_ForcePolygonCW() {
        addProperty(PROP_REMARKS, "Forces (Multi)Polygons to use a clockwise orientation for their exterior ring, and a counter-clockwise orientation for their interior rings.\n" +
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
            forcePolygonCW(geom, geometries);
            return geom.getFactory().buildGeometry(geometries);
        }
        return null;
    }


    private static void forcePolygonCW(final Geometry geometry,
                                          final List<Geometry> geometries) throws SQLException {
        if (geometry instanceof Polygon) {
            forcePolygonCW((Polygon) geometry, geometries);
        } else if (geometry instanceof GeometryCollection) {
            forcePolygonCW((GeometryCollection) geometry, geometries);
        }
        else{
            geometries.add(geometry);
        }
    }

    private static void forcePolygonCW(final Polygon polygon,
                                          final List<Geometry> geometries) {
        if(Orientation.isCCW(polygon.getExteriorRing().getCoordinateSequence())){
            geometries.add( polygon.reverse());
        }
        else {
            geometries.add(polygon);
        }
    }

    private static void forcePolygonCW(final GeometryCollection geometryCollection,
                                          final List<Geometry> geometries) throws SQLException {
        int size = geometryCollection.getNumGeometries();
        for (int i = 0; i < size; i++) {
            forcePolygonCW(geometryCollection.getGeometryN(i), geometries);
        }
    }

}
