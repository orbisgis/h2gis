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

import org.h2.api.Aggregate;
import org.h2.util.StringUtils;
import org.h2.value.Value;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.DataSourceWrapper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.locationtech.jts.util.Assert.shouldNeverReachHere;

/**
 * Test SFSUtilities
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public class SFSUtilitiesTest {

    private static Connection connection;

    @BeforeClass
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

        String functionAlias = "_GeometryTypeFromConstraint";
        ScalarFunction scalarFunction = new GeometryTypeFromConstraint();
        try {
            st.execute("DROP ALIAS IF EXISTS " + functionAlias);
        } catch (SQLException ignored) {}
        st.execute("CREATE FORCE ALIAS IF NOT EXISTS _GeometryTypeFromConstraint DETERMINISTIC NOBUFFER FOR \"" +
                GeometryTypeFromConstraint.class.getName() + "." + scalarFunction.getJavaStaticMethod() + "\"");

        st.execute("DROP AGGREGATE IF EXISTS " + ST_Extent.class.getSimpleName().toUpperCase());
        st.execute("CREATE FORCE AGGREGATE IF NOT EXISTS " + ST_Extent.class.getSimpleName().toUpperCase() +
                " FOR \"" + ST_Extent.class.getName() + "\"");

        functionAlias = "_ColumnSRID";
        scalarFunction = new ColumnSRID();
        try {
            st.execute("DROP ALIAS IF EXISTS " + functionAlias);
        } catch (SQLException ignored) {}
        st.execute("CREATE FORCE ALIAS IF NOT EXISTS _ColumnSRID DETERMINISTIC NOBUFFER FOR \"" +
                ColumnSRID.class.getName() + "." + scalarFunction.getJavaStaticMethod() + "\"");

        //registerGeometryType
        st = connection.createStatement();
        st.execute("CREATE DOMAIN IF NOT EXISTS POINT AS GEOMETRY(" + GeometryTypeCodes.POINT + ")");
        st.execute("CREATE DOMAIN IF NOT EXISTS LINESTRING AS GEOMETRY(" + GeometryTypeCodes.LINESTRING + ")");
        st.execute("CREATE DOMAIN IF NOT EXISTS POLYGON AS GEOMETRY(" + GeometryTypeCodes.POLYGON + ")");
        st.execute("CREATE DOMAIN IF NOT EXISTS GEOMCOLLECTION AS GEOMETRY(" + GeometryTypeCodes.GEOMCOLLECTION + ")");
        st.execute("CREATE DOMAIN IF NOT EXISTS MULTIPOINT AS GEOMETRY(" + GeometryTypeCodes.MULTIPOINT + ")");
        st.execute("CREATE DOMAIN IF NOT EXISTS MULTILINESTRING AS GEOMETRY(" + GeometryTypeCodes.MULTILINESTRING + ")");
        st.execute("CREATE DOMAIN IF NOT EXISTS MULTIPOLYGON AS GEOMETRY(" + GeometryTypeCodes.MULTIPOLYGON + ")");

        //registerSpatialTables
        st = connection.createStatement();
        st.execute("drop view if exists geometry_columns");
        /*st.execute("create view geometry_columns as select TABLE_CATALOG f_table_catalog,TABLE_SCHEMA f_table_schema,TABLE_NAME f_table_name," +
                "COLUMN_NAME f_geometry_column,1 storage_type,_GeometryTypeFromConstraint(CHECK_CONSTRAINT || REMARKS, NUMERIC_PRECISION) geometry_type" +
                " from INFORMATION_SCHEMA.COLUMNS WHERE TYPE_NAME = 'GEOMETRY'");*/
        st.execute("create view geometry_columns as select TABLE_CATALOG f_table_catalog,TABLE_SCHEMA f_table_schema,TABLE_NAME f_table_name," +
                "COLUMN_NAME f_geometry_column,1 storage_type,_GeometryTypeFromConstraint(CHECK_CONSTRAINT || REMARKS, NUMERIC_PRECISION) geometry_type," +
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
        connection.createStatement().execute("CREATE TABLE GEOMTABLE (geom GEOMETRY, pt POINT, linestr LINESTRING, " +
                "plgn POLYGON, multipt MULTIPOINT, multilinestr MULTILINESTRING, multiplgn MULTIPOLYGON, " +
                "geomcollection GEOMCOLLECTION)");
        connection.createStatement().execute("INSERT INTO GEOMTABLE VALUES ('POINT(1 1)', 'POINT(1 1)'," +
                " 'LINESTRING(1 1, 2 2)', 'POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))', 'MULTIPOINT((1 1))'," +
                " 'MULTILINESTRING((1 1, 2 2))', 'MULTIPOLYGON(((1 1, 1 2, 2 2, 2 1, 1 1)))'," +
                " 'GEOMETRYCOLLECTION(POINT(1 1))')");
        connection.createStatement().execute("INSERT INTO GEOMTABLE VALUES ('LINESTRING(1 1, 2 2)', 'POINT(2 2)'," +
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
        assertEquals(GeometryTypeCodes.POINT,
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

    @Test(expected = SQLException.class)
    public void testGeometryTypeNoGeomTableEmptyField() throws SQLException {
        SFSUtilities.getGeometryType(connection, TableLocation.parse("NOGEOM"), "");
        shouldNeverReachHere();
    }

    @Test(expected = SQLException.class)
    public void testGeometryTypeNoGeomTable() throws SQLException {
        SFSUtilities.getGeometryType(connection, TableLocation.parse("NOGEOM"), "id");
        shouldNeverReachHere();
    }

    @Test(expected = SQLException.class)
    public void testGeometryTypeNotValidField() throws SQLException {
        SFSUtilities.getGeometryType(connection, TableLocation.parse("NOGEOM"), "notAField");
        shouldNeverReachHere();
    }

    // getGeometryTypes(Connection connection, TableLocation location)
    @Test
    public void testGeometryTypes() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE");
        Map<String, Integer> map = SFSUtilities.getGeometryTypes(connection, tableLocation);
        assertTrue(map.containsKey("GEOM"));
        assertEquals(GeometryTypeCodes.GEOMETRY, map.get("GEOM").intValue());
        assertTrue(map.containsKey("PT"));
        assertEquals(GeometryTypeCodes.POINT,
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

    
    @Test
    public void testTableEnvelope() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE");
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                SFSUtilities.getTableEnvelope(connection, tableLocation, ""));
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                SFSUtilities.getTableEnvelope(connection, tableLocation, "GEOM"));
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                SFSUtilities.getTableEnvelope(connection, tableLocation, "PT"));
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                SFSUtilities.getTableEnvelope(connection, tableLocation, "LINESTR"));
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                SFSUtilities.getTableEnvelope(connection, tableLocation, "PLGN"));
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                SFSUtilities.getTableEnvelope(connection, tableLocation, "MULTIPT"));
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                SFSUtilities.getTableEnvelope(connection, tableLocation, "MULTILINESTR"));
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                SFSUtilities.getTableEnvelope(connection, tableLocation, "MULTIPLGN"));
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0),
                SFSUtilities.getTableEnvelope(connection, tableLocation, "GEOMCOLLECTION"));
    }

    @Test(expected = SQLException.class)
    public void testBadTableEnvelope() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("NOGEOM");
        SFSUtilities.getTableEnvelope(connection, tableLocation, "");
        shouldNeverReachHere();
    }

    // getGeometryFields(Connection connection,String catalog, String schema, String table)
    @Test
    public void testGeometryFields1() throws SQLException {
        List<String> list = SFSUtilities.getGeometryFields(connection, "", "", "GEOMTABLE");
        assertEquals(8, list.size());
        assertTrue(list.contains("GEOM"));
        assertTrue(list.contains("PT"));
        assertTrue(list.contains("LINESTR"));
        assertTrue(list.contains("PLGN"));
        assertTrue(list.contains("MULTIPT"));
        assertTrue(list.contains("MULTILINESTR"));
        assertTrue(list.contains("MULTIPLGN"));
        assertTrue(list.contains("GEOMCOLLECTION"));
    }

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

    // getGeometryFields(Connection connection,String catalog, String schema, String table)
    @Test
    public void testGeometryFields2() throws SQLException {
        List<String> list = SFSUtilities.getGeometryFields(connection, TableLocation.parse("GEOMTABLE"));
        assertEquals(8, list.size());
        assertTrue(list.contains("GEOM"));
        assertTrue(list.contains("PT"));
        assertTrue(list.contains("LINESTR"));
        assertTrue(list.contains("PLGN"));
        assertTrue(list.contains("MULTIPT"));
        assertTrue(list.contains("MULTILINESTR"));
        assertTrue(list.contains("MULTIPLGN"));
        assertTrue(list.contains("GEOMCOLLECTION"));
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
        assertEquals(1, SFSUtilities.getFirstGeometryFieldIndex(rs));
        rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        assertEquals(-1, SFSUtilities.getFirstGeometryFieldIndex(rs));
    }

    // getFirstGeometryFieldName(ResultSet resultSet)
    @Test
    public void testFirstGeometryFieldName1() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals("GEOM", SFSUtilities.getFirstGeometryFieldName(rs));
    }

    @Test(expected = SQLException.class)
    public void testFirstGeometryFieldName2() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        SFSUtilities.getFirstGeometryFieldName(rs);
        shouldNeverReachHere();
    }

    // hasGeometryField(ResultSet resultSet)
    @Test
    public void testHasGeometryField() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertTrue(SFSUtilities.hasGeometryField(rs));
        rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        assertFalse(SFSUtilities.hasGeometryField(rs));
    }

    // getResultSetEnvelope(ResultSet resultSet)
    @Test
    public void testResultSetEnvelope1() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0), SFSUtilities.getResultSetEnvelope(rs));
    }

    @Test(expected = SQLException.class)
    public void testResultSetEnvelope2() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        SFSUtilities.getResultSetEnvelope(rs);
        shouldNeverReachHere();
    }

    // getResultSetEnvelope(ResultSet resultSet, String fieldName)
    @Test
    public void testResultSetEnvelope3() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0), SFSUtilities.getResultSetEnvelope(rs, "GEOM"));
        rs = connection.createStatement().executeQuery("SELECT * FROM GEOMTABLE");
        assertEquals(new Envelope(1.0, 3.0, 1.0, 3.0), SFSUtilities.getResultSetEnvelope(rs, "MULTILINESTR"));
    }

    @Test(expected = SQLException.class)
    public void testResultSetEnvelope4() throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM NOGEOM");
        SFSUtilities.getResultSetEnvelope(rs, "GEOM");
        shouldNeverReachHere();
    }

    // getSRID(Connection connection, TableLocation table)
    @Test
    public void testGetSRID() throws SQLException {
        assertEquals(0, SFSUtilities.getSRID(connection, TableLocation.parse("GEOMTABLE")));
        assertEquals(0, SFSUtilities.getSRID(connection, TableLocation.parse("NOGEOM")));
    }

    /**
     * Function declared in test database
     * @see org.h2gis.functions.spatial.type.GeometryTypeFromConstraint
     */
    public static class GeometryTypeFromConstraint extends DeterministicScalarFunction {
        private static final Pattern TYPE_CODE_PATTERN = Pattern.compile(
                "ST_GeometryTypeCode\\s*\\(\\s*((([\"`][^\"`]+[\"`])|(\\w+)))\\s*\\)\\s*=\\s*(\\d)+", Pattern.CASE_INSENSITIVE);
        private static final int CODE_GROUP_ID = 5;

        public GeometryTypeFromConstraint() {
            addProperty(PROP_REMARKS, "Convert H2 constraint string into a OGC geometry type index.");
            addProperty(PROP_NAME, "_GeometryTypeFromConstraint");
        }

        @Override public String getJavaStaticMethod() {return "geometryTypeFromConstraint";}

        public static int geometryTypeFromConstraint(String constraint, int numericPrecision) {
            if(constraint.isEmpty() && numericPrecision > GeometryTypeCodes.GEOMETRYZM) {
                return GeometryTypeCodes.GEOMETRY;
            }
            if(numericPrecision <= GeometryTypeCodes.GEOMETRYZM) {
                return numericPrecision;
            }
            Matcher matcher = TYPE_CODE_PATTERN.matcher(constraint);
            if(matcher.find()) {
                return Integer.valueOf(matcher.group(CODE_GROUP_ID));
            } else {
                return GeometryTypeCodes.GEOMETRY;
            }
        }
    }

    /**
     * Function declared in test database
     * @see org.h2gis.functions.spatial.spatial.properties.ST_Extent
     */
    public static class ST_Extent extends AbstractFunction implements Aggregate {
        private Envelope aggregatedEnvelope = new Envelope();

        public ST_Extent() {
            addProperty(PROP_REMARKS, "Return an envelope of the aggregation of all geometries in the table.");
        }

        @Override
        public void init(Connection connection) throws SQLException {
            aggregatedEnvelope = new Envelope();
        }

        @Override
        public int getInternalType(int[] inputTypes) throws SQLException {
            if(inputTypes.length!=1) {
                throw new SQLException(ST_Extent.class.getSimpleName()+" expect 1 argument.");
            }
            if(inputTypes[0]!=Value.GEOMETRY) {
                throw new SQLException(ST_Extent.class.getSimpleName()+" expect a geometry argument");
            }
            return Value.GEOMETRY;
        }

        @Override
        public void add(Object o) throws SQLException {
            if (o instanceof Geometry) {
                Geometry geom = (Geometry) o;
                aggregatedEnvelope.expandToInclude(geom.getEnvelopeInternal());
            }
        }

        @Override
        public Geometry getResult() throws SQLException {
            if(aggregatedEnvelope.isNull()) {
                return null;
            } else {
                return new GeometryFactory().toGeometry(aggregatedEnvelope);
            }
        }
    }

    public static class ColumnSRID extends AbstractFunction implements ScalarFunction {
        private static final String SRID_FUNC = "ST_SRID";
        private static final Pattern SRID_CONSTRAINT_PATTERN = Pattern.compile("ST_SRID\\s*\\(\\s*((([\"`][^\"`]+[\"`])|(\\w+)))\\s*\\)\\s*=\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

        public ColumnSRID() {
            addProperty(PROP_REMARKS, "Get the column SRID from constraints and data.");
            addProperty(PROP_NAME, "_ColumnSRID");
        }

        @Override
        public String getJavaStaticMethod() {
            return "getSRID";
        }

        public static int getSRIDFromConstraint(String constraint, String columnName) {
            int srid = 0;
            Matcher matcher = SRID_CONSTRAINT_PATTERN.matcher(constraint);
            while (matcher.find()) {
                String extractedColumnName = matcher.group(1).replace("\"","").replace("`","");
                int sridConstr = Integer.valueOf(matcher.group(5));
                if (extractedColumnName.equalsIgnoreCase(columnName)) {
                    if(srid != 0 && srid != sridConstr) {
                        // Two srid constraint on the same column
                        return 0;
                    }
                    srid = sridConstr;
                }
            }
            return srid;
        }

        public static String fetchConstraint(Connection connection, String catalogName, String schemaName, String tableName) throws SQLException {
            // Merge column constraint and table constraint
            PreparedStatement pst = SFSUtilities.prepareInformationSchemaStatement(connection, catalogName, schemaName,
                    tableName, "INFORMATION_SCHEMA.CONSTRAINTS", "", "TABLE_CATALOG", "TABLE_SCHEMA","TABLE_NAME");
            ResultSet rsConstraint = pst.executeQuery();
            try {
                StringBuilder constraint = new StringBuilder();
                while (rsConstraint.next()) {
                    String tableConstr = rsConstraint.getString("CHECK_EXPRESSION");
                    if(tableConstr != null) {
                        constraint.append(tableConstr);
                    }
                }
                return constraint.toString();
            } finally {
                rsConstraint.close();
                pst.close();
            }
        }

        public static int getSRID(Connection connection, String catalogName, String schemaName, String tableName, String columnName,String constraint) {
            try {
                Statement st = connection.createStatement();
                constraint+=fetchConstraint(connection, catalogName, schemaName,tableName);
                if(constraint.toUpperCase().contains(SRID_FUNC)) {
                    int srid = getSRIDFromConstraint(constraint, columnName);
                    if(srid > 0) {
                        return srid;
                    }
                }
                ResultSet rs = st.executeQuery(String.format("select ST_SRID(%s) from %s LIMIT 1;",
                        StringUtils.quoteJavaString(columnName.toUpperCase()),new TableLocation(catalogName, schemaName, tableName)));
                if(rs.next()) {
                    int srid = rs.getInt(1);
                    if(srid > 0) {
                        return srid;
                    }
                }
                rs.close();
                return 0;
            } catch (SQLException ex) {
                return 0;
            }
        }
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
    
    @Test
    public void testEstimatedExtentWithoutIndex() throws SQLException {
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE");
        assertEquals(new Envelope(1.0, 2.0, 1.0, 2.0),
                SFSUtilities.getEstimatedExtent(connection, tableLocation, "GEOM").getEnvelopeInternal());
    }
    @Test
    public void testEstimatedExtentWithIndex() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS GEOMTABLE_INDEX; CREATE TABLE GEOMTABLE_INDEX (THE_GEOM GEOMETRY);");
        st.execute("INSERT INTO GEOMTABLE_INDEX VALUES ('POLYGON ((150 360, 200 360, 200 310, 150 310, 150 360))'),('POLYGON ((195.5 279, 240 279, 240 250, 195.5 250, 195.5 279))' )");
        st.execute("CREATE SPATIAL INDEX ON GEOMTABLE_INDEX(THE_GEOM)");
        TableLocation tableLocation = TableLocation.parse("GEOMTABLE_INDEX");
        assertEquals(new Envelope(150.0 , 240.0, 250.0 , 360.0),
                SFSUtilities.getEstimatedExtent(connection, tableLocation, "THE_GEOM").getEnvelopeInternal());
    }
}
