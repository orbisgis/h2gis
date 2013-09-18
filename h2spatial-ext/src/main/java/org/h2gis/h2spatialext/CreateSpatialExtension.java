package org.h2gis.h2spatialext;

import org.h2gis.drivers.DriverManager;
import org.h2gis.drivers.shp.SHPRead;
import org.h2gis.drivers.shp.SHPWrite;
import org.h2gis.h2spatialext.function.spatial.aggregate.ST_Extent;
import org.h2gis.h2spatialext.function.spatial.table.ST_Explode;
import org.h2gis.h2spatialapi.Function;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Nicolas Fortin
 */
public class CreateSpatialExtension {
    /**
     * @return instance of all built-ins functions
     */
    public static Function[] getBuiltInsFunctions() {
        return new Function[] {
                new ST_Extent(),
                new ST_Explode(),
                new DriverManager(),
                new SHPRead(),
                new SHPWrite()};
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
