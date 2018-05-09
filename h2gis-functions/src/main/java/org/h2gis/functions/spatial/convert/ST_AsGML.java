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

package org.h2gis.functions.spatial.convert;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.gml2.GMLWriter;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Store a geometry as a GML representation
 * @author Erwan Bocher
 */
public class ST_AsGML extends DeterministicScalarFunction{
    
    public ST_AsGML(){
        addProperty(PROP_REMARKS, "Store a geometry as a GML representation.\n"
                + "It supports OGC GML standard 2.1.2");
    }

    @Override
    public String getJavaStaticMethod() {
        return "toGML";
    }    
    
    /**
     * Write the GML
     * @param geom
     * @return 
     */
    public static String toGML(Geometry geom) {
        if (geom == null) {
            return null;
        }
        int srid = geom.getSRID();
        GMLWriter gmlw = new GMLWriter();
        if (srid != -1 || srid != 0) {
            gmlw.setSrsName("EPSG:" + geom.getSRID());
        }
        return gmlw.write(geom);
    }

}
