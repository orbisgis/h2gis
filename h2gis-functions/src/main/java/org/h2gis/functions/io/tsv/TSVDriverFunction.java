/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.io.tsv;

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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;
import org.h2gis.utilities.FileUtilities;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;

/**
 * This driver allow to import and export the Tab Separated Values (TSV): a
 * format for tabular data exchange
 *
 * A file in TSV format consists of lines. Each line contain fields separated
 * from each other by TAB characters (horizontal tab, HT, Ascii control code 9).
 *
 * "Field" means here just any string of characters, excluding TABs. The point
 * is simply that TABs divide a line into pieces, components.
 *
 * Each line must contain the same number of fields.
 *
 * The first line contains the names for the fields (on all lines), i.e. column
 * headers.
 *
 * Please read : http://www.cs.tut.fi/~jkorpela/TSV.html
 *
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class TSVDriverFunction implements DriverFunction {

    public static String DESCRIPTION = "TSV file (Tab Separated Values)";
    private static final int BATCH_MAX_SIZE = 100;

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[]{"tsv","tsv.gz"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"tsv","tsv.gz"};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("tsv")) {
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
    public String[] exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, null, false, progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, null, deleteFiles, progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String encoding, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        progress =DriverManager.check(connection,tableReference, fileName, progress);
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableReference);
        if (matcher.find()) {
            if (tableReference.startsWith("(") && tableReference.endsWith(")")) {
                if (FileUtilities.isExtensionWellFormated(fileName, "tsv")) {
                    if (deleteFiles) {
                        Files.deleteIfExists(fileName.toPath());
                    } else if (fileName.exists()) {
                        throw new IOException("The tsv file already exist.");
                    }
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)))) {
                        try (Statement st = connection.createStatement()) {
                            JDBCUtilities.attachCancelResultSet(st, progress);
                            exportFromResultSet(connection, st.executeQuery(tableReference), bw, encoding, progress);
                            return new String[]{fileName.getAbsolutePath()};
                        }
                    }
                } else if (FileUtilities.isExtensionWellFormated(fileName, "gz")) {
                    if (deleteFiles) {
                        Files.deleteIfExists(fileName.toPath());
                    } else if (fileName.exists()) {
                        throw new IOException("The gz file already exist.");
                    }
                    final DBTypes dbType = DBUtils.getDBType(connection);
                    TableLocation location = TableLocation.parse(tableReference, dbType);
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                            new GZIPOutputStream(new FileOutputStream(fileName))))) {
                        try (Statement st = connection.createStatement()) {
                            JDBCUtilities.attachCancelResultSet(st, progress);
                            exportFromResultSet(connection, st.executeQuery(location.toString()), bw, encoding, progress);
                            return new String[]{fileName.getAbsolutePath()};

                        }
                    }
                } else if (FileUtilities.isExtensionWellFormated(fileName, "zip")) {
                    if (deleteFiles) {
                        Files.deleteIfExists(fileName.toPath());
                    }
                    else if (fileName.exists()) {
                        throw new IOException("The zip file already exist.");
                    }
                    final DBTypes dbType = DBUtils.getDBType(connection);
                    TableLocation location = TableLocation.parse(tableReference, dbType);
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                            new ZipOutputStream(new FileOutputStream(fileName))))) {
                        try (Statement st = connection.createStatement()) {
                            JDBCUtilities.attachCancelResultSet(st, progress);
                            exportFromResultSet(connection, st.executeQuery(location.toString()), bw, encoding, progress);
                            return new String[]{fileName.getAbsolutePath()};

                        }
                    }
                } else {
                    throw new SQLException("Only .tsv, .gz or .zip extensions are supported");
                }

            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM ORDERS)'.");
            }
        } else {
            if (FileUtilities.isExtensionWellFormated(fileName, "tsv")) {
                if (deleteFiles) {
                    Files.deleteIfExists(fileName.toPath());
                } else if (fileName.exists()) {
                    throw new IOException("The tsv file already exist.");
                }
                final DBTypes dbType = DBUtils.getDBType(connection);
                TableLocation location = TableLocation.parse(tableReference, dbType);
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)))) {
                    try (Statement st = connection.createStatement()) {
                        JDBCUtilities.attachCancelResultSet(st, progress);
                        exportFromResultSet(connection, st.executeQuery("SELECT * FROM " + location.toString()), bw, encoding, progress);
                        return new String[]{fileName.getAbsolutePath()};

                    }
                }
            } else if (FileUtilities.isExtensionWellFormated(fileName, "gz")) {
                if (deleteFiles) {
                    Files.deleteIfExists(fileName.toPath());
                }else if (fileName.exists()) {
                    throw new IOException("The gz file already exist.");
                }
                final DBTypes dbType = DBUtils.getDBType(connection);
                TableLocation location = TableLocation.parse(tableReference, dbType);
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new GZIPOutputStream(new FileOutputStream(fileName))))) {
                    try (Statement st = connection.createStatement()) {
                        JDBCUtilities.attachCancelResultSet(st, progress);
                        exportFromResultSet(connection, st.executeQuery("SELECT * FROM " + location.toString()), bw, encoding, progress);
                        return new String[]{fileName.getAbsolutePath()};
                    }
                }
            } else if (FileUtilities.isExtensionWellFormated(fileName, "zip")) {
                if (deleteFiles) {
                    Files.deleteIfExists(fileName.toPath());
                }else if (fileName.exists()) {
                    throw new IOException("The zip file already exist.");
                }
                final DBTypes dbType = DBUtils.getDBType(connection);
                TableLocation location = TableLocation.parse(tableReference, dbType);
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new ZipOutputStream(new FileOutputStream(fileName))))) {
                    try (Statement st = connection.createStatement()) {
                        JDBCUtilities.attachCancelResultSet(st, progress);
                        exportFromResultSet(connection, st.executeQuery("SELECT * FROM " + location.toString()), bw, encoding, progress);
                        return new String[]{fileName.getAbsolutePath()};
                    }
                }
            } else {
                throw new SQLException("Only .tsv, .gz or .zip extensions are supported");
            }
        }
    }

    /**
     * Export a table or a query to as TSV file
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference [[catalog.]schema.]table reference
     * @param fileName File path to read
     * @param progress Progress visitor following the execution.
     * @param encoding chartset encoding
     */
    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String encoding, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, encoding, false, progress);
    }

    /**
     * Export a resultset to a TSV file
     *
     * @param connection database
     * @param res {@link ResultSet}
     * @param writer {@link Writer}
     * @param progress Progress visitor following the execution.
     * @param encoding chartset encoding
     */
    public void exportFromResultSet(Connection connection, ResultSet res, Writer writer, String encoding, ProgressVisitor progress) throws SQLException {
        Csv csv = new Csv();
        String csvOptions = "charset=UTF-8 fieldSeparator=\t fieldDelimiter=\t";
        if (encoding != null) {
            csvOptions = String.format("charset=%s fieldSeparator=\t fieldDelimiter=\t", encoding);
        }
        csv.setOptions(csvOptions);
        csv.write(writer, res);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, null, false, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName,
            String options, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, options, false, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName,
            boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, null, deleteTables, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String options, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        progress = DriverManager.check(connection,tableReference, fileName,progress);
        final DBTypes dbType = DBUtils.getDBType(connection);
        TableLocation requestedTable = TableLocation.parse(tableReference, dbType);
        if (fileName != null && fileName.getName().toLowerCase().endsWith(".tsv")) {
            if (!fileName.exists()) {
                throw new SQLException("The file " + requestedTable + " doesn't exist ");
            }
            if (deleteTables) {
                Statement stmt = connection.createStatement();
                stmt.execute("DROP TABLE IF EXISTS " + requestedTable);
                stmt.close();
            }
            String table = requestedTable.toString();

            int AVERAGE_NODE_SIZE = 500;
            FileInputStream fis = new FileInputStream(fileName);
            FileChannel fc = fis.getChannel();
            long fileSize = fc.size();
            // Given the file size and an average node file size.
            // Skip how many nodes in order to update progression at a step of 1%
            long readFileSizeEachNode = Math.max(1, (fileSize / AVERAGE_NODE_SIZE) / 100);
            int average_row_size = 0;

            Csv csv = new Csv();
            csv.setFieldDelimiter('\t');
            csv.setFieldSeparatorRead('\t');
            ResultSet reader = csv.read(new BufferedReader(new InputStreamReader(fis)), null);
            ResultSetMetaData metadata = reader.getMetaData();
            int columnCount = metadata.getColumnCount();

            StringBuilder createTable = new StringBuilder("CREATE TABLE ");
            createTable.append(table).append("(");

            StringBuilder insertTable = new StringBuilder("INSERT INTO ");
            insertTable.append(table).append(" VALUES(");
            for (int i = 0; i < columnCount; i++) {
                if (i > 0) {
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

            connection.setAutoCommit(false);
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
                    connection.commit();
                }
                connection.setAutoCommit(true);
                return new String[]{table};
            } finally {
                pst.close();
            }
        } else if (fileName != null && fileName.getName().toLowerCase().endsWith(".gz")) {
            if (!fileName.exists()) {
                throw new SQLException("The file " + requestedTable + " doesn't exist ");
            }
            if (deleteTables) {
                Statement stmt = connection.createStatement();
                stmt.execute("DROP TABLE IF EXISTS " + requestedTable);
                stmt.close();
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new FileInputStream(fileName))))) {
                String table = requestedTable.toString();
                Csv csv = new Csv();
                csv.setFieldDelimiter('\t');
                csv.setFieldSeparatorRead('\t');
                ResultSet reader = csv.read(br, null);
                ResultSetMetaData metadata = reader.getMetaData();
                int columnCount = metadata.getColumnCount();
                StringBuilder createTable = new StringBuilder("CREATE TABLE ");
                createTable.append(table).append("(");

                StringBuilder insertTable = new StringBuilder("INSERT INTO ");
                insertTable.append(table).append(" VALUES(");
                for (int i = 0; i < columnCount; i++) {
                    if (i > 0) {
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

                connection.setAutoCommit(false);
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
                    }
                    if (batchSize > 0) {
                        pst.executeBatch();
                        connection.commit();
                    }
                    return new String[]{table};
                } finally {
                    connection.setAutoCommit(true);
                    pst.close();
                }
            }
        } else {
            throw new SQLException("The TSV read driver supports only tsv or gz extensions");
        }
    }
}
