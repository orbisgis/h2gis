package org.h2gis.h2spatial;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Adam Gouge
 */
public class DelayedSimpleResultSetTest {

    @Test
    public void test() throws SQLException {
        DelayedSimpleResultSet rs = new DelayedSimpleResultSet() {
            @Override
            protected void init() throws SQLException {
            }
        };
        assertFalse(rs.isInitialized());
        assertFalse(rs.next());
        assertTrue(rs.isInitialized());
    }
}
