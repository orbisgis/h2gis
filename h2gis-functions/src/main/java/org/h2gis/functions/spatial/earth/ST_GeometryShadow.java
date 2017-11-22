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

package org.h2gis.functions.spatial.earth;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import java.util.ArrayList;
import java.util.Collection;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.functions.spatial.edit.ST_UpdateZ.UpdateZCoordinateSequenceFilter;

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
                + "(5) Optional parameter to unified or not the shadow polygons. True is the default value.\n"
                + "Note 1: The z of the output geometry is set to 0.\n"
                + "Note 2: The azimuth is a direction along the horizon, measured from north to east.\n"
                + "The altitude is expressed above the horizon in radians, e.g. 0 at the horizon and PI/2 at the zenith.\n"
                + "The user can set the azimut and the altitude using a point see ST_SunPosition function,\n"
                + "the folowing signature must be used ST_GeometryShadow(INPUT_GEOM,ST_SUNPosition(), HEIGHT).");
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeShadow";
    }
    
    /**
     * Compute the shadow footprint based on
     * 
     * @param geometry input geometry
     * @param sunPosition as a point where x = azimuth and y=altitude
     * @param height of the geometry
     * @return 
     */
    public static Geometry computeShadow(Geometry geometry, Geometry sunPosition, double height) {
        if (sunPosition != null) {
            if (sunPosition instanceof Point) {
                return computeShadow(geometry, sunPosition.getCoordinate().x, sunPosition.getCoordinate().y, height, true);
            }
            throw new IllegalArgumentException("The sun position must be stored in a point with \n"
                    + "x = sun azimuth in radians (direction along the horizon, measured from north to\n"
                    + "east and y = sun altitude above the horizon in radians.");
        }
        return null;
    }
    
    /**
     * Compute the shadow footprint based on
     * 
     * @param geometry input geometry
     * @param azimuth of the sun in radians
     * @param altitude of the sun in radians
     * @param height of the geometry
     * @return 
     */
    public static Geometry computeShadow(Geometry geometry, double azimuth, double altitude, double height) {
        return computeShadow(geometry, azimuth, altitude, height, true);
    }

    /**
     * Compute the shadow footprint based on
     *
     * @param geometry input geometry
     * @param azimuth of the sun in radians
     * @param altitude of the sun in radians
     * @param height of the geometry
     * @param doUnion unified or not the polygon shadows
     * @return
     */
    public static Geometry computeShadow(Geometry geometry, double azimuth, double altitude, double height, boolean doUnion) {
        if (geometry == null) {
            return null;
        }
        if (height <= 0) {
            throw new IllegalArgumentException("The height of the geometry must be greater than 0.");
        }
        //Compute the shadow offset
        double[] shadowOffSet = shadowOffset(azimuth, altitude, height);
        if (geometry instanceof Polygon) {
            return shadowPolygon((Polygon) geometry, shadowOffSet, geometry.getFactory(), doUnion);
        } else if (geometry instanceof LineString) {
            return shadowLine((LineString) geometry, shadowOffSet, geometry.getFactory(), doUnion);
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
    private static Geometry shadowLine(LineString lineString, double[] shadowOffset, GeometryFactory factory, boolean doUnion) {
        Coordinate[] coords = lineString.getCoordinates();
        Collection<Polygon> shadows = new ArrayList<Polygon>();
        createShadowPolygons(shadows, coords, shadowOffset, factory);
        if (!doUnion) {
            return factory.buildGeometry(shadows);
        } else {
            if (!shadows.isEmpty()) {
                CascadedPolygonUnion union = new CascadedPolygonUnion(shadows);
                Geometry result = union.union();
                result.apply(new UpdateZCoordinateSequenceFilter(0, 1));
                return result;
            }
            return null;
        }
    }

    /**
     * Compute the shadow for a polygon
     *
     * @param polygon the input polygon
     * @param shadowOffset computed according the sun position and the height of
     * the geometry
     * @return
     */
    private static Geometry shadowPolygon(Polygon polygon, double[] shadowOffset, GeometryFactory factory, boolean doUnion) {
        Coordinate[] shellCoords = polygon.getExteriorRing().getCoordinates();
        Collection<Polygon> shadows = new ArrayList<Polygon>();
        createShadowPolygons(shadows, shellCoords, shadowOffset, factory);
        final int nbOfHoles = polygon.getNumInteriorRing();
        for (int i = 0; i < nbOfHoles; i++) {
            createShadowPolygons(shadows, polygon.getInteriorRingN(i).getCoordinates(), shadowOffset, factory);
        }
        if (!doUnion) {
            return factory.buildGeometry(shadows);
        } else {
            if (!shadows.isEmpty()) {
                Collection<Geometry> shadowParts = new ArrayList<Geometry>();
                for (Polygon shadowPolygon : shadows) {
                    shadowParts.add(shadowPolygon.difference(polygon));
                }
                Geometry allShadowParts = factory.buildGeometry(shadowParts);
                Geometry union = allShadowParts.buffer(0);
                union.apply(new UpdateZCoordinateSequenceFilter(0, 1));
                return union;
            }
            return null;
        }
        
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
            Polygon polygon = factory.createPolygon(new Coordinate[]{startCoord,
                endCoord, nextEnd,
                nextStart, startCoord});
            if (polygon.isValid()) {                
                shadows.add(polygon);
            }
        }
    }
}
