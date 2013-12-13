/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.topography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.geometries.DEdge;
import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;

/**
 * A factory used to create jDelaunay objects from JTS geometries.
 *
 * @author Erwan Bocher
 */
public final class TINFeatureFactory {

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
        public static DTriangle createDTriangle(Geometry geom) throws DelaunayError {

                Coordinate[] coords = geom.getCoordinates();
                if (coords.length != 4) {
                        throw new IllegalArgumentException("The geometry must be a triangle");
                }
                return new DTriangle(new DEdge(coords[0].x, coords[0].y, coords[0].z, coords[1].x, coords[1].y, coords[1].z),
                        new DEdge(coords[1].x, coords[1].y, coords[1].z, coords[2].x, coords[2].y, coords[2].z),
                        new DEdge(coords[2].x, coords[2].y, coords[2].z, coords[0].x, coords[0].y, coords[0].z));
        }

        /**
         * A factory to create a DPoint from a Geometry
         *
         * @param geom
         * @return
         * @throws DelaunayError
         * If the DPoint can't be generated. In most cases, it means that
         * the input point has a Double.NaN coordinate.
         * @throws IllegalArgumentException
         * If there are not exactly 1 coordinates in geom.
         */
        public static DPoint createDPoint(Geometry geom) throws DelaunayError {
                Coordinate[] coords = geom.getCoordinates();
                if (coords.length != 1) {
                        throw new IllegalArgumentException("The geometry must be a point");
                }
                return new DPoint(coords[0]);
        }

        /**
         * Tries to create a DEdge from the given geometry, which is supposed to be formed
         * of exactly two coordinates.
         *
         * @param geom
         * @return
         * @throws DelaunayError
         * @throws IllegalArgumentException
         * If there are not exactly 2 coordinates in geom.
         */
        public static DEdge createDEdge(Geometry geom) throws DelaunayError {
                Coordinate[] coords = geom.getCoordinates();
                if (coords.length != 2) {
                        throw new IllegalArgumentException("the geometry is supposed to be line with two points.");
                }
                return new DEdge(new DPoint(coords[0]), new DPoint(coords[1]));
        }

        /**
         * A factory to create a DPoint from a Coordinate.
         *
         * @param coord
         * @return
         * @throws DelaunayError if one of the coordinate values is Double.Nan
         */
        public static DPoint createDPoint(Coordinate coord) throws DelaunayError {
                return new DPoint(coord);
        }
}