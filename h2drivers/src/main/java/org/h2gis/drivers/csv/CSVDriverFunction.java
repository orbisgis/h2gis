/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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

package org.h2gis.drivers.csv;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.tools.Csv;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.ProgressVisitor;
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
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        if (fileName.exists()) {
            throw new SQLException("The file " + fileName.getPath() + " already exists.");
        }
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        TableLocation location = TableLocation.parse(tableReference, isH2);
        Statement st = null;
        try {
            st = connection.createStatement();
            new Csv().write(fileName.getPath(), st.executeQuery("SELECT * FROM " + location.toString()), null);
        } finally {
            if (st != null) {
                st.close();
            }
        }
        
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        TableLocation requestedTable = TableLocation.parse(tableReference, isH2);
        String table = requestedTable.getTable();
        ResultSet reader = new Csv().read(fileName.getPath(), null, null);
        ResultSetMetaData metadata = reader.getMetaData();
        int columnCount = metadata.getColumnCount();

        StringBuilder createTable = new StringBuilder("CREATE TABLE ");
        createTable.append(table).append("(");

        StringBuilder insertTable = new StringBuilder("INSERT INTO ");
        insertTable.append(table).append(" VALUES(");

        for (int i = 0; i < columnCount; i++) {
            createTable.append(metadata.getColumnName(i+1)).append(" VARCHAR,");
            insertTable.append("?,");
        }
        createTable.append(")");
        insertTable.append(")");

        Statement stmt = connection.createStatement();
        stmt.execute(createTable.toString());
        stmt.close();

        PreparedStatement pst = connection.prepareStatement(insertTable.toString());
        long batchSize = 0;
        try {
            while (reader.next()) {
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
}
