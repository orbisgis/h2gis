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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
    private boolean isFirstParsing = false;

    public GeoJsonReaderDriver(Connection connection, String tableName, File fileName) {
        this.connection = connection;
        this.tableName = tableName;
        this.fileName = fileName;
    }

    /**
     *
     * @param progress
     */
    public void parse(ProgressVisitor progress) throws SQLException, IOException {
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
     * Parses a GeoJson file and writes it into a table.
     *
     * @param progress
     */
    private void parseGeoJson(ProgressVisitor progress) throws SQLException, IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            JsonFactory factory = new JsonFactory();
            factory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            factory.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            factory.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);

            JsonParser jsParser = factory.createParser(fis);

            jsParser.nextToken(); // start of object

            jsParser.nextToken(); // field_name (type)
            jsParser.nextToken(); // value_string
            String geomType = jsParser.getText();

            jsParser.nextToken(); // field_name

            jsParser.nextToken(); // value of field (start object / start array)

            if (geomType.equalsIgnoreCase("featurecollection")) {
                parseFeatureCollection(jsParser);
            } else if (geomType.equalsIgnoreCase("feature")) {
            }

            jsParser.close();


            // Read table content
            Statement st = connection.createStatement();
            try {
            } finally {
                st.close();
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

    /**
     *
     * @return
     */
    public PreparedStatement createPreparedStatement() {
        return null;
    }

    public static void main(String[] args) throws Exception {
        String jsonFile = "/tmp/points.geojson";
        GeoJsonReaderDriver geoJsonReaderDriver = new GeoJsonReaderDriver(null, "points", new File(jsonFile));
        geoJsonReaderDriver.parse(new EmptyProgressVisitor());
    }

    private void parseFeatureCollection(JsonParser jsParser) throws IOException, SQLException {
        jsParser.nextToken();
        while (jsParser.nextToken() != JsonToken.END_ARRAY) {
            jsParser.nextToken(); // field_name (usually 'type', ignored here)
            String geomType = jsParser.getText();
            if (geomType.equalsIgnoreCase("feature")) {
                parseFeature(jsParser);
            } else {
                throw new SQLException("Malformed geojson file. Expected 'Feature', found '" + geomType + "'");
            }
        }

    }

    private void parseFeature(JsonParser jsParser) {
    }
}
