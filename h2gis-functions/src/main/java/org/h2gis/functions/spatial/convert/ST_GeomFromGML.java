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

package org.h2gis.functions.spatial.convert;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.gml2.GMLReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Read a GML representation and convert it to a geometry.
 * @author Erwan Bocher
 */
public class ST_GeomFromGML extends DeterministicScalarFunction{
    public static GeometryFactory gf;
    
    public ST_GeomFromGML(){
        addProperty(PROP_REMARKS, "Convert an input GML representation of geometry to a geometry.\n"
                +" An optional argument is used to set a SRID.\n"
                + "This function supports only GML 2.1.2");
    }
    
    @Override
    public String getJavaStaticMethod() {
        return "toGeometry";
    }
    
    /**
     * Read the GML representation
     * 
     * @param gmlFile read gml geometry
     * @return Geometry
     */
    public  static Geometry toGeometry(String gmlFile) throws SAXException, IOException, ParserConfigurationException{        
        return toGeometry(gmlFile, 0);
    }
    
    /**
     * Read the GML representation with a specified SRID
     *
     * @param gmlFile gml geometry
     * @param srid force srid
     * @return new geometry
     */
    public static Geometry toGeometry(String gmlFile, int srid) throws SAXException, IOException, ParserConfigurationException {
        if (gmlFile == null) {
            return null;
        }
        if (gf == null) {
            gf = new GeometryFactory(new PrecisionModel(), srid);
        }
        GMLReader gMLReader = new GMLReader();
        return gMLReader.read(gmlFile, gf);
    }
    
}
