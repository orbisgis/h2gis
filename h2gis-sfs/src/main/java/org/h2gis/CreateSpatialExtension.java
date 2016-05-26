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

package org.h2gis;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.drivers.DriverManager;
import org.h2gis.drivers.dbf.DBFRead;
import org.h2gis.drivers.dbf.DBFWrite;
import org.h2gis.drivers.geojson.GeoJsonRead;
import org.h2gis.drivers.geojson.GeoJsonWrite;
import org.h2gis.drivers.geojson.ST_AsGeoJSON;
import org.h2gis.drivers.geojson.ST_GeomFromGeoJSON;
import org.h2gis.drivers.gpx.GPXRead;
import org.h2gis.drivers.kml.KMLWrite;
import org.h2gis.drivers.kml.ST_AsKml;
import org.h2gis.drivers.osm.OSMRead;
import org.h2gis.drivers.osm.ST_OSMDownloader;
import org.h2gis.drivers.shp.SHPRead;
import org.h2gis.drivers.shp.SHPWrite;
import org.h2gis.drivers.tsv.TSVRead;
import org.h2gis.drivers.tsv.TSVWrite;
import org.h2gis.api.Function;
import org.h2gis.ext.functions.spatial.affine_transformations.ST_Rotate;
import org.h2gis.ext.functions.spatial.affine_transformations.ST_Scale;
import org.h2gis.ext.functions.spatial.affine_transformations.ST_Translate;
import org.h2gis.ext.functions.spatial.convert.ST_AsGML;
import org.h2gis.ext.functions.spatial.convert.ST_Force2D;
import org.h2gis.ext.functions.spatial.convert.ST_Force3D;
import org.h2gis.ext.functions.spatial.convert.ST_GeomFromGML;
import org.h2gis.ext.functions.spatial.convert.ST_GoogleMapLink;
import org.h2gis.ext.functions.spatial.convert.ST_Holes;
import org.h2gis.ext.functions.spatial.convert.ST_OSMMapLink;
import org.h2gis.ext.functions.spatial.convert.ST_ToMultiLine;
import org.h2gis.ext.functions.spatial.convert.ST_ToMultiPoint;
import org.h2gis.ext.functions.spatial.convert.ST_ToMultiSegments;
import org.h2gis.ext.functions.spatial.create.ST_BoundingCircle;
import org.h2gis.ext.functions.spatial.create.ST_BoundingCircleCenter;
import org.h2gis.ext.functions.spatial.create.ST_Expand;
import org.h2gis.ext.functions.spatial.create.ST_Extrude;
import org.h2gis.ext.functions.spatial.create.ST_MakeEllipse;
import org.h2gis.ext.functions.spatial.create.ST_MakeEnvelope;
import org.h2gis.ext.functions.spatial.create.ST_MakeGrid;
import org.h2gis.ext.functions.spatial.create.ST_MakeGridPoints;
import org.h2gis.ext.functions.spatial.create.ST_MakeLine;
import org.h2gis.ext.functions.spatial.create.ST_MakePoint;
import org.h2gis.ext.functions.spatial.create.ST_MakePolygon;
import org.h2gis.ext.functions.spatial.create.ST_MinimumBoundingCircle;
import org.h2gis.ext.functions.spatial.create.ST_MinimumRectangle;
import org.h2gis.ext.functions.spatial.create.ST_OctogonalEnvelope;
import org.h2gis.ext.functions.spatial.create.ST_RingBuffer;
import org.h2gis.ext.functions.spatial.distance.ST_ClosestCoordinate;
import org.h2gis.ext.functions.spatial.distance.ST_ClosestPoint;
import org.h2gis.ext.functions.spatial.distance.ST_FurthestCoordinate;
import org.h2gis.ext.functions.spatial.distance.ST_LocateAlong;
import org.h2gis.ext.functions.spatial.distance.ST_LongestLine;
import org.h2gis.ext.functions.spatial.distance.ST_MaxDistance;
import org.h2gis.ext.functions.spatial.distance.ST_ProjectPoint;
import org.h2gis.ext.functions.spatial.earth.ST_GeometryShadow;
import org.h2gis.ext.functions.spatial.earth.ST_SunPosition;
import org.h2gis.ext.functions.spatial.edit.ST_AddPoint;
import org.h2gis.ext.functions.spatial.edit.ST_AddZ;
import org.h2gis.ext.functions.spatial.edit.ST_CollectionExtract;
import org.h2gis.ext.functions.spatial.edit.ST_Densify;
import org.h2gis.ext.functions.spatial.edit.ST_FlipCoordinates;
import org.h2gis.ext.functions.spatial.edit.ST_Interpolate3DLine;
import org.h2gis.ext.functions.spatial.edit.ST_MultiplyZ;
import org.h2gis.ext.functions.spatial.edit.ST_Normalize;
import org.h2gis.ext.functions.spatial.edit.ST_RemoveHoles;
import org.h2gis.ext.functions.spatial.edit.ST_RemovePoints;
import org.h2gis.ext.functions.spatial.edit.ST_RemoveRepeatedPoints;
import org.h2gis.ext.functions.spatial.edit.ST_Reverse;
import org.h2gis.ext.functions.spatial.edit.ST_Reverse3DLine;
import org.h2gis.ext.functions.spatial.edit.ST_UpdateZ;
import org.h2gis.ext.functions.spatial.edit.ST_ZUpdateLineExtremities;
import org.h2gis.ext.functions.spatial.mesh.ST_ConstrainedDelaunay;
import org.h2gis.ext.functions.spatial.mesh.ST_Delaunay;
import org.h2gis.ext.functions.spatial.mesh.ST_Tessellate;
import org.h2gis.ext.functions.spatial.mesh.ST_Voronoi;
import org.h2gis.ext.functions.spatial.predicates.ST_Covers;
import org.h2gis.ext.functions.spatial.predicates.ST_DWithin;
import org.h2gis.ext.functions.spatial.processing.ST_LineIntersector;
import org.h2gis.ext.functions.spatial.processing.ST_LineMerge;
import org.h2gis.ext.functions.spatial.processing.ST_OffSetCurve;
import org.h2gis.ext.functions.spatial.processing.ST_Polygonize;
import org.h2gis.ext.functions.spatial.processing.ST_PrecisionReducer;
import org.h2gis.ext.functions.spatial.processing.ST_RingSideBuffer;
import org.h2gis.ext.functions.spatial.processing.ST_SideBuffer;
import org.h2gis.ext.functions.spatial.processing.ST_Simplify;
import org.h2gis.ext.functions.spatial.processing.ST_SimplifyPreserveTopology;
import org.h2gis.ext.functions.spatial.processing.ST_Snap;
import org.h2gis.ext.functions.spatial.processing.ST_Split;
import org.h2gis.ext.functions.spatial.properties.ST_3DArea;
import org.h2gis.ext.functions.spatial.properties.ST_3DLength;
import org.h2gis.ext.functions.spatial.properties.ST_3DPerimeter;
import org.h2gis.ext.functions.spatial.properties.ST_CompactnessRatio;
import org.h2gis.ext.functions.spatial.properties.ST_Explode;
import org.h2gis.ext.functions.spatial.properties.ST_Extent;
import org.h2gis.ext.functions.spatial.properties.ST_IsRectangle;
import org.h2gis.ext.functions.spatial.properties.ST_IsValid;
import org.h2gis.ext.functions.spatial.properties.ST_IsValidDetail;
import org.h2gis.ext.functions.spatial.properties.ST_IsValidReason;
import org.h2gis.ext.functions.spatial.properties.ST_MinimumDiameter;
import org.h2gis.ext.functions.spatial.properties.ST_NPoints;
import org.h2gis.ext.functions.spatial.properties.ST_Perimeter;
import org.h2gis.ext.functions.spatial.properties.ST_XMax;
import org.h2gis.ext.functions.spatial.properties.ST_XMin;
import org.h2gis.ext.functions.spatial.properties.ST_YMax;
import org.h2gis.ext.functions.spatial.properties.ST_YMin;
import org.h2gis.ext.functions.spatial.properties.ST_ZMax;
import org.h2gis.ext.functions.spatial.properties.ST_ZMin;
import org.h2gis.ext.functions.spatial.topography.ST_TriangleAspect;
import org.h2gis.ext.functions.spatial.topography.ST_TriangleContouring;
import org.h2gis.ext.functions.spatial.topography.ST_TriangleDirection;
import org.h2gis.ext.functions.spatial.topography.ST_TriangleSlope;
import org.h2gis.ext.functions.spatial.trigonometry.ST_Azimuth;
import org.h2gis.ext.functions.system.DoubleRange;
import org.h2gis.ext.functions.system.IntegerRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Registers the SQL functions contained in h2spatial-ext.
 *
 * @author Nicolas Fortin
 * @author Adam Gouge
 */
