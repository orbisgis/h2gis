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
package org.h2gis.utilities.wrapper;

import org.h2gis.utilities.SpatialResultSetMetaData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Wrap ResultSetMetaData, in order to be converted to SpatialResultSetMetaData if the result set is spatial.
 * @author Nicolas Fortin
 */
public class ResultSetMetaDataWrapper implements ResultSetMetaData {
    private ResultSetMetaData resultSetMetaData;
    protected StatementWrapper statement;

    public ResultSetMetaDataWrapper(ResultSetMetaData resultSetMetaData, StatementWrapper statement) {
        this.statement = statement;
        this.resultSetMetaData = resultSetMetaData;
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        return resultSetMetaData.getCatalogName(column);
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return resultSetMetaData.getColumnClassName(column);
    }

    @Override
    public int getColumnCount() throws SQLException {
        return resultSetMetaData.getColumnCount();
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return resultSetMetaData.getColumnDisplaySize(column);
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return resultSetMetaData.getColumnLabel(column);
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return resultSetMetaData.getColumnName(column);
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return resultSetMetaData.getColumnType(column);
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return resultSetMetaData.getColumnTypeName(column);
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        return resultSetMetaData.getPrecision(column);
    }

    @Override
    public int getScale(int column) throws SQLException {
        return resultSetMetaData.getScale(column);
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        return resultSetMetaData.getSchemaName(column);
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return resultSetMetaData.getTableName(column);
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return resultSetMetaData.isAutoIncrement(column);
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return resultSetMetaData.isCaseSensitive(column);
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return resultSetMetaData.isCurrency(column);
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return resultSetMetaData.isDefinitelyWritable(column);
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return resultSetMetaData.isNullable(column);
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return resultSetMetaData.isReadOnly(column);
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return resultSetMetaData.isSearchable(column);
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return resultSetMetaData.isSigned(column);
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return resultSetMetaData.isWritable(column);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(SpatialResultSetMetaData.class) || resultSetMetaData.isWrapperFor(iface);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if(iface.isAssignableFrom(SpatialResultSetMetaDataImpl.class)) {
            try {
                return iface.cast(new SpatialResultSetMetaDataImpl(resultSetMetaData,statement));
            } catch (ClassCastException ex) {
                // Should never throw this as it is checked before.
                throw new SQLException("Cannot cast "+SpatialResultSetMetaDataImpl.class.getName()+" into "+iface.getName(),ex);
            }
        } else {
            return resultSetMetaData.unwrap(iface);
        }
    }
}
