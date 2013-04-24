/**
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

package org.h2spatial.osgi.test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import org.apache.felix.ipojo.junit4osgi.OSGiTestCase;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * {@see http://felix.apache.org/site/apache-felix-ipojo-junit4osgi-tutorial.html}
 * @author Nicolas Fortin
 */
public class BundleTest extends OSGiTestCase {
    private static final String DB_FILE_PATH = "target/test-resources/dbH2";
    private static final String DATABASE_PATH = "jdbc:h2:"+DB_FILE_PATH;
    private DataSource dataSource;
    ServiceReference ref;

    /**
     * Create data source
     */
    public void setUp() throws SQLException {
        ref =  getContext().getServiceReference(DataSourceFactory.class.getName());
        Properties properties = new Properties();
        properties.put(DataSourceFactory.JDBC_URL,DATABASE_PATH);
        properties.put(DataSourceFactory.JDBC_USER,"sa");
        properties.put(DataSourceFactory.JDBC_PASSWORD,"");
        dataSource = ((DataSourceFactory)getServiceObject(ref)).createDataSource(properties);
        getBundleContext().registerService(DataSource.class.getName(),dataSource,null);
    }

    public void tearDown() {
        getContext().ungetService(ref);
    }
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Create and feed a spatial table, read a Geometry value
     * @throws Exception
     */
    public void testCreateGeometryTable() throws Exception  {
        Connection connection = getConnection();
        try {
            Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS POINT2D");
            stat.execute("CREATE TABLE POINT2D (gid int , the_geom GEOMETRY)");
            PreparedStatement insert = connection.prepareStatement("INSERT INTO POINT2D VALUES (?,?)");
            insert.setInt(1,0);
            GeometryFactory f = new GeometryFactory();
            WKBWriter wkbWriter = new WKBWriter();
            insert.setBytes(2, wkbWriter.write(f.createPoint(new Coordinate(5, 8, 15))));
            insert.execute();
        } finally {
            connection.close();
        }
        System.out.println("Table POINT2D created..");
    }

    /**
     * Test alias creation with local method through SQL request
     * @throws Exception
     */
    public void testCustomCreateAlias() throws Exception {
        Connection connection = getConnection();
        try {
            Statement stat = connection.createStatement();
            createAlias(stat, "StringCapitalize");
            stat.execute("DROP TABLE IF EXISTS TEST");
            stat.execute("CREATE TABLE TEST (fld VARCHAR)");
            stat.execute("INSERT INTO TEST VALUES ('didier')");
            ResultSet source = stat.executeQuery("select StringCapitalize(fld) as fld from test");
            assertTrue(source.next());
            assertEquals(source.getString("fld"), "DIDIER");
        } finally {
            connection.close();
        }
    }

    /**
     * Test alias creation with local method through SQL request
     * @throws Exception
     */
    public void testCustomCreateGeometryFunctionAlias() throws Exception {
        Connection connection = getConnection();
        try {
            Statement stat = connection.createStatement();
            createAlias(stat,"UT_AREA");
            stat.execute("DROP TABLE IF EXISTS TEST");
            stat.execute("CREATE TABLE TEST (the_geom GEOMETRY)");
            PreparedStatement pStat = connection.prepareStatement("INSERT INTO TEST VALUES (ST_GeomFromText(?, ?))");
            pStat.setString(1,"POLYGON((0 0,10 0,10 10,0 10,0 0))"); //    POLYGON(0 12, 0 14, 5 14, 5 12, 0 12)
            pStat.setInt(2,27582);
            pStat.execute();
            ResultSet source = stat.executeQuery("select UT_AREA(the_geom) as area from test");
            assertTrue(source.next());
            assertEquals(100.0,Double.valueOf(source.getString("area")),1e-8);
        } finally {
            connection.close();
        }
    }

    /**
     * Test read geometry as string
     * @throws Exception
     */
    public void testGeometryString() throws Exception {
        Connection connection = getConnection();
        try {
            String geoString = "POLYGON((0 0,10 0,10 10,0 10,0 0))";
            String wktGeo = (new WKTWriter().write(new WKTReader().read(geoString)));
            Statement stat = connection.createStatement();
            createAlias(stat,"UT_AREA");
            stat.execute("DROP TABLE IF EXISTS TEST");
            stat.execute("CREATE TABLE TEST (the_geom GEOMETRY)");
            PreparedStatement pStat = connection.prepareStatement("INSERT INTO TEST VALUES (ST_GeomFromText(?, ?))");
            pStat.setString(1, geoString);
            pStat.setInt(2, 27582);
            pStat.execute();
            ResultSet source = stat.executeQuery("select the_geom from test");
            assertTrue(source.next());
            assertEquals(wktGeo,source.getString("the_geom"));
        } finally {
            connection.close();
        }
    }

    private void createAlias(Statement stat, String functionName) throws SQLException {
        Bundle bundle = getBundleContext().getBundle();
        String bundleLocation = bundle.getSymbolicName()+":"+bundle.getVersion().toString();
        stat.execute("DROP ALIAS IF EXISTS "+functionName);
        stat.execute("CREATE ALIAS "+functionName+" FOR \""+bundleLocation+":"+BundleTest.class.getName()+"."+functionName+"\"");
    }

    public static double UT_AREA(Geometry geometry) {
        return geometry.getArea();
    }

    /**
     * used by {@link #testCustomCreateAlias}
     * @param inputString A string
     * @return Upper case version of parameter
     */
    public static String StringCapitalize(String inputString) {
        return inputString.toUpperCase();
    }


    public static Boolean IsValidGeometry(byte[] bytes) {
        WKBReader wkbReader = new WKBReader();
        try {
            Geometry geom = wkbReader.read(bytes);
            return geom.isValid();
        } catch (ParseException ex) {
            return false;
        }
    }


    /**
     * Test bytes of generated geometry
     * @throws Exception
     */
    public void testGeometryBytes() throws Exception {
        Connection connection = getConnection();
        try {
            Statement stat = connection.createStatement();
            createAlias(stat,"IsValidGeometry");
            stat.execute("DROP TABLE IF EXISTS TEST");
            stat.execute("CREATE TABLE TEST (the_geom GEOMETRY)");
            PreparedStatement pStat = connection.prepareStatement("INSERT INTO TEST VALUES (ST_GeomFromText(?, ?))");
            pStat.setString(1,"POLYGON((0 0,10 0,10 10,0 10,0 0))"); //    POLYGON(0 12, 0 14, 5 14, 5 12, 0 12)
            pStat.setInt(2,27572);
            pStat.execute();
            ResultSet source = stat.executeQuery("select IsValidGeometry(the_geom) as valid from test");
            assertTrue(source.next());
            assertTrue(source.getBoolean("valid"));
        } finally {
            connection.close();
        }
    }
}
