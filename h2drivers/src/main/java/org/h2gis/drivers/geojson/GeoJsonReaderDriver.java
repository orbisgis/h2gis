/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.drivers.geojson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ProgressVisitor;

/**
 * This driver import a geojson file into a spatial table.
 *
 * Supported geometries are Point, LineString, Polygon and GeometryCollection.
 *
 * The driver requires that all Feature objects in a collection must have the
 * same schema of properties.
 *
 * To build the table schema the first feature of the FeatureCollection is
 * parsed.
 *
 * If the geojson format does not contain any properties a default primary key
 * will be added.
 *
 * @author Erwan Bocher
 */
public class GeoJsonReaderDriver {

    private final String tableName;
    private final File fileName;
    private final Connection connection;
    private GeometryFactory GF = new GeometryFactory();
    private PreparedStatement preparedStatement = null;
    private JsonFactory jsFactory;

    public GeoJsonReaderDriver(Connection connection, String tableName, File fileName) {
        this.connection = connection;
        this.tableName = tableName;
        this.fileName = fileName;
    }

    /**
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
     * Parses a GeoJson 1.0 file and writes it into a table.
     *
     * A GeoJson is structured as
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
     * Note : To include information on the coordinate range for geometries a
     * GeoJSON object may have a member named "bbox".
     *
     * Syntax :
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
            throw new SQLException("Cannot create the table " + tableName + " to import the geojson data");
        }
    }

    /**
     * Parses the first geojson feature to create the preparedstatment.
     *
     * @throws SQLException
     * @throws IOException
     */
    private boolean parseMetadata() throws SQLException, IOException {
        FileInputStream fis = null;
        boolean hasGeometryField = false;
        boolean hasProperties = false;
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
                            //If there is only one geometry field in the feature them the next
                            //token corresponds to the end object of the feature
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
                            //if(!hasProperties){
                            //    metadataBuilder.append("ID INT, PRIMARY KEY (ID)");
                            //}
                            metadataBuilder.append(")");
                        } else {
                            throw new SQLException("Malformed geojson file. Expected 'Feature', found '" + geomType + "'");
                        }
                    }
                } else {
                    throw new SQLException("Malformed geojson file. Expected 'features', found '" + firstParam + "'");
                }
            } else {
                throw new SQLException("Malformed geojson file. Expected 'FeatureCollection', found '" + geomType + "'");
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

        //Now we create the table if there is at leat one geometry field
        if (hasGeometryField) {
            Statement stmt = connection.createStatement();
            stmt.execute(metadataBuilder.toString());
            stmt.close();
            //We return the preparedstatement of the waypoints table
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
     * Parses geometry metadata
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
                || geomType.equalsIgnoreCase("multipolygon")
                || geomType.equalsIgnoreCase("geometrycollection")) {
            jp.nextToken(); // FIELD_NAME coordinates
            if (jp.getText().equalsIgnoreCase("coordinates")) {
                jp.nextToken();//START coordinates array
                jp.skipChildren();
                //jp.nextToken();//End coordinates array
                metadataBuilder.append("THE_GEOM GEOMETRY,");
            } else {
                throw new SQLException("Malformed geojson file. Expected 'coordinates', found '" + jp.getText() + "'");
            }
        } else {
            throw new SQLException("Unsupported geometry : " + geomType);
        }
    }

    /**
     * Parses the metadata properties
     *
     * @param jp
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
                //TODO ignore value
            }
            metadataBuilder.append(",");
        }
        return fieldIndex;
    }

    /**
     * Creates the JsonFactory
     */
    private void init() {
        jsFactory = new JsonFactory();
        jsFactory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        jsFactory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        jsFactory.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
    }

    /**
     * Get the preparedStatement to set the values to the table
     *
     * @return
     */
    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    /**
     * Syntax :
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
            getPreparedStatement().setObject(fieldIndex, parseGeometry(jp));
            fieldIndex++;
            jp.nextToken();//END_OBJECT } geometry
        } else if (firstField.equalsIgnoreCase("properties")) {
            parseProperties(jp, fieldIndex);
        }
        //If there is only one geometry field in the feature them the next
        //token corresponds to the end object of the feature
        jp.nextToken();
        if (jp.getCurrentToken() != JsonToken.END_OBJECT) {
            String secondParam = jp.getText();// field name
            if (secondParam.equalsIgnoreCase("geometry")) {
                getPreparedStatement().setObject(fieldIndex, parseGeometry(jp));
                fieldIndex++;
                jp.nextToken();//END_OBJECT } geometry;
            } else if (secondParam.equalsIgnoreCase("properties")) {
                parseProperties(jp, fieldIndex);
            }
            jp.nextToken(); //END_OBJECT } feature
        }
        getPreparedStatement().execute();
    }

    /**
     * Parses a geojson geometry and returns its JTS representation.
     *
     * Syntax :
     *
     * "geometry":{"type": "Point", "coordinates": [102.0,0.5]}
     *
     * @param jsParser
     * @throws IOException
     * @return Geometry
     */
    private Geometry parseGeometry(JsonParser jsParser) throws IOException, SQLException {
        jsParser.nextToken(); //START_OBJECT {
        jsParser.nextToken(); // FIELD_NAME type     
        jsParser.nextToken(); //VALUE_STRING Point
        String geomType = jsParser.getText();
        if (geomType.equalsIgnoreCase("point")) {
            return parsePoint(jsParser);
        } else if (geomType.equalsIgnoreCase("linestring")) {
            return parseLinestring(jsParser);
        } else {
            throw new SQLException("Unsupported geometry : " + geomType);
        }

    }

    /**
     * Parses one position
     *
     * Syntax :
     *
     * { "type": "Point", "coordinates": [100.0, 0.0] }
     *
     * @param jsParser
     * @throws IOException
     */
    private Point parsePoint(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase("coordinates")) {
            return GF.createPoint(parseCoordinate(jp));
        } else {
            throw new SQLException("Malformed geojson file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }

    /**
     * Parses the properties of a feature
     *
     * Syntax :
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
                } else {
                    throw new SQLException("Malformed geojson file. Expected 'Feature', found '" + geomType + "'");
                }
            }
            //LOOP END_ARRAY ]
        } else {
            throw new SQLException("Malformed geojson file. Expected 'features', found '" + firstParam + "'");
        }
    }

    /**
     *
     * Parse the array of positions.
     *
     * Syntax :
     *
     * { "type": "LineString", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param jsParser
     */
    private LineString parseLinestring(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase("coordinates")) {
            jp.nextToken(); // START_ARRAY [
            ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                coords.add(parseCoordinate(jp));
            }
            return GF.createLineString(coords.toArray(new Coordinate[coords.size()]));
        } else {
            throw new SQLException("Malformed geojson file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }

    /**
     * Parses a geojson coordinate array and returns a JTS coordinate
     *
     * Syntax :
     *
     * [100.0, 0.0]
     *
     */
    private Coordinate parseCoordinate(JsonParser jp) throws IOException {
        jp.nextToken(); // START_ARRAY [
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
        return coord;
    }

    /**
     * Parses the geojson data and set the values to the table
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

            //TODO take into account crs as
            /**
             * "type": "FeatureCollection", "crs": { "type": "name",
             * "properties": { "name": "EPSG:4326" } }, features:
             */
            if (geomType.equalsIgnoreCase("featurecollection")) {
                parseFeatures(jp);
            } else {
                throw new SQLException("Malformed geojson file. Expected 'FeatureCollection', found '" + geomType + "'");
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

    public static void main(String[] args) throws Exception {
        String jsonFile = "/tmp/points_properties.geojson";
        GeoJsonReaderDriver geoJsonReaderDriver = new GeoJsonReaderDriver(null, "points", new File(jsonFile));
        geoJsonReaderDriver.read(new EmptyProgressVisitor());
    }
}
