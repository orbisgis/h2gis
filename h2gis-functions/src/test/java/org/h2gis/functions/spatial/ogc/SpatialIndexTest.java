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

package org.h2gis.functions.spatial.ogc;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Nicolas Fortin
 */
public class SpatialIndexTest {
    private static Connection connection;
    private static final String DB_NAME = "SpatialIndexTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        // Set up test data
        OGCConformance1Test.executeScript(connection, "spatial_index_test_data.sql");
        
        reopen();
    }
    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    private static void assertEqualsRS(ResultSet rs,int index, Object... expected) throws SQLException {
        for(Object object : expected) {
            assertTrue(rs.next());
            assertEquals(object,rs.getObject(index));
        }
    }

    /**
     *  For this test, we will check to see that all of the feature tables are
     *  represented by entries in the GEOMETRY_COLUMNS table/view.
     *  @throws Exception
     */
    @Test
    public void T1() throws Exception {
        long deb = System.currentTimeMillis();
        intersectsPredicate();
        long end = System.currentTimeMillis() - deb;
        System.out.println("Done in "+end+" ms");
        deb = System.currentTimeMillis();
        Statement st = connection.createStatement();
        st.execute("create spatial index idx1 on DEP(the_geom)");
        intersectsPredicate();
        st.execute("drop index idx1");
        end = System.currentTimeMillis() - deb;
        System.out.println("With index Done in "+end+" ms");
    }

    private void intersectsPredicate() throws SQLException  {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("select b.id from DEP a,DEP b where a.id = 59 and " +
                "a.the_geom && b.the_geom AND ST_Intersects(a.the_geom,b.the_geom) and a.ID!=b.ID ORDER BY b.id ASC");
        assertEqualsRS(rs,1,45,49,61,62,63,66);
    }

    private static void reopen()  throws Exception   {
        // Close and reopen database
        connection.close();
        connection = H2GISDBFactory.openSpatialDataBase(DB_NAME);
    }

}
