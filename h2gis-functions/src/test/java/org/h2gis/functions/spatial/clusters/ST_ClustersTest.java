/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.clusters;


import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class dedicated to cluster functions.
 *
 * @author Erwan Bocher (CNRS)
 */
public class ST_ClustersTest {

    private static Connection connection;
    private static Statement st;
    private static final String DB_NAME = "ST_ClustersTest";

    @BeforeAll
    static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME, true);
    }

    @BeforeEach
    void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @Test
    public void st_clusterDBSCAN1() throws SQLException {
        st.execute("DROP TABLE IF EXISTS sample_points;" +
                "CREATE TABLE sample_points (id INT ," +
                " the_geom  GEOMETRY(POINT, 2154));" +
                "INSERT INTO sample_points VALUES" +
                "    (1,  ST_GeomFromText('POINT(100 100)', 2154))," +
                "    (2,  ST_GeomFromText('POINT(120 110)', 2154))," +
                "    (3,  ST_GeomFromText('POINT(110 130)', 2154))," +
                "    (4,  ST_GeomFromText('POINT(400 400)', 2154))," +
                "    (5,  ST_GeomFromText('POINT(420 390)', 2154))," +
                "    (6,  ST_GeomFromText('POINT(410 420)', 2154))," +
                "    (7,  ST_GeomFromText('POINT(700 700)', 2154));");

        st.execute("DROP TABLE IF EXISTS CLUSTERS;" +
                        "CREATE TABLE CLUSTERS AS SELECT * FROM ST_ClusterDBSCAN('sample_points', 'the_geom', 'id', 50.0, 2)");

        ResultSet res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(1,2,3)");
        while (res.next()){
            assertEquals(1, res.getObject("CLUSTER_ID"));
            assertEquals(3, res.getObject("CLUSTER_SIZE"));
        }
        res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(4,5,6)");
        while (res.next()){
            assertEquals(2, res.getObject("CLUSTER_ID"));
            assertEquals(3, res.getObject("CLUSTER_SIZE"));
        }
        res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(7)");
        while (res.next()){
            assertNull(res.getObject("CLUSTER_ID"));
            assertNull(res.getObject("CLUSTER_SIZE"));
        }
        st.execute("DROP TABLE IF EXISTS sample_points, clusters");
    }

    @Test
    public void st_clusterDBSCAN2() {
        assertThrows(SQLException.class, () ->  st.execute("SELECT * FROM ST_ClusterDBSCAN('sample_points', 'the_geom', 'id', -0.05, 2))"));
    }

    @Test
    public void st_clusterDBSCAN3() {
        assertThrows(SQLException.class, () ->  st.execute("SELECT * FROM ST_ClusterDBSCAN('sample_points', 'the_geom', 'id', 0.05, 0))"));
    }

    @Test
    public void st_ClusterIntersecting1() throws SQLException {
        st.execute("DROP TABLE IF EXISTS sample_geoms;" +
                "CREATE TABLE sample_geoms (id INT ," +
                " the_geom  GEOMETRY(GEOMETRY, 2154));" +
                "INSERT INTO sample_geoms VALUES" +
                "    (1,  ST_GeomFromText('POLYGON ((151 255, 200 255, 200 200, 151 200, 151 255))', 2154))," +
                "    (2,  ST_GeomFromText('POLYGON((200 260, 250 260, 250 200, 200 200, 200 260))', 2154))," +
                "    (3,  ST_GeomFromText('POLYGON((125 407, 190 407, 190 350, 125 350, 125 407))', 2154))," +
                "    (4,  ST_GeomFromText('POLYGON((388 125, 457 125, 457 75, 388 75, 388 125))', 2154))," +
                "    (5,  ST_GeomFromText('POINT (160 390)', 2154))," +
                "    (6,  ST_GeomFromText('POINT (430 350)', 2154));");

        st.execute("DROP TABLE IF EXISTS CLUSTERS;" +
                "CREATE TABLE CLUSTERS AS SELECT * FROM ST_ClusterIntersecting('sample_geoms', 'the_geom', 'id')");

        ResultSet res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(1,2)");
        while (res.next()) {
            assertEquals(1, res.getObject("CLUSTER_ID"));
            assertEquals(2, res.getObject("CLUSTER_SIZE"));
        }
        res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(3,5)");
        while (res.next()) {
            assertEquals(2, res.getObject("CLUSTER_ID"));
            assertEquals(2, res.getObject("CLUSTER_SIZE"));
        }
        res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(4)");
        while (res.next()) {
            assertEquals(3, res.getObject("CLUSTER_ID"));
            assertEquals(1, res.getObject("CLUSTER_SIZE"));
        }
        res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(6)");
        while (res.next()) {
            assertEquals(4, res.getObject("CLUSTER_ID"));
            assertEquals(1, res.getObject("CLUSTER_SIZE"));
        }
        st.execute("DROP TABLE IF EXISTS sample_points, clusters");
    }

    @Disabled
    @Test
    public void st_ClusterTest() throws SQLException {
        st.execute("CALL GEOJSONREAD('/home/ebocher/Autres/data/geoclimate/uhi_lcz/Dijon/osm_47.23165_4.8946776_47.38684_5.170579/building.geojson', 'building', true);");

        st.execute("DROP TABLE IF EXISTS CLUSTERS;" +
                "CREATE TABLE CLUSTERS AS SELECT * FROM ST_ClusterIntersecting('building', 'the_geom', 'id_build')");

        st.execute("CALL fgbWRITE('/tmp/clusters_building.fgb','CLUSTERS',true)");
    }

    @Test
    public void st_ClusterWithin1() throws SQLException {
        st.execute("DROP TABLE IF EXISTS sample_geoms;" +
                "CREATE TABLE sample_geoms (id INT ," +
                " the_geom  GEOMETRY(GEOMETRY, 2154));" +
                "INSERT INTO sample_geoms VALUES" +
                "    (1,  ST_GeomFromText('POLYGON ((151 255, 200 255, 200 200, 151 200, 151 255))', 2154))," +
                "    (2,  ST_GeomFromText('POLYGON((200 260, 250 260, 250 200, 200 200, 200 260))', 2154))," +
                "    (3,  ST_GeomFromText('POLYGON((125 407, 190 407, 190 350, 125 350, 125 407))', 2154))," +
                "    (4,  ST_GeomFromText('POLYGON((388 125, 457 125, 457 75, 388 75, 388 125))', 2154))," +
                "    (5,  ST_GeomFromText('POINT (160 390)', 2154))," +
                "    (6,  ST_GeomFromText('POINT (430 350)', 2154));");

        st.execute("DROP TABLE IF EXISTS CLUSTERS;" +
                "CREATE TABLE CLUSTERS AS SELECT * FROM ST_ClusterWithin('sample_geoms', 'the_geom', 'id',10 )");

        ResultSet res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(1,2)");
        while (res.next()) {
            assertEquals(1, res.getObject("CLUSTER_ID"));
            assertEquals(2, res.getObject("CLUSTER_SIZE"));
        }
        res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(3,5)");
        while (res.next()) {
            assertEquals(2, res.getObject("CLUSTER_ID"));
            assertEquals(2, res.getObject("CLUSTER_SIZE"));
        }
        res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(4)");
        while (res.next()) {
            assertEquals(3, res.getObject("CLUSTER_ID"));
            assertEquals(1, res.getObject("CLUSTER_SIZE"));
        }
        res = st.executeQuery("SELECT * FROM CLUSTERS WHERE ID IN(6)");
        while (res.next()) {
            assertEquals(4, res.getObject("CLUSTER_ID"));
            assertEquals(1, res.getObject("CLUSTER_SIZE"));
        }
        st.execute("DROP TABLE IF EXISTS sample_points, clusters");
    }

    @Test
    public void st_ClusterWithin2() throws SQLException {
        st.execute("DROP TABLE IF EXISTS sample_geoms;" +
                "CREATE TABLE sample_geoms (id INT ," +
                " the_geom  GEOMETRY(GEOMETRY, 2154));" +
                "INSERT INTO sample_geoms VALUES" +
                "    (1,  ST_GeomFromText('POLYGON ((151 255, 200 255, 200 200, 151 200, 151 255))', 2154))," +
                "    (2,  ST_GeomFromText('POLYGON((200 260, 250 260, 250 200, 200 200, 200 260))', 2154))," +
                "    (3,  ST_GeomFromText('POLYGON((125 407, 190 407, 190 350, 125 350, 125 407))', 2154))," +
                "    (4,  ST_GeomFromText('POLYGON((388 125, 457 125, 457 75, 388 75, 388 125))', 2154))," +
                "    (5,  ST_GeomFromText('POINT (160 390)', 2154))," +
                "    (6,  ST_GeomFromText('POINT (430 350)', 2154));");

        st.execute("DROP TABLE IF EXISTS CLUSTERS;" +
                "CREATE TABLE CLUSTERS AS SELECT * FROM ST_ClusterWithin('sample_geoms', 'the_geom', 'id',1000 )");


        ResultSet res = st.executeQuery("SELECT * FROM CLUSTERS");
        while (res.next()) {
            assertEquals(1, res.getObject("CLUSTER_ID"));
            assertEquals(6, res.getObject("CLUSTER_SIZE"));
        }
        st.execute("DROP TABLE IF EXISTS sample_points, clusters");
    }
}
