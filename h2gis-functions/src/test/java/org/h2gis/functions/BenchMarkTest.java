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
package org.h2gis.functions;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A class to perform single benchmark
 * @author Erwan Bocher, CNRS
 */
public class BenchMarkTest {

    private static Connection connection;
    private static final String DB_NAME = BenchMarkTest.class.getSimpleName() + "_PerformanceTest";

    /**
     * Number of run not measured, to warm the JVM.
     */
    private static final int WARMUP = 3;

    /**
     * Number of run of each query.
     */
    private static final int RUNS = 10;

    /**
     * Queries that create the data.
     * They are executed once, before the runs.
     */
    private static final String[] DATA = {
            "CALL RAND(42);" +
                    "DROP TABLE IF EXISTS POINTS_INDEXED; " +
                    "CREATE TABLE POINTS_INDEXED AS SELECT st_makepoint(-60 + random() * 200, 30 + random() * 200) AS THE_GEOM FROM GENERATE_SERIES(0, 100000);" +                    "CREATE SPATIAL INDEX ON POINTS_INDEXED(THE_GEOM);",
            "DROP TABLE IF EXISTS POLYGON_INDEXED;" +
                    "CREATE TABLE POLYGON_INDEXED AS SELECT * FROM ST_MAKEGRID('POINTS_INDEXED', 5, 5);" +
                    "CREATE SPATIAL INDEX ON POLYGON_INDEXED(THE_GEOM);",
            "DROP TABLE IF EXISTS CIRCLES_INDEXED;" +
                    "CREATE TABLE CIRCLES_INDEXED AS SELECT X AS ID, " +
                    "ST_Buffer(ST_MakePoint(-60 + MOD(X, 20) * 10, 30 + (X / 20) * 10), 8, 'quad_segs=64') AS THE_GEOM " +
                    "FROM GENERATE_SERIES(0, 399);" +
                    "CREATE SPATIAL INDEX ON CIRCLES_INDEXED(THE_GEOM);",
            "DROP TABLE IF EXISTS ONE_BIG_POLYGON;" +
                    "CREATE TABLE ONE_BIG_POLYGON AS SELECT 1 AS ID, " +
                    "ST_Densify(ST_Buffer(ST_MakePoint(40, 130), 80, 'quad_segs=64'), 0.1) AS THE_GEOM ",
            "DROP TABLE IF EXISTS POLYGON_DIFF_SIZES;" +
                    "CREATE TABLE POLYGON_DIFF_SIZES AS SELECT Q.QS * 4 AS NB_POINTS, " +
                    "ST_Buffer(ST_MakePoint(40, 130), 10, CONCAT('quad_segs=', Q.QS)) AS THE_GEOM " +
                    "FROM (VALUES (25), (250), (1250), (2500)) AS Q(QS);"
    };

