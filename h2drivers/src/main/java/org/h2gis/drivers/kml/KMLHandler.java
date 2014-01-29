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

import java.sql.Connection;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Erwan Bocher
 */
public class KMLHandler extends DefaultHandler {

    private final Connection connection;
    private final String tableReference;
    private String DESCRIPTION = "description";
    private String NAME = "name";
    private String SIMPLEFIELD = "SimpleField";
    private String TYPE = "type";
    private String DOCUMENT = "document";
    private String FOLDER = "folder";
    private String SCHEMA = "schema";
    private String PLACEMARK = "placemark";
    private String SIMPLEDATA = "SimpleData";
    private String POINT = "point";
    private String EXTENDEDDATA = "ExtendedData";
    private String SCHEMADATA = "SchemaData";
    private String SCHEMAURL = "schemaUrl";
    private String LINESTRING = "LineString";
    private String COORDINATES ="coordinates";
    
    private boolean kmlSucess = true;

    public KMLHandler(Connection connection, String tableReference) {
        this.connection = connection;
        this.tableReference = tableReference;
    }

    @Override
    public void startElement(String uri, String localName,
            String name, Attributes attributes) throws SAXException {
        if (name.equalsIgnoreCase(DOCUMENT)) {
            System.out.println(DOCUMENT);
        } else if (name.equalsIgnoreCase(FOLDER)) {
            System.out.println(attributes.getValue(name));
        } else if (name.equalsIgnoreCase(SCHEMA)) {
            String schemaName = attributes.getValue("name");
            System.out.println("Schema name " + schemaName);
            String schemaId = attributes.getValue("id");
            System.out.println("Schema id " + schemaId);
        } else if (name.equalsIgnoreCase(SIMPLEFIELD)) {
        }
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        if (name.equalsIgnoreCase(PLACEMARK)) {
            System.out.println("Placemark");
        } else if (name.equalsIgnoreCase(SCHEMA)) {
            System.out.println("End " + SCHEMA);
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        kmlSucess = false;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        kmlSucess = false;
    }

    /**
     * Returns true is the KML file is parsed without any problems.
     *
     * @return
     */
    public boolean IsSucess() {
        return kmlSucess;
    }
}
