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

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;

import java.sql.*;
import java.util.*;

/**
 * Implements the DBSCAN (Density-Based Spatial Clustering of Applications with Noise) algorithm.
 */
public class ClusterDBSCAN extends AbstractCluster {

    private final double eps;
    private final int minPoints;
    private ArrayList<ClusterGeometry> clusterGeometries;
    private Iterator<ClusterGeometry> clusterIterator;

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
        if (clusterIterator.hasNext()) {
            ClusterGeometry pt = clusterIterator.next();
            Integer clusterId = (pt.label == NOISE) ? null : pt.label;
            Integer clusterSize = (pt.label == NOISE) ? null : pt.clusterSize;
            return new Object[]{pt.originalId, pt.geom, clusterId, clusterSize};
        }
        return null;
    }

    @Override
    public void reset() throws SQLException {
        computeClusters();
        firstRow = false;
        clusterIterator = clusterGeometries.iterator();
    }

    @Override
    protected void computeClusters() throws SQLException {
        STRtree tree = new STRtree();
        clusterGeometries = loadPoints(tree);
        int nextCluster = 1;
        for (ClusterGeometry pt : clusterGeometries) {
            if (pt.label != UNVISITED) continue;
            List<ClusterGeometry> neighbors = regionQuery(tree, pt, eps);

            if (neighbors.size() < minPoints) {
                pt.label = NOISE;
            } else {
                pt.label = nextCluster;
                expandCluster(tree, neighbors, nextCluster, eps, minPoints);
                nextCluster++;
            }
        }
    }

    private ArrayList<ClusterGeometry> loadPoints(STRtree tree) throws SQLException {
        ArrayList<ClusterGeometry> list = new ArrayList<>();
        String sql = "SELECT " + idColumn + ", " + geomColumn + " FROM " + tableLocation;
        try (Statement stmt = connection.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
            while (res.next()) {
                Object id = res.getObject(1);
                Geometry geom = (Geometry) res.getObject(2);
                if (geom != null) {
                    ClusterGeometry pt = new ClusterGeometry(id, geom);
                    tree.insert(geom.getEnvelopeInternal(), pt);
                    list.add(pt);
                }
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<ClusterGeometry> regionQuery(STRtree tree, ClusterGeometry center, double eps) {
        Envelope searchEnv = new Envelope(center.geom.getEnvelopeInternal());
        searchEnv.expandBy(eps);
        List<ClusterGeometry> candidates = tree.query(searchEnv);

        List<ClusterGeometry> result = new ArrayList<>(candidates.size());
        for (ClusterGeometry candidate : candidates) {
            if (center.geom.distance(candidate.geom) <= eps) {
                result.add(candidate);
            }
        }
        return result;
    }

    private void expandCluster(STRtree tree, List<ClusterGeometry> seeds, int clusterId, double eps, int minPoints) {
        Queue<ClusterGeometry> queue = new LinkedList<>(seeds);
        int size = 0;

        while (!queue.isEmpty()) {
            ClusterGeometry current = queue.poll();
            if (current.label == NOISE) {
                current.label = clusterId;
                size++;
            }
            if (current.label != UNVISITED) continue;
            current.label = clusterId;
            size++;

            List<ClusterGeometry> currentNeighbors = regionQuery(tree, current, eps);
            if (currentNeighbors.size() >= minPoints) {
                for (ClusterGeometry neighbor : currentNeighbors) {
                    if (neighbor.label == UNVISITED || neighbor.label == NOISE) {
                        queue.add(neighbor);
                    }
                }
            }
        }
        for (ClusterGeometry pt : seeds) {
            if (pt.label == clusterId) {
                pt.setClusterSize(size + 1);
            }
        }
    }
}