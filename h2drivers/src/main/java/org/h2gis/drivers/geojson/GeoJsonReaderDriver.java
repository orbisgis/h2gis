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
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ProgressVisitor;

/**
 *
 * @author Erwan Bocher
 */
public class GeoJsonReaderDriver {

    private final String tableName;
    private final File fileName;
    private final Connection connection;
    private GeometryFactory GF = new GeometryFactory();
    private PreparedStatement preparedStatement = null;
    private StringBuilder metadataBuilder;
    private JsonFactory jsFactory;
    int fieldCount = 0;

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
            throw new SQLException("Create the table " + tableName + " to import the geojson data");
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
        try {
            fis = new FileInputStream(fileName);

            parseMetadata();
            JsonParser jp = jsFactory.createParser(fis);
            metadataBuilder = new StringBuilder("CREATE TABLE ");
            metadataBuilder.append(tableName);


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
                                metadataBuilder.append("THE_GEOM GEOMETRY");
                                fieldCount++;
                                hasGeometryField = true;
                                jp.nextToken();//END_OBJECT } geometry
                            } else if (firstField.equalsIgnoreCase("properties")) {
                                parseMetadataProperties(jp);
                            }
                            jp.nextToken(); // field name
                            String secondParam = jp.getText();
                            if (secondParam.equalsIgnoreCase("geometry")) {
                                metadataBuilder.append("THE_GEOM GEOMETRY");
                                fieldCount++;
                                hasGeometryField = true;
                                jp.nextToken();//END_OBJECT } geometry;
                            } else if (secondParam.equalsIgnoreCase("properties")) {
                                parseMetadataProperties(jp);
                            }
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

        //Now we create the table
        if (hasGeometryField) {
            Statement stmt = connection.createStatement();
            stmt.execute(metadataBuilder.toString());
            stmt.close();
            //We return the preparedstatement of the waypoints table
            if (fieldCount > 0) {
                StringBuilder insert = new StringBuilder("INSERT INTO ").append(tableName).append(" VALUES ( ?");
                for (int i = 1; i < fieldCount; i++) {
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
     * Parses the metadata properties
     *
     * @param jp
     */
    private void parseMetadataProperties(JsonParser jp) throws IOException {
        jp.nextToken();//START_OBJECT {
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getText(); //FIELD_NAME columnName            
            JsonToken value = jp.nextToken();
            if (value == JsonToken.VALUE_STRING) {
                metadataBuilder.append(fieldName).append(" TEXT");
            } else if (value == JsonToken.VALUE_TRUE) {
                metadataBuilder.append(fieldName).append(" BOOLEAN");
            } else if (value == JsonToken.VALUE_FALSE) {
                metadataBuilder.append(fieldName).append(" BOOLEAN");
            } else if (value == JsonToken.VALUE_NUMBER_FLOAT) {
                metadataBuilder.append(fieldName).append(" DOUBLE");
            } else if (value == JsonToken.VALUE_NUMBER_INT) {
                metadataBuilder.append(fieldName).append(" INT");
            } else if (value == JsonToken.VALUE_NULL) {
                metadataBuilder.append(fieldName).append(" TEXT");
            } else {
                //TODO ignore value
            }
            metadataBuilder.append(",");
            fieldCount++;
        }
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
        if (firstField.equalsIgnoreCase("geometry")) {
            parseGeometry(jp);
            jp.nextToken();//END_OBJECT } geometry
        } else if (firstField.equalsIgnoreCase("properties")) {
            parseProperties(jp);
        }

        jp.nextToken(); // field name
        String secondParam = jp.getText();

        if (secondParam.equalsIgnoreCase("geometry")) {
            System.out.println("geometry found");
            parseGeometry(jp);
            jp.nextToken();//END_OBJECT } geometry;
        } else if (secondParam.equalsIgnoreCase("properties")) {
            parseProperties(jp);
        }

    }

    /**
     * Parses a geometry
     *
     * Syntax :
     *
     * "geometry":{"type": "Point", "coordinates": [102.0,0.5]}
     *
     * @param jsParser
     * @throws IOException
     */
    private void parseGeometry(JsonParser jsParser) throws IOException, SQLException {
        jsParser.nextToken(); //START_OBJECT {
        jsParser.nextToken(); // FIELD_NAME type     
        jsParser.nextToken(); //VALUE_STRING Point
        String geomType = jsParser.getText();
        if (geomType.equalsIgnoreCase("point")) {
            parsePoint(jsParser);
        } else if (geomType.equalsIgnoreCase("linestring")) {
            parseLinestring(jsParser);
        } else {
            System.out.println(geomType);
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
    private void parsePoint(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase("coordinates")) {
            GF.createPoint(parseCoordinate(jp));
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
    private void parseProperties(JsonParser jp) throws IOException, SQLException {
        jp.nextToken();//START_OBJECT {
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getText(); //FIELD_NAME columnName            
            JsonToken value = jp.nextToken();
            if (value == JsonToken.VALUE_STRING) {
                System.out.println(fieldName + " : " + jp.getText());
            } else if (value == JsonToken.VALUE_TRUE) {
                System.out.println(fieldName + " : " + jp.getValueAsBoolean());
            } else if (value == JsonToken.VALUE_FALSE) {
                System.out.println(fieldName + " : " + jp.getValueAsBoolean());
            } else if (value == JsonToken.VALUE_NUMBER_FLOAT) {
                System.out.println(fieldName + " : " + jp.getValueAsDouble());
            } else if (value == JsonToken.VALUE_NUMBER_INT) {
                System.out.println(fieldName + " : " + jp.getValueAsInt());
            } else if (value == JsonToken.VALUE_NULL) {
                System.out.println("Null value");
            } else {
                //TODO ignore value
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
    private void parseFeatureCollection(JsonParser jp) throws IOException, SQLException {
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
                    jp.nextToken(); //END_OBJECT } feature
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
    private void parseLinestring(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase("coordinates")) {
            jp.nextToken(); // START_ARRAY [
            ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                coords.add(parseCoordinate(jp));
            }
            GF.createLineString(coords.toArray(new Coordinate[coords.size()]));
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

            if (geomType.equalsIgnoreCase("featurecollection")) {
                parseFeatureCollection(jp);
            } else {
                throw new SQLException("Malformed geojson file. Expected 'FeatureCollection', found '" + geomType + "'");
            }
            jp.close();

            if (connection != null) {
                // Read table content
                Statement st = connection.createStatement();
                try {
                } finally {
                    st.close();
                }
            }
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
