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

package org.h2gis.functions.io.file_table;

import org.h2.command.ddl.CreateTableData;
import org.h2.engine.SessionLocal;
import org.h2.index.Index;
import org.h2.index.IndexType;
import org.h2.mvstore.db.MVTable;
import org.h2.result.Row;
import org.h2.table.Column;
import org.h2.table.IndexColumn;
import org.h2.table.TableType;
import org.h2.value.TypeInfo;

import java.util.ArrayList;

/**
 * When linked files are not available, this table defines an empty MV table
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (Lab-STICC UBS 2020)
 */
public class DummyMVTable extends MVTable {

    public DummyMVTable(CreateTableData data) {
        super(data, data.session.getDatabase().getStore());
    }

    @Override
    public void removeChildrenAndResources(SessionLocal session) {
        super.removeChildrenAndResources(session);
    }

    @Override
    public Index addIndex(SessionLocal session, String indexName, int indexId, IndexColumn[] cols, int uniqueColumnCount, IndexType indexType, boolean create, String indexComment) {
        return null;
    }

    @Override
    public boolean lock(SessionLocal session, int lockType) {
        return false;
    }

    @Override
    public void close(SessionLocal session) {
        //Nothing to do
    }

    @Override
    public void unlock(SessionLocal s) {
        //Nothing to do
    }

    @Override
    public void removeRow(SessionLocal session, Row row) {
        //Nothing to do
    }

    @Override
    public long truncate(SessionLocal session) {
        return -1;
    }

    @Override
    public void addRow(SessionLocal session, Row row) {
        //Nothing to do
    }

    @Override
    public void checkSupportAlter() {
        //Nothing to do
    }

    @Override
    public TableType getTableType() {
        return TableType.EXTERNAL_TABLE_ENGINE;
    }

    @Override
    public Index getScanIndex(SessionLocal session) {
        return createIndex();
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

    //TODO check this method
    /*@Override
    public boolean canGetRowCount() {
        return true;
    }*/

    @Override
    public boolean canDrop() {
        return true;
    }

    @Override
    public long getRowCount(SessionLocal session) {
        return 0;
    }

    @Override
    public long getRowCountApproximation(SessionLocal session) {
        return 0;
    }

    @Override
    public long getDiskSpaceUsed(boolean total, boolean approximate) {
        return 0;
    }

    @Override
    public void checkRename() {
        //Nothing to do
    }
    
    /**
     * Create a dummy index
     */
    private Index createIndex(){
        IndexColumn indexColumn = new IndexColumn("key");
        indexColumn.column = new Column("key", TypeInfo.TYPE_BIGINT);
        return new DummyIndex(this, getId(), indexColumn);
    }
}
