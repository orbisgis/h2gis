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


import org.h2.command.query.AllColumnsForPlan;
import org.h2.engine.SessionLocal;
import org.h2.index.Index;
import org.h2.index.Cursor;
import org.h2.index.IndexType;
import org.h2.result.Row;
import org.h2.result.SearchRow;
import org.h2.result.SortOrder;
import org.h2.table.IndexColumn;
import org.h2.table.Table;
import org.h2.table.TableFilter;

/**
 * When linked files are not available, this table index defines an empty table
 * @author Nicolas Fortin
 */
public class DummyIndex extends Index {
   
        
    public DummyIndex(Table table,int id, IndexColumn indexColumn) {
        super(table,id,table.getName()+"_DATA",new IndexColumn[] {indexColumn}, IndexType.createScan(true));
    }

    @Override
    public void checkRename() {

    }

    @Override
    public void close(SessionLocal session) {
    }

    @Override
    public void add(SessionLocal session, Row row) {
    }

    @Override
    public void remove(SessionLocal session, Row row) {
    }

    @Override
    public Cursor find(SessionLocal session, SearchRow first, SearchRow last) {
        return new DummyCursor();
    }

    @Override
    public double getCost(SessionLocal session, int[] ints, TableFilter[] tableFilters, int i, SortOrder sortOrder, AllColumnsForPlan allColumnsForPlan) {
        return 0;
    }

    @Override
    public void remove(SessionLocal session) {
    }

    @Override
    public void truncate(SessionLocal session) {
    }

    @Override
    public boolean canGetFirstOrLast() {
        return true;
    }

    @Override
    public Cursor findFirstOrLast(SessionLocal session, boolean first) {
        return new DummyCursor();
    }

    @Override
    public boolean needRebuild() {
        return false;
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
    public long getDiskSpaceUsed() {
        return 0;
    }    

    private static class DummyCursor implements Cursor {
        @Override
        public Row get() {
            return null;
        }

        @Override
        public SearchRow getSearchRow() {
            return get();
        }

        @Override
        public boolean next() {
            return false;
        }

        @Override
        public boolean previous() {
            return false;
        }
    }
}
