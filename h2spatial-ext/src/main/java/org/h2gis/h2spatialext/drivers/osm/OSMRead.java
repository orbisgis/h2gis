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
package org.h2gis.h2spatialext.drivers.osm;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.URIUtility;

/**
 * SQL Function to copy OSM File data into a set of tables.
 *
 * @author Erwan Bocher
 */
public class OSMRead extends AbstractFunction implements ScalarFunction {

    public OSMRead() {
        addProperty(PROP_REMARKS, "Read a OSM file and copy the content in the specified tables.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readOSM";
    }

    /**
     * 
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws FileNotFoundException
     * @throws SQLException 
     */
    public static void readOSM(Connection connection, String fileName, String tableReference) throws FileNotFoundException, SQLException {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException("The following file does not exists:\n" + fileName);
        }
        OSMParser osmp = new OSMParser();
        osmp.read(connection, tableReference, URIUtility.fileFromString(fileName), new EmptyProgressVisitor());
    }

    /**
     * 
     * @param connection
     * @param fileName
     * @throws FileNotFoundException
     * @throws SQLException 
     */
    public static void readOSM(Connection connection, String fileName) throws FileNotFoundException, SQLException {
        final String name = URIUtility.fileFromString(fileName).getName();
        readOSM(connection, fileName, name.substring(0, name.lastIndexOf(".")).toUpperCase());
    }

}
