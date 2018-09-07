/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.h2gis.functions.spatial.properties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.h2.expression.Function;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.locationtech.jts.geom.Geometry;

/**
 *
 * @author Erwan Bocher
 */
public class ST_EstimatedExtent extends AbstractFunction implements ScalarFunction{

    public ST_EstimatedExtent(){
        addProperty(PROP_REMARKS, "");
    }
    @Override
    public String getJavaStaticMethod() {
    return "computeEstimatedExtent";
    }
    
    /**
     * 
     * @param connection
     * @param tableName
     * @param geometryColumn
     * @return 
     * @throws java.sql.SQLException 
     */
    public static Geometry computeEstimatedExtent(Connection connection,
                                      String tableName, String geometryColumn) throws SQLException{
        
        TableLocation tableLocation =  TableLocation.parse(tableName, true);
        
        return SFSUtilities.getEstimatedExtent(connection, tableLocation, geometryColumn);
    }
}
