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

import org.h2.value.ValueGeometry;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.BitSet;

/**
 * One column of a serialized result set.
 * <p>
 * Values are read with the getter matching the column type and written
 * directly to the column buffer, so no boxed value is kept in memory.
 * <p>
 * Serialized form (little endian):
 * <pre>
 * u32 name_length, utf-8 name
 * u32 type_code                         // TypeCodes, | NULL_MASK_FLAG if the column has NULLs
 * [u8 null_mask[(row_count + 7) / 8]]   // bit (i % 8) of byte (i / 8) set: row i is NULL
 * u32 values_length
 * values:
 *     INT int32 | LONG int64 | FLOAT float32 | DOUBLE float64 | BOOLEAN uint8
 *     other types: u32 length + bytes
 * </pre>
 * A NULL keeps a placeholder (0, false or an empty value) in the values, so
 * that fixed size columns stay aligned; only the mask tells it is NULL.
 *
 * @author Maël PHILIPPE, CNRS
 * @author Erwan BOCHER, CNRS
 */
public class ColumnWrapper {

    private final String name;
    private final int jdbcType;
    private final String typeName;
    private final int typeCode;
    private final ByteArrayOutputStream values = new ByteArrayOutputStream();
    private final ByteBuffer scratch = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
    private final BitSet nulls = new BitSet();
    private int rowCount = 0;

    /**
     * @param name     column label
     * @param jdbcType {@link Types} value of the column
     * @param typeName SQL type name of the column
     */
    public ColumnWrapper(String name, int jdbcType, String typeName) {
        this.name = name;
        this.jdbcType = jdbcType;
        this.typeName = typeName;
        this.typeCode = TypeCodes.of(jdbcType, typeName);
    }

    public String getName() {
        return name;
    }

    /** @return the buffer type code, without the null mask flag */
    public int getType() {
        return typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getRowCount() {
        return rowCount;
    }

    public boolean hasNulls() {
        return !nulls.isEmpty();
    }

    /**
     * Read the value of this column in the current row of {@code rs}.
     *
     * @param rs     result set positioned on a row
     * @param column 1-based index of this column
     * @throws SQLException if the value can't be read
     */
    public void read(ResultSet rs, int column) throws SQLException {
        int row = rowCount++;
        switch (typeCode) {
            case TypeCodes.INT: {
                int v = rs.getInt(column);
                putInt(rs.wasNull() ? markNull(row, 0) : v);
                break;
            }
            case TypeCodes.LONG: {
                long v = rs.getLong(column);
                if (rs.wasNull()) {
                    markNull(row, 0);
                    v = 0L;
                }
                putLong(v);
                break;
            }
            case TypeCodes.FLOAT: {
                float v = rs.getFloat(column);
                if (rs.wasNull()) {
                    markNull(row, 0);
                    v = 0f;
                }
                putInt(Float.floatToRawIntBits(v));
                break;
            }
            case TypeCodes.DOUBLE: {
                double v = rs.getDouble(column);
                if (rs.wasNull()) {
                    markNull(row, 0);
                    v = 0d;
                }
                putLong(Double.doubleToRawLongBits(v));
                break;
            }
            case TypeCodes.BOOLEAN: {
                boolean v = rs.getBoolean(column);
                if (rs.wasNull()) {
                    markNull(row, 0);
                    v = false;
                }
                values.write(v ? 1 : 0);
                break;
            }
            case TypeCodes.GEOMETRY: {
                Object geometry = rs.getObject(column);
                putBytes(row, geometry == null ? null : ValueGeometry.getFromGeometry(geometry).getBytes());
                break;
            }
            case TypeCodes.BINARY:
                putBytes(row, rs.getBytes(column));
                break;
            case TypeCodes.TEMPORAL:
                putString(row, temporal(rs, column));
                break;
            case TypeCodes.DECIMAL:
                putString(row, decimal(rs, column));
                break;
            default: // STRING, JSON, UUID, OTHER
                putString(row, rs.getString(column));
                break;
        }
    }

    private String temporal(ResultSet rs, int column) throws SQLException {
        switch (jdbcType) {
            case Types.DATE: {
                LocalDate v = rs.getObject(column, LocalDate.class);
                return v == null ? null : DateTimeFormatter.ISO_LOCAL_DATE.format(v);
            }
            case Types.TIME: {
                LocalTime v = rs.getObject(column, LocalTime.class);
                return v == null ? null : DateTimeFormatter.ISO_LOCAL_TIME.format(v);
            }
            case Types.TIME_WITH_TIMEZONE: {
                OffsetTime v = rs.getObject(column, OffsetTime.class);
                return v == null ? null : DateTimeFormatter.ISO_OFFSET_TIME.format(v);
            }
            case Types.TIMESTAMP_WITH_TIMEZONE: {
                OffsetDateTime v = rs.getObject(column, OffsetDateTime.class);
                return v == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(v);
            }
            default: {
                LocalDateTime v = rs.getObject(column, LocalDateTime.class);
                return v == null ? null : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(v);
            }
        }
    }

    private static String decimal(ResultSet rs, int column) throws SQLException {
        try {
            BigDecimal v = rs.getBigDecimal(column);
            return v == null ? null : v.toPlainString();
        } catch (SQLException e) {
            // DECFLOAT special values (NaN, Infinity) have no BigDecimal form
            return rs.getString(column);
        }
    }

    private int markNull(int row, int placeholder) {
        nulls.set(row);
        return placeholder;
    }

    private void putInt(int v) {
        scratch.clear();
        values.write(scratch.putInt(v).array(), 0, 4);
    }

    private void putLong(long v) {
        scratch.clear();
        values.write(scratch.putLong(v).array(), 0, 8);
    }

    private void putString(int row, String v) {
        putBytes(row, v == null ? null : v.getBytes(StandardCharsets.UTF_8));
    }

    private void putBytes(int row, byte[] v) {
        if (v == null) {
            nulls.set(row);
            putInt(0);
            return;
        }
        putInt(v.length);
        values.write(v, 0, v.length);
    }

    /**
     * @return the serialized column, see the class documentation
     */
    public byte[] serialize() {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] mask = null;
        if (hasNulls()) {
            mask = new byte[(rowCount + 7) / 8];
            byte[] bits = nulls.toByteArray(); // same bit order as the buffer format
            System.arraycopy(bits, 0, mask, 0, Math.min(bits.length, mask.length));
        }
        int size = 4 + nameBytes.length + 4 + (mask == null ? 0 : mask.length) + 4 + values.size();
        ByteBuffer out = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(nameBytes.length).put(nameBytes);
        out.putInt(mask == null ? typeCode : typeCode | TypeCodes.NULL_MASK_FLAG);
        if (mask != null) {
            out.put(mask);
        }
        out.putInt(values.size()).put(values.toByteArray());
        return out.array();
    }
}
