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

package org.h2gis.functions.io.geojson;

import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2.value.ValueBigint;
import org.h2.value.ValueBoolean;
import org.h2.value.ValueDouble;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueInteger;
import org.h2.value.ValueJson;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.functions.io.file_table.FileTableReader;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.utilities.URIUtilities;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a GeoJSON file as a table, without importing it, like CSVREAD:
 *
 * <pre>
 * SELECT * FROM GeoJsonTable('/data/building.geojson') WHERE HEIGHT_WALL  &gt; 10;
 * SELECT AVG(HEIGHT_WALL) FROM GeoJsonTable('/data/building.geojson', 'HEIGHT_WALL');
 * </pre>
 *
 * The optional list of columns makes the reading faster: the geometry and the
 * properties that are not in the list are skipped.
 *
 * The columns are found with a first pass over the file and kept in a cache
 * (key: path, size and date of the file), the features are then read one by
 * one at each query. While reading the features, the columns are found again
 * and compared with the cached ones: if the file has been modified the query
 * fails and the cache entry is removed, a result never mixes two versions of
 * a file.
 *
 * @author Erwan Bocher, CNRS
 */
public class GeoJsonTable extends AbstractFunction implements ScalarFunction {

    private static final int COLUMNS_CACHE_SIZE = 16;

