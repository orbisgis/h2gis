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

package org.h2gis.functions.io.file_table;

import org.h2.api.DatabaseEventListener;
import org.h2.api.ErrorCode;
import org.h2.command.ddl.CreateTableData;
import org.h2.engine.Session;
import org.h2.index.Cursor;
import org.h2.index.Index;
import org.h2.index.IndexType;
import org.h2.message.DbException;
import org.h2.mvstore.db.MVSecondaryIndex;
import org.h2.mvstore.db.MVSpatialIndex;
import org.h2.mvstore.db.MVTable;
import org.h2.result.Row;
import org.h2.result.SortOrder;
import org.h2.table.Column;
import org.h2.table.IndexColumn;
import org.h2.table.TableType;
import org.h2.util.MathUtils;
import org.h2.value.TypeInfo;
import org.h2gis.api.FileDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static org.h2gis.functions.io.file_table.H2TableIndex.PK_COLUMN_NAME;

/**
 * A MV table linked with a {@link FileDriver}
 *
 * @author Erwan Bocher (CNRS, 2020)
 * @author Sylvain PALOMINOS (Lab-STICC UBS 2020)
 */
public class H2MVTable extends MVTable {
    private FileDriver driver;
    private static final Logger LOG = LoggerFactory.getLogger(H2MVTable.class);
    private final ArrayList<Index> indexes = new ArrayList<>();
    private Column rowIdColumn;

    public H2MVTable(FileDriver driver, CreateTableData data) {
        super(data, data.session.getDatabase().getStore());
        IndexColumn indexColumn = new IndexColumn(PK_COLUMN_NAME);
        indexColumn.column = data.columns.get(0);
        indexColumn.sortType = SortOrder.ASCENDING;
        indexes.add(new H2TableIndex(driver,this,this.getId(),
                data.schema.getUniqueIndexName(data.session, this,data.tableName + "." +
                        data.columns.get(0).getName() + "_INDEX_"),indexColumn));
        this.driver = driver;
    }
    /**
     * Create row index
     * @param session database session
     */
    public void init(Session session) {        
        IndexColumn indexColumn = new IndexColumn("pk");
        indexColumn.column = new Column("pk", TypeInfo.TYPE_BIGINT);
        indexes.add(0, new H2TableIndex(driver,this,this.getId(), indexColumn));
    }

    @Override
    public boolean lock(Session session, boolean exclusive, boolean force) {
        return false;
    }

    @Override
    public void close(Session session) {
        for (Index index : indexes) {
            index.close(session);
        }
        try {
            driver.close();
        } catch (IOException ex) {
            LOG.error("Error while closing the SHP driver", ex);
        }
    }

    @Override
    public void unlock(Session session) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Row getRow(Session session, long key) {
        return indexes.get(0).getRow(session, key);
    }

    @Override
    public Index addIndex(Session session, String indexName, int indexId, IndexColumn[] cols, IndexType indexType, boolean create, String indexComment) {
        if (indexType.isPrimaryKey()) {
            for (IndexColumn c : cols) {
                Column column = c.column;
                if (column.isNullable()) {
                    throw DbException.get(
                            ErrorCode.COLUMN_MUST_NOT_BE_NULLABLE_1,
                            column.getName());
                }
                column.setPrimaryKey(true);
            }
        }
        boolean isSessionTemporary = isTemporary() && !isGlobalTemporary();
        if (!isSessionTemporary) {
            database.lockMeta(session);
        }
        Index index;
        if (indexType.isSpatial()) {
            index = new MVSpatialIndex(session.getDatabase(), this, indexId, indexName, cols, indexType);
        } else {
            index = new MVSecondaryIndex(session.getDatabase(), this, indexId, indexName, cols, indexType);
        }

        if (index.needRebuild() && getRowCount(session) > 0) {
            rebuild(session, index);
        }

        index.setTemporary(isTemporary());
        if (index.getCreateSQL() != null) {
            index.setComment(indexComment);
            if (isSessionTemporary) {
                session.addLocalTempTableIndex(index);
            } else {
                database.addSchemaObject(session, index);
            }
        }
        indexes.add(index);
        setModified();
        return index;
    }

    /**
     * Rebuild the index
     * @param session
     * @param index
     */
    private void rebuild(Session session, Index index){
        Index scan = getScanIndex(session);
        long remaining = scan.getRowCount(session);
        long total = remaining;
        Cursor cursor = scan.find(session, null, null);
        long i = 0;
        int bufferSize = (int) Math.min(total, database.getMaxMemoryRows());
        ArrayList<Row> buffer = new ArrayList<>(bufferSize);
        String n = getName() + ":" + index.getName();
        int t = MathUtils.convertLongToInt(total);
        while (cursor.next()) {
            Row row = cursor.get();
            buffer.add(row);
            database.setProgress(DatabaseEventListener.STATE_CREATE_INDEX, n,
                    MathUtils.convertLongToInt(i++), t);
            if (buffer.size() >= bufferSize) {
                addRowsToIndex(session, buffer, index);
            }
            remaining--;
        }
        addRowsToIndex(session, buffer, index);
        if (remaining != 0) {
            throw DbException.throwInternalError("rowcount remaining=" + remaining +
                    " " + getName());
        }
    }

    @Override
    public void removeChildrenAndResources(Session session) {
        while (indexes.size() > 2) {
            Index index = indexes.get(2);
            index.remove(session);
            if (index.getName() != null) {
                database.removeSchemaObject(session, index);
            }
            indexes.remove(index);
        }
        super.removeChildrenAndResources(session);
    }

    public static void addRowsToIndex(Session session, ArrayList<Row> list,
                                       Index index) {
        final Index idx = index;
        Collections.sort(list, new Comparator<Row>() {
            @Override
            public int compare(Row r1, Row r2) {
                return idx.compareRows(r1, r2);
            }
        });
        for (Row row : list) {
            index.add(session, row);
        }
        list.clear();
    }

    @Override
    public void removeRow(Session session, Row row) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"removeRow in this driver file");
    }

    @Override
    public void truncate(Session session) {
        long result = getRowCountApproximation();
        for(Index index : indexes) {
            index.truncate(session);
        }
    }

    @Override
    public void addRow(Session session, Row row) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"addRow in this driver file");
    }

    @Override
    public void checkSupportAlter() {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"addRow in this driver file");
    }

    @Override
    public TableType getTableType() {
        return TableType.EXTERNAL_TABLE_ENGINE;
    }

    @Override
    public Index getScanIndex(Session session) {
        // Look for scan index
        for(Index index : indexes) {
            if(index.getIndexType().isScan()) {
                return index;
            }
        }
        return null;
    }

    @Override
    public Index getUniqueIndex() {
        for (Index idx : indexes) {
            if (idx.getIndexType().isUnique()) {
                return idx;
            }
        }
        return null;
    }

    @Override
    public ArrayList<Index> getIndexes() {
        return indexes;
    }

    @Override
    public boolean isLockedExclusively() {
        return false;
    }

    @Override
    public long getMaxDataModificationId() {
        return 0;
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }

    @Override
    public boolean canGetRowCount() {
        return true;
    }

    @Override
    public boolean canDrop() {
        return true;
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
    public void checkRename() {
        //Nothing to check
    }

    @Override
    public Column getRowIdColumn() {
        if (rowIdColumn == null) {
            rowIdColumn = new Column(Column.ROWID, TypeInfo.TYPE_BIGINT);
            rowIdColumn.setTable(this, -1);
        }
        return rowIdColumn;
    }
}
