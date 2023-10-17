/**
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
package org.h2gis.functions.io.fgb;

import org.h2.command.ddl.CreateTableData;
import org.h2.table.Column;
import org.h2.util.ParserUtil;
import org.h2.util.StringUtils;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.DriverManager;
import org.h2gis.functions.io.fgb.fileTable.FGBDriver;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

/**
 * FlatGeobuffer reader/write driver function
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class FGBDriverFunction implements DriverFunction {
    private static final int BATCH_MAX_SIZE = 100;
    public static String DESCRIPTION = "FlatGeoBuffer";
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[0];
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"fgb"};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("fgb")) {
            return "FlatGeobuf";
        } else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
         return extension.equals("fgb");
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, null, false,progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, "", deleteFiles, progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String options, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        progress  = DriverManager.check(connection, tableReference, fileName, progress);
        FGBWriteDriver fgbWriteDriver = new FGBWriteDriver(connection);
        if(options != null) {
            String[] keyValuePairs = StringUtils.arraySplit(options, ' ', false);
            for (String pair : keyValuePairs) {
                int index = pair.indexOf('=');
                String key = StringUtils.trimSubstring(pair, 0, index);
                String value = pair.substring(index + 1);
                if (key.equalsIgnoreCase("createIndex")) {
                    fgbWriteDriver.setCreateIndex(Boolean.parseBoolean(value));
                } else if (key.equalsIgnoreCase("nodeSize")) {
                    fgbWriteDriver.setPackedRTreeNodeSize(Short.parseShort(value.trim()));
                }
            }
        }
        try {
            fgbWriteDriver.write(progress, tableReference, fileName, deleteFiles);
            return new String[]{fileName.getAbsolutePath()};
        }catch (SQLException|IOException ex){
            throw new SQLException(ex);
        }
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String options, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, options, false, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, "", false, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String options, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, options, false, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, "", deleteTables, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String options, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        final DBTypes dbType = DBUtils.getDBType(connection);
        String sqlTableName = TableLocation.parse(tableReference, dbType).toString();
        // forge the sql query to create table
        try(Statement st = connection.createStatement()) {
            if(deleteTables) {
                st.execute("DROP TABLE IF EXISTS " + sqlTableName);
            } else if (JDBCUtilities.tableExists(connection, tableReference)) {
                throw new SQLException("Table " + tableReference + " already exists in the database");
            }
            FGBEngine fgbEngine = new FGBEngine();
            FGBDriver fgbDriver = fgbEngine.createDriver(fileName, Collections.singletonList(options));
            fgbDriver.setCacheRowAddress(false); // we will read the file sequentially, it is useless to cache feature location
            CreateTableData createTableData = new CreateTableData();
            fgbEngine.feedCreateTableData(fgbDriver, createTableData);
            StringBuilder createTableQuery = new StringBuilder("CREATE TABLE ");
            createTableQuery.append(sqlTableName);
            createTableQuery.append("(");
            boolean firstColumn = true;
            for(Column column : createTableData.columns) {
                if(!firstColumn) {
                    createTableQuery.append(",");
                }
                firstColumn = false;
                createTableQuery.append(column.getCreateSQL());
            }
            createTableQuery.append(")");
            st.execute(createTableQuery.toString());
            StringBuilder preparedStatementQuery = new StringBuilder("INSERT INTO ");
            preparedStatementQuery.append(sqlTableName);
            preparedStatementQuery.append("(");
            firstColumn = true;
            for(Column column : createTableData.columns) {
                if(!firstColumn) {
                    preparedStatementQuery.append(",");
                }
                firstColumn = false;
                ParserUtil.quoteIdentifier(preparedStatementQuery, column.getName(), Column.DEFAULT_SQL_FLAGS);
            }
            preparedStatementQuery.append(") VALUES (");
            preparedStatementQuery.append(String.join(",",
                    Collections.nCopies(createTableData.columns.size(), "?")));
            preparedStatementQuery.append(")");
            int columnCount = fgbDriver.getFieldCount();
            ProgressVisitor rowCopyProgress = progress.subProcess((int)fgbDriver.getRowCount());
            long batchSize = 0;
            connection.setAutoCommit(false);
            try(PreparedStatement pst = connection.prepareStatement(preparedStatementQuery.toString())) {
                for(long rowId=0; rowId < fgbDriver.getRowCount(); rowId++) {
                    for(int columnId = 0; columnId < columnCount; columnId++) {
                        pst.setObject(columnId+1, fgbDriver.getField(rowId, columnId));
                    }
                    pst.addBatch();
                    batchSize++;
                    rowCopyProgress.endStep();
                    if (batchSize >= BATCH_MAX_SIZE) {
                        pst.executeBatch();
                        connection.commit();
                        pst.clearBatch();
                        batchSize = 0;
                    }
                }
                if (batchSize > 0) {
                    pst.executeBatch();
                    connection.commit();
                    pst.clearBatch();
                }
            }
        }
        return null;
    }
}
