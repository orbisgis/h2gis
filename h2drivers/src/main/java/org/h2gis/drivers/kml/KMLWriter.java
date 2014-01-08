/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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
package org.h2gis.drivers.kml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.h2.store.fs.FileUtils;
import org.h2gis.h2spatialapi.ProgressVisitor;

/**
 *
 * @author Erwan Bocher
 */
public class KMLWriter {

    private String namespaceKML = "http://earth.google.com/kml/2.2";
    private final String tableName;
    private final File fileName;

    public KMLWriter(String tableName, File fileName) {
        this.tableName = tableName;
        this.fileName = fileName;
    }

    public void write(ProgressVisitor progress) throws SQLException {
        
        
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(fileName);
            XMLStreamWriter xmlOut = XMLOutputFactory.newInstance().createXMLStreamWriter(
                    new OutputStreamWriter(outputStream, "utf-8"));
            xmlOut.writeStartDocument("UTF-8", "1.0");
            xmlOut.writeStartElement("kml");
            xmlOut.writeDefaultNamespace("http://www.opengis.net/kml/2.2");
            xmlOut.writeNamespace("atom", "http://www.w3.org/2005/Atom");
            xmlOut.writeNamespace("kml", "http://www.opengis.net/kml/2.2");
            xmlOut.writeNamespace("gx", "http://www.google.com/kml/ext/2.2");
            xmlOut.writeNamespace("xal", "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0");

            xmlOut.writeStartElement("Document");
            xmlOut.writeStartElement("Folder");
            xmlOut.writeStartElement("name");
            xmlOut.writeCharacters("The name of the layer");
            xmlOut.writeEndElement();//Name
            writeSchema(xmlOut, tableName);

            xmlOut.writeEndElement();//Folder
            xmlOut.writeEndElement();//KML
            xmlOut.writeEndDocument();//DOC
            xmlOut.close();
        } catch (FileNotFoundException ex) {
            throw new SQLException(ex);
        } catch (XMLStreamException ex) {
            throw new SQLException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new SQLException(ex);
        } finally {
            try {
                outputStream.close();
            } catch (IOException ex) {
                throw new SQLException(ex);
            }
        }
    }

    /**
     *
     * @param xmlOut
     * @param tableName
     */
    private void writeSchema(XMLStreamWriter xmlOut, String tableName) throws XMLStreamException {
        xmlOut.writeStartElement("Schema");
        xmlOut.writeAttribute("name", tableName);
        xmlOut.writeAttribute("id", tableName);
        //Write column metadata
        
        xmlOut.writeEndElement();//Write schema
    }
    
    private void writeSimpleField(XMLStreamWriter xmlOut, String columnName, String columnType) throws XMLStreamException {
        xmlOut.writeStartElement("SimpleField");
        xmlOut.writeAttribute("name", tableName);
        xmlOut.writeAttribute("type", tableName);
        xmlOut.writeEndElement();//Write schema
    }

    public static void main(String[] args) throws SQLException {
        KMLWriter kMLWriter = new KMLWriter(null, new File("/tmp/test.kml"));
        kMLWriter.write(null);
    }
}
