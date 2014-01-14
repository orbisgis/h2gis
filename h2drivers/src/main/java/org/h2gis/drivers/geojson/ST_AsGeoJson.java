/*
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
package org.h2gis.drivers.geojson;

import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Transform a JTS geometry to a geojson geometry representation
 *
 * @author Erwan Bocher
 */
public class ST_AsGeoJson extends DeterministicScalarFunction {

    public ST_AsGeoJson() {
        addProperty(PROP_REMARKS, "Return the geometry as a Geometry Javascript Object Notation (GeoJSON 1.0) element. \n"
                + "2D and 3D Geometries are both supported. \n"
                + "GeoJSON only support SFS 1.1 geometry type (POINT, LINESTRING, POLYGON and COLLECTION).");
    }

    @Override
    public String getJavaStaticMethod() {
        return "toGeojson";
    }

    /**
     * Convert the geometry to a geojson representation
     *
     * @param geom
     * @return
     */
    public static String toGeojson(Geometry geom) {
        StringBuilder sb = new StringBuilder();
        GeojsonGeometry.toGeojsonGeometry(geom, sb);
        return sb.toString();
    }
}
