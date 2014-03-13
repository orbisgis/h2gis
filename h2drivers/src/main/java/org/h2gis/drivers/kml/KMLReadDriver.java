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
package org.h2gis.drivers.kml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.xml.sax.SAXException;

/**
 * The KMLReader driver reads a KML file and imports the result into tables.
 *
 * Due to the nature of the KML file, the reader works as follow :
 *
 * Each folder is parsed and splited into a table.
 *
 * The name of the table correponds to the name of the folder.
 *
 * The schema of the table is specified by the element <Schema name="#" id="#">.
 * Only one schema within a folder is allowed.
 *
 * The elements <Placemark> store the data : geometry and attributes.
 *
 * In a <Placemark> the element <ExtendedData><SchemaData schemaUrl="#"> is
 * parsed to obtain the attributes and populate the table.
 *
 *
 * @author Erwan Bocher
 */
public class KMLReadDriver {

    private final Connection connection;
    private final String tableReference;
    private final File fileName;
    private static SAXParserFactory sParserFactory;

    static {
        sParserFactory = SAXParserFactory.newInstance();
    }

    public KMLReadDriver(Connection connection, String tableReference, File fileName) {
        this.connection = connection;
        this.tableReference = tableReference;
        this.fileName = fileName;
    }

    /**
     * Read the KML file.
     *
     * @param progress
     * @return <code>true</code> if success.
     */
    public boolean read(ProgressVisitor progress) throws SQLException, IOException {
        String path = fileName.getAbsolutePath();
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i >= 0) {
            extension = path.substring(i + 1);
        }
        if (extension.equalsIgnoreCase("kml")) {
            return parseKML(progress);
        } else {
            throw new SQLException("Please kml extension.");
        }
    }

    /**
     * Parses the KML file.
     *
     * @return <code>true</code> if success.
     */
    private boolean parseKML(ProgressVisitor progress) throws IOException, SQLException {
        FileInputStream fis = null;
        KMLHandler kHandler = new KMLHandler(connection, tableReference);
        try {
            SAXParser parser = sParserFactory.newSAXParser();
            sParserFactory.setValidating(true);
            parser.parse(new BufferedInputStream(new FileInputStream(fileName)),
                    kHandler);
        } catch (ParserConfigurationException ex) {
            throw new SQLException(ex);
        } catch (SAXException ex) {
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
        return kHandler.IsSucess();
    }
}
