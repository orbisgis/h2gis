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

import org.h2.tools.Csv;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.DriverManager;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.h2gis.utilities.FileUtilities;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;

/**
 * Basic CSV importer and exporter
 * 
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class CSVDriverFunction implements DriverFunction{

    public static String DESCRIPTION = "CSV file (Comma Separated Values)";
    private static final int BATCH_MAX_SIZE = 200;
    private static final int AVERAGE_NODE_SIZE = 500;
    
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
    public String[] exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
             throws SQLException, IOException {
        return exportTable( connection,  tableReference,  fileName,  null,  false,  progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
            return exportTable( connection,  tableReference,  fileName,  null,  deleteFiles,  progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String csvOptions, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        progress = DriverManager.check(connection, tableReference,fileName,progress);
        if (!FileUtilities.isExtensionWellFormated(fileName, "csv")) {
            throw new SQLException("Only .csv extension is supported");
        }
        if(deleteFiles){
            Files.deleteIfExists(fileName.toPath());
        }
        else if(fileName.exists()){
            throw new IOException("The CSV file already exist.");
        }
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableReference);
        if (matcher.find()) {
            if (tableReference.startsWith("(") && tableReference.endsWith(")")) {
                try (Statement st = connection.createStatement()) {
                    JDBCUtilities.attachCancelResultSet(st, progress);
                    Csv csv = new Csv();
                    if (csvOptions != null && csvOptions.indexOf('=') >= 0) {
                        csv.setOptions(csvOptions);
                    }
                    csv.write(fileName.getPath(), st.executeQuery(tableReference), null);
                    return new String[]{tableReference};
                }
            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM ORDERS)'.");
            }

        } else {
            final DBTypes dbType = DBUtils.getDBType(connection);
            TableLocation requestedTable = TableLocation.parse(tableReference, dbType);
            String outputTable = requestedTable.toString();
   
            try (Statement st = connection.createStatement()) {
                JDBCUtilities.attachCancelResultSet(st, progress);
                Csv csv = new Csv();
                if (csvOptions != null && csvOptions.indexOf('=') >= 0) {
                    csv.setOptions(csvOptions);
                }
                csv.write(fileName.getPath(), st.executeQuery("SELECT * FROM " + outputTable), null);
                return new String[]{outputTable};
            }
        }
    }

    /**
     * Export a table or a query to a CSV file
     * 
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @param csvOptions  the CSV options ie "charset=UTF-8 fieldSeparator=| fieldDelimiter=,"
     * @param progress
     * @throws SQLException
     * @throws IOException 
     */
    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String csvOptions, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable( connection,  tableReference,  fileName,  csvOptions,  false,  progress);
    }
    
    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, null, false,progress);
    }

    /**
     * 
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @param csvOptions  the CSV options ie "charset=UTF-8 fieldSeparator=| fieldDelimiter=,"
     * @param progress
     * @throws SQLException
     * @throws IOException 
     */
    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName,
                           String csvOptions, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, csvOptions, false,progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName,
                           boolean deleteTables,ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, null, deleteTables,progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String csvOptions, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        progress = DriverManager.check(connection,tableReference,fileName,progress);
        if (FileUtilities.isFileImportable(fileName, "csv")) {
            final DBTypes dbType = DBUtils.getDBType(connection);
            if(deleteTables) {
                TableLocation requestedTable = TableLocation.parse(tableReference, dbType);
                Statement stmt = connection.createStatement();
                stmt.execute("DROP TABLE IF EXISTS " + requestedTable);
                stmt.close();
            }
            TableLocation requestedTable = TableLocation.parse(tableReference, dbType);
            String outputTable = requestedTable.getTable();
            FileInputStream fis = new FileInputStream(fileName);
            FileChannel fc = fis.getChannel();
            long fileSize = fc.size();
            // Given the file size and an average node file size.
            // Skip how many nodes in order to update progression at a step of 1%
            long readFileSizeEachNode = Math.max(1, (fileSize / AVERAGE_NODE_SIZE) / 100);
            int average_row_size = 0;
            connection.setAutoCommit(false);
            Csv csv = new Csv();
            if (csvOptions != null && csvOptions.indexOf('=') >= 0) {
                csv.setOptions(csvOptions);
            }
            ResultSet reader = csv.read(new BufferedReader(new InputStreamReader(fis)), null);
            ResultSetMetaData metadata = reader.getMetaData();
            int columnCount = metadata.getColumnCount();

            StringBuilder createTable = new StringBuilder("CREATE TABLE ");
            createTable.append(outputTable).append("(");

            StringBuilder insertTable = new StringBuilder("INSERT INTO ");
            insertTable.append(outputTable).append(" VALUES(");

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
                        connection.commit();
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
                    pst.clearBatch();
                    connection.commit();
                }

            } finally {
                pst.close();
                connection.setAutoCommit(true);
            }
            return new String[]{outputTable};
        }
        return null;
    }
}
