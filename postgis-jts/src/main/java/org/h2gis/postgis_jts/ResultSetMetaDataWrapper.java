/*
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
package org.h2gis.postgis_jts;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Wrap metadata for geometry related columns.
 * @author Nicolas Fortin
 */
public class ResultSetMetaDataWrapper implements ResultSetMetaData {
    private ResultSetMetaData resultSetMetaData;

    public ResultSetMetaDataWrapper(ResultSetMetaData resultSetMetaData) {
        this.resultSetMetaData = resultSetMetaData;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return resultSetMetaData.getColumnCount();
    }

    @Override
    public boolean isAutoIncrement(int i) throws SQLException {
        return resultSetMetaData.isAutoIncrement(i);
    }

    @Override
    public boolean isCaseSensitive(int i) throws SQLException {
        return resultSetMetaData.isCaseSensitive(i);
    }

    @Override
    public boolean isSearchable(int i) throws SQLException {
        return resultSetMetaData.isSearchable(i);
    }

    @Override
    public boolean isCurrency(int i) throws SQLException {
        return resultSetMetaData.isCurrency(i);
    }

    @Override
    public int isNullable(int i) throws SQLException {
        return resultSetMetaData.isNullable(i);
    }

    @Override
    public boolean isSigned(int i) throws SQLException {
        return resultSetMetaData.isSigned(i);
    }

    @Override
    public int getColumnDisplaySize(int i) throws SQLException {
        return resultSetMetaData.getColumnDisplaySize(i);
    }

    @Override
    public String getColumnLabel(int i) throws SQLException {
        return resultSetMetaData.getColumnLabel(i);
    }

    @Override
    public String getColumnName(int i) throws SQLException {
        return resultSetMetaData.getColumnName(i);
    }

    @Override
    public String getSchemaName(int i) throws SQLException {
        return resultSetMetaData.getSchemaName(i);
    }

    @Override
    public int getPrecision(int i) throws SQLException {
        return resultSetMetaData.getPrecision(i);
    }

    @Override
    public int getScale(int i) throws SQLException {
        return resultSetMetaData.getScale(i);
    }

    @Override
    public String getTableName(int i) throws SQLException {
        return resultSetMetaData.getTableName(i);
    }

    @Override
    public String getCatalogName(int i) throws SQLException {
        return resultSetMetaData.getCatalogName(i);
    }

    @Override
    public int getColumnType(int i) throws SQLException {
        return resultSetMetaData.getColumnType(i);
    }

    @Override
    public String getColumnTypeName(int i) throws SQLException {
        String typeName = resultSetMetaData.getColumnTypeName(i);
        if(ResultSetWrapper.GEOMETRY_COLUMNS.contains(typeName)) {
            return "GEOMETRY";
        } else {
            return typeName;
        }
    }

    @Override
    public boolean isReadOnly(int i) throws SQLException {
        return resultSetMetaData.isReadOnly(i);
    }

    @Override
    public boolean isWritable(int i) throws SQLException {
        return resultSetMetaData.isWritable(i);
    }

    @Override
    public boolean isDefinitelyWritable(int i) throws SQLException {
        return resultSetMetaData.isDefinitelyWritable(i);
    }

    @Override
    public String getColumnClassName(int i) throws SQLException {
        return resultSetMetaData.getColumnClassName(i);
    }


    @Override
    public <T> T unwrap(Class<T> tClass) throws SQLException {
        if(tClass.isAssignableFrom(this.getClass())) {
            return tClass.cast(this);
        } else {
            return resultSetMetaData.unwrap(tClass);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return aClass.isAssignableFrom(this.getClass()) || resultSetMetaData.isWrapperFor(aClass);
    }
}
