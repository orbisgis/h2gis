package org.h2gis.functions.io.fgb;

import org.h2.index.Cursor;
import org.h2.value.Value;
import org.h2.value.ValueVarchar;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.io.fgb.fileTable.FGBDriver;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.URIUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.flatgeobuf.ColumnMeta;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FGBImportExportTest {


    private static Connection connection;
    private static final String DB_NAME = "FGBImportExportTest";
    private static final Logger log = LoggerFactory.getLogger(FGBImportExportTest.class);

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = JDBCUtilities.wrapConnection(H2GISDBFactory.createSpatialDataBase(DB_NAME));
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testWriteReadFGBPoint() throws Exception {
        File file = new File("target/points.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), area float, " +
                    "perimeter double precision, name varchar, smallint_col smallint, int_col integer, " +
                    "numeric_col NUMERIC(10, 1),  real_col real, float_precision_col float(1), bigint_col bigint )");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 12.10, 156.12345678, 'OrbisGIS', 1, 1,10.5,12.1234, 12.8, 1000000)");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 10.25,  156.12345678, 'NoiseModelling', null, 1,10.5,12.1234, 12.8, null)");
            stat.execute("CALL FGBWrite('target/points.fgb', 'TABLE_POINTS', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('target/points.fgb', 'TABLE_POINTS', true);");

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
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("ID"));
            assertEquals("POINT (150 290)", rs.getString("THE_GEOM"));
            assertEquals(10.25, rs.getFloat("area"), 10-2);
            assertFalse(rs.next());
        }
    }

    @Test
    public void testFGBEngine() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), land varchar)");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 'corn')");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 'grass')");
            stat.execute("CALL FGBWrite('target/points.fgb', 'TABLE_POINTS', true);");

            File fgbFile = new File("target/points.fgb");
            FGBDriver fgbDriver = new FGBDriver();
            fgbDriver.initDriverFromFile(fgbFile);
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
    public void testFGBFileTable() throws Exception {
        File file = new File("target/points.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), land varchar)");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 'corn')");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 'grass')");
            stat.execute("CALL FGBWrite('target/points.fgb', 'TABLE_POINTS', true);");
            stat.execute("CALL FILE_TABLE('target/points.fgb', 'points');");

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
    public void testReadWriteSpatialIndex() throws Exception {
        File tempOutputFile = new File("target/countries_exported.fgb");
        tempOutputFile.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL FGBRead('" + FGBImportExportTest.class.getResource("countries.fgb") + "', 'COUNTRIES_FGB', true);");
            stat.execute("DROP TABLE IF EXISTS COUNTRIES");
            stat.execute("CREATE TABLE COUNTRIES AS SELECT * FROM COUNTRIES_FGB ORDER BY ID");
            stat.execute("CALL FGBWrite('target/countries_exported.fgb', 'COUNTRIES', true, 'createIndex=true');");
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
    public void testWriteReadFGBGeometry() throws Exception {
        File file = new File("target/points.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY)");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)')");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)')");
            stat.execute("CALL FGBWrite('target/points.fgb', 'TABLE_POINTS', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('target/points.fgb', 'TABLE_POINTS', true);");

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

    @Test
    public void testWriteReadFGBPointSrid() throws Exception {
        File file = new File("target/points.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT, 4326))");
            stat.execute("insert into TABLE_POINTS values(1, 'SRID=4326;POINT (140 260)')");
            stat.execute("insert into TABLE_POINTS values(2, 'SRID=4326;POINT (150 290)')");
            stat.execute("CALL FGBWrite('target/points.fgb', 'TABLE_POINTS', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('target/points.fgb', 'TABLE_POINTS', true);");

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
    public void testWriteSelectReadFGBPoint() throws Exception {
        File file = new File("target/points.fgb");
        file.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT), area float, " +
                    "perimeter double precision, name varchar, smallint_col smallint, int_col integer, " +
                    "numeric_col NUMERIC(10, 1),  real_col real, float_precision_col float(1), bigint_col bigint )");
            stat.execute("insert into TABLE_POINTS values(1, 'POINT (140 260)', 12.10, 156.12345678, 'OrbisGIS', 1, 1,10.5,12.1234, 12.8, 1000000)");
            stat.execute("insert into TABLE_POINTS values(2, 'POINT (150 290)', 10.25,  156.12345678, 'NoiseModelling', null, 1,10.5,12.1234, 12.8, null)");
            stat.execute("CALL FGBWrite('target/points.fgb', '(SELECT * FROM TABLE_POINTS WHERE ID=1)', true);");
            stat.execute("DROP TABLE IF EXISTS TABLE_POINTS");
            stat.execute("CALL FGBRead('target/points.fgb', 'TABLE_POINTS', true);");
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
}