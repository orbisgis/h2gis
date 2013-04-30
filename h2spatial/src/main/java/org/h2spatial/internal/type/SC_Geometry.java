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

package org.h2spatial.internal.type;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import org.h2spatialapi.ScalarFunction;

/**
 * Space Constraint on Geometry field.
 * @author Nicolas Fortin
 */
public class SC_Geometry implements ScalarFunction {
    @Override
    public String getJavaStaticMethod() {
        return "IsGeometryOrNull";
    }

    @Override
    public Object getProperty(String propertyName) {
        return null;
    }

    /**
     * @param bytes Byte array or null
     * @return True if bytes is Geometry or if bytes is null
     */
    public static Boolean IsGeometryOrNull(byte[] bytes) {
        if(bytes==null) {
            return true;
        }
        WKBReader wkbReader = new WKBReader();
        try {
            wkbReader.read(bytes);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }
}
