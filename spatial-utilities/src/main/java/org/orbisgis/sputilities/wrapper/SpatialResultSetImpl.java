package org.orbisgis.sputilities.wrapper;

import com.vividsolutions.jts.geom.Geometry;
import org.orbisgis.sputilities.SpatialResultSet;
import org.orbisgis.sputilities.SpatialResultSetMetaData;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Nicolas Fortin
 */
public class SpatialResultSetImpl extends ResultSetWrapper implements SpatialResultSet {
    private int firstGeometryFieldIndex = -1;

    public SpatialResultSetImpl(ResultSet resultSet, StatementWrapper statement) {
        super(resultSet,statement);
    }

    private int getFirstGeometryFieldIndex() throws SQLException {
        if(firstGeometryFieldIndex==-1) {
            firstGeometryFieldIndex = getMetaData().unwrap(SpatialResultSetMetaData.class).getFirstGeometryFieldIndex();
        }
        return firstGeometryFieldIndex;
    }

    @Override
    public Geometry getGeometry(int columnIndex) throws SQLException {
        Object field =  getObject(columnIndex);
        if(field instanceof Geometry) {
            return (Geometry)field;
        } else {
            throw new SQLException("The column "+getMetaData().getColumnName(columnIndex)+ " is not a Geometry");
        }
    }

    @Override
    public Geometry getGeometry(String columnLabel) throws SQLException {
        Object field =  getObject(columnLabel);
        if(field instanceof Geometry) {
            return (Geometry)field;
        } else {
            throw new SQLException("The column "+columnLabel+ " is not a Geometry");
        }
    }

    @Override
    public Geometry getGeometry() throws SQLException {
        return getGeometry(getFirstGeometryFieldIndex());
    }

    @Override
    public void updateGeometry(int columnIndex, Geometry geometry) throws SQLException {

    }

    @Override
    public void updateGeometry(String columnLabel, Geometry geometry) throws SQLException {

    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if(iface.isInstance(this)) {
            try {
                return iface.cast(this);
            } catch (ClassCastException ex) {
                //Should never happen
                throw new SQLException(ex);
            }
        } else {
            return super.unwrap(iface);
        }
    }
}
