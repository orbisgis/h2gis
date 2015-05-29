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

import org.h2.api.TableEngine;
import org.h2.command.ddl.CreateTableData;
import org.h2.api.ErrorCode;
import org.h2.message.DbException;
import org.h2.table.Column;
import org.h2.table.Table;
import org.h2.util.StringUtils;
import org.h2.value.Value;
import org.h2gis.drivers.FileDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Implement theses abstract methods in order to define a file engine.
 * @author Nicolas Fortin
 * @param <Driver> file driver
 */
public abstract class FileEngine<Driver extends FileDriver> implements TableEngine {
    private Logger LOGGER = LoggerFactory.getLogger(FileEngine.class);

    @Override
    public Table createTable(CreateTableData data) {
        if(data.tableEngineParams.isEmpty()) {
            throw DbException.get(ErrorCode.FILE_NOT_FOUND_1);
        }
        File filePath = new File(StringUtils.javaDecode(data.tableEngineParams.get(0)));
        if(!filePath.exists()) {
            // Do not throw an exception as it will prevent the user from opening the database
            LOGGER.error("File not found:\n"+filePath.getAbsolutePath()+"\nThe table "+data.tableName+" will be empty.");
            return new DummyTable(data);
        }
        try {
            Driver driver = createDriver(filePath, data.tableEngineParams);
            if(data.columns.isEmpty()) {
                feedCreateTableData(driver, data);
                // Add primary key column
                String pkColumnName = getUniqueColumnName(H2TableIndex.PK_COLUMN_NAME, data.columns);
                Column pk = new Column(pkColumnName, Value.LONG);
                pk.setPrimaryKey(true);
                pk.setNullable(false);
                data.columns.add(0, pk);
            }
            H2Table shpTable = new H2Table(driver, data);
            shpTable.init(data.session);
            return shpTable;
        } catch (IOException ex) {
            throw DbException.get(ErrorCode.IO_EXCEPTION_1,ex);
        }
    }

    /**
     * Compute unique column name among the other columns
     * @param base Returned name if there is no duplicate
     * @param columns Other existing columns
     * @return Unique column name
     */
    public static String getUniqueColumnName(String base, List<Column> columns) {
        String cursor = base;
        int cpt = 2;
        boolean findDuplicate= true;
        while(findDuplicate) {
            findDuplicate = false;
            for (Column column : columns) {
                if (column.getName().equalsIgnoreCase(cursor)) {
                    findDuplicate = true;
                    break;
                }
            }
            if(findDuplicate) {
                cursor = base + Integer.toString(cpt);
            }
        }
        return cursor;
    }
    /**
     * Create the driver instance using the file name and additional arguments provided in SQL create table request.
     * @param filePath First argument, file name
     * @param args Additional argument, contains the file name as first argument
     * @return Instance of FileDriver
     * @throws IOException
     */
    protected  abstract Driver createDriver(File filePath, List<String> args) throws IOException;


    /**
     * Add columns definition of the file into the CreateTableData instance.
     * @param driver driver object
     * @param data Data to initialise
     * @throws java.io.IOException
     */
    protected abstract void feedCreateTableData(Driver driver,CreateTableData data) throws IOException;
}
