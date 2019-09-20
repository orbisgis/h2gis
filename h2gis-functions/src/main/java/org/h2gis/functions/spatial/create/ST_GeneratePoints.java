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
package org.h2gis.functions.spatial.create;

import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.shape.random.RandomPointsBuilder;

/**
 * This function is used to generate pseudo-random points 
 * until the requested number are found within the input area 
 * (polygon or multipolygon)
 * 
 * @author Erwan Bocher, CNRS
 */
public class ST_GeneratePoints extends DeterministicScalarFunction {

    
    public ST_GeneratePoints() {
        addProperty(PROP_REMARKS, "Generates pseudo-random points until "
                + "the requested number are found within the input polygon or multipolygon.");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "generatePoints";
    }
    
     /**
     * Make the random points
     *
     * @param geom input geometry as polygon or multipolygon
     * @param nPts number of random points
     * @return
     * @throws java.sql.SQLException
     */
    public static Geometry generatePoints(Geometry geom, int nPts) throws SQLException {
        if (geom == null) {
            return null;
        }
        if (geom instanceof Polygon || geom instanceof MultiPolygon) {
            RandomPointsBuilder shapeBuilder = new RandomPointsBuilder(geom.getFactory());
            shapeBuilder.setExtent(geom);
            shapeBuilder.setNumPoints(nPts);
            return shapeBuilder.getGeometry();
        } else {
            throw new SQLException("Only polygon or multipolygon is supported");
        }
    }
    
    
}
