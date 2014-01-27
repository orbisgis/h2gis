/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.drivers.kml;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class KMLRead extends AbstractFunction implements ScalarFunction {

    @Override
    public String getJavaStaticMethod() {
        return "readKML";
    }

    /**
     * 
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws SQLException
     * @throws IOException 
     */
    public static void readKML(Connection connection, String fileName, String tableReference) throws SQLException, IOException {
        KMLDriverFunction kMLDriverFunction = new KMLDriverFunction();
        kMLDriverFunction.importFile(connection, tableReference, new File(fileName), new EmptyProgressVisitor());
    }
}
