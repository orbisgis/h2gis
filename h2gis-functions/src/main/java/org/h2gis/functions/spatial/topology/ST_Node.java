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
package org.h2gis.functions.spatial.topology;

import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.h2gis.api.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_Node extends DeterministicScalarFunction{

      public ST_Node(){
        addProperty(PROP_REMARKS, "Add nodes on a geometry for each intersection ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "node";
    }
    
    /**
     * Nodes a geometry using a monotone chain and a spatial index
     * @param geom
     * @return 
     */
    public static Geometry node(Geometry geom) {
        if (geom == null) {
            return null;
        }        
        Noder noder = new MCIndexNoder(new IntersectionAdder(new RobustLineIntersector()));
        noder.computeNodes(SegmentStringUtil.extractNodedSegmentStrings(geom));
        return SegmentStringUtil.toGeometry(noder.getNodedSubstrings(), geom.getFactory());
    }
}
