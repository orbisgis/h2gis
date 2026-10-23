/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.graalvm;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Locale;

/**
 * Type codes of the binary result buffer shared with the C callers.
 * <p>
 * This is the single place where JDBC types are mapped to buffer types: the
 * serialization ({@link ColumnWrapper}), {@code h2gis_get_column_types} and
 * {@code h2gis_get_metadata_json} all use it.
 *
 * <pre>
 *  1 INT        int32        TINYINT, SMALLINT, INTEGER
 *  2 LONG       int64        BIGINT
 *  3 FLOAT      float32      REAL
 *  4 DOUBLE     float64      DOUBLE PRECISION, FLOAT
 *  5 BOOLEAN    uint8        BOOLEAN
 *  6 STRING     utf-8        CHARACTER [VARYING], CLOB, ENUM
 *  7 TEMPORAL   utf-8 ISO    DATE, TIME [WITH TIME ZONE], TIMESTAMP [WITH TIME ZONE]
 *  8 GEOMETRY   EWKB         GEOMETRY
 *  9 BINARY     bytes        BINARY [VARYING], BLOB
 * 10 DECIMAL    utf-8        NUMERIC, DECIMAL, DECFLOAT (exact, BigDecimal.toPlainString)
 * 11 JSON       utf-8        JSON
 * 12 UUID       utf-8        UUID
 * 99 OTHER      utf-8        anything else, as ResultSet.getString()
 * </pre>
 *
 * A type code with the {@link #NULL_MASK_FLAG} bit set is followed by a null
 * mask in the buffer, see {@link ColumnWrapper}.
 *
 * @author Erwan BOCHER, CNRS
 */
public final class TypeCodes {

    public static final int INT = 1;
    public static final int LONG = 2;
    public static final int FLOAT = 3;
    public static final int DOUBLE = 4;
    public static final int BOOLEAN = 5;
    public static final int STRING = 6;
    public static final int TEMPORAL = 7;
    public static final int GEOMETRY = 8;
    public static final int BINARY = 9;
    public static final int DECIMAL = 10;
    public static final int JSON = 11;
    public static final int UUID = 12;
    public static final int OTHER = 99;

    /** Set in the type code of a column followed by a null mask. */
    public static final int NULL_MASK_FLAG = 0x100;

    private TypeCodes() {
    }

    /**
     * @param jdbcType a {@link Types} value
     * @param typeName the SQL type name ({@link ResultSetMetaData#getColumnTypeName})
     * @return the buffer type code
     */
    public static int of(int jdbcType, String typeName) {
        String name = typeName == null ? "" : typeName.toUpperCase(Locale.ROOT);
        // H2 reports these types with generic JDBC types: check the name first
        if (name.startsWith("GEOMETRY")) {
            return GEOMETRY;
        }
        if (name.equals("UUID")) {
            return UUID;
        }
        if (name.equals("JSON")) {
            return JSON;
        }
        if (name.startsWith("ENUM")) {
            return STRING;
        }
        switch (jdbcType) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return INT;
            case Types.BIGINT:
                return LONG;
            case Types.REAL:
                return FLOAT;
            case Types.FLOAT:   // JDBC FLOAT is a double precision number
            case Types.DOUBLE:
                return DOUBLE;
            case Types.BOOLEAN:
            case Types.BIT:
                return BOOLEAN;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
                return STRING;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return TEMPORAL;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return BINARY;
            case Types.NUMERIC:
            case Types.DECIMAL:
                return DECIMAL;
            default:
                return OTHER;
        }
    }

    /**
     * @param meta result set metadata
     * @param column 1-based column index
     * @return the buffer type code of the column
     * @throws SQLException if the metadata can't be read
     */
    public static int of(ResultSetMetaData meta, int column) throws SQLException {
        return of(meta.getColumnType(column), meta.getColumnTypeName(column));
    }
}
