/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */


package org.h2gis.functions.io.geojson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This class is used to convert a geojon geometry to a JTS geometry.
 * 
 * @author Erwan Bocher
 */
public class GJGeometryReader {
    private final GeometryFactory GF;
    
    public GJGeometryReader(GeometryFactory GF) {
        this.GF=GF;        
    }  
    
    /**
     * Parses a GeoJSON geometry and returns its JTS representation.
     *
     * Syntax:
     *
     * "geometry":{"type": "Point", "coordinates": [102.0,0.5]}
     *
     * @param jsParser
     * @throws IOException
     * @return Geometry
     * @throws java.sql.SQLException
     */
    public Geometry parseGeometry(JsonParser jsParser) throws IOException, SQLException {        
        jsParser.nextToken(); // START_OBJECT {        
        jsParser.nextToken(); // FIELD_NAME type     
        jsParser.nextToken(); // VALUE_STRING Point or whatever supported
        String geomType = jsParser.getText();
        if (geomType.equalsIgnoreCase(GeoJsonField.POINT)) {
            return parsePoint(jsParser);
        } else if (geomType.equalsIgnoreCase(GeoJsonField.MULTIPOINT)) {
            return parseMultiPoint(jsParser);
        } else if (geomType.equalsIgnoreCase(GeoJsonField.LINESTRING)) {
            return parseLinestring(jsParser);
        } else if (geomType.equalsIgnoreCase(GeoJsonField.MULTILINESTRING)) {
            return parseMultiLinestring(jsParser);
        } else if (geomType.equalsIgnoreCase(GeoJsonField.POLYGON)) {
            return parsePolygon(jsParser);
        } else if (geomType.equalsIgnoreCase(GeoJsonField.MULTIPOLYGON)) {
            return parseMultiPolygon(jsParser);
        } else if (geomType.equalsIgnoreCase(GeoJsonField.GEOMETRYCOLLECTION)) {
            return parseGeometryCollection(jsParser);
        } else {
            throw new SQLException("Unsupported geometry : " + geomType);
        }
    }
    
    /**
     * Parses one position
     *
     * Syntax:
     *
     * { "type": "Point", "coordinates": [100.0, 0.0] }
     *
     * @param jsParser
     * @throws IOException
     * @return Point
     */
    private Point parsePoint(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            jp.nextToken(); // START_ARRAY [ to parse the coordinate
            Point point = GF.createPoint(parseCoordinate(jp));
            jp.nextToken();
            return point;
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }

    /**
     * Parses an array of positions
     *
     * Syntax:
     *
     * { "type": "MultiPoint", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param jsParser
     * @throws IOException
     * @return MultiPoint
     */
    private MultiPoint parseMultiPoint(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            jp.nextToken(); // START_ARRAY [ coordinates
            MultiPoint mPoint = GF.createMultiPoint(parseCoordinates(jp));
            jp.nextToken();//END_OBJECT } geometry
            return mPoint;
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }

    /**
     *
     * Parse the array of positions.
     *
     * Syntax:
     *
     * { "type": "LineString", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param jsParser
     */
    private LineString parseLinestring(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            jp.nextToken(); // START_ARRAY [ coordinates
            LineString line = GF.createLineString(parseCoordinates(jp));
            jp.nextToken();//END_OBJECT } geometry
            return line;
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }

