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

import org.h2.api.TableEngine;
import org.h2.command.ddl.CreateTableData;
import org.h2.constant.ErrorCode;
import org.h2.message.DbException;
import org.h2.table.TableBase;
import org.h2gis.drivers.shp.internal.SHPDriver;

import java.io.File;
import java.io.IOException;

/**
 * SHP Table factory.
 * @author Nicolas Fortin
 */
public class SHPEngine implements TableEngine {
    /**
     * @param data tableEngineParams must contains file path.
     * @return A Table instance connected to the provided file path. First column is geometry field.
     */
    @Override
    public TableBase createTable(CreateTableData data) {
        if(data.tableEngineParams.isEmpty()) {
            throw DbException.get(ErrorCode.FILE_NOT_FOUND_1);
        }
        File filePath = new File(data.tableEngineParams.get(0));
        if(!filePath.exists()) {
            throw DbException.get(ErrorCode.FILE_NOT_FOUND_1,filePath.getAbsolutePath());
        }
        try {
            SHPDriver driver = new SHPDriver();
            driver.initDriverFromFile(filePath);
            SHPTableIndex.feedCreateTableData(driver, data);
            SHPTable shpTable = new SHPTable(driver, data);
            shpTable.init(data.session);
            return shpTable;
        } catch (IOException ex) {
            throw DbException.get(ErrorCode.IO_EXCEPTION_1,ex);
        }
    }
}
