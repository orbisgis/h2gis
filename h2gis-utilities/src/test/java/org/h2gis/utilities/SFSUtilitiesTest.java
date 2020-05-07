/*
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

package org.h2gis.utilities;

import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.DataSourceWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test SFSUtilities
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public class SFSUtilitiesTest {

    private static Connection connection;

    @BeforeAll
    public static void init() throws ClassNotFoundException, SQLException {
        String dataBaseLocation = new File("target/JDBCUtilitiesTest").getAbsolutePath();
        String databasePath = "jdbc:h2:"+dataBaseLocation;
        File dbFile = new File(dataBaseLocation+".mv.db");
        Class.forName("org.h2.Driver");
        if(dbFile.exists()) {
            dbFile.delete();
        }
        // Keep a connection alive to not close the DataBase on each unit test
        connection = DriverManager.getConnection(databasePath,
                "sa", "");

        Statement st = connection.createStatement();

        

        //registerGeometryType
        st = connection.createStatement();
        st.execute("CREATE DOMAIN IF NOT EXISTS POINT AS GEOMETRY(POINT)");
        st.execute("CREATE DOMAIN IF NOT EXISTS LINESTRING AS GEOMETRY(  LINESTRING)");
        st.execute("CREATE DOMAIN IF NOT EXISTS POLYGON AS GEOMETRY(POLYGON)");
        st.execute("CREATE DOMAIN IF NOT EXISTS GEOMCOLLECTION AS GEOMETRY(GEOMETRYCOLLECTION)");
        st.execute("CREATE DOMAIN IF NOT EXISTS MULTIPOINT AS GEOMETRY(MULTIPOINT)");
        st.execute("CREATE DOMAIN IF NOT EXISTS MULTILINESTRING AS GEOMETRY(MULTILINESTRING)");
        st.execute("CREATE DOMAIN IF NOT EXISTS MULTIPOLYGON AS GEOMETRY(MULTIPOLYGON)");

        //registerSpatialTables
        st = connection.createStatement();
        st.execute("drop view if exists geometry_columns");
        /*st.execute("create view geometry_columns as select TABLE_CATALOG f_table_catalog,TABLE_SCHEMA f_table_schema,TABLE_NAME f_table_name," +
                "COLUMN_NAME f_geometry_column,1 storage_type,_GeometryTypeFromConstraint(CHECK_CONSTRAINT || REMARKS, NUMERIC_PRECISION) geometry_type" +
                " from INFORMATION_SCHEMA.COLUMNS WHERE TYPE_NAME = 'GEOMETRY'");*/
        st.execute("create view geometry_columns as select TABLE_CATALOG f_table_catalog,TABLE_SCHEMA f_table_schema,TABLE_NAME f_table_name," +
                "COLUMN_NAME f_geometry_column,1 storage_type,_GeometryTypeFromConstraint(COLUMN_TYPE) geometry_type," +
                "_ColumnSRID(TABLE_CATALOG,TABLE_SCHEMA, TABLE_NAME,COLUMN_NAME,CHECK_CONSTRAINT) srid" +
                " from INFORMATION_SCHEMA.COLUMNS WHERE TYPE_NAME = 'GEOMETRY'");
        /*st.execute("create view geometry_columns as select TABLE_CATALOG f_table_catalog,TABLE_SCHEMA f_table_schema,TABLE_NAME f_table_name," +
                "COLUMN_NAME f_geometry_column,1 storage_type,_GeometryTypeFromConstraint(CHECK_CONSTRAINT || REMARKS, NUMERIC_PRECISION) geometry_type," +
                "_DimensionFromConstraint(TABLE_CATALOG,TABLE_SCHEMA, TABLE_NAME,COLUMN_NAME,CHECK_CONSTRAINT) coord_dimension," +
                "_ColumnSRID(TABLE_CATALOG,TABLE_SCHEMA, TABLE_NAME,COLUMN_NAME,CHECK_CONSTRAINT) srid," +
                " _GeometryTypeNameFromConstraint(CHECK_CONSTRAINT || REMARKS, NUMERIC_PRECISION) type" +
                " from INFORMATION_SCHEMA.COLUMNS WHERE TYPE_NAME = 'GEOMETRY'");*/
        /*ResultSet rs = connection.getMetaData().getTables("","PUBLIC","SPATIAL_REF_SYS",null);
        if(!rs.next()) {
            InputStreamReader reader = new InputStreamReader(
                    H2GISFunctions.class.getResourceAsStream("spatial_ref_sys.sql"));
            RunScript.execute(connection, reader);

            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        connection.createStatement().execute("DROP TABLE IF EXISTS NOGEOM");
        connection.createStatement().execute("CREATE TABLE NOGEOM (id INT, str VARCHAR(100))");
        connection.createStatement().execute("INSERT INTO NOGEOM VALUES (25, 'twenty five')");
        connection.createStatement().execute("INSERT INTO NOGEOM VALUES (6, 'six')");

        connection.createStatement().execute("DROP TABLE IF EXISTS POINTTABLE");
        connection.createStatement().execute("CREATE TABLE POINTTABLE (geom GEOMETRY)");
        connection.createStatement().execute("INSERT INTO POINTTABLE VALUES ('POINT(1 1)')");

        connection.createStatement().execute("DROP TABLE IF EXISTS GEOMTABLE");
        connection.createStatement().execute("CREATE TABLE GEOMTABLE (geom GEOMETRY, pt GEOMETRY(  POINTZM    ), linestr LINESTRING, " +
                "plgn POLYGON, multipt MULTIPOINT, multilinestr MULTILINESTRING, multiplgn MULTIPOLYGON, " +
                "geomcollection GEOMCOLLECTION)");
        connection.createStatement().execute("INSERT INTO GEOMTABLE VALUES ('POINT(1 1)', 'POINT(1 1 0 0)'," +
                " 'LINESTRING(1 1, 2 2)', 'POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))', 'MULTIPOINT((1 1))'," +
                " 'MULTILINESTRING((1 1, 2 2))', 'MULTIPOLYGON(((1 1, 1 2, 2 2, 2 1, 1 1)))'," +
                " 'GEOMETRYCOLLECTION(POINT(1 1))')");
        connection.createStatement().execute("INSERT INTO GEOMTABLE VALUES ('LINESTRING(1 1, 2 2)', 'POINT(2 2 0 0)'," +
                " 'LINESTRING(2 2, 1 1)', 'POLYGON((1 1, 1 3, 3 3, 3 1, 1 1))', 'MULTIPOINT((3 3))'," +
                " 'MULTILINESTRING((1 1, 3 3))', 'MULTIPOLYGON(((1 1, 1 3, 3 3, 3 1, 1 1)))'," +
                " 'GEOMETRYCOLLECTION(POINT(3 3))')");
    }

    // getGeometryTypeNameFromCode(int geometryTypeCode)
    @Test
    public void testGeometryTypeNameFromCode(){
        assertEquals("GEOMETRY", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.GEOMETRY));
        assertEquals("POINT", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POINT));
        assertEquals("LINESTRING", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.LINESTRING));
        assertEquals("POLYGON", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POLYGON));
        assertEquals("MULTIPOINT", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTIPOINT));
        assertEquals("MULTILINESTRING", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTILINESTRING));
        assertEquals("MULTIPOLYGON", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTIPOLYGON));
        assertEquals("GEOMCOLLECTION", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.GEOMCOLLECTION));
        assertEquals("MULTICURVE", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTICURVE));
        assertEquals("MULTISURFACE", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTISURFACE));
        assertEquals("CURVE", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.CURVE));
        assertEquals("SURFACE", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.SURFACE));
        assertEquals("POLYHEDRALSURFACE", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POLYHEDRALSURFACE));
        assertEquals("TIN", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.TIN));
        assertEquals("TRIANGLE", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.TRIANGLE));

        assertEquals("GEOMETRYZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.GEOMETRYZ));
        assertEquals("POINTZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POINTZ));
        assertEquals("LINESTRINGZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.LINESTRINGZ));
        assertEquals("POLYGONZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POLYGONZ));
        assertEquals("MULTIPOINTZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTIPOINTZ));
        assertEquals("MULTILINESTRINGZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTILINESTRINGZ));
        assertEquals("MULTIPOLYGONZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTIPOLYGONZ));
        assertEquals("GEOMCOLLECTIONZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.GEOMCOLLECTIONZ));
        assertEquals("MULTICURVEZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTICURVEZ));
        assertEquals("MULTISURFACEZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTISURFACEZ));
        assertEquals("CURVEZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.CURVEZ));
        assertEquals("SURFACEZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.SURFACEZ));
        assertEquals("POLYHEDRALSURFACEZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POLYHEDRALSURFACEZ));
        assertEquals("TINZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.TINZ));
        assertEquals("TRIANGLEZ", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.TRIANGLEZ));

        assertEquals("GEOMETRYM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.GEOMETRYM));
        assertEquals("POINTM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POINTM));
        assertEquals("LINESTRINGM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.LINESTRINGM));
        assertEquals("POLYGONM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POLYGONM));
        assertEquals("MULTIPOINTM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTIPOINTM));
        assertEquals("MULTILINESTRINGM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTILINESTRINGM));
        assertEquals("MULTIPOLYGONM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTIPOLYGONM));
        assertEquals("GEOMCOLLECTIONM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.GEOMCOLLECTIONM));
        assertEquals("MULTICURVEM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTICURVEM));
        assertEquals("MULTISURFACEM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTISURFACEM));
        assertEquals("CURVEM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.CURVEM));
        assertEquals("SURFACEM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.SURFACEM));
        assertEquals("POLYHEDRALSURFACEM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POLYHEDRALSURFACEM));
        assertEquals("TINM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.TINM));
        assertEquals("TRIANGLEM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.TRIANGLEM));

        assertEquals("GEOMETRYZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.GEOMETRYZM));
        assertEquals("POINTZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POINTZM));
        assertEquals("LINESTRINGZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.LINESTRINGZM));
        assertEquals("POLYGONZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POLYGONZM));
        assertEquals("MULTIPOINTZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTIPOINTZM));
        assertEquals("MULTILINESTRINGZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTILINESTRINGZM));
        assertEquals("MULTIPOLYGONZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTIPOLYGONZM));
        assertEquals("GEOMCOLLECTIONZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.GEOMCOLLECTIONZM));
        assertEquals("MULTICURVEZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTICURVEZM));
        assertEquals("MULTISURFACEZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.MULTISURFACEZM));
        assertEquals("CURVEZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.CURVEZM));
        assertEquals("SURFACEZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.SURFACEZM));
        assertEquals("POLYHEDRALSURFACEZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.POLYHEDRALSURFACEZM));
        assertEquals("TINZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.TINZM));
        assertEquals("TRIANGLEZM", SFSUtilities.getGeometryTypeNameFromCode(GeometryTypeCodes.TRIANGLEZM));
    }

    // getGeometryTypeFromGeometry(Geometry geometry)
    @Test
    public void testGeometryTypeFromGeometry() throws ParseException {
        WKTReader wktReader = new WKTReader();

        assertEquals(GeometryTypeCodes.POINT, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("POINT(1 1)")));
        assertEquals(GeometryTypeCodes.LINESTRING, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("LINESTRING(1 1, 2 2)")));
        assertEquals(GeometryTypeCodes.POLYGON, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))")));
        assertEquals(GeometryTypeCodes.MULTIPOINT, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTIPOINT((1 1))")));
        assertEquals(GeometryTypeCodes.MULTILINESTRING, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTILINESTRING((1 1, 2 2))")));
        assertEquals(GeometryTypeCodes.MULTIPOLYGON, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTIPOLYGON(((1 1, 1 2, 2 2, 2 1, 1 1)))")));
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("GEOMETRYCOLLECTION(POINT(1 1))")));

        assertEquals(GeometryTypeCodes.POINT, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("POINTZ(1 1 1)")));
        assertEquals(GeometryTypeCodes.LINESTRING, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("LINESTRINGZ(1 1 1, 2 2 2)")));
        assertEquals(GeometryTypeCodes.POLYGON, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("POLYGONZ((1 1 1, 1 2 1, 2 2 1, 2 1 1, 1 1 1))")));
        assertEquals(GeometryTypeCodes.MULTIPOINT, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTIPOINTZ((1 1 1))")));
        assertEquals(GeometryTypeCodes.MULTILINESTRING, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTILINESTRINGZ((1 1 1, 2 2 2))")));
        assertEquals(GeometryTypeCodes.MULTIPOLYGON, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTIPOLYGONZ(((1 1 1, 1 2 1, 2 2 1, 2 1 1, 1 1 1)))")));
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("GEOMETRYCOLLECTIONZ(POINTZ(1 1 1))")));

        assertEquals(GeometryTypeCodes.POINT, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("POINTM(1 1 1)")));
        assertEquals(GeometryTypeCodes.LINESTRING, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("LINESTRINGM(1 1 1, 2 2 2)")));
        assertEquals(GeometryTypeCodes.POLYGON, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("POLYGONM((1 1 1, 1 2 1, 2 2 1, 2 1 1, 1 1 1))")));
        assertEquals(GeometryTypeCodes.MULTIPOINT, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTIPOINTM((1 1 1))")));
        assertEquals(GeometryTypeCodes.MULTILINESTRING, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTILINESTRINGM((1 1 1, 2 2 2))")));
        assertEquals(GeometryTypeCodes.MULTIPOLYGON, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTIPOLYGONM(((1 1 1, 1 2 1, 2 2 1, 2 1 1, 1 1 1)))")));
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("GEOMETRYCOLLECTIONM(POINTM(1 1 1))")));

        assertEquals(GeometryTypeCodes.POINT, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("POINTZM(1 1 1 1)")));
        assertEquals(GeometryTypeCodes.LINESTRING, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("LINESTRINGZM(1 1 1 1, 2 2 2 2)")));
        assertEquals(GeometryTypeCodes.POLYGON, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("POLYGONZM((1 1 1 1, 1 2 1 1, 2 2 1 1, 2 1 1 1, 1 1 1 1))")));
        assertEquals(GeometryTypeCodes.MULTIPOINT, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTIPOINTZM((1 1 1 1))")));
        assertEquals(GeometryTypeCodes.MULTILINESTRING, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTILINESTRINGZM((1 1 1 1, 2 2 1 1))")));
        assertEquals(GeometryTypeCodes.MULTIPOLYGON, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("MULTIPOLYGONZM(((1 1 1 1, 1 2 1 1, 2 2 1 1, 2 1 1 1, 1 1 1 1)))")));
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("GEOMETRYCOLLECTIONZM(POINTZM(1 1 1 1))")));
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("GEOMETRYCOLLECTIONZM(POINTZM(1 1 1 1))")));

        assertEquals(GeometryTypeCodes.GEOMETRY, SFSUtilities.getGeometryTypeFromGeometry(
                wktReader.read("LINEARRING(1 1, 2 2, 3 3, 1 1))")));
    }

    // getGeometryType(Connection connection,TableLocation location, String fieldName)
    @Test
    public void testGeometryType() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE");
        assertEquals(GeometryTypeCodes.GEOMETRY,
                SFSUtilities.getGeometryType(connection, tableLocation, "geom"));
        assertEquals(GeometryTypeCodes.POINTZM,
                SFSUtilities.getGeometryType(connection, tableLocation, "pt"));
        assertEquals(GeometryTypeCodes.LINESTRING,
                SFSUtilities.getGeometryType(connection, tableLocation, "linestr"));
        assertEquals(GeometryTypeCodes.POLYGON,
                SFSUtilities.getGeometryType(connection, tableLocation, "plgn"));
        assertEquals(GeometryTypeCodes.MULTIPOINT,
                SFSUtilities.getGeometryType(connection, tableLocation, "multipt"));
        assertEquals(GeometryTypeCodes.MULTILINESTRING,
                SFSUtilities.getGeometryType(connection, tableLocation, "multilinestr"));
        assertEquals(GeometryTypeCodes.MULTIPOLYGON,
                SFSUtilities.getGeometryType(connection, tableLocation, "multiplgn"));
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION,
                SFSUtilities.getGeometryType(connection, tableLocation, "geomcollection"));

        assertEquals(GeometryTypeCodes.GEOMETRY,
                SFSUtilities.getGeometryType(connection, tableLocation, ""));
    }

    @Test
    public void testGeometryTypeNoGeomTableEmptyField() {
        assertThrows(SQLException.class,() ->
                SFSUtilities.getGeometryType(connection, TableLocation.parse("NOGEOM"), ""));
    }

    @Test
    public void testGeometryTypeNoGeomTable() {
        assertThrows(SQLException.class,() ->
                SFSUtilities.getGeometryType(connection, TableLocation.parse("NOGEOM"), "id"));
    }

    @Test
    public void testGeometryTypeNotValidField() {
        assertThrows(SQLException.class,() ->
                SFSUtilities.getGeometryType(connection, TableLocation.parse("NOGEOM"), "notAField"));
    }

    // getGeometryTypes(Connection connection, TableLocation location)
    @Test
    public void testGeometryTypes() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE");
        Map<String, Integer> map = SFSUtilities.getGeometryTypes(connection, tableLocation);
        assertTrue(map.containsKey("GEOM"));
        assertEquals(GeometryTypeCodes.GEOMETRY, map.get("GEOM").intValue());
        assertTrue(map.containsKey("PT"));
        assertEquals(GeometryTypeCodes.POINTZM,
                SFSUtilities.getGeometryType(connection, tableLocation, "pt"));
        assertTrue(map.containsKey("LINESTR"));
        assertEquals(GeometryTypeCodes.LINESTRING,
                SFSUtilities.getGeometryType(connection, tableLocation, "linestr"));
        assertTrue(map.containsKey("PLGN"));
        assertEquals(GeometryTypeCodes.POLYGON,
                SFSUtilities.getGeometryType(connection, tableLocation, "plgn"));
        assertTrue(map.containsKey("PLGN"));
        assertEquals(GeometryTypeCodes.MULTIPOINT,
                SFSUtilities.getGeometryType(connection, tableLocation, "multipt"));
        assertTrue(map.containsKey("MULTILINESTR"));
        assertEquals(GeometryTypeCodes.MULTILINESTRING,
                SFSUtilities.getGeometryType(connection, tableLocation, "multilinestr"));
        assertTrue(map.containsKey("MULTIPLGN"));
        assertEquals(GeometryTypeCodes.MULTIPOLYGON,
                SFSUtilities.getGeometryType(connection, tableLocation, "multiplgn"));
        assertTrue(map.containsKey("GEOMCOLLECTION"));
        assertEquals(GeometryTypeCodes.GEOMCOLLECTION,
                SFSUtilities.getGeometryType(connection, tableLocation, "geomcollection"));
    }

    // wrapSpatialDataSource(DataSource dataSource)
    @Test
    public void testWrapSpatialDataSource(){
        assertTrue(SFSUtilities.wrapSpatialDataSource(new CustomDataSource()) instanceof CustomDataSource);
        assertTrue(SFSUtilities.wrapSpatialDataSource(new CustomDataSource1()) instanceof DataSourceWrapper);
        assertTrue(SFSUtilities.wrapSpatialDataSource(new CustomDataSource2()) instanceof DataSourceWrapper);
    }

    // wrapConnection(Connection connection)
    @Test
    public void testWrapConnection(){
        assertTrue(SFSUtilities.wrapConnection(connection) instanceof ConnectionWrapper);
        assertTrue(SFSUtilities.wrapConnection(new CustomConnection1(connection)) instanceof ConnectionWrapper);
        assertTrue(SFSUtilities.wrapConnection(new CustomConnection(connection)) instanceof ConnectionWrapper);
    }

    
   

    // getGeometryFields(Connection connection,String catalog, String schema, String table)
    

    // prepareInformationSchemaStatement(Connection connection,String catalog, String schema, String table,
    //                                String informationSchemaTable, String endQuery, String catalog_field,
    //                                String schema_field, String table_field)
    @Test
    public void testPrepareInformationSchemaStatement() throws SQLException {
        PreparedStatement ps = SFSUtilities.prepareInformationSchemaStatement(connection, "cat", "sch", "tab",
                "INFORMATION_SCHEMA.CONSTRAINTS", "limit 1", "TABLE_CATALOG", "TABLE_SCHEMA","TABLE_NAME");
        assertEquals(ps.toString().substring(ps.toString().indexOf(": ")+2), "SELECT * from INFORMATION_SCHEMA.CONSTRAINTS where UPPER(TABLE_CATALOG) " +
                "= ? AND UPPER(TABLE_SCHEMA) = ? AND UPPER(TABLE_NAME) = ? limit 1 {1: 'CAT', 2: 'SCH', 3: 'TAB'}");
    }

    // prepareInformationSchemaStatement(Connection connection,String catalog, String schema, String table,
    //                                   String informationSchemaTable, String endQuery)
    @Test
    public void testPrepareInformationSchemaStatement2() throws SQLException {
        PreparedStatement ps = SFSUtilities.prepareInformationSchemaStatement(connection, "cat", "sch", "tab",
                "geometry_columns", "limit 1");
        assertEquals(ps.toString().substring(ps.toString().indexOf(": ")+2), "SELECT * from geometry_columns where UPPER(f_table_catalog) " +
                "= ? AND UPPER(f_table_schema) = ? AND UPPER(f_table_name) = ? limit 1 {1: 'CAT', 2: 'SCH', 3: 'TAB'}");
    }

    

    // getGeometryFields(ResultSet resultSet)
    @Test
    public void testGeometryFields() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        List<String> list = SFSUtilities.getGeometryFields(rs);
        assertEquals(8, list.size());
        assertTrue(list.contains("GEOM"));
        assertTrue(list.contains("PT"));
        assertTrue(list.contains("LINESTR"));
        assertTrue(list.contains("PLGN"));
        assertTrue(list.contains("MULTIPT"));
        assertTrue(list.contains("MULTILINESTR"));
        assertTrue(list.contains("MULTIPLGN"));
        assertTrue(list.contains("GEOMCOLLECTION"));
        rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        list = SFSUtilities.getGeometryFields(rs);
        assertEquals(0, list.size());
    }

    // getFirstGeometryFieldIndex(ResultSet resultSet)
    @Test
    public void testGeometryFieldIndex() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals(1, GeometryTableUtilities.getFirstGeometryFieldIndex(rs));
        rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        assertEquals(-1, GeometryTableUtilities.getFirstGeometryFieldIndex(rs));
    }
    

    private class CustomDataSource implements DataSource {
        @Override public Connection getConnection() throws SQLException {return null;}
        @Override public Connection getConnection(String s, String s1) throws SQLException {return null;}
        @Override public <T> T unwrap(Class<T> aClass) throws SQLException {return null;}
        @Override public boolean isWrapperFor(Class<?> aClass) throws SQLException {return true;}
        @Override public PrintWriter getLogWriter() throws SQLException {return null;}
        @Override public void setLogWriter(PrintWriter printWriter) throws SQLException {}
        @Override public void setLoginTimeout(int i) throws SQLException {}
        @Override public int getLoginTimeout() throws SQLException {return 0;}
        @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {return null;}
    }

    private class CustomDataSource1 extends CustomDataSource {
        @Override public boolean isWrapperFor(Class<?> aClass) throws SQLException {throw new SQLException();}
    }

    private class CustomDataSource2 extends CustomDataSource {
        @Override public boolean isWrapperFor(Class<?> aClass) throws SQLException {return false;}
    }

    private class CustomConnection1 extends ConnectionWrapper {
        public CustomConnection1(Connection connection) {super(connection);}
        @Override public boolean isWrapperFor(Class<?> var1) throws SQLException{throw new SQLException();}
    }

    private class CustomConnection extends ConnectionWrapper {
        public CustomConnection(Connection connection) {super(connection);}
        @Override public boolean isWrapperFor(Class<?> var1) throws SQLException{return true;}
    }    
   
}
