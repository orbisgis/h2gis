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

package org.h2gis.osgi.test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import org.apache.felix.ipojo.junit4osgi.OSGiTestCase;
import org.orbisgis.sputilities.JDBCUrlParser;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * {@see http://felix.apache.org/site/apache-felix-ipojo-junit4osgi-tutorial.html}
 * Test PostGIS JDBC OSGi {@link DataSourceFactory}
 * @author Nicolas Fortin
 */
public class PostGISBundleTest extends OSGiTestCase {
    private DataSource dataSource;
    private ServiceReference<DataSourceFactory> ref;
    private ServiceRegistration dsService;
    private static final String POSTGRESQL_DRIVER_NAME = "postgresql";
    /**
     * Create data source
     */
    public void setUp() throws SQLException, InvalidSyntaxException {
        String pgJdbcUri = System.getProperty("pg_jdbc_uri");
        if(pgJdbcUri==null || pgJdbcUri.isEmpty()) {
            throw new SQLException("Run unit test with -Dpg_jdbc_uri=jdbc:postgresql://hostname:port/dbname&user=username&password=password");
        }
        // Find if DataSource service is already online
        Collection<ServiceReference<DataSourceFactory>> refs =  getContext().getServiceReferences(DataSourceFactory.class,null);
        for(ServiceReference<DataSourceFactory> driverRef : refs) {
            Object driverName = driverRef.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
            if(driverName instanceof String && POSTGRESQL_DRIVER_NAME.equals(driverName)) {
                // Found the right driver
                ref = driverRef;
                dataSource = ((DataSourceFactory)getServiceObject(ref)).createDataSource(JDBCUrlParser.parse(pgJdbcUri));
                dsService = getBundleContext().registerService(DataSource.class.getName(), dataSource, null);
                break;
            }
        }
        if(dataSource==null) {
            throw new SQLException("Could not find PostgreSQL driver");
        }
    }

    public void tearDown() {
        if(ref!=null) {
            getBundleContext().ungetService(ref);
        }
        if(dsService!=null) {
            dsService.unregister();
        }
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
            stat.execute("CREATE TABLE POINT2D (gid serial PRIMARY KEY, the_geom GEOMETRY)");
            try {
                PreparedStatement insert = connection.prepareStatement("INSERT INTO POINT2D (the_geom) VALUES (?)");
                GeometryFactory f = new GeometryFactory();
                insert.setObject(1, f.createPoint(new Coordinate(5, 8, 15)));
                insert.execute();
                // Read the value
                ResultSet rs = stat.executeQuery("select the_geom from POINT2D");
                assertTrue(rs.next());
                assertTrue(rs.getObject(1) instanceof Geometry);
                assertEquals(f.createPoint(new Coordinate(5, 8, 15)),rs.getObject(1));
                System.out.println("testCreateGeometryTable OK");
            } finally {
                stat.execute("DROP TABLE POINT2D");
            }
        } finally {
            connection.close();
        }
    }
}
