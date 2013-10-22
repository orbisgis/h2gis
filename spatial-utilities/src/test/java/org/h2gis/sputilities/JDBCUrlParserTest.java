package org.h2gis.sputilities;

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
