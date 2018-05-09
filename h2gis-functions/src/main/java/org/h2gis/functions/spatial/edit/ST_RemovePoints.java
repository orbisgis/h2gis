/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.edit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedPolygon;
import org.locationtech.jts.geom.util.GeometryEditor;
import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Remove all points on a geometry that are located within a polygon.
 *
 * @author Erwan Bocher
 */
public class ST_RemovePoints extends DeterministicScalarFunction {  
    
    public ST_RemovePoints() {
        addProperty(PROP_REMARKS, "Remove all points on a geometry that are located within a polygon.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "removePoint";
    }

    /**
     * Remove all vertices that are located within a polygon
     *
     * @param geometry
     * @param polygon
     * @return
     * @throws SQLException
     */
    public static Geometry removePoint(Geometry geometry, Polygon polygon) throws SQLException {
        if(geometry == null){
            return null;
        }
        GeometryEditor localGeometryEditor = new GeometryEditor();
        PolygonDeleteVertexOperation localBoxDeleteVertexOperation = new PolygonDeleteVertexOperation(geometry.getFactory(), new PreparedPolygon(polygon));
        Geometry localGeometry = localGeometryEditor.edit(geometry, localBoxDeleteVertexOperation);
        if (localGeometry.isEmpty()) {
            return null;
        }
        return localGeometry;            
    }


    /**
     * This class is used to remove vertexes that are contained into a polygon.
     *
     */
    private static class PolygonDeleteVertexOperation extends GeometryEditor.CoordinateOperation {

        private final GeometryFactory GF;
        //This polygon used to select the coordinates to removed
        private PreparedPolygon polygon;

        public PolygonDeleteVertexOperation(GeometryFactory GF, PreparedPolygon polygon) {
            this.polygon = polygon;
            this.GF=GF;
        }       

        @Override
        public Coordinate[] edit(Coordinate[] paramArrayOfCoordinate, Geometry paramGeometry) {           
            if (!this.polygon.intersects(paramGeometry)) {
                return paramArrayOfCoordinate;
            }            
            Coordinate[] arrayOfCoordinate1 = new Coordinate[paramArrayOfCoordinate.length];
            int j = 0;
            for (Coordinate coordinate : paramArrayOfCoordinate) {
                if (!this.polygon.contains(GF.createPoint(coordinate))) {
                    arrayOfCoordinate1[(j++)] = coordinate;
                }
            }

            Coordinate[] arrayOfCoordinate2 = CoordinateArrays.removeNull(arrayOfCoordinate1);
            Coordinate[] localObject = arrayOfCoordinate2;
            if (((paramGeometry instanceof LinearRing)) && (arrayOfCoordinate2.length > 1) && (!arrayOfCoordinate2[(arrayOfCoordinate2.length - 1)].equals2D(arrayOfCoordinate2[0]))) {
                Coordinate[] arrayOfCoordinate3 = new Coordinate[arrayOfCoordinate2.length + 1];
                CoordinateArrays.copyDeep(arrayOfCoordinate2, 0, arrayOfCoordinate3, 0, arrayOfCoordinate2.length);
                arrayOfCoordinate3[(arrayOfCoordinate3.length - 1)] = new Coordinate(arrayOfCoordinate3[0]);
                localObject = arrayOfCoordinate3;
            }
            return localObject;
        }        
    }   
}
