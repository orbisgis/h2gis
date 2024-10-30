/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities.jts_utils;

import org.locationtech.jts.geom.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * An utility class to convert a query in a JSON list
 *
 * @author Erwan Bocher, CNRS 2023
 */
public class GeometryFeatureUtils {

    static  int maxdecimaldigits = 9;
    /**
     * @param connection to the database
     * @param query the select query to execute
     * @return a JSON list
     */
    public static ArrayList toList(Connection connection, String query) throws SQLException {
        return toList(connection, query, maxdecimaldigits);
    }

    /**
     * @param connection to the database
     * @param query the select query to execute
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @return a JSON list
     */
    public static ArrayList<LinkedHashMap> toList(Connection connection, String query, int maxdecimaldigits) throws SQLException {
        if (connection == null || query == null) {
            throw new SQLException("Unable to get a JSON list");
        }

        final Statement statement = connection.createStatement();
        try {
            final ResultSet resultSet = statement.executeQuery(query);
            try {
                return toList(resultSet, maxdecimaldigits);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                resultSet.close();
            }
        } finally {
            statement.close();
        }
    }
    /**
     * Convert a ResulSet to a JSON list
     * @param resultSet the resulset
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @return a JSON list
     */
     public static ArrayList<LinkedHashMap> toList(ResultSet resultSet, int maxdecimaldigits) throws Exception {
        ArrayList<String> columns = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        String firstGeom =null;
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnTypeName = metaData.getColumnTypeName(i);
            if (firstGeom ==null && columnTypeName.toLowerCase().startsWith("geometry")) {
                firstGeom=metaData.getColumnName(i);
            }else {
                columns.add(metaData.getColumnName(i));
            }
        }

