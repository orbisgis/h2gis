package org.orbisgis.sputilities.wrapper;

import com.vividsolutions.jts.geom.Geometry;
import org.orbisgis.sputilities.SpatialResultSet;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Nicolas Fortin
 */
public class SpatialResultSetImpl extends ResultSetWrapper implements SpatialResultSet {

    public SpatialResultSetImpl(ResultSet resultSet, StatementWrapper statement) {
        super(resultSet,statement);
    }

    @Override
    public Geometry getGeometry(int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Geometry getGeometry(String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void updateGeometry(int columnIndex, Geometry geometry) throws SQLException {

    }

    @Override
    public void updateGeometry(String columnLabel, Geometry geometry) throws SQLException {

    }
}
