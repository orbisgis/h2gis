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

package org.h2gis.functions.io.osm;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;

/**
 *
 * @author Erwan Bocher
 */
public class OSMDriverFunction implements DriverFunction {

    public static String DESCRIPTION = "OSM file (0.6)";
    public static String DESCRIPTION_GZ = "OSM Gzipped file (0.6)";
    public static String DESCRIPTION_BZ2 = "OSM Bzipped file (0.6)";


    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getExportFormats() {
        return new String[0];
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("osm")) {
            return DESCRIPTION;
        } else if (format.equalsIgnoreCase("gz")) {
            return DESCRIPTION_GZ;
        } else  if (format.equalsIgnoreCase("bz2")) {
            return DESCRIPTION_BZ2;
        } else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return extension.equalsIgnoreCase("osm") ||
                extension.equalsIgnoreCase("gz") ||
                extension.equalsIgnoreCase("bz2");
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        importFile(connection, tableReference, fileName, progress, false);
    }
    
    
     /**
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference prefix uses to store the OSM tables
     * @param fileName File path to read
     * @param progress
     * @param deleteTables  true to delete the existing tables
     * @throws SQLException Table write error
     * @throws IOException File read error
     */
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress, boolean deleteTables) throws SQLException, IOException {
        if(fileName == null || !(fileName.getName().endsWith(".osm") || fileName.getName().endsWith("osm.gz") || fileName.getName().endsWith("osm.bz2"))) {
            throw new IOException(new IllegalArgumentException("This driver handle only .osm, .osm.gz and .osm.bz2 files"));
        }
        if(deleteTables){
            OSMTablesFactory.dropOSMTables(connection, JDBCUtilities.isH2DataBase(connection.getMetaData()), tableReference);
        }
        OSMParser osmp = new OSMParser();
        osmp.read(connection, tableReference, fileName, progress);
        }

    @Override
    public String[] getImportFormats() {
        return new String[]{"osm","gz","bz2"};
    }

}
