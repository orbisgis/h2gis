/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
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
package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialext.function.spatial.volume.GeometryExtrude;

/**
 * The ST_Extrude function returns a geometry collection that contains walls, 
 * roof and floor geometries.
 * Only single input geometry are allowed : polygon and linestring.
 * @author Erwan Bocher
 */
public class ST_Extrude extends DeterministicScalarFunction{

    
    public ST_Extrude(){
        addProperty(PROP_REMARKS, "Extrude a single geometry (polygon or linestring)\n"
                + " to a geometry collection. The extrude algorithm build a 3D representation of\n"
                + "the floor, the walls and the roof of the inout geometry.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "extrudeGeometry";
    }
    
    public static GeometryCollection extrudeGeometry(Geometry geometry, double hight) throws SQLException{
         if(geometry instanceof Polygon){
             return GeometryExtrude.extrudePolygonAsGeometry((Polygon)geometry, hight);
         }
         else if (geometry instanceof LineString){
             return GeometryExtrude.extrudeLineStringAsGeometry((LineString)geometry, hight);
         }
         throw new SQLException("This function supports only single geometry polygon or linestring.");        
    }
}
