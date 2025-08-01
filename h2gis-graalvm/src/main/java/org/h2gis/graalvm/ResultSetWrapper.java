/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.graalvm;

import org.h2.value.ValueGeometry;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class for representing the content of a JDBC {@link ResultSet} in memory,
 * allowing for structured access to column data and metadata. Used for transferring
 * data from JDBC to native code via GraalVM.
 *
 * @author Maël PHILIPPE, CNRS
 * @author Erwan BOCHER, CNRS
 */
public class ResultSetWrapper {
    private final List<ColumnWrapper> columns = new ArrayList<>();
    private int rowCount = 0;

    /**
     * Returns the list of wrapped columns in the result set.
     *
     * @return list of {@link ColumnWrapper}
     */
    public List<ColumnWrapper> getColumns() {
        return columns;
    }

    /**
     * Returns the number of columns in the result set.
     *
     * @return column count
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Returns the number of rows in the result set.
     *
     * @return row count
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Adds a column to the result set.
     *
     * @param column the {@link ColumnWrapper} to add
     */
    public void addColumn(ColumnWrapper column) {
        columns.add(column);
    }

    /**
     * Adds a new column to the result set using its name and SQL type.
     *
     * @param name     the name of the column
     * @param type     the SQL type (from {@link java.sql.Types})
     * @param typeName the name of the SQL type
     */
    public void addColumn(String name, int type, String typeName) {
        columns.add(new ColumnWrapper(name, type, typeName));
    }

    /**
     * Retrieves the column at the specified index.
     *
     * @param columnIndex the column index (0-based)
     * @return the {@link ColumnWrapper} if the index is valid, otherwise {@code null}
     */
    public ColumnWrapper getColumn(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columns.size()) {
            return null;
        }
        return columns.get(columnIndex);
    }

    /**
     * Sets the number of rows in the result set.
     *
     * @param rowCount the number of rows
     */
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * Creates a {@code ResultSetWrapper} instance from a JDBC {@link ResultSet}.
     * Reads all column metadata and row data eagerly into memory.
     *
     * @param rs the {@link ResultSet} to wrap
     * @return a new {@code ResultSetWrapper} containing the extracted data
     */
    public static ResultSetWrapper from(ResultSet rs) {
        try {
            ResultSetWrapper wrapper = new ResultSetWrapper();
            ResultSetMetaData rsm = rs.getMetaData();
            int colCount = rsm.getColumnCount();

            createColumns(wrapper, rsm, colCount);

            int rowCounter = 0;
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    if (wrapper.columns.get(i - 1).getTypeName().startsWith("geometry")) {
                        Object obj = rs.getObject(i);
                        wrapper.columns.get(i - 1).addValue(ValueGeometry.getFromGeometry(obj).getBytes());
                    } else {
                        wrapper.columns.get(i - 1).addValue(rs.getObject(i));
                    }
                }
                rowCounter++;
            }
            wrapper.setRowCount(rowCounter);
            return wrapper;

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

    /**
     * Instantiate the ColumnsWrappers for a ResultsetWrapper given a resultset metadata and a column number.
     * @param wrapper  the resultset to which the columns will be added
     * @param rsm      the ResultsetMetadata that will be used to know the columns to add
     * @param colCount the number of columns to add.
     **/
    public static void createColumns(ResultSetWrapper wrapper, ResultSetMetaData rsm, int colCount) throws Exception {
        for (int i = 1; i <= colCount; i++) {
            ColumnWrapper column = new ColumnWrapper(rsm.getColumnLabel(i), rsm.getColumnType(i), rsm.getColumnTypeName(i).toLowerCase());
            wrapper.addColumn(column);
        }
    }

    /**
     * Creates a {@code ResultSetWrapper} instance from a JDBC {@link ResultSet}.
     * Reads all column metadata and the first row data eagerly into memory.
     *
     * @param rs the {@link ResultSet} to wrap
     * @return a new {@code ResultSetWrapper} containing the extracted data
     */
    public static ResultSetWrapper fromOne(ResultSet rs) {
        try {
            ResultSetWrapper wrapper = new ResultSetWrapper();
            ResultSetMetaData rsm = rs.getMetaData();
            int colCount = rsm.getColumnCount();

            createColumns(wrapper, rsm, colCount);

            int rowCounter = 0;
            if (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    wrapper.columns.get(i - 1).addValue(rs.getObject(i));
                }
                rowCounter++;
            }
            wrapper.setRowCount(rowCounter);
            return wrapper;

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }


    /**
     * Serializes the full result set into a flat byte buffer.
     * Format:
     * - 4 bytes reserved (0)
     * - 4 bytes: column count
     * - 4 bytes: row count
     * - N * 8 bytes: offset to each column’s serialized data (relative to start of buffer)
     * - Serialized data of each column
     *
     * @return byte array of the fully serialized result set (metadata + columns)
     * @throws Exception if serialization fails
     */
    public byte[] serialize() throws Exception {
        int metadataSize = 4 + 4 + (8 * columns.size()); // reserved + colCount + rowCount + pointers

        List<byte[]> columnBuffers = new ArrayList<>();
        for (ColumnWrapper col : columns) {
            columnBuffers.add(col.serialize());
        }

        ByteBuffer meta = ByteBuffer.allocate(metadataSize).order(ByteOrder.LITTLE_ENDIAN);
        meta.putInt(columns.size());
        meta.putInt(rowCount);

        int currentOffset = metadataSize;
        for (byte[] buf : columnBuffers) {
            meta.putLong(currentOffset);
            currentOffset += buf.length;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(meta.array());
        for (byte[] colBuf : columnBuffers) {
            out.write(colBuf);
        }

        return out.toByteArray();
    }
}
