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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.vividsolutions.jts.geom.Geometry;
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
 * A simple geojson driver to write a spatial table to a geojson file.
 *
 * GeoJSON is a format for encoding a variety of geographic data structures. A
 * GeoJSON object may represent a geometry, a feature, or a collection of
 * features. GeoJSON supports the following geometry types: Point, LineString,
 * Polygon, MultiPoint, MultiLineString, MultiPolygon, and GeometryCollection.
 *
 * Syntax :
 *
 * { "type": "FeatureCollection", "features": [ { "type": "Feature",
 * "geometry":{"type": "Point", "coordinates": [102.0, 0.5]}, "properties":
 * {"prop0": "value0"} } ]}
 *
 * @author Erwan
 */
public class GeoJsonWriteDriver {

    private final String tableName;
    private final File fileName;
    private final Connection connection;
    private Map<String, Integer> cachedColumnNames;
    private int columnCountProperties = -1;

    public GeoJsonWriteDriver(Connection connection, String tableName, File fileName) {
        this.connection = connection;
        this.tableName = tableName;
        this.fileName = fileName;
    }

    /**
     * Write spatial table to geojson file format.
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
            throw new SQLException("Please kml or kmz extension.");
        }
    }

    /**
     * Write the spatial table to a geojson format
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

                // header of the geojson file
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
     * Write a geojson feature
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
     * Syntax :
     *
     * * { "type": "Feature", "geometry":{"type": "Point", "coordinates":
     * [102.0, 0.5]}, "properties": {"prop0": "value0"} }
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
        writeGeometry(jsonGenerator, (Geometry) rs.getObject(geoFieldIndex));
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
                    && isSupportedPropertyType(resultSetMetaData.getColumnType(i), tableName)) {
                cachedColumnNames.put(resultSetMetaData.getColumnName(i).toUpperCase(), i);
                columnCountProperties++;
            }
        }
    }

    /**
     *
     * @param jsonGenerator
     * @param geometry
     */
    private void writeGeometry(JsonGenerator jsonGenerator, Geometry geometry) throws IOException {
        jsonGenerator.writeObjectFieldStart("geometry");
        StringBuilder sb = new StringBuilder();
        GeojsonGeometry.toGeojsonGeometry(geometry, sb);
        jsonGenerator.writeString(sb.toString());
        jsonGenerator.writeEndObject();
    }

    /**
     * Write the geojson properties *
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
     * Return true is the SQL type is supported by the geojson driver
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
                throw new SQLException("Field type not supported by geojson driver : " + sqlTypeName);
        }
    }
}
