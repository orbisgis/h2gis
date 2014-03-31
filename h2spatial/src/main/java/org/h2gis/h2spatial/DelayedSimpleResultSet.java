package org.h2gis.h2spatial;

import org.h2.tools.SimpleResultSet;

import java.sql.SQLException;

/**
 * A {@link org.h2.tools.SimpleResultSet} where the initialization can be delayed.
 *
 * See https://groups.google.com/forum/#!topic/h2-database/NHH0rDeU258
 * @author Adam Gouge
 */
public abstract class DelayedSimpleResultSet extends SimpleResultSet {

    private boolean isInitialized = false;

    @Override
    public boolean next() throws SQLException {
        if (!isInitialized) {
            init();
            isInitialized = true;
        }
        return super.next();
    }

    /**
     * Initialize the ResultSet.
     */
    protected abstract void init() throws SQLException;

    /**
     * Return true if the init method has been called.
     * @return true if the init method has been called
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}
