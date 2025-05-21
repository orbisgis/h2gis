/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.io.geojson;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.*;

import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.h2gis.utilities.FileUtilities;

import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.Tuple;

/**
 * A simple GeoJSON driver to write a spatial table to a GeoJSON file.
 *
 * GeoJSON is a format for encoding a variety of geographic data structures. A
 * GeoJSON object may represent a geometry, a feature, or a collection of
 * features. GeoJSON supports the following geometry types: POINT, LINESTRING,
 * POLYGON, MULTIPOINT, MULTILINESTRING, MULTIPOLYGON, and GEOMETRYCOLLECTION.
 *
 * Syntax:
 *
 * { "type": "FeatureCollection", "features": [ { "type": "Feature",
 * "geometry":{"type": "Point", "coordinates": [102.0, 0.5]}, "properties":
 * {"prop0": "value0"} } ]}
 *
 * @author Erwan Bocher
 * @author Hai Trung Pham
 */
public class GeoJsonWriteDriver {

    private final Connection connection;
    private Map<String, String> cachedSpecificColumns;
    private LinkedHashMap<String, Integer> cachedColumnIndex;
    private int columnCountProperties = -1;

    /**
     * A simple GeoJSON driver to write a spatial table to a GeoJSON file.
     *
     * @param connection database connection
     */
    public GeoJsonWriteDriver(Connection connection) {
        this.connection = connection;
    }    

