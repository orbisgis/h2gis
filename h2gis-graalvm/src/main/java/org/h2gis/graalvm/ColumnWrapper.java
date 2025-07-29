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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single column of a JDBC {@link java.sql.ResultSet}, holding the name,
 * SQL type, and all values for that column. Provides serialization into a native-friendly
 * binary format using little-endian encoding, suitable for communication with native code
 * (e.g., via GraalVM).
 *
 * @author Maël PHILIPPE, CNRS
 * @author Erwan BOCHER, cnrs
 */
public class ColumnWrapper {
    private final String name;
    private final int typeCode;

    private final String typeName;

    private final List<Object> values;

    /**
     * Constructs a {@code ColumnWrapper} with the given name and SQL type.
     *
     * @param name     the name of the column
     * @param typeCode the SQL type (as defined in {@link java.sql.Types})
     * @param typeName the name of the SQL type
     */
    public ColumnWrapper(String name, int typeCode, String typeName) {
        this.name = name;
        this.typeName = typeName.toLowerCase();
        this.values = new ArrayList<>();

        switch (typeCode) {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                this.typeCode = 1; // INT
                break;
            case Types.BIGINT:
                this.typeCode = 2; // LONG
                break;
            case Types.FLOAT:
            case Types.REAL:
                this.typeCode = 3; // FLOAT (32-bit)
                break;
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                this.typeCode = 4; // DOUBLE (64-bit)
                break;
            case Types.BOOLEAN:
            case Types.BIT:
                this.typeCode = 5; // BOOLEAN
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                this.typeCode = 6; // STRING
                break;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                this.typeCode = 7; // DATE as string
                break;
            case Types.OTHER:
            case Types.STRUCT:
            default:
                if (typeName.startsWith("geometry")) {
                    this.typeCode = 8; // GEOMETRY (WKB)
                } else {
                    this.typeCode = 99; // OTHER (as string)
                }
                break;
        }


    }

    /**
     * Adds a value to the column (typically one row).
     *
     * @param value the value to add; may be {@code null}
     */
    public void addValue(Object value) {
        values.add(value);
    }

    /**
     * Sets a value at the specified index in the column.
     * If the index is out of bounds, fills missing values with {@code null}.
     *
     * @param index the row index
     * @param value the value to set
     */
    public void setValueAt(int index, Object value) {
        while (index >= values.size()) values.add(null);
        values.set(index, value);
    }

    /**
     * Returns the column name.
     *
     * @return column name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the column.
     *
     * @return SQL type
     */
    public int getType() {
        return this.typeCode;
    }

    /**
     * Returns the SQL type name of the column.
     *
     * @return SQL type (see {@link java.sql.Types})
     */
    public String getTypeName() {
        return this.typeName;
    }


    /**
     * Returns the list of values contained in this column.
     *
     * @return list of values
     */
    public List<Object> getValues() {
        return values;
    }

    /**
     * Serializes the column into a binary format with the following layout:
     * <pre>
     * [int32 nameLength] [UTF-8 name bytes]
     * [int32 typeCode]
     * For each value:
     *     - INTEGER / SMALLINT / TINYINT → int32
     *     - BIGINT                       → int64
     *     - FLOAT / REAL                 → float32
     *     - DOUBLE / DECIMAL / NUMERIC  → float64
     *     - BOOLEAN / BIT               → byte (1 if true, 0 if false)
     *     - Other (VARCHAR, DATE, etc.) → [int32 length][UTF-8 bytes]
     * </pre>
     * This format is little-endian and is optimized for native deserialization.
     *
     * @return a byte array representing the serialized column
     * @throws Exception if serialization fails
     */
    public byte[] serialize() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteBuffer bb4 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer bb8 = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);

        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        out.write(bb4.putInt(nameBytes.length).array());
        bb4.clear();
        out.write(nameBytes);
        out.write(bb4.putInt(this.typeCode).array());
        bb4.clear();

        ByteArrayOutputStream data = new ByteArrayOutputStream();

        for (Object val : values) {
            switch (this.typeCode) {
                case 1:
                    data.write(bb4.putInt(val == null ? 0 : ((Number) val).intValue()).array());
                    bb4.clear();
                    break;
                case 2:
                    data.write(bb8.putLong(val == null ? 0L : ((Number) val).longValue()).array());
                    bb8.clear();
                    break;
                case 3:
                    data.write(bb4.putFloat(val == null ? 0f : ((Number) val).floatValue()).array());
                    bb4.clear();
                    break;
                case 4:
                    data.write(bb8.putDouble(val == null ? 0.0 : ((Number) val).doubleValue()).array());
                    bb8.clear();
                    break;
                case 5:
                    data.write(val != null && ((Boolean) val) ? 1 : 0);
                    break;
                case 6:
                case 7:
                case 99:
                    byte[] str = (val == null) ? new byte[0]
                            : val.toString().getBytes(StandardCharsets.UTF_8);
                    data.write(bb4.putInt(str.length).array());
                    bb4.clear();
                    data.write(str);
                    break;
                case 8:
                    byte[] geom = (val == null) ? new byte[0] : (byte[]) val;
                    data.write(bb4.putInt(geom.length).array());
                    bb4.clear();
                    data.write(geom);
                    break;
            }
        }
        byte[] valuesBytes = data.toByteArray();
        out.write(bb4.putInt(valuesBytes.length).array());
        bb4.clear();
        out.write(valuesBytes);
        return out.toByteArray();
    }
}
