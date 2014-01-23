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
 * SQL function to write a spatial table to a geoJson file
 *
 * @author Erwan Bocher
 */
public class GeojsonWrite extends AbstractFunction implements ScalarFunction {

    
    public GeojsonWrite(){
        addProperty(PROP_REMARKS, "Export a spatial table to a geojson 1.0 file.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "writeGeoJson";
    }

    /**
     * Write the geoJson file
     *
     * @param connection
     * @param fileName
     * @param tableReference
     * @throws IOException
     * @throws SQLException
     */
    public static void writeGeoJson(Connection connection, String fileName, String tableReference) throws IOException, SQLException {
            GeoJsonDriverFunction gjdf = new GeoJsonDriverFunction();
            gjdf.exportTable(connection, tableReference,  new  File(fileName), new EmptyProgressVisitor());
    }
}
