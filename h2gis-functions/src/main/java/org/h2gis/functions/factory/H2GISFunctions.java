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

package org.h2gis.functions.factory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.api.Aggregate;
import org.h2.tools.RunScript;
import org.h2gis.api.Function;
import org.h2gis.api.ScalarFunction;
import org.h2gis.functions.io.dbf.DBFDriverFunction;
import org.h2gis.functions.spatial.split.ST_LineIntersector;
import org.h2gis.functions.spatial.split.ST_Split;
import org.h2gis.functions.io.DriverManager;
import org.h2gis.functions.io.dbf.DBFRead;
import org.h2gis.functions.io.dbf.DBFWrite;
import org.h2gis.functions.io.geojson.GeoJsonRead;
import org.h2gis.functions.io.geojson.GeoJsonWrite;
import org.h2gis.functions.io.geojson.ST_AsGeoJSON;
import org.h2gis.functions.io.geojson.ST_GeomFromGeoJSON;
import org.h2gis.functions.io.gpx.GPXRead;
import org.h2gis.functions.io.json.JsonWrite;
import org.h2gis.functions.io.kml.KMLWrite;
import org.h2gis.functions.io.kml.ST_AsKml;
import org.h2gis.functions.io.osm.OSMRead;
import org.h2gis.functions.io.osm.ST_OSMDownloader;
import org.h2gis.functions.io.shp.SHPRead;
import org.h2gis.functions.io.shp.SHPWrite;
import org.h2gis.functions.io.tsv.TSVRead;
import org.h2gis.functions.io.tsv.TSVWrite;
import org.h2gis.functions.spatial.affine_transformations.ST_Rotate;
import org.h2gis.functions.spatial.affine_transformations.ST_Scale;
import org.h2gis.functions.spatial.affine_transformations.ST_Translate;
import org.h2gis.functions.spatial.aggregate.ST_Accum;
import org.h2gis.functions.spatial.aggregate.ST_Collect;
import org.h2gis.functions.spatial.aggregate.ST_LineMerge;
import org.h2gis.functions.spatial.buffer.ST_Buffer;
import org.h2gis.functions.spatial.buffer.ST_OffSetCurve;
import org.h2gis.functions.spatial.buffer.ST_RingSideBuffer;
import org.h2gis.functions.spatial.buffer.ST_SideBuffer;
import org.h2gis.functions.spatial.clean.ST_MakeValid;
import org.h2gis.functions.spatial.convert.ST_AsBinary;
import org.h2gis.functions.spatial.convert.ST_AsGML;
import org.h2gis.functions.spatial.convert.ST_AsText;
import org.h2gis.functions.spatial.convert.ST_AsWKT;
import org.h2gis.functions.spatial.convert.ST_Force2D;
import org.h2gis.functions.spatial.convert.ST_Force3D;
import org.h2gis.functions.spatial.convert.ST_GeomFromGML;
import org.h2gis.functions.spatial.convert.ST_GeomFromText;
import org.h2gis.functions.spatial.convert.ST_GeomFromWKB;
import org.h2gis.functions.spatial.convert.ST_GoogleMapLink;
import org.h2gis.functions.spatial.convert.ST_Holes;
import org.h2gis.functions.spatial.convert.ST_LineFromText;
import org.h2gis.functions.spatial.convert.ST_LineFromWKB;
import org.h2gis.functions.spatial.convert.ST_MLineFromText;
import org.h2gis.functions.spatial.convert.ST_MPointFromText;
import org.h2gis.functions.spatial.convert.ST_MPolyFromText;
import org.h2gis.functions.spatial.convert.ST_OSMMapLink;
import org.h2gis.functions.spatial.convert.ST_PointFromText;
import org.h2gis.functions.spatial.convert.ST_PointFromWKB;
import org.h2gis.functions.spatial.convert.ST_PolyFromText;
import org.h2gis.functions.spatial.convert.ST_PolyFromWKB;
import org.h2gis.functions.spatial.convert.ST_ToMultiLine;
import org.h2gis.functions.spatial.convert.ST_ToMultiPoint;
import org.h2gis.functions.spatial.convert.ST_ToMultiSegments;
import org.h2gis.functions.spatial.create.ST_BoundingCircle;
import org.h2gis.functions.spatial.create.ST_BoundingCircleCenter;
import org.h2gis.functions.spatial.create.ST_Expand;
import org.h2gis.functions.spatial.create.ST_Extrude;
import org.h2gis.functions.spatial.create.ST_MakeEllipse;
import org.h2gis.functions.spatial.create.ST_MakeEnvelope;
import org.h2gis.functions.spatial.create.ST_MakeGrid;
import org.h2gis.functions.spatial.create.ST_MakeGridPoints;
import org.h2gis.functions.spatial.create.ST_MakeLine;
import org.h2gis.functions.spatial.create.ST_MakePoint;
import org.h2gis.functions.spatial.create.ST_MakePolygon;
import org.h2gis.functions.spatial.create.ST_MinimumBoundingCircle;
import org.h2gis.functions.spatial.create.ST_MinimumRectangle;
import org.h2gis.functions.spatial.create.ST_OctogonalEnvelope;
import org.h2gis.functions.spatial.create.ST_Point;
import org.h2gis.functions.spatial.create.ST_RingBuffer;
import org.h2gis.functions.spatial.crs.ST_SetSRID;
import org.h2gis.functions.spatial.crs.ST_Transform;
import org.h2gis.functions.spatial.distance.ST_ClosestCoordinate;
import org.h2gis.functions.spatial.distance.ST_ClosestPoint;
import org.h2gis.functions.spatial.distance.ST_FurthestCoordinate;
import org.h2gis.functions.spatial.distance.ST_LocateAlong;
import org.h2gis.functions.spatial.distance.ST_LongestLine;
import org.h2gis.functions.spatial.distance.ST_MaxDistance;
import org.h2gis.functions.spatial.distance.ST_ProjectPoint;
import org.h2gis.functions.spatial.distance.ST_ShortestLine;
import org.h2gis.functions.spatial.earth.ST_GeometryShadow;
import org.h2gis.functions.spatial.earth.ST_SunPosition;
import org.h2gis.functions.spatial.earth.ST_Svf;
import org.h2gis.functions.spatial.edit.ST_AddPoint;
import org.h2gis.functions.spatial.edit.ST_AddZ;
import org.h2gis.functions.spatial.edit.ST_CollectionExtract;
import org.h2gis.functions.spatial.edit.ST_Densify;
import org.h2gis.functions.spatial.edit.ST_FlipCoordinates;
import org.h2gis.functions.spatial.edit.ST_Interpolate3DLine;
import org.h2gis.functions.spatial.edit.ST_MultiplyZ;
import org.h2gis.functions.spatial.edit.ST_Normalize;
import org.h2gis.functions.spatial.edit.ST_RemoveDuplicatedCoordinates;
import org.h2gis.functions.spatial.edit.ST_RemoveHoles;
import org.h2gis.functions.spatial.edit.ST_RemovePoints;
import org.h2gis.functions.spatial.edit.ST_RemoveRepeatedPoints;
import org.h2gis.functions.spatial.edit.ST_Reverse;
import org.h2gis.functions.spatial.edit.ST_Reverse3DLine;
import org.h2gis.functions.spatial.edit.ST_UpdateZ;
import org.h2gis.functions.spatial.edit.ST_ZUpdateLineExtremities;
import org.h2gis.functions.spatial.generalize.ST_PrecisionReducer;
import org.h2gis.functions.spatial.generalize.ST_Simplify;
import org.h2gis.functions.spatial.generalize.ST_SimplifyPreserveTopology;
import org.h2gis.functions.spatial.mesh.ST_ConstrainedDelaunay;
import org.h2gis.functions.spatial.mesh.ST_Delaunay;
import org.h2gis.functions.spatial.mesh.ST_Tessellate;
import org.h2gis.functions.spatial.mesh.ST_Voronoi;
import org.h2gis.functions.spatial.operators.ST_ConvexHull;
import org.h2gis.functions.spatial.operators.ST_Difference;
import org.h2gis.functions.spatial.operators.ST_Intersection;
import org.h2gis.functions.spatial.operators.ST_SymDifference;
import org.h2gis.functions.spatial.operators.ST_Union;
import org.h2gis.functions.spatial.predicates.ST_Contains;
import org.h2gis.functions.spatial.predicates.ST_Covers;
import org.h2gis.functions.spatial.predicates.ST_Crosses;
import org.h2gis.functions.spatial.predicates.ST_DWithin;
import org.h2gis.functions.spatial.predicates.ST_Disjoint;
import org.h2gis.functions.spatial.predicates.ST_EnvelopesIntersect;
import org.h2gis.functions.spatial.predicates.ST_Equals;
import org.h2gis.functions.spatial.predicates.ST_Intersects;
import org.h2gis.functions.spatial.predicates.ST_OrderingEquals;
import org.h2gis.functions.spatial.predicates.ST_Overlaps;
import org.h2gis.functions.spatial.predicates.ST_Relate;
import org.h2gis.functions.spatial.predicates.ST_Touches;
import org.h2gis.functions.spatial.predicates.ST_Within;
import org.h2gis.functions.spatial.properties.ColumnSRID;
import org.h2gis.functions.spatial.properties.ST_3DArea;
import org.h2gis.functions.spatial.properties.ST_3DLength;
import org.h2gis.functions.spatial.properties.ST_3DPerimeter;
import org.h2gis.functions.spatial.properties.ST_Area;
import org.h2gis.functions.spatial.properties.ST_Boundary;
import org.h2gis.functions.spatial.properties.ST_Centroid;
import org.h2gis.functions.spatial.properties.ST_CompactnessRatio;
import org.h2gis.functions.spatial.properties.ST_CoordDim;
import org.h2gis.functions.spatial.properties.ST_Dimension;
import org.h2gis.functions.spatial.properties.ST_Distance;
import org.h2gis.functions.spatial.properties.ST_DistanceSphere;
import org.h2gis.functions.spatial.properties.ST_EndPoint;
import org.h2gis.functions.spatial.properties.ST_Envelope;
import org.h2gis.functions.spatial.properties.ST_Explode;
import org.h2gis.functions.spatial.properties.ST_Extent;
import org.h2gis.functions.spatial.properties.ST_ExteriorRing;
import org.h2gis.functions.spatial.properties.ST_GeometryN;
import org.h2gis.functions.spatial.properties.ST_GeometryType;
import org.h2gis.functions.spatial.properties.ST_GeometryTypeCode;
import org.h2gis.functions.spatial.properties.ST_InteriorRingN;
import org.h2gis.functions.spatial.properties.ST_Is3D;
import org.h2gis.functions.spatial.properties.ST_IsClosed;
import org.h2gis.functions.spatial.properties.ST_IsEmpty;
import org.h2gis.functions.spatial.properties.ST_IsRectangle;
import org.h2gis.functions.spatial.properties.ST_IsRing;
import org.h2gis.functions.spatial.properties.ST_IsSimple;
import org.h2gis.functions.spatial.properties.ST_IsValid;
import org.h2gis.functions.spatial.properties.ST_IsValidDetail;
import org.h2gis.functions.spatial.properties.ST_IsValidReason;
import org.h2gis.functions.spatial.properties.ST_Length;
import org.h2gis.functions.spatial.properties.ST_MinimumDiameter;
import org.h2gis.functions.spatial.properties.ST_NPoints;
import org.h2gis.functions.spatial.properties.ST_NumGeometries;
import org.h2gis.functions.spatial.properties.ST_NumInteriorRing;
import org.h2gis.functions.spatial.properties.ST_NumInteriorRings;
import org.h2gis.functions.spatial.properties.ST_NumPoints;
import org.h2gis.functions.spatial.properties.ST_Perimeter;
import org.h2gis.functions.spatial.properties.ST_PointN;
import org.h2gis.functions.spatial.properties.ST_PointOnSurface;
import org.h2gis.functions.spatial.properties.ST_SRID;
import org.h2gis.functions.spatial.properties.ST_StartPoint;
import org.h2gis.functions.spatial.properties.ST_X;
import org.h2gis.functions.spatial.properties.ST_XMax;
import org.h2gis.functions.spatial.properties.ST_XMin;
import org.h2gis.functions.spatial.properties.ST_Y;
import org.h2gis.functions.spatial.properties.ST_YMax;
import org.h2gis.functions.spatial.properties.ST_YMin;
import org.h2gis.functions.spatial.properties.ST_Z;
import org.h2gis.functions.spatial.properties.ST_ZMax;
import org.h2gis.functions.spatial.properties.ST_ZMin;
import org.h2gis.functions.spatial.snap.ST_Snap;
import org.h2gis.functions.spatial.topography.ST_Drape;
import org.h2gis.functions.spatial.topography.ST_TriangleAspect;
import org.h2gis.functions.spatial.topography.ST_TriangleContouring;
import org.h2gis.functions.spatial.topography.ST_TriangleDirection;
import org.h2gis.functions.spatial.topography.ST_TriangleSlope;
import org.h2gis.functions.spatial.topology.ST_Graph;
import org.h2gis.functions.spatial.topology.ST_Node;
import org.h2gis.functions.spatial.topology.ST_Polygonize;
import org.h2gis.functions.spatial.trigonometry.ST_Azimuth;
import org.h2gis.functions.spatial.type.DimensionFromConstraint;
import org.h2gis.functions.spatial.type.DomainInfo;
import org.h2gis.functions.spatial.type.GeometryTypeFromConstraint;
import org.h2gis.functions.spatial.type.GeometryTypeNameFromConstraint;
import org.h2gis.functions.string.HexToVarBinary;
import org.h2gis.functions.system.DoubleRange;
import org.h2gis.functions.system.H2GISversion;
import org.h2gis.functions.system.IntegerRange;
import org.h2gis.utilities.GeometryTypeCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add H2GIS features to an H2 database
 *
 * Execute the following sql to init spatial features :
 * <pre>
 * CREATE ALIAS IF NOT EXISTS H2GIS_FUNCTIONS FOR
 *      &quot;H2GISFunctions.load&quot;;
 * CALL H2GIS_FUNCTIONS();
 * </pre>
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class H2GISFunctions {
    /** H2 base type for geometry column {@link java.sql.ResultSetMetaData#getColumnTypeName(int)} */
    public static final String GEOMETRY_BASE_TYPE = "GEOMETRY";
    private static final Logger LOGGER = LoggerFactory.getLogger(H2GISFunctions.class);

    /**
     * @return instance of all built-ins functions
     */
    public static Function[] getBuiltInsFunctions() {
        return new Function[] {
                new HexToVarBinary(),
                new GeometryTypeFromConstraint(),
                new ColumnSRID(),
                new GeometryTypeNameFromConstraint(),
                new DimensionFromConstraint(),
                new ST_GeomFromText(),
                new ST_Area(),
                new ST_AsBinary(),
                new ST_GeometryType(),
                new ST_PointFromText(),
                new ST_MPointFromText(),
                new ST_LineFromText(),
                new ST_MLineFromText(),
                new ST_PolyFromText(),
                new ST_MPolyFromText(),
                new ST_Dimension(),
                new ST_AsText(),
                new ST_AsWKT(),
                new ST_PolyFromWKB(),
                new ST_IsEmpty(),
                new ST_IsSimple(),
                new ST_Boundary(),
                new ST_Envelope(),
                new ST_X(),
                new ST_Y(),
                new ST_Z(),
                new ST_StartPoint(),
                new ST_EndPoint(),
                new ST_IsClosed(),
                new ST_IsRing(),
                new ST_LineFromWKB(),
                new ST_Length(),
                new ST_NumPoints(),
                new ST_PointN(),
                new ST_Centroid(),
                new ST_PointOnSurface(),
                new ST_Contains(),
                new ST_ExteriorRing(),
                new ST_NumInteriorRings(),
                new ST_NumInteriorRing(),
                new ST_InteriorRingN(),
                new ST_NumGeometries(),
                new ST_GeometryN(),
                new ST_Equals(),
                new ST_Disjoint(),
                new ST_Touches(),
                new ST_Within(),
                new ST_Overlaps(),
                new ST_Crosses(),
                new ST_Intersects(),
                new ST_Relate(),
                new ST_Distance(),
                new ST_DistanceSphere(),
                new ST_Intersection(),
                new ST_Difference(),
                new ST_Union(),
                new ST_SymDifference(),
                new ST_Buffer(),
                new ST_ConvexHull(),
                new ST_SRID(),
                new ST_EnvelopesIntersect(),
                new ST_Accum(),
                new ST_Transform(),
                new ST_SetSRID(),
                new ST_CoordDim(),
                new ST_GeometryTypeCode(),
                new ST_OrderingEquals(),
                new ST_Is3D(),
                new ST_PointFromWKB(),
                new ST_GeomFromWKB(),
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
                new ST_NPoints(),
                new ST_Graph(),
                new H2GISversion(),
                new ST_Collect(),
                new ST_RemoveDuplicatedCoordinates(),
                new ST_MakeValid(),
                new ST_Point(),
                new ST_Node(),
                new ST_Drape(),
                new ST_Svf(),
                new JsonWrite(),
                new ST_ShortestLine()};
    }

    /**
     * @return instance of all spatial built-ins field type
     */
    public static DomainInfo[] getBuiltInsType() {
        return new DomainInfo[] {
                new DomainInfo("POINT", GeometryTypeCodes.POINT),
                new DomainInfo("LINESTRING", GeometryTypeCodes.LINESTRING),
                new DomainInfo("POLYGON", GeometryTypeCodes.POLYGON),
                new DomainInfo("GEOMCOLLECTION", GeometryTypeCodes.GEOMCOLLECTION),
                new DomainInfo("MULTIPOINT", GeometryTypeCodes.MULTIPOINT),
                new DomainInfo("MULTILINESTRING", GeometryTypeCodes.MULTILINESTRING),
                new DomainInfo("MULTIPOLYGON", GeometryTypeCodes.MULTIPOLYGON)
        };
    }

    /**
     * Register GEOMETRY type and register H2GIS functions
     * @param connection Active H2 connection
     * @param BundleSymbolicName OSGi Bundle symbolic name
     * @param BundleVersion OSGi Bundle version
     * @throws java.sql.SQLException
     */
    public static void load(Connection connection, String BundleSymbolicName, String BundleVersion) throws SQLException {
        String packagePrepend = BundleSymbolicName+":"+BundleVersion+":";
        registerH2GISFunctions(connection,packagePrepend);
        registerGeometryType(connection);
        connection.commit();
    }

    /**
     * Register GEOMETRY type and register H2GIS functions
     * @param connection Active H2 connection
     * @throws java.sql.SQLException
     */
    public static void load(Connection connection) throws SQLException {
        registerH2GISFunctions(connection,"");
        registerGeometryType(connection);
        registerSpatialTables(connection);
    }

    /**
     * Register geometry type in an OSGi environment
     * @param connection Active H2 connection
     * @throws SQLException
     */
    public static void registerGeometryType(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        for(DomainInfo domainInfo : getBuiltInsType()) {
            // Check for byte array first, to not throw an enigmatic error CastException
            st.execute("CREATE DOMAIN IF NOT EXISTS "+domainInfo.getDomainName()+" AS "+GEOMETRY_BASE_TYPE+"("+domainInfo.getGeometryTypeCode()+") CHECK (ST_GeometryTypeCode(VALUE) = "+domainInfo.getGeometryTypeCode()+");");
        }
    }

    /**
     * Register view in order to create GEOMETRY_COLUMNS standard table.
     * @param connection Open connection
     * @throws java.sql.SQLException
     */
    public static void registerSpatialTables(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop view if exists geometry_columns");
        st.execute("create view geometry_columns as select TABLE_CATALOG f_table_catalog,TABLE_SCHEMA f_table_schema,TABLE_NAME f_table_name," +
                "COLUMN_NAME f_geometry_column,1 storage_type,_GeometryTypeFromConstraint(CHECK_CONSTRAINT || REMARKS, NUMERIC_PRECISION) geometry_type," +
                "_DimensionFromConstraint(TABLE_CATALOG,TABLE_SCHEMA, TABLE_NAME,COLUMN_NAME,CHECK_CONSTRAINT) coord_dimension," +
                "_ColumnSRID(TABLE_CATALOG,TABLE_SCHEMA, TABLE_NAME,COLUMN_NAME,CHECK_CONSTRAINT) srid," +
                " _GeometryTypeNameFromConstraint(CHECK_CONSTRAINT || REMARKS, NUMERIC_PRECISION) type" +
                " from INFORMATION_SCHEMA.COLUMNS WHERE TYPE_NAME = 'GEOMETRY'");
        ResultSet rs = connection.getMetaData().getTables("","PUBLIC","SPATIAL_REF_SYS",null);
        if(!rs.next()) {
        	InputStreamReader reader = new InputStreamReader(
					H2GISFunctions.class.getResourceAsStream("spatial_ref_sys.sql"));
			RunScript.execute(connection, reader);
				
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

    /**
     * Release geometry type
     * @param connection Active h2 connection with DROP DOMAIN and DROP ALIAS rights
     * @throws java.sql.SQLException
     */
    public static void unRegisterGeometryType(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        DomainInfo[] domainInfos = getBuiltInsType();
        for(DomainInfo domainInfo : domainInfos) {
            st.execute("DROP DOMAIN IF EXISTS " + domainInfo.getDomainName());
        }
    }

    /**
     * Return a string property of the function
     * @param function
     * @param propertyKey
     * @return 
     */
    private static String getStringProperty(Function function, String propertyKey) {
        Object value = function.getProperty(propertyKey);
        return value instanceof String ? (String)value : "";
    }

    /**
     * Return a boolean property of the function
     * 
     * @param function
     * @param propertyKey
     * @param defaultValue
     * @return 
     */
    private static boolean getBooleanProperty(Function function, String propertyKey, boolean defaultValue) {
        Object value = function.getProperty(propertyKey);
        return value instanceof Boolean ? (Boolean)value : defaultValue;
    }

    /**
     * Register a H2GIS java code function
     *
     * @param st SQL Statement
     * @param function Function instance
     * @param packagePrepend For OSGi environment only, use
     * Bundle-SymbolicName:Bundle-Version:
     * @throws java.sql.SQLException
     */
    public static void registerFunction(Statement st,Function function,String packagePrepend) throws SQLException {
        registerFunction(st,function,packagePrepend,true);
    }

    /**
     * Register a H2GIS java code function
     * 
     * @param st SQL Statement
     * @param function Function instance
     * @param packagePrepend For OSGi environment only, use Bundle-SymbolicName:Bundle-Version:
     * @param dropAlias Drop alias if exists before define it.
     * @throws java.sql.SQLException
     */
    public static void registerFunction(Statement st,Function function,String packagePrepend,boolean dropAlias) throws SQLException {
        String functionClass = function.getClass().getName();
        String functionAlias = getAlias(function);

        if(function instanceof ScalarFunction) {
            ScalarFunction scalarFunction = (ScalarFunction)function;
            String functionName = scalarFunction.getJavaStaticMethod();
            if(dropAlias) {
                try {
                    st.execute("DROP ALIAS IF EXISTS " + functionAlias);
                } catch (SQLException ex) {
                    // Ignore, some tables constraints may depend on this function
                    LOGGER.debug(ex.getLocalizedMessage(), ex);
                }
            }
            String deterministic = "";
            if(getBooleanProperty(function,ScalarFunction.PROP_DETERMINISTIC,false)) {
                deterministic = " DETERMINISTIC";
            }
            String nobuffer = "";
            if(getBooleanProperty(function, ScalarFunction.PROP_NOBUFFER, false)) {
                nobuffer = " NOBUFFER";
            }
            // Create alias, H2 does not support prepare statement on create alias
            // "FORCE ALIAS means that the class not existing will not prevent the database from being opened."
            st.execute("CREATE FORCE ALIAS IF NOT EXISTS " + functionAlias + deterministic + nobuffer + " FOR \"" + packagePrepend + functionClass + "." + functionName + "\"");
            // Set comment
            String functionRemarks = getStringProperty(function, Function.PROP_REMARKS);
            if(!functionRemarks.isEmpty()) {
                PreparedStatement ps = st.getConnection().prepareStatement("COMMENT ON ALIAS "+functionAlias+" IS ?");
                ps.setString(1, functionRemarks);
                ps.execute();
            }
        } else if(function instanceof Aggregate) {
                if(dropAlias) {
                    st.execute("DROP AGGREGATE IF EXISTS " + functionAlias);
                }
                st.execute("CREATE FORCE AGGREGATE IF NOT EXISTS " + functionAlias + " FOR \"" + packagePrepend + functionClass + "\"");
        } else {
                throw new SQLException("Unsupported function "+functionClass);
        }
    }

    /**
     * Return the alias name of the function
     * @param function Function instance
     * @return the function ALIAS, name of the function in SQL engine
     */
    public static String getAlias(Function function) {
        String functionAlias = getStringProperty(function,Function.PROP_NAME);
        if(!functionAlias.isEmpty()) {
            return functionAlias;
        }
        return function.getClass().getSimpleName();
    }
    /**
     * Remove the specified function from the provided DataBase connection
     * @param st Active statement
     * @param function function to remove
     * @throws SQLException
     */
    public static void unRegisterFunction(Statement st, Function function) throws SQLException {
        String functionAlias = getStringProperty(function, Function.PROP_NAME);
        if(functionAlias.isEmpty()) {
            functionAlias = function.getClass().getSimpleName();
        }
        st.execute("DROP ALIAS IF EXISTS " + functionAlias);
    }
    
    
    /**
     * Register all H2GIS functions
     *
     * @param connection JDBC Connection
     * @param packagePrepend For OSGi environment only, use
     * Bundle-SymbolicName:Bundle-Version:
     * @throws SQLException
     */
    private static void registerH2GISFunctions(Connection connection, String packagePrepend) throws SQLException {
        Statement st = connection.createStatement();
        for (Function function : getBuiltInsFunctions()) {
            try {
                registerFunction(st, function, packagePrepend);
            } catch (SQLException ex) {
                // Catch to register other functions
                ex.printStackTrace(System.err);
            }
        }
    }

    /**
     * Unregister spatial type and H2GIS functions from the current connection.
     *
     * @param connection Active H2 connection with DROP ALIAS rights
     * @throws java.sql.SQLException
     */
    public static void unRegisterH2GISFunctions(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        for (Function function : getBuiltInsFunctions()) {
            unRegisterFunction(st, function);
        }
        unRegisterGeometryType(connection);
    }
}
