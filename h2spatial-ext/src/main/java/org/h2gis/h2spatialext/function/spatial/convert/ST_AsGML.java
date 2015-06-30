/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatialext.function.spatial.convert;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.gml2.GMLWriter;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

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
