/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.io.tsv;

import org.h2.tools.Csv;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.utility.FileUtil;
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
    private static final int BATCH_MAX_SIZE = 200;

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[]{"tsv"};
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"tsv"};
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
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        exportTable(connection, tableReference, fileName, null, false, progress);
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        exportTable(connection, tableReference, fileName, null, deleteFiles, progress);
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, String encoding, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableReference);
        if (matcher.find()) {
            if (tableReference.startsWith("(") && tableReference.endsWith(")")) {
                if (FileUtil.isExtensionWellFormated(fileName, "tsv")) {
                    if (deleteFiles) {
                        Files.deleteIfExists(fileName.toPath());
                    }
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)))) {
                        try (Statement st = connection.createStatement()) {
                            JDBCUtilities.attachCancelResultSet(st, progress);
                            exportFromResultSet(connection, st.executeQuery(tableReference), bw, encoding, new EmptyProgressVisitor());
                        }
                    }
                } else if (FileUtil.isExtensionWellFormated(fileName, "gz")) {
                    if (deleteFiles) {
                        Files.deleteIfExists(fileName.toPath());
                    }
                    final boolean isH2 = JDBCUtilities.isH2DataBase(connection);
                    TableLocation location = TableLocation.parse(tableReference, isH2);
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                            new GZIPOutputStream(new FileOutputStream(fileName))))) {
                        try (Statement st = connection.createStatement()) {
                            JDBCUtilities.attachCancelResultSet(st, progress);
                            exportFromResultSet(connection, st.executeQuery(tableReference), bw, encoding, new EmptyProgressVisitor());
                        }
                    }
                } else if (FileUtil.isExtensionWellFormated(fileName, "zip")) {
                    if (deleteFiles) {
                        Files.deleteIfExists(fileName.toPath());
                    }
                    final boolean isH2 = JDBCUtilities.isH2DataBase(connection);
                    TableLocation location = TableLocation.parse(tableReference, isH2);
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                            new ZipOutputStream(new FileOutputStream(fileName))))) {
                        try (Statement st = connection.createStatement()) {
                            JDBCUtilities.attachCancelResultSet(st, progress);
                            exportFromResultSet(connection, st.executeQuery(location.toString(isH2)), bw, encoding, new EmptyProgressVisitor());
                        }
                    }
                } else {
                    throw new SQLException("Only .tsv, .gz or .zip extensions are supported");
                }

            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM ORDERS)'.");
            }
        } else {
            if (FileUtil.isExtensionWellFormated(fileName, "tsv")) {
                if (deleteFiles) {
                    Files.deleteIfExists(fileName.toPath());
                }
                final boolean isH2 = JDBCUtilities.isH2DataBase(connection);
                TableLocation location = TableLocation.parse(tableReference, isH2);
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)))) {
                    try (Statement st = connection.createStatement()) {
                        JDBCUtilities.attachCancelResultSet(st, progress);
                        exportFromResultSet(connection, st.executeQuery("SELECT * FROM " + location.toString(isH2)), bw, encoding, new EmptyProgressVisitor());
                    }
                }
            } else if (FileUtil.isExtensionWellFormated(fileName, "gz")) {
                if (deleteFiles) {
                    Files.deleteIfExists(fileName.toPath());
                }
                final boolean isH2 = JDBCUtilities.isH2DataBase(connection);
                TableLocation location = TableLocation.parse(tableReference, isH2);
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new GZIPOutputStream(new FileOutputStream(fileName))))) {
                    try (Statement st = connection.createStatement()) {
                        JDBCUtilities.attachCancelResultSet(st, progress);
                        exportFromResultSet(connection, st.executeQuery("SELECT * FROM " + location.toString(isH2)), bw, encoding, new EmptyProgressVisitor());
                    }
                }
            } else if (FileUtil.isExtensionWellFormated(fileName, "zip")) {
                if (deleteFiles) {
                    Files.deleteIfExists(fileName.toPath());
                }
                final boolean isH2 = JDBCUtilities.isH2DataBase(connection);
                TableLocation location = TableLocation.parse(tableReference, isH2);
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new ZipOutputStream(new FileOutputStream(fileName))))) {
                    try (Statement st = connection.createStatement()) {
                        JDBCUtilities.attachCancelResultSet(st, progress);
                        exportFromResultSet(connection, st.executeQuery("SELECT * FROM " + location.toString(isH2)), bw, encoding, new EmptyProgressVisitor());
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
     * @param progress
     * @param encoding
     * @throws SQLException
     * @throws IOException
     */
    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, String encoding, ProgressVisitor progress) throws SQLException, IOException {
        exportTable(connection, tableReference, fileName, encoding, false, progress);
    }

    /**
     * Export a resultset to a TSV file
     *
     * @param connection
     * @param res
     * @param writer
     * @param progress
     * @param encoding
     * @throws java.sql.SQLException
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
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        importFile(connection, tableReference, fileName, null, false, progress);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName,
            String options, ProgressVisitor progress) throws SQLException, IOException {
        importFile(connection, tableReference, fileName, options, false, progress);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName,
            boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        importFile(connection, tableReference, fileName, null, deleteTables, progress);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, String options, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection);
        TableLocation requestedTable = TableLocation.parse(tableReference, isH2);
        if (fileName != null && fileName.getName().toLowerCase().endsWith(".tsv")) {
            if (!fileName.exists()) {
                throw new SQLException("The file " + requestedTable + " doesn't exist ");
            }
            if (deleteTables) {
                Statement stmt = connection.createStatement();
                stmt.execute("DROP TABLE IF EXISTS " + requestedTable);
                stmt.close();
            }
            String table = requestedTable.toString(isH2);

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
                String table = requestedTable.toString(isH2);
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
                    }
                    if (batchSize > 0) {
                        pst.executeBatch();
                    }
                } finally {
                    pst.close();
                }
            }
        } else {
            throw new SQLException("The TSV read driver supports only tsv or gz extensions");
        }
    }
}
