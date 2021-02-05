/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.junit.jupiter.api.*;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.h2gis.utilities.JDBCUtilities.TABLE_TYPE;
import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.DataSourceWrapper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Fortin
 */
public class JDBCUtilitiesTest {

    private static Connection connection;
    private static Statement st;

    @BeforeAll
    public static void init() throws Exception {
        String dataBaseLocation = new File("target/JDBCUtilitiesTest").getAbsolutePath();
        String databasePath = "jdbc:h2:" + dataBaseLocation;
        File dbFile = new File(dataBaseLocation + ".mv.db");
        Class.forName("org.h2.Driver");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        // Keep a connection alive to not close the DataBase on each unit test
        connection = DriverManager.getConnection(databasePath,
                "sa", "");
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @AfterAll
    public static void dispose() throws Exception {
        connection.close();
    }

    @Test
    public void testTemporaryTable() throws SQLException {        
        st.execute("DROP view IF EXISTS perstable_view cascade");
        st.execute("DROP TABLE IF EXISTS TEMPTABLE1,perstable");
        st.execute("CREATE TEMPORARY TABLE TEMPTABLE1");
        st.execute("CREATE TABLE perstable");
        assertTrue(JDBCUtilities.isTemporaryTable(connection, "temptable1"));
        assertFalse(JDBCUtilities.isTemporaryTable(connection, "PERSTable"));
    }

    @Test
    public void testTableType() throws SQLException {        
        st.execute("DROP view IF EXISTS perstable_view cascade");
        st.execute("DROP TABLE IF EXISTS TEMPTABLE1,perstable");
        st.execute("CREATE TEMPORARY TABLE TEMPTABLE1(id int)");
        st.execute("CREATE TABLE perstable(id int)");
        st.execute("CREATE VIEW perstable_view as select * from perstable; ");
        assertEquals(TABLE_TYPE.TEMPORARY, JDBCUtilities.getTableType(connection, TableLocation.parse("temptable1")));
        assertEquals(TABLE_TYPE.TABLE, JDBCUtilities.getTableType(connection, TableLocation.parse("perstable")));
    }

    @Test
    public void testRowCount() throws SQLException {
        st.execute("DROP SCHEMA IF EXISTS testschema");
        st.execute("CREATE SCHEMA testschema");
        st.execute("DROP TABLE IF EXISTS testschema.testRowCount");
        st.execute("CREATE TABLE testschema.testRowCount(id integer primary key, val double)");
        st.execute("INSERT INTO testschema.testRowCount VALUES (1, 0.2)");
        st.execute("INSERT INTO testschema.testRowCount VALUES (2, 0.2)");
        st.execute("INSERT INTO testschema.testRowCount VALUES (3, 0.5)");
        st.execute("INSERT INTO testschema.testRowCount VALUES (4, 0.6)");
        assertEquals(4, JDBCUtilities.getRowCount(connection, "TESTSCHEMA.TESTROWCOUNT"));
    }

    @Test
    public void testPrimaryKeyExtract() throws SQLException {
        st.execute("DROP SCHEMA IF EXISTS ATEMPSCHEMA");
        st.execute("CREATE SCHEMA ATEMPSCHEMA");
        st.execute("CREATE TABLE ATEMPSCHEMA.TEMPTABLE(id integer)");
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("CREATE TABLE TEMPTABLE(id integer primary key)");
        assertEquals(1, JDBCUtilities.getIntegerPrimaryKey(connection, TableLocation.parse("TEMPTABLE")));
        st.execute("DROP SCHEMA IF EXISTS SCHEM");
        st.execute("CREATE SCHEMA SCHEM");
        st.execute("DROP TABLE IF EXISTS SCHEM.TEMPTABLE");
        st.execute("CREATE TABLE SCHEM.TEMPTABLE(id integer primary key)");
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("CREATE TABLE TEMPTABLE(id varchar primary key)");
        assertEquals(0, JDBCUtilities.getIntegerPrimaryKey(connection, TableLocation.parse("TEMPTABLE")));
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
    }

    @Test
    public void testPrimaryKeyExtractOnNonexistantTable() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        assertThrows(SQLException.class, () -> {
            JDBCUtilities.getIntegerPrimaryKey(connection, TableLocation.parse("TEMPTABLE"));
        });
    }

