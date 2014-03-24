/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.drivers.geojson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.h2gis.h2spatialapi.ProgressVisitor;

/**
 * Driver to import a GeoJSON file into a spatial table.
 *
 * Supported geometries are POINT, LINESTRING, POLYGON and GEOMETRYCOLLECTION.
 *
 * The driver requires all Feature objects in a collection to have the same
 * schema of properties. To build the table schema the first feature of the
 * FeatureCollection is parsed. If the GeoJSON format does not contain any
 * properties, a default primary key is added.
 *
 * @author Erwan Bocher
 */
public class GeoJsonReaderDriver {

    private final String tableName;
    private final File fileName;
    private final Connection connection;
    private static final GeometryFactory GF = new GeometryFactory();
    private PreparedStatement preparedStatement = null;
    private JsonFactory jsFactory;
    private boolean hasProperties = false;
    private int featureCounter = 1;

    /**
     * Driver to import a GeoJSON file into a spatial table.
     *
     * @param connection
     * @param tableName
     * @param fileName
     */
    public GeoJsonReaderDriver(Connection connection, String tableName, File fileName) {
        this.connection = connection;
        this.tableName = tableName;
        this.fileName = fileName;
    }

    /**
     * Read the GeoJSON file.
     *
     * @param progress
     */
    public void read(ProgressVisitor progress) throws SQLException, IOException {
        String path = fileName.getAbsolutePath();
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i >= 0) {
            extension = path.substring(i + 1);
        }
        if (extension.equalsIgnoreCase("geojson")) {
            parseGeoJson(progress);
        } else {
            throw new SQLException("Please geojson extension.");
        }
    }

    /**
     * Parses a GeoJSON 1.0 file and writes it to a table.
     *
     * A GeoJSON file is structured as follows:
     *
     * { "type": "FeatureCollection", "features": [ { "type": "Feature",
     * "geometry": {"type": "Point", "coordinates": [102.0, 0.5]}, "properties":
     * {"prop0": "value0"} }, { "type": "Feature", "geometry": { "type":
     * "LineString", "coordinates": [ [102.0, 0.0], [103.0, 1.0], [104.0, 0.0],
     * [105.0, 1.0] ] }, "properties": { "prop0": "value0", "prop1": 0.0 } },
     * {"type": "Feature", "geometry": { "type": "Polygon", "coordinates": [
     * [[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]
     * ]}, "properties": { "prop0": "value0", "prop1": {"this": "that"} } } ] }
     *
     * Note: To include information on the coordinate range for geometries, a
     * GeoJSON object may have a member named "bbox".
     *
     * Syntax:
     *
     * { "type": "FeatureCollection", "bbox": [100.0, 0.0, 105.0, 1.0],
     * "features": [ ... ] }
     *
     *
     * @param progress
     */
    private void parseGeoJson(ProgressVisitor progress) throws SQLException, IOException {
        init();
        if (parseMetadata()) {
            parseData();
        } else {
            throw new SQLException("Cannot create the table " + tableName + " to import the GeoJSON data");
        }
    }

    /**
     * Parses the first GeoJSON feature to create the PreparedStatement.
     *
     * @throws SQLException
     * @throws IOException
     */
    private boolean parseMetadata() throws SQLException, IOException {
        FileInputStream fis = null;
        boolean hasGeometryField = false;
        int fieldIndex = 0;
        StringBuilder metadataBuilder = new StringBuilder();
        try {
            fis = new FileInputStream(fileName);
            JsonParser jp = jsFactory.createParser(fis);
            metadataBuilder.append("CREATE TABLE ");
            metadataBuilder.append(tableName);
            metadataBuilder.append(" (");

            jp.nextToken();//START_OBJECT
            jp.nextToken(); // field_name (type)
            jp.nextToken(); // value_string (FeatureCollection)
            String geomType = jp.getText();
            // TODO take into account CRS as
            /**
             * "type": "FeatureCollection", "crs": { "type": "name",
             * "properties": { "name": "EPSG:4326" } }, features:
             */
            if (geomType.equalsIgnoreCase("featurecollection")) {
                jp.nextToken(); // FIELD_NAME features
                String firstParam = jp.getText();
                if (firstParam.equalsIgnoreCase("features")) {
                    jp.nextToken(); // START_ARRAY [
                    JsonToken token = jp.nextToken(); // START_OBJECT {
                    if (token != JsonToken.END_ARRAY) {
                        jp.nextToken(); // FIELD_NAME type
                        jp.nextToken(); // VALUE_STRING Feature
                        geomType = jp.getText();
                        if (geomType.equalsIgnoreCase("feature")) {
                            jp.nextToken(); // FIELD_NAME geometry
                            String firstField = jp.getText();
                            if (firstField.equalsIgnoreCase("geometry")) {
                                parseGeometryMetadata(jp, metadataBuilder);
                                hasGeometryField = true;
                                fieldIndex++;
                                jp.nextToken();//END_OBJECT } geometry
                            } else if (firstField.equalsIgnoreCase("properties")) {
                                fieldIndex = parseMetadataProperties(jp, metadataBuilder, fieldIndex);
                                hasProperties = true;
                            }
                            // If there is only one geometry field in the feature them the next
                            // token corresponds to the end object of the feature.
                            jp.nextToken();
                            if (jp.getCurrentToken() != JsonToken.END_OBJECT) {
                                String secondParam = jp.getText();
                                if (secondParam.equalsIgnoreCase("geometry")) {
                                    parseGeometryMetadata(jp, metadataBuilder);
                                    hasGeometryField = true;
                                    fieldIndex++;
                                    jp.nextToken();//END_OBJECT } geometry;
                                } else if (secondParam.equalsIgnoreCase("properties")) {
                                    fieldIndex = parseMetadataProperties(jp, metadataBuilder, fieldIndex);
                                    hasProperties = true;
                                }
                                jp.nextToken(); //END_OBJECT } feature
                            }
                            if (!hasProperties) {
                                metadataBuilder.append("ID INT, PRIMARY KEY (ID)");
                                fieldIndex++;
                            }
                            metadataBuilder.append(")");
                        } else {
                            throw new SQLException("Malformed GeoJSON file. Expected 'Feature', found '" + geomType + "'");
                        }
                    }
                } else {
                    throw new SQLException("Malformed GeoJSON file. Expected 'features', found '" + firstParam + "'");
                }
            } else {
                throw new SQLException("Malformed GeoJSON file. Expected 'FeatureCollection', found '" + geomType + "'");
            }
            jp.close();
        } catch (FileNotFoundException ex) {
            throw new SQLException(ex);

        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                throw new IOException(ex);
            }
        }

        // Now we create the table if there is at leat one geometry field.
        if (hasGeometryField) {
            Statement stmt = connection.createStatement();
            stmt.execute(metadataBuilder.toString());
            stmt.close();
            // We return the preparedstatement of the waypoints table.
            if (fieldIndex > 0) {
                StringBuilder insert = new StringBuilder("INSERT INTO ").append(tableName).append(" VALUES ( ?");
                for (int i = 1; i < fieldIndex; i++) {
                    insert.append(",?");
                }
                insert.append(");");
                preparedStatement = connection.prepareStatement(insert.toString());
                return true;
            }
        } else {
            throw new SQLException("The first feature must contains a geomtry field.");
        }
        return false;
    }

    /**
     * Parses geometry metadata.
     *
     * @param jp
     * @param metadataBuilder
     */
    private void parseGeometryMetadata(JsonParser jp, StringBuilder metadataBuilder) throws IOException, SQLException {
        jp.nextToken(); //START_OBJECT {
        jp.nextToken(); // FIELD_NAME type     
        jp.nextToken(); //VALUE_STRING Point
        String geomType = jp.getText();
        if (geomType.equalsIgnoreCase("point") || geomType.equalsIgnoreCase("linestring")
                || geomType.equalsIgnoreCase("polygon") || geomType.equalsIgnoreCase("multipoint")
                || geomType.equalsIgnoreCase("multilinestring")
                || geomType.equalsIgnoreCase("multipolygon")) {
            jp.nextToken(); // FIELD_NAME coordinates
            if (jp.getText().equalsIgnoreCase("coordinates")) {
                jp.nextToken();//START coordinates array
                jp.skipChildren();
                metadataBuilder.append("THE_GEOM GEOMETRY,");
            } else {
                throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + jp.getText() + "'");
            }
        } else if (geomType.equalsIgnoreCase("geometrycollection")) {
            jp.nextToken();//START geometries array
            if (jp.getText().equalsIgnoreCase("geometries")) {
                jp.skipChildren();
                metadataBuilder.append("THE_GEOM GEOMETRY,");
            } else {
                throw new SQLException("Malformed GeoJSON file. Expected 'geometries', found '" + jp.getText() + "'");
            }
        } else {
            throw new SQLException("Unsupported geometry : " + geomType);
        }
    }

    /**
     * Parses the metadata properties.
     *
     * @param jp
     * @return index
     */
    private int parseMetadataProperties(JsonParser jp, StringBuilder metadataBuilder, int fieldIndex) throws IOException {
        jp.nextToken();//START_OBJECT {
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getText().toUpperCase(); //FIELD_NAME columnName            
            JsonToken value = jp.nextToken();
            if (value == JsonToken.VALUE_STRING) {
                metadataBuilder.append(fieldName).append(" VARCHAR");
                fieldIndex++;
            } else if (value == JsonToken.VALUE_TRUE) {
                metadataBuilder.append(fieldName).append(" BOOLEAN");
                fieldIndex++;
            } else if (value == JsonToken.VALUE_FALSE) {
                metadataBuilder.append(fieldName).append(" BOOLEAN");
                fieldIndex++;
            } else if (value == JsonToken.VALUE_NUMBER_FLOAT) {
                metadataBuilder.append(fieldName).append(" DOUBLE");
                fieldIndex++;
            } else if (value == JsonToken.VALUE_NUMBER_INT) {
                metadataBuilder.append(fieldName).append(" INT");
                fieldIndex++;
            } else if (value == JsonToken.VALUE_NULL) {
                metadataBuilder.append(fieldName).append(" VARCHAR");
                fieldIndex++;
            } else {
                // TODO: ignore value.
            }
            metadataBuilder.append(",");
        }
        return fieldIndex;
    }

    /**
     * Creates the JsonFactory.
     */
    private void init() {
        jsFactory = new JsonFactory();
        jsFactory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        jsFactory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        jsFactory.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
    }

    /**
     * Get the PreparedStatement to set the values to the table.
     *
     * @return
     */
    private PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    /**
     * Features in GeoJSON contain a geometry object and additional properties
     *
     * Syntax:
     *
     * { "type": "Feature", "geometry":{"type": "Point", "coordinates": [102.0,
     * 0.5]}, "properties": {"prop0": "value0"} }
     *
     * @param jsParser
     */
    private void parseFeature(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME geometry
        String firstField = jp.getText();
        int fieldIndex = 1;
        if (firstField.equalsIgnoreCase("geometry")) {
            jp.nextToken(); //START_OBJECT {
            getPreparedStatement().setObject(fieldIndex, parseGeometry(jp));
            fieldIndex++;
        } else if (firstField.equalsIgnoreCase("properties")) {
            parseProperties(jp, fieldIndex);
        }
        //If there is only one geometry field in the feature them the next
        //token corresponds to the end object of the feature
        jp.nextToken();
        if (jp.getCurrentToken() != JsonToken.END_OBJECT) {
            String secondParam = jp.getText();// field name
            if (secondParam.equalsIgnoreCase("geometry")) {
                jp.nextToken(); //START_OBJECT {
                getPreparedStatement().setObject(fieldIndex, parseGeometry(jp));
                fieldIndex++;
            } else if (secondParam.equalsIgnoreCase("properties")) {
                parseProperties(jp, fieldIndex);
            }
            jp.nextToken(); //END_OBJECT } feature
        }
        if (!hasProperties) {
            getPreparedStatement().setObject(fieldIndex, featureCounter);
        }
        getPreparedStatement().execute();
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
     */
    private Geometry parseGeometry(JsonParser jsParser) throws IOException, SQLException {
        jsParser.nextToken(); // FIELD_NAME type     
        jsParser.nextToken(); //VALUE_STRING Point
        String geomType = jsParser.getText();
        if (geomType.equalsIgnoreCase("point")) {
            return parsePoint(jsParser);
        } else if (geomType.equalsIgnoreCase("multipoint")) {
            return parseMultiPoint(jsParser);
        } else if (geomType.equalsIgnoreCase("linestring")) {
            return parseLinestring(jsParser);
        } else if (geomType.equalsIgnoreCase("multilinestring")) {
            return parseMultiLinestring(jsParser);
        } else if (geomType.equalsIgnoreCase("polygon")) {
            return parsePolygon(jsParser);
        } else if (geomType.equalsIgnoreCase("multipolygon")) {
            return parseMultiPolygon(jsParser);
        } else if (geomType.equalsIgnoreCase("geometrycollection")) {
            return parseGeometryCollection(jsParser);
        } else {
            throw new SQLException("Unsupported geometry : " + geomType);
        }
    }

    /**
     * Parses the properties of a feature
     *
     * Syntax:
     *
     * "properties": {"prop0": "value0"}
     *
     * @param jsParser
     */
    private void parseProperties(JsonParser jp, int fieldIndex) throws IOException, SQLException {
        jp.nextToken();//START_OBJECT {
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            JsonToken value = jp.nextToken();
            if (value == JsonToken.VALUE_STRING) {
                getPreparedStatement().setObject(fieldIndex, jp.getText());
                fieldIndex++;
            } else if (value == JsonToken.VALUE_TRUE) {
                getPreparedStatement().setObject(fieldIndex, jp.getValueAsBoolean());
                fieldIndex++;
            } else if (value == JsonToken.VALUE_FALSE) {
                getPreparedStatement().setObject(fieldIndex, jp.getValueAsBoolean());
                fieldIndex++;
            } else if (value == JsonToken.VALUE_NUMBER_FLOAT) {
                getPreparedStatement().setObject(fieldIndex, jp.getValueAsDouble());
                fieldIndex++;
            } else if (value == JsonToken.VALUE_NUMBER_INT) {
                getPreparedStatement().setObject(fieldIndex, jp.getValueAsInt());
                fieldIndex++;
            } else if (value == JsonToken.VALUE_NULL) {
                getPreparedStatement().setObject(fieldIndex, null);
                fieldIndex++;
            } else {
                //ignore other value
            }
        }

    }

    /**
     * Parses the featureCollection
     *
     * @param jp
     * @throws IOException
     * @throws SQLException
     */
    private void parseFeatures(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME features
        String firstParam = jp.getText();
        if (firstParam.equalsIgnoreCase("features")) {
            jp.nextToken(); // START_ARRAY [
            JsonToken token = jp.nextToken(); // START_OBJECT {
            while (token != JsonToken.END_ARRAY) {
                jp.nextToken(); // FIELD_NAME type
                jp.nextToken(); // VALUE_STRING Feature
                String geomType = jp.getText();
                if (geomType.equalsIgnoreCase("feature")) {
                    parseFeature(jp);
                    token = jp.nextToken(); //START_OBJECT new feature                    
                    featureCounter++;
                } else {
                    throw new SQLException("Malformed GeoJSON file. Expected 'Feature', found '" + geomType + "'");
                }
            }
            //LOOP END_ARRAY ]
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'features', found '" + firstParam + "'");
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
        if (coordinatesField.equalsIgnoreCase("coordinates")) {
            jp.nextToken(); // START_ARRAY [ to parse the coordinate
            Point point = GF.createPoint(parseCoordinate(jp));
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
        if (coordinatesField.equalsIgnoreCase("coordinates")) {
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
        if (coordinatesField.equalsIgnoreCase("coordinates")) {
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
        if (coordinatesField.equalsIgnoreCase("coordinates")) {
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
        if (coordinatesField.equalsIgnoreCase("coordinates")) {
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
        if (coordinatesField.equalsIgnoreCase("coordinates")) {
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
     * [101.0, 0.0], [102.0, 1.0] ] } ]
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
        if (coordinatesField.equalsIgnoreCase("geometries")) {
            jp.nextToken();//START array
            jp.nextToken();//START object
            ArrayList<Geometry> geometries = new ArrayList<Geometry>();
            while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                geometries.add(parseGeometry(jp));
                jp.nextToken();
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

    /**
     * Parses the GeoJSON data and set the values to the table.
     *
     * @throws IOException
     * @throws SQLException
     */
    private void parseData() throws IOException, SQLException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            JsonParser jp = jsFactory.createParser(fis);

            jp.nextToken();//START_OBJECT
            jp.nextToken(); // field_name (type)
            jp.nextToken(); // value_string (FeatureCollection)
            String geomType = jp.getText();

            if (geomType.equalsIgnoreCase("featurecollection")) {
                parseFeatures(jp);
            } else {
                throw new SQLException("Malformed GeoJSON file. Expected 'FeatureCollection', found '" + geomType + "'");
            }
            jp.close();
        } catch (FileNotFoundException ex) {
            throw new SQLException(ex);

        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                throw new SQLException(ex);
            }
        }
    }
}
