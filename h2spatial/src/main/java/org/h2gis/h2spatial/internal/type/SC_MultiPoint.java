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

package org.h2gis.h2spatial.internal.type;

import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.utilities.GeometryTypeCodes;
import org.h2gis.h2spatialapi.ScalarFunction;
import org.h2gis.utilities.jts_utils.GeometryMetaData;

import java.io.IOException;

/**
 * Constraint for MultiPoint field type.
 * @author Nicolas Fortin
 */
public class SC_MultiPoint extends AbstractFunction implements ScalarFunction , GeometryConstraint {

    public SC_MultiPoint() {
        addProperty(PROP_REMARKS, "Return true if the geometry is a MultiPoint.");
    }

    @Override
    public int getGeometryTypeCode() {
        return GeometryTypeCodes.MULTIPOINT;
    }

    @Override
    public String getJavaStaticMethod() {
        return "isMultiPoint";
    }

    /**
     * @param bytes Geometry WKB or NULL
     * @return True if null or if the field type fit with the constraint.
     */
    public static boolean isMultiPoint(byte[] bytes) {
        if(bytes==null) {
            return true;
        }
        try {
            return GeometryMetaData.getMetaDataFromWKB(bytes).geometryType == GeometryTypeCodes.MULTIPOINT;
        } catch (IOException ex) {
            return false;
        }
    }
}