    @Test
    public void testGetFieldNameFromIndex() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("CREATE TABLE TEMPTABLE(id integer, name varchar)");
        assertEquals("ID", JDBCUtilities.getColumnName(connection, TableLocation.parse("TEMPTABLE"), 1));
        assertEquals("NAME", JDBCUtilities.getColumnName(connection, TableLocation.parse("TEMPTABLE"), 2));
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
    }

    @Test
    public void testTableExists() throws SQLException {
        // Don't use quotes
        st.execute("DROP TABLE IF EXISTS temptable");
        st.execute("CREATE TABLE temptable(id integer, name varchar)");
        assertTrue(JDBCUtilities.tableExists(connection, TableLocation.parse("TEMPTABLE")));
        assertFalse(JDBCUtilities.tableExists(connection, TableLocation.parse("temptable")));
        assertFalse(JDBCUtilities.tableExists(connection, TableLocation.parse("teMpTAbLE")));
        assertFalse(JDBCUtilities.tableExists(connection, TableLocation.parse("\"teMpTAbLE\"")));
        st.execute("DROP TABLE IF EXISTS teMpTAbLE");
        st.execute("CREATE TABLE teMpTAbLE(id integer, name varchar)");
        assertTrue(JDBCUtilities.tableExists(connection, TableLocation.parse("TEMPTABLE")));
        assertFalse(JDBCUtilities.tableExists(connection, TableLocation.parse("temptable")));
        assertFalse(JDBCUtilities.tableExists(connection, TableLocation.parse("teMpTAbLE")));
        assertFalse(JDBCUtilities.tableExists(connection, TableLocation.parse("\"teMpTAbLE\"")));
        // Use quotes
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("DROP TABLE IF EXISTS \"teMpTAbLE\"");
        st.execute("CREATE TABLE \"teMpTAbLE\"(id integer, name varchar)");
        assertTrue(JDBCUtilities.tableExists(connection, TableLocation.parse("\"teMpTAbLE\"")));
        assertTrue(JDBCUtilities.tableExists(connection, TableLocation.parse("teMpTAbLE")));
        assertFalse(JDBCUtilities.tableExists(connection, TableLocation.parse("temptable")));
        assertFalse(JDBCUtilities.tableExists(connection, TableLocation.parse("TEMPTABLE")));
    }

    @Test
    public void isH2() throws SQLException {
        assertNotNull(DBUtils.getDBType(connection));
        assertNotNull(DBUtils.getDBType(new ConnectionWrapper(connection)));
    }

    @Test
    public void testGetColumnNamesAndIndexes() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("CREATE TABLE TEMPTABLE(id integer, name varchar)");
        List<Tuple<String, Integer>> fields = JDBCUtilities.getColumnNamesAndIndexes(connection, TableLocation.parse("TEMPTABLE"));
        assertEquals(2, fields.size());
        assertNotNull(fields.stream()
                .filter(tuple -> ("ID".equals(tuple.first()) || "NAME".equals(tuple.first())))
                .findAny()
                .orElse(null));
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
    }

    @Test
    public void testHasField() throws SQLException {
        st.execute("DROP TABLE IF EXISTS temptable");
        st.execute("CREATE TABLE temptable(id integer, name varchar)");
        assertTrue(JDBCUtilities.hasField(connection, "TEMPTABLE", "ID"));
        // The field name does not necessarily need to be capitalized.
        assertTrue(JDBCUtilities.hasField(connection, "TEMPTABLE", "id"));
        // The table name needs to be capitalized
        assertFalse(JDBCUtilities.hasField(connection, "temptable", "id"));
        assertFalse(JDBCUtilities.hasField(connection, "TEMPTABLE", "some_other_field"));
    }

    @Test
    public void testCancel() throws SQLException {
        boolean aborted = false;
        ProgressVisitor progressVisitor = new EmptyProgressVisitor();
        Statement statement = connection.createStatement();
        PropertyChangeListener listener = JDBCUtilities.attachCancelResultSet(statement, progressVisitor);
        statement.execute("CREATE ALIAS SLEEP FOR \"java.lang.Thread.sleep\"");
        CancelThread cancelThread = new CancelThread(progressVisitor);
        cancelThread.start();
        try {
            statement.execute("SELECT SLEEP(10) FROM SYSTEM_RANGE(1, 200)");
        } catch (SQLException ex) {
            aborted = true;
        } finally {
            progressVisitor.removePropertyChangeListener(listener);
        }
        statement.close();
        assertTrue(aborted);
    }

    private static class CancelThread extends Thread {

        private ProgressVisitor pm;

        private CancelThread(ProgressVisitor pm) {
            this.pm = pm;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // Ignore
            }
            pm.cancel();
        }
    }

    @Test
    public void testIsLinkedTable() throws ClassNotFoundException, SQLException {
        String dataBaseLocation = new File("target/JDBCUtilitiesTest2").getAbsolutePath();
        String databasePath = "jdbc:h2:" + dataBaseLocation;
        File dbFile = new File(dataBaseLocation + ".mv.db");
        Class.forName("org.h2.Driver");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        // Keep a connection alive to not close the DataBase on each unit test
        Connection con = DriverManager.getConnection(databasePath, "sa", "");

        Statement st = con.createStatement();
        st.execute("DROP TABLE IF EXISTS TEMPTABLE");
        st.execute("CREATE TABLE TEMPTABLE(id integer, name varchar)");

        Statement statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS LINKEDTABLE");
        statement.execute("CREATE LINKED TABLE LINKEDTABLE('org.h2.Driver', '" + databasePath + "', 'sa', '', 'TEMPTABLE');");
        assertTrue(JDBCUtilities.isLinkedTable(connection, "LINKEDTABLE"));
        assertEquals(TABLE_TYPE.TABLE_LINK, JDBCUtilities.getTableType(connection, TableLocation.parse("LINKEDTABLE")));
    }

    @Test
    public void testCreateEmptyTable() throws SQLException {
        st.execute("drop table if exists emptytable");
        JDBCUtilities.createEmptyTable(connection, "emptytable");
        ResultSet res = st.executeQuery("SELECT * FROM emptytable;");
        ResultSetMetaData rsmd = res.getMetaData();
        assertTrue(rsmd.getColumnCount() == 0);
        assertTrue(!res.next());
        st.execute("DROP TABLE emptytable");
    }

    @Test
    public void testColumnNames() throws SQLException {
        st.execute("DROP TABLE IF EXISTS temptable");
        st.execute("CREATE TABLE temptable(id integer, name varchar)");
        ResultSet rs = st.executeQuery("SELECT * from temptable");
        ResultSetMetaData md = rs.getMetaData();
        ArrayList<String> expecteds = new ArrayList<>();
        expecteds.add("ID");
        expecteds.add("NAME");
        assertEquals(expecteds, JDBCUtilities.getColumnNames(md));
    }

    @Test
    public void testGetObjectClass() throws SQLException {
        st.execute("drop table if exists mytable; create table mytable(temperature double precision);"
                + "insert into mytable values(12.1258952354)");
        ResultSet res = st.executeQuery("SELECT * FROM mytable;");
        res.next();
        assertEquals((float) 12.1258952354d, res.getObject(1, Float.class));
        assertEquals(12, res.getObject(1, Integer.class));
        assertEquals((float) 12.1258952354d, res.getObject("temperature", Float.class));
        assertEquals(12, res.getObject("temperature", Integer.class));
        st.execute("DROP TABLE mytable");
    }

    // wrapSpatialDataSource(DataSource dataSource)
    @Test
    public void testWrapSpatialDataSource() {
        assertTrue(JDBCUtilities.wrapSpatialDataSource(new CustomDataSource()) instanceof CustomDataSource);
        assertTrue(JDBCUtilities.wrapSpatialDataSource(new CustomDataSource1()) instanceof DataSourceWrapper);
        assertTrue(JDBCUtilities.wrapSpatialDataSource(new CustomDataSource2()) instanceof DataSourceWrapper);
    }

    // wrapConnection(Connection connection)
    @Test
    public void testWrapConnection() {
        assertTrue(JDBCUtilities.wrapConnection(connection) instanceof ConnectionWrapper);
        assertTrue(JDBCUtilities.wrapConnection(new CustomConnection1(connection)) instanceof ConnectionWrapper);
        assertTrue(JDBCUtilities.wrapConnection(new CustomConnection(connection)) instanceof ConnectionWrapper);
    }

    @Test
    public void isIndexedTest() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEST_INDEX");
        st.execute("CREATE TABLE TEST_INDEX(no_idx GEOMETRY, idx GEOMETRY, spatial_idx GEOMETRY)");
        st.execute("CREATE INDEX ON TEST_INDEX(idx)");
        st.execute("CREATE SPATIAL INDEX ON TEST_INDEX (spatial_idx)");
        String tableName = "test_index";
        TableLocation table = TableLocation.parse(tableName, DBUtils.getDBType(connection));
        assertFalse(JDBCUtilities.isIndexed(connection, tableName, "no_idx"));
        assertFalse(JDBCUtilities.isIndexed(connection, table, "no_idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, tableName, "idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, table, "idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, tableName, "spatial_idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, table, "spatial_idx"));
    }

    @Test
    public void isSpatialIndexedTest() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEST_INDEX");
        st.execute("CREATE TABLE TEST_INDEX(no_idx GEOMETRY, idx GEOMETRY, spatial_idx GEOMETRY)");
        st.execute("CREATE INDEX ON TEST_INDEX(idx)");
        st.execute("CREATE SPATIAL INDEX ON TEST_INDEX (spatial_idx)");

        String tableName = "test_index";
        TableLocation table = TableLocation.parse(tableName, DBUtils.getDBType(connection));

        assertFalse(JDBCUtilities.isSpatialIndexed(connection, tableName, "no_idx"));
        assertFalse(JDBCUtilities.isSpatialIndexed(connection, table, "no_idx"));
        assertFalse(JDBCUtilities.isSpatialIndexed(connection, tableName, "idx"));
        assertFalse(JDBCUtilities.isSpatialIndexed(connection, table, "idx"));
        assertTrue(JDBCUtilities.isSpatialIndexed(connection, tableName, "spatial_idx"));
        assertTrue(JDBCUtilities.isSpatialIndexed(connection, table, "spatial_idx"));
    }

    @Test
    public void createIndexTest() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEST_INDEX");
        st.execute("CREATE TABLE TEST_INDEX(no_idx GEOMETRY, idx GEOMETRY, spatial_idx GEOMETRY)");

        String tableName = "test_index";
        TableLocation table = TableLocation.parse(tableName, DBUtils.getDBType(connection));

        assertTrue(JDBCUtilities.createIndex(connection, table, "idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, table, "idx"));
        assertFalse(JDBCUtilities.isSpatialIndexed(connection, table, "idx"));
        assertTrue(JDBCUtilities.createIndex(connection, tableName, "spatial_idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, tableName, "spatial_idx"));
        assertFalse(JDBCUtilities.isSpatialIndexed(connection, tableName, "spatial_idx"));
    }

    @Test
    public void createSpatialIndexTest() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEST_INDEX");
        st.execute("CREATE TABLE TEST_INDEX(no_idx GEOMETRY, idx GEOMETRY, spatial_idx GEOMETRY)");

        String tableName = "test_index";
        TableLocation table = TableLocation.parse(tableName, DBUtils.getDBType(connection));

        assertTrue(JDBCUtilities.createSpatialIndex(connection, table, "idx"));
        assertTrue(JDBCUtilities.isSpatialIndexed(connection, table, "idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, table, "idx"));
        assertTrue(JDBCUtilities.createSpatialIndex(connection, tableName, "spatial_idx"));
        assertTrue(JDBCUtilities.isSpatialIndexed(connection, tableName, "spatial_idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, tableName, "spatial_idx"));
    }

    @Test
    public void isDropIndexesTest() throws SQLException {
        st.execute("DROP TABLE IF EXISTS TEST_INDEX");
        st.execute("CREATE TABLE TEST_INDEX(no_idx GEOMETRY, idx GEOMETRY, spatial_idx GEOMETRY)");
        st.execute("CREATE INDEX ON TEST_INDEX(idx)");
        st.execute("CREATE SPATIAL INDEX ON TEST_INDEX (spatial_idx)");

        String tableName = "test_index";
        TableLocation table = TableLocation.parse(tableName, DBUtils.getDBType(connection));

        assertTrue(JDBCUtilities.isIndexed(connection, table, "idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, table, "spatial_idx"));

        JDBCUtilities.dropIndex(connection, tableName);

        assertFalse(JDBCUtilities.isIndexed(connection, table, "idx"));
        assertFalse(JDBCUtilities.isIndexed(connection, table, "spatial_idx"));



        st.execute("CREATE INDEX ON TEST_INDEX(idx)");
        st.execute("CREATE SPATIAL INDEX ON TEST_INDEX (spatial_idx)");

        assertTrue(JDBCUtilities.isIndexed(connection, table, "idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, table, "spatial_idx"));

        JDBCUtilities.dropIndex(connection, table);

        assertFalse(JDBCUtilities.isIndexed(connection, table, "idx"));
        assertFalse(JDBCUtilities.isIndexed(connection, table, "spatial_idx"));



        st.execute("CREATE INDEX ON TEST_INDEX(idx)");
        st.execute("CREATE SPATIAL INDEX ON TEST_INDEX (spatial_idx)");

        assertTrue(JDBCUtilities.isIndexed(connection, table, "idx"));
        assertTrue(JDBCUtilities.isIndexed(connection, table, "spatial_idx"));

        JDBCUtilities.dropIndex(connection, table, "idx");
        assertFalse(JDBCUtilities.isIndexed(connection, table, "idx"));

        JDBCUtilities.dropIndex(connection, table, "spatial_idx");
        assertFalse(JDBCUtilities.isIndexed(connection, table, "spatial_idx"));
    }

    private class CustomDataSource implements DataSource {

        @Override
        public Connection getConnection() throws SQLException {
            return null;
        }

        @Override
        public Connection getConnection(String s, String s1) throws SQLException {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> aClass) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> aClass) throws SQLException {
            return true;
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter printWriter) throws SQLException {
        }

        @Override
        public void setLoginTimeout(int i) throws SQLException {
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }
    }

    private class CustomDataSource1 extends CustomDataSource {

        @Override
        public boolean isWrapperFor(Class<?> aClass) throws SQLException {
            throw new SQLException();
        }
    }

    private class CustomDataSource2 extends CustomDataSource {

        @Override
        public boolean isWrapperFor(Class<?> aClass) throws SQLException {
            return false;
        }
    }

    private class CustomConnection1 extends ConnectionWrapper {

        public CustomConnection1(Connection connection) {
            super(connection);
        }

        @Override
        public boolean isWrapperFor(Class<?> var1) throws SQLException {
            throw new SQLException();
        }
    }

    private class CustomConnection extends ConnectionWrapper {

        public CustomConnection(Connection connection) {
            super(connection);
        }

        @Override
        public boolean isWrapperFor(Class<?> var1) throws SQLException {
            return true;
        }
    }

}