        int size = columns.size();
        ArrayList<LinkedHashMap> features =  new ArrayList();
        while (resultSet.next()){
            if(firstGeom!=null){
                LinkedHashMap<String, Object> feature =  new LinkedHashMap();
                feature.put("type", "Feature");
                feature.putAll(toMap((Geometry) resultSet.getObject(firstGeom), maxdecimaldigits));
                if(size>0) {
                    feature.put("properties",getProperties(resultSet, columns) );
                }
                features.add(feature);
            }
            else{
                features.add(getProperties(resultSet, columns));
            }
        }
        return features;
    }

    /**
     * Convert the resulSet values to JSON list
     * @param resultSet values
     * @param columns column names
     * @return
     */
    public static LinkedHashMap getProperties(ResultSet resultSet, Collection<String> columns) throws Exception {
        LinkedHashMap properties = new LinkedHashMap();
        for (String column:columns) {
            properties.put(column, resultSet.getObject(column));
        }
        return properties;
    }

    /**
     * Convert a Geometry to a JSON map
     * @param geom the geometry
     * @return
     */
    public static LinkedHashMap toMap(Geometry geom){
        return toMap(geom, 9);
    }

    /**
     * Convert a Geometry to a JSON map
     * @param geom the geometry
     * @param maxdecimaldigits argument may be used to reduce the maximum number of decimal places
     * @return a map
     */
    public static LinkedHashMap toMap(Geometry geom, int maxdecimaldigits){
        LinkedHashMap geometry_map = new LinkedHashMap();
        if(geom==null){
            geometry_map.put("geometry", null);
        }
        else if (geom instanceof Point) {
            geometry_map.put("geometry", toPoint((Point) geom, maxdecimaldigits));
        } else if (geom instanceof LineString) {
            geometry_map.put("geometry", toGeojsonLineString((LineString) geom, maxdecimaldigits));
        } else if (geom instanceof Polygon) {
            geometry_map.put("geometry", toGeojsonPolygon((Polygon) geom,maxdecimaldigits));
        } else if (geom instanceof MultiPoint) {
            geometry_map.put("geometry", toGeojsonMultiPoint((MultiPoint) geom, maxdecimaldigits));
        } else if (geom instanceof MultiLineString) {
            geometry_map.put("geometry", toGeojsonMultiLineString((MultiLineString) geom, maxdecimaldigits));
        } else if (geom instanceof MultiPolygon) {
            geometry_map.put("geometry", toGeojsonMultiPolygon((MultiPolygon) geom,maxdecimaldigits));
        } else {
            geometry_map.put("geometry", toGeojsonGeometryCollection((GeometryCollection) geom, maxdecimaldigits));
        }
        return geometry_map;
    }

    public static LinkedHashMap toPoint(Point point, int maxdecimaldigits ) {
        LinkedHashMap geometry_values= new LinkedHashMap();
        geometry_values.put("type", "Point");
        Coordinate coord = point.getCoordinate();
        ArrayList coord_= new ArrayList();
        coord_.add(CoordinateUtils.round(coord.x, maxdecimaldigits));
        coord_.add(CoordinateUtils.round(coord.y, maxdecimaldigits));
        if (!Double.isNaN(coord.z)) {
            coord_.add(CoordinateUtils.round(coord.z, maxdecimaldigits));
        }
        geometry_values.put("coordinates", coord_);
        return geometry_values;
    }

    public static  LinkedHashMap  toGeojsonLineString(LineString lineString, int maxdecimaldigits) {
        LinkedHashMap geometry_values= new LinkedHashMap();
        geometry_values.put("type", "LineString");
        geometry_values.put("coordinates", toGeojsonCoordinates(lineString.getCoordinates(),maxdecimaldigits));
        return geometry_values;
    }

    public static LinkedHashMap toGeojsonPolygon(Polygon polygon,int maxdecimaldigits) {
        LinkedHashMap geometry_values= new LinkedHashMap();
        geometry_values.put("type", "Polygon");
        //Process exterior ring
        ArrayList allCoords =  new ArrayList<>();
        allCoords.add(toGeojsonCoordinates(polygon.getExteriorRing().getCoordinates(), maxdecimaldigits));
        //Process interior rings
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            allCoords.add(toGeojsonCoordinates(polygon.getInteriorRingN(i).getCoordinates(),maxdecimaldigits));
        }
        geometry_values.put("coordinates", allCoords);
        return geometry_values;
    }

    public static LinkedHashMap toGeojsonMultiPoint(MultiPoint multiPoint,int maxdecimaldigits) {
        LinkedHashMap geometry_values= new LinkedHashMap();
        geometry_values.put("type", "MultiPoint");
        geometry_values.put("coordinates", toGeojsonCoordinates(multiPoint.getCoordinates(), maxdecimaldigits));
        return geometry_values;
    }

    public static LinkedHashMap toGeojsonMultiLineString(MultiLineString multiLineString,int maxdecimaldigits) {
        LinkedHashMap geometry_values= new LinkedHashMap();
        geometry_values.put("type", "MultiLineString");

        int size = multiLineString.getNumGeometries();
        ArrayList allCoords =  new ArrayList<>();
        for (int i = 0; i < size; i++) {
            allCoords.add(toGeojsonCoordinates(multiLineString.getGeometryN(i).getCoordinates(),maxdecimaldigits));

        }
        geometry_values.put("coordinates",allCoords);
        return geometry_values;
    }

    public static LinkedHashMap toGeojsonMultiPolygon(MultiPolygon multiPolygon, int maxdecimaldigits) {
        LinkedHashMap geometry_values= new LinkedHashMap();
        geometry_values.put("type", "MultiPolygon");
        int size = multiPolygon.getNumGeometries();
        ArrayList allCoords =  new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ArrayList pCoords =  new ArrayList<>();
            Polygon p = (Polygon) multiPolygon.getGeometryN(i);
            //Process exterior ring
            pCoords.add(toGeojsonCoordinates(p.getExteriorRing().getCoordinates(),maxdecimaldigits));
            //Process interior rings
            int size_p = p.getNumInteriorRing();
            for (int j = 0; j < size_p; j++) {
                pCoords.add(toGeojsonCoordinates(p.getInteriorRingN(j).getCoordinates(), maxdecimaldigits));
            }
            allCoords.add(pCoords);
        }
        geometry_values.put("coordinates",allCoords);
        return geometry_values;
    }


    public static LinkedHashMap toGeojsonGeometryCollection(GeometryCollection geometryCollection,int maxdecimaldigits) {
        LinkedHashMap geometry_values= new LinkedHashMap();
        geometry_values.put("type", "GeometryCollection");
        ArrayList geometries= new ArrayList();
        int size = geometryCollection.getNumGeometries();
        for (int i = 0; i < size; i++) {
            Geometry geom = geometryCollection.getGeometryN(i);
            if (geom instanceof Point) {
                geometries.add(toPoint((Point) geom, maxdecimaldigits));
            } else if (geom instanceof LineString) {
                geometries.add(toGeojsonLineString((LineString) geom, maxdecimaldigits ));
            } else if (geom instanceof Polygon) {
                geometries.add(toGeojsonPolygon((Polygon) geom,maxdecimaldigits));
            }
        }
        geometry_values.put("geometries",geometries );
        return geometry_values;

    }


    public static ArrayList toGeojsonCoordinates(Coordinate[] coords, int maxdecimaldigits) {
        ArrayList coords_= new ArrayList();
        for (int i = 0; i < coords.length; i++) {
            coords_.add(toGeojsonCoordinate(coords[i],maxdecimaldigits));
        }
        return coords_;
    }

    public static ArrayList toGeojsonCoordinate(Coordinate coord, int maxdecimaldigits) {
        ArrayList coord_= new ArrayList();
        coord_.add(CoordinateUtils.round(coord.x, maxdecimaldigits));
        coord_.add(CoordinateUtils.round(coord.y, maxdecimaldigits));
        if (!Double.isNaN(coord.z)) {
            coord_.add(CoordinateUtils.round(coord.z, maxdecimaldigits));
        }
        return coord_;
    }

}
