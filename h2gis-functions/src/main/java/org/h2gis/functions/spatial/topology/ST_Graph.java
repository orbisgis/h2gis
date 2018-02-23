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
package org.h2gis.functions.spatial.topology;


import java.sql.*;
import java.util.List;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;

import org.h2gis.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static final String TYPE_ERROR = "Only LINESTRINGs " +
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
     * <p/>
     * Since no column is specified in this signature, we take the first
     * geometry column we find.
     * <p/>
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection Connection
     * @param tableName  Input table containing LINESTRINGs
     * @return true if both output tables were created
     * @throws SQLException
     */
    public static boolean createGraph(Connection connection,
                                      String tableName) throws SQLException {
        return createGraph(connection, tableName, null);
    }

    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs in the given column.
     * <p/>
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection       Connection
     * @param tableName        Input table
     * @param spatialFieldName Name of column containing LINESTRINGs
     * @return true if both output tables were created
     * @throws SQLException
     */
    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      String spatialFieldName) throws SQLException {
        // The default tolerance is zero.
        return createGraph(connection, tableName, spatialFieldName, 0.0);
    }

    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs in the given column and using the given
     * tolerance.
     * <p/>
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     * <p/>
     * If the input table has name 'input', then the output tables are named
     * 'input_nodes' and 'input_edges'.
     *
     * @param connection       Connection
     * @param tableName        Input table
     * @param spatialFieldName Name of column containing LINESTRINGs
     * @param tolerance        Tolerance
     * @return true if both output tables were created
     * @throws SQLException
     */
    public static boolean createGraph(Connection connection,
                                      String tableName,
                                      String spatialFieldName,
                                      double tolerance) throws SQLException {
        // By default we do not orient by slope.
        return createGraph(connection, tableName, spatialFieldName, tolerance, false);
    }
    
    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs in the given column and using the given
     * tolerance, and potentially orienting edges by slope.
     * <p/>
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     * <p/>
     * The boolean orientBySlope is set to true if edges should be oriented by
     * the z-value of their first and last coordinates (decreasing).
     * <p/>
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
     * @throws SQLException
     */
    public static boolean createGraph(Connection connection,
                                      String inputTable,
                                      String spatialFieldName,
                                      double tolerance,
                                      boolean orientBySlope) throws SQLException {
         return createGraph(connection, inputTable, spatialFieldName, tolerance, orientBySlope, false);
     }

    /**
     * Create the nodes and edges tables from the input table containing
     * LINESTRINGs in the given column and using the given
     * tolerance, and potentially orienting edges by slope.
     * <p/>
     * The tolerance value is used specify the side length of a square Envelope
     * around each node used to snap together other nodes within the same
     * Envelope. Note, however, that edge geometries are left untouched.
     * Note also that coordinates within a given tolerance of each
     * other are not necessarily snapped together. Only the first and last
     * coordinates of a geometry are considered to be potential nodes, and
     * only nodes within a given tolerance of each other are snapped
     * together. The tolerance works only in metric units.
     * <p/>
     * The boolean orientBySlope is set to true if edges should be oriented by
     * the z-value of their first and last coordinates (decreasing).
     * <p/>
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
     * @throws SQLException
     */
    public static boolean createGraph(Connection connection,
                                      String inputTable,
                                      String spatialFieldName,
                                      double tolerance,
                                      boolean orientBySlope,
                                      boolean deleteTables) throws SQLException {
        if (tolerance < 0) {
            throw new IllegalArgumentException("Only positive tolerances are allowed.");
        }
        final TableLocation tableName = TableUtilities.parseInputTable(connection, inputTable);
        final TableLocation nodesName = TableUtilities.suffixTableLocation(tableName, NODES_SUFFIX);
        final TableLocation edgesName = TableUtilities.suffixTableLocation(tableName, EDGES_SUFFIX); 
        boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        if(deleteTables){            
            Statement stmt = connection.createStatement();
            StringBuilder sb = new StringBuilder("drop table if exists ");
            sb.append(nodesName.toString(isH2)).append(",").append(edgesName.toString(isH2));
            stmt.execute(sb.toString());
            stmt.close();
        }
        // Check if ST_Graph has already been run on this table.
        else if (JDBCUtilities.tableExists(connection, nodesName.getTable()) ||
                JDBCUtilities.tableExists(connection, edgesName.getTable())) {
            throw new IllegalArgumentException(ALREADY_RUN_ERROR + tableName.getTable());
        }
        //Tables used to store intermediate data
        PTS_TABLE = TableLocation.parse(System.currentTimeMillis()+"_PTS", isH2).toString();
        COORDS_TABLE = TableLocation.parse(System.currentTimeMillis()+"_COORDS", isH2).toString();
        // Check for a primary key
        final int pkIndex = JDBCUtilities.getIntegerPrimaryKey(connection, tableName.getTable());
        if (pkIndex == 0) {
            throw new IllegalStateException("Table " + tableName.getTable()
                    + " must contain a single integer primary key.");
        }
        final DatabaseMetaData md = connection.getMetaData();
        final String pkColName = JDBCUtilities.getFieldName(md, tableName.getTable(), pkIndex);
        // Check the geometry column type;
        final Object[] spatialFieldIndexAndName = getSpatialFieldIndexAndName(connection, tableName, spatialFieldName);
        int spatialFieldIndex = (int) spatialFieldIndexAndName[1];
        spatialFieldName = (String) spatialFieldIndexAndName[0];
        checkGeometryType(connection, tableName, spatialFieldIndex);
        final String geomCol = JDBCUtilities.getFieldName(md, tableName.getTable(), spatialFieldIndex);
        final Statement st = connection.createStatement();
        try {
            firstFirstLastLast(st, tableName, pkColName, geomCol, tolerance);            
            int srid = SFSUtilities.getSRID(connection, tableName, spatialFieldName);
            makeEnvelopes(st, tolerance, isH2, srid);
            nodesTable(st, nodesName, tolerance, isH2,srid);
            edgesTable(st, nodesName, edgesName, tolerance, isH2);
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

    private static void checkGeometryType(Connection connection,
            TableLocation tableName,
            int spatialFieldIndex) throws SQLException {
        final String fieldName
                = JDBCUtilities.getFieldName(connection.getMetaData(), tableName.getTable(), spatialFieldIndex);
        int geomType = SFSUtilities.getGeometryType(connection, tableName, fieldName);
        if (geomType != GeometryTypeCodes.LINESTRING) {
            throw new IllegalArgumentException(TYPE_ERROR
                    + SFSUtilities.getGeometryTypeNameFromCode(geomType));
        }
    }

    /**
     * Get the column index of the given spatial field, or the first one found
     * if none is given (specified by null).
     * 
     * Return the first geometry field if the spatialFieldName name is null.
     *
     * @param spatialFieldName Spatial field name
     * @return Spatial field index and its name
     * @throws SQLException
     */
    private static Object[] getSpatialFieldIndexAndName(Connection connection,
                                            TableLocation tableName,
                                            String spatialFieldName) throws SQLException {
        // Find the name of the first geometry column if not provided by the user.
        if (spatialFieldName == null) {
            List<String> geomFields = SFSUtilities.getGeometryFields(connection, tableName);
            if (!geomFields.isEmpty()) {
                spatialFieldName = geomFields.get(0);
            } else {
                throw new SQLException("Table " + tableName + " does not contain a geometry field.");
            }
        }
        // Set up tables
        final ResultSet columns = connection.getMetaData()
                .getColumns(tableName.getCatalog(null), tableName.getSchema(null), tableName.getTable(), null);
        int spatialFieldIndex = -1;
        try {
            while (columns.next()) {
                if (columns.getString("COLUMN_NAME").equalsIgnoreCase(spatialFieldName)) {
                    spatialFieldIndex = columns.getRow();
                }
            }
        } finally {
            columns.close();
        }
        if (spatialFieldIndex == -1) {
            throw new SQLException("Geometry field " + spatialFieldName + " of table " + tableName + " not found");
        }
        return new Object[]{spatialFieldName,spatialFieldIndex};
    }

    private static String expand(String geom, double tol) {
        return "ST_Expand(" + geom + ", " + tol + ")";
    }

    /**
     * Return the first and last coordinates table
     * @param st
     * @param tableName
     * @param pkCol
     * @param geomCol
     * @param tolerance
     * @throws SQLException 
     */
    private static void firstFirstLastLast(Statement st,
                                           TableLocation tableName,
                                           String pkCol,
                                           String geomCol,
                                           double tolerance) throws SQLException {
        LOGGER.info("Selecting the first coordinate of the first geometry and " +
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
                    + "FROM " + tableName);
        } else {
            // If the tolerance is zero, there is no need to call ST_Expand.
            st.execute("CREATE  TABLE "+ COORDS_TABLE+" AS "
                    + "SELECT " + pkCol + " EDGE_ID, "
                    + firstPointFirstGeom + " START_POINT, "
                    + lastPointLastGeom + " END_POINT "
                    + "FROM " + tableName);
        }
    }

    /**
     * Make a big table of all points in the coords table with an envelope around each point.
     * We will use this table to remove duplicate points.
     */
    private static void makeEnvelopes(Statement st, double tolerance, boolean isH2, int srid) throws SQLException {
        st.execute("DROP TABLE IF EXISTS"+ PTS_TABLE + ";");
        if (tolerance > 0) {
            LOGGER.info("Calculating envelopes around coordinates...");            
            if (isH2) {
                // Putting all points and their envelopes together...
                st.execute("CREATE  TABLE "+ PTS_TABLE +"( "
                        + "ID SERIAL PRIMARY KEY, "
                        + "THE_GEOM POINT, "
                        + "AREA POLYGON "
                        + ") AS "
                        + "SELECT NULL, START_POINT, START_POINT_EXP FROM "+ COORDS_TABLE
                        + " UNION ALL "
                        + "SELECT NULL, END_POINT, END_POINT_EXP FROM "+ COORDS_TABLE+";");
                // Putting a spatial index on the envelopes...
                st.execute("CREATE SPATIAL INDEX ON "+ PTS_TABLE +"(AREA);");
            } else {
                // Putting all points and their envelopes together...
                st.execute("CREATE  TABLE "+ PTS_TABLE + "( ID SERIAL PRIMARY KEY, "
                        + "THE_GEOM GEOMETRY(POINT,"+srid+"),"
                        + "AREA GEOMETRY(POLYGON, "+srid+")"
                        + ") ");
                 st.execute("INSERT INTO " + PTS_TABLE +" (SELECT (row_number() over())::int , a.THE_GEOM, A.AREA FROM  "
                        + "(SELECT  START_POINT AS THE_GEOM, START_POINT_EXP as AREA FROM "+ COORDS_TABLE
                        + " UNION ALL "
                        + "SELECT  END_POINT AS THE_GEOM, END_POINT_EXP as AREA FROM "+ COORDS_TABLE+") as a);");
                // Putting a spatial index on the envelopes...
                st.execute("CREATE INDEX ON "+ PTS_TABLE +" USING GIST(AREA);");
            }
        } else {
            LOGGER.info("Preparing temporary nodes table from coordinates...");
            if (isH2) {
                // If the tolerance is zero, we just put all points together
                st.execute("CREATE  TABLE "+ PTS_TABLE +"( "
                        + "ID SERIAL PRIMARY KEY, "
                        + "THE_GEOM POINT"
                        + ") AS "
                        + "SELECT NULL, START_POINT FROM "+ COORDS_TABLE
                        + " UNION ALL "
                        + "SELECT NULL, END_POINT FROM "+ COORDS_TABLE+";");
                // Putting a spatial index on the points themselves...
                st.execute("CREATE SPATIAL INDEX ON "+ PTS_TABLE +"(THE_GEOM);");
            } else {
                // If the tolerance is zero, we just put all points together
                st.execute("CREATE  TABLE "+ PTS_TABLE +"( "
                        + "ID SERIAL PRIMARY KEY, "
                        + "THE_GEOM GEOMETRY(POINT,"+srid+")"
                        + ")");
                st.execute("INSERT INTO "+ PTS_TABLE +" (SELECT (row_number() over())::int , a.the_geom FROM "
                        + "(SELECT  START_POINT as THE_GEOM FROM "+ COORDS_TABLE
                        + " UNION ALL "
                        + "SELECT  END_POINT as THE_GEOM FROM "+ COORDS_TABLE+") as a);");
                // Putting a spatial index on the points themselves...
                st.execute("CREATE INDEX ON "+ PTS_TABLE + " USING GIST(THE_GEOM);");
            }            
        }
    }

    /**
     * Create the nodes table.
     */
    private static void nodesTable(Statement st,
                                   TableLocation nodesName,
                                   double tolerance, boolean isH2, int srid) throws SQLException {
        LOGGER.info("Creating the nodes table...");
        // Creating nodes table by removing copies from the pts table.
        if (tolerance > 0) {
            if(isH2){
            st.execute("CREATE TABLE " + nodesName + "(" +
                    "NODE_ID SERIAL PRIMARY KEY, " +
                    "THE_GEOM POINT, " +
                    "EXP POLYGON" +
                    ") AS " +
                    "SELECT NULL, A.THE_GEOM, A.AREA FROM "+ PTS_TABLE + " as A," + PTS_TABLE +" as B " +
                    "WHERE A.AREA && B.AREA " +
                    "GROUP BY A.ID " +
                    "HAVING A.ID=MIN(B.ID);");
            }
            else{
               st.execute("CREATE TABLE " + nodesName + "(" +
                    "NODE_ID SERIAL PRIMARY KEY, " +
                    "THE_GEOM GEOMETRY(POINT, " + srid+"), "+
                    "EXP GEOMETRY(POLYGON," +srid+")"+
                    ") " );
                st.execute( "INSERT INTO "+nodesName +" (SELECT (row_number() over())::int , c.the_geom, c.area FROM (SELECT  A.THE_GEOM, A.AREA FROM "+ PTS_TABLE +" as  A, "+ PTS_TABLE +" as B " +
                    "WHERE A.AREA && B.AREA " +
                    "GROUP BY A.ID " +
                    "HAVING A.ID=MIN(B.ID)) as c);"); 
            }
        } else {
            
            if(isH2){
               // If the tolerance is zero, we can create the NODES table
            // by using = rather than &&.
            st.execute("CREATE TABLE " + nodesName + "(" +
                    "NODE_ID SERIAL PRIMARY KEY, " +
                    "THE_GEOM POINT" +
                    ") AS " +
                    "SELECT NULL, A.THE_GEOM FROM "+ PTS_TABLE + " as A," + PTS_TABLE + " as B " +
                    "WHERE A.THE_GEOM && B.THE_GEOM AND A.THE_GEOM=B.THE_GEOM " +
                    "GROUP BY A.ID " +
                    "HAVING A.ID=MIN(B.ID);"); 
            }
            else{
            // If the tolerance is zero, we can create the NODES table
            // by using = rather than &&.
            st.execute("CREATE TABLE " + nodesName + "(" +
                    "NODE_ID SERIAL PRIMARY KEY, " +
                    "THE_GEOM GEOMETRY(POINT, "+srid+")" +
                    ") " );            
            st.execute("INSERT INTO "+nodesName +" (SELECT (row_number() over())::int , c.the_geom FROM (SELECT A.THE_GEOM FROM "+ PTS_TABLE + " as A," + PTS_TABLE + " as B " +
                    "WHERE A.THE_GEOM && B.THE_GEOM AND A.THE_GEOM=B.THE_GEOM " +
                    "GROUP BY A.ID " +
                    "HAVING A.ID=MIN(B.ID)) as c);");
            }
        }
    }

    /**
     * Create the edges table.
     */
    private static void edgesTable(Statement st,
                                   TableLocation nodesName,
                                   TableLocation edgesName,
                                   double tolerance, boolean isH2) throws SQLException {
        LOGGER.info("Creating the edges table...");
        if (tolerance > 0) {
            if (isH2) {
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
                    "FROM "+ COORDS_TABLE+";");
            st.execute("ALTER TABLE " + nodesName + " DROP COLUMN EXP;");
        } else {
            if (isH2) {
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
                    "FROM "+ COORDS_TABLE+";");
        }
    }

    /**
     * Edges direction according the slope (start and end z)
     * @param st
     * @param nodesName
     * @param edgesName
     * @throws SQLException 
     */
    private static void orientBySlope(Statement st,
                                      TableLocation nodesName,
                                      TableLocation edgesName) throws SQLException {
        LOGGER.info("Orienting edges by slope...");
        st.execute("UPDATE " + edgesName + " c " +
                    "SET START_NODE=END_NODE, " +
                    "    END_NODE=START_NODE " +
                    "WHERE (SELECT ST_Z(A.THE_GEOM) < ST_Z(B.THE_GEOM) " +
                            "FROM " + nodesName + " A, " + nodesName + " B " +
                            "WHERE C.START_NODE=A.NODE_ID AND C.END_NODE=B.NODE_ID);");
    }

    private static void checkForNullEdgeEndpoints(Statement st,
                                                  TableLocation edgesName) throws SQLException {
        LOGGER.info("Checking for null edge endpoints...");
        final ResultSet nullEdges = st.executeQuery("SELECT COUNT(*) FROM " + edgesName + " WHERE " +
                "START_NODE IS NULL OR END_NODE IS NULL;");
        try {
            nullEdges.next();
            final int n = nullEdges.getInt(1);
            if (n > 0) {
                String msg = "There " + (n == 1 ? "is one edge " : "are " + n + " edges ");
                throw new IllegalStateException(msg + "with a null start node or end node. " +
                        "Try using a slightly smaller tolerance.");
            }
        } finally {
            nullEdges.close();
        }
    }
}
