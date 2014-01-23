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
package org.h2gis.drivers.geojson;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialapi.ScalarFunction;

/**
 * SQL function to read a geojson file an creates the corresponding spatial
 * table.
 *
 * @author Erwan Bocher
 */
public class GeojsonRead extends AbstractFunction implements ScalarFunction {

    public GeojsonRead() {
        addProperty(PROP_REMARKS, "Import a geojson 1.0 file.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "readGeoJson";
    }

    /**
     * Read the geojson file.
     * 
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws IOException
     * @throws SQLException 
     */
    public static void readGeoJson(Connection connection, String fileName, String tableReference) throws IOException, SQLException {
        GeoJsonDriverFunction gjdf = new GeoJsonDriverFunction();
        gjdf.importFile(connection, tableReference, new File(fileName), new EmptyProgressVisitor());
    }
}
