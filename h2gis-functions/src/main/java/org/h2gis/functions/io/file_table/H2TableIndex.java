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

package org.h2gis.functions.io.file_table;

import org.h2.api.ErrorCode;
import org.h2.command.dml.AllColumnsForPlan;
import org.h2.engine.Constants;
import org.h2.engine.Session;
import org.h2.index.BaseIndex;
import org.h2.index.Cursor;
import org.h2.index.IndexType;
import org.h2.message.DbException;
import org.h2.result.Row;
import org.h2.result.SearchRow;
import org.h2.result.SortOrder;
import org.h2.table.IndexColumn;
import org.h2.table.Table;
import org.h2.table.TableFilter;
import org.h2.value.Value;
import org.h2.value.ValueBigint;
import org.h2gis.api.FileDriver;

import java.io.IOException;

/**
 * ScanIndex of {@link org.h2gis.api.FileDriver}, the key is the row index [1-n].
 * @author Nicolas Fortin
 * @author Erwan Bocher, CNRS, 2020
 */
public class H2TableIndex extends BaseIndex {
    public static final String PK_COLUMN_NAME = "PK";

    private FileDriver driver;
    private final boolean isScanIndex;

    /**
     * Constructor for scan index. Hidden column _ROWID_.
     * @param driver Linked file driver
     * @param table Linked table
     * @param id Index identifier
     * @param indexColumn Column to index
     */
    public H2TableIndex(FileDriver driver, Table table, int id,  IndexColumn indexColumn) {  
        super(table, id, table.getName() + "_ROWID_", new IndexColumn[]{indexColumn}, IndexType.createScan(true));
        this.isScanIndex = true;
        this.driver = driver;
    }

    /**
     * Constructor for primary key index.
     * @param driver Linked file driver
     * @param table Linked table
     * @param id Index identifier
     * @param indexName Unique index name
     * @param indexColumn Column to index
     */
    public H2TableIndex(FileDriver driver, Table table, int id, String indexName, IndexColumn indexColumn) {
            super(table, id, indexName, new IndexColumn[]{indexColumn}, IndexType.createPrimaryKey(true, false));
            this.isScanIndex = false;
            this.driver = driver;
    }

    @Override
    public void checkRename() {
        // Nothing to check
    }

    public FileDriver getDriver() {
        return driver;
    }

    @Override
    public Row getRow(Session session, long key) {
        return new DriverRow(driver, key);
    }

