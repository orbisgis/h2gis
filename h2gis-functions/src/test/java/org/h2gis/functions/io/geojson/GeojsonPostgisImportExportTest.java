/**
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

package org.h2gis.functions.io.geojson;

import org.h2.util.StringUtils;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.postgis_jts_osgi.DataSourceFactoryImpl;
import org.h2gis.utilities.JDBCUtilities;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test with PostgreSQL-PostGIS for GitHub actions
 */
public class GeojsonPostgisImportExportTest {
    private static final WKTReader WKTREADER = new WKTReader();
    private static final Logger log = LoggerFactory.getLogger(GeojsonPostgisImportExportTest.class);

    @Test
    public void testPostgisImport() throws Exception {
        DataSourceFactoryImpl dataSourceFactory = new DataSourceFactoryImpl();
        Properties p = new Properties();
        p.setProperty("serverName", "localhost");
        p.setProperty("portNumber", "5432");
        p.setProperty("databaseName", "postgres");
        p.setProperty("user", "orbisgis");
        p.setProperty("password", "orbisgis");
        try (Connection connection = dataSourceFactory.createDataSource(p).getConnection()) {
            GeoJsonDriverFunction geoJsonDriverFunction = new GeoJsonDriverFunction();
            geoJsonDriverFunction.importFile(connection, "geojsontest", new File(GeojsonImportExportTest.class.getResource("data.geojson").getFile()), true, new EmptyProgressVisitor());
            try(ResultSet res = connection.createStatement().executeQuery("SELECT * FROM geojsontest;")) {
                assertTrue(res.next());
                assertTrue(((Geometry) res.getObject(1)).equals(WKTREADER.read("POLYGON ((7.49587624983838 48.5342070572556, 7.49575955525988 48.5342516702309, 7.49564286068138 48.5342070572556, 7.49564286068138 48.534117831187, 7.49575955525988 48.5340732180938, 7.49587624983838 48.534117831187, 7.49587624983838 48.5342070572556))")));
                assertEquals(-105576, res.getDouble(2), 0);
                assertEquals(275386, res.getDouble(3), 0);
                assertEquals(56.848998452816424, res.getDouble(4), 0);
                assertEquals(55.87291487481895, res.getDouble(5), 0);
                assertEquals(0.0, res.getDouble(6), 0);
                assertNull(res.getString(7));
                assertEquals(2, res.getDouble(8), 0);
                assertEquals("2017-01-19T18:29:26+01:00", res.getString(9));
                assertEquals("1484846966000", res.getBigDecimal(10).toString());
                assertEquals("2017-01-19T18:29:27+01:00", res.getString(11));
                assertEquals("1484846967000", res.getBigDecimal(12).toString());
                assertEquals("{\"member1\":1,\"member2\":{\"member21\":21,\"member22\":22}}", res.getString(13));
                assertEquals("[49,40.0,{\"member1\":1,\"member2\":{\"member21\":21,\"member22\":22}},\"string\",[13,\"string\",{\"member3\":3,\"member4\":4}]]", res.getString(14));
                assertEquals("[58,47,58,57,58,49,58,51,58,58,49,57,58,58,49,58,57,56,57,58,59,58,57,58,49,47,48,57,48,58,57,57,51,56,52,57,51,57,49,58,55,58,50,48,48,52,56,57,48,58,52,48,53,50,57,54,57,47,58,57,54,54,53,56,57,55,58,58,57,58,57,57]", res.getString(15));
                res.next();
            }
        } catch (PSQLException ex) {
            if (ex.getCause() == null || ex.getCause() instanceof ConnectException) {
                // Connection issue ignore
                log.warn("Connection error to local PostGIS, ignored", ex);
            } else {
                throw ex;
            }
        } catch (SQLException ex) {
            log.error(ex.getLocalizedMessage(), ex.getNextException());
            throw ex;
        }
    }
}
