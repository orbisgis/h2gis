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

package org.h2gis.utilities.wrapper;

import org.locationtech.jts.geom.Geometry;
import org.h2gis.utilities.SpatialResultSet;
import org.h2gis.utilities.SpatialResultSetMetaData;

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
        if(field==null) {
            return (Geometry)field;
        }
        if(field instanceof Geometry) {
            return (Geometry)field;
        } else {
            throw new SQLException("The column "+getMetaData().getColumnName(columnIndex)+ " is not a Geometry");
        }
    }

    @Override
    public Geometry getGeometry(String columnLabel) throws SQLException {
        Object field =  getObject(columnLabel);
        if(field==null) {
            return (Geometry)field;
        }
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
        updateObject(columnIndex, geometry);
    }

    @Override
    public void updateGeometry(String columnLabel, Geometry geometry) throws SQLException {
        updateObject(columnLabel, geometry);
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
