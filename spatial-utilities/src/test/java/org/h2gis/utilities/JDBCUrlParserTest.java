/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import org.junit.Test;
import org.osgi.service.jdbc.DataSourceFactory;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Nicolas Fortin
 */
public class JDBCUrlParserTest {
    @Test
    public void testParsePostgreSQL() throws Exception {
        Properties properties = JDBCUrlParser.parse("jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true");
        assertEquals("localhost",properties.getProperty(DataSourceFactory.JDBC_SERVER_NAME));
        assertEquals("test",properties.getProperty(DataSourceFactory.JDBC_DATABASE_NAME));
        assertEquals("fred",properties.getProperty(DataSourceFactory.JDBC_USER));
        assertEquals("secret",properties.getProperty(DataSourceFactory.JDBC_PASSWORD));
        assertEquals("true",properties.getProperty("ssl"));
    }

    @Test
    public void testParseH2Embeded() throws Exception {
        Properties properties = JDBCUrlParser.parse("jdbc:h2:~/test");
        assertEquals("~/test",properties.getProperty(DataSourceFactory.JDBC_DATABASE_NAME));
    }
    @Test
    public void testParseH2EmbededAbsolute() throws Exception {
        Properties properties = JDBCUrlParser.parse("jdbc:h2:/home/fred/mydb");
        assertEquals("/home/fred/mydb",properties.getProperty(DataSourceFactory.JDBC_DATABASE_NAME));
    }

    @Test
    public void testParseH2EmbededWithOptions() throws Exception {
        Properties properties = JDBCUrlParser.parse("jdbc:h2:~/test;FILE_LOCK=FS");
        assertEquals("~/test",properties.getProperty(DataSourceFactory.JDBC_DATABASE_NAME));
        assertEquals("FS",properties.getProperty("FILE_LOCK"));
    }

    @Test
    public void testParsePostgreSQLWithPort() throws Exception {
        Properties properties = JDBCUrlParser.parse("jdbc:postgresql://localhost:8080/test");
        assertEquals("localhost",properties.getProperty(DataSourceFactory.JDBC_SERVER_NAME));
        assertEquals("test",properties.getProperty(DataSourceFactory.JDBC_DATABASE_NAME));
        assertEquals("8080",properties.getProperty(DataSourceFactory.JDBC_PORT_NUMBER));

    }
}
