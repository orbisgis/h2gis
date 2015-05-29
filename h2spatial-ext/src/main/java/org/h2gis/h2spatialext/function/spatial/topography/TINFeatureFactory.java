/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatialext.function.spatial.topography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Triangle;

/**
 * A factory used to create jDelaunay objects from JTS geometries.
 *
 * @author Erwan Bocher
 */
public final class TINFeatureFactory {
    public static final double EPSILON = 1e-12;

        /**
         * We don't want to instantiate any TINFeatureFactory.
         */
        private TINFeatureFactory() {
        }

        /**
         * A factory to create a DTriangle from a Geometry
         *
         * @param geom
         * @return
         * @throws DelaunayError
         * If the triangle can't be generated
         * @throws IllegalArgumentException
         * If there are not exactly 3 coordinates in geom.
         */
        public static Triangle createTriangle(Geometry geom) throws IllegalArgumentException {

                Coordinate[] coordinates = geom.getCoordinates();
                if (coordinates.length != 4) {
                        throw new IllegalArgumentException("The geometry must be a triangle");
                }
                return new Triangle(coordinates[0],coordinates[1],coordinates[2]);
        }

}