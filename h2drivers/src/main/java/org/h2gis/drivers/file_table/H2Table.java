/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.drivers.file_table;

import org.h2.command.ddl.CreateTableData;
import org.h2.constant.ErrorCode;
import org.h2.engine.Session;
import org.h2.index.Index;
import org.h2.index.IndexType;
import org.h2.message.DbException;
import org.h2.result.Row;
import org.h2.table.Column;
import org.h2.table.IndexColumn;
import org.h2.table.TableBase;
import org.h2.value.Value;
import org.h2gis.drivers.FileDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A table linked with a {@link org.h2gis.drivers.FileDriver}
 * @author Nicolas Fortin
 */
public class H2Table extends TableBase {
    private FileDriver driver;
    private static final Logger LOG = LoggerFactory.getLogger(H2Table.class);
    private H2TableIndex baseIndex;
    private Column rowIdColumn;

    public H2Table(FileDriver driver, CreateTableData data) throws IOException {
        super(data);
        this.driver = driver;
    }

    /**
     * Create row index
     * @param session database session
     */
    public void init(Session session) {
        baseIndex = new H2TableIndex(driver,this,this.getId());
    }

    @Override
    public void lock(Session session, boolean exclusive, boolean force) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close(Session session) {
        try {
            driver.close();
        } catch (IOException ex) {
            LOG.error("Error while closing the SHP driver", ex);
        }
    }

    @Override
    public void unlock(Session s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Index addIndex(Session session, String indexName, int indexId, IndexColumn[] cols, IndexType indexType, boolean create, String indexComment) {
        // Index not managed
        return null;
    }

    @Override
    public void removeRow(Session session, Row row) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"removeRow in Shape files");
    }

    @Override
    public void truncate(Session session) {
        baseIndex.truncate(session);
    }

    @Override
    public void addRow(Session session, Row row) {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"addRow in Shape files");
    }

    @Override
    public void checkSupportAlter() {
        throw DbException.get(ErrorCode.FEATURE_NOT_SUPPORTED_1,"addRow in Shape files");
    }

    @Override
    public String getTableType() {
        return TableBase.EXTERNAL_TABLE_ENGINE;
    }

    @Override
    public Index getScanIndex(Session session) {
        return baseIndex;
    }

    @Override
    public Index getUniqueIndex() {
        return null;
    }

    @Override
    public ArrayList<Index> getIndexes() {
        return new ArrayList<Index>();
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
            rowIdColumn = new Column(Column.ROWID, Value.LONG);
            rowIdColumn.setTable(this, -1);
        }
        return rowIdColumn;
    }
}
