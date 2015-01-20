/**
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

package org.h2gis.h2spatialext.function.spatial.earth;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_Shadow extends DeterministicScalarFunction{

    

    @Override
    public String getJavaStaticMethod() {
        return "computeShadow";
    }
    
    public static Geometry computeShadow(Geometry geometry, double heigth){
        return computeShadow(geometry, heigth, new Date());
    }

    public static Geometry computeShadow(Geometry geometry, double height, Date date) {
        if(geometry instanceof Polygon){
            return shadowPolygon(geometry, height, date);
        }
        else if(geometry instanceof LineString){
            return  shadowLine((LineString) geometry, height, date);
        }
        else if(geometry instanceof Point){
            
        }
        else{
           return null;
        }
        return null;
    }
    
    /**
     * 
     * @param lineString
     * @param height
     * @param date
     * @return 
     */
    private static Geometry shadowLine(LineString lineString, double height, Date date) { 
        Coordinate sunDirection = ST_SunDirection.sunDirection(lineString, date).getCoordinate();        
        GeometryFactory factory = lineString.getFactory();
        Coordinate[] coords = lineString.getCoordinates();
        Collection<Polygon> shadows = new ArrayList<Polygon>();
        for (int i = 0; i < coords.length - 1; i++) {
            Coordinate startCoord = coords[i];
            Coordinate endCoord = coords[i+1];            
            shadows.add(factory.createPolygon(new Coordinate[]{startCoord, projection(startCoord, height, sunDirection),
            projection(endCoord, height, sunDirection), endCoord, startCoord}));
        }        
        CascadedPolygonUnion union = new CascadedPolygonUnion(shadows);
        return union.union();
    }
    
     /**
     * Calculating the coordinates of the shadow of a 2D point with a height
     * according to the position of the sun
     *     
     * @param c : coordinates of the point whose shade is calculated
     * @param height : height of the point
     * @param direction : sun direction
     * @return : shadow coordinate
     */
    public static Coordinate projection(Coordinate c, double height, Coordinate direction) {
        Coordinate project = new Coordinate();
        project.x = c.x + direction.x * height;
        project.y = c.y + direction.y * height;
        return project;
    }

    private static Geometry shadowPolygon(Geometry geometry, double heigth, Date date) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
