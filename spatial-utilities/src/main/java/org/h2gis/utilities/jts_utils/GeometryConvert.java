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
package org.h2gis.utilities.jts_utils;

import java.util.Set;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;

/**
 * Conversion tools for JTS {@link Geometry}s.
 * 
 * @author Erwan Bocher
 */
public final class GeometryConvert extends GeometryUtils{

        /**  
         * Converts a geometry into a MultiPoint.
         * @param geometry  
         * @return MutiPoint  
         */
        public static MultiPoint toMultiPoint(Geometry geometry) {
                return FACTORY.createMultiPoint(geometry.getCoordinates());
        }

        /**  
         * Converts a geometry into a set of simple LineString (segment).
         * @param geometry  
         * @return  
         */
        public static Set<LineString> toSegmentsLineString(Geometry geometry) {
                if (geometry.getDimension() > 0) {
                        Set<LineString> segmentSet = new TreeSet<LineString>();
                        Coordinate[] coords = CoordinateArrays.removeRepeatedPoints(geometry.getCoordinates());
                        for (int j = 0; j < coords.length - 1; j++) {
                                LineString lineString = FACTORY.createLineString(new Coordinate[]{coords[j], coords[j + 1]});
                                segmentSet.add(lineString);
                        }
                        return segmentSet;
                }
                return null;
        }

        /**  
         * Converts a geometry into a set of simple LineString (segment) stored in a MultilineString.
         * @param geometry  
         * @return  
         */
        public static MultiLineString toSegmentsMultiLineString(Geometry geometry) {
                Set<LineString> result = toSegmentsLineString(geometry);
                if (result != null) {
                        return FACTORY.createMultiLineString(result.toArray(new LineString[result.size()]));
                }
                return null;
        }

        /**
         * Converts from Envelope to a polygon geometry.
         * @param envelope
         * @param factory
         * @return 
         */
        public static Geometry toGeometry(Envelope envelope, GeometryFactory factory) {
                if ((envelope.getWidth() == 0) && (envelope.getHeight() == 0)) {
                        return factory.createPoint(new Coordinate(envelope.getMinX(),
                                envelope.getMinY()));
                }

                if ((envelope.getWidth() == 0) || (envelope.getHeight() == 0)) {
                        return factory.createLineString(new Coordinate[]{
                                        new Coordinate(envelope.getMinX(), envelope.getMinY()),
                                        new Coordinate(envelope.getMaxX(), envelope.getMaxY())});
                }

                return factory.createPolygon(factory.createLinearRing(new Coordinate[]{
                                new Coordinate(envelope.getMinX(), envelope.getMinY()),
                                new Coordinate(envelope.getMinX(), envelope.getMaxY()),
                                new Coordinate(envelope.getMaxX(), envelope.getMaxY()),
                                new Coordinate(envelope.getMaxX(), envelope.getMinY()),
                                new Coordinate(envelope.getMinX(), envelope.getMinY())}), null);
        }

        /**  
         * Converts from Envelope to a polygon geometry.
         * @param envelope  
         * @return  
         */
        public static Geometry toGeometry(Envelope envelope) {
                return toGeometry(envelope, FACTORY);
        }

        /**
         * Private constructor for utility class.
         */
        private GeometryConvert() {
        }
}