    @Override
    public void close(Session session) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void add(Session session, Row row) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void remove(Session session, Row row) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"remove in file");
    }

    @Override
    public Cursor find(Session session, SearchRow first, SearchRow last) {
        if (!isScanIndex) {
            Row remakefirst = Row.get(null, 0);
            if(first != null) {
                remakefirst.setKey(first.getValue(0).getLong());
            } else {
                remakefirst.setKey(1);
            }
            Row remakeLast = Row.get(null, 0);
            if(last != null) {
                remakeLast.setKey(last.getValue(0).getLong());
            } else {
                remakeLast.setKey(getRowCount(session));
            }
            first = remakefirst;
            last = remakeLast;
        }
        return new SHPCursor(this, first, last, session);
    }

    @Override
    public double getCost(Session session, int[] masks, TableFilter[] tableFilters, int filter, SortOrder sortOrder, AllColumnsForPlan allColumnsForPlan) {
        // Copied from h2/src/main/org/h2/mvstore/db/MVPrimaryIndex.java#L210
        // Must kept sync with this
        try {
            return 10 * getCostRangeIndex(masks, driver.getRowCount(),
                    tableFilters, filter, sortOrder, true, allColumnsForPlan);
        } catch (IllegalStateException e) {
            throw DbException.get(ErrorCode.OBJECT_CLOSED, e);
        }
    }

    @Override
    public void remove(Session session) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"remove in Shape files");
    }

    @Override
    public void truncate(Session session) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"truncate in Shape files");
    }

    @Override
    public boolean canGetFirstOrLast() {
        return true;
    }

    @Override
    public Cursor findFirstOrLast(Session session, boolean first) {
        return new SHPCursor(this,first ? 0 : getRowCount(session),session);
    }

    @Override
    public boolean needRebuild() {
        return false;
    }

    @Override
    public long getRowCount(Session session) {
        return driver.getRowCount();
    }

    @Override
    public long getRowCountApproximation() {
        return driver.getRowCount();
    }

    @Override
    public long getDiskSpaceUsed() {
        return 0;
    }

    @Override
    public boolean isRowIdIndex() {
        return isScanIndex;
    }
    

    private static class SHPCursor implements Cursor {
        private H2TableIndex tIndex;
        private long rowIndex;
        private Session session;
        private SearchRow begin, end;

        private SHPCursor(H2TableIndex tIndex, long rowIndex, Session session) {
            this.tIndex = tIndex;
            this.rowIndex = rowIndex;
            this.session = session;
        }

        private SHPCursor(H2TableIndex tIndex, SearchRow begin, SearchRow end, Session session) {
            this.tIndex = tIndex;
            this.session = session;
            this.begin = begin;
            this.end = end;
            this.rowIndex = begin == null ? 0 : begin.getKey() - 1;
        }

        @Override
        public Row get() {
            return tIndex.getRow(session, rowIndex);
        }

        @Override
        public SearchRow getSearchRow() {
            Row row = Row.get(new Value[tIndex.getTable().getColumns().length], Row.MEMORY_CALCULATE);
            row.setKey(rowIndex);
            // Add indexed columns values
            for(IndexColumn column : tIndex.getIndexColumns()) {
                if(column.column.getColumnId() >= 0) {
                    row.setValue(column.column.getColumnId(), ValueBigint.get(rowIndex));
                }
            }
            return row;
        }

        @Override
        public boolean next() {
            if(rowIndex < tIndex.getRowCount(session) && (end == null || rowIndex < end.getKey())) {
                rowIndex ++;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean previous() {
            if(rowIndex > 0 && (begin == null || rowIndex >= begin.getKey())) {
                rowIndex --;
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * This class is requiring only field value on demand instead of gathering the full row values from drivers
     */
    public static class DriverRow extends Row {
        FileDriver driver;
        int memory; // estimated row size in bytes

        public DriverRow(FileDriver driver, long key) {
            this.driver = driver;
            this.key = key;
        }

        @Override
        public Value[] getValueList() {
            try {
                Value[] values = new Value[getColumnCount()];
                values[0] = ValueBigint.get(key);
                for(int i = 1; i < values.length; i++) {
                    values[i] = (Value)(driver.getField(key - 1, i - 1));
                }
                return values;
            } catch (IOException ex) {
                throw DbException.get(ErrorCode.IO_EXCEPTION_1, ex);
            }
        }

        @Override
        public int getColumnCount() {
            return driver.getFieldCount() + 1;
        }

        @Override
        public Value getValue(int column) {
            if(column == ROWID_INDEX) {
                return ValueBigint.get(key);
            } else {
                try {
                    if(column == 0) {
                        // pk
                        return ValueBigint.get(key);
                    } else {
                        return (Value)(driver.getField(key - 1, column - 1));
                    }
                } catch (IOException ex) {
                    throw DbException.get(ErrorCode.IO_EXCEPTION_1,ex);
                }
            }
        }

        @Override
        public void setValue(int i, Value value) {
            if (i == ROWID_INDEX) {
                key = value.getLong();
            }
        }

        @Override
        public int getMemory() {
            if (memory != MEMORY_CALCULATE) {
                return memory;
            }
            return memory = calculateMemory();
        }

        @Override
        public void copyFrom(SearchRow source) {
            setKey(source.getKey());
            for (int i = 0; i < getColumnCount(); i++) {
                setValue(i, source.getValue(i));
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("( /* key:").append(key).append(" */ ");
            for (int i = 0, length = getColumnCount(); i < length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                Value v = getValue(i);
                builder.append(v == null ? "null" : v.getTraceSQL());
            }
            return builder.append(')').toString();
        }

        /**
         * Calculate the estimated memory used for this row, in bytes.
         *
         * @return the memory
         */
        int calculateMemory() {
            int m = Constants.MEMORY_ROW + Constants.MEMORY_ARRAY + getColumnCount() * Constants.MEMORY_POINTER;
            m += driver.getEstimatedRowSize(key - 1);
            return m;
        }
    }
}