public class CreateSpatialExtension {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateSpatialExtension.class);

    /**
     * @return instance of all built-ins functions
     * @throws java.sql.SQLException
     */
    public static Function[] getBuiltInsFunctions() throws SQLException {
        return new Function[] {
                new DBFRead(),
                new DBFWrite(),
                new DriverManager(),
                new GPXRead(),
                new GeoJsonRead(),
                new GeoJsonWrite(),
                new KMLWrite(),
                new SHPRead(),
                new SHPWrite(),
                new ST_3DLength(),
                new ST_AddPoint(),
                new ST_AddZ(),
                new ST_AsGeoJSON(),
                new ST_AsKml(),
                new ST_BoundingCircle(),
                new ST_BoundingCircleCenter(),
                new ST_MinimumBoundingCircle(),
                new ST_ClosestCoordinate(),
                new ST_ClosestPoint(),
                new ST_CompactnessRatio(),
                new ST_ConstrainedDelaunay(),
                new ST_Covers(),
                new ST_DWithin(),
                new ST_Delaunay(),
                new ST_Densify(),
                new ST_Expand(),
                new ST_Explode(),
                new ST_Extent(),
                new ST_Extrude(),
                new ST_FurthestCoordinate(),
                new ST_Holes(),
                new ST_Interpolate3DLine(),
                new ST_IsRectangle(),
                new ST_IsValid(),
                new ST_LocateAlong(),
                new ST_MakeEllipse(),
                new ST_MakeEnvelope(),
                new ST_MakeGrid(),
                new ST_MakeGridPoints(),
                new ST_MakeLine(),
                new ST_MakePoint(),
                new ST_MinimumRectangle(),
                new ST_MultiplyZ(),
                new ST_Normalize(),
                new ST_OctogonalEnvelope(),
                new ST_Polygonize(),
                new ST_PrecisionReducer(),
                new ST_RemoveHoles(),
                new ST_RemovePoints(),
                new ST_RemoveRepeatedPoints(),
                new ST_Reverse(),
                new ST_Reverse3DLine(),
                new ST_Rotate(),
                new ST_Scale(),
                new ST_Simplify(),
                new ST_SimplifyPreserveTopology(),
                new ST_Snap(),
                new ST_Split(),
                new ST_ToMultiLine(),
                new ST_ToMultiPoint(),
                new ST_ToMultiSegments(),
                new ST_Translate(),
                new ST_TriangleAspect(),
                new ST_TriangleContouring(),
                new ST_TriangleDirection(),
                new ST_TriangleSlope(),
                new ST_UpdateZ(),
                new ST_XMax(),
                new ST_XMin(),
                new ST_YMax(),
                new ST_YMin(),
                new ST_ZMax(),
                new ST_ZMin(),
                new ST_ZUpdateLineExtremities(),
                new ST_MinimumDiameter(),
                new ST_RingBuffer(),
                new ST_Force2D(),
                new ST_Force3D(),
                new ST_Azimuth(),
                new ST_MakePolygon(),
                new ST_IsValidReason(),
                new ST_IsValidDetail(),
                new ST_LineIntersector(),        
                new ST_OffSetCurve(),
                new OSMRead(),
                new ST_OSMDownloader(),
                new ST_ProjectPoint(),
                new ST_CollectionExtract(),
                new DoubleRange(),
                new IntegerRange(),
                new ST_SideBuffer(),
                new ST_RingSideBuffer(),
                new ST_SunPosition(),
                new ST_GeometryShadow(),
                new ST_Voronoi(),
                new ST_Tessellate(),
                new ST_LineMerge(),
                new ST_FlipCoordinates(),
                new ST_MaxDistance(),
                new ST_LongestLine(),
                new ST_Perimeter(),
                new ST_3DPerimeter(),
                new ST_3DArea(),
                new ST_GeomFromGML(),
                new ST_GeomFromGeoJSON(),
                new ST_OSMMapLink(),
                new ST_GoogleMapLink(),
                new ST_AsGML(),
                new TSVRead(),
                new TSVWrite(),
                new ST_NPoints()};
    }

    /**
     * Init H2 DataBase with extended spatial functions
     *
     * @param connection Active connection
     * @throws SQLException
     */
    public static void initSpatialExtension(Connection connection) throws SQLException {
        org.h2gis.sfs.CreateSpatialExtension.initSpatialExtension(connection);
        // Register project's functions
        addSpatialFunctions(connection);
    }

    /**
     * Register built-in functions
     *
     * @param connection Active connection
     * @throws SQLException Error while creating statement
     */
    public static void addSpatialFunctions(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        for (Function function : getBuiltInsFunctions()) {
            try {
                org.h2gis.sfs.CreateSpatialExtension.registerFunction(st, function, "");
            } catch (SQLException ex) {
                // Catch to register other functions
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
        }
    }
}
