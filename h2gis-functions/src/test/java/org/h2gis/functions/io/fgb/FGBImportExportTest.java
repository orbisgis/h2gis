package org.h2gis.functions.io.fgb;

import com.google.common.io.LittleEndianDataInputStream;
import org.h2.util.geometry.EWKTUtils;
import org.h2.util.geometry.JTSUtils;
import org.h2.value.Value;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueVarchar;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.factory.H2GISFunctions;
import org.h2gis.functions.io.fgb.fileTable.FGBDriver;
import org.h2gis.functions.io.geojson.*;
import org.h2gis.postgis_jts.PostGISDBFactory;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SpatialResultSet;
import org.h2gis.utilities.URIUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.NodeItem;
import org.wololo.flatgeobuf.PackedRTree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class FGBImportExportTest {


    private static Connection connection;
    private static final String DB_NAME = "FGBImportExportTest";
    private static final WKTReader WKTREADER = new WKTReader();
    private static final Logger log = LoggerFactory.getLogger(FGBImportExportTest.class);

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = JDBCUtilities.wrapConnection(H2GISDBFactory.createSpatialDataBase(DB_NAME));
        H2GISFunctions.registerFunction(connection.createStatement(), new FGBWrite(), "");
        H2GISFunctions.registerFunction(connection.createStatement(), new FGBRead(), "");
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
            stat.execute("create table TABLE_POINTS(id int, the_geom GEOMETRY(POINT))");
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

    @Test
    public void testPackedRTree() throws Exception {
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL GEOJSONREAD('" + FGBImportExportTest.class.getResource("countries.geojson") + "', 'COUNTRIES_GEOJSON', true);");
        }

        List<PackedRTree.FeatureItem> nodeItemList = new ArrayList<>();
        int index=0;
        List<String> ids = new ArrayList<>();
        try (SpatialResultSet geojsonRs = connection.createStatement().executeQuery(
                "SELECT the_geom, id, name FROM COUNTRIES_GEOJSON ORDER BY ID").unwrap(SpatialResultSet.class)) {
            while (geojsonRs.next()) {
                Geometry geometry = geojsonRs.getGeometry();
                Envelope geomEnvelope = geometry.getEnvelopeInternal();
                PackedRTree.FeatureItem featureItem = new PackedRTree.FeatureItem();
                featureItem.nodeItem = new NodeItem(geomEnvelope.getMinX(), geomEnvelope.getMinY(),
                        geomEnvelope.getMaxX(), geomEnvelope.getMaxY(), index++);
                ids.add(geojsonRs.getString("ID"));
                nodeItemList.add(featureItem);
            }
            NodeItem extend = new NodeItem(0);
            nodeItemList.forEach(x -> extend.expand(x.nodeItem));
            PackedRTree.hilbertSort(nodeItemList, extend);
            short nodeSize = 16;
            PackedRTree packedRTree = new PackedRTree(nodeItemList, nodeSize);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream((int)PackedRTree.calcSize(
                    nodeItemList.size(), nodeSize));
            packedRTree.write(byteArrayOutputStream);
            // Read RTree
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                // Envelope over Indonesia, Malaysia and Philippines
                Envelope queryEnv = new WKTReader().read(
                        "POLYGON ((115.97 5.17,115.95 11.78,125.031 11.88,124.60 5.29,115.97 5.17))").getEnvelopeInternal();
                long[] fids = new long[nodeItemList.size()];
                for (long id = 0; id < fids.length; id++) {
                    fids[(int) id] = id;
                }
                HeaderMeta headerMeta = new HeaderMeta();
                headerMeta.featuresCount = nodeItemList.size();
                headerMeta.indexNodeSize = nodeSize;
                long[] featuresAddress = PackedRTree.readFeatureOffsets(new LittleEndianDataInputStream(byteArrayInputStream),
                        fids, headerMeta);
                byteArrayInputStream.reset();
                PackedRTree.SearchResult searchResult = PackedRTree.search(byteArrayInputStream, 0, nodeItemList.size(),
                        nodeSize, queryEnv);
                assertEquals(3, searchResult.hits.size());
                Set<String> hitsIds = new TreeSet<>();
                for (PackedRTree.SearchHit hit : searchResult.hits) {
                    hitsIds.add(ids.get((int)hit.offset));
                }
                Iterator<String> it = hitsIds.iterator();
                assertEquals("IDN", it.next());
                assertEquals("MYS", it.next());
                assertEquals("PHL", it.next());

                queryEnv = new Envelope(-1.504, 5.577, 52.723, 61.934);
                byteArrayInputStream.reset();
                searchResult = PackedRTree.search(byteArrayInputStream, 0, nodeItemList.size(),
                        nodeSize, queryEnv);
                assertEquals(4, searchResult.hits.size());
                hitsIds = new TreeSet<>();
                for (PackedRTree.SearchHit hit : searchResult.hits) {
                    System.out.println(ids.get((int)hit.offset));
                    hitsIds.add(ids.get((int)hit.offset));
                }
            }
        }
    }
    @Test
    public void testExternalFGPSpatialIndex() throws Exception {
        File tempOutputFile = new File("target/countries_exported.fgb");
        //tempOutputFile.deleteOnExit();
        try (Statement stat = connection.createStatement()) {
            stat.execute("CALL FGBRead('" + FGBImportExportTest.class.getResource("countries.fgb") + "', 'COUNTRIES_FGB', true);");
            stat.execute("CALL FGBWrite('target/countries_exported.fgb', 'COUNTRIES_FGB', true, 'createIndex=false');");
        }
        assertTrue(tempOutputFile.exists());
        FGBDriver fgbDriver = new FGBDriver();
        fgbDriver.initDriverFromFile(tempOutputFile);
        fgbDriver.cacheFeatureAddressFromIndex();
    }

    @Test
    public void testRandomFGPReadGdal() throws Exception {
        FGBDriver fgbDriver = new FGBDriver();
        File file = URIUtilities.fileFromString(Objects.requireNonNull(FGBImportExportTest.class.getResource(
                "countries_gdal.fgb")).getFile());
        fgbDriver.initDriverFromFile(file);
        assertEquals(179, fgbDriver.getRowCount());
        assertEquals(3, fgbDriver.getFieldCount());
        fgbDriver.cacheFeatureAddressFromIndex();
    }
}