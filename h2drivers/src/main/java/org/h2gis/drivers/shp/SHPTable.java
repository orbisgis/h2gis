/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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

package org.h2gis.drivers.shp;

import org.h2.command.ddl.CreateTableData;
import org.h2.engine.Session;
import org.h2.index.Index;
import org.h2.index.IndexType;
import org.h2.result.Row;
import org.h2.table.IndexColumn;
import org.h2.table.TableBase;

import java.io.File;
import java.util.ArrayList;

/**
 * A table linked to a SHP and DBF files.
 * @author Nicolas Fortin
 */
public class SHPTable extends TableBase {
    private File shpFile;

    public SHPTable(CreateTableData data, File shpFile) {
        super(data);
        this.shpFile = shpFile;
    }

    @Override
    public void lock(Session session, boolean exclusive, boolean force) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close(Session session) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void unlock(Session s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Index addIndex(Session session, String indexName, int indexId, IndexColumn[] cols, IndexType indexType, boolean create, String indexComment) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeRow(Session session, Row row) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void truncate(Session session) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addRow(Session session, Row row) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void checkSupportAlter() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getTableType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Index getScanIndex(Session session) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Index getUniqueIndex() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ArrayList<Index> getIndexes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isLockedExclusively() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getMaxDataModificationId() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDeterministic() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canGetRowCount() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canDrop() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getRowCount(Session session) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getRowCountApproximation() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getDiskSpaceUsed() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void checkRename() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
