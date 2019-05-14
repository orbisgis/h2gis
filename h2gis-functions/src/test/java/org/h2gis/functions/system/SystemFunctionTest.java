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

package org.h2gis.functions.system;


import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Erwan Bocher
 */
public class SystemFunctionTest {

    private static Connection connection;
    private Statement st;
    private static final String DB_NAME = "SystemFunctionTest";

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
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
        assertArrayEquals(expecteds, actuals, 10 - 5);
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
        assertArrayEquals(expecteds, actuals, 10 - 5);
        rs.close();
    }
    
    @Test
    public void test_DoubleRange3() throws Throwable {
        assertThrows(JdbcSQLNonTransientException.class, ()-> {
            try {
                st.execute("SELECT DoubleRange(10, 1, 0.5);");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
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
        assertArrayEquals(expecteds, actuals);
        rs.close();
    }

    @Test
    public void test_IntegerRange2() throws Throwable {
        assertThrows(JdbcSQLNonTransientException.class, ()-> {
            try {
                st.execute("SELECT IntegerRange(10, 1, 0.5);");
            } catch (JdbcSQLException e) {
                throw e.getCause();
            }
        });
    }
    
    @Test
    public void test_H2GISVersion() throws Exception {
        ResultSet rs = st.executeQuery("SELECT H2GISVersion();");
        rs.next();
        assertNotEquals("unknown", rs.getString(1));
        rs.close();
    }

}
