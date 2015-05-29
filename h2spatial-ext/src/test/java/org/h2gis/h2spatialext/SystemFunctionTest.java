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
package org.h2gis.h2spatialext;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.h2.jdbc.JdbcSQLException;
import org.h2gis.h2spatial.ut.SpatialH2UT;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erwan Bocher
 */
public class SystemFunctionTest {

    private static Connection connection;
    private Statement st;
    private static final String DB_NAME = "SystemFunctionTest";

    @BeforeClass
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = SpatialH2UT.createSpatialDataBase(DB_NAME, false);
        CreateSpatialExtension.initSpatialExtension(connection);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    @Before
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @After
    public void tearDownStatement() throws Exception {
        st.close();
    }
    
    @Test
    public void test_DoubleRange1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT DoubleRange(0, 5, 1);");
        rs.next();
        Array data = rs.getArray(1);
        Object[] valueArray = (Object[]) data.getArray();
        double[] actuals = new double[valueArray.length];
        for (int i = 0; i < valueArray.length; i++) {
            actuals[i] = (Double) valueArray[i];
        }
        double[] expecteds = new double[]{0, 1, 2, 3, 4};
        Assert.assertArrayEquals(expecteds, actuals, 10 - 5);
        rs.close();
    }
    
    @Test
    public void test_DoubleRange2() throws Exception {
        ResultSet rs = st.executeQuery("SELECT DoubleRange(0, 1, 0.5);");
        rs.next();
        Array data = rs.getArray(1);
        Object[] valueArray = (Object[]) data.getArray();
        double[] actuals = new double[valueArray.length];
        for (int i = 0; i < valueArray.length; i++) {
            actuals[i] = (Double) valueArray[i];
        }
        double[] expecteds = new double[]{0, 0.5};
        Assert.assertArrayEquals(expecteds, actuals, 10 - 5);
        rs.close();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void test_DoubleRange3() throws Throwable {
        try {
            st.execute("SELECT DoubleRange(10, 1, 0.5);");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }
    
    @Test
    public void test_IntegerRange1() throws Exception {
        ResultSet rs = st.executeQuery("SELECT IntegerRange(0, 5, 1);");
        rs.next();
        Array data = rs.getArray(1);
        Object[] valueArray = (Object[]) data.getArray();
        int[] actuals = new int[valueArray.length];
        for (int i = 0; i < valueArray.length; i++) {
            actuals[i] = (Integer) valueArray[i];
        }
        int[] expecteds = new int[]{0, 1, 2, 3, 4};
        Assert.assertArrayEquals(expecteds, actuals);
        rs.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_IntegerRange2() throws Throwable {
        try {
            st.execute("SELECT IntegerRange(10, 1, 0.5);");
        } catch (JdbcSQLException e) {
            throw e.getOriginalCause();
        }
    }

}