    /**
     * Measured queries: {name, sql}.
     * They must be repeatable (SELECT): each one is executed WARMUP + RUNS times.
     */
    private static final String[][] QUERIES = {
            {"points intersect polygons", "SELECT COUNT(*) FROM POINTS_INDEXED A, POLYGON_INDEXED B "
                     + "WHERE A.THE_GEOM && B.THE_GEOM AND ST_Intersects(A.THE_GEOM, B.THE_GEOM)"},
            {"polygons contain points", "SELECT COUNT(*) FROM POLYGON_INDEXED A, POINTS_INDEXED B "
                    + "WHERE A.THE_GEOM && B.THE_GEOM AND ST_CONTAINS(A.THE_GEOM, B.THE_GEOM)"},
            {"points intersect large polygons", "SELECT COUNT(*) FROM POINTS_INDEXED A, CIRCLES_INDEXED B "
                    + "WHERE A.THE_GEOM && B.THE_GEOM AND ST_Intersects(A.THE_GEOM, B.THE_GEOM)"},
            {"large polygons contain points", "SELECT COUNT(*) FROM  CIRCLES_INDEXED A, POINTS_INDEXED B "
                    + "WHERE A.THE_GEOM && B.THE_GEOM AND ST_CONTAINS(A.THE_GEOM, B.THE_GEOM)"},
            {"points intersect one large polygon", "SELECT COUNT(*) FROM POINTS_INDEXED A, ONE_BIG_POLYGON B "
                    + "WHERE A.THE_GEOM && B.THE_GEOM AND ST_Intersects(A.THE_GEOM, B.THE_GEOM)"},
            {"one large polygon contain points", "SELECT COUNT(*) FROM ONE_BIG_POLYGON A, POINTS_INDEXED B "
                    + "WHERE A.THE_GEOM && B.THE_GEOM AND ST_CONTAINS(A.THE_GEOM, B.THE_GEOM)"},  {"points in circle AND in big polygon",
            "SELECT COUNT(*) FROM ONE_BIG_POLYGON P, CIRCLES_INDEXED C, POINTS_INDEXED A "
                    + "WHERE C.THE_GEOM && P.THE_GEOM AND A.THE_GEOM && C.THE_GEOM "
                    + "AND ST_Contains(C.THE_GEOM, A.THE_GEOM) AND ST_Contains(P.THE_GEOM, A.THE_GEOM)"},
            {"points in circle, outside big polygon",
                    "SELECT COUNT(*) FROM ONE_BIG_POLYGON P, CIRCLES_INDEXED C, POINTS_INDEXED A "
                            + "WHERE A.THE_GEOM && C.THE_GEOM AND ST_Intersects(C.THE_GEOM, A.THE_GEOM) "
                            + "AND NOT ST_Contains(P.THE_GEOM, A.THE_GEOM)"},
            {"count points per circle (scalar subquery)",
                    "SELECT SUM(NB) FROM (SELECT C.ID, (SELECT COUNT(*) FROM POINTS_INDEXED A "
                            + "WHERE A.THE_GEOM && C.THE_GEOM AND ST_Contains(C.THE_GEOM, A.THE_GEOM)) AS NB "
                            + "FROM CIRCLES_INDEXED C)"},
            {"circles with a point outside big polygon (EXISTS)",
                    "SELECT COUNT(*) FROM CIRCLES_INDEXED C WHERE EXISTS (SELECT 1 FROM POINTS_INDEXED A "
                            + "WHERE A.THE_GEOM && C.THE_GEOM AND ST_Contains(C.THE_GEOM, A.THE_GEOM) "
                            + "AND NOT ST_Intersects((SELECT THE_GEOM FROM ONE_BIG_POLYGON), A.THE_GEOM))"},
            {"points in big polygon (geometry subquery)",
                    "SELECT COUNT(*) FROM POINTS_INDEXED A "
                            + "WHERE A.THE_GEOM && (SELECT THE_GEOM FROM ONE_BIG_POLYGON) "
                            + "AND ST_Intersects((SELECT THE_GEOM FROM ONE_BIG_POLYGON), A.THE_GEOM)"},
            {"points in cells covered by big polygon (IN)",
                    "SELECT COUNT(*) FROM POLYGON_INDEXED G, POINTS_INDEXED A "
                            + "WHERE A.THE_GEOM && G.THE_GEOM AND ST_Contains(G.THE_GEOM, A.THE_GEOM) "
                            + "AND G.ID IN (SELECT G2.ID FROM POLYGON_INDEXED G2, ONE_BIG_POLYGON P "
                            + "WHERE G2.THE_GEOM && P.THE_GEOM AND ST_CoveredBy(G2.THE_GEOM, P.THE_GEOM))"},
            {"polygon 100 points contain points", "SELECT COUNT(*) FROM POLYGON_DIFF_SIZES A, POINTS_INDEXED B "
                    + "WHERE A.NB_POINTS = 100 AND A.THE_GEOM && B.THE_GEOM AND ST_Contains(A.THE_GEOM, B.THE_GEOM)"},
            {"polygon 1000 points contain points", "SELECT COUNT(*) FROM POLYGON_DIFF_SIZES A, POINTS_INDEXED B "
                    + "WHERE A.NB_POINTS = 1000 AND A.THE_GEOM && B.THE_GEOM AND ST_Contains(A.THE_GEOM, B.THE_GEOM)"},
            {"polygon 5000 points contain points", "SELECT COUNT(*) FROM POLYGON_DIFF_SIZES A, POINTS_INDEXED B "
                    + "WHERE A.NB_POINTS = 5000 AND A.THE_GEOM && B.THE_GEOM AND ST_Contains(A.THE_GEOM, B.THE_GEOM)"},
            {"polygon 10000 points contain points", "SELECT COUNT(*) FROM POLYGON_DIFF_SIZES A, POINTS_INDEXED B "
                    + "WHERE A.NB_POINTS = 10000 AND A.THE_GEOM && B.THE_GEOM AND ST_Contains(A.THE_GEOM, B.THE_GEOM)"},
    };

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(DB_NAME);
        try (Statement st = connection.createStatement()) {
            for (String sql : DATA) {
                st.execute(sql);
            }
            printInputData(st);
        }
    }

    @AfterAll
    public static void tearDown() throws Exception {
        //Close it after the test
        connection.close();
    }

    /**
     * Print the number of rows and the extent of each table, to check the input data.
     */
    private static void printInputData(Statement st) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (ResultSet rs = st.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = 'PUBLIC' AND COLUMN_NAME = 'THE_GEOM' ORDER BY TABLE_NAME")) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        }
        for (String table : tables) {
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*), ST_Extent(THE_GEOM) FROM " + table)) {
                rs.next();
                System.out.println(table + " : " + rs.getLong(1) + " rows, extent " + rs.getString(2));
            }
        }
    }

    @Disabled("Benchmark, run manually")
    @Test
    public void measureQueries() throws SQLException {
        System.out.printf("%-40s %10s %12s   %s%n", "Query", "min (ms)", "median (ms)", "result");
        try (Statement st = connection.createStatement()) {
            for (String[] query : QUERIES) {
                long[] times = new long[RUNS];
                String result = "";
                for (int i = 0; i < WARMUP + RUNS; i++) {
                    String sql = "/* run " + i + " */ " + query[1];
                    long start = System.nanoTime();
                    result = execute(st, sql);
                    if (i >= WARMUP) {
                        times[i - WARMUP] = (System.nanoTime() - start) / 1_000_000;
                    }
                }
                Arrays.sort(times);
                System.out.printf("%-40s %10d %12d   %s%n", query[0], times[0], times[RUNS / 2], result);
            }
        }
    }

    /**
     * Execute the query and read the whole result.
     *
     * @return the first value of the first row and the number of rows, to compare the results
     */
    private static String execute(Statement st, String sql) throws SQLException {
        if (!st.execute(sql)) {
            return st.getUpdateCount() + " rows updated";
        }
        try (ResultSet rs = st.getResultSet()) {
            String first = null;
            int rows = 0;
            while (rs.next()) {
                if (rows == 0) {
                    first = rs.getString(1);
                }
                rows++;
            }
            return rows == 1 ? String.valueOf(first) : rows + " rows, first value: " + first;
        }
    }
}
