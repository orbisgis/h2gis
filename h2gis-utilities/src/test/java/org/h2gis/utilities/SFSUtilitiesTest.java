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

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
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
