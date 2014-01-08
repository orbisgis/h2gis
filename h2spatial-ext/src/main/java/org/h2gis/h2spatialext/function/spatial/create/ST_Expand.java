/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Expands the envelope of the geometry by a given delta X and Y.
 *
 * @author Erwan Bocher
 */
public class ST_Expand extends DeterministicScalarFunction {

    private static GeometryFactory gf = new GeometryFactory();

    public ST_Expand() {
        addProperty(PROP_REMARKS, "Expands the envelope of the geometry by a given delta X and Y.\n Both"
                + " positive and negative distances are supported.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "expand";
    }

    /**
     * Expands the envelope of the geometry by a given delta X and Y. Both
     * positive and negative distances are supported.
     *
     * @param deltaX the distance to expand the envelope along the the X axis
     * @param deltaY the distance to expand the envelope along the the Y axis
     */
    public static Geometry expand(Geometry geometry, double detlatX, double deltaY) {
        Envelope expand = geometry.getEnvelopeInternal();
        expand.expandBy(detlatX, deltaY);
        return toGeometry(expand);
    }

    /**
     * Converts from Envelope to a polygon geometry.
     *
     * @param envelope
     * @return
     */
    public static Geometry toGeometry(Envelope envelope) {
        if ((envelope.getWidth() == 0) && (envelope.getHeight() == 0)) {
            return gf.createPoint(new Coordinate(envelope.getMinX(),
                    envelope.getMinY()));
        }

        if ((envelope.getWidth() == 0) || (envelope.getHeight() == 0)) {
            return gf.createLineString(new Coordinate[]{
                new Coordinate(envelope.getMinX(), envelope.getMinY()),
                new Coordinate(envelope.getMaxX(), envelope.getMaxY())});
        }

        return gf.createPolygon(gf.createLinearRing(new Coordinate[]{
            new Coordinate(envelope.getMinX(), envelope.getMinY()),
            new Coordinate(envelope.getMinX(), envelope.getMaxY()),
            new Coordinate(envelope.getMaxX(), envelope.getMaxY()),
            new Coordinate(envelope.getMaxX(), envelope.getMinY()),
            new Coordinate(envelope.getMinX(), envelope.getMinY())}), null);
    }
}
