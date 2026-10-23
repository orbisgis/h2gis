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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Rows of a JDBC {@link ResultSet} serialized column by column for the C callers.
 * <p>
 * Buffer layout (little endian):
 * <pre>
 * u32 column_count
 * u32 row_count
 * u64 column_offset[column_count]   // from the start of the buffer
 * column[column_count]              // see ColumnWrapper
 * </pre>
 *
 * @author Maël PHILIPPE, CNRS
 * @author Erwan BOCHER, CNRS
 */
public class ResultSetWrapper {

    private final List<ColumnWrapper> columns = new ArrayList<>();
    private int rowCount = 0;

    public List<ColumnWrapper> getColumns() {
        return columns;
    }

    public int getColumnCount() {
        return columns.size();
    }

    public int getRowCount() {
        return rowCount;
    }

    /**
     * Read the remaining rows of a result set.
     *
     * @param rs result set
     * @return the wrapper
     * @throws SQLException if a value can't be read
     */
    public static ResultSetWrapper from(ResultSet rs) throws SQLException {
        return from(rs, -1);
    }

    /**
     * Read the next row of a result set (no row at the end).
     *
     * @param rs result set
     * @return the wrapper
     * @throws SQLException if a value can't be read
     */
    public static ResultSetWrapper fromOne(ResultSet rs) throws SQLException {
        return from(rs, 1);
    }

    /**
     * Read up to {@code batchSize} rows of a result set.
     *
     * @param rs        result set
     * @param batchSize maximum number of rows
     * @return the wrapper
     * @throws SQLException if a value can't be read
     */
    public static ResultSetWrapper fromBatch(ResultSet rs, int batchSize) throws SQLException {
        return from(rs, Math.max(0, batchSize));
    }

    /**
     * @param rs      result set
     * @param maxRows maximum number of rows to read, negative for all
     * @return the wrapper
     * @throws SQLException if a value can't be read
     */
    public static ResultSetWrapper from(ResultSet rs, int maxRows) throws SQLException {
        ResultSetWrapper wrapper = new ResultSetWrapper();
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        for (int i = 1; i <= colCount; i++) {
            wrapper.columns.add(new ColumnWrapper(meta.getColumnLabel(i), meta.getColumnType(i),
                    meta.getColumnTypeName(i)));
        }
        while ((maxRows < 0 || wrapper.rowCount < maxRows) && rs.next()) {
            for (int i = 1; i <= colCount; i++) {
                wrapper.columns.get(i - 1).read(rs, i);
            }
            wrapper.rowCount++;
        }
        return wrapper;
    }

    /**
     * @return the serialized result, see the class documentation
     */
    public byte[] serialize() {
        List<byte[]> blobs = new ArrayList<>(columns.size());
        long total = 8L + 8L * columns.size();
        for (ColumnWrapper col : columns) {
            byte[] b = col.serialize();
            blobs.add(b);
            total += b.length;
        }
        if (total > Integer.MAX_VALUE - 8) {
            throw new IllegalStateException("result too large for one buffer (" + total
                    + " bytes): read it in batches with h2gis_fetch_batch");
        }
        ByteBuffer out = ByteBuffer.allocate((int) total).order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(columns.size());
        out.putInt(rowCount);
        long offset = 8L + 8L * columns.size();
        for (byte[] b : blobs) {
            out.putLong(offset);
            offset += b.length;
        }
        for (byte[] b : blobs) {
            out.put(b);
        }
        return out.array();
    }
}
