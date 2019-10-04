/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.create;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.shape.random.RandomPointsBuilder;
import org.locationtech.jts.shape.random.RandomPointsInGridBuilder;

/**
 * This function is used to generate pseudo-random points until the requested
 * number are found within the input area (polygon or multipolygon)
 *
 * @author Erwan Bocher, CNRS
 */
public class ST_GeneratePointsInGrid extends DeterministicScalarFunction {

    static PointOnGeometryLocator extentLocator;

    public ST_GeneratePointsInGrid() {
        addProperty(PROP_REMARKS, "ST_GeneratePoints(Geometry geom, int cellSizeX, int cellSizeY, boolean useMask)\n"
                + " generates a regular set of points according a x and y cell sizes. \n"
                + "The usemask argument is used to keep the points loacted inside the input geometry. \n");
    }

    @Override
    public String getJavaStaticMethod() {
        return "generatePointsInGrid";
    }

    public static Geometry generatePointsInGrid(Geometry geom, int cellSizeX, int cellSizeY) throws SQLException {
        return generatePointsInGrid(geom, cellSizeX, cellSizeY, false);
    }

    /**
     * Make a regular distribution of points
     *
     * @param geom input geometry as polygon or multipolygon
     * @param cellSizeX size of the x cell
     * @param cellSizeY size of the y cell
     * @param useMask set to true to keep the points loacted inside the input
     * geometry
     * @return a regular distribution of points as multipoint
     * @throws java.sql.SQLException
     */
    public static Geometry generatePointsInGrid(Geometry geom, int cellSizeX, int cellSizeY, boolean useMask) throws SQLException {
        if (geom == null) {
            return null;
        }
        if (geom.isEmpty()) {
            return null;
        }
        if (geom instanceof Polygon || geom instanceof MultiPolygon) {
            GeometryFactory geomFact = geom.getFactory();
            List<Coordinate> geoms = createGridPoints(geom, cellSizeX, cellSizeY, useMask);

            return geomFact.createMultiPointFromCoords(geoms.toArray(new Coordinate[0]));

        } else {
            throw new SQLException("Only polygon or multipolygon is supported");
        }
    }

    /**
     * Make a regular distribution of points stored in an array
     *
     * @param geom input geometry as polygon or multipolygon
     * @param cellSizeX size of the x cell
     * @param cellSizeY size of the y cell
     * @param useMask set to true to keep the points loacted inside the input
     * geometry
     * @return
     */
    static List<Coordinate> createGridPoints(Geometry geom, int cellSizeX, int cellSizeY, boolean useMask) {
        Envelope env = geom.getEnvelopeInternal();
        int nCellsOnSideX = (int) (env.getWidth() / cellSizeX) + 1;
        int nCellsOnSideY = (int) (env.getHeight() / cellSizeY) + 1;
        List<Coordinate> geoms = new ArrayList<Coordinate>();
        double envMinX = env.getMinX() + (env.getWidth() % cellSizeX) / 2;
        double envMinY = env.getMinY() + (env.getHeight() % cellSizeY) / 2;
        if (useMask) {
            extentLocator = new IndexedPointInAreaLocator(geom);
            for (int i = 0; i < nCellsOnSideX; i++) {
                for (int j = 0; j < nCellsOnSideY; j++) {
                    Coordinate c = new Coordinate(envMinX + i * cellSizeX, envMinY + j * cellSizeY);
                    if (extentLocator.locate(c) != Location.EXTERIOR) {
                        geoms.add(c);
                    }
                }
            }
        } else {
            for (int i = 0; i < nCellsOnSideX; i++) {
                for (int j = 0; j < nCellsOnSideY; j++) {
                    geoms.add(new Coordinate(envMinX + i * cellSizeX, envMinY + j * cellSizeY));
                }
            }
        }
        return geoms;
    }
}
