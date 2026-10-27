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

package org.h2gis.functions.spatial.properties;

import org.h2.util.geometry.GeometryUtils;
import org.h2.value.Value;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueNull;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

/**
 * Get geometry envelope as geometry.
 * @author Nicolas Fortin
 */
public class ST_Envelope extends DeterministicScalarFunction {

    /**
     * Default constructor
     */
    public ST_Envelope() {
        addProperty(PROP_REMARKS, "Get geometry envelope as geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "envelope";
    }

    /**
     * @param geometry Geometry instance
     * @param srid input SRID
     * @return Geometry envelope
     */
    public static Geometry getEnvelope(Geometry geometry, int srid) {
        if(geometry==null) {
            return null;
        }
        Geometry geometryEnvelope = geometry.getEnvelope();
        geometryEnvelope.setSRID(srid);
        return geometryEnvelope;
    }

    /**
     * @param geometry Geometry instance
     * @return Geometry envelope
     */
    public static Geometry getEnvelope(Geometry geometry) {
        if(geometry==null) {
            return null;
        }
        return geometry.getEnvelope();
    }

    /**
     * @param geometry Geometry instance
     * @param srid input SRID
     * @return Geometry envelope
     */
    public static Geometry envelope(Value geometry, int srid) {
        if (geometry == ValueNull.INSTANCE) {
            return null;
        }
        return toGeometry(geometry.convertToGeometry(null), srid);
    }

    /**
     * @param geometry Geometry instance
     * @return Geometry envelope
     */
    public static Geometry envelope(Value geometry) {
        if (geometry == ValueNull.INSTANCE) {
            return null;
        }
        ValueGeometry value = geometry.convertToGeometry(null);
        return toGeometry(value, value.getSRID());
    }

    /**
     * Envelope of a geometry read from the header of its EWKB, without
     * decoding it: a point, a line or a polygon as with
     * {@link Geometry#getEnvelope()}, an empty point for an empty geometry.
     *
     * @param geometry geometry
     * @param srid SRID of the envelope
     * @return the envelope
     */
    static Geometry toGeometry(ValueGeometry geometry, int srid) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), srid);
        double[] envelope = geometry.getEnvelopeNoCopy();
        if (envelope == null) {
            return factory.toGeometry(new Envelope());
        }
        return factory.toGeometry(new Envelope(envelope[GeometryUtils.MIN_X], envelope[GeometryUtils.MAX_X],
                envelope[GeometryUtils.MIN_Y], envelope[GeometryUtils.MAX_Y]));
    }
}