    /**
     * Parses an array of positions defined as:
     *
     * { "type": "MultiLineString", "coordinates": [ [ [100.0, 0.0], [101.0,
     * 1.0] ], [ [102.0, 2.0], [103.0, 3.0] ] ] }
     *
     * @param jsParser
     * @return MultiLineString
     */
    private MultiLineString parseMultiLinestring(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            ArrayList<LineString> lineStrings = new ArrayList<LineString>();
            jp.nextToken();//START_ARRAY [ coordinates
            jp.nextToken(); // START_ARRAY [ coordinates line
            while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                lineStrings.add(GF.createLineString(parseCoordinates(jp)));
                jp.nextToken();
            }
            MultiLineString line = GF.createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
            jp.nextToken();//END_OBJECT } geometry
            return line;
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }

    }

    /**
     * Coordinates of a Polygon are an array of LinearRing coordinate arrays.
     * The first element in the array represents the exterior ring. Any
     * subsequent elements represent interior rings (or holes).
     *
     * Syntax:
     *
     * No holes:
     *
     * { "type": "Polygon", "coordinates": [ [ [100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ] ] }
     *
     * With holes:
     *
     * { "type": "Polygon", "coordinates": [ [ [100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ], [ [100.2, 0.2], [100.8, 0.2],
     * [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ] ] }
     *
     *
     *
     * @param jp
     * @return Polygon
     */
    private Polygon parsePolygon(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            jp.nextToken(); // START_ARRAY [ coordinates
            jp.nextToken(); //Start the RING
            int linesIndex = 0;
            LinearRing linearRing = null;
            ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
            while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                if (linesIndex == 0) {
                    linearRing = GF.createLinearRing(parseCoordinates(jp));
                } else {
                    holes.add(GF.createLinearRing(parseCoordinates(jp)));
                }
                jp.nextToken();//END RING
                linesIndex++;
            }
            if (linesIndex > 1) {
                jp.nextToken();//END_OBJECT } geometry
                return GF.createPolygon(linearRing, holes.toArray(new LinearRing[holes.size()]));
            } else {
                jp.nextToken();//END_OBJECT } geometry
                return GF.createPolygon(linearRing, null);
            }
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }

    /**
     * Coordinates of a MultiPolygon are an array of Polygon coordinate arrays:
     *
     * { "type": "MultiPolygon", "coordinates": [ [[[102.0, 2.0], [103.0, 2.0],
     * [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]], [[[100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]], [[100.2, 0.2], [100.8, 0.2],
     * [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]] ] }
     *
     * @param jp
     * @throws IOException
     * @throws SQLException
     * @return MultiPolygon
     */
    private MultiPolygon parseMultiPolygon(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            ArrayList<Polygon> polygons = new ArrayList<Polygon>();
            jp.nextToken(); // START_ARRAY [ coordinates             
            jp.nextToken(); //Start the polygon
            while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                //Parse the polygon
                jp.nextToken(); //Start the RING
                int linesIndex = 0;
                LinearRing linearRing = null;
                ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
                while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                    if (linesIndex == 0) {
                        linearRing = GF.createLinearRing(parseCoordinates(jp));
                    } else {
                        holes.add(GF.createLinearRing(parseCoordinates(jp)));
                    }
                    jp.nextToken();//END RING
                    linesIndex++;
                }
                if (linesIndex > 1) {
                    jp.nextToken();//END_OBJECT
                    polygons.add(GF.createPolygon(linearRing, holes.toArray(new LinearRing[holes.size()])));
                } else {
                    jp.nextToken();//END_OBJECT
                    polygons.add(GF.createPolygon(linearRing, null));
                }
            }
            jp.nextToken();//END_OBJECT } geometry
            return GF.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));

        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }

    /**
     * Each element in the geometries array of a GeometryCollection is one of
     * the geometry objects described above:
     *
     * { "type": "GeometryCollection", "geometries": [ { "type": "Point",
     * "coordinates": [100.0, 0.0] }, { "type": "LineString", "coordinates": [
     * [101.0, 0.0], [102.0, 1.0] ] } ]}
     *
     * @param jp
     *
     * @throws IOException
     * @throws SQLException
     * @return GeometryCollection
     */
    private GeometryCollection parseGeometryCollection(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME geometries        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.GEOMETRIES)) {
            jp.nextToken();//START array
            //jp.nextToken();//START object
            ArrayList<Geometry> geometries = new ArrayList<Geometry>();
            while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                geometries.add(parseGeometry(jp));
            }
            jp.nextToken();//END_OBJECT } geometry
            return GF.createGeometryCollection(geometries.toArray(new Geometry[geometries.size()]));
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'geometries', found '" + coordinatesField + "'");
        }

    }

    /**
     * Parses a sequence of coordinates array expressed as
     *
     * [ [100.0, 0.0], [101.0, 1.0] ]
     *
     * @param jp
     * @throws IOException
     * @throws SQLException
     * @return Coordinate[]
     */
    private Coordinate[] parseCoordinates(JsonParser jp) throws IOException {
        jp.nextToken(); // START_ARRAY [ to parse the each positions
        ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
        while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
            coords.add(parseCoordinate(jp));
        }
        return coords.toArray(new Coordinate[coords.size()]);
    }

    /**
     * Parses a GeoJSON coordinate array and returns a JTS coordinate. The first
     * token corresponds to the first X value. The last token correponds to the
     * end of the coordinate array "]".
     *
     * Parsed syntax:
     *
     * 100.0, 0.0]
     *
     * @param jp
     * @throws IOException
     * @return Coordinate
     */
    private Coordinate parseCoordinate(JsonParser jp) throws IOException {
        jp.nextToken();
        double x = jp.getDoubleValue();// VALUE_NUMBER_FLOAT
        jp.nextToken(); // second value
        double y = jp.getDoubleValue();
        Coordinate coord;
        //We look for a z value
        jp.nextToken();
        if (jp.getCurrentToken() == JsonToken.END_ARRAY) {
            coord = new Coordinate(x, y);
        } else {
            double z = jp.getDoubleValue();
            jp.nextToken(); // exit array
            coord = new Coordinate(x, y, z);
        }
        jp.nextToken();
        return coord;
    }
    
}
