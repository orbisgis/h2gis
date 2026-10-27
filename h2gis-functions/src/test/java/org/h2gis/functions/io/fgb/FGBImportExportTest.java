package org.h2gis.functions.io.fgb;

import org.h2.index.Cursor;
import org.h2.value.Value;
import org.h2.util.StringUtils;
import org.h2.value.ValueVarchar;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.io.fgb.fileTable.FGBDriver;
import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.URIUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.flatgeobuf.ColumnMeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FGBImportExportTest {


    private static Connection connection;
    private static final String DB_NAME = "FGBImportExportTest";
    private static final Logger log = LoggerFactory.getLogger(FGBImportExportTest.class);

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = JDBCUtilities.wrapConnection(H2GISDBFactory.createSpatialDataBase(DB_NAME));
        H2GISFunctions.registerFunction(connection.createStatement(), new FGBTable(), "");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testWriteReadFGBPoint(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "points.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), area float, " +
                    "perimeter double precision, name varchar, smallint_col smallint, int_col integer, " +
                    "numeric_col NUMERIC(10, 1),  real_col real, float_precision_col float(1), bigint_col bigint," +
                    "timestamp_col TIMESTAMP, date_col DATE )");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 12.10, 156.12345678, 'OrbisGIS', 1, 1,10.5,12.1234, 12.8, 1000000, CAST('2026-01-19 10:00' AS TIMESTAMP), '2026-01-19')");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 10.25,  156.12345678, 'NoiseModelling', null, 1,10.5,12.1234, 12.8, null, CAST('2026-01-18 12:00' AS TIMESTAMP), '2026-01-18')");
            stat.execute("CALL FGBWrite('"+file+"', 'TABLE_POINTS', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_POINTS', true);");

            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINTS order by  ID");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
            assertEquals("POINT (140 260)", rs.getString("THE_GEOM"));
            assertEquals(12.1, rs.getFloat("area"), 10-2);
            assertEquals("OrbisGIS", rs.getString("name"));
            assertEquals(1, rs.getObject("smallint_col"));
            assertEquals(1, rs.getObject("int_col"));
            assertEquals(10.5, rs.getObject("numeric_col"));
            assertEquals(12.1234 , rs.getFloat("real_col"), 10-3);
            assertEquals(12.8, rs.getFloat("float_precision_col"), 10-1);
            assertEquals(1000000L, rs.getObject("bigint_col"));
            assertEquals("2026-01-19 10:00:00.0", rs.getTimestamp("timestamp_col").toString());
            assertEquals("2026-01-19 00:00:00.0", rs.getTimestamp("date_col").toString());
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("ID"));
            assertEquals("POINT (150 290)", rs.getString("THE_GEOM"));
            assertEquals(10.25, rs.getFloat("area"), 10-2);
            assertEquals("2026-01-18 12:00:00.0", rs.getTimestamp("timestamp_col").toString());
            assertEquals("2026-01-18 00:00:00.0", rs.getTimestamp("date_col").toString());
            assertFalse(rs.next());
        }
    }

    @Test
    public void testFGBEngine(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "points.fgb");
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), land varchar)");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 'corn')");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 'grass')");
            stat.execute("CALL FGBWrite('"+file+"', 'TABLE_POINTS', true);");

            FGBDriver fgbDriver = new FGBDriver();
            fgbDriver.initDriverFromFile(file);
            assertEquals(3, fgbDriver.getFieldCount());
            assertEquals(2, fgbDriver.getRowCount());

            assertEquals("POINT (140 260)", fgbDriver.getField(0, 0).getString());
            assertEquals(1, fgbDriver.getField(0, 1).getInt());
            assertEquals("corn", fgbDriver.getField(0, 2).getString());

            assertEquals("POINT (150 290)", fgbDriver.getField(1, 0).getString());
            assertEquals(2, fgbDriver.getField(1, 1).getInt());
            assertEquals("grass", fgbDriver.getField(1, 2).getString());
        }
    }

    @Test
    public void testFGBFileTable(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "points.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), land varchar)");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 'corn')");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 'grass')");
            stat.execute("CALL FGBWrite('"+file+"', 'TABLE_POINTS', true);");
            stat.execute("CALL FILE_TABLE('"+file+"', 'points');");

            ResultSet rs = stat.executeQuery("SELECT * FROM points");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
            assertEquals("POINT (140 260)", rs.getString("THE_GEOM"));
            assertEquals("corn", rs.getString("land"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("ID"));
            assertEquals("POINT (150 290)", rs.getString("THE_GEOM"));
            assertEquals("grass", rs.getString("land"));
            assertFalse(rs.next());
        }
    }

    /**
     * Use externally generated FGP and GeoJSON files from flatgeobuf repository
     */
    @Test
    public void testExternalFGPImport() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL FGBRead('" + FGBImportExportTest.class.getResource("countries.fgb") + "', 'COUNTRIES_FGB', true);");
            stat.execute("CALL GEOJSONREAD('" + FGBImportExportTest.class.getResource("countries.geojson") + "', 'COUNTRIES_GEOJSON', true);");
            // Compare results
        }
        try (ResultSet geojsonRs = connection.createStatement().executeQuery("SELECT the_geom, id, name FROM COUNTRIES_GEOJSON ORDER BY ID")) {
            try (ResultSet fgbRs = connection.createStatement().executeQuery("SELECT the_geom, id, name FROM COUNTRIES_FGB ORDER BY ID")) {
                while (geojsonRs.next()) {
                    assertTrue(fgbRs.next());
                    assertEquals(geojsonRs.getString(2), fgbRs.getString(2));
                    assertEquals(geojsonRs.getString(3), fgbRs.getString(3));
                    Geometry geojsonGeom = (Geometry) geojsonRs.getObject(1);
                    Geometry fgbGeom = (Geometry) fgbRs.getObject(1);
                    assertNotNull(geojsonGeom);
                    assertNotNull(fgbGeom);
                    assertEquals(0, geojsonGeom.getCentroid().getCoordinate().distance(fgbGeom.getCentroid().getCoordinate()), 1e-6);
                }
            }
        }
    }

    @Test
    public void testFGPIndex() throws IOException {
        FGBDriver fgbDriver = new FGBDriver();
        File file = URIUtilities.fileFromString(Objects.requireNonNull(FGBImportExportTest.class.getResource(
                "countries.fgb")).getFile());
        fgbDriver.initDriverFromFile(file);
        fgbDriver.cacheFeatureAddressFromIndex();


        assertEquals(179, fgbDriver.getRowCount());
        assertEquals(3, fgbDriver.getFieldCount());
        Object idObj = fgbDriver.getField(50, 1);
        assertInstanceOf(ValueVarchar.class, idObj);
        assertEquals("LVA", ((ValueVarchar) idObj).getString());

        idObj = fgbDriver.getField(35, 1);
        assertInstanceOf(ValueVarchar.class, idObj);
        assertEquals("BTN", ((ValueVarchar) idObj).getString());

        idObj = fgbDriver.getField(100, 1);
        assertInstanceOf(ValueVarchar.class, idObj);
        assertEquals("KWT", ((ValueVarchar) idObj).getString());
    }

    @Test
    public void testRandomFGPRead() throws Exception {
        FGBDriver fgbDriver = new FGBDriver();
        File file = URIUtilities.fileFromString(Objects.requireNonNull(FGBImportExportTest.class.getResource(
                "countries.fgb")).getFile());
        fgbDriver.initDriverFromFile(file);
        assertEquals(179, fgbDriver.getRowCount());
        assertEquals(3, fgbDriver.getFieldCount());
        Object idObj = fgbDriver.getField(50, 1);
        assertInstanceOf(ValueVarchar.class, idObj);
        assertEquals("LVA", ((ValueVarchar) idObj).getString());

        idObj = fgbDriver.getField(35, 1);
        assertInstanceOf(ValueVarchar.class, idObj);
        assertEquals("BTN", ((ValueVarchar) idObj).getString());

        idObj = fgbDriver.getField(100, 1);
        assertInstanceOf(ValueVarchar.class, idObj);
        assertEquals("KWT", ((ValueVarchar) idObj).getString());
    }

    private static <R> List<R> idsFromCursor(Cursor cursor, FGBDriver fgbDriver, String columnName,
                                             Function<? super Value, ? extends R> var1) {
        List<Value> values = new ArrayList<>();
        int indexColum = 0;
        for (ColumnMeta column : fgbDriver.getHeader().columns) {
            if(column.name.equalsIgnoreCase(columnName)) {
                if(indexColum >= fgbDriver.getGeometryFieldIndex()) {
                    indexColum++;
                }
                break;
            }
            indexColum++;
        }
        while(cursor.next()) {
            values.add(cursor.get().getValue(indexColum));
        }
        return values.stream().map(var1).sorted().collect(Collectors.toList());
    }

    @Test
    public void testFGPReadGdal() throws Exception {
        FGBDriver fgbDriver = new FGBDriver();
        File file = URIUtilities.fileFromString(Objects.requireNonNull(FGBImportExportTest.class.getResource(
                "countries_gdal.fgb")).getFile());
        fgbDriver.initDriverFromFile(file);
        assertEquals(179, fgbDriver.getRowCount());
        assertEquals(3, fgbDriver.getFieldCount());
        Cursor cursor = fgbDriver.queryIndex(new Envelope(115.95, 125.031, 5.17, 11.88));
        assertIterableEquals(Arrays.asList("IDN", "MYS", "PHL"),
                idsFromCursor(cursor, fgbDriver, "ID", Value::getString));
    }


    @Test
    public void testReadWriteSpatialIndex(@TempDir File temporaryDirectory) throws Exception {
        File tempOutputFile = new File(temporaryDirectory, "countries_exported.fgb");
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL FGBRead('" + FGBImportExportTest.class.getResource("countries.fgb") + "', 'COUNTRIES_FGB', true);");
            stat.execute("DROP TABLE IF EXISTS COUNTRIES");
            stat.execute("CREATE TABLE COUNTRIES AS SELECT * FROM COUNTRIES_FGB ORDER BY ID");
            stat.execute("CALL FGBWrite('"+tempOutputFile+"', 'COUNTRIES', true, 'createIndex=true');");
        }
        FGBDriver fgbDriver = new FGBDriver();
        fgbDriver.initDriverFromFile(tempOutputFile);
        fgbDriver.cacheFeatureAddressFromIndex();

        assertEquals(179, fgbDriver.getRowCount());
        assertEquals(3, fgbDriver.getFieldCount());
        Cursor cursor = fgbDriver.queryIndex(new Envelope(115.95, 125.031, 5.17, 11.88));
        assertIterableEquals(Arrays.asList("IDN", "MYS", "PHL"),
                idsFromCursor(cursor, fgbDriver, "ID", Value::getString));

        // Check random access built from Spatial Index offsets

        Object idObj = fgbDriver.getField(50, 1);
        assertInstanceOf(ValueVarchar.class, idObj);
        assertEquals("LVA", ((ValueVarchar) idObj).getString());

        idObj = fgbDriver.getField(35, 1);
        assertInstanceOf(ValueVarchar.class, idObj);
        assertEquals("BTN", ((ValueVarchar) idObj).getString());

        idObj = fgbDriver.getField(100, 1);
        assertInstanceOf(ValueVarchar.class, idObj);
        assertEquals("KWT", ((ValueVarchar) idObj).getString());
    }

    @Test
    public void testWriteReadFGBGeometry(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "points.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY)");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)')");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)')");
            stat.execute("CALL FGBWrite('"+file+"', 'TABLE_POINTS', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_POINTS', true);");

            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINTS");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
            assertEquals("POINT (140 260)", rs.getString("THE_GEOM"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("ID"));
            assertEquals("POINT (150 290)", rs.getString("THE_GEOM"));
            assertFalse(rs.next());
        }
    }

    /**
     * Test Read Write PointZ geometries
     * @throws Exception
     */
    @Test
    public void testWriteReadFGBPointZSrid(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "pointsz.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINTZ, 4326))");
            stat.execute("insert into TABLE_POINTS values(1, 'SRID=4326;POINT Z (140 260 3)')");
            stat.execute("insert into TABLE_POINTS values(2, 'SRID=4326;POINT Z (150 290 3)')");
            stat.execute("CALL FGBWrite('"+file+"', 'TABLE_POINTS', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_POINTS', true);");

            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINTS");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
            assertEquals("SRID=4326;POINT Z (140 260 3)", rs.getString("THE_GEOM"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("ID"));
            assertEquals("SRID=4326;POINT Z (150 290 3)", rs.getString("THE_GEOM"));
            assertFalse(rs.next());
        }
    }

    /**
     * Test Read Write PointZM geometries
     * @throws Exception
     */
    @Test
    public void testWriteReadFGBPointZMSrid(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "pointsz.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINTZM, 4326))");
            stat.execute("insert into TABLE_POINTS values(1, 'SRID=4326;POINT ZM (140 260 3 5)')");
            stat.execute("insert into TABLE_POINTS values(2, 'SRID=4326;POINT ZM (150 290 3 10)')");
            stat.execute("CALL FGBWrite('"+file+"', 'TABLE_POINTS', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_POINTS', true);");

            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINTS");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
            assertEquals("SRID=4326;POINT ZM (140 260 3 5)", rs.getString("THE_GEOM"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("ID"));
            assertEquals("SRID=4326;POINT ZM (150 290 3 10)", rs.getString("THE_GEOM"));
            assertFalse(rs.next());
        }
    }


    /**
     * Test Read Write LineStringZ geometries
     * @throws Exception
     */
    @Test
    public void testWriteReadFGBLineStringZSrid(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "linestringz.fgb");
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRING");
            stat.execute("create table TABLE_LINESTRING(id int, the_geom GEOMETRY(LINESTRINGZ, 4326))");
            stat.execute("insert into TABLE_LINESTRING values(1, 'SRID=4326;LINESTRING Z (-2.75686 47.6546 55.1,-2.7559 47.655 55.1,-2.7555 47.65511 51.1)')");
            stat.execute("insert into TABLE_LINESTRING values(2, 'SRID=4326;LINESTRING Z (-2.75696 47.6846 58.1,-2.7559 47.66 65.1,-2.7555 47.65511 65.1)')");
            stat.execute("CALL FGBWrite('"+file+"', 'TABLE_LINESTRING', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRING");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_LINESTRING', true);");

            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_LINESTRING");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
            assertEquals("SRID=4326;LINESTRING Z (-2.75686 47.6546 55.1, -2.7559 47.655 55.1, -2.7555 47.65511 51.1)", rs.getString("THE_GEOM"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("ID"));
            assertEquals("SRID=4326;LINESTRING Z (-2.75696 47.6846 58.1, -2.7559 47.66 65.1, -2.7555 47.65511 65.1)", rs.getString("THE_GEOM"));
            assertFalse(rs.next());
        }
    }

    /**
     * Test Read Write PointZM geometries
     * @throws Exception
     */
    @Test
    public void testWriteReadFGBLineZMSrid(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "linestringzm.fgb");
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRING");
            stat.execute("create table TABLE_LINESTRING(id int, the_geom GEOMETRY(LINESTRINGZM, 4326))");
            stat.execute("insert into TABLE_LINESTRING values(1, 'SRID=4326;LINESTRING ZM (-2.75686 47.6546 55.1 5,-2.7559 47.655 55.1 5,-2.7555 47.65511 51.1 6)')");
            stat.execute("insert into TABLE_LINESTRING values(2, 'SRID=4326;LINESTRING ZM (-2.75696 47.6846 58.1 8,-2.7559 47.66 65.1 8,-2.7555 47.65511 65.1 1)')");
            stat.execute("CALL FGBWrite('"+file+"', 'TABLE_LINESTRING', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_LINESTRING");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_LINESTRING', true);");

            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_LINESTRING");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
            assertEquals("SRID=4326;LINESTRING ZM (-2.75686 47.6546 55.1 5, -2.7559 47.655 55.1 5, -2.7555 47.65511 51.1 6)", rs.getString("THE_GEOM"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("ID"));
            assertEquals("SRID=4326;LINESTRING ZM (-2.75696 47.6846 58.1 8, -2.7559 47.66 65.1 8, -2.7555 47.65511 65.1 1)", rs.getString("THE_GEOM"));
            assertFalse(rs.next());
        }
    }



    @Test
    public void testWriteReadFGBPointSrid(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "points.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT, 4326))");
            stat.execute("insert into TABLE_POINTS values(1, 'SRID=4326;POINT (140 260)')");
            stat.execute("insert into TABLE_POINTS values(2, 'SRID=4326;POINT (150 290)')");
            stat.execute("CALL FGBWrite('"+file+"', 'TABLE_POINTS', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_POINTS', true);");

            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINTS");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
            assertEquals("SRID=4326;POINT (140 260)", rs.getString("THE_GEOM"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("ID"));
            assertEquals("SRID=4326;POINT (150 290)", rs.getString("THE_GEOM"));
            assertFalse(rs.next());
        }
    }

    @Test
    public void testWriteSelectReadFGBPoint(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "points.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), area float, " +
                    "perimeter double precision, name varchar, smallint_col smallint, int_col integer, " +
                    "numeric_col NUMERIC(10, 1),  real_col real, float_precision_col float(1), bigint_col bigint )");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 12.10, 156.12345678, 'OrbisGIS', 1, 1,10.5,12.1234, 12.8, 1000000)");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 10.25,  156.12345678, 'NoiseModelling', null, 1,10.5,12.1234, 12.8, null)");
            stat.execute("CALL FGBWrite('"+file+"', '(SELECT * FROM TABLE_POINTS WHERE ID=1)', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_POINTS', true);");
            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINTS");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
            assertEquals("POINT (140 260)", rs.getString("THE_GEOM"));
            assertEquals(12.1, rs.getFloat("area"), 10-2);
            assertEquals("OrbisGIS", rs.getString("name"));
            assertEquals(1, rs.getObject("smallint_col"));
            assertEquals(1, rs.getObject("int_col"));
            assertEquals(10.5, rs.getObject("numeric_col"));
            assertEquals(12.1234 , rs.getFloat("real_col"), 10-3);
            assertEquals(12.8, rs.getFloat("float_precision_col"), 10-1);
            assertEquals(1000000L, rs.getObject("bigint_col"));
            assertFalse(rs.next());
        }
    }
    @Test
    public void testWriteReadEmptyFGB(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "empty_file.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT, 4326))");
            stat.execute("CALL FGBWrite('"+file+"', 'TABLE_POINTS', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_POINTS', true);");
            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINTS");
            List<String> columns = JDBCUtilities.getColumnNames(rs.getMetaData());
            assertEquals(2, columns.size());
            assertTrue(columns.contains("THE_GEOM"));
            assertTrue(columns.contains("ID"));
            assertFalse(rs.next());
        }
    }

    @Test
    public void testWriteReadEmptyFGB2(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "empty_file.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT, 4326))");
            stat.execute("INSERT INTO TABLE_POINTS VALUES(1, 'SRID=4326;POINT(2 2)')");
            stat.execute("CALL FGBWrite('"+file+"', '(SELECT * FROM TABLE_POINTS WHERE the_geom && ST_BUFFER(''POINT(-1 -1)''::GEOMETRY, 0.001))', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_POINTS', true);");
            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINTS");
            List<String> columns = JDBCUtilities.getColumnNames(rs.getMetaData());
            assertEquals(2, columns.size());
            assertTrue(columns.contains("THE_GEOM"));
            assertTrue(columns.contains("ID"));
            assertFalse(rs.next());
        }
    }

    @Test
    public void testWriteReadFGBPointBoolean(@TempDir Path tempDir) throws Exception {
        File file = Path.of(tempDir.toString(), "points.fgb").toFile();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), area float, " +
                    "perimeter double precision, name varchar, smallint_col smallint, int_col integer, " +
                    "numeric_col NUMERIC(10, 1),  real_col real, float_precision_col float(1), bigint_col bigint, boolean_col BOOLEAN DEFAULT FALSE )");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 12.10, 156.12345678, 'OrbisGIS', 1, 1,10.5,12.1234, 12.8, 1000000, true)");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 10.25,  156.12345678, 'NoiseModelling', null, 1,10.5,12.1234, 12.8, null, false)");
            stat.execute(String.format("CALL FGBWrite('%s', 'TABLE_POINTS', true);", file));
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute(String.format("CALL FGBRead('%s', 'TABLE_POINTS', true);", file));

            ResultSet rs = stat.executeQuery("SELECT * FROM TABLE_POINTS");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt("ID"));
            assertEquals("POINT (140 260)", rs.getString("THE_GEOM"));
            assertEquals(12.1, rs.getFloat("area"), 10-2);
            assertEquals("OrbisGIS", rs.getString("name"));
            assertEquals(1, rs.getObject("smallint_col"));
            assertEquals(1, rs.getObject("int_col"));
            assertEquals(10.5, rs.getObject("numeric_col"));
            assertEquals(12.1234 , rs.getFloat("real_col"), 10-3);
            assertEquals(12.8, rs.getFloat("float_precision_col"), 10-1);
            assertEquals(1000000L, rs.getObject("bigint_col"));
            assertTrue(rs.getBoolean("boolean_col"));
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("ID"));
            assertEquals("POINT (150 290)", rs.getString("THE_GEOM"));
            assertEquals(10.25, rs.getFloat("area"), 10-2);
            assertFalse(rs.getBoolean("boolean_col"));
            assertFalse(rs.next());
        }
    }

    @Test
    public void testSaveSelectSrid(@TempDir File temporaryDirectory) throws Exception {
        File file = new File(temporaryDirectory, "select_srid.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL FGBWrite('"+file+"', '(SELECT ST_GEOMFROMTEXT(''POINT(0 0)'', 4326) As the_geom, 1 as id)', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('"+file+"', 'TABLE_POINTS', true);");
            assertEquals(1, JDBCUtilities.getRowCount(connection,"TABLE_POINTS"));
            assertEquals(4326, GeometryTableUtilities.getSRID(connection,"TABLE_POINTS"));
        }
    }
    /**
     * @return the number of rows of the query a that are not in b, and of b not in a
     */
    private static long[] except(Statement stat, String a, String b) throws SQLException {
        ResultSet res = stat.executeQuery("SELECT (SELECT COUNT(*) FROM (" + a + " EXCEPT " + b + ")), "
                + "(SELECT COUNT(*) FROM (" + b + " EXCEPT " + a + ")), (SELECT COUNT(*) FROM (" + a + ")), (SELECT COUNT(*) FROM (" + b + "))");
        assertTrue(res.next());
        long[] counts = {res.getLong(1), res.getLong(2), res.getLong(3), res.getLong(4)};
        res.close();
        return counts;
    }

    @Test
    public void testFGBTable() throws SQLException {
        try (Statement stat = connection.createStatement()) {
            String path = StringUtils.quoteStringSQL(FGBImportExportTest.class.getResource("countries.fgb").getPath());
            stat.execute("DROP TABLE IF EXISTS COUNTRIES_LINKED");
            stat.execute("CALL FILE_TABLE(" + path + ", 'COUNTRIES_LINKED')");
            long[] counts = except(stat, "SELECT THE_GEOM, ID, NAME FROM COUNTRIES_LINKED", "SELECT * FROM FGBTable(" + path + ")");
            assertEquals(0, counts[0]);
            assertEquals(0, counts[1]);
            assertEquals(179, counts[3]);
            // some columns, in the order of the list
            counts = except(stat, "SELECT NAME, ID FROM COUNTRIES_LINKED", "SELECT * FROM FGBTable(" + path + ", 'name, ID')");
            assertEquals(0, counts[0]);
            assertEquals(0, counts[1]);
            ResultSet res = stat.executeQuery("SELECT ST_SRID(THE_GEOM) FROM FGBTable(" + path + ", 'THE_GEOM') LIMIT 1");
            assertTrue(res.next());
            assertEquals(4326, res.getInt(1));
            res.close();
            assertThrows(SQLException.class, () -> stat.executeQuery("SELECT * FROM FGBTable(" + path + ", 'NOPE')"));
            assertThrows(SQLException.class, () -> stat.executeQuery("SELECT * FROM FGBTable('target/does_not_exist.fgb')"));
            stat.execute("DROP TABLE COUNTRIES_LINKED");
        }
    }
    /**
     * The rows read by FGBDriver in any order, the attributes alone or with the
     * geometry, are the ones imported by FGBRead
     */
    @Test
    public void testFGBDriverRandomAccess() throws Exception {
        File file = new File(FGBImportExportTest.class.getResource("countries.fgb").getFile());
        // reference: the file imported by FGBRead, in the order of the file
        List<List<String>> expected = new ArrayList<>();
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL FGBRead(" + StringUtils.quoteStringSQL(file.getPath()) + ", 'COUNTRIES_REFERENCE', true)");
            try (ResultSet res = stat.executeQuery("SELECT THE_GEOM, ID, NAME FROM COUNTRIES_REFERENCE ORDER BY _ROWID_")) {
                while (res.next()) {
                    expected.add(Arrays.asList(res.getString(1), res.getString(2), res.getString(3)));
                }
            }
            stat.execute("DROP TABLE COUNTRIES_REFERENCE");
        }
        assertEquals(179, expected.size());
        FGBDriver driver = new FGBDriver();
        driver.initDriverFromFile(file);
        try {
            assertEquals(0, driver.getGeometryFieldIndex());
            // in order, the attributes before the geometry
            for (int row = 0; row < expected.size(); row++) {
                for (int column : new int[]{2, 1, 0}) {
                    assertEquals(expected.get(row).get(column), driver.getField(row, column).getString(), "row " + row + " column " + column);
                }
            }
            // backward
            for (int row = expected.size() - 1; row >= 0; row--) {
                for (int column = 0; column < 3; column++) {
                    assertEquals(expected.get(row).get(column), driver.getField(row, column).getString(), "row " + row + " column " + column);
                }
            }
            // random order
            java.util.Random random = new java.util.Random(42);
            for (int i = 0; i < 1000; i++) {
                int row = random.nextInt(expected.size());
                int column = random.nextInt(3);
                assertEquals(expected.get(row).get(column), driver.getField(row, column).getString(), "row " + row + " column " + column);
            }
        } finally {
            driver.close();
        }
        // jumps forward in a file not read yet, with and without the cache of the row addresses
        for (boolean cache : new boolean[]{true, false}) {
            FGBDriver jumpDriver = new FGBDriver();
            jumpDriver.initDriverFromFile(file);
            jumpDriver.setCacheRowAddress(cache);
            try {
                for (int row : new int[]{100, 3, 150, 151, 50, 178, 0, 120}) {
                    for (int column = 0; column < 3; column++) {
                        assertEquals(expected.get(row).get(column), jumpDriver.getField(row, column).getString(),
                                "cache " + cache + " row " + row + " column " + column);
                    }
                }
            } finally {
                jumpDriver.close();
            }
        }
    }
}