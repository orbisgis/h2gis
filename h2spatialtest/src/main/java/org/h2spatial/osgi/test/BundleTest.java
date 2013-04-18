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

import org.apache.felix.ipojo.junit4osgi.OSGiTestCase;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;

import java.sql.Connection;
import java.sql.Driver;
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
    private Connection getConnection(DataSourceFactory dataSourceFactory) throws SQLException{
        Driver driver = dataSourceFactory.createDriver(null);
        Properties properties = new Properties();
        properties.put("user","sa");
        properties.put("password","");
        return driver.connect(DATABASE_PATH,properties);
    }
    public void testH2SpatialService() throws Exception {
        ServiceReference[] refs =  getContext().getServiceReferences(DataSourceFactory.class.getName(),
                "(&(" + DataSourceFactory.OSGI_JDBC_DRIVER_NAME + "=H2Spatial))");
            assertNotNull(refs);
            assertEquals(refs.length,1); // h2spatial service
            ServiceReference ref = refs[0];
        try {
            Connection connection = getConnection((DataSourceFactory)getServiceObject(ref));
            Statement stat = connection.createStatement();
            stat.execute("DROP TABLE IF EXISTS POINT2D");
            stat.execute("CREATE TABLE POINT2D (gid int , the_geom GEOMETRY)");
        } finally {
            getContext().ungetService(ref);
        }
    }
}
