/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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

package org.h2spatial.internal.function.spatial.interoperability;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import org.h2spatial.ValueGeometry;
import org.h2spatialapi.ScalarFunction;
import org.postgis.PGgeometry;
import org.postgis.binary.BinaryWriter;

import java.sql.SQLException;

/**
 * Convert PostGIS Geometry into H2Spatial geometry value
 * @author Nicolas Fortin
 */
public class PGtoValueGeometry implements ScalarFunction {
    private static final WKBReader WKB_READER = new WKBReader();

    @Override
    public String getJavaStaticMethod() {
        // This function is not standard and its dependency is optional
        try {
            System.out.println(PGgeometry.class.getName()+" is available");
            return "toValueGeometry";
        } catch (NoClassDefFoundError ex) {
            return null;
        }
    }

    @Override
    public Object getProperty(String propertyName) {
        return null;
    }

    /**
     * Convert PostGIS Geometry into H2Spatial geometry value
     * @param geometry {@link PGgeometry} instance
     * @return ValueGeometry or null if parameter is null
     * @throws SQLException If the conversion cannot be done
     */
    public static ValueGeometry toValueGeometry(PGgeometry geometry) throws SQLException {
        try {
            if(geometry==null) {
                return null;
            }
            BinaryWriter binaryWriter = new BinaryWriter();
            byte[] wKB = binaryWriter.writeBinary(geometry.getGeometry());
            try {
                return new ValueGeometry(WKB_READER.read(wKB));
            } catch (ParseException ex) {
                throw new SQLException(ex);
            }
        } catch (NoClassDefFoundError ex) {
            throw new SQLException("Load the PostGIS library before using this function",ex);
        }
    }
}