    /** Columns of the files read recently: H2 asks for them several times per query */
    private static final Map<String, GeoJsonReaderDriver> COLUMNS_CACHE =
            new LinkedHashMap<String, GeoJsonReaderDriver>(COLUMNS_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, GeoJsonReaderDriver> eldest) {
                    return size() > COLUMNS_CACHE_SIZE;
                }
            };

    public GeoJsonTable() {
        addProperty(PROP_REMARKS, "Read a GeoJSON file (.geojson, .json or .gz) as a table, without importing it.\n"
                + "SELECT * FROM GeoJsonTable('/data/file.geojson');\n"
                + "An optional second argument lists the columns to read, faster than reading all of them:\n"
                + "SELECT * FROM GeoJsonTable('/data/file.geojson', 'THE_GEOM, HEIGHT');\n"
                + "The file must be encoded in UTF-8, as required by the GeoJSON specification (RFC 7946).");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readTable";
    }

    /**
     * Reads a GeoJSON file as a table.
     *
     * @param connection database connection
     * @param fileName path of the file
     * @return the features of the file, the geometry in the THE_GEOM column
     */
    public static ResultSet readTable(Connection connection, String fileName) throws SQLException, IOException {
        return readTable(connection, fileName, null);
    }

    /**
     * Reads some columns of a GeoJSON file as a table.
     *
     * @param connection database connection
     * @param fileName path of the file
     * @param columns names of the columns to read, separated by commas (THE_GEOM for
     *                the geometry), null or empty for all the columns
     * @return the columns of the features, in the order of the list
     */
    public static ResultSet readTable(Connection connection, String fileName, String columns) throws SQLException, IOException {
        File file = URIUtilities.fileFromString(fileName);
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".geojson") && !name.endsWith(".json") && !name.endsWith(".gz")) {
            throw new SQLException("GeoJsonTable supports only geojson, json or gz extensions");
        }
        if (!file.isFile()) {
            throw new SQLException("The file " + file + " doesn't exist ");
        }
        GeoJsonReaderDriver fileColumns = getColumns(file);
        int[] selection = FileTableReader.selectColumns(fileColumns.getColumnNames(), columns);
        if (TableUtilities.isColumnListConnection(connection)) {
            // H2 only wants the columns
            SimpleResultSet rs = new SimpleResultSet();
            addColumns(rs, fileColumns, selection);
            return rs;
        }
        GeoJsonReaderDriver driver = new GeoJsonReaderDriver(fileColumns);
        if (columns != null && !columns.trim().isEmpty()) {
            driver.selectColumns(selection);
        }
        SimpleResultSet rs = new SimpleResultSet(new FeatureRowSource(driver, selection, cacheKey(file)));
        addColumns(rs, fileColumns, selection);
        return rs;
    }

    private static String cacheKey(File file) {
        return file.getAbsolutePath() + '\n' + file.length() + '\n' + file.lastModified();
    }

    private static GeoJsonReaderDriver getColumns(File file) throws SQLException, IOException {
        String key = cacheKey(file);
        synchronized (COLUMNS_CACHE) {
            GeoJsonReaderDriver columns = COLUMNS_CACHE.get(key);
            if (columns != null) {
                return columns;
            }
        }
        GeoJsonReaderDriver columns = new GeoJsonReaderDriver(file, null);
        columns.readColumns();
        synchronized (COLUMNS_CACHE) {
            COLUMNS_CACHE.put(key, columns);
        }
        return columns;
    }

    /**
     * @param columns driver with the columns of the file
     * @param selection indexes of the columns to add, 0 for the geometry
     */
    private static void addColumns(SimpleResultSet rs, GeoJsonReaderDriver columns, int[] selection) {
        List<String> names = columns.getColumnNames();
        int[] types = columns.getColumnTypes();
        for (int column : selection) {
            String columnName = names.get(column);
            if (column == 0) {
                rs.addColumn(columnName, Types.OTHER, "GEOMETRY", 0, 0);
                continue;
            }
            switch (types[column - 1]) {
                case Types.BOOLEAN:
                    rs.addColumn(columnName, Types.BOOLEAN, 1, 0);
                    break;
                case Types.DOUBLE:
                    rs.addColumn(columnName, Types.DOUBLE, 17, 0);
                    break;
                case Types.BIGINT:
                    rs.addColumn(columnName, Types.BIGINT, 19, 0);
                    break;
                case Types.ARRAY:
                    rs.addColumn(columnName, Types.OTHER, "JSON", 0, 0);
                    break;
                default:
                    rs.addColumn(columnName, Types.VARCHAR, Integer.MAX_VALUE, 0);
                    break;
            }
        }
    }

    /**
     * Rows of the result set, read from the file when H2 asks for them.
     */
    private static final class FeatureRowSource implements SimpleRowSource {
        private final GeoJsonReaderDriver driver;
        private final int[] types;
        private final int[] selection;
        private final String cacheKey;
        private GeoJsonReaderDriver.FeatureReader reader;

        private FeatureRowSource(GeoJsonReaderDriver driver, int[] selection, String cacheKey) {
            this.driver = driver;
            this.selection = selection;
            this.cacheKey = cacheKey;
            this.types = driver.getColumnTypes();
        }

        @Override
        public Object[] readRow() throws SQLException {
            try {
                if (reader == null) {
                    reader = driver.openFeatures();
                }
                Object[] feature = reader.next();
                if (feature == null) {
                    close();
                    return null;
                }
                Object[] values = new Object[selection.length];
                for (int i = 0; i < selection.length; i++) {
                    int column = selection[i];
                    Object value = feature[column];
                    if (value == null) {
                        continue;
                    }
                    if (column == 0) {
                        value = ValueGeometry.getFromEWKB((byte[]) value);
                    } else if (types[column - 1] == Types.ARRAY) {
                        value = ValueJson.fromJson((String) value);
                    } else if (types[column - 1] == Types.BIGINT && value instanceof Integer) {
                        value = ((Integer) value).longValue();
                    } else if (types[column - 1] == Types.DOUBLE && value instanceof Number) {
                        // integers in a column of decimal numbers
                        value = ((Number) value).doubleValue();
                    } else if (types[column - 1] == Types.VARCHAR && !(value instanceof String)) {
                        // numbers or booleans in a column of mixed values: same text as GeoJsonRead
                        value = toText(value);
                    }
                    values[i] = value;
                }
                return values;
            } catch (IOException | SQLException ex) {
                // modified or malformed file: the columns will be read again by the next query
                close();
                synchronized (COLUMNS_CACHE) {
                    COLUMNS_CACHE.remove(cacheKey);
                }
                throw ex instanceof SQLException ? (SQLException) ex : new SQLException(ex);
            }
        }

        private static String toText(Object value) {
            if (value instanceof Boolean) {
                return ValueBoolean.get((Boolean) value).getString();
            } else if (value instanceof Double) {
                return ValueDouble.get((Double) value).getString();
            } else if (value instanceof Integer) {
                return ValueInteger.get((Integer) value).getString();
            } else if (value instanceof Long) {
                return ValueBigint.get((Long) value).getString();
            }
            return value.toString();
        }

        @Override
        public void close() {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    // the file has been read, nothing to do
                }
                reader = null;
            }
        }

        @Override
        public void reset() {
            // the next row will be the first one again
            close();
        }
    }
}