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
import org.h2gis.h2spatialext.function.spatial.edit.ST_UpdateZ.UpdateZCoordinateSequenceFilter;

/**
 * Compute the shadow footprint for a single geometry. 
 * The shadow is represented as an unified polygon.
 * 
 * The user must specified the sun position : azimuth and altitude and a height
 * to compute the shadow footprint.
 * 
 * @author Erwan Bocher
 */
public class ST_GeometryShadow extends DeterministicScalarFunction {

    public ST_GeometryShadow() {
        addProperty(PROP_REMARKS, "This function computes the shadow footprint as a polygon(s) for a LINE and a POLYGON \n"
                + "or LINE for a POINT."
                + "Avalaible arguments are :\n"
                + "(1) The geometry."
                + "(2 and 3) The position of the sun is specified with two parameters in radians : azimuth and altitude.\n"
                + "(4) The height value is used to extrude the facades of geometry.\n"
                + "Note 1: The z of the output geometry is set to 0.\n"
                + "Note 2: The azimuth is a direction along the horizon, measured from north to east.\n"
                + "The altitude is expressed above the horizon in radians, e.g. 0 at the horizon and PI/2 at the zenith.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeShadow";
    }

    /**
     * Compute the shadow footprint based on
     *
     * @param geometry input geometry
     * @param height of the geometry
     * @param azimuth of the sun in radians
     * @param altitude of the sun in radians
     * @return
     */
    public static Geometry computeShadow(Geometry geometry, double azimuth, double altitude, double height) {
        if (geometry == null) {
            return null;
        }
        if (height <= 0) {
            throw new IllegalArgumentException("The height of the geometry must be greater than 0.");
        }
        //Compute the shadow offset
        double[] shadowOffSet = shadowOffset(azimuth, altitude, height);
        if (geometry instanceof Polygon) {
            return shadowPolygon((Polygon) geometry, shadowOffSet, geometry.getFactory());
        } else if (geometry instanceof LineString) {
            return shadowLine((LineString) geometry, shadowOffSet, geometry.getFactory());
        } else if (geometry instanceof Point) {
            return shadowPoint((Point) geometry, shadowOffSet, geometry.getFactory());
        } else {
            throw new IllegalArgumentException("The shadow function supports only single geometry POINT, LINE or POLYGON.");
        }
    }

    /**
     * Compute the shadow for a linestring
     *
     * @param lineString the input linestring
     * @param shadowOffset computed according the sun position and the height of
     * the geometry
     * @return
     */
    private static Geometry shadowLine(LineString lineString, double[] shadowOffset, GeometryFactory factory) {
        Coordinate[] coords = lineString.getCoordinates();
        Collection<Polygon> shadows = new ArrayList<Polygon>();
        createShadowPolygons(shadows, coords, shadowOffset, factory);
        if (!shadows.isEmpty()) {
            CascadedPolygonUnion union = new CascadedPolygonUnion(shadows);
            Geometry result = union.union();
            result.apply(new UpdateZCoordinateSequenceFilter(0, 1));
            return result;
        }
        return null;
    }

    /**
     * Compute the shadow for a polygon
     *
     * @param polygon the input polygon
     * @param shadowOffset computed according the sun position and the height of
     * the geometry
     * @return
     */
    private static Geometry shadowPolygon(Polygon polygon, double[] shadowOffset, GeometryFactory factory) {
        Coordinate[] shellCoords = polygon.getExteriorRing().getCoordinates();
        Collection<Polygon> shadows = new ArrayList<Polygon>();
        createShadowPolygons(shadows, shellCoords, shadowOffset, factory);
        final int nbOfHoles = polygon.getNumInteriorRing();
        for (int i = 0; i < nbOfHoles; i++) {
            createShadowPolygons(shadows, polygon.getInteriorRingN(i).getCoordinates(), shadowOffset, factory);
        }
        shadows.add(polygon);
        if (!shadows.isEmpty()) {
            CascadedPolygonUnion union = new CascadedPolygonUnion(shadows);
            Geometry geomUnion = union.union();
            Geometry diff = geomUnion.difference(polygon);
            diff.apply(new UpdateZCoordinateSequenceFilter(0, 1));
            return diff;
        }
        return null;
    }

    /**
     * Compute the shadow for a point
     *
     * @param point the input point
     * @param shadowOffset computed according the sun position and the height of
     * the geometry
     * @return
     */
    private static Geometry shadowPoint(Point point, double[] shadowOffset, GeometryFactory factory) {
        Coordinate startCoord = point.getCoordinate();
        Coordinate offset = moveCoordinate(startCoord, shadowOffset);
        if (offset.distance(point.getCoordinate()) < 10E-3) {
            return point;
        } else {
            startCoord.z = 0;
            return factory.createLineString(new Coordinate[]{startCoord, offset});
        }
    }

    /**
     * Return the shadow offset in X and Y directions
     *
     * @param azimuth in radians from north.
     * @param altitude in radians from east.
     * @param height of the geometry
     * @return
     */
    public static double[] shadowOffset(double azimuth, double altitude, double height) {
        double spread = 1 / Math.tan(altitude);
        return new double[]{-height * spread * Math.sin(azimuth), -height * spread * Math.cos(azimuth)};
    }

    /**
     * Move the input coordinate according X and Y offset
     *
     * @param inputCoordinate
     * @param shadowOffset
     * @return
     */
    private static Coordinate moveCoordinate(Coordinate inputCoordinate, double[] shadowOffset) {
        return new Coordinate(inputCoordinate.x + shadowOffset[0], inputCoordinate.y + shadowOffset[1], 0);
    }

    /**
     * Create and collect shadow polygons.
     *
     * @param shadows
     * @param coordinates
     * @param shadow
     * @param factory
     */
    private static void createShadowPolygons(Collection<Polygon> shadows, Coordinate[] coordinates, double[] shadow, GeometryFactory factory) {
        for (int i = 1; i < coordinates.length; i++) {
            Coordinate startCoord = coordinates[i - 1];
            Coordinate endCoord = coordinates[i];
            Coordinate nextEnd = moveCoordinate(endCoord, shadow);
            Coordinate nextStart = moveCoordinate(startCoord, shadow);
            Coordinate start = new Coordinate(startCoord.x, startCoord.y, 0);
            Coordinate end = new Coordinate(endCoord.x, endCoord.y, 0);
            Polygon polygon = factory.createPolygon(new Coordinate[]{start,
                end, nextEnd,
                nextStart, start});
            if (polygon.isValid()) {
                shadows.add(polygon);
            }
        }
    }
}
