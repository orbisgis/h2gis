/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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

package org.h2gis.h2spatialext.function.spatial.earth;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import java.util.ArrayList;
import java.util.Collection;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_Shadow extends DeterministicScalarFunction {

    private static double[] shadowOffSet;
    private static boolean shadowOffSetComputed = false;

    @Override
    public String getJavaStaticMethod() {
        return "computeShadow";
    }

    public static Geometry computeShadow(Geometry geometry, double height, Geometry sunPosition) {
        if (geometry == null || sunPosition == null) {
            return null;
        }
        if (height <= 0) {
            throw new IllegalArgumentException("The height value must be greater than 0.");
        }
        if (sunPosition instanceof Point) {
            if (!shadowOffSetComputed) {
                shadowOffSet = shadowOffset(sunPosition.getCoordinate(), height);
                shadowOffSetComputed = true;
            }

            if (geometry instanceof Polygon) {
                return shadowPolygon((Polygon) geometry, shadowOffSet, geometry.getFactory());
            } else if (geometry instanceof LineString) {
                return shadowLine((LineString) geometry, shadowOffSet, geometry.getFactory());
            } else if (geometry instanceof Point) {
                return shadowPoint((Point) geometry, shadowOffSet, geometry.getFactory());
            } else {
                return null;
            }
        } else {
            throw new IllegalArgumentException("The sun position must be stored as a Point.");
        }
    }

    /**
     *
     * @param lineString
     * @param height
     * @param date
     * @return
     */
    private static Geometry shadowLine(LineString lineString, double[] shadowOffset, GeometryFactory factory) {
        Coordinate[] coords = lineString.getCoordinates();
        Collection<Polygon> shadows = new ArrayList<Polygon>();
        createShadowPolygons(shadows, coords, shadowOffset, factory);
        CascadedPolygonUnion union = new CascadedPolygonUnion(shadows);
        return union.union();
    }

    /**
     *
     * @param inputCoordinate
     * @param shadowOffset
     * @return
     */
    private static Coordinate moveCoordinate(Coordinate inputCoordinate, double[] shadowOffset) {
        return new Coordinate(inputCoordinate.x + shadowOffset[0], inputCoordinate.y + shadowOffset[1]);
    }

    /**
     * Return the shadow offset in X and Y directions
     *
     * @param sunPosition
     * @param height
     * @return
     */
    public static double[] shadowOffset(Coordinate sunPosition, double height) {
        double spread = 1 / Math.tan(sunPosition.y);
        return new double[]{-height * spread * Math.sin(sunPosition.x), -height * spread * Math.cos(sunPosition.x)};
    }

    private static Geometry shadowPolygon(Polygon polygon, double[] shadowOffset, GeometryFactory factory) {
        Coordinate[] shellCoords = polygon.getExteriorRing().getCoordinates();
        Collection<Polygon> shadows = new ArrayList<Polygon>();
        createShadowPolygons(shadows, shellCoords, shadowOffset, factory);
        final int nbOfHoles = polygon.getNumInteriorRing();
        for (int i = 0; i < nbOfHoles; i++) {
            createShadowPolygons(shadows, polygon.getInteriorRingN(i).getCoordinates(), shadowOffset, factory);
        }
        shadows.add(polygon);
        CascadedPolygonUnion union = new CascadedPolygonUnion(shadows);
        return union.union();
    }

    private static void createShadowPolygons(Collection<Polygon> shadows, Coordinate[] coordinates, double[] shadow, GeometryFactory factory) {
        for (int i = 1; i < coordinates.length; i++) {
            Coordinate startCoord = coordinates[i - 1];
            Coordinate endCoord = coordinates[i];
            shadows.add(factory.createPolygon(new Coordinate[]{startCoord, endCoord, moveCoordinate(endCoord, shadow),
                moveCoordinate(startCoord, shadow), startCoord}));
        }
    }

    private static Geometry shadowPoint(Point point, double[] shadowOffset, GeometryFactory factory) {
        Coordinate offset = moveCoordinate(point.getCoordinate(), shadowOffset);
        if (offset.distance(point.getCoordinate()) < 10E-3) {
            return point;
        } else {
            return factory.createLineString(new Coordinate[]{point.getCoordinate(), offset});
        }
    }
}
