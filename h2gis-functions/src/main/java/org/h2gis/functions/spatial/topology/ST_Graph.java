/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.topology;


import org.h2.value.*;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.*;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Assigns integer node and edge ids to LINESTRING or MULTILINESTRING
 * geometries from a table named input, resulting in two new tables:
 * input_nodes and input_edges.
 *
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class ST_Graph extends AbstractFunction implements ScalarFunction {
    
    
    
    public static final String NODES_SUFFIX = "_NODES";
    public static final String EDGES_SUFFIX = "_EDGES";
    public static String PTS_TABLE;
    public static String COORDS_TABLE;
    
    public static final String REMARKS =
            "ST_Graph produces two tables (nodes and edges) from an input table containing\n" +
            "`LINESTRING`s or `MULTILINESTRING`s in the given column and using the given\n" +
            "tolerance, and potentially orienting edges by slope. If the input table has\n" +
            "name `input`, then the output tables are named `input_nodes` and `input_edges`.\n" +
            "The nodes table consists of an integer `node_id` and a `POINT` geometry\n" +
            "representing each node. The edges table is a copy of the input table with three\n" +
            "extra columns: `edge_id`, `start_node`, and `end_node`. The `start_node` and\n" +
            "`end_node` correspond to the `node_id`s in the nodes table.\n" +
            "\n" +
            "If the specified geometry column of the input table contains geometries other\n" +
            "than `LINESTRING`s, the operation will fail.\n" +
            "\n" +
            "A tolerance value may be given to specify the side length of a square envelope\n" +
            "around each node used to snap together other nodes within the same envelope.\n" +
            "Note, however, that edge geometries are left untouched. Note also that\n" +
            "coordinates within a given tolerance of each other are not necessarily snapped\n" +
            "together. Only the first and last coordinates of a geometry are considered to\n" +
            "be potential nodes, and only nodes within a given tolerance of each other are\n" +
            "snapped together. The tolerance works only in metric units.\n" +
            "\n" +
            "A boolean value may be set to true to specify that edges should be oriented by\n" +
            "the z-value of their first and last coordinates (decreasing).\n";

    private static final Logger LOGGER = LoggerFactory.getLogger("gui." + ST_Graph.class);
    public static final String TYPE_ERROR = "Only LINESTRINGs and LINESTRING Zs " +
            "are accepted. Type code: ";
    public static final String ALREADY_RUN_ERROR = "ST_Graph has already been called on table ";
    
    /**
     * Constructor
     */
    public ST_Graph() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "createGraph";
    }

    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs.
     * 
     * Since no column is specified in this signature, we take the first
     * geometry column we find.
     * 
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection Connection
     * @param tableName  Input table containing LINESTRINGs
     * @return true if both output tables were created
     */
    public static boolean createGraph(Connection connection,
                                      String tableName) throws SQLException {
        return createGraph(connection, tableName, null, 0, false,false, null);
    }


    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs in the given column.
     * 
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection       Connection
     * @param tableName        Input table
     * @param value            Name of column containing LINESTRINGs or an array of columns
     * @return true if both output tables were created
     */
    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      Value value) throws SQLException {
        // The default tolerance is zero.
        if(value instanceof ValueVarchar) {
            return createGraph(connection, tableName, value.toString(), 0.0, false, false, null);
        }else if(value instanceof ValueArray){
            Value[] list = ((ValueArray) value).getList();
            ArrayList<String> columns = new ArrayList<>();
            for (Value arrVal : list) {
                columns.add(arrVal.getString());
            }
            return createGraph(connection, tableName, null,0.0, false, false, columns);
        }
        else if(value instanceof ValueBoolean){
            return createGraph(connection, tableName, null,0.0, false, value.getBoolean(), null);
        }
        else if(value instanceof ValueNumeric){
            return createGraph(connection, tableName, null,value.getDouble(), false, false, null);
        }
        throw new SQLException("Unsupported second argument. Possible solutions :" +
                " geometry column name,  array of columns, boolean value to delete existing graph tables, tolerance value to snap the edges");
    }

    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs in the given column and using the given
     * tolerance.
     * 
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     * 
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection       Connection
     * @param tableName        Input table
     * @param secondValue Name of column containing LINESTRINGs
     * @param thirdValue        Tolerance
     * @return true if both output tables were created
     */
    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      Value secondValue,
                                      Value thirdValue) throws SQLException {
        String spatialFieldName = null;
        double tolerance = 0;
        boolean orientBySlope = false;
        boolean deleteTables = false;
        ArrayList<String> columns =null;
        if(secondValue instanceof ValueVarchar) {
            spatialFieldName = secondValue.getString();
            if(thirdValue instanceof ValueBoolean){
                deleteTables = thirdValue.getBoolean();
            }
            else if(thirdValue instanceof ValueNumeric){
                tolerance = thirdValue.getDouble();
            }
            else if(thirdValue instanceof ValueArray){
                columns = getColumns(((ValueArray) secondValue));
            }
            else{
                throw new SQLException("Unsupported signature. Possible arguments are : \"" +
                        " ST_GRAPH(tableName, geometryColumn, true to delete tables\n" +
                        " ST_GRAPH(tableName, geometryColumn, tolerance to snap the edges)\"" +
                        " ST_GRAPH(tableName, geometryColumn, array of columns to keep)\n");
            }
         }else if(secondValue instanceof ValueArray){
            columns = getColumns(((ValueArray) secondValue));
             if(thirdValue instanceof ValueBoolean){
                deleteTables = thirdValue.getBoolean();
            }else if(thirdValue instanceof ValueNumeric){
                tolerance = thirdValue.getDouble();
            } else{
                 throw new SQLException("Unsupported signature. Possible arguments are : \"" +
                         " ST_GRAPH(tableName, array of columns to keep, true to delete tables\n" +
                         " ST_GRAPH(tableName, array of columns to keep, tolerance to snap the edges)\"");
             }
        }
        return createGraph(connection, tableName, spatialFieldName, tolerance, orientBySlope, deleteTables, columns);
    }

    private static ArrayList<String> getColumns(ValueArray valueArray){
        Value[] list = valueArray.getList();
        ArrayList<String> columns = new ArrayList<>();
        for (Value arrVal : list) {
            columns.add(arrVal.getString());
        }
        return columns;
    }
    
    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs in the given column and using the given
     * tolerance, and potentially orienting edges by slope.
     * 
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     * 
     * The boolean orientBySlope is set to true if edges should be oriented by
     * the z-value of their first and last coordinates (decreasing).
     * 
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection       Connection
     * @param inputTable        Input table
     * @param spatialFieldName Name of column containing LINESTRINGs
     * @param tolerance        Tolerance
     * @param orientBySlope    True if edges should be oriented by the z-value of
     *                         their first and last coordinates (decreasing)
     * @return true if both output tables were created
     */
    public static boolean createGraph(Connection connection,
                                      String inputTable,
                                      String spatialFieldName,
                                      double tolerance,
                                      boolean orientBySlope) throws SQLException {
         return createGraph(connection, inputTable, spatialFieldName, tolerance, orientBySlope, false, null);
     }


    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs in the given column and using the given
     * tolerance, and potentially orienting edges by slope.
     *
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     *
     * The boolean orientBySlope is set to true if edges should be oriented by
     * the z-value of their first and last coordinates (decreasing).
     *
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection       Connection
     * @param inputTable        Input table
     * @param spatialFieldName Name of column containing LINESTRINGs
     * @param tolerance        Tolerance
     * @param orientBySlope    True if edges should be oriented by the z-value of
     *                         their first and last coordinates (decreasing)
     * @param deleteTables     True delete the existing tables
     * @return true if both output tables were created
     */
    public static boolean createGraph(Connection connection,
                                      String inputTable,
                                      String spatialFieldName,
                                      double tolerance,
                                      boolean orientBySlope, boolean deleteTables) throws SQLException {
        return createGraph(connection, inputTable, spatialFieldName, tolerance, orientBySlope, deleteTables, null);
    }


    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs in the given column and using the given
     * tolerance, and potentially orienting edges by slope.
     * 
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     * 
     * The boolean orientBySlope is set to true if edges should be oriented by
     * the z-value of their first and last coordinates (decreasing).
     * 
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection       Connection
     * @param inputTable       Input table
     * @param spatialFieldName Name of column containing LINESTRINGs
     * @param tolerance        Tolerance
     * @param orientBySlope    True if edges should be oriented by the z-value of
     *                         their first and last coordinates (decreasing)
     * @param deleteTables     True delete the existing tables
     * @param columns          an array of columns to keep
     * @return true if both output tables were created
     */
    public static boolean createGraph(Connection connection,
                                      String inputTable,
                                      String spatialFieldName,
                                      double tolerance,
                                      boolean orientBySlope,
                                      boolean deleteTables, ArrayList<String> columns) throws SQLException {
        if (tolerance < 0) {
            throw new IllegalArgumentException("Only positive tolerances are allowed.");
        }
        final TableLocation tableName = TableUtilities.parseInputTable(connection, inputTable);
        final TableLocation nodesName = TableUtilities.suffixTableLocation(tableName, NODES_SUFFIX);
        final TableLocation edgesName = TableUtilities.suffixTableLocation(tableName, EDGES_SUFFIX);
        final DBTypes dbType = DBUtils.getDBType(connection);
        if(deleteTables){            
            try (Statement stmt = connection.createStatement()) {
                StringBuilder sb = new StringBuilder("drop table if exists ");
                sb.append(nodesName.toString()).append(",").append(edgesName.toString());
                stmt.execute(sb.toString());
            }
        }
        // Check if ST_Graph has already been run on this table.
        else if (JDBCUtilities.tableExists(connection, nodesName) ||
                JDBCUtilities.tableExists(connection, edgesName)) {
            throw new IllegalArgumentException(ALREADY_RUN_ERROR + tableName.getTable());
        }
        //Tables used to store intermediate data
        PTS_TABLE = TableLocation.parse(System.currentTimeMillis()+"_PTS", dbType).toString();
        COORDS_TABLE = TableLocation.parse(System.currentTimeMillis()+"_COORDS", dbType).toString();

        // Check for a primary key
        final Tuple<String, Integer> pkIndex = JDBCUtilities.getIntegerPrimaryKeyNameAndIndex(connection, tableName);
        String idRowColumn =null;
        if (pkIndex==null) {
            //Check if there is an autoincrement table and use it
            String autoColumn = JDBCUtilities.getFirstAutoIncrementColumn(connection, tableName);
            if(autoColumn==null) {
                throw new IllegalStateException("Table " + tableName.getTable()
                        + " must contain a single integer primary key or an autoincremented column.");
            }else {
                idRowColumn=autoColumn;
            }
        }else {
            idRowColumn = pkIndex.first();
        }
        // Check the geometry column type;
        LinkedHashMap<String, GeometryMetaData> geomMetadatas = GeometryTableUtilities.getMetaData(connection, tableName);
        Map.Entry<String, GeometryMetaData> geometryMetada = geomMetadatas.entrySet().iterator().next();
        if(spatialFieldName!=null && !spatialFieldName.isEmpty()){
            Map.Entry<String, GeometryMetaData> result = geomMetadatas.entrySet().stream()
                    .filter(columnName -> spatialFieldName.equalsIgnoreCase(columnName.getKey()))
                    .findAny()
                    .orElse(null);
            if(result!=null){
                geometryMetada=result;
            }
        }
        checkGeometryType(geometryMetada.getValue().geometryTypeCode);
        final Statement st = connection.createStatement();
        try {
            String selectedColumns="";
            if(columns!=null && !columns.isEmpty())  {
                selectedColumns = ","+String.join(",", columns);

            }
            firstFirstLastLast(st, tableName, idRowColumn, geometryMetada.getKey(), tolerance,selectedColumns);
            int srid = geometryMetada.getValue().SRID;
            boolean hasZ = geometryMetada.getValue().hasZ;
            makeEnvelopes(st, tolerance, dbType, srid,hasZ);
            nodesTable(st, nodesName, tolerance, srid, hasZ);
            edgesTable(st, nodesName, edgesName, tolerance, dbType, selectedColumns);
            checkForNullEdgeEndpoints(st, edgesName);
            if (orientBySlope) {
                orientBySlope(st, nodesName, edgesName);
            }
        } finally {            
            st.execute("DROP TABLE IF EXISTS "+ PTS_TABLE+ ","+ COORDS_TABLE);
            st.close();
        }
        return true;
    }

    private static void checkGeometryType(int geomType) throws SQLException {
        if (geomType != GeometryTypeCodes.LINESTRING && geomType != GeometryTypeCodes.LINESTRINGZ) {
            throw new IllegalArgumentException(TYPE_ERROR);
        }
    }   

    private static String expand(String geom, double tol) {
        return "ST_Expand(" + geom + ", " + tol + ")";
    }

    /**
     * Return the first and last coordinates table
     * @param st {@link Statement}
     * @param tableName table name
     * @param pkCol primary key column name
     * @param geomCol geometry column name
     * @param tolerance distance
     * @param columns an array of columns
     */
    private static void firstFirstLastLast(Statement st,
                                           TableLocation tableName,
                                           String pkCol,
                                           String geomCol,
                                           double tolerance, String  columns) throws SQLException {
        LOGGER.debug("Selecting the first coordinate of the first geometry and " +
                "the last coordinate of the last geometry...");
        final String numGeoms = "ST_NumGeometries(" + geomCol + ")";
        final String firstGeom = "ST_GeometryN(" + geomCol + ", 1)";
        final String firstPointFirstGeom = "ST_PointN(" + firstGeom + ", 1)";
        final String lastGeom = "ST_GeometryN(" + geomCol + ", " + numGeoms + ")";
        final String lastPointLastGeom = "ST_PointN(" + lastGeom + ", ST_NumPoints(" + lastGeom + "))";
        st.execute("drop TABLE if exists "+ COORDS_TABLE);
        if (tolerance > 0) {
            st.execute("CREATE TABLE "+ COORDS_TABLE+" AS "
                    + "SELECT " + pkCol + " EDGE_ID, "
                    + firstPointFirstGeom + " START_POINT, "
                    + expand(firstPointFirstGeom, tolerance) + " START_POINT_EXP, "
                    + lastPointLastGeom + " END_POINT, "
                    + expand(lastPointLastGeom, tolerance) + " END_POINT_EXP "
                    + columns
                    + " FROM " + tableName + " WHERE ST_ISEMPTY(ROAD) = FALSE");
        } else {
            // If the tolerance is zero, there is no need to call ST_Expand.
            st.execute("CREATE  TABLE "+ COORDS_TABLE+" AS "
                    + "SELECT " + pkCol + " EDGE_ID, "
                    + firstPointFirstGeom + " START_POINT, "
                    + lastPointLastGeom + " END_POINT "
                    + columns
                    + " FROM " + tableName + " WHERE ST_ISEMPTY(ROAD) = FALSE");
        }
    }

    /**
     * Make a big table of all points in the coords table with an envelope around each point.
     * We will use this table to remove duplicate points.
     */
    private static void makeEnvelopes(Statement st, double tolerance, DBTypes dbType, int srid, boolean hasZ) throws SQLException {
        st.execute("DROP TABLE IF EXISTS" + PTS_TABLE + ";");
        String pointSignature = hasZ?"POINTZ":"POINT";
        if (tolerance > 0) {
            LOGGER.debug("Calculating envelopes around coordinates...");
            // Putting all points and their envelopes together...
            st.execute("CREATE  TABLE " + PTS_TABLE + "( ID SERIAL PRIMARY KEY, "
                    + "THE_GEOM GEOMETRY("+pointSignature+"," + srid + "),"
                    + "AREA GEOMETRY(POLYGON, " + srid + ")"
                    + ") ");
            st.execute("INSERT INTO " + PTS_TABLE + " (SELECT CAST((row_number() over()) as Integer) , a.THE_GEOM, A.AREA FROM  "
                    + "(SELECT  START_POINT AS THE_GEOM, START_POINT_EXP as AREA FROM " + COORDS_TABLE
                    + " UNION ALL "
                    + "SELECT  END_POINT AS THE_GEOM, END_POINT_EXP as AREA FROM " + COORDS_TABLE + ") as a);");
            // Putting a spatial index on the envelopes...
            if (dbType == DBTypes.H2 || dbType == DBTypes.H2GIS) {
                st.execute("CREATE SPATIAL INDEX ON " + PTS_TABLE + "(AREA);");
            } else {
                st.execute("CREATE INDEX ON " + PTS_TABLE + " USING GIST(AREA);");
            }
        } else {
            LOGGER.debug("Preparing temporary nodes table from coordinates...");
            // If the tolerance is zero, we just put all points together
            st.execute("CREATE  TABLE " + PTS_TABLE + "( "
                    + "ID SERIAL PRIMARY KEY, "
                    + "THE_GEOM GEOMETRY("+pointSignature+"," + srid + ")"
                    + ")");
            st.execute("INSERT INTO " + PTS_TABLE + " (SELECT (row_number() over())::int , a.the_geom FROM "
                    + "(SELECT  START_POINT as THE_GEOM FROM " + COORDS_TABLE
                    + " UNION ALL "
                    + "SELECT  END_POINT as THE_GEOM FROM " + COORDS_TABLE + ") as a);");
            if (dbType == DBTypes.H2 || dbType == DBTypes.H2GIS) {
                // Putting a spatial index on the points themselves...
                st.execute("CREATE SPATIAL INDEX ON " + PTS_TABLE + "(THE_GEOM);");
            } else {
                // Putting a spatial index on the points themselves...
                st.execute("CREATE INDEX ON " + PTS_TABLE + " USING GIST(THE_GEOM);");
            }
        }
    }

    /**
     * Create the nodes table.
     */
    private static void nodesTable(Statement st,
                                   TableLocation nodesName,
                                   double tolerance, int srid, boolean hasZ) throws SQLException {
        LOGGER.debug("Creating the nodes table...");
        // Creating nodes table by removing copies from the pts table.
        String pointSignature = hasZ?"POINTZ":"POINT";
        if (tolerance > 0) {
               st.execute("CREATE TABLE " + nodesName + "(" +
                    "NODE_ID SERIAL PRIMARY KEY, " +
                    "THE_GEOM GEOMETRY("+pointSignature+", " + srid+"), "+
                    "EXP GEOMETRY(POLYGON," +srid+")"+
                    ") " );
                st.execute( "INSERT INTO "+nodesName +" (SELECT CAST((row_number() over()) AS INTEGER) , c.the_geom, c.area FROM (SELECT  A.THE_GEOM, A.AREA FROM "+ PTS_TABLE +" as  A, "+ PTS_TABLE +" as B " +
                    "WHERE A.AREA && B.AREA " +
                    "GROUP BY A.ID " +
                    "HAVING A.ID=MIN(B.ID)) as c);"); 

        } else {
            // If the tolerance is zero, we can create the NODES table
            // by using = rather than &&.
            st.execute("CREATE TABLE " + nodesName + "(" +
                    "NODE_ID SERIAL PRIMARY KEY, " +
                    "THE_GEOM GEOMETRY("+pointSignature+", "+srid+")" +
                    ") " );            
            st.execute("INSERT INTO "+nodesName +" (SELECT CAST((row_number() over()) as INTEGER) , c.the_geom FROM (SELECT A.THE_GEOM FROM "+ PTS_TABLE + " as A," + PTS_TABLE + " as B " +
                    "WHERE A.THE_GEOM && B.THE_GEOM AND A.THE_GEOM=B.THE_GEOM " +
                    "GROUP BY A.ID " +
                    "HAVING A.ID=MIN(B.ID)) as c);");
        }
    }

    /**
     * Create the edges table.
     */
    private static void edgesTable(Statement st,
                                   TableLocation nodesName,
                                   TableLocation edgesName,
                                   double tolerance, DBTypes dbType, String columns) throws SQLException {
        LOGGER.debug("Creating the edges table...");
        if (tolerance > 0) {
            if (dbType == DBTypes.H2 || dbType == DBTypes.H2GIS) {
                st.execute("CREATE SPATIAL INDEX ON " + nodesName + "(EXP);");
                st.execute("CREATE SPATIAL INDEX ON "+ COORDS_TABLE+"(START_POINT_EXP);");
                st.execute("CREATE SPATIAL INDEX ON "+ COORDS_TABLE+"(END_POINT_EXP);");
            } else {
                st.execute("CREATE  INDEX ON " + nodesName + " USING GIST(EXP);");
                st.execute("CREATE  INDEX ON "+ COORDS_TABLE+" USING GIST(START_POINT_EXP);");
                st.execute("CREATE  INDEX ON "+ COORDS_TABLE+" USING GIST(END_POINT_EXP);");
            }
            st.execute("CREATE TABLE " + edgesName + " AS " +
                    "SELECT EDGE_ID, " +
                    "(SELECT NODE_ID FROM " + nodesName +
                    " WHERE " + nodesName + ".EXP && "+ COORDS_TABLE+".START_POINT_EXP LIMIT 1) START_NODE, " +
                    "(SELECT NODE_ID FROM " + nodesName +
                    " WHERE " + nodesName + ".EXP && "+ COORDS_TABLE+".END_POINT_EXP LIMIT 1) END_NODE " +
                    columns +
                    " FROM "+ COORDS_TABLE+";");
            st.execute("ALTER TABLE " + nodesName + " DROP COLUMN EXP;");
        } else {
            if (dbType == DBTypes.H2 || dbType == DBTypes.H2GIS) {
                st.execute("CREATE SPATIAL INDEX ON " + nodesName + "(THE_GEOM);");
                st.execute("CREATE SPATIAL INDEX ON "+ COORDS_TABLE+"(START_POINT);");
                st.execute("CREATE SPATIAL INDEX ON "+ COORDS_TABLE+"(END_POINT);");
            } else {
                st.execute("CREATE INDEX ON " + nodesName + " USING GIST(THE_GEOM);");
                st.execute("CREATE INDEX ON "+ COORDS_TABLE+" USING GIST(START_POINT);");
                st.execute("CREATE INDEX ON "+ COORDS_TABLE+" USING GIST(END_POINT);");
            }
            // If the tolerance is zero, then we can use = on the geometries
            // instead of && on the envelopes.
            st.execute("CREATE TABLE " + edgesName + " AS " +
                    "SELECT EDGE_ID, " +
                    "(SELECT NODE_ID FROM " + nodesName +
                    " WHERE " + nodesName + ".THE_GEOM && "+ COORDS_TABLE+".START_POINT " +
                    "AND " + nodesName + ".THE_GEOM="+ COORDS_TABLE+".START_POINT LIMIT 1) START_NODE, " +
                    "(SELECT NODE_ID FROM " + nodesName +
                    " WHERE " + nodesName + ".THE_GEOM && "+ COORDS_TABLE+".END_POINT " +
                    "AND " + nodesName + ".THE_GEOM="+ COORDS_TABLE+".END_POINT LIMIT 1) END_NODE " +
                    columns +
                    " FROM "+ COORDS_TABLE+";");
        }
    }

    /**
     * Edges direction according the slope (start and end z)
     * @param st {@link Statement}
     * @param nodesName nodes table name
     * @param edgesName edges table name
     */
    private static void orientBySlope(Statement st,
                                      TableLocation nodesName,
                                      TableLocation edgesName) throws SQLException {
        LOGGER.debug("Orienting edges by slope...");
        st.execute("UPDATE " + edgesName + " c " +
                    "SET START_NODE=END_NODE, " +
                    "    END_NODE=START_NODE " +
                    "WHERE (SELECT ST_Z(A.THE_GEOM) < ST_Z(B.THE_GEOM) " +
                            "FROM " + nodesName + " A, " + nodesName + " B " +
                            "WHERE C.START_NODE=A.NODE_ID AND C.END_NODE=B.NODE_ID);");
    }

    private static void checkForNullEdgeEndpoints(Statement st,
                                                  TableLocation edgesName) throws SQLException {
        LOGGER.debug("Checking for null edge endpoints...");
        try (ResultSet nullEdges = st.executeQuery("SELECT COUNT(*) FROM " + edgesName + " WHERE " +
                "START_NODE IS NULL OR END_NODE IS NULL;")) {
            nullEdges.next();
            final int n = nullEdges.getInt(1);
            if (n > 0) {
                String msg = "There " + (n == 1 ? "is one edge " : "are " + n + " edges ");
                throw new IllegalStateException(msg + "with a null start node or end node. " +
                        "Try using a slightly smaller tolerance.");
            }
        }
    }
}
