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
package org.h2gis.functions.io.utility;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.functions.io.asc.AscDriverFunction;
import org.h2gis.functions.io.csv.CSVDriverFunction;
import org.h2gis.functions.io.dbf.DBFDriverFunction;
import org.h2gis.functions.io.geojson.GeoJsonDriverFunction;
import org.h2gis.functions.io.gpx.GPXDriverFunction;
import org.h2gis.functions.io.json.JsonDriverFunction;
import org.h2gis.functions.io.kml.KMLDriverFunction;
import org.h2gis.functions.io.osm.OSMDriverFunction;
import org.h2gis.functions.io.shp.SHPDriverFunction;
import org.h2gis.functions.io.tsv.TSVDriverFunction;
import org.h2gis.utilities.FileUtilities;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.URIUtilities;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.Geometry;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods to :
 *
 *
 * - import, export a file in a database (H2GIS, POSTGIS) - export a table from
 * another database (H2GIS, POSTGIS) - link a file or table (H2GIS only)
 *
 *
 * @author Erwan Bocher, CNRS, 2020
 * @author Sylvain PALOMINOS (UBS 2019)
 * @author Nicolas Fortin (Univ. Gustave Eiffel 2020)
 */
public class IOMethods {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOMethods.class);
    private List<DriverFunction> driverFunctionList = new ArrayList<>();

    private static final String ENCODING_OPTION = "charset=";
    private static final String UTF_ENCODING = "UTF-8";

    /**
     * Create a new instance of IOMethods in order to be able to use custom file
     * drivers Add built-in supported drivers
     */
    public IOMethods() {
        driverFunctionList.add(new CSVDriverFunction());
        driverFunctionList.add(new DBFDriverFunction());
        driverFunctionList.add(new GeoJsonDriverFunction());
        driverFunctionList.add(new GPXDriverFunction());
        driverFunctionList.add(new JsonDriverFunction());
        driverFunctionList.add(new KMLDriverFunction());
        driverFunctionList.add(new OSMDriverFunction());
        driverFunctionList.add(new SHPDriverFunction());
        driverFunctionList.add(new TSVDriverFunction());
        driverFunctionList.add(new AscDriverFunction());
    }

    /**
     * @return Current list of supported drivers
     */
    public List<DriverFunction> getDriverFunctionList() {
        return Collections.unmodifiableList(driverFunctionList);
    }

    /**
     * @param driver Driver to add to supported file drivers
     */
    public void addDriver(DriverFunction driver) {
        driverFunctionList.add(driver);
    }

    /**
     * @param driver Driver to remove from the list of supported drivers
     */
    public void removeDriver(DriverFunction driver) {
        driverFunctionList.remove(driver);
    }

    /**
     * @return Collect all supported file extensions
     */
    public List<String> getAllExportDriverSupportedExtensions() {
        List<String> extensions = new ArrayList<>();
        for (DriverFunction f : driverFunctionList) {
            extensions.addAll(Arrays.asList(f.getExportFormats()));
        }
        return extensions;
    }

    /**
     * @return Collect all supported file extensions
     */
    public List<String> getAllImportDriverSupportedExtensions() {
        List<String> extensions = new ArrayList<>();
        for (DriverFunction f : driverFunctionList) {
            extensions.addAll(Arrays.asList(f.getImportFormats()));
        }
        return extensions;
    }

    /**
     * @param file File path
     * @return First compatible driver
     */
    public DriverFunction getExportDriverFromFile(File file) {
        String path = file.getAbsolutePath();
        for (DriverFunction f : driverFunctionList) {
            for (String ext : f.getExportFormats()) {
                if (path.endsWith("." + ext)) {
                    return f;
                }
            }
        }
        LOGGER.error("Unsupported file format.\n"
                + "Supported formats are : [" + String.join(",", getAllExportDriverSupportedExtensions()) + "].");
        return null;
    }

    /**
     * @param file File path
     * @return First compatible driver
     */
    public DriverFunction getImportDriverFromFile(File file) {
        String path = file.getAbsolutePath();
        for (DriverFunction f : driverFunctionList) {
            for (String ext : f.getImportFormats()) {
                if (path.endsWith(ext)) {
                    return f;
                }
            }
        }
        LOGGER.error("Unsupported file format.\n"
                + "Supported formats are : [" + String.join(",", getAllImportDriverSupportedExtensions()) + "].");
        return null;
    }

    /**
     * Export a table to a file
     *
     * @param connection The connection to database
     * @param tableName Name of the table to save.
     * @param filePath Path of the destination file.
     * @param encoding Encoding of the file. Can be null
     * @param deleteFile true to delete the file if exists
     * @throws java.sql.SQLException
     * @return The absolute path of the exported files
     */
    public String[] exportToFile(Connection connection, String tableName,
            String filePath, String encoding, boolean deleteFile) throws SQLException {
        String enc = encoding;
        File fileToSave = URIUtilities.fileFromString(filePath);
        DriverFunction driverFunction = getExportDriverFromFile(fileToSave);
        if (driverFunction == null) {
            throw new SQLException("Cannot find any file driver for the file." + filePath);
        }
        try {
            if (FileUtilities.isExtensionWellFormated(fileToSave, "csv")) {
                if (enc == null) {
                    enc = ENCODING_OPTION + UTF_ENCODING;
                }
            }
            return driverFunction.exportTable(connection, tableName, fileToSave,
                    enc, deleteFile, new EmptyProgressVisitor());

        } catch (SQLException | IOException e) {
            throw new SQLException("Cannot save the table.\n", e);
        }
    }

    /**
     * Import a file to a database
     *
     * @param connection The connection to database
     * @param filePath The path of the file
     * @param tableName The name of the table created to store the file
     * @param encoding An encoding value to read the file. Can be null
     * @param deleteTable True to delete the table if exists
     * @throws java.sql.SQLException
     * @return the name of table imported and formated according the database rules
     *
     */
    public String[] importFile(Connection connection, String filePath, String tableName, String encoding,
            boolean deleteTable) throws SQLException {
        File fileToImport = URIUtilities.fileFromString(filePath);
        DriverFunction driverFunction = getImportDriverFromFile(fileToImport);
        if (driverFunction == null) {
            throw new SQLException("Cannot find any file driver for the file." + filePath);
        }
        try {
            return driverFunction.importFile(connection, tableName, fileToImport, encoding, deleteTable,
                    new EmptyProgressVisitor());
        } catch (SQLException | IOException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                throw new SQLException("Unable to rollback.", e1);
            }
            throw new SQLException("Cannot import the file.", e);
        }
    }

    /**
     * Link a table from another database to an H2GIS database
     *
     * @param targetConnection The targetConnection to the database that will
     * received the table
     * @param databaseProperties External database databaseProperties to set up
     * a connection to the target database
     * @param sourceTable The name of the table in the external database
     * @param targetTable The name of the table in the H2GIS database
     * @param delete True to delete the table if exists
     * @throws java.sql.SQLException
     * @return  the name of the linked table
     */
    public static String linkedTable(Connection targetConnection, Map<String, String> databaseProperties, String sourceTable, String targetTable,
            boolean delete) throws SQLException {
        if (targetConnection == null) {
            throw new SQLException("The connection to the output database cannot be null.\n");
        }
        if (sourceTable == null || sourceTable.isEmpty()) {
            throw new SQLException("The source table cannot be null or empty.\n");
        }
        if (targetTable == null || targetTable.isEmpty()) {
            throw new SQLException("The target table cannot be null or empty.\n");
        }
        if (databaseProperties == null || databaseProperties.isEmpty()) {
            throw new SQLException("The external database connection properties cannot be null or empty.\n");
        }
        final DBTypes targetDBType = DBUtils.getDBType(targetConnection);
        if (targetDBType != DBTypes.H2 && targetDBType != DBTypes.H2GIS) {
            throw new SQLException("Link file is only supported with an H2GIS database");
        }

        String user = databaseProperties.getOrDefault(DataSourceFactory.JDBC_USER, "sa");
        String password = databaseProperties.getOrDefault(DataSourceFactory.JDBC_PASSWORD, "");
        String driverName = "";
        String jdbc_url = databaseProperties.get("url");
        if (jdbc_url != null) {
            if (jdbc_url.startsWith("jdbc:")) {
                String url = jdbc_url.substring("jdbc:".length());
                if (url.startsWith("h2")) {
                    driverName = "org.h2.Driver";
                } else if (url.startsWith("postgresql_h2")) {
                    driverName = "org.h2gis.postgis_jts.Driver";
                } else if (url.startsWith("postgresql")) {
                    driverName = "org.h2gis.postgis_jts.Driver";
                    jdbc_url = "jdbc:postgresql_h2" + jdbc_url.substring("jdbc:postgresql".length());
                }
                if (!driverName.isEmpty()) {
                    TableLocation targetTableLocation = TableLocation.parse(targetTable, targetDBType);
                    String ouputTableName = targetTableLocation.toString(targetDBType);
                    if (delete) {
                        try ( //Drop table if exists
                                Statement stmt = targetConnection.createStatement()) {
                            stmt.execute("DROP TABLE IF EXISTS " + ouputTableName);
                            if (!targetConnection.getAutoCommit()) {
                                targetConnection.commit();
                            }
                        } catch (SQLException e) {
                            try {
                                targetConnection.rollback();
                            } catch (SQLException e1) {
                                throw new SQLException("Unable to rollback.", e1);
                            }
                            throw new SQLException("Cannot drop the table", e);
                        }
                    }

                    try (Statement statement = targetConnection.createStatement()) {
                        statement.execute(String.format("CREATE LINKED TABLE %s('%s', '%s', '%s', '%s', '%s')",
                                ouputTableName, driverName, jdbc_url, user, password, sourceTable));
                        if (!targetConnection.getAutoCommit()) {
                            targetConnection.commit();
                        }
                    } catch (SQLException e) {
                        try {
                            targetConnection.rollback();
                        } catch (SQLException e1) {
                            throw new SQLException("Unable to rollback.", e1);
                        }
                        throw new SQLException("Cannot linked the table", e);
                    }
                    return ouputTableName;
                } else {
                    throw new SQLException("This database is not yet supported");
                }
            } else {
                throw new SQLException("JDBC URL must start with jdbc:");
            }
        } else {
            throw new SQLException("The URL of the external database cannot be null");
        }
    }

    /**
     * Create a dynamic link from a file to a H2GIS database
     *
     * @param connection The connection to database
     * @param filePath The path of the file
     * @param tableName The name of the table created to store the file
     * @param delete True to delete the table if exists
     * @throws java.sql.SQLException
     */
    public static String linkedFile(Connection connection, String filePath, String tableName, boolean delete) throws SQLException {
        final DBTypes dbType = DBUtils.getDBType(connection);
        if (dbType != DBTypes.H2 && dbType != DBTypes.H2GIS) {
            throw new SQLException("Link file is only supported with an H2GIS database");
        }
        if (delete) {
            try {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("DROP TABLE IF EXISTS " + tableName);
                    if (!connection.getAutoCommit()) {
                        connection.commit();
                    }
                }
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    throw new SQLException("Unable to rollback.", e1);
                }
                throw new SQLException("Cannot drop the table", e);
            }
        }

        try {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("CALL FILE_TABLE('%s','%s')", filePath, tableName));
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                throw new SQLException("Unable to rollback.", e1);
            }
            throw new SQLException("Cannot link the file", e);
        }
        return tableName;
    }

    /**
     * Method to export a table into another database
     *
     * @param sourceConnection source database connection
     * @param sourceTable the name of the table to export or a select query
     * @param targetConnection target database connection
     * @param targetTable target table name
     * @param mode -1 delete the target table if exists and create a new table,
     * 0 create a new table, 1 update the target table if exists
     * @param batch_size batch size value before sending the data
     *
     * @throws java.sql.SQLException
     * @return name of the export table formatted according the database target
     */
    public static String exportToDataBase(Connection sourceConnection, String sourceTable,
            Connection targetConnection, String targetTable, int mode, int batch_size) throws SQLException {
        if (sourceConnection == null) {
            throw new SQLException("The connection to the source database cannot be null.\n");
        }
        if (targetConnection == null) {
            throw new SQLException("The connection to the output database cannot be null.\n");
        }

        if (-2 > mode && mode > 2) {
            throw new SQLException("Supported mode to export the table is : \n"
                    + "-1 delete the target table if exists and create a new table, \n"
                    + "0 create a new table\n"
                    + "1 update the target table if exists");
        }

        if (batch_size <= 0) {
            throw new SQLException("The batch size must be greater than 0.\n");
        }

        if (sourceTable == null || sourceTable.isEmpty()) {
            throw new SQLException("The source table cannot be null or empty.\n");
        }

        if (targetTable == null || targetTable.isEmpty()) {
            throw new SQLException("The target table cannot be null or empty.\n");
        }

        final DBTypes sourceDBType = DBUtils.getDBType(sourceConnection);
        final DBTypes targetDBType = DBUtils.getDBType(targetConnection);

        TableLocation targetTableLocation = TableLocation.parse(targetTable, targetDBType);
        String ouputTableName = targetTableLocation.toString(targetDBType);

        String query;
        //Check if the source table is a query
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sourceTable);
        if (matcher.find()) {
            if (sourceTable.startsWith("(") && sourceTable.endsWith(")")) {
                query = sourceTable;
            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM MYTATBLE)'.");
            }
        } else {
            TableLocation sourceTableLocation = TableLocation.parse(sourceTable, sourceDBType);
            if (!JDBCUtilities.tableExists(sourceConnection, sourceTableLocation)) {
                throw new SQLException("The source table doesn't exist.\n");
            }
            query = "SELECT * FROM " + sourceTableLocation.toString(sourceDBType);
        }

        try {
            Statement inputStat = sourceConnection.createStatement();
            ResultSet inputRes = inputStat.executeQuery(query);
            ResultSetMetaData inputMetadata = inputRes.getMetaData();

            boolean isTargetAutoCommit = targetConnection.getAutoCommit();
            targetConnection.setAutoCommit(false);
            if (mode == -1) {
                try ( //Drop table if exists
                        Statement stmt = targetConnection.createStatement()) {
                    stmt.execute("DROP TABLE IF EXISTS " + ouputTableName);
                    targetConnection.commit();

                } catch (SQLException e) {
                    try {
                        targetConnection.rollback();
                    } catch (SQLException e1) {
                        throw new SQLException("Unable to rollback.", e1);
                    }
                    throw new SQLException("Cannot drop the table", e);
                }
                //Re-create the table
                String ddlCommand = JDBCUtilities.createTableDDL(inputMetadata, ouputTableName);
                if (!ddlCommand.isEmpty()) {
                    try (Statement outputST = targetConnection.createStatement()) {
                        outputST.execute(ddlCommand);
                        targetConnection.commit();
                    } catch (SQLException e) {
                        try {
                            targetConnection.rollback();
                        } catch (SQLException e1) {
                            throw new SQLException("Unable to rollback.", e1);
                        }
                        throw new SQLException("Cannot create the output table", e);
                    }
                }
            } else if (mode == 0) {
                //Check if target table exists
                if (JDBCUtilities.tableExists(targetConnection, targetTableLocation)) {
                    throw new SQLException("The target table already exists.\n" + ""
                            + "Please use a -1 (delete) or 2 (insert) mode to export the table");
                }
                String ddlCommand = JDBCUtilities.createTableDDL(inputMetadata, ouputTableName);
                if (!ddlCommand.isEmpty()) {
                    try (Statement outputST = targetConnection.createStatement()) {
                        targetConnection.rollback();
                        outputST.execute(ddlCommand);
                        targetConnection.commit();

                    } catch (SQLException e) {
                        try {
                            targetConnection.rollback();
                        } catch (SQLException e1) {
                            LOGGER.error("Unable to rollback.", e1);
                        }
                        throw new SQLException("Cannot create the output table", e);
                    }
                } else if (mode == 1) {
                    //Check if target table exists
                    //and do insert
                    //Check if target table exists
                    if (!JDBCUtilities.tableExists(targetConnection, targetTableLocation)) {
                        throw new SQLException("The target table doesn't exist.\n" + ""
                                + "Please use a 0 mode to create a new table and populate it");
                    }
                }
            }
            PreparedStatement preparedStatement = null;
            try {
                int columnsCount = inputMetadata.getColumnCount();
                HashMap<String, Integer> geomColumnAndSRID = new HashMap<>();
                StringBuilder insertTable = new StringBuilder("INSERT INTO ");
                insertTable.append(ouputTableName).append(" VALUES(?");
                for (int i = 1; i < columnsCount; i++) {
                    insertTable.append(",").append("?");
                }
                insertTable.append(")");

                preparedStatement = targetConnection.prepareStatement(insertTable.toString());
                //Check the first row in order to limit the batch size if the query doesn't work
                inputRes.next();
                for (int i = 0; i < columnsCount; i++) {
                    int index = i + 1;
                    Object value = inputRes.getObject(index);
                    if(inputMetadata.getColumnTypeName(index).equalsIgnoreCase("GEOMETRY")) {
                        geomColumnAndSRID.put(inputMetadata.getColumnName(index), ((Geometry) value).getSRID());
                    }
                    preparedStatement.setObject(index, value);
                }
                preparedStatement.execute();
                long batchSize = 0;
                while (inputRes.next()) {
                    for (int i = 0; i < columnsCount; i++) {
                        int index = i + 1;
                        Object value = inputRes.getObject(index);
                        String columnName = inputMetadata.getColumnName(index);
                         if(geomColumnAndSRID.containsKey(columnName)) {
                             Geometry geometry = (Geometry) value;
                             int currentSRID = geometry.getSRID();
                             Integer tmpSRID = geomColumnAndSRID.get(columnName);
                             if (tmpSRID != currentSRID) {
                                 geomColumnAndSRID.remove(inputMetadata.getColumnName(index));
                             }
                         }
                       preparedStatement.setObject(index, value);
                    }
                    preparedStatement.addBatch();
                    batchSize++;
                    if (batchSize >= batch_size) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                        batchSize = 0;
                    }
                }
                if (batchSize > 0) {
                    preparedStatement.executeBatch();
                }
                //Alter SRID
                if(!geomColumnAndSRID.isEmpty()){
                    StringBuilder querySRID = new StringBuilder();
                    for (Map.Entry<String, Integer> entry : geomColumnAndSRID.entrySet()) {
                        String fieldName = TableLocation.capsIdentifier(entry.getKey(), targetDBType);
                        Integer srid = entry.getValue();
                        querySRID.append("ALTER TABLE ").append(ouputTableName).append(" ALTER COLUMN ").append(fieldName);
                        querySRID.append(" TYPE GEOMETRY(GEOMETRY, ").append(srid).append(") USING ST_SetSRID(").append(fieldName).append(",").append(srid).append(");\n");
                    }

                    try (Statement outputST = targetConnection.createStatement()) {
                        outputST.execute(querySRID.toString());
                        targetConnection.commit();
                    } catch (SQLException e) {
                        try {
                            targetConnection.rollback();
                        } catch (SQLException e1) {
                            LOGGER.error("Unable to rollback.", e1);
                        }
                        throw new SQLException("Cannot alter the table with the SRID", e);
                    }

                }
            } catch (SQLException e) {
                try {
                    targetConnection.rollback();
                } catch (SQLException e1) {
                    throw new SQLException("Unable to rollback.", e1);
                }
                throw new SQLException("Cannot insert the data in the table", e);
            } finally {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                targetConnection.setAutoCommit(isTargetAutoCommit);

            }
        } catch (SQLException e) {
            throw new SQLException("Cannot save the table " + sourceTable + " to the " + targetTable + "\n", e);

        }
        return ouputTableName;
    }
}
