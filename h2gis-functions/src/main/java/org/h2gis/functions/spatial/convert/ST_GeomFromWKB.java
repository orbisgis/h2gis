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
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Convert a WKB object to a geometry
 * 
 * @author Erwan Bocher
 */
public class ST_GeomFromWKB extends DeterministicScalarFunction{

    public ST_GeomFromWKB(){
        addProperty(PROP_REMARKS, "Convert a binary large object to a geometry object.\n"
                + "An optional integer parameter could be used to specify the SRID."
                );
    }
    @Override
    public String getJavaStaticMethod() {
        return "toGeometry";
    }
    
    /**
     * Convert a WKB representation to a geometry
     * @param bytes the input WKB object
     * @param srid the input SRID
     * @return
     * @throws SQLException 
     */
    public static Geometry toGeometry(byte[] bytes, int srid) throws SQLException{
        if(bytes==null) {
            return null;
        }
        WKBReader wkbReader = new WKBReader();
        try {
            Geometry geometry = wkbReader.read(bytes);
            geometry.setSRID(srid);
            return geometry;
        } catch (ParseException ex) {
            throw new SQLException("Cannot parse the input bytes",ex);
        }
    }
    
    /**
     * Convert a WKB representation to a geometry without specify a SRID.
     * @param bytes
     * @return
     * @throws SQLException 
     */
    public static Geometry toGeometry(byte[] bytes) throws SQLException{
        return toGeometry(bytes, 0);
    }
    
}
