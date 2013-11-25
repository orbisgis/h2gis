/**
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

package org.h2gis.h2spatialext;

import org.h2gis.drivers.DriverManager;
import org.h2gis.drivers.dbf.DBFRead;
import org.h2gis.drivers.dbf.DBFWrite;
import org.h2gis.drivers.shp.SHPRead;
import org.h2gis.drivers.shp.SHPWrite;
import org.h2gis.h2spatialext.function.spatial.affine_transformations.ST_Rotate;
import org.h2gis.h2spatialext.function.spatial.affine_transformations.ST_Scale;
import org.h2gis.h2spatialext.function.spatial.properties.ST_Extent;
import org.h2gis.h2spatialext.function.spatial.predicates.ST_Covers;
import org.h2gis.h2spatialext.function.spatial.predicates.ST_DWithin;
import org.h2gis.h2spatialext.function.spatial.predicates.ST_IsRectangle;
import org.h2gis.h2spatialext.function.spatial.predicates.ST_IsValid;
import org.h2gis.h2spatialext.function.spatial.properties.*;
import org.h2gis.h2spatialext.function.spatial.properties.ST_Explode;
import org.h2gis.h2spatialapi.Function;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.drivers.gpx.GPXRead;

/**
 * Registers the SQL functions contained in h2spatial-ext.
 *
 * @author Nicolas Fortin
 * @author Adam Gouge
 */
public class CreateSpatialExtension {
    /**
     * @return instance of all built-ins functions
     */
    public static Function[] getBuiltInsFunctions() {
        return new Function[] {
                new ST_3DLength(),
                new ST_CompactnessRatio(),
                new ST_CoordDim(),
                new ST_Covers(),
                new ST_DWithin(),
                new ST_Extent(),
                new ST_Explode(),
                new ST_IsRectangle(),
                new ST_IsValid(),
                new ST_Rotate(),
                new ST_Scale(),
                new ST_XMin(),
                new ST_XMax(),
                new ST_YMin(),
                new ST_YMax(),
                new ST_ZMin(),
                new ST_ZMax(),
                new DriverManager(),
                new SHPRead(),
                new SHPWrite(),
                new DBFRead(),
                new DBFWrite(),
                new GPXRead()};
    }

    /**
     * Init H2 DataBase with extended spatial functions
     * @param connection
     * @throws SQLException
     */
    public static void initSpatialExtension(Connection connection) throws SQLException {
        org.h2gis.h2spatial.CreateSpatialExtension.initSpatialExtension(connection);
        // Register project's functions
        addSpatialFunctions(connection);
    }

    /**
     * Register built-in functions
     * @param connection Active connection
     * @throws SQLException Error while creating statement
     */
    public static void addSpatialFunctions(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        for(Function function : getBuiltInsFunctions()) {
            try {
                org.h2gis.h2spatial.CreateSpatialExtension.registerFunction(st,function,"");
            } catch (SQLException ex) {
                // Catch to register other functions
                ex.printStackTrace(System.err);
            }
        }
    }
}
