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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.clusters;

import java.sql.*;
import java.util.*;

/**
 * @author Erwan Bocher (CNRS)
 * Implements the DBSCAN algorithm using Union-Find and SQL spatial queries.
 * Core points are identified via a COUNT + HAVING query with a spatial index
 * pre-filter (ST_EXPAND bbox), then neighbors are unioned.
 *
 * <p>The spatial self-join is computed only once and materialized into a
 * CACHED LOCAL TEMPORARY table to avoid repeating the
 * most expensive operation.</p>
 */
public class ClusterDBSCAN extends AbstractCluster {

    private final double eps;
    private final int minPoints;
    private Map<Object, int[]> clusterResults; // id : [clusterId, clusterSize]

    public ClusterDBSCAN(Connection connection, String tableName,
                         String geomColumn, String idColumn,
                         double eps, int minPoints) throws SQLException {
        super(connection, tableName, geomColumn, idColumn);
        if (eps <= 0) {
            throw new SQLException("eps must be greater than 0");
        }
        if (minPoints < 1) {
            throw new SQLException("minPoints must be at least 1");
        }
        this.eps = eps;
        this.minPoints = minPoints;
    }

    @Override
    public Object[] readRow() throws SQLException {
        if (firstRow) {
            reset();
        }
        if (streamRS != null && streamRS.next()) {
            Object id = streamRS.getObject(1);
            Object geom = streamRS.getObject(2);
            int[] info = clusterResults.get(id);
            Integer clusterId   = (info != null && info[0] != NOISE) ? info[0] : null;
            Integer clusterSize = (info != null && info[0] != NOISE) ? info[1] : null;
            return new Object[]{id, geom, clusterId, clusterSize};
        }
        closeStream();
        return null;
    }

    @Override
    public void reset() throws SQLException {
        closeStream();
        computeClusters();
        firstRow = false;
        streamStmt = connection.createStatement();
        streamRS = streamStmt.executeQuery(
                "SELECT " + idColumn + ", " + geomColumn + " FROM " + tableLocation);
    }

    @Override
    protected void computeClusters() throws SQLException {

        // Load IDs
        List<Object> ids = new ArrayList<>();
        Map<Object, Integer> idx = new HashMap<>();

        try (Statement stmt = connection.createStatement();
             ResultSet res = stmt.executeQuery(
                     "SELECT " + idColumn + " FROM " + tableLocation)) {
            while (res.next()) {
                Object id = res.getObject(1);
                idx.put(id, ids.size());
                ids.add(id);
            }
        }

        int n = ids.size();
        if (n == 0) {
            clusterResults = Collections.emptyMap();
            return;
        }

        // Materialise pairs once (CACHED = B-tree on disk, no OOM risk)
        String createPairsSql = String.format(
                "CREATE CACHED LOCAL TEMPORARY TABLE tmp_pairs AS " +
                        "SELECT a.%s AS id_a, b.%s AS id_b " +
                        "FROM %s a, %s b " +
                        "WHERE a.%s < b.%s " +
                        "AND ST_EXPAND(a.%s, %f) && b.%s " +
                        "AND ST_DWithin(a.%s, b.%s, %f)",
                idColumn, idColumn,
                tableLocation, tableLocation,
                idColumn, idColumn,
                geomColumn, eps, geomColumn,
                geomColumn, geomColumn, eps
        );

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS tmp_pairs");
            stmt.execute(createPairsSql);
        }

        try {
            // Identify core points from tmp_pairs
            String corePointSql = String.format(
                    "SELECT id, COUNT(*) AS cnt " +
                            "FROM ( " +
                            "  SELECT id_a AS id FROM tmp_pairs " +
                            "  UNION ALL " +
                            "  SELECT id_b AS id FROM tmp_pairs " +
                            ") t " +
                            "GROUP BY id " +
                            "HAVING COUNT(*) >= %d",
                    minPoints - 1
            );
            Set<Object> coreIds = new HashSet<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet res = stmt.executeQuery(corePointSql)) {
                while (res.next()) {
                    coreIds.add(res.getObject(1));
                }
            }

            // Union-Find approach
            int[] parent = new int[n];
            int[] rank   = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
            }

            // Union pairs where at least one endpoint is a core point
            try (Statement stmt = connection.createStatement();
                 ResultSet res = stmt.executeQuery("SELECT id_a, id_b FROM tmp_pairs")) {
                while (res.next()) {
                    Object idA = res.getObject(1);
                    Object idB = res.getObject(2);
                    if (coreIds.contains(idA) || coreIds.contains(idB)) {
                        Integer i = idx.get(idA);
                        Integer j = idx.get(idB);
                        if (i != null && j != null) {
                            UnionFind.union(parent, rank, i, j);
                        }
                    }
                }
            }

            // Compute sizes and assign sequential labels
            Map<Integer, Integer> sizeByRoot  = new HashMap<>();
            Map<Integer, Integer> labelByRoot = new HashMap<>();

            for (int i = 0; i < n; i++) {
                sizeByRoot.merge(UnionFind.find(parent, i), 1, Integer::sum);
            }

            int nextLabel = 1;
            for (int i = 0; i < n; i++) {
                int root = UnionFind.find(parent, i);
                if (!labelByRoot.containsKey(root)) {
                    labelByRoot.put(root, nextLabel++);
                }
            }

            // Build result map
            clusterResults = new HashMap<>((int) (n / 0.75) + 1);
            for (int i = 0; i < n; i++) {
                Object id  = ids.get(i);
                int root   = UnionFind.find(parent, i);
                int size   = sizeByRoot.get(root);
                boolean isNoise = !coreIds.contains(id) && size < minPoints;
                int label = isNoise ? NOISE : labelByRoot.get(root);
                clusterResults.put(id, new int[]{label, size});
            }

        } finally {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS tmp_pairs");
            }
        }
    }
}