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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
     * Parses the KML file.
     *
     * @return <code>true</code> if success.
     */
    public boolean read() throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser = sParserFactory.newSAXParser();        
        sParserFactory.setNamespaceAware(true);

        KMLHandler kHandler = new KMLHandler(connection, tableReference);

        parser.parse(new BufferedInputStream(new FileInputStream(fileName)),
                kHandler);

        return kHandler.IsSucess();
    }
}
