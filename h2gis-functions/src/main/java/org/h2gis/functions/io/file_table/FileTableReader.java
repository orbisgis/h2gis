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

package org.h2gis.functions.io.file_table;

import org.h2.command.ddl.CreateTableData;
import org.h2.table.Column;
import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2.value.DataType;
import org.h2.value.TypeInfo;
import org.h2.value.Value;
import org.h2gis.api.FileDriver;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.utilities.URIUtilities;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads a file through the driver of a {@link FileEngine} and returns its rows
 * as a result set, for the table functions FGBTable, SHPTable and DBFTable:
 *
 * <pre>
 * SELECT * FROM SHPTable('/data/building.shp', 'THE_GEOM, HEIGHT');
 * </pre>
 *
 * The columns come from the header of the file; only the selected columns are
 * read.
 *
 * @author Erwan Bocher, CNRS
 */
public final class FileTableReader {

    private FileTableReader() {
    }

    /**
     * Reads a file as a table.
     *
     * @param connection database connection
     * @param engine engine of the file format
     * @param fileName path of the file
     * @param extension expected extension of the file, without dot
     * @param columns names of the columns to read separated by commas, null or empty for all
     * @param encoding encoding of the file, null for the default one of the format
     * @param <D> driver of the engine
     * @return the rows of the file
     */
    public static <D extends FileDriver> ResultSet readTable(Connection connection, FileEngine<D> engine, String fileName,
                                                             String extension, String columns, String encoding) throws SQLException {
        File file = URIUtilities.fileFromString(fileName);
        if (!file.getName().toLowerCase().endsWith("." + extension)) {
            throw new SQLException("The file " + file.getName() + " must have the ." + extension + " extension");
        }
        if (!file.isFile()) {
            throw new SQLException("The file " + file + " doesn't exist ");
        }
        List<String> args = new ArrayList<>(Arrays.asList(file.getPath(), encoding));
        D driver = openDriver(engine, file, args);
        List<Column> fileColumns;
        try {
            fileColumns = getColumns(engine, driver);
        } catch (SQLException ex) {
            closeDriver(driver);
            throw ex;
        }
        List<String> names = new ArrayList<>();
        for (Column column : fileColumns) {
            names.add(column.getName());
        }
        int[] selection;
        try {
            selection = selectColumns(names, columns);
        } catch (SQLException ex) {
            closeDriver(driver);
            throw ex;
        }
        SimpleResultSet rs;
        if (TableUtilities.isColumnListConnection(connection)) {
            // H2 only wants the columns
            closeDriver(driver);
            rs = new SimpleResultSet();
        } else {
            rs = new SimpleResultSet(new DriverRowSource<>(engine, file, args, driver, selection));
        }
        for (int column : selection) {
            addColumn(rs, fileColumns.get(column));
        }
        return rs;
    }

    /**
     * Index of the columns to read.
     *
     * @param fileColumns the names of the columns of the file
     * @param columns the names of the columns to read separated by commas: case
     *                insensitive, case sensitive between double quotes; null or empty for all
     * @return the indexes in fileColumns of the columns to read, in the order of the list
     */
    public static int[] selectColumns(List<String> fileColumns, String columns) throws SQLException {
        if (columns == null || columns.trim().isEmpty()) {
            int[] all = new int[fileColumns.size()];
            for (int i = 0; i < all.length; i++) {
                all[i] = i;
            }
            return all;
        }
        String[] names = columns.split(",");
        int[] selection = new int[names.length];
        for (int i = 0; i < names.length; i++) {
            String columnName = names[i].trim();
            boolean quoted = columnName.length() > 1 && columnName.startsWith("\"") && columnName.endsWith("\"");
            if (quoted) {
                columnName = columnName.substring(1, columnName.length() - 1).replace("\"\"", "\"");
            }
            int index = -1;
            for (int j = 0; j < fileColumns.size() && index < 0; j++) {
                if (quoted ? fileColumns.get(j).equals(columnName) : fileColumns.get(j).equalsIgnoreCase(columnName)) {
                    index = j;
                }
            }
            if (index < 0) {
                throw new SQLException("Column \"" + columnName + "\" not found in the file, the columns are " + fileColumns);
            }
            for (int j = 0; j < i; j++) {
                if (selection[j] == index) {
                    throw new SQLException("Duplicate column \"" + columnName + "\"");
                }
            }
            selection[i] = index;
        }
        return selection;
    }

    private static <D extends FileDriver> D openDriver(FileEngine<D> engine, File file, List<String> args) throws SQLException {
        try {
            return engine.createDriver(file, args);
        } catch (IOException ex) {
            throw new SQLException("Cannot read the file " + file + ": " + ex.getMessage(), ex);
        }
    }

    private static <D extends FileDriver> List<Column> getColumns(FileEngine<D> engine, D driver) throws SQLException {
        CreateTableData data = new CreateTableData();
        data.columns = new ArrayList<>();
        try {
            engine.feedCreateTableData(driver, data);
        } catch (IOException ex) {
            throw new SQLException(ex);
        }
        return data.columns;
    }

    private static void closeDriver(FileDriver driver) {
        try {
            driver.close();
        } catch (IOException ex) {
            // only read, nothing to do
        }
    }

    private static void addColumn(SimpleResultSet rs, Column column) {
        TypeInfo type = column.getType();
        if (type.getValueType() == Value.GEOMETRY) {
            rs.addColumn(column.getName(), Types.OTHER, "GEOMETRY", 0, 0);
        } else if (type.getValueType() == Value.JSON) {
            rs.addColumn(column.getName(), Types.OTHER, "JSON", 0, 0);
        } else {
            rs.addColumn(column.getName(), DataType.convertTypeToSQLType(type),
                    (int) Math.min(type.getPrecision(), Integer.MAX_VALUE), type.getScale());
        }
    }

    /**
     * Rows of the result set, read from the driver when H2 asks for them.
     */
    private static final class DriverRowSource<D extends FileDriver> implements SimpleRowSource {
        private final FileEngine<D> engine;
        private final File file;
        private final List<String> args;
        private final int[] selection;
        private D driver;
        private long rowCount;
        private long row;

        private DriverRowSource(FileEngine<D> engine, File file, List<String> args, D driver, int[] selection) {
            this.engine = engine;
            this.file = file;
            this.args = args;
            this.driver = driver;
            this.selection = selection;
            this.rowCount = driver.getRowCount();
        }

        @Override
        public Object[] readRow() throws SQLException {
            if (driver == null) {
                // after reset()
                driver = openDriver(engine, file, args);
                rowCount = driver.getRowCount();
            }
            if (row >= rowCount) {
                close();
                return null;
            }
            Object[] values = new Object[selection.length];
            try {
                for (int i = 0; i < selection.length; i++) {
                    values[i] = driver.getField(row, selection[i]);
                }
            } catch (IOException | RuntimeException ex) {
                close();
                throw new SQLException("Cannot read the row " + (row + 1) + " of the file " + file, ex);
            }
            row++;
            return values;
        }

        @Override
        public void close() {
            if (driver != null) {
                closeDriver(driver);
                driver = null;
            }
        }

        @Override
        public void reset() {
            close();
            row = 0;
        }
    }
}