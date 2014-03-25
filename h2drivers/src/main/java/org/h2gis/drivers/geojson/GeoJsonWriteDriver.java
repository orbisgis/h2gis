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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;

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
 */
public class GeoJsonWriteDriver {

    private final String tableName;
    private final File fileName;
    private final Connection connection;
    private Map<String, Integer> cachedColumnNames;
    private int columnCountProperties = -1;

    /**
     * A simple GeoJSON driver to write a spatial table to a GeoJSON file.
     *
     * @param connection
     * @param tableName
     * @param fileName
     */
    public GeoJsonWriteDriver(Connection connection, String tableName, File fileName) {
        this.connection = connection;
        this.tableName = tableName;
        this.fileName = fileName;
    }

    /**
     * Write the spatial table to GeoJSON format.
     *
     * @param progress
     * @throws SQLException
     */
    public void write(ProgressVisitor progress) throws SQLException, IOException {
        String path = fileName.getAbsolutePath();
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i >= 0) {
            extension = path.substring(i + 1);
        }
        if (extension.equalsIgnoreCase("geojson")) {
            writeGeoJson(progress);
        } else {
            throw new SQLException("Please geojson extension.");
        }
    }

    /**
     * Write the spatial table to GeoJSON format.
     *
     * @param progress
     * @throws SQLException
     */
    private void writeGeoJson(ProgressVisitor progress) throws SQLException, IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            // Read Geometry Index and type
            List<String> spatialFieldNames = SFSUtilities.getGeometryFields(connection, TableLocation.parse(tableName));
            if (spatialFieldNames.isEmpty()) {
                throw new SQLException(String.format("The table %s does not contain a geometry field", tableName));
            }

            // Read table content
            Statement st = connection.createStatement();
            try {
                ResultSet rs = st.executeQuery(String.format("select * from `%s`", tableName));

                JsonFactory jsonFactory = new JsonFactory();
                JsonGenerator jsonGenerator = jsonFactory.createGenerator(new BufferedOutputStream(fos), JsonEncoding.UTF8);

                // header of the GeoJSON file
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("type", "FeatureCollection");
                jsonGenerator.writeArrayFieldStart("features");

                try {
                    ResultSetMetaData resultSetMetaData = rs.getMetaData();
                    int geoFieldIndex = JDBCUtilities.getFieldIndex(resultSetMetaData, spatialFieldNames.get(0));

                    cacheMetadata(resultSetMetaData);
                    while (rs.next()) {
                        writeFeature(jsonGenerator, rs, geoFieldIndex);
                    }
                    progress.endStep();
                    // footer
                    jsonGenerator.writeEndArray();
                    jsonGenerator.writeEndObject();
                    jsonGenerator.flush();
                    jsonGenerator.close();

                } finally {
                    rs.close();
                }
            } finally {
                st.close();
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
     * @param writer
     * @param resultSetMetaData
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
     * @param resultSetMetaData
     * @throws SQLException
     */
    private void cacheMetadata(ResultSetMetaData resultSetMetaData) throws SQLException {
        cachedColumnNames = new HashMap<String, Integer>();
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            final String fieldTypeName = resultSetMetaData.getColumnTypeName(i);
            if (!fieldTypeName.equalsIgnoreCase("geometry")
                    && isSupportedPropertyType(resultSetMetaData.getColumnType(i), fieldTypeName)) {
                cachedColumnNames.put(resultSetMetaData.getColumnName(i).toUpperCase(), i);
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
     * @param jsonGenerator
     * @param geometry
     */
    private void writeGeometry(Geometry geom, JsonGenerator gen) throws IOException {
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
    }

    /**
     * Point coordinates are in x, y order (easting, northing for projected
     * coordinates, longitude, latitude for geographic coordinates):
     *
     * { "type": "Point", "coordinates": [100.0, 0.0] }
     *
     *
     * @param point
     * @param gen
     * @throws IOException
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
     * @param points
     * @param gen
     * @throws IOException
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
     * @param geom
     * @param gen
     * @throws IOException
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
     * @param geom
     * @param gen
     * @throws IOException
     */
    private void write(MultiLineString geom, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "MultiLineString");
        gen.writeFieldName("coordinates");
        gen.writeStartArray();
        for (int i = 0; i < geom.getNumGeometries(); ++i) {
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
     * @param coll
     * @param gen
     * @throws IOException
     */
    private void write(GeometryCollection coll, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "GeometryCollection");
        gen.writeArrayFieldStart("geometries");
        for (int i = 0; i < coll.getNumGeometries(); ++i) {
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
     * @param geom
     * @param gen
     * @throws IOException
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
     * @param geom
     * @param gen
     * @throws IOException
     */
    private void write(MultiPolygon geom, JsonGenerator gen) throws IOException {
        gen.writeStringField("type", "MultiPolygon");
        gen.writeFieldName("coordinates");
        gen.writeStartArray();
        for (int i = 0; i < geom.getNumGeometries(); ++i) {
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
     * @param coordinate
     * @param gen
     * @throws IOException
     */
    private void writeCoordinate(Coordinate coordinate, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        gen.writeNumber(coordinate.x);
        gen.writeNumber(coordinate.y);
        if (!Double.isNaN(coordinate.z)) {
            gen.writeNumber(coordinate.z);
        }
        gen.writeEndArray();
    }

    /**
     * Write coordinate array.
     *
     * @param coordinates
     * @param gen
     * @throws IOException
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
     * @param jsonGenerator
     * @param rs
     * @throws IOException
     */
    private void writeProperties(JsonGenerator jsonGenerator, ResultSet rs) throws IOException, SQLException {
        if (columnCountProperties != -1) {
            jsonGenerator.writeObjectFieldStart("properties");
            for (Map.Entry<String, Integer> entry : cachedColumnNames.entrySet()) {
                String string = entry.getKey();
                Integer fieldId = entry.getValue();
                jsonGenerator.writeObjectField(string, rs.getObject(fieldId));
            }
            jsonGenerator.writeEndObject();
        }
    }

    /**
     * Return true is the SQL type is supported by the GeoJSON driver.
     *
     * @param sqlTypeId
     * @param sqlTypeName
     * @return
     * @throws SQLException
     */
    public boolean isSupportedPropertyType(int sqlTypeId, String sqlTypeName) throws SQLException {
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
                return true;
            default:
                throw new SQLException("Field type not supported by GeoJSON driver: " + sqlTypeName);
        }
    }
}
