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

package org.h2gis.functions.io.shp.internal;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.h2gis.drivers.utility.CoordinatesUtils;
import org.h2gis.functions.io.utility.ReadBufferManager;
import org.h2gis.functions.io.utility.WriteBufferManager;

import java.io.IOException;

/**
 *
 * @author aaime
 * @author Ian Schneider
 * @see "http://svn.geotools.org/geotools/tags/2.3.1/plugin/shapefile/src/org/geotools/data/shapefile/shp/MultiPointHandler.java"
 *
 */
public class MultiPointHandler implements ShapeHandler {

        final ShapeType shapeType;
        GeometryFactory geometryFactory = new GeometryFactory();

        /** Creates new MultiPointHandler */
        public MultiPointHandler() {
                shapeType = ShapeType.POINT;
        }

        public MultiPointHandler(ShapeType type) throws ShapefileException {
                if ((type != ShapeType.MULTIPOINT) && (type != ShapeType.MULTIPOINTM) && (type != ShapeType.MULTIPOINTZ)) {
                        throw new ShapefileException(
                                "Multipointhandler constructor - expected type to be 8, 18, or 28");
                }

                shapeType = type;
        }

        /**
         * Returns the shapefile shape type value for a point
         * @return int Shapefile.POINT
         */
        @Override
        public ShapeType getShapeType() {
                return shapeType;
        }

        /**
         * Calcuates the record length of this object.
         * @return int The length of the record that this shapepoint will take up in a shapefile
         **/
        @Override
        public int getLength(Object geometry) {
                MultiPoint mp = (MultiPoint) geometry;

                int length;

                if (shapeType == ShapeType.MULTIPOINT) {
                        // two doubles per coord (16 * numgeoms) + 40 for header
                        length = (mp.getNumGeometries() * 16) + 40;
                } else if (shapeType == ShapeType.MULTIPOINTM) {
                        // add the additional MMin, MMax for 16, then 8 per measure
                        length = (mp.getNumGeometries() * 16) + 40 + 16 + (8 * mp.getNumGeometries());
                } else if (shapeType == ShapeType.MULTIPOINTZ) {
                        // add the additional ZMin,ZMax, plus 8 per Z
                        length = (mp.getNumGeometries() * 16) + 40 + 16 + (8 * mp.getNumGeometries()) + 16
                                + (8 * mp.getNumGeometries());
                } else {
                        throw new IllegalStateException("Expected ShapeType of Arc, got " + shapeType);
                }

                return length;
        }

        @Override
        public Geometry read(ReadBufferManager buffer, ShapeType type) throws IOException {
                if (type == ShapeType.NULL) {
                        return null;
                }

                //read bounding box (not needed)
                buffer.skip(4 * 8);

                int numpoints = buffer.getInt();
                Coordinate[] coords = new Coordinate[numpoints];

                for (int t = 0; t < numpoints; t++) {
                        double x = buffer.getDouble();
                        double y = buffer.getDouble();
                        coords[t] = new Coordinate(x, y);
                }

                if (shapeType == ShapeType.MULTIPOINTZ) {
                        buffer.skip(2 * 8);

                        for (int t = 0; t < numpoints; t++) {
                                coords[t].z = buffer.getDouble(); //z
                        }
                }

                return geometryFactory.createMultiPoint(coords);
        }

        @Override
        public void write(WriteBufferManager buffer, Object geometry) throws IOException {
                MultiPoint mp = (MultiPoint) geometry;

                Envelope box = mp.getEnvelopeInternal();
                buffer.putDouble(box.getMinX());
                buffer.putDouble(box.getMinY());
                buffer.putDouble(box.getMaxX());
                buffer.putDouble(box.getMaxY());


                buffer.putInt(mp.getNumGeometries());


                for (int t = 0, tt = mp.getNumGeometries(); t < tt; t++) {
                        Coordinate c = (mp.getGeometryN(t)).getCoordinate();
                        buffer.putDouble(c.x);
                        buffer.putDouble(c.y);
                }


                if (shapeType == ShapeType.MULTIPOINTZ) {
                        double[] zExtreame = CoordinatesUtils.zMinMax(mp.getCoordinates());

                        if (Double.isNaN(zExtreame[0])) {
                                buffer.putDouble(0.0);
                                buffer.putDouble(0.0);
                        } else {
                                buffer.putDouble(zExtreame[0]);
                                buffer.putDouble(zExtreame[1]);
                        }


                        for (int t = 0; t < mp.getNumGeometries(); t++) {
                                Coordinate c = (mp.getGeometryN(t)).getCoordinate();
                                double z = c.z;

                                if (Double.isNaN(z)) {
                                        buffer.putDouble(0.0);
                                } else {
                                        buffer.putDouble(z);
                                }
                        }
                }



                if (shapeType == ShapeType.MULTIPOINTM || shapeType == ShapeType.MULTIPOINTZ) {
                        buffer.putDouble(-10E40);
                        buffer.putDouble(-10E40);


                        for (int t = 0; t < mp.getNumGeometries(); t++) {
                                buffer.putDouble(-10E40);
                        }
                }
        }
}
