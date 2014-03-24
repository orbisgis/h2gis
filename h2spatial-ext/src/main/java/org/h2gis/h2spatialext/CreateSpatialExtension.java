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
package org.h2gis.h2spatialext;

import org.h2gis.drivers.DriverManager;
import org.h2gis.drivers.dbf.DBFRead;
import org.h2gis.drivers.dbf.DBFWrite;
import org.h2gis.drivers.geojson.GeoJsonRead;
import org.h2gis.drivers.geojson.GeoJsonWrite;
import org.h2gis.drivers.geojson.ST_AsGeoJSON;
import org.h2gis.drivers.gpx.GPXRead;
import org.h2gis.drivers.shp.SHPRead;
import org.h2gis.drivers.shp.SHPWrite;
import org.h2gis.h2spatialapi.Function;
import org.h2gis.h2spatialext.function.spatial.affine_transformations.ST_Rotate;
import org.h2gis.h2spatialext.function.spatial.affine_transformations.ST_Scale;
import org.h2gis.h2spatialext.function.spatial.affine_transformations.ST_Translate;
import org.h2gis.h2spatialext.function.spatial.convert.ST_Holes;
import org.h2gis.h2spatialext.function.spatial.convert.ST_ToMultiLine;
import org.h2gis.h2spatialext.function.spatial.convert.ST_ToMultiPoint;
import org.h2gis.h2spatialext.function.spatial.convert.ST_ToMultiSegments;
import org.h2gis.h2spatialext.function.spatial.create.*;
import org.h2gis.h2spatialext.function.spatial.distance.ST_ClosestCoordinate;
import org.h2gis.h2spatialext.function.spatial.distance.ST_ClosestPoint;
import org.h2gis.h2spatialext.function.spatial.distance.ST_FurthestCoordinate;
import org.h2gis.h2spatialext.function.spatial.distance.ST_LocateAlong;
import org.h2gis.h2spatialext.function.spatial.edit.*;
import org.h2gis.h2spatialext.function.spatial.mesh.ST_ConstrainedDelaunay;
import org.h2gis.h2spatialext.function.spatial.mesh.ST_Delaunay;
import org.h2gis.h2spatialext.function.spatial.predicates.ST_Covers;
import org.h2gis.h2spatialext.function.spatial.predicates.ST_DWithin;
import org.h2gis.h2spatialext.function.spatial.processing.*;
import org.h2gis.h2spatialext.function.spatial.properties.*;
import org.h2gis.h2spatialext.function.spatial.topography.ST_TriangleAspect;
import org.h2gis.h2spatialext.function.spatial.topography.ST_TriangleDirection;
import org.h2gis.h2spatialext.function.spatial.topography.ST_TriangleSlope;
import org.h2gis.network.graph_creator.ST_Graph;
import org.h2gis.network.graph_creator.ST_ShortestPathLength;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Registers the SQL functions contained in h2spatial-ext.
 *
 * @author Nicolas Fortin
 * @author Adam Gouge
 */
public class CreateSpatialExtension {

    /**
     * @return instance of all built-ins functions
     */
    public static Function[] getBuiltInsFunctions() {
        return new Function[] {
                new DBFRead(),
                new DBFWrite(),
                new DriverManager(),
                new GPXRead(),
                new GeoJsonRead(),
                new GeoJsonWrite(),
                new SHPRead(),
                new SHPWrite(),
                new ST_3DLength(),
                new ST_AddPoint(),
                new ST_AddZ(),
                new ST_AsGeoJSON(),
                new ST_BoundingCircle(),
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
                new ST_RemovePoint(),
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
                new ST_TriangleDirection(),
                new ST_TriangleSlope(),
                new ST_UpdateZ(),
                new ST_XMax(),
                new ST_XMin(),
                new ST_YMax(),
                new ST_YMin(),
                new ST_ZMax(),
                new ST_ZMin(),
                new ST_ZUpdateExtremities(),
                // h2network functions
                new ST_Graph(),
                new ST_ShortestPathLength()};
    }

    /**
     * Init H2 DataBase with extended spatial functions
     *
     * @param connection Active connection
     * @throws SQLException
     */
    public static void initSpatialExtension(Connection connection) throws SQLException {
        org.h2gis.h2spatial.CreateSpatialExtension.initSpatialExtension(connection);
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
                org.h2gis.h2spatial.CreateSpatialExtension.registerFunction(st, function, "");
            } catch (SQLException ex) {
                // Catch to register other functions
                ex.printStackTrace(System.err);
            }
        }
    }
}
