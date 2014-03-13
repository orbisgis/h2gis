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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
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
    private String POINT = "point";
    private String LINESTRING = "LineString";
    private String COORDINATES = "coordinates";
    private boolean kmlSucess = true;
    private int schemaCounter = 0;
    private Object[] kmldata;
    private String currentTableName;
    //Current placemark values
    private Object[] values;

    public KMLHandler(Connection connection, String tableReference) {
        this.connection = connection;
        this.tableReference = tableReference;
    }

    @Override
    public void startElement(String uri, String localName,
            String name, Attributes attributes) throws SAXException {
        if (name.equalsIgnoreCase(SCHEMA)) {           
        } else if (name.equalsIgnoreCase(SIMPLEFIELD)) {
            kmlMetadata.addField(attributes.getValue(NAME), attributes.getValue(TYPE));
        } else if (name.equalsIgnoreCase(PLACEMARK)) {
            System.out.println("PLACEMARK Id" + attributes.getValue("ID"));
            System.out.println("PLACEMARK Name" + attributes.getValue(NAME));
            System.out.println("PLACEMARK " + attributes.getValue(COORDINATES));
            System.out.println("PLACEMARK Id" + attributes.getValue("DESCRIPTION"));
        } else if (name.equalsIgnoreCase(SCHEMADATA)) {
            String schemaURL = attributes.getValue(SCHEMAURL);
            if (schemaURL.startsWith("#")) {
                currentTableName = tableReference + "_" + schemaURL.substring(1);
            } else {
                throw new SAXException("The schema URL " + schemaURL + " is not supported.");
            }

        } else if (name.equalsIgnoreCase(NAME)) {
        } else if (name.equalsIgnoreCase(SIMPLEDATA)) {
            //Get the values
            System.out.println("Simple data " + attributes.getValue(NAME));
        }
    }

    /**
     * Returns the appropriate preparedStatement to store the values in a table.
     *
     * @param placeMarkKey
     * @return
     */
    public PreparedStatement getAppropriateStatement(String placeMarkKey) {
        return statements.get(placeMarkKey).getPreparedStatement();
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        if (name.equalsIgnoreCase(PLACEMARK)) {
            System.out.println("Placemark");
            insertData();
        } else if (name.equalsIgnoreCase(SCHEMA)) {
            try {
                System.out.println("End " + SCHEMA);
                createSchemaStatement();
            } catch (SQLException ex) {
                throw new SAXException(ex);
            }
        } else if (name.equalsIgnoreCase(DOCUMENT)) {
            //We must closed all statement.
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

    /**
     * Create the PreparedStatement corresponding to the table.
     * The statement schema contains the following columns :
     * 
     *  the_geom -> GEOMETRY,
     *  id -> VARCHAR,
     *  placemarkName -> VARCHAR,
     *  placemarkDesc -> VARCHAR,
     *  folderName -> VARCHAR,
     *  folderDesc -> VARCHAR,
     *  parentFolders -> VARCHAR
     * 
     */
    private void createSchemaStatement() throws SQLException {
        Statement stmt = connection.createStatement();
        StringBuilder sb = new StringBuilder("CREATE TABLE ");
        sb.append(tableReference);
        sb.append("(");
        //We return the preparedstatement
        StringBuilder insert = new StringBuilder("INSERT INTO ").append(schemaTable).append(" VALUES (");

        for (int i = 0; i < kmlMetadata.getFieldCount(); i++) {
            String fieldName = kmlMetadata.getFieldName(i);
            String fieldType = kmlMetadata.getFieldType(i);
            sb.append(fieldName).append(" ").append(fieldType).append(",");
            insert.append("?,");
        }
        sb.append(")");
        insert.append(");");
        stmt.execute(sb.toString());
        stmt.close();
        PreparedStatement schemaStmt = connection.prepareStatement(insert.toString());
        kmlMetadata.setPreparedStatement(schemaStmt);
        statements.put(schemaTable, kmlMetadata);
        kmlMetadata = null;
    }

    /**
     *
     * @param kmlTableIdentifier
     * @return
     */
    private String createCurrentTableName(String kmlTableIdentifier) {
        return tableReference + "_" + kmlTableIdentifier;
    }

    /**
     * Insert the current placemark data into the corresponding table.
     */
    private void insertData() throws SAXException {
        PreparedStatement pStm = getAppropriateStatement(currentTableName);
        //For each values well ordering set it
        try {
            int i = 1;
            for (Object value : values) {
                pStm.setObject(i, value);
                i++;
            }
            //Insert values
            pStm.execute();

        } catch (SQLException ex) {
            throw new SAXException("Cannot import the placemark.", ex);
        }
    }
}