    /**
     * Write a resulset to a geojson file
     *
     * @param progress Progress visitor following the execution.
     * @param rs input resulset
     * @param fileName the output file
     * @param encoding file encoding
     * @param deleteFile true to delete the file if exist
     */
    public void write(ProgressVisitor progress, ResultSet rs, File fileName, String encoding, boolean deleteFile) throws SQLException, IOException {
        if (FileUtilities.isExtensionWellFormated(fileName, "geojson")|| FileUtilities.isExtensionWellFormated(fileName, "json")) {
            if (deleteFile) {
                Files.deleteIfExists(fileName.toPath());
            } else if (fileName.exists()) {
                throw new IOException("The geojson file already exist.");
            }
            geojsonWriter(progress, rs, new FileOutputStream(fileName), encoding);
        } else if (FileUtilities.isExtensionWellFormated(fileName, "gz")) {
            if (deleteFile) {
                Files.deleteIfExists(fileName.toPath());
            } else if (fileName.exists()) {
                throw new IOException("The gz file already exist.");
            }
            GZIPOutputStream gzos = null;
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                gzos = new GZIPOutputStream(fos);
                geojsonWriter(progress, rs, gzos, encoding);
            } finally {
                try {
                    if (gzos != null) {
                        gzos.close();
                    }
                } catch (IOException ex) {
                    throw new SQLException(ex);
                }
            }
        } else if (FileUtilities.isExtensionWellFormated(fileName, "zip")) {
            if (deleteFile) {
                Files.deleteIfExists(fileName.toPath());
            } else if (fileName.exists()) {
                throw new IOException("The zip file already exist.");
            }
            ZipOutputStream zip = null;
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                zip = new ZipOutputStream(fos);
                geojsonWriter(progress, rs, zip, encoding);
            } finally {
                try {
                    if (zip != null) {
                        zip.close();
                    }
                } catch (IOException ex) {
                    throw new SQLException(ex);
                }
            }
        } else {
            throw new SQLException("Only .geojson , .gz or .zip extensions are supported");
        }
    }

    /**
     * Method to write a resulset to a geojson file
     *
     * @param progress Progress visitor following the execution.
     * @param rs {@link ResultSet}
     * @param fos {@link OutputStream}
     * @param encoding chartset
     */
    private void geojsonWriter(ProgressVisitor progress, ResultSet rs, OutputStream fos, String encoding) throws SQLException, IOException {
        JsonEncoding jsonEncoding = JsonEncoding.UTF8;
        if (encoding != null && !encoding.isEmpty()) {
            try {
                jsonEncoding = JsonEncoding.valueOf(encoding);
            } catch (IllegalArgumentException ex) {
                throw new SQLException("Only UTF-8, UTF-16BE, UTF-16LE, UTF-32BE, UTF-32LE encoding is supported");
            }
        }
        try {
            int srid = 0;
            int rowCount = 0;
            int type = rs.getType();
            if (type == ResultSet.TYPE_SCROLL_INSENSITIVE || type == ResultSet.TYPE_SCROLL_SENSITIVE) {
                rs.last();
                rowCount = rs.getRow();
                rs.beforeFirst();
            }
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            ProgressVisitor copyProgress = progress.subProcess(rowCount);
            Tuple<String, Integer> geometryInfo = GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(resultSetMetaData);
            JsonFactory jsonFactory = new JsonFactory();
            JsonGenerator jsonGenerator = jsonFactory.createGenerator(new BufferedOutputStream(fos), jsonEncoding);

            // header of the GeoJSON file
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("type", "FeatureCollection");
            try {
                cacheMetadata(resultSetMetaData);
                //Read the first geometry to find its SRID
                rs.next();
                Geometry firstGeom = (Geometry) rs.getObject(geometryInfo.second());
                String[] authAndSrid = GeometryTableUtilities.getAuthorityAndSRID(connection, firstGeom.getSRID());
                if (authAndSrid != null) {
                    //Write the SRID based on the first geometry
                    writeCRS(jsonGenerator, authAndSrid);
                    srid = Integer.parseInt(authAndSrid[1]);
                }
                jsonGenerator.writeArrayFieldStart("features");
                //Don't forget to save the first geom
                // feature header
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("type", "Feature");
                //Write the first geometry
                writeGeometry((Geometry) rs.getObject(geometryInfo.second()), jsonGenerator);
                //Write the properties
                writeProperties(jsonGenerator, rs);
                // feature footer
                jsonGenerator.writeEndObject();
                copyProgress.endStep();

                //Iterate next rows and check SRID
                while (rs.next()) {
                    writeFeatureCheckSRID(jsonGenerator, rs, geometryInfo.second(), srid);
                    copyProgress.endStep();
                }
                copyProgress.endOfProgress();
                // footer
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
                jsonGenerator.flush();
                jsonGenerator.close();
            } finally {
                rs.close();
            }

        } catch (FileNotFoundException ex) {
            throw new SQLException(ex);

        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                throw new SQLException(ex);
            }
        }
    }

    /**
     * Method to write a table to a geojson file
     *
     * @param progress Progress visitor following the execution.
     * @param tableName table name
     * @param fos {@link OutputStream}
     * @param encoding file encoding
     */
    private void geojsonWriter(ProgressVisitor progress, String tableName, OutputStream fos, String encoding) throws SQLException, IOException {
        DBTypes dbTypes = DBUtils.getDBType(connection);
        JsonEncoding jsonEncoding = JsonEncoding.UTF8;
        if (encoding != null) {
            try {
                jsonEncoding = JsonEncoding.valueOf(encoding);
            } catch (IllegalArgumentException ex) {
                throw new SQLException("Only UTF-8, UTF-16BE, UTF-16LE, UTF-32BE, UTF-32LE encoding is supported");
            }
        }
        try {
            final TableLocation parse = TableLocation.parse(tableName, dbTypes);
            int recordCount = JDBCUtilities.getRowCount(connection, parse.toString());
            if (recordCount > 0) {
                ProgressVisitor copyProgress = progress.subProcess(recordCount);
                // Read Geometry Index and type
                Tuple<String, Integer> geometryTableInfo = GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(connection, parse);

                try ( // Read table content
                        Statement st = connection.createStatement()) {
                    JsonFactory jsonFactory = new JsonFactory();
                    JsonGenerator jsonGenerator = jsonFactory.createGenerator(new BufferedOutputStream(fos), jsonEncoding);

                    // header of the GeoJSON file
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("type", "FeatureCollection");
                    writeCRS(jsonGenerator, GeometryTableUtilities.getAuthorityAndSRID(connection, parse, geometryTableInfo.first()));
                    jsonGenerator.writeArrayFieldStart("features");

                    ResultSet rs = st.executeQuery(String.format("select * from %s", tableName));

                    try {
                        ResultSetMetaData resultSetMetaData = rs.getMetaData();
                        cacheMetadata(resultSetMetaData);
                        while (rs.next()) {
                            writeFeature(jsonGenerator, rs, geometryTableInfo.second());
                            copyProgress.endStep();
                        }
                        copyProgress.endOfProgress();
                        // footer
                        jsonGenerator.writeEndArray();
                        jsonGenerator.writeEndObject();
                        jsonGenerator.flush();
                        jsonGenerator.close();

                    } finally {
                        rs.close();
                    }
                }
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                throw new SQLException(ex);
            }
        }
    }

    /**
     * Write the spatial table to GeoJSON format.
     *
     * @param progress Progress visitor following the execution.
     * @param tableName table name
     * @param fileName input file
     * @param encoding file encoding
     * @param deleteFile true to delete the file if exist
     */
    public void write(ProgressVisitor progress, String tableName, File fileName, String encoding, boolean deleteFile) throws SQLException, IOException {
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableName);
        if (matcher.find()) {
            if (tableName.startsWith("(") && tableName.endsWith(")")) {
                if (FileUtilities.isExtensionWellFormated(fileName, "geojson")|| FileUtilities.isExtensionWellFormated(fileName, "json")) {
                    if (deleteFile) {
                        Files.deleteIfExists(fileName.toPath());
                    } else if (fileName.exists()) {
                        throw new IOException("The geojson file already exist.");
                    }
                    PreparedStatement ps = connection.prepareStatement(tableName, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    ResultSet rs = ps.executeQuery();
                    geojsonWriter(progress, rs, new FileOutputStream(fileName), encoding);
                } else if (FileUtilities.isExtensionWellFormated(fileName, "gz")) {
                    if (deleteFile) {
                        Files.deleteIfExists(fileName.toPath());
                    } else if (fileName.exists()) {
                        throw new IOException("The gz file already exist.");
                    }
                    GZIPOutputStream gzos = null;
                    try {
                        FileOutputStream fos = new FileOutputStream(fileName);
                        gzos = new GZIPOutputStream(fos);
                        PreparedStatement ps = connection.prepareStatement(tableName, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        ResultSet rs = ps.executeQuery();
                        geojsonWriter(progress, rs, gzos, encoding);
                    } finally {
                        try {
                            if (gzos != null) {
                                gzos.close();
                            }
                        } catch (IOException ex) {
                            throw new SQLException(ex);
                        }
                    }
                } else if (FileUtilities.isExtensionWellFormated(fileName, "zip")) {
                    if (deleteFile) {
                        Files.deleteIfExists(fileName.toPath());
                    } else if (fileName.exists()) {
                        throw new IOException("The zip file already exist.");
                    }
                    ZipOutputStream zip = null;
                    try {
                        FileOutputStream fos = new FileOutputStream(fileName);
                        zip = new ZipOutputStream(fos);
                        zip.putNextEntry(new ZipEntry(fileName.getName().substring(0, fileName.getName().length()-4)));
                        PreparedStatement ps = connection.prepareStatement(tableName, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        ResultSet rs = ps.executeQuery();
                        geojsonWriter(progress, rs, zip, encoding);
                    } finally {
                        try {
                            if (zip != null) {
                                zip.close();
                            }
                        } catch (IOException ex) {
                            throw new SQLException(ex);
                        }
                    }
                } else {
                    throw new SQLException("Only .geojson , .gz or .zip extensions are supported");
                }
            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM ORDERS)'.");
            }
        } else {
            if (FileUtilities.isExtensionWellFormated(fileName, "geojson")|| FileUtilities.isExtensionWellFormated(fileName, "json")) {
                if (deleteFile) {
                    Files.deleteIfExists(fileName.toPath());
                } else if (fileName.exists()) {
                    throw new IOException("The geojson file already exist.");
                }
                geojsonWriter(progress, tableName, new FileOutputStream(fileName), encoding);
            } else if (FileUtilities.isExtensionWellFormated(fileName, "gz")) {
                if (deleteFile) {
                    Files.deleteIfExists(fileName.toPath());
                } else if (fileName.exists()) {
                    throw new IOException("The gz file already exist.");
                }
                GZIPOutputStream gzos = null;
                try {
                    FileOutputStream fos = new FileOutputStream(fileName);
                    gzos = new GZIPOutputStream(fos);
                    geojsonWriter(progress, tableName, gzos, encoding);
                } finally {
                    try {
                        if (gzos != null) {
                            gzos.close();
                        }
                    } catch (IOException ex) {
                        throw new SQLException(ex);
                    }
                }
            } else if (FileUtilities.isExtensionWellFormated(fileName, "zip")) {
                if (deleteFile) {
                    Files.deleteIfExists(fileName.toPath());
                } else if (fileName.exists()) {
                    throw new IOException("The zip file already exist.");
                }
                ZipOutputStream zip = null;
                try {
                    FileOutputStream fos = new FileOutputStream(fileName);
                    zip = new ZipOutputStream(fos);
                    zip.putNextEntry(new ZipEntry(fileName.getName().substring(0, fileName.getName().length()-4)));
                    geojsonWriter(progress, tableName, zip, encoding);
                } finally {
                    try {
                        if (zip != null) {
                            zip.close();
                        }
                    } catch (IOException ex) {
                        throw new SQLException(ex);
                    }
                }
            } else {
                throw new SQLException("Only .geojson , .gz or .zip extensions are supported");
            }
        }
    }

    /**
     * Write a GeoJSON feature and check its SRID.
     *
     * Features in GeoJSON contain a geometry object and additional properties,
     * and a feature collection represents a list of features.
     *
     * A complete GeoJSON data structure is always an object (in JSON terms). In
     * GeoJSON, an object consists of a collection of name/value pairs -- also
     * called members. For each member, the name is always a string. Member
     * values are either a string, number, object, array or one of the literals:
     * true, false, and null. An array consists of elements where each element
     * is a value as described above.
     *
     * Syntax:
     *
     * { "type": "Feature", "geometry":{"type": "Point", "coordinates": [102.0,
     * 0.5]}, "properties": {"prop0": "value0"} }
     *
     * @param jsonGenerator
     * @param rs
     * @param geoFieldIndex
     */
    private void writeFeatureCheckSRID(JsonGenerator jsonGenerator, ResultSet rs, int geoFieldIndex, int srid) throws IOException, SQLException {
        // feature header
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", "Feature");
        //Write the first geometry
        Geometry geom = (Geometry) rs.getObject(geoFieldIndex);
        int geomSRID = geom.getSRID();
        if (geomSRID != srid) {
            throw new SQLException("Geojson file doesn't support mixed srid. \n"
                    + srid + " != " + geomSRID);
        }
        writeGeometry(geom, jsonGenerator);
        //Write the properties
        writeProperties(jsonGenerator, rs);
        // feature footer
        jsonGenerator.writeEndObject();
    }

    /**
     * Write a GeoJSON feature.
     *
     * Features in GeoJSON contain a geometry object and additional properties,
     * and a feature collection represents a list of features.
     *
     * A complete GeoJSON data structure is always an object (in JSON terms). In
     * GeoJSON, an object consists of a collection of name/value pairs -- also
     * called members. For each member, the name is always a string. Member
     * values are either a string, number, object, array or one of the literals:
     * true, false, and null. An array consists of elements where each element
     * is a value as described above.
     *
     * Syntax:
     *
     * { "type": "Feature", "geometry":{"type": "Point", "coordinates": [102.0,
     * 0.5]}, "properties": {"prop0": "value0"} }
     *
     * @param jsonGenerator
     * @param rs
     * @param geoFieldIndex
     */
    private void writeFeature(JsonGenerator jsonGenerator, ResultSet rs, int geoFieldIndex) throws IOException, SQLException {
        // feature header
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", "Feature");
        //Write the first geometry
        writeGeometry((Geometry) rs.getObject(geoFieldIndex), jsonGenerator);
        //Write the properties
        writeProperties(jsonGenerator, rs);
        // feature footer
        jsonGenerator.writeEndObject();
    }

    /**
     * Cache the column name and its index.
     *
     * @param resultSetMetaData {@link ResultSetMetaData}
     */
    private void cacheMetadata(ResultSetMetaData resultSetMetaData) throws SQLException {
        cachedColumnIndex = new LinkedHashMap<String, Integer>();
        cachedSpecificColumns = new LinkedHashMap<String, String>();
        int columnCount =resultSetMetaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            final String fieldTypeName = resultSetMetaData.getColumnTypeName(i);
            String columnName = resultSetMetaData.getColumnName(i);
            if (!fieldTypeName.toLowerCase().startsWith("geometry")
                    && isSupportedPropertyType(columnName, resultSetMetaData.getColumnType(i), fieldTypeName)) {
                cachedColumnIndex.put(columnName, i);
                columnCountProperties++;
            }
        }
    }

    /**
     * Write a JTS geometry to its GeoJSON geometry representation.
     *
     * Syntax:
     *
     * "geometry":{"type": "Point", "coordinates": [102.0, 0.5]}
     *
     * @param geom geometry to write
     * @param gen json writer
     */
    private void writeGeometry(Geometry geom, JsonGenerator gen) throws IOException {
        if (geom != null) {
            gen.writeObjectFieldStart("geometry");
            if (geom instanceof Point) {
                write((Point) geom, gen);
            } else if (geom instanceof MultiPoint) {
                write((MultiPoint) geom, gen);
            } else if (geom instanceof LineString) {
                write((LineString) geom, gen);
            } else if (geom instanceof MultiLineString) {
                write((MultiLineString) geom, gen);
            } else if (geom instanceof Polygon) {
                write((Polygon) geom, gen);
            } else if (geom instanceof MultiPolygon) {
                write((MultiPolygon) geom, gen);
            } else if (geom instanceof GeometryCollection) {
                write((GeometryCollection) geom, gen);
            } else {
                throw new RuntimeException("Unsupported Geomery type");
            }
            gen.writeEndObject();
        } else {
            gen.writeNullField("geometry");
        }

    }

    /**
     * Point coordinates are in x, y order (easting, northing for projected
     * coordinates, longitude, latitude for geographic coordinates):
     *
     * { "type": "Point", "coordinates": [100.0, 0.0] }
     *
     *
     * @param point point
     * @param gen json writer
     */
    private void write(Point point, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "Point");
        gen.writeFieldName("coordinates");
        writeCoordinate(point.getCoordinate(), gen);
    }

    /**
     * Coordinates of a MultiPoint are an array of positions:
     *
     * { "type": "MultiPoint", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     *
     * @param points points
     * @param gen json writer
     */
    private void write(MultiPoint points, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "MultiPoint");
        gen.writeFieldName("coordinates");
        writeCoordinates(points.getCoordinates(), gen);
    }

    /**
     * Coordinates of LineString are an array of positions :
     *
     * { "type": "LineString", "coordinates": [ [100.0, 0.0], [101.0, 1.0] ] }
     *
     * @param geom linestring
     * @param gen json writer
     */
    private void write(LineString geom, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "LineString");
        gen.writeFieldName("coordinates");
        writeCoordinates(geom.getCoordinates(), gen);
    }

    /**
     * Coordinates of a MultiLineString are an array of LineString coordinate
     * arrays:
     *
     * { "type": "MultiLineString", "coordinates": [ [ [100.0, 0.0], [101.0,
     * 1.0] ], [ [102.0, 2.0], [103.0, 3.0] ] ] }
     *
     * @param geom multi lines
     * @param gen json writer
     */
    private void write(MultiLineString geom, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "MultiLineString");
        gen.writeFieldName("coordinates");
        gen.writeStartArray();
        int size = geom.getNumGeometries();
        for (int i = 0; i < size; ++i) {
            writeCoordinates(geom.getGeometryN(i).getCoordinates(), gen);
        }
        gen.writeEndArray();
    }

    /**
     * Each element in the geometries array of a GeometryCollection is one of
     * the geometry objects described above:
     *
     * { "type": "GeometryCollection", "geometries": [ { "type": "Point",
     * "coordinates": [100.0, 0.0] }, { "type": "LineString", "coordinates": [
     * [101.0, 0.0], [102.0, 1.0] ] } ] }
     *
     * @param coll geometry collection
     * @param gen json writer
     */
    private void write(GeometryCollection coll, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "GeometryCollection");
        gen.writeArrayFieldStart("geometries");
        int size = coll.getNumGeometries();
        for (int i = 0; i < size; ++i) {
            Geometry geom = coll.getGeometryN(i);
            gen.writeStartObject();
            if (geom instanceof Point) {
                write((Point) geom, gen);
            } else if (geom instanceof MultiPoint) {
                write((MultiPoint) geom, gen);
            } else if (geom instanceof LineString) {
                write((LineString) geom, gen);
            } else if (geom instanceof MultiLineString) {
                write((MultiLineString) geom, gen);
            } else if (geom instanceof Polygon) {
                write((Polygon) geom, gen);
            } else if (geom instanceof MultiPolygon) {
                write((MultiPolygon) geom, gen);
            } else if (geom instanceof GeometryCollection) {
                write((GeometryCollection) geom, gen);
            } else {
                throw new RuntimeException("Unsupported Geomery type");
            }
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }

    /**
     * Coordinates of a Polygon are an array of LinearRing coordinate arrays.
     * The first element in the array represents the exterior ring. Any
     * subsequent elements represent interior rings (or holes).
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
     * @param geom polygon
     * @param gen json writer
     */
    private void write(Polygon geom, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "Polygon");
        gen.writeFieldName("coordinates");
        gen.writeStartArray();
        writeCoordinates(geom.getExteriorRing().getCoordinates(), gen);
        for (int i = 0; i < geom.getNumInteriorRing(); ++i) {
            writeCoordinates(geom.getInteriorRingN(i).getCoordinates(), gen);
        }
        gen.writeEndArray();
    }

    /**
     *
     *
     * Coordinates of a MultiPolygon are an array of Polygon coordinate arrays:
     *
     * { "type": "MultiPolygon", "coordinates": [ [[[102.0, 2.0], [103.0, 2.0],
     * [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]], [[[100.0, 0.0], [101.0, 0.0],
     * [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]], [[100.2, 0.2], [100.8, 0.2],
     * [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]] ] }
     *
     * @param geom multi polygons
     * @param gen json writer
     */
    private void write(MultiPolygon geom, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "MultiPolygon");
        gen.writeFieldName("coordinates");
        gen.writeStartArray();
        int size = geom.getNumGeometries();
        for (int i = 0; i < size; ++i) {
            Polygon p = (Polygon) geom.getGeometryN(i);
            gen.writeStartArray();
            writeCoordinates(p.getExteriorRing().getCoordinates(), gen);
            for (int j = 0; j < p.getNumInteriorRing(); ++j) {
                writeCoordinates(p.getInteriorRingN(j).getCoordinates(), gen);
            }
            gen.writeEndArray();
        }
        gen.writeEndArray();
    }

    /**
     * Write coordinate positions.
     *
     * @param coordinate coordinate
     * @param gen json writer
     */
    private void writeCoordinate(Coordinate coordinate, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        gen.writeNumber(coordinate.x);
        gen.writeNumber(coordinate.y);
        if (!Double.isNaN(coordinate.getZ())) {
            gen.writeNumber(coordinate.getZ());
        }
        if(!Double.isNaN(coordinate.getM())) {
            gen.writeNumber(coordinate.getM());
        }
        gen.writeEndArray();
    }

    /**
     * Write coordinate array.
     *
     * @param coordinates coordinate array
     * @param gen json writer
     */
    private void writeCoordinates(Coordinate[] coordinates, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (Coordinate coord : coordinates) {
            writeCoordinate(coord, gen);
        }
        gen.writeEndArray();
    }

    /**
     * Write the GeoJSON properties.
     *
     * @param jsonGenerator json writer
     * @param rs resulset
     */
    private void writeProperties(JsonGenerator jsonGenerator, ResultSet rs) throws IOException, SQLException {
        if (columnCountProperties != -1) {
            jsonGenerator.writeObjectFieldStart("properties");
            for (Map.Entry<String, Integer> entry : cachedColumnIndex.entrySet()) {
                String columnName = entry.getKey();
                Integer fieldId = entry.getValue();
                if(cachedSpecificColumns.containsKey(columnName)){
                    String specificType = cachedSpecificColumns.get(columnName);
                    if(specificType.equalsIgnoreCase("JSON")) {
                        jsonGenerator.writeFieldName(columnName);
                        jsonGenerator.writeString(rs.getString(fieldId));
                    }
                    else if (specificType.equalsIgnoreCase("TIME")){
                        Object obj = rs.getObject(fieldId);
                        jsonGenerator.writeStringField(columnName, obj == null ? "null" : obj.toString());
                    }
                }
                else if (rs.getObject(fieldId) instanceof Object[]) {
                    Object[] array = (Object[]) rs.getObject(fieldId);
                    jsonGenerator.writeArrayFieldStart(columnName);
                    writeArray(jsonGenerator, array, true);
                    jsonGenerator.writeEndArray();
                } else if (rs.getObject(fieldId) != null && rs.getObject(fieldId).equals("{}")) {
                    jsonGenerator.writeObjectFieldStart(columnName);
                    jsonGenerator.writeEndObject();
                } else if (rs.getObject(fieldId) == "null") {
                    jsonGenerator.writeFieldName(columnName);
                    jsonGenerator.writeNull();
                } else {
                    jsonGenerator.writeObjectField(columnName, rs.getObject(fieldId));
                }
            }
            jsonGenerator.writeEndObject();
        }
    }

    /**
     * Return true is the SQL type is supported by the GeoJSON driver.
     *
     * @param columnName column name
     * @param sqlTypeId sql type id
     * @param sqlTypeName sql type name
     * @return true if the column is supported
     */
    public boolean isSupportedPropertyType(String columnName, int sqlTypeId, String sqlTypeName) throws SQLException {
        switch (sqlTypeId) {
            case Types.BOOLEAN:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.SMALLINT:
            case Types.DATE:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.CHAR:
            case Types.ARRAY:
            case Types.OTHER:
                if(sqlTypeName.equalsIgnoreCase("JSON")){
                    cachedSpecificColumns.put(columnName, "JSON");
                    return true;
                }
            case Types.DECIMAL:
            case Types.REAL:
            case Types.TINYINT:
            case Types.NUMERIC:
            case Types.NULL:
                return true;
            case Types.TIME:
            case Types.TIMESTAMP:
                cachedSpecificColumns.put(columnName, "TIME");
                return true;
            default:
                throw new SQLException("Field type not supported by GeoJSON driver: " + sqlTypeName);
        }
    }

    /**
     * Write the CRS in the geojson
     *
     * @param jsonGenerator json writer
     * @param authorityAndSRID srid list
     */
    private void writeCRS(JsonGenerator jsonGenerator, String[] authorityAndSRID) throws IOException {
        if (authorityAndSRID[1] != null) {
            jsonGenerator.writeObjectFieldStart("crs");
            jsonGenerator.writeStringField("type", "name");
            jsonGenerator.writeObjectFieldStart("properties");
            jsonGenerator.writeStringField("name", "urn:ogc:def:crs:" + authorityAndSRID[0] + "::" + authorityAndSRID[1]);
            jsonGenerator.writeEndObject();
            jsonGenerator.writeEndObject();
        }
    }

    /**
     * Write the array in the geojson
     *
     * @param jsonGenerator
     * @param array
     * @throw IOException
     */
    private void writeArray(JsonGenerator jsonGenerator, Object[] array, boolean firstInHierarchy) throws IOException, SQLException {
        if (!firstInHierarchy) {
            jsonGenerator.writeStartArray();
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof Integer) {
                jsonGenerator.writeNumber((int) array[i]);
            } else if (array[i] instanceof String) {
                if (array[i].equals("{}")) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeEndObject();
                } else {
                    jsonGenerator.writeString((String) array[i]);
                }
            } else if (array[i] instanceof Double) {
                jsonGenerator.writeNumber((double) array[i]);
            } else if (array[i] instanceof Boolean) {
                jsonGenerator.writeBoolean((boolean) array[i]);
            } else if (array[i] instanceof Object[]) {
                writeArray(jsonGenerator, (Object[]) array[i], false);
            }
        }
        if (!firstInHierarchy) {
            jsonGenerator.writeEndArray();
        }
    }
}
