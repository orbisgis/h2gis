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
 * Compute the shadow footprint for a single geometry. 
 * The shadow is represented as a set of polygons or a unified one.
 * 
 * The user must specified the sun position : azimuth and altitude and a height
 * to compute the shadow footprint.
 * 
 * @author Erwan Bocher
 */
public class ST_GeometryShadow extends DeterministicScalarFunction {

    private static double[] shadowOffSet;
    private static boolean shadowOffSetComputed = false;

    public ST_GeometryShadow() {
        addProperty(PROP_REMARKS, "This function computes the shadow footprint as a polygon(s) for a LINE and a POLYGON \n"
                + "or LINE for a POINT."
                + "The z of the input geometry is set to 0. A height value is used to extrude the facades of geometry.\n"
                + "The position of the sun is specified with two parameters in radians : azimuth and altitude.\n"
                +" Note : The last boolean argument could be used to unified or not the polygon shadows.");
    }
    
    //Attention l'altitude est exprimée entre 0 et 90 °. De l'horizon vers le zenith

    @Override
    public String getJavaStaticMethod() {
        return "computeShadow";
    }

    /**
     * Compute the shadow footprint based on     
     * @param geometry input geometry
     * @param height of the geometry
     * @param azimuth of the sun in radians
     * @param altitude of the sun in radians
     * @return 
     */
    public static Geometry computeShadow(Geometry geometry, double height, double azimuth, double altitude) {
        return computeShadow(geometry, height, azimuth, altitude, false);

    }

    /**
     * Compute the shadow footprint based on
     * @param geometry input geometry
     * @param height of the geometry
     * @param azimuth of the sun in radians
     * @param altitude of the sun in radians
     * @param useZ specify if the z coordinates of the geometry will be used to compute shadow.
     * 
     * @return 
     */
   public static Geometry computeShadow(Geometry geometry, double height, double azimuth, double altitude, boolean useZ) {
        return computeShadow(geometry, height, azimuth, altitude, useZ, true);
    }
   
   
    /**
     * Compute the shadow footprint based on
     * @param geometry input geometry
     * @param height of the geometry
     * @param azimuth of the sun in radians
     * @param altitude of the sun in radians
     * @param doUnion unioning or not the shadows
     * @return 
     */
   public static Geometry computeShadow(Geometry geometry, double height, double azimuth, double altitude, boolean useZ, boolean doUnion) {
       if (geometry == null) {
           return null;
       }


        if (!shadowOffSetComputed) {
            shadowOffSet = shadowOffset(azimuth, altitude);
            shadowOffSetComputed = true;
        }

        if (geometry instanceof Polygon) {
            return shadowPolygon((Polygon) geometry, shadowOffSet, geometry.getFactory(), height, useZ, doUnion);
        } else if (geometry instanceof LineString) {
            return shadowLine((LineString) geometry, shadowOffSet, geometry.getFactory(), height, useZ,doUnion);
        } else if (geometry instanceof Point) {
            return shadowPoint((Point) geometry, shadowOffSet, geometry.getFactory(),height, useZ);
        } else {
            throw new IllegalArgumentException("The shadow function supports only single geometry POINT, LINE or POLYGON.");
        }
   }
    
    /**
     * Compute the shadow for a linestring
     * @param lineString
     * @param height
     * @param date
     * @return
     */
    private static Geometry shadowLine(LineString lineString, double[] shadowOffset, GeometryFactory factory, double height, boolean useZ,boolean doUnion) {
        Coordinate[] coords = lineString.getCoordinates();
        Collection<Polygon> shadows = new ArrayList<Polygon>();
        createShadowPolygons(shadows, coords, shadowOffset, height, useZ, factory);
        if (doUnion) {
            CascadedPolygonUnion union = new CascadedPolygonUnion(shadows);
            return union.union();
        }
        return factory.buildGeometry(shadows);
    }

    /**
     * Compute the shadow for a polygon
     * @param polygon
     * @param shadowOffset
     * @param factory
     * @param doUnion
     * @return
     */
    private static Geometry shadowPolygon(Polygon polygon, double[] shadowOffset, GeometryFactory factory,double height,boolean useZ, boolean doUnion) {
        Coordinate[] shellCoords = polygon.getExteriorRing().getCoordinates();
        Collection<Polygon> shadows = new ArrayList<Polygon>();
        createShadowPolygons(shadows, shellCoords, shadowOffset,height, useZ, factory);
        final int nbOfHoles = polygon.getNumInteriorRing();
        for (int i = 0; i < nbOfHoles; i++) {
            createShadowPolygons(shadows, polygon.getInteriorRingN(i).getCoordinates(), shadowOffset,height, useZ,factory);
        }
        shadows.add(polygon);
        if (doUnion) {
            CascadedPolygonUnion union = new CascadedPolygonUnion(shadows);
            return union.union();
        }
        return factory.buildGeometry(shadows);
    }

    /**
     * Compute the shadow for a point
     * @param point
     * @param shadowOffset
     * @param factory
     * @return
     */
    private static Geometry shadowPoint(Point point, double[] shadowOffset, GeometryFactory factory,double height, boolean useZ) {
        Coordinate offset = moveCoordinate(point.getCoordinate(), shadowOffset, height, useZ);
        if (offset.distance(point.getCoordinate()) < 10E-3) {
            return point;
        } else {
            return factory.createLineString(new Coordinate[]{point.getCoordinate(), offset});
        }
    }

    /**
     * Return the shadow offset in X and Y directions
     *
     * @param sunPosition
     * @return
     */
    public static double[] shadowOffset(Coordinate sunPosition) {
        return shadowOffset(sunPosition.x, sunPosition.y);
    }

    /**
     * Return the shadow offset in X and Y directions
     *
     * @param azimuth in radians from north.
     * @param altitude in radians from east.
     * @return
     */
    public static double[] shadowOffset(double azimuth, double altitude) {
        double spread = 1 / Math.tan(altitude);
        //return new double[]{-height * spread * Math.sin(azimuth), -height * spread * Math.cos(azimuth)};
        return new double[]{spread * Math.sin(azimuth), spread * Math.cos(azimuth)};
    }

    /**
     * Move the coordinate according X and Y offset
     * @param inputCoordinate
     * @param shadowOffset
     * @return
     */
    private static Coordinate moveCoordinate(Coordinate inputCoordinate, double[] shadowOffset,double height, boolean useZ) {
        double zCoordinate = 0;
        if(useZ){
            zCoordinate = inputCoordinate.z;
        }
        return new Coordinate(inputCoordinate.x + (-(zCoordinate+height)* shadowOffset[0]), inputCoordinate.y + (-(zCoordinate+height) * shadowOffset[1]));
    }

    /**
     * Create and collect shadow polygons.
     * @param shadows
     * @param coordinates
     * @param shadow
     * @param factory
     */
    private static void createShadowPolygons(Collection<Polygon> shadows, Coordinate[] coordinates, double[] shadow, double height,boolean useZ, GeometryFactory factory) {
        for (int i = 1; i < coordinates.length; i++) {
            Coordinate startCoord = coordinates[i - 1];
            Coordinate endCoord = coordinates[i];
            shadows.add(factory.createPolygon(new Coordinate[]{startCoord, endCoord, moveCoordinate(endCoord, shadow,height, useZ),
                moveCoordinate(startCoord, shadow,height, useZ), startCoord}));
        }
    }

}
