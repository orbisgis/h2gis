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

package org.h2gis.functions.spatial.clusters;

import java.sql.*;
import java.util.*;

/**
 * Implements ST_ClusterIntersecting: groups geometries that intersect each other.
 */
public class ClusterIntersecting extends AbstractCluster {

    public ClusterIntersecting(Connection connection, String tableName,
                                 String geomColumn, String idColumn) throws SQLException {
        super(connection, tableName, geomColumn, idColumn);
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
            Integer clusterId = (info != null) ? info[0] : null;
            Integer clusterSize = (info != null) ? info[1] : null;
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

    private Map<Object, int[]> clusterResults;

    @Override
    protected void computeClusters() throws SQLException {
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

        int[] parent = new int[n];
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }

        String pairSql =
                "SELECT a." + idColumn + ", b." + idColumn +
                        " FROM " + tableLocation + " a, " + tableLocation + " b" +
                        " WHERE a." + idColumn + " < b." + idColumn +
                        " AND ST_Intersects(a." + geomColumn + ", b." + geomColumn + ")";

        try (Statement stmt = connection.createStatement();
             ResultSet res = stmt.executeQuery(pairSql)) {
            while (res.next()) {
                Integer i = idx.get(res.getObject(1));
                Integer j = idx.get(res.getObject(2));
                if (i != null && j != null) {
                    UnionFind.union(parent, rank, i, j);
                }
            }
        }

        Map<Integer, Integer> sizeByRoot = new HashMap<>();
        for (int i = 0; i < n; i++) {
            sizeByRoot.merge(UnionFind.find(parent, i), 1, Integer::sum);
        }

        Map<Integer, Integer> labelByRoot = new HashMap<>();
        int nextLabel = 1;
        for (int i = 0; i < n; i++) {
            int root = UnionFind.find(parent, i);
            if (!labelByRoot.containsKey(root)) {
                labelByRoot.put(root, nextLabel++);
            }
        }

        clusterResults = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            int root = UnionFind.find(parent, i);
            int label = labelByRoot.get(root);
            int size = sizeByRoot.get(root);
            clusterResults.put(ids.get(i), new int[]{label, size});
        }
    }
}