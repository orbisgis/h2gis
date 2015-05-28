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

import org.h2.command.ddl.CreateTableData;
import org.h2.engine.Session;
import org.h2.index.Index;
import org.h2.index.IndexType;
import org.h2.result.Row;
import org.h2.table.IndexColumn;
import org.h2.table.TableBase;

import java.util.ArrayList;

/**
 * When linked files are not available, this table defines an empty table
 * @author Nicolas Fortin
 */
public class DummyTable extends TableBase {

    public DummyTable(CreateTableData data) {
        super(data);
    }
    @Override
    public Index addIndex(Session session, String indexName, int indexId, IndexColumn[] cols, IndexType indexType, boolean create, String indexComment) {
        return null;
    }

    @Override
    public boolean lock(Session session, boolean exclusive, boolean force) {
        return false;
    }

    @Override
    public void close(Session session) {
    }

    @Override
    public void unlock(Session s) {
    }

    @Override
    public void removeRow(Session session, Row row) {
    }

    @Override
    public void truncate(Session session) {
    }

    @Override
    public void addRow(Session session, Row row) {
    }

    @Override
    public void checkSupportAlter() {
    }

    @Override
    public String getTableType() {
        return TableBase.EXTERNAL_TABLE_ENGINE;
    }

    @Override
    public Index getScanIndex(Session session) {
        return new DummyIndex(this, getId());
    }

    @Override
    public Index getUniqueIndex() {
        return new DummyIndex(this, getId());
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
        return 0;
    }

    @Override
    public long getRowCountApproximation() {
        return 0;
    }

    @Override
    public long getDiskSpaceUsed() {
        return 0;
    }

    @Override
    public void checkRename() {
    }
}
