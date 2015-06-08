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
package org.h2gis.drivers.file_table;

import org.h2.api.ErrorCode;
import org.h2.engine.Session;
import org.h2.index.BaseIndex;
import org.h2.index.Cursor;
import org.h2.index.IndexCondition;
import org.h2.index.IndexType;
import org.h2.message.DbException;
import org.h2.result.Row;
import org.h2.result.SearchRow;
import org.h2.result.SortOrder;
import org.h2.table.Column;
import org.h2.table.IndexColumn;
import org.h2.table.Table;
import org.h2.table.TableFilter;
import org.h2.value.DataType;
import org.h2.value.Value;
import org.h2.value.ValueLong;
import org.h2gis.drivers.FileDriver;

import java.io.IOException;

/**
 * ScanIndex of {@link org.h2gis.drivers.FileDriver}, the key is the row index [1-n].
 * @author Nicolas Fortin
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
     */
    public H2TableIndex(FileDriver driver, Table table, int id) {
        this.isScanIndex = true;
        this.driver = driver;
        IndexColumn indexColumn = new IndexColumn();
        indexColumn.columnName = "key";
        indexColumn.column = new Column("key", Value.LONG);
        initBaseIndex(table, id, table.getName() + "_ROWID_", new IndexColumn[]{indexColumn}, IndexType.createScan(true));
    }

    /**
     * Constructor for primary key index.
     * @param driver Linked file driver
     * @param table Linked table
     * @param id Index identifier
     * @param PKColumn Primary key column declaration
     * @param indexName Unique index name
     */
    public H2TableIndex(FileDriver driver, Table table, int id, Column PKColumn, String indexName) {
            this.isScanIndex = false;
            this.driver = driver;
            IndexColumn indexColumn = new IndexColumn();
            indexColumn.columnName = PK_COLUMN_NAME;
            indexColumn.column = PKColumn;
            indexColumn.sortType = SortOrder.ASCENDING;
            initBaseIndex(table, id, indexName, new IndexColumn[]{indexColumn}, IndexType.createPrimaryKey(true, false));
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
        try {
            Object[] driverRow = driver.getRow(key - 1);
            Value[] values = new Value[driverRow.length + 1];
            Column[] columns = table.getColumns();
            values[0] = ValueLong.get(key);
            for(int idField=1;idField<=driverRow.length;idField++) {
                // TODO in H2, switch on type parameter instead of if elseif
                values[idField] = DataType.convertToValue(session, driverRow[idField - 1], columns[idField - 1].getType());
            }
            Row row =  new Row(values, Row.MEMORY_CALCULATE);
            row.setKey(key);
            return row;
        } catch (IOException ex) {
            throw DbException.get(ErrorCode.IO_EXCEPTION_1,ex);
        }
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
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"remove in Shape files");
    }

    @Override
    public Cursor find(Session session, SearchRow first, SearchRow last) {
        if (!isScanIndex) {
            Row remakefirst = new Row(null, 0);
            if(first != null) {
                remakefirst.setKey(first.getValue(0).getLong());
            } else {
                remakefirst.setKey(1);
            }
            Row remakeLast = new Row(null, 0);
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
    public boolean canScan() {
        return true;
    }

    @Override
    public boolean canFindNext() {
        return true;
    }

    @Override
    public double getCost(Session session, int[] masks,TableFilter filter ,SortOrder sortOrder) {
        if(masks == null) {
            return Double.MAX_VALUE;
        }
        for (Column column : columns) {
            int index = column.getColumnId();
            int mask = masks[index];
            if ((mask & IndexCondition.EQUALITY) != IndexCondition.EQUALITY &&
                    (mask & IndexCondition.START) != IndexCondition.START &&
                    (mask & IndexCondition.END) != IndexCondition.END &&
                    (mask & IndexCondition.RANGE) != IndexCondition.RANGE) {
                return Double.MAX_VALUE;
            }
        }
        return 2;
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
            Row row =  new Row(new Value[tIndex.getTable().getColumns().length], Row.MEMORY_CALCULATE);
            row.setKey(rowIndex);
            // Add indexed columns values
            for(IndexColumn column : tIndex.getIndexColumns()) {
                if(column.column.getColumnId() >= 0) {
                    row.setValue(column.column.getColumnId(), ValueLong.get(rowIndex));
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
}
