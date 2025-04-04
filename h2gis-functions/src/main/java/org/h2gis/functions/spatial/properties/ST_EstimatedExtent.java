/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.h2gis.functions.spatial.properties;

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.utilities.GeometryTableUtilities;

/**
 * Estimated extent function based on the internal H2 ESTIMATED_ENVELOPE
 * @author Erwan Bocher
 */
public class ST_EstimatedExtent extends AbstractFunction implements ScalarFunction{

    public ST_EstimatedExtent(){
        addProperty(PROP_REMARKS, "Return the 'estimated' extent of the given spatial table.\n"
                + "Only 2D coordinate plane is supported\n"
                + "The Extent is first calculated from the spatial index of the table.\n"
                + "if the pointed geometry column doesn't have a spatial index then\n"
                + "the extent is based on all geometries.\n"
                + "This function is fast, but estimation may include uncommitted data \n"
                + "(including data from other transactions),\n" 
                +"may return approximate bounds, or be different with actual value due to other reasons.");
    }
    @Override
    public String getJavaStaticMethod() {
    return "computeEstimatedExtent";
    }
    
    
    /**
     * Compute the estimated extent based on the first geometry column
     * @param connection database
     * @param tableName table name
     * @return the estimated geometry extent
     */
    public static Geometry computeEstimatedExtent(Connection connection,
                                      String tableName) throws SQLException{
        return GeometryTableUtilities.getEstimatedExtent(connection, TableLocation.parse(tableName, DBTypes.H2GIS));
    }
    
    /**
     * Compute the estimated extent based on a geometry field
     * @param connection database
     * @param tableName table name
     * @param geometryColumn geometry column name
     * @return the estimated geometry extent
     */
    public static Geometry computeEstimatedExtent(Connection connection,
                                      String tableName, String geometryColumn) throws SQLException{  
        return GeometryTableUtilities.getEstimatedExtent(connection, TableLocation.parse(tableName, DBTypes.H2GIS), geometryColumn);
    }
}
