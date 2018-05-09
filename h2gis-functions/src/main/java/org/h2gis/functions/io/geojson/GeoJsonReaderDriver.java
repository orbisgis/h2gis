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

import com.fasterxml.jackson.core.JsonFactory;
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
import org.locationtech.jts.geom.PrecisionModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.h2gis.functions.io.utility.FileUtil;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Hai Trung Pham
 */
public class GeoJsonReaderDriver {
    private final static ArrayList<String> geomTypes;    
    private final File fileName;
    private final Connection connection;
    private static GeometryFactory GF ;
    private PreparedStatement preparedStatement = null;
    private JsonFactory jsFactory;
    private int featureCounter = 1;
    private ProgressVisitor progress = new EmptyProgressVisitor();
    private FileChannel fc;
    private long fileSize = 0;    
    private long readFileSizeEachNode = 1;
    private long nodeCountProgress = 0;
    // For progression information return
    private static final int AVERAGE_NODE_SIZE = 500;
    boolean hasGeometryField = false;
    private static final Logger log = LoggerFactory.getLogger(GeoJsonReaderDriver.class);
    private int parsedSRID =0;
    private boolean isH2;
    private TableLocation tableLocation;
    private Map<String, String> cachedColumnNames;
    private Map<String, Integer> cachedColumnIndex;
    private static final int BATCH_MAX_SIZE = 100;
    
    static {
        geomTypes = new ArrayList<String>();
        geomTypes.add(GeoJsonField.POINT);
        geomTypes.add(GeoJsonField.MULTIPOINT);
        geomTypes.add(GeoJsonField.LINESTRING);
        geomTypes.add(GeoJsonField.MULTILINESTRING);
        geomTypes.add(GeoJsonField.POLYGON);
        geomTypes.add(GeoJsonField.MULTIPOLYGON);

    }
    private Set finalGeometryTypes;

    /**
     * Driver to import a GeoJSON file into a spatial table.
     *
     * @param connection
     * @param fileName
     */
    public GeoJsonReaderDriver(Connection connection, File fileName) {
        this.connection = connection;
        this.fileName = fileName;
    }

