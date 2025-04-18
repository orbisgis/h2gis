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

package org.h2gis.functions.io.osm;

import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.functions.io.DriverManager;

/**
 *
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS (UBS 2019)
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
    public String[] exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[]  exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
       throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String options, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
       throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName,
                            String options, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress)
            throws SQLException, IOException {
        return importFile(connection, tableReference, fileName, false, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName,String options, ProgressVisitor progress
                           ) throws SQLException, IOException {
        return importFile(connection, tableReference, fileName,false, progress);
    }

    /**
     *
     * @param connection Active connection, do not close this connection.
     * @param tableReference prefix uses to store the OSM tables
     * @param fileName File path to read
     * @param progress Progress visitor following the execution.
     * @param deleteTables  true to delete the existing tables
     * @throws SQLException Table write error
     * @throws IOException File read error
     */
    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        return importFile( connection,  tableReference,  fileName,  null,  deleteTables,  progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String options, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        progress = DriverManager.check(connection,tableReference,fileName, progress);
        OSMParser osmp = new OSMParser(connection, fileName, options, deleteTables);
        return osmp.read(tableReference, progress);
    }

    @Override
    public String[] getImportFormats() {
        return new String[]{"osm","osm.gz","osm.bz2"};
    }

}
