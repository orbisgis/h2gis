/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.drivers;

import org.h2.util.StringUtils;
import org.h2gis.drivers.dbf.DBFDriverFunction;
import org.h2gis.drivers.dbf.DBFEngine;
import org.h2gis.drivers.shp.SHPDriverFunction;
import org.h2gis.drivers.shp.SHPEngine;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.DriverFunction;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.URIUtility;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

/**
 * Manage additional table engines in H2.
 * Use the appropriate driver to open a specified file path.
 * @author Nicolas Fortin
 */
public class DriverManager extends AbstractFunction implements ScalarFunction, DriverFunction {

    private static final DriverDef[] DRIVERS = new DriverDef[] {
            new DriverDef(DBFEngine.class.getName(),"dbf"),
            new DriverDef(SHPEngine.class.getName(),"shp")};
    private static final int FORMAT = 0;
    private static final int DESCRIPTION = 1;
    private static final String[][] formatDescription = new String[][] {{"dbf", DBFDriverFunction.DESCRIPTION},
                                                                        {"shp", SHPDriverFunction.DESCRIPTION}};

    public DriverManager() {
        addProperty(PROP_NAME, "FILE_TABLE");
        addProperty(PROP_REMARKS, "Use the appropriate driver to open a specified file path.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "openFile";
    }

    @Override
    public String getFormatDescription(String format) {
        for(String[] descr : formatDescription) {
            if(descr[FORMAT].equalsIgnoreCase(format)) {
                return descr[DESCRIPTION];
            }
        }
        return "";
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return extension.equalsIgnoreCase("shp");
    }

    /**
     * Create a new table
     * @param connection Active connection, do not close this connection.
     * @param fileName File path to write, if exists it may be replaced
     * @param tableName [[catalog.]schema.]table reference
     */
    public static void openFile(Connection connection, String fileName, String tableName) throws SQLException {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1,fileName.length());
        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        for(DriverDef driverDef : DRIVERS) {
            if(driverDef.getFileExt().equalsIgnoreCase(ext)) {
                Statement st = connection.createStatement();
                st.execute(String.format("CREATE TABLE %s COMMENT %s ENGINE %s WITH %s",
                        TableLocation.parse(tableName, isH2).toString(isH2),StringUtils.quoteStringSQL(fileName),
                        StringUtils.quoteJavaString(driverDef.getClassName()),StringUtils.quoteJavaString(fileName)));
                st.close();
                return;
            }
        }
        throw new SQLException("No driver is available to open the "+ext+" file format");
    }

    /**
     * Driver declaration
     */
    private static class DriverDef {
        private String className;
        private String fileExt;

        private DriverDef(String className, String fileExt) {
            this.className = className;
            this.fileExt = fileExt;
        }

        /**
         * @return Class package and name
         */
        public String getClassName() {
            return className;
        }

        /**
         * @return File extension, case insensitive
         */
        public String getFileExt() {
            return fileExt;
        }
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        throw new SQLFeatureNotSupportedException("Work in progress..");
    }

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.LINK;
    }

    @Override
    public String[] getImportFormats() {
        String[] formats = new String[DRIVERS.length];
        for(int idDriver = 0; idDriver < DRIVERS.length; idDriver++) {
            formats[idDriver] = DRIVERS[idDriver].getFileExt();
        }
        return formats;
    }

    @Override
    public String[] getExportFormats() {
        return new String[0];
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        openFile(connection, fileName.getAbsolutePath(), tableReference);
    }
}
