/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.topology;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.RobustLineIntersector3D;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.noding.*;

import java.util.*;

/**
 *
 * @author Erwan Bocher (CNRS)
 * @author Sylvain PALOMINOS (UBS 2019)
 */
public class ST_Node extends DeterministicScalarFunction{

    public ST_Node(){
        addProperty(PROP_REMARKS, "Add nodes on a geometry for each intersection." +
                "\nDuplicate lines are removed ");
    }

    @Override
    public String getJavaStaticMethod() {
        return "node";
    }
    
    /**
     * Nodes a geometry using a monotone chain and a spatial index
     * @param geom {@link Geometry}
     * @return geometry noded
     */
    public static Geometry node(Geometry geom) {
        if (geom == null) {
            return null;
        }
        Noder noder = new MCIndexNoder(new IntersectionAdder(new RobustLineIntersector3D()));
        noder.computeNodes(SegmentStringUtil.extractNodedSegmentStrings(geom));
        Collection segStrings = noder.getNodedSubstrings();
        GeometryFactory geomFact = geom.getFactory();
        Set<LineString> lines = new HashSet<>();
        for (Iterator<SegmentString> segment = segStrings.iterator();  segment.hasNext();){
            SegmentString ss = segment.next();
            LineString line = geomFact.createLineString(ss.getCoordinates());
            lines.add(line);
        }
        return geomFact.createMultiLineString(lines.toArray(lines.toArray(new LineString[0])));

    }
}
