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

package org.h2gis.functions.io.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.tools.Csv;
import org.h2gis.functions.io.utility.FileUtil;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;

/**
 * Basic CSV importer and exporter
 * 
 * @author Erwan Bocher
 */
public class CSVDriverFunction implements DriverFunction{

    public static String DESCRIPTION = "CSV file (Comma Separated Values)";
    private static final int BATCH_MAX_SIZE = 100;
    
    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[]{"csv"};
    }

    @Override
    public String[] getExportFormats() {
      return new String[]{"csv"};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("csv")) {
            return DESCRIPTION;
        } else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return false;
    }
    
     @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
         exportTable(connection, tableReference, fileName, progress, null);
    }

    /**
     * 
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @param progress
     * @param csvOptions  the CSV options ie "charset=UTF-8 fieldSeparator=| fieldDelimiter=,"
     * @throws SQLException
     * @throws IOException 
     */
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress, String csvOptions) throws SQLException, IOException {
        if(FileUtil.isExtensionWellFormated(fileName, "csv")){
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        TableLocation location = TableLocation.parse(tableReference, isH2);
        try (Statement st = connection.createStatement()) {
            JDBCUtilities.attachCancelResultSet(st, progress);
            Csv csv = new Csv();
            if (csvOptions != null && csvOptions.indexOf('=') >= 0) {
                csv.setOptions(csvOptions);
            }  
            csv.write(fileName.getPath(), st.executeQuery("SELECT * FROM " + location.toString()), null);
        }
        }
        else{
            throw new SQLException("Only .csv extension is supported");
        }
        
    }
    
    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        importFile(connection, tableReference, fileName, progress, null);
    }

    /**
     * 
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @param progress
     * @param csvOptions  the CSV options ie "charset=UTF-8 fieldSeparator=| fieldDelimiter=,"
     * @throws SQLException
     * @throws IOException 
     */
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress, String csvOptions) throws SQLException, IOException {
        if (FileUtil.isFileImportable(fileName, "csv")) {
            final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
            TableLocation requestedTable = TableLocation.parse(tableReference, isH2);
            String table = requestedTable.getTable();
            int AVERAGE_NODE_SIZE = 500;
            FileInputStream fis = new FileInputStream(fileName);
            FileChannel fc = fis.getChannel();
            long fileSize = fc.size();
            // Given the file size and an average node file size.
            // Skip how many nodes in order to update progression at a step of 1%
            long readFileSizeEachNode = Math.max(1, (fileSize / AVERAGE_NODE_SIZE) / 100);            
            int average_row_size = 0;
            Csv csv = new Csv();
            if (csvOptions != null && csvOptions.indexOf('=') >= 0) {
                csv.setOptions(csvOptions);
            }            
            ResultSet reader = csv.read(new BufferedReader(new InputStreamReader(fis)), null);
            ResultSetMetaData metadata = reader.getMetaData();
            int columnCount = metadata.getColumnCount();

            StringBuilder createTable = new StringBuilder("CREATE TABLE ");
            createTable.append(table).append("(");

            StringBuilder insertTable = new StringBuilder("INSERT INTO ");
            insertTable.append(table).append(" VALUES(");

            for (int i = 0; i < columnCount; i++) {
                if(i>0){
                    createTable.append(",");
                    insertTable.append(",");
                }
                createTable.append(metadata.getColumnName(i + 1)).append(" VARCHAR");
                insertTable.append("?");
            }
            createTable.append(")");
            insertTable.append(")");

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTable.toString());
            }

            PreparedStatement pst = connection.prepareStatement(insertTable.toString());
            long batchSize = 0;
            try {
                while (reader.next()) {
                    if (progress.isCanceled()) {
                        throw new SQLException("Canceled by user");
                    }
                    
                    for (int i = 0; i < columnCount; i++) {
                        pst.setString(i + 1, reader.getString(i + 1));
                    }
                    pst.addBatch();
                    batchSize++;
                    if (batchSize >= BATCH_MAX_SIZE) {
                        pst.executeBatch();
                        pst.clearBatch();
                        batchSize = 0;
                    }
                    if (average_row_size++ % readFileSizeEachNode == 0) {
                        // Update Progress
                        try {
                            progress.setStep((int) (((double) fc.position() / fileSize) * 100));
                        } catch (IOException ex) {
                            // Ignore
                        }
                    }
                }
                if (batchSize > 0) {
                    pst.executeBatch();
                }                

            } finally {
                pst.close();
            }
        }
    }    
}
