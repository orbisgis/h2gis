/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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

package org.h2gis.h2spatial;

import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
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
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
        // Set up test data
        URL sqlURL = OGCConformance1Test.class.getResource("spatial_index_test_data.sql");
        Statement st = connection.createStatement();
        st.execute("RUNSCRIPT FROM '"+sqlURL+"'");
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
        connection = SpatialH2UT.openSpatialDataBase(DB_NAME);
    }

}
