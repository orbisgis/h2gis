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

package org.h2gis.functions.spatial.create;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Expands a geometry's envelope by the given delta X and delta Y.
 *
 * @author Erwan Bocher, Nicolas Fortin
 */
public class ST_Expand extends DeterministicScalarFunction {

    public ST_Expand() {
        addProperty(PROP_REMARKS, "Expands a geometry's envelope in both X and or Y directions.\n Both"
                + " positive and negative distances are supported.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "expand";
    }
    
    /**
     * Expands a geometry's envelope by the given delta X and delta Y. Both
     * positive and negative distances are supported.
     *
     * @param geometry the input geometry
     * @param delta the distance to expand the envelope along the X and Y axis
     * @return the expanded geometry
     */
    public static Geometry expand(Geometry geometry, double delta) {
        return expand(geometry, delta, delta);
    }

    /**
     * Expands a geometry's envelope by the given delta X and delta Y. Both
     * positive and negative distances are supported.
     *
     * @param geometry the input geometry
     * @param deltaX the distance to expand the envelope along the X axis
     * @param deltaY the distance to expand the envelope along the Y axis
     * @return the expanded geometry
     */
    public static Geometry expand(Geometry geometry, double deltaX, double deltaY) {
        if(geometry == null){
            return null;
        }
        Envelope env = geometry.getEnvelopeInternal();
        // As the time of writing Envelope.expand is buggy with negative parameters
        double minX = env.getMinX() - deltaX;
        double maxX = env.getMaxX() + deltaX;
        double minY = env.getMinY() - deltaY;
        double maxY = env.getMaxY() + deltaY;
        Envelope expandedEnvelope = new Envelope(minX < maxX ? minX : (env.getMaxX() - env.getMinX()) / 2 + env.getMinX(),
                                                 minX < maxX ? maxX : (env.getMaxX() - env.getMinX()) / 2 + env.getMinX(),
                                                 minY < maxY ? minY : (env.getMaxY() - env.getMinY()) / 2 + env.getMinY(),
                                                 minY < maxY ? maxY : (env.getMaxY() - env.getMinY()) / 2 + env.getMinY());
        return geometry.getFactory().toGeometry(expandedEnvelope);
    }
}
