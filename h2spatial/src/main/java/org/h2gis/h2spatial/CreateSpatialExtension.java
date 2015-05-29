/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatial;

import org.h2.api.Aggregate;
import org.h2.tools.RunScript;
import org.h2gis.h2spatial.internal.function.HexToVarBinary;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_AsWKT;
import org.h2gis.h2spatial.internal.function.spatial.crs.ST_SetSRID;
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
import org.h2gis.h2spatial.internal.function.spatial.properties.*;
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
import org.h2gis.h2spatial.internal.type.*;
import org.h2gis.h2spatialapi.Function;
import org.h2gis.h2spatialapi.ScalarFunction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2gis.h2spatial.internal.function.spatial.convert.ST_PointFromWKB;

import org.h2gis.h2spatial.internal.function.spatial.crs.ST_Transform;
import org.h2gis.h2spatial.internal.function.spatial.predicates.ST_OrderingEquals;
import org.h2gis.utilities.GeometryTypeCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateSpatialExtension.class);

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
                new ST_PointFromWKB()};
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
     * Register GEOMETRY type and register spatial functions
     * @param connection Active H2 connection
     * @param BundleSymbolicName OSGi Bundle symbolic name
     * @param BundleVersion OSGi Bundle version
     */
    public static void initSpatialExtension(Connection connection, String BundleSymbolicName, String BundleVersion) throws SQLException {
        String packagePrepend = BundleSymbolicName+":"+BundleVersion+":";
        addSpatialFunctions(connection,packagePrepend);
        registerGeometryType(connection);
        connection.commit();
    }

    /**
     * Register GEOMETRY type and register spatial functions
     * @param connection Active H2 connection
     * @throws java.sql.SQLException
     */
    public static void initSpatialExtension(Connection connection) throws SQLException {
        addSpatialFunctions(connection,"");
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
					CreateSpatialExtension.class.getResourceAsStream("spatial_ref_sys.sql"));
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
     */
    public static void unRegisterGeometryType(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        DomainInfo[] domainInfos = getBuiltInsType();
        for(DomainInfo domainInfo : domainInfos) {
            st.execute("DROP DOMAIN IF EXISTS " + domainInfo.getDomainName());
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
