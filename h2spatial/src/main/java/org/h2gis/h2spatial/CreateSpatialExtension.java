/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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
package org.h2gis.h2spatial;

import org.h2.api.AggregateFunction;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_Contains;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_Crosses;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_Disjoint;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_EnvelopesIntersect;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_Equals;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_Intersects;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_Overlaps;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_Relate;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_Touches;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_Within;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_Envelope;
import org.h2gis.h2spatial.internal.type.SC_MultiPolygon;
import org.h2gis.h2spatial.internal.function.HexToVarBinary;
import org.h2gis.h2spatial.internal.function.spatial.aggregate.ST_Accum;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_AsBinary;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_AsText;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_GeomFromText;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_LineFromText;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_LineFromWKB;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_MLineFromText;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_MPointFromText;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_MPolyFromText;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_PointFromText;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_PolyFromText;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_PolyFromWKB;
import org.h2gis.h2spatial.internal.function.spatial.operators.ST_Buffer;
import org.h2gis.h2spatial.internal.function.spatial.operators.ST_ConvexHull;
import org.h2gis.h2spatial.internal.function.spatial.operators.ST_Difference;
import org.h2gis.h2spatial.internal.function.spatial.operators.ST_Intersection;
import org.h2gis.h2spatial.internal.function.spatial.operators.ST_SymDifference;
import org.h2gis.h2spatial.internal.function.spatial.operators.ST_Union;
import org.h2gis.h2spatial.internal.function.spatial.properties.ColumnSRID;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_Area;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_Boundary;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_Centroid;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_Dimension;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_Distance;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_EndPoint;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_ExteriorRing;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_GeometryN;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_GeometryType;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_InteriorRingN;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_IsClosed;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_IsEmpty;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_IsRing;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_IsSimple;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_Length;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_NumGeometries;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_NumInteriorRing;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_NumInteriorRings;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_NumPoints;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_PointN;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_PointOnSurface;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_SRID;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_StartPoint;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_X;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_Y;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_Z;
import org.h2gis.h2spatial.internal.type.DomainInfo;
import org.h2gis.h2spatial.internal.type.GeometryTypeFromConstraint;
import org.h2gis.h2spatial.internal.type.SC_GeomCollection;
import org.h2gis.h2spatial.internal.type.SC_LineString;
import org.h2gis.h2spatial.internal.type.SC_MultiLineString;
import org.h2gis.h2spatial.internal.type.SC_MultiPoint;
import org.h2gis.h2spatial.internal.type.SC_Point;
import org.h2gis.h2spatial.internal.type.SC_Polygon;
import org.h2gis.h2spatialapi.Function;
import org.h2gis.h2spatialapi.ScalarFunction;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Add spatial features to an H2 database
 *
 * Execute the following sql to init spatial features :
 * <pre>
 * CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR
 *      &quot;CreateSpatialExtension.initSpatialExtension&quot;;
 * CALL SPATIAL_INIT();
 * </pre>
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class CreateSpatialExtension {
    /** H2 base type for geometry column {@link java.sql.ResultSetMetaData#getColumnTypeName(int)} */
    public static final String GEOMETRY_BASE_TYPE = "GEOMETRY";

    /**
     * @return instance of all built-ins functions
     */
    public static Function[] getBuiltInsFunctions() {
        return new Function[] {
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
                new HexToVarBinary(),
                new ST_Dimension(),
                new GeometryTypeFromConstraint(),
                new ST_AsText(),
                new ST_PolyFromWKB(),
                new ST_IsEmpty(),
                new ST_IsSimple(),
                new ST_Boundary(),
                new ST_Envelope(),
                new ST_X(),
                new ST_Y(),
                new ST_Z(),
                new ColumnSRID(),
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
                new ST_Intersection(),
                new ST_Difference(),
                new ST_Union(),
                new ST_SymDifference(),
                new ST_Buffer(),
                new ST_ConvexHull(),
                new ST_SRID(),
                new ST_EnvelopesIntersect(),
                new ST_Accum()};
    }

    /**
     * @return instance of all spatial built-ins field type
     */
    public static DomainInfo[] getBuiltInsType() {
        return new DomainInfo[] {
                new DomainInfo("POINT", new SC_Point()),
                new DomainInfo("LINESTRING", new SC_LineString()),
                new DomainInfo("POLYGON", new SC_Polygon()),
                new DomainInfo("GEOMCOLLECTION", new SC_GeomCollection()),
                new DomainInfo("MULTIPOINT", new SC_MultiPoint()),
                new DomainInfo("MULTILINESTRING", new SC_MultiLineString()),
                new DomainInfo("MULTIPOLYGON", new SC_MultiPolygon())
        };
    }

    /**
     * Register GEOMETRY type and register spatial functions
     * @param connection Active H2 connection
     * @param BundleSymbolicName OSGi Bundle symbolic name
     * @param BundleVersion OSGi Bundle version
     */
    public static void initSpatialExtension(Connection connection, String BundleSymbolicName, String BundleVersion) throws SQLException {
        String packagePrepend = BundleSymbolicName+":"+BundleVersion+":";
        registerGeometryType(connection,packagePrepend);
        addSpatialFunctions(connection,packagePrepend);
        connection.commit();
    }

    /**
     * Register GEOMETRY type and register spatial functions
     * @param connection Active H2 connection
     */
    public static void initSpatialExtension(Connection connection) throws SQLException {
        registerGeometryType(connection,"");
        addSpatialFunctions(connection,"");
        registerSpatialTables(connection);
    }

    /**
     * Register geometry type in an OSGi environment
     * @param connection Active H2 connection
     * @param packagePrepend For OSGi environment only, use Bundle-SymbolicName:Bundle-Version:
     * @throws SQLException
     */
    public static void registerGeometryType(Connection connection,String packagePrepend) throws SQLException {
        Statement st = connection.createStatement();
        for(DomainInfo domainInfo : getBuiltInsType()) {
            // Do not drop constraint function as some table may use this constraint in CHECK statement
            registerFunction(st,domainInfo.getDomainConstraint(),packagePrepend,false);
            // Check for byte array first, to not throw an enigmatic error CastException
            st.execute("CREATE DOMAIN IF NOT EXISTS "+domainInfo.getDomainName()+" AS "+GEOMETRY_BASE_TYPE+" CHECK ("+getAlias(domainInfo.getDomainConstraint())+"(VALUE));");
        }
    }

    /**
     * Register view in order to create GEOMETRY_COLUMNS standard table.
     * @param connection Open connection
     */
    public static void registerSpatialTables(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        st.execute("drop view if exists geometry_columns");
        st.execute("create view geometry_columns as select TABLE_SCHEMA f_table_schema,TABLE_NAME f_table_name," +
                "COLUMN_NAME f_geometry_column,1 storage_type,GeometryTypeFromConstraint(CHECK_CONSTRAINT || REMARKS) geometry_type,2 coord_dimension,ColumnSRID(TABLE_NAME,COLUMN_NAME) srid" +
                " from INFORMATION_SCHEMA.COLUMNS WHERE TYPE_NAME = 'GEOMETRY'");
        ResultSet rs = connection.getMetaData().getTables("","PUBLIC","SPATIAL_REF_SYS",null);
        if(!rs.next()) {
            URL resource = CreateSpatialExtension.class.getResource("spatial_ref_sys.sql");
            st.execute(String.format("RUNSCRIPT FROM '%s'",resource));
        }
    }

    /**
     * Release geometry type
     * @param connection Active h2 connection with DROP DOMAIN and DROP ALIAS rights
     */
    public static void unRegisterGeometryType(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        DomainInfo[] domainInfos = getBuiltInsType();
        for(DomainInfo domainInfo : domainInfos) {
            st.execute("DROP DOMAIN IF EXISTS "+domainInfo.getDomainName());
        }
        // Same constraint may be used by multiple domains
        // Removal must be done in another loop
        for(DomainInfo domainInfo : domainInfos) {
            unRegisterFunction(st,domainInfo.getDomainConstraint());
        }
    }

    private static String getStringProperty(Function function, String propertyKey) {
        Object value = function.getProperty(propertyKey);
        return value instanceof String ? (String)value : "";
    }

    private static boolean getBooleanProperty(Function function, String propertyKey, boolean defaultValue) {
        Object value = function.getProperty(propertyKey);
        return value instanceof Boolean ? (Boolean)value : defaultValue;
    }

	/**
	 * Create java code to add function copy paste into
	 * GeoSpatialFunctionsAddRemove to upload it
	 * @param st SQL Statement
	 * @param function Function instance
     * @param packagePrepend For OSGi environment only, use Bundle-SymbolicName:Bundle-Version:
	 */
    public static void registerFunction(Statement st,Function function,String packagePrepend) throws SQLException {
        registerFunction(st,function,packagePrepend,true);
    }

    /**
     * Create java code to add function copy paste into
     * GeoSpatialFunctionsAddRemove to upload it
     * @param st SQL Statement
     * @param function Function instance
     * @param packagePrepend For OSGi environment only, use Bundle-SymbolicName:Bundle-Version:
     * @param dropAlias Drop alias if exists before define it.
     */
    public static void registerFunction(Statement st,Function function,String packagePrepend,boolean dropAlias) throws SQLException {
        String functionClass = function.getClass().getName();
        String functionAlias = getAlias(function);

        if(function instanceof ScalarFunction) {
            ScalarFunction scalarFunction = (ScalarFunction)function;
            String functionName = scalarFunction.getJavaStaticMethod();
            if(dropAlias) {
                st.execute("DROP ALIAS IF EXISTS " + functionAlias);
            }
            String deterministic = "";
            if(getBooleanProperty(function,ScalarFunction.PROP_DETERMINISTIC,false)) {
                deterministic = " DETERMINISTIC";
            }
            // Create alias, H2 does not support prepare statement on create alias
            st.execute("CREATE ALIAS IF NOT EXISTS " + functionAlias + deterministic + " FOR \"" + packagePrepend + functionClass + "." + functionName + "\"");
        } else if(function instanceof AggregateFunction) {
                st.execute("CREATE AGGREGATE IF NOT EXISTS " + functionAlias + " FOR \"" + packagePrepend + functionClass + "\"");
        } else {
                throw new SQLException("Unsupported function "+functionClass);
        }
        // Set comment
        String functionRemarks = getStringProperty(function, Function.PROP_REMARKS);
        if(!functionRemarks.isEmpty()) {
            PreparedStatement ps = st.getConnection().prepareStatement("COMMENT ON ALIAS "+functionAlias+" IS ?");
            ps.setString(1, functionRemarks);
            ps.execute();
        }
    }

    /**
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
     * Register all built-ins function
     * @param connection JDBC Connection
     * @param packagePrepend For OSGi environment only, use Bundle-SymbolicName:Bundle-Version:
     * @throws SQLException
     */
	private static void addSpatialFunctions(Connection connection,String packagePrepend) throws SQLException {
        Statement st = connection.createStatement();
        for(Function function : getBuiltInsFunctions()) {
            try {
                registerFunction(st,function,packagePrepend);
            } catch (SQLException ex) {
                // Catch to register other functions
                ex.printStackTrace(System.err);
            }
        }
	}

	/**
	 * Remove spatial type and functions from the current connection.
	 * @param connection Active H2 connection with DROP ALIAS rights
	 */
	public static void disposeSpatialExtension(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        for(Function function : getBuiltInsFunctions()) {
            unRegisterFunction(st,function);
        }
        unRegisterGeometryType(connection);
	}
}