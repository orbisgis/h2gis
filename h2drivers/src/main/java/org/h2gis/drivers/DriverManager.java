/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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

package org.h2gis.drivers;

import org.h2gis.drivers.dbf.DBFEngine;
import org.h2gis.drivers.shp.SHPEngine;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Use the appropriate driver to open a specified file path.
 * @author Nicolas Fortin
 */
public class DriverManager extends AbstractFunction implements ScalarFunction {

    private static final DriverDef[] DRIVERS = new DriverDef[] {
            new DriverDef(DBFEngine.class.getName(),"dbf"),
            new DriverDef(SHPEngine.class.getName(),"shp")};

    public DriverManager() {
        addProperty(PROP_NAME, "FILE_TABLE");
    }

    @Override
    public String getJavaStaticMethod() {
        return "openFile";
    }
    /*
    public static String openFile(Connection connection, String fileName) {

    }
    */

    /**
     * Create a new table
     * @param connection
     * @param fileName
     * @param tableName
     */
    public static void openFile(Connection connection, String fileName, String tableName) throws SQLException {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1,fileName.length());
        for(DriverDef driverDef : DRIVERS) {
            if(driverDef.getFileExt().equalsIgnoreCase(ext)) {
                Statement st = connection.createStatement();
                st.execute(String.format("CREATE TABLE %s COMMENT '%s' ENGINE \"%s\" WITH \"%s\"",tableName,fileName, driverDef.className,fileName));
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
        private String getClassName() {
            return className;
        }

        /**
         * @return File extension, case insensitive
         */
        private String getFileExt() {
            return fileExt;
        }
    }
}
