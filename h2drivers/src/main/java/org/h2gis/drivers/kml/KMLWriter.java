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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;

/**
 *
 * @author Erwan Bocher
 */
public class KMLWriter {

    private final String tableName;
    private final File fileName;
    private final Connection connection;
    private HashMap<Integer, String> kmlFields;
    private int columnCount =-1;

    public KMLWriter(Connection connection, String tableName, File fileName) {
        this.connection = connection;
        this.tableName = tableName;
        this.fileName = fileName;
    }

    public void write(ProgressVisitor progress) throws SQLException {
        // Read Geometry Index and type
        List<String> spatialFieldNames = SFSUtilities.getGeometryFields(connection, TableLocation.parse(tableName));
        if (spatialFieldNames.isEmpty()) {
            throw new SQLException(String.format("The table %s does not contain a geometry field", tableName));
        }
       
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
            xmlOut.writeCharacters(tableName);
            xmlOut.writeEndElement();//Name

            // Read table content
            Statement st = connection.createStatement();
            try {
                ResultSet rs = st.executeQuery(String.format("select * from `%s`", tableName));
                try {
                    ResultSetMetaData resultSetMetaData = rs.getMetaData();
                    int geoFieldIndex = JDBCUtilities.getFieldIndex(resultSetMetaData, spatialFieldNames.get(0));

                    writeSchema(xmlOut, resultSetMetaData);
                    while (rs.next()) {
                        writePlacemark(xmlOut, rs, geoFieldIndex);
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

        progress.endOfProgress();
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
    public void writePlacemark(XMLStreamWriter xmlOut, ResultSet rs, int geoFieldIndex) throws XMLStreamException, SQLException {
        xmlOut.writeStartElement("Placemark");
        if (columnCount > 1) {
            writeExtendedData(xmlOut, rs);
        }
        //Write Geometry
        Geometry geometry  = (Geometry) rs.getObject(geoFieldIndex);
        if(geometry instanceof Point){
            writeKMLPoint(xmlOut, (Point) geometry);
        }
        else if(geometry instanceof LineString){
            writeKMLLineString(xmlOut, (LineString) geometry);
        }
        else if (geometry instanceof Polygon){
            writeKMLPolygon(xmlOut, (Polygon) geometry);
        }
        else if(geometry instanceof GeometryCollection){
            writeKMLMultiGeometry(xmlOut, (GeometryCollection) geometry);
        }
        else{
            throw new SQLException("This geometry type is not supported : "+ geometry.toString());
        }
        
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
        xmlOut.writeAttribute("schemaUrl", "#"+tableName);
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
    public void writeSimpleData(XMLStreamWriter xmlOut, String columnName, String value) throws XMLStreamException{
        xmlOut.writeStartElement("SimpleData");
        xmlOut.writeAttribute("name", columnName);
        xmlOut.writeCharacters(value);
        xmlOut.writeEndElement();//Write ExtendedData
    }

    /**
     * A geographic location defined by longitude, latitude, and (optional)
     * altitude.
     *
     * Syntax :
     *
     * <Point id="ID">
     * <!-- specific to Point -->
     * <extrude>0</extrude> <!-- boolean -->
     * <altitudeMode>clampToGround</altitudeMode>
     * <!-- kml:altitudeModeEnum: clampToGround, relativeToGround, or absolute
     * -->
     * <!-- or, substitute gx:altitudeMode: clampToSeaFloor, relativeToSeaFloor
     * -->
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </Point>
     *
     * @param xmlOut
     * @param point
     * @throws XMLStreamException
     */
    public void writeKMLPoint(XMLStreamWriter xmlOut, Point point) throws XMLStreamException {
        xmlOut.writeStartElement("Point");
        xmlOut.writeStartElement("coordinates");
        Coordinate coord = point.getCoordinate();
        StringBuilder sb = new StringBuilder();
        sb.append(coord.y).append(",").append(coord.x);
        if (!Double.isNaN(coord.z)) {
            sb.append(",").append(coord.z);
        }
        xmlOut.writeCharacters(sb.toString());
        xmlOut.writeEndElement();//Write coordinates
        xmlOut.writeEndElement();//Write Point
    }

    /**
     * Defines a connected set of line segments.
     *
     * Syntax :
     *
     * <LineString id="ID">
     * <!-- specific to LineString -->
     * <gx:altitudeOffset>0</gx:altitudeOffset> <!-- double -->
     * <extrude>0</extrude> <!-- boolean -->
     * <tessellate>0</tessellate> <!-- boolean -->
     * <altitudeMode>clampToGround</altitudeMode>
     * <!-- kml:altitudeModeEnum: clampToGround, relativeToGround, or absolute
     * -->
     * <!-- or, substitute gx:altitudeMode: clampToSeaFloor, relativeToSeaFloor
     * -->
     * <gx:drawOrder>0</gx:drawOrder> <!-- integer -->
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LineString>
     *
     * @param xmlOut
     * @param lineString
     * @throws XMLStreamException
     */
    public void writeKMLLineString(XMLStreamWriter xmlOut, LineString lineString) throws XMLStreamException {
        xmlOut.writeStartElement("LineString");
        writeKMLCoordinates(xmlOut, lineString.getCoordinates());
        xmlOut.writeEndElement();//Write Point
    }

    /**
     * A Polygon is defined by an outer boundary and 0 or more inner boundaries.
     * The boundaries, in turn, are defined by LinearRings.
     *
     * Syntax :
     *
     * <Polygon id="ID">
     * <!-- specific to Polygon -->
     * <extrude>0</extrude> <!-- boolean -->
     * <tessellate>0</tessellate> <!-- boolean -->
     * <altitudeMode>clampToGround</altitudeMode>
     * <!-- kml:altitudeModeEnum: clampToGround, relativeToGround, or absolute
     * -->
     * <!-- or, substitute gx:altitudeMode: clampToSeaFloor, relativeToSeaFloor
     * -->
     * <outerBoundaryIs>
     * <LinearRing>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LinearRing>
     * </outerBoundaryIs>
     * <innerBoundaryIs>
     * <LinearRing>
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] -->
     * </LinearRing>
     * </innerBoundaryIs>
     * </Polygon>
     *
     * @param xmlOut
     * @param polygon
     * @throws XMLStreamException
     */
    public void writeKMLPolygon(XMLStreamWriter xmlOut, Polygon polygon) throws XMLStreamException {
        xmlOut.writeStartElement("Polygon");
        xmlOut.writeStartElement("outerBoundaryIs");
        writeKMLLinearRing(xmlOut, polygon.getExteriorRing().getCoordinates());
        xmlOut.writeEndElement();//Write outerBoundaryIs
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            xmlOut.writeStartElement("innerBoundaryIs");
            writeKMLLinearRing(xmlOut, polygon.getInteriorRingN(i).getCoordinates());
            xmlOut.writeEndElement();//Write innerBoundaryIs
        }
        xmlOut.writeEndElement();//Write Polygon   
    }

    /**
     * Defines a closed line string, typically the outer boundary of a Polygon.
     *
     * Syntax :
     *
     * <LinearRing id="ID">
     * <!-- specific to LinearRing -->
     * <gx:altitudeOffset>0</gx:altitudeOffset> <!-- double -->
     * <extrude>0</extrude> <!-- boolean -->
     * <tessellate>0</tessellate> <!-- boolean -->
     * <altitudeMode>clampToGround</altitudeMode>
     * <!-- kml:altitudeModeEnum: clampToGround, relativeToGround, or absolute
     * -->
     * <!-- or, substitute gx:altitudeMode: clampToSeaFloor, relativeToSeaFloor
     * -->
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] tuples -->
     * </LinearRing>
     *
     * @param xmlOut
     * @param coordinates
     * @throws XMLStreamException
     */
    public void writeKMLLinearRing(XMLStreamWriter xmlOut, Coordinate[] coordinates) throws XMLStreamException {
        xmlOut.writeStartElement("LinearRing");
        writeKMLCoordinates(xmlOut, coordinates);
        xmlOut.writeEndElement();//Write LinearRing 
    }

    /**
     * A container for zero or more geometry primitives associated with the same
     * feature.
     *
     * Syntax :
     * 
     * <MultiGeometry id="ID">
     * <!-- specific to MultiGeometry -->
     * <!-- 0 or more Geometry elements -->
     * </MultiGeometry>
     *
     * @param xmlOut
     * @param gc
     * @throws XMLStreamException
     */
    public void writeKMLMultiGeometry(XMLStreamWriter xmlOut, GeometryCollection gc) throws XMLStreamException {
        xmlOut.writeStartElement("MultiGeometry");
        for (int i = 0; i < gc.getNumGeometries(); i++) {
            Geometry geom = gc.getGeometryN(i);
            if (geom instanceof Point) {
                writeKMLPoint(xmlOut,(Point) geom);
            }
            else if(geom instanceof LineString){
                writeKMLLineString(xmlOut, (LineString)geom);
            }
            else if(geom instanceof Polygon){
                writeKMLPolygon(xmlOut,(Polygon) geom);
            }
        }
        xmlOut.writeEndElement();//Write MultiGeometry 
    }

    /**
     * 
     * 
     * Syntax :
     * 
     * <coordinates>...</coordinates> <!-- lon,lat[,alt] tuples -->
     * 
     * @param xmlOut
     * @param coords
     * @throws XMLStreamException 
     */
    public void writeKMLCoordinates(XMLStreamWriter xmlOut, Coordinate[] coords) throws XMLStreamException {
        xmlOut.writeStartElement("coordinates");
        StringBuilder sb = new StringBuilder();
        for (Coordinate coord : coords) {
            sb.append(coord.y).append(",").append(coord.x);
            if (!Double.isNaN(coord.z)) {
                sb.append(",").append(coord.z);
            }
            sb.append(" ");
        }
        xmlOut.writeCharacters(sb.toString());
        xmlOut.writeEndElement();//Write coordinates
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
                throw new SQLException("Field type not supported by DBF : " + sqlTypeName);
        }
    }
    
    /**
     * Return the string kml value representation from SQL data type
     *
     * @param sqlTypeId
     * @param sqlTypeName
     * @return
     * @throws SQLException
     */
    private static String getKMLValue(int sqlTypeId, String fieldName, ResultSet rs) throws SQLException {
        switch (sqlTypeId) {
            case Types.BOOLEAN:
                return "";
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
                throw new SQLException("Field type not supported by KML : " + fieldName);
        }
    }
    
        
}
