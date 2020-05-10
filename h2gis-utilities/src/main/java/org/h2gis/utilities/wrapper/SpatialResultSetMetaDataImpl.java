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

import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.SpatialResultSetMetaData;
import org.h2gis.utilities.TableLocation;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.h2gis.utilities.GeometryTableUtilities;

/**
 * @author Nicolas Fortin
 */
public class SpatialResultSetMetaDataImpl extends ResultSetMetaDataWrapper implements SpatialResultSetMetaData {
    private int firstGeometryFieldIndex = -1;

    /**
     * Constructor
     * @param resultSetMetaData meta data
     * @param statement Active statement
     */
    public SpatialResultSetMetaDataImpl(ResultSetMetaData resultSetMetaData, StatementWrapper statement) {
        super(resultSetMetaData,statement);
    }

    @Override
    public int getFirstGeometryFieldIndex() throws SQLException {
        if(firstGeometryFieldIndex==-1) {
            for(int idColumn=1;idColumn<=getColumnCount();idColumn++) {
                if(getColumnTypeName(idColumn).equalsIgnoreCase("geometry")) {
                    firstGeometryFieldIndex = idColumn;
                    break;
                }
            }
        }
        return firstGeometryFieldIndex;
    }
    @Override
    public int getGeometryType() throws SQLException {
        return getGeometryType(getFirstGeometryFieldIndex());
    }

    @Override
    public int getGeometryType(int column) throws SQLException {
        return GeometryTableUtilities.getMetaData(statement.getConnection(),
                new TableLocation(getCatalogName(column), getSchemaName(column), getTableName(column)),
                getColumnName(column)).geometryTypeCode;
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
