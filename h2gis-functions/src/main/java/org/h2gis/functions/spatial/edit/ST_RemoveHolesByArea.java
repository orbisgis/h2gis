package org.h2gis.functions.spatial.edit;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;


public class ST_RemoveHolesByArea extends DeterministicScalarFunction {


    public ST_RemoveHolesByArea() {
        addProperty(PROP_REMARKS, "Remove all holes in a polygon or a multipolygon. "
                + "\n If the geometry doesn't contain any hole return the input geometry."
                + "\n If the input geometry is not a polygon or multipolygon return null.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "removeHoles";
    }

    /**
     * Remove any holes from the geometry. If the geometry doesn't contain any
     * holes, return it unchanged.
     *
     * @param geometry Geometry
     * @return Geometry with no holes *
     */
    public static Geometry removeHoles(Geometry geometry, double area) throws IllegalArgumentException {
        if (geometry == null) {
            return null;
        }
        if(area<0){
            throw new IllegalArgumentException("The area argument must be greater or equal than 0");
        }
        if (geometry instanceof Polygon) {
            return removeHolesPolygon((Polygon) geometry, area);
        } else if (geometry instanceof MultiPolygon) {
            return removeHolesMultiPolygon((MultiPolygon) geometry, area);
        } else if (geometry instanceof GeometryCollection) {
            int size = geometry.getNumGeometries();
            Geometry[] geometries = new Geometry[size];
            for (int i = 0; i < size; i++) {
                Geometry geom = geometry.getGeometryN(i);
                if (geometry instanceof Polygon) {
                    geometries[i] = removeHolesPolygon((Polygon) geom, area);
                } else if (geometry instanceof MultiPolygon) {
                    geometries[i] = removeHolesMultiPolygon((MultiPolygon) geom, area);
                } else {
                    geometries[i] = geom;
                }
            }
            return geometry.getFactory().createGeometryCollection(geometries);
        }
        return null;
    }

    /**
     * Create a new multiPolygon without hole.
     *
     * @param multiPolygon
     * @return
     */
    public static MultiPolygon removeHolesMultiPolygon(MultiPolygon multiPolygon, double area) {
        int num = multiPolygon.getNumGeometries();
        Polygon[] polygons = new Polygon[num];
        for (int i = 0; i < num; i++) {
            polygons[i] = removeHolesPolygon((Polygon) multiPolygon.getGeometryN(i), area);
        }
        return multiPolygon.getFactory().createMultiPolygon(polygons);
    }

    /**
     * Create a new polygon and filter the hole according an area
     *
     * @param polygon
     * @return
     */
    public static Polygon removeHolesPolygon(Polygon polygon, double area) {
        int numGeometries = polygon.getNumInteriorRing();
        ArrayList<LinearRing> holes = new ArrayList<>();
        for (int i = 0; i < numGeometries; i++) {
            LinearRing lr = polygon.getInteriorRingN(i);
            Polygon hole = new Polygon(lr, null, polygon.getFactory());
            if(hole.isValid() && hole.getArea()<=area){
                holes.add(lr);
            }
        }
        return new Polygon(polygon.getExteriorRing(),holes.toArray(new LinearRing[0]), polygon.getFactory());
    }
}