    /**
     * Read the GeoJSON file.
     *
     * @param progress
     * @param tableReference
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public void read(ProgressVisitor progress, String tableReference) throws SQLException, IOException {
        if (FileUtil.isFileImportable(fileName, "geojson")) {
            this.isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
            this.tableLocation = TableLocation.parse(tableReference, isH2);
            if (fileName.length() > 0) {
                parseGeoJson(progress);
            } else {
                JDBCUtilities.createEmptyTable(connection, tableLocation.toString());
            }
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
        this.progress = progress.subProcess(100);
        init();
        if (parseMetadata()) {
            GF = new GeometryFactory(new PrecisionModel(), parsedSRID);
            parseData();
            setGeometryTypeConstraints();
        } else {
            throw new SQLException("Cannot create the table " + tableLocation + " to import the GeoJSON data");
        }
    }
    
    
    /**
     * Parses the all GeoJSON feature to create the PreparedStatement.
     *
     * @throws SQLException
     * @throws IOException
     */
    private boolean parseMetadata() throws SQLException, IOException {
        FileInputStream fis = null;        
        try {
            fis = new FileInputStream(fileName);
            this.fc = fis.getChannel();
            this.fileSize = fc.size();           
                
            // Given the file size and an average node file size.
            // Skip how many nodes in order to update progression at a step of 1%
            readFileSizeEachNode = Math.max(1, (this.fileSize / AVERAGE_NODE_SIZE) / 100);
            nodeCountProgress = 0;
            cachedColumnNames = new LinkedHashMap<String, String>();
            finalGeometryTypes=new HashSet<String>();
            
            JsonParser jp = jsFactory.createParser(fis);           

            jp.nextToken();//START_OBJECT
            jp.nextToken(); // field_name (type)
            jp.nextToken(); // value_string (FeatureCollection)
            String geomType = jp.getText();      
            
            if (geomType.equalsIgnoreCase(GeoJsonField.FEATURECOLLECTION)) {                
                parseFeaturesMetadata(jp);
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
         // Now we create the table if there is at least one geometry field.          
         if (hasGeometryField) {              
            StringBuilder createTable = new StringBuilder();
            createTable.append("CREATE TABLE ");
            createTable.append(tableLocation);
            createTable.append(" (");
            
             //Add the geometry column
             if (isH2) {
                 createTable.append("THE_GEOM GEOMETRY");
             } else {
                 createTable.append("THE_GEOM GEOMETRY(geometry,").append(parsedSRID).append(")");
             }
            
             cachedColumnIndex = new HashMap<String, Integer>();
             StringBuilder insertTable = new StringBuilder("INSERT INTO ");
             insertTable.append(tableLocation).append(" VALUES(?");
             int i =1;
             for (Map.Entry<String, String> columns : cachedColumnNames.entrySet()) {
                 String columnName = columns.getKey();
                 cachedColumnIndex.put(columnName, i++);
                 createTable.append(",").append(columns.getKey()).append(" ").append(columns.getValue());
                 insertTable.append(",").append("?");
             }
             createTable.append(")");
             insertTable.append(")");

             try (Statement stmt = connection.createStatement()) {
                 stmt.execute(createTable.toString());
             }
             preparedStatement = connection.prepareStatement(insertTable.toString());
             return true;

        } else {
            throw new SQLException("The geojson file  does not contain any geometry.");
        }
         
    }
    
    /**
     * Parses the featureCollection to collect the field properties
     *
     * @param jp
     * @throws IOException
     * @throws SQLException
     */
    private void parseFeaturesMetadata(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME features
        // Passes all the properties until "Feature" object is found
        while(!jp.getText().equalsIgnoreCase(GeoJsonField.FEATURES) &&
                !jp.getText().equalsIgnoreCase(GeoJsonField.CRS)){
            jp.nextToken();
            if(jp.getCurrentToken().equals(JsonToken.START_ARRAY) || jp.getCurrentToken().equals(JsonToken.START_OBJECT)){
                jp.skipChildren();
            }
            jp.nextToken();
        }
        if(jp.getText().equalsIgnoreCase(GeoJsonField.CRS)){
            parsedSRID = readCRS(jp);
        }
        if (jp.getText().equalsIgnoreCase(GeoJsonField.FEATURES)) {
            jp.nextToken(); // START_ARRAY [
            JsonToken token = jp.nextToken(); // START_OBJECT {
            while (token != JsonToken.END_ARRAY) {
                jp.nextToken(); // FIELD_NAME type
                jp.nextToken(); // VALUE_STRING Feature
                String geomType = jp.getText();
                if (geomType.equalsIgnoreCase(GeoJsonField.FEATURE)) {         
                    if (progress.isCanceled()) {
                        throw new SQLException("Canceled by user");
                    }
                    parseFeatureMetadata(jp);
                    token = jp.nextToken(); //START_OBJECT new feature                   
                    featureCounter++;
                    
                    if (nodeCountProgress++ % readFileSizeEachNode == 0) {
                        // Update Progress
                        try {
                            progress.setStep((int) (((double) fc.position() / fileSize) * 100));
                        } catch (IOException ex) {
                            // Ignore
                        }
                    }
                } else {
                    throw new SQLException("Malformed GeoJSON file. Expected 'Feature', found '" + geomType + "'");
                }
            }
            //LOOP END_ARRAY ]
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'features', found '" + jp.getText() + "'");
        }
    }
    
    /**
     * Features in GeoJSON contain a geometry object and additional properties
     * This method is used to collect metadata
     *
     * Syntax:
     *
     * { "type": "Feature", "geometry":{"type": "Point", "coordinates": [102.0,
     * 0.5]}, "properties": {"prop0": "value0"} }
     *
     * @param jp
     */
    private void parseFeatureMetadata(JsonParser jp) throws IOException, SQLException {
        jp.nextToken();
        String field = jp.getText();
        //Avoid all token which are not 'properties', 'geometry', 'type'
        while (!field.equalsIgnoreCase(GeoJsonField.GEOMETRY) &&
                !field.equalsIgnoreCase(GeoJsonField.PROPERTIES) &&
                !jp.getCurrentToken().equals(JsonToken.END_OBJECT)){
            jp.nextToken();
            if(jp.getCurrentToken().equals(JsonToken.START_ARRAY) || jp.getCurrentToken().equals(JsonToken.START_OBJECT)){
                jp.skipChildren();
            }
            jp.nextToken();
            field = jp.getText();
        }
         // FIELD_NAME geometry
        if (field.equalsIgnoreCase(GeoJsonField.GEOMETRY)) {
            parseParentGeometryMetadata(jp);
            hasGeometryField = true;
            jp.nextToken();
        } else if (field.equalsIgnoreCase(GeoJsonField.PROPERTIES)) {
            parsePropertiesMetadata(jp);
            jp.nextToken();
        }
        //If there is only one geometry field in the feature them the next
        //token corresponds to the end object of the feature

        //Avoid all token which are not 'properties', 'geometry', 'type'
        field = jp.getText();
        while (!field.equalsIgnoreCase(GeoJsonField.GEOMETRY) &&
                !field.equalsIgnoreCase(GeoJsonField.PROPERTIES) &&
                !jp.getCurrentToken().equals(JsonToken.END_OBJECT)){
            jp.nextToken();
            if(jp.getCurrentToken().equals(JsonToken.START_ARRAY) || jp.getCurrentToken().equals(JsonToken.START_OBJECT)){
                jp.skipChildren();
            }
            jp.nextToken();
            field = jp.getText();
        }
        if (jp.getCurrentToken() != JsonToken.END_OBJECT) {
            String secondParam = jp.getText();// field name
            if (secondParam.equalsIgnoreCase(GeoJsonField.GEOMETRY)) {
                parseParentGeometryMetadata(jp);
                hasGeometryField = true;
            } else if (secondParam.equalsIgnoreCase(GeoJsonField.PROPERTIES)) {
                parsePropertiesMetadata(jp);
            }
            while (jp.nextToken() != JsonToken.END_OBJECT); //END_OBJECT } feature
        }
       
    }
    
    
     /**
     * Parses the geometries to return its properties
     * 
     * @param jp
     * @throws IOException
     * @throws SQLException 
     */
    private void parseParentGeometryMetadata(JsonParser jp) throws IOException, SQLException {
        if(jp.nextToken()!=JsonToken.VALUE_NULL){//START_OBJECT { in case of null geometry
        jp.nextToken(); // FIELD_NAME type     
        jp.nextToken(); //VALUE_STRING Point
        String geometryType = jp.getText();        
        parseGeometryMetadata(jp, geometryType);
        } 
        
    }
    
    /**
     * Parses a all type of geometries and check if the geojson is wellformed.
     *
     * Syntax:
     *
     * "geometry":{"type": "Point", "coordinates": [102.0,0.5]}
     *
     * @param jp
     * @throws IOException
     */
    private void parseGeometryMetadata(JsonParser jp, String geometryType) throws IOException, SQLException {        
        if (geometryType.equalsIgnoreCase(GeoJsonField.POINT)) {
             parsePointMetadata(jp);
             finalGeometryTypes.add(GeoJsonField.POINT);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.MULTIPOINT)) {
             parseMultiPointMetadata(jp);
             finalGeometryTypes.add(GeoJsonField.MULTIPOINT);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.LINESTRING)) {
             parseLinestringMetadata(jp);
             finalGeometryTypes.add(GeoJsonField.LINESTRING);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.MULTILINESTRING)) {
            parseMultiLinestringMetadata(jp);
            finalGeometryTypes.add(GeoJsonField.MULTILINESTRING);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.POLYGON)) {
             parsePolygonMetadata(jp);
             finalGeometryTypes.add(GeoJsonField.POLYGON);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.MULTIPOLYGON)) {
             parseMultiPolygonMetadata(jp);
             finalGeometryTypes.add(GeoJsonField.MULTIPOLYGON);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.GEOMETRYCOLLECTION)) {
             parseGeometryCollectionMetadata(jp);
             finalGeometryTypes.add(GeoJsonField.GEOMETRYCOLLECTION);
        } else {
            throw new SQLException("Unsupported geometry : " + geometryType);
        }
    }
    
    /**
     * Parses a point and check if it's wellformated
     *
     * Syntax:
     *
     * { "type": "Point", "coordinates": [100.0, 0.0] }
     *
     * @param jp
     * @throws IOException
     */
    private void parsePointMetadata(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            jp.nextToken(); // START_ARRAY [ to parse the coordinate
            parseCoordinateMetadata(jp);            
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }
    
    /**
     * Parses a MultiPoint and check if it's wellformated
     *
     * Syntax:
     *
     * { "type": "MultiPoint", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param jp
     * @throws IOException
     */
    private void parseMultiPointMetadata(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            jp.nextToken(); // START_ARRAY [ coordinates
            parseCoordinatesMetadata(jp);
            jp.nextToken();//END_OBJECT } geometry
            
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }
    
    
    /**
     *
     * Parses a LineString and check if it's wellformated
     *
     * Syntax:
     *
     * { "type": "LineString", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param jp
     */
    private void parseLinestringMetadata(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            jp.nextToken(); // START_ARRAY [ coordinates
            parseCoordinatesMetadata(jp);
            jp.nextToken();//END_OBJECT } geometry            
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }
    
    /**
     * Parses MultiLineString defined as:
     *
     * { "type": "MultiLineString", "coordinates": [ [ [100.0, 0.0], [101.0,
     * 1.0] ], [ [102.0, 2.0], [103.0, 3.0] ] ] }
     *
     * @param jp
     */
    private void parseMultiLinestringMetadata(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            jp.nextToken();//START_ARRAY [ coordinates
            jp.nextToken(); // START_ARRAY [ coordinates line
            while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                parseCoordinatesMetadata(jp);
                jp.nextToken();
            }
            jp.nextToken();//END_OBJECT } geometry           
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }

    }
    
    /**
     * Parses a Polygon as an array of LinearRing coordinate arrays.
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
     */
    private void parsePolygonMetadata(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            jp.nextToken(); // START_ARRAY [ coordinates
            jp.nextToken(); //Start the RING
            int linesIndex = 0;
            while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                if (linesIndex == 0) {
                    parseCoordinatesMetadata(jp);
                } else {
                    parseCoordinatesMetadata(jp);
                }
                jp.nextToken();//END RING
                linesIndex++;
            }
            if (linesIndex > 1) {
                jp.nextToken();//END_OBJECT } geometry                
            } else {
                jp.nextToken();//END_OBJECT } geometry
            }
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }
    
    /**
     * Parses a MultiPolygon as an array of Polygon coordinate arrays:
     *
     * { "type": "MultiPolygon", "coordinates": [ [[[102.0, 2.0], [103.0, 2.0],
     * [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]], [[[100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]], [[100.2, 0.2], [100.8, 0.2],
     * [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]] ] }
     *
     * @param jp
     * @throws IOException
     * @throws SQLException
     */
    private void parseMultiPolygonMetadata(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
            jp.nextToken(); // START_ARRAY [ coordinates             
            jp.nextToken(); //Start the polygon
            while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                //Parses the polygon
                jp.nextToken(); //Start the RING
                while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                    parseCoordinatesMetadata(jp);
                    jp.nextToken();//END RING
                }
                jp.nextToken();//END_OBJECT
            }
            jp.nextToken();//END_OBJECT } geometry

        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'coordinates', found '" + coordinatesField + "'");
        }
    }
    
    /**
     * Parses a GeometryCollection
     * the geometry objects are described above:
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
    private void parseGeometryCollectionMetadata(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME geometries        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.GEOMETRIES)) {
            jp.nextToken();//START array
            jp.nextToken();//START object
            while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                jp.nextToken(); // FIELD_NAME type     
                jp.nextToken(); //VALUE_STRING Point
                String geometryType = jp.getText();
                parseGeometryMetadata(jp, geometryType);
                jp.nextToken();
            }
            jp.nextToken();//END_OBJECT } geometry            
        } else {
            throw new SQLException("Malformed GeoJSON file. Expected 'geometries', found '" + coordinatesField + "'");
        }

    }
    
    /**
     * Parses a GeoJSON coordinate array and check if it's wellformed. The first
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
    private void parseCoordinateMetadata(JsonParser jp) throws IOException {
        jp.nextToken();
        jp.nextToken(); // second value
        //We look for a z value
        jp.nextToken();
        if (jp.getCurrentToken() != JsonToken.END_ARRAY) {
            jp.nextToken(); // exit array
        }
        jp.nextToken();
    }
    
    /**
     * Parses a sequence of coordinates array expressed as
     *
     * [ [100.0, 0.0], [101.0, 1.0] ]
     * 
     * and check if it's wellformated
     *
     * @param jp
     * @throws IOException
     * @throws SQLException
     * @return Coordinate[]
     */
    private void parseCoordinatesMetadata(JsonParser jp) throws IOException {
        jp.nextToken(); // START_ARRAY [ to parse the each positions        
        while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
            parseCoordinateMetadata(jp);
        }
    }
   
    
    /**
     * Parses the properties of a feature
     *
     * Syntax:
     *
     * "properties": {"prop0": "value0"}
     *
     * @param jp
     */
    private void parsePropertiesMetadata(JsonParser jp) throws IOException, SQLException {
        jp.nextToken();//START_OBJECT {
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = TableLocation.quoteIdentifier(jp.getText().toUpperCase(), isH2); //FIELD_NAME columnName 
            JsonToken value = jp.nextToken();
            if (null != value) switch (value) {
                case VALUE_STRING:
                    cachedColumnNames.put(fieldName, "VARCHAR");
                    break;
                case VALUE_TRUE:
                case VALUE_FALSE:
                    cachedColumnNames.put(fieldName, "BOOLEAN");
                    break;
                case VALUE_NUMBER_FLOAT:
                    cachedColumnNames.put(fieldName, "DOUBLE PRECISION");
                    break;
                case VALUE_NUMBER_INT:
                    cachedColumnNames.put(fieldName, "BIGINT");
                    break;
                case START_ARRAY:
                    cachedColumnNames.put(fieldName, "ARRAY");
                    parseArrayMetadata(jp);
                    break;
                case START_OBJECT:
                    cachedColumnNames.put(fieldName, "VARCHAR");
                    parseObjectMetadata(jp);
                    break;
                case VALUE_NULL:
                    cachedColumnNames.put(fieldName, "VARCHAR");
                //ignore other value
                default:
                    break;
            }
        }
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
     * Features in GeoJSON contain a geometry object and additional properties
     * This method returns all values stored in a feature.
     *
     * Syntax:
     *
     * { "type": "Feature", "geometry":{"type": "Point", "coordinates": [102.0,
     * 0.5]}, "properties": {"prop0": "value0"} }
     *
     * @param jp
     */
    private Object[] parseFeature(JsonParser jp) throws IOException, SQLException {
        jp.nextToken();
        String field = jp.getText();
        //Avoid all token which are not 'properties', 'geometry', 'type'
        while (!field.equalsIgnoreCase(GeoJsonField.GEOMETRY) &&
                !field.equalsIgnoreCase(GeoJsonField.PROPERTIES) &&
                !jp.getCurrentToken().equals(JsonToken.END_OBJECT)){
            jp.nextToken();
            if(jp.getCurrentToken().equals(JsonToken.START_ARRAY) || jp.getCurrentToken().equals(JsonToken.START_OBJECT)){
                jp.skipChildren();
            }
            jp.nextToken();
            field = jp.getText();
        }
        Object[] values= new Object[cachedColumnIndex.size()+1];
        if (field.equalsIgnoreCase(GeoJsonField.GEOMETRY)) {
            setGeometry(jp, values);
            jp.nextToken();
        } else if (field.equalsIgnoreCase(GeoJsonField.PROPERTIES)) {
            parseProperties(jp, values);
            jp.nextToken();
        }
        //If there is only one geometry field in the feature them the next
        //token corresponds to the end object of the feature

        //Avoid all token which are not 'properties', 'geometry', 'type'
        field = jp.getText();
        while (!field.equalsIgnoreCase(GeoJsonField.GEOMETRY) &&
                !field.equalsIgnoreCase(GeoJsonField.PROPERTIES) &&
                !jp.getCurrentToken().equals(JsonToken.END_OBJECT)){
            jp.nextToken();
            if(jp.getCurrentToken().equals(JsonToken.START_ARRAY) || jp.getCurrentToken().equals(JsonToken.START_OBJECT)){
                jp.skipChildren();
            }
            jp.nextToken();
            field = jp.getText();
        }
        if (jp.getCurrentToken() != JsonToken.END_OBJECT) {
            String secondParam = jp.getText();// field name
            if (secondParam.equalsIgnoreCase(GeoJsonField.GEOMETRY)) {
                setGeometry(jp, values);
            } else if (secondParam.equalsIgnoreCase(GeoJsonField.PROPERTIES)) {
                parseProperties(jp, values);
            }
            while (jp.nextToken() != JsonToken.END_OBJECT); //END_OBJECT } feature
        }
        
        return values;
    }
    
    /**
     * Sets the parsed geometry to the table     *
     * 
     * @param jp
     * @throws IOException
     * @throws SQLException 
     */
    private void setGeometry(JsonParser jp, Object[] values) throws IOException, SQLException {
        if(jp.nextToken()!=JsonToken.VALUE_NULL){//START_OBJECT { in case of null geometry
        jp.nextToken(); // FIELD_NAME type     
        jp.nextToken(); //VALUE_STRING Point
        String geometryType = jp.getText();
        values[0] = parseGeometry(jp, geometryType);
        } 
    }

    /**
     * Parses a GeoJSON geometry and returns its JTS representation.
     *
     * Syntax:
     *
     * "geometry":{"type": "Point", "coordinates": [102.0,0.5]}
     *
     * @param jp
     * @throws IOException
     * @return Geometry
     */
    private Geometry parseGeometry(JsonParser jp, String geometryType) throws IOException, SQLException {        
        if (geometryType.equalsIgnoreCase(GeoJsonField.POINT)) {
            return parsePoint(jp);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.MULTIPOINT)) {
            return parseMultiPoint(jp);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.LINESTRING)) {
            return parseLinestring(jp);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.MULTILINESTRING)) {
            return parseMultiLinestring(jp);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.POLYGON)) {
            return parsePolygon(jp);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.MULTIPOLYGON)) {
            return parseMultiPolygon(jp);
        } else if (geometryType.equalsIgnoreCase(GeoJsonField.GEOMETRYCOLLECTION)) {
            return parseGeometryCollection(jp);
        } else {
            throw new SQLException("Unsupported geometry : " + geometryType);
        }
    }

    /**
     * Parses the properties of a feature
     *
     * Syntax:
     *
     * "properties": {"prop0": "value0"}
     *
     * @param jp
     */
    private void parseProperties(JsonParser jp, Object[] values) throws IOException, SQLException {
        jp.nextToken();//START_OBJECT {
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = TableLocation.quoteIdentifier(jp.getText().toUpperCase(), isH2); //FIELD_NAME columnName 
            JsonToken value = jp.nextToken();
            if (value == JsonToken.VALUE_STRING) {                
                values[cachedColumnIndex.get(fieldName)] =  jp.getText();
            } else if (value == JsonToken.VALUE_TRUE) {
                values[cachedColumnIndex.get(fieldName)] =  jp.getValueAsBoolean();
            } else if (value == JsonToken.VALUE_FALSE) {
                values[cachedColumnIndex.get(fieldName)] =  jp.getValueAsBoolean();
            } else if (value == JsonToken.VALUE_NUMBER_FLOAT) {
                values[cachedColumnIndex.get(fieldName)] =  jp.getValueAsDouble();
            } else if (value == JsonToken.VALUE_NUMBER_INT) {
                values[cachedColumnIndex.get(fieldName)] =  jp.getBigIntegerValue();
            } else if (value == JsonToken.START_ARRAY) {
                ArrayList<Object> arrayList = parseArray(jp);
                values[cachedColumnIndex.get(fieldName)] = arrayList.toArray();
            } else if (value == JsonToken.START_OBJECT) {
                String str = parseObject(jp);
                values[cachedColumnIndex.get(fieldName)] = str;
            } else if (value == JsonToken.VALUE_NULL) {
                values[cachedColumnIndex.get(fieldName)] = "null";
            }
            else {
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
        // Passes all the properties until "Feature" object is found
        while(!jp.getText().equalsIgnoreCase(GeoJsonField.FEATURES) &&
                !jp.getText().equalsIgnoreCase(GeoJsonField.CRS)){
            jp.nextToken();
            if(jp.getCurrentToken().equals(JsonToken.START_ARRAY) || jp.getCurrentToken().equals(JsonToken.START_OBJECT)){
                jp.skipChildren();
            }
            jp.nextToken();
        }
        String firstParam = jp.getText();
        if(firstParam.equalsIgnoreCase(GeoJsonField.CRS)){
            firstParam = skipCRS(jp);
        }
        if (firstParam.equalsIgnoreCase(GeoJsonField.FEATURES)) {
            jp.nextToken(); // START_ARRAY [
            JsonToken token = jp.nextToken(); // START_OBJECT {
            long batchSize = 0;
            while (token != JsonToken.END_ARRAY) {
                jp.nextToken(); // FIELD_NAME type
                jp.nextToken(); // VALUE_STRING Feature
                String geomType = jp.getText();
                if (geomType.equalsIgnoreCase(GeoJsonField.FEATURE)) {         
                    if (progress.isCanceled()) {
                        throw new SQLException("Canceled by user");
                    }
                    Object[] values = parseFeature(jp);
                    
                    for (int i = 0; i < values.length; i++) {
                        preparedStatement.setObject(i+1, values[i]);                        
                    }
                    
                    preparedStatement.addBatch();
                    batchSize++;
                    if (batchSize >= BATCH_MAX_SIZE) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        batchSize = 0;
                    }
                    
                    token = jp.nextToken(); //START_OBJECT new feature                    
                    featureCounter++;
                    if (nodeCountProgress++ % readFileSizeEachNode == 0) {
                        // Update Progress
                        try {
                            progress.setStep((int) (((double) fc.position() / fileSize) * 100));
                        } catch (IOException ex) {
                            // Ignore
                        }
                    }
                    if (batchSize > 0) {
                        preparedStatement.executeBatch();
                    }
                } else {
                    throw new SQLException("Malformed GeoJSON file. Expected 'Feature', found '" + geomType + "'");
                }
            }
            //LOOP END_ARRAY ]
            log.info(featureCounter+ " geojson features have been imported.");
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
     * @param jp
     * @throws IOException
     * @return Point
     */
    private Point parsePoint(JsonParser jp) throws IOException, SQLException {
        jp.nextToken(); // FIELD_NAME coordinates        
        String coordinatesField = jp.getText();
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.COORDINATES)) {
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
     * @param jp
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
     * Parses the array of positions.
     *
     * Syntax:
     *
     * { "type": "LineString", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param jp
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
     * @param jp
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
                //Parses the polygon
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
        if (coordinatesField.equalsIgnoreCase(GeoJsonField.GEOMETRIES)) {
            jp.nextToken();//START array
            jp.nextToken();//START object
            ArrayList<Geometry> geometries = new ArrayList<Geometry>();
            while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
                jp.nextToken(); // FIELD_NAME type     
                jp.nextToken(); //VALUE_STRING Point
                String geometryType = jp.getText();
                geometries.add(parseGeometry(jp, geometryType));
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
            if (geomType.equalsIgnoreCase(GeoJsonField.FEATURECOLLECTION)) {                
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

    /**
     * Reads the CRS element and return the database SRID.
     * 
     * Parsed syntax:
     * 
     * "crs":{
     * "type":"name",
     * "properties":
     * {"name":"urn:ogc:def:crs:EPSG::4326"
     * }
     * }
     * 
     * @param jp
     * @return 
     */
    private int readCRS(JsonParser jp) throws IOException, SQLException {
        int srid = 0;
        jp.nextToken(); //START_OBJECT {
        jp.nextToken();// crs type
        jp.nextToken(); // crs name
        String firstField = jp.getText();
        if(firstField.equalsIgnoreCase(GeoJsonField.NAME)){
            jp.nextToken(); // crs properties
            jp.nextToken(); //START_OBJECT {
            jp.nextToken(); // crs name
            jp.nextToken(); // crs value
            String crsURI = jp.getText();
            String[] split = crsURI.toLowerCase().split(GeoJsonField.CRS_URN_EPSG);
            if (split != null) {
                srid = Integer.valueOf(split[1]);
            } else {
                log.warn("The CRS URN " + crsURI + " is not supported.");
            }
            jp.nextToken(); //END_OBJECT }
            jp.nextToken(); //END_OBJECT }
            jp.nextToken(); //Go to features
        }        
        else if (firstField.equalsIgnoreCase(GeoJsonField.LINK)) {
            log.warn("Linked CRS is not supported.");
            jp.nextToken();
            jp.nextToken();
            jp.nextToken(); //END_OBJECT }
            jp.nextToken(); //END_OBJECT }
            jp.nextToken(); //Go to features
        }
        else{
            throw new SQLException("Malformed GeoJSON CRS element.");
        }
        
        return srid;
    }

    /**
     * We skip the CRS because it has been already parsed.
     * 
     *
     * @param jp
     */
    private String skipCRS(JsonParser jp) throws IOException {
        jp.nextToken(); //START_OBJECT {
        jp.skipChildren();
        jp.nextToken(); //Go to features
        return jp.getText();
    }

     /**
     * Adds the geometry type constraint and the SRID
     */
    private void setGeometryTypeConstraints() throws SQLException {
        String finalGeometryType = GeoJsonField.GEOMETRY;
        if (finalGeometryTypes.size() == 1) {
            finalGeometryType = (String) finalGeometryTypes.iterator().next();
        }        
        if(isH2){
             finalGeometryType = GeoJsonField.GEOMETRY;//workaround for H2
             connection.createStatement().execute(String.format("ALTER TABLE %s ALTER COLUMN the_geom %s", tableLocation.toString(), finalGeometryType));        
        }
        else{
            connection.createStatement().execute(String.format("ALTER TABLE %s ALTER COLUMN the_geom SET DATA TYPE geometry(%s,%d)", tableLocation.toString(), finalGeometryType, parsedSRID));
        }
    }


    /**
     * Parses Json Array.
     * Syntax:
     * Json Array:
     * {"member1": value1}, value2, value3, {"member4": value4}]
     * @param jp the json parser
     * @return the array but written like a String
     */
    private void parseArrayMetadata(JsonParser jp) throws IOException {
        JsonToken value = jp.nextToken();
        while(value != JsonToken.END_ARRAY) {
            if (value == JsonToken.START_OBJECT) {
                parseObjectMetadata(jp);
            } else if (value == JsonToken.START_ARRAY) {
                parseArrayMetadata(jp);
            }
            value = jp.nextToken();
        }
    }

    /**
     * Parses Json Object.
     * Syntax:
     * Json Object:
     * "member1": value1, "member2": value2}
     * @param jp the json parser
     * @return the object but written like a String
     */
    private void parseObjectMetadata(JsonParser jp) throws IOException {
        JsonToken value;
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            value = jp.nextToken();
            if (value == JsonToken.START_OBJECT) {
                parseObjectMetadata(jp);
            } else if (value == JsonToken.START_ARRAY) {
                parseArrayMetadata(jp);
            }
        }
    }

    /**
     * Parses Json Array and returns an ArrayList
     * Syntax:
     * Json Array:
     * {"member1": value1}, value2, value3, {"member4": value4}]
     * @param jp the json parser
     * @return the array
     */
    private ArrayList<Object> parseArray(JsonParser jp) throws IOException {
        JsonToken value = jp.nextToken();
        ArrayList<Object> ret = new ArrayList<>();
        while(value != JsonToken.END_ARRAY) {
            if (value == JsonToken.START_OBJECT) {
                Object object = parseObject(jp);
                ret.add(object);
            } else if (value == JsonToken.START_ARRAY) {
                ArrayList<Object> arrayList = parseArray(jp);
                ret.add(arrayList.toArray());
            } else if (value == JsonToken.VALUE_NUMBER_INT) {
                ret.add(jp.getValueAsInt());
            } else if (value == JsonToken.VALUE_FALSE || value == JsonToken.VALUE_TRUE) {
                ret.add(jp.getValueAsBoolean());
            } else if (value == JsonToken.VALUE_NUMBER_FLOAT) {
                ret.add(jp.getValueAsDouble());
            } else if (value == JsonToken.VALUE_STRING) {
                ret.add(jp.getValueAsString());
            } else if (value == JsonToken.VALUE_NULL) {
                ret.add("null");
            }
            value = jp.nextToken();
        }
        return ret;
    }

    /**
     * Parses Json Object. Since their elements could be
     * anything and H2GIS doesn't support such complicated
     * structure, this parser will just write ordinary
     * String object "{}".
     * Syntax:
     * Json Object:
     * "member1": value1, "member2": value2}
     * @param jp the json parser
     * @return the object but written like a String
     */
    private String parseObject(JsonParser jp) throws IOException {
        String ret = "{";
        JsonToken value;
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            value = jp.nextToken();
            if (value == JsonToken.START_OBJECT) {
                parseObjectMetadata(jp);
            } else if (value == JsonToken.START_ARRAY) {
                parseArrayMetadata(jp);
            }
        }
        ret += "}";
        return ret;
    }

}
