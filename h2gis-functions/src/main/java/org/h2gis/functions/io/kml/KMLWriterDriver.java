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

package org.h2gis.functions.io.kml;

import org.locationtech.jts.geom.Geometry;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.h2gis.functions.io.utility.FileUtil;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;

/**
 *
 * @author Erwan Bocher
 */
public class KMLWriterDriver {

    private final String tableName;
    private final File fileName;
    private final Connection connection;
    private HashMap<Integer, String> kmlFields;
    private int columnCount = -1;

    public KMLWriterDriver(Connection connection, String tableName, File fileName) {
        this.connection = connection;
        this.tableName = tableName;
        this.fileName = fileName;
    }

    /**
     * Write spatial table to kml or kmz file format.
     *
     * @param progress
     * @throws SQLException
     */
    public void write(ProgressVisitor progress) throws SQLException {        
        if (FileUtil.isExtensionWellFormated(fileName, "kml")) {
            writeKML(progress);
        } else if (FileUtil.isExtensionWellFormated(fileName, "kmz")) {
            String name = fileName.getName();
            int pos = name.lastIndexOf(".");
            writeKMZ(progress, name.substring(0, pos) + ".kml");
        } else {
            throw new SQLException("Please use the extensions .kml or kmz.");
        }
    }

    /**
     * Write the spatial table to a KML format
     *
     * @param progress
     * @throws SQLException
     */
    private void writeKML(ProgressVisitor progress) throws SQLException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            writeKMLDocument(progress, fos);
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
     * Write the spatial table to a KMZ format
     *
     * @param progress
     * @param fileNameWithExtension
     * @throws SQLException
     */
    private void writeKMZ(ProgressVisitor progress, String fileNameWithExtension) throws SQLException {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(fileName));
            // Create a zip entry for the main KML file
            zos.putNextEntry(new ZipEntry(fileNameWithExtension));
            writeKMLDocument(progress, zos);
        } catch (FileNotFoundException ex) {
            throw new SQLException(ex);
        } catch (IOException ex) {
            throw new SQLException(ex);
        } finally {
            try {
                if (zos != null) {
                    zos.closeEntry();
                    zos.finish();
                }
            } catch (IOException ex) {
                throw new SQLException(ex);
            } finally {
                try {
                    if (zos != null) {
                        zos.close();
                    }
                } catch (IOException ex) {
                    throw new SQLException(ex);
                }
            }
        }


    }

    /**
     * Write the KML document Note the document stores only the first geometry
     * column in the placeMark element. The other geomtry columns are ignored.
     *
     * @param progress
     * @param outputStream
     * @throws SQLException
     */
    private void writeKMLDocument(ProgressVisitor progress, OutputStream outputStream) throws SQLException {
        // Read Geometry Index and type
        List<String> spatialFieldNames = SFSUtilities.getGeometryFields(connection, TableLocation.parse(tableName, JDBCUtilities.isH2DataBase(connection.getMetaData())));
        if (spatialFieldNames.isEmpty()) {
            throw new SQLException(String.format("The table %s does not contain a geometry field", tableName));
        }
        try {
            final XMLOutputFactory streamWriterFactory = XMLOutputFactory.newFactory();
            streamWriterFactory.setProperty("escapeCharacters", false);
            XMLStreamWriter xmlOut = streamWriterFactory.createXMLStreamWriter(
                    new BufferedOutputStream(outputStream), "UTF-8");
            xmlOut.writeStartDocument("UTF-8", "1.0");
            xmlOut.writeStartElement("kml");
            xmlOut.writeDefaultNamespace("http://www.opengis.net/kml/2.2");
            xmlOut.writeNamespace("atom", "http://www.w3.org/2005/Atom");
            xmlOut.writeNamespace("kml", "http://www.opengis.net/kml/2.2");
            xmlOut.writeNamespace("gx", "http://www.google.com/kml/ext/2.2");
            xmlOut.writeNamespace("xal", "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0");

            xmlOut.writeStartElement("Document");

            // Read table content
            Statement st = connection.createStatement();
            try {
                ResultSet rs = st.executeQuery(String.format("select * from %s", tableName));
                try {
                    int recordCount = JDBCUtilities.getRowCount(connection, tableName);
                    ProgressVisitor copyProgress = progress.subProcess(recordCount);
                    ResultSetMetaData resultSetMetaData = rs.getMetaData();
                    int geoFieldIndex = JDBCUtilities.getFieldIndex(resultSetMetaData, spatialFieldNames.get(0));

                    writeSchema(xmlOut, resultSetMetaData);
                    xmlOut.writeStartElement("Folder");
                    xmlOut.writeStartElement("name");
                    xmlOut.writeCharacters(tableName);
                    xmlOut.writeEndElement();//Name
                    while (rs.next()) {
                        writePlacemark(xmlOut, rs, geoFieldIndex, spatialFieldNames.get(0));
                        copyProgress.endStep();
                    }

                } finally {
                    rs.close();
                }
            } finally {
                st.close();
            }
            xmlOut.writeEndElement();//Folder
            xmlOut.writeEndElement();//KML
            xmlOut.writeEndDocument();//DOC
            xmlOut.close();
        } catch (XMLStreamException ex) {
            throw new SQLException(ex);
        }
    }

    /**
     * Specifies a custom KML schema that is used to add custom data to KML
     * Features. The "id" attribute is required and must be unique within the
     * KML file.
     * <Schema> is always a child of <Document>.
     *
     * Syntax :
     *
     * <Schema name="string" id="ID">
     * <SimpleField type="string" name="string">
     * <displayName>...</displayName> <!-- string -->
     * </SimpleField>
     * </Schema>
     *
     * @param xmlOut
     * @param tableName
     */
    private void writeSchema(XMLStreamWriter xmlOut, ResultSetMetaData metaData) throws XMLStreamException, SQLException {
        columnCount = metaData.getColumnCount();
        //The schema is writing only if there is more than one column
        if (columnCount > 1) {
            xmlOut.writeStartElement("Schema");
            xmlOut.writeAttribute("name", tableName);
            xmlOut.writeAttribute("id", tableName);
            //Write column metadata
            kmlFields = new HashMap<Integer, String>();
            for (int fieldId = 1; fieldId <= metaData.getColumnCount(); fieldId++) {
                final String fieldTypeName = metaData.getColumnTypeName(fieldId);
                if (!fieldTypeName.equalsIgnoreCase("geometry")) {
                    String fieldName = metaData.getColumnName(fieldId);
                    writeSimpleField(xmlOut, fieldName, getKMLType(metaData.getColumnType(fieldId), fieldTypeName));
                    kmlFields.put(fieldId, fieldName);

                }
            }
            xmlOut.writeEndElement();//Write schema
        }
    }

    /**
     * The declaration of the custom field, which must specify both the type and
     * the name of this field. If either the type or the name is omitted, the
     * field is ignored. The type can be one of the following : string, int,
     * uint, short, ushort, float, double, bool.
     *
     * Syntax :
     *
     * <SimpleField type="string" name="string">
     *
     * @param xmlOut
     * @param columnName
     * @param columnType
     * @throws XMLStreamException
     */
    private void writeSimpleField(XMLStreamWriter xmlOut, String columnName, String columnType) throws XMLStreamException {
        xmlOut.writeStartElement("SimpleField");
        xmlOut.writeAttribute("name", columnName);
        xmlOut.writeAttribute("type", columnType);
        xmlOut.writeEndElement();//Write schema
    }

    /**
     * A Placemark is a Feature with associated Geometry.
     *
     * Syntax :
     *
     * <Placemark id="ID">
     * <!-- inherited from Feature element -->
     * <name>...</name> <!-- string -->
     * <visibility>1</visibility> <!-- boolean -->
     * <open>0</open> <!-- boolean -->
     * <atom:author>...<atom:author> <!-- xmlns:atom -->
     * <atom:link href=" "/> <!-- xmlns:atom -->
     * <address>...</address> <!-- string -->
     * <xal:AddressDetails>...</xal:AddressDetails> <!-- xmlns:xal -->
     * <phoneNumber>...</phoneNumber> <!-- string -->
     * <Snippet maxLines="2">...</Snippet> <!-- string -->
     * <description>...</description> <!-- string -->
     * <AbstractView>...</AbstractView> <!-- Camera or LookAt -->
     * <TimePrimitive>...</TimePrimitive>
     * <styleUrl>...</styleUrl> <!-- anyURI -->
     * <StyleSelector>...</StyleSelector>
     * <Region>...</Region>
     * <Metadata>...</Metadata> <!-- deprecated in KML 2.2 -->
     * <ExtendedData>...</ExtendedData> <!-- new in KML 2.2 -->
     *
     * <!-- specific to Placemark element -->
     * <Geometry>...</Geometry>
     * </Placemark>
     *
     * @param xmlOut
     */
    public void writePlacemark(XMLStreamWriter xmlOut, ResultSet rs, int geoFieldIndex, String spatialFieldName) throws XMLStreamException, SQLException {
        xmlOut.writeStartElement("Placemark");
        if (columnCount > 1) {
            writeExtendedData(xmlOut, rs);
        }
        StringBuilder sb = new StringBuilder();
        Geometry geom = (Geometry) rs.getObject(geoFieldIndex);
        int inputSRID = geom.getSRID();
        if (inputSRID == 0) {
            throw new SQLException("A coordinate reference system must be set to save the KML file");
        } else if (inputSRID != 4326) {
            throw new SQLException("The kml format supports only the WGS84 projection. \n"
                    + "Please use ST_Transform(" + spatialFieldName + "," + inputSRID + ")");
        }
        KMLGeometry.toKMLGeometry(geom, ExtrudeMode.NONE, AltitudeMode.NONE, sb);
        //Write geometry
        xmlOut.writeCharacters(sb.toString());
        xmlOut.writeEndElement();//Write Placemark
    }

    /**
     * The ExtendedData element offers three techniques for adding custom data
     * to a KML Feature (NetworkLink, Placemark, GroundOverlay, PhotoOverlay,
     * ScreenOverlay, Document, Folder). These techniques are
     *
     * Adding untyped data/value pairs using the <Data> element (basic)
     * Declaring new typed fields using the <Schema> element and then instancing
     * them using the <SchemaData> element (advanced) Referring to XML elements
     * defined in other namespaces by referencing the external namespace within
     * the KML file (basic)
     *
     * These techniques can be combined within a single KML file or Feature for
     * different pieces of data.
     *
     * Syntax :
     *
     * <ExtendedData>
     * <Data name="string">
     * <displayName>...</displayName> <!-- string -->
     * <value>...</value> <!-- string -->
     * </Data>
     * <SchemaData schemaUrl="anyURI">
     * <SimpleData name=""> ... </SimpleData> <!-- string -->
     * </SchemaData>
     * <namespace_prefix:other>...</namespace_prefix:other>
     * </ExtendedData>
     *
     * @param xmlOut
     */
    public void writeExtendedData(XMLStreamWriter xmlOut, ResultSet rs) throws XMLStreamException, SQLException {
        xmlOut.writeStartElement("ExtendedData");
        xmlOut.writeStartElement("SchemaData");
        xmlOut.writeAttribute("schemaUrl", "#" + tableName);
        for (Map.Entry<Integer, String> entry : kmlFields.entrySet()) {
            Integer fieldIndex = entry.getKey();
            String fieldName = entry.getValue();
            writeSimpleData(xmlOut, fieldName, rs.getString(fieldIndex));
        }
        xmlOut.writeEndElement();//Write SchemaData
        xmlOut.writeEndElement();//Write ExtendedData

    }

    /**
     *
     * @param xmlOut
     */
    public void writeSimpleData(XMLStreamWriter xmlOut, String columnName, String value) throws XMLStreamException {
        xmlOut.writeStartElement("SimpleData");
        xmlOut.writeAttribute("name", columnName);
        xmlOut.writeCharacters(value);
        xmlOut.writeEndElement();//Write ExtendedData
    }

    /**
     * Return the kml type representation from SQL data type
     *
     * @param sqlTypeId
     * @param sqlTypeName
     * @return
     * @throws SQLException
     */
    private static String getKMLType(int sqlTypeId, String sqlTypeName) throws SQLException {
        switch (sqlTypeId) {
            case Types.BOOLEAN:
                return "bool";
            case Types.DOUBLE:
                return "double";
            case Types.FLOAT:
                return "float";
            case Types.INTEGER:
            case Types.BIGINT:
                return "int";
            case Types.SMALLINT:
                return "short";
            case Types.DATE:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.CHAR:
                return "string";
            default:
                throw new SQLException("Field type not supported by KML : " + sqlTypeName);
        }
    }
}
