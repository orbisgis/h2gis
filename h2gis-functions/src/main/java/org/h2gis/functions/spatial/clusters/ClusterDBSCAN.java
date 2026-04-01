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

import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;

import java.sql.*;
import java.util.*;

/**
 * @author Erwan Bocher (CNRS)
 * Implements the DBSCAN (Density-Based Spatial Clustering of Applications with Noise) algorithm
 * for spatial clustering. This class allows clustering geometries stored in a database table
 * and returns the cluster ID and size for each geometry.
 */
public class ClusterDBSCAN  implements SimpleRowSource {

    private static final int UNVISITED = -1;
    private static final int NOISE     = -2;
    private final String idColumn;
    private final double eps;
    private final int minPoints;
    // If true, table query is closed the read again
    public boolean firstRow = true;
    public String tableName;
    public String geomColumn;
    public Connection connection;
    private final TableLocation tableLocation;
    private ArrayList<ClusterGeometry> clusterGeometries;
    private Iterator<ClusterGeometry> clusterIterators;

    /**
     * Constructs a new ClusterDBSCAN instance.
     *
     * @param connection The database connection.
     * @param tableName The name of the table containing the geometries.
     * @param geomColumn The name of the geometry column.
     * @param idColumn The name of the ID column.
     * @param eps The maximum distance between two points to be considered in the same neighborhood (must be > 0).
     * @param minPoints The minimum number of points required to form a cluster (must be >= 1).
     */
    public  ClusterDBSCAN(Connection connection, String tableName, String geomColumn,  String  idColumn,
                          double     eps,
                          int        minPoints) throws SQLException {
        if (eps <= 0) {
            throw new SQLException("eps must be greater than 0");
        }
        if (minPoints < 1) {
            throw new SQLException("minPoints must be at least 1");
        }
        this.tableName = tableName;
        this.tableLocation=TableLocation.parse(tableName, DBUtils.getDBType(connection));
        this.geomColumn = geomColumn;
        this.idColumn = idColumn;
        this.eps=eps;
        this.minPoints=minPoints;
        this.connection = connection;
    }

    /**
     * Reads a row from the result set.
     *
     * @return An array of objects representing the row data.
     * @throws SQLException If a database access error occurs.
     */
    @Override
    public Object[] readRow() throws SQLException {
        if(firstRow) {
            reset();
        }
        if(clusterIterators.hasNext()) {
            ClusterGeometry pt = clusterIterators.next();
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
        clusterIterators = clusterGeometries.iterator();
    }

    @Override
    public void close() {
    }

    /**
     * Returns the result set containing the clustered geometries.
     *
     * @return The result set with the clustered geometries.
     * @throws SQLException If a database access error occurs.
     */
    public ResultSet getResultSet() throws SQLException {
        SimpleResultSet rs = new SimpleResultSet(this);
        // Feed with fields
        getMetadata(rs);
        rs.addColumn("CLUSTER_ID", Types.INTEGER, 10, 0);
        rs.addColumn("CLUSTER_SIZE", Types.INTEGER, 10, 0);
        return rs;
    }

    /**
     * Retrieves metadata for the result set.
     *
     * @param rs The result set to populate with metadata.
     * @throws SQLException If a database access error occurs.
     */
    private void getMetadata(SimpleResultSet rs) throws SQLException {
        String sql = "SELECT " + idColumn + ", " + geomColumn + " FROM " + tableLocation + " limit 0";
        try (Statement stmt = connection.createStatement();
             ResultSet res   = stmt.executeQuery(sql)) {
            // Feed with fields
            TableUtilities.copyFields(rs, res.getMetaData());
        }
    }


    /**
     * Class to manage geometry and identifier
     */
    private static class ClusterGeometry {
        final Object   originalId;
        final Geometry geom;
        int   label = UNVISITED;
        int   clusterSize = 0;

        ClusterGeometry(Object originalId, Geometry geom) {
            this.originalId = originalId;
            this.geom       = geom;
        }

        /**
         * Cluster size
         * @param size size of the cluster
         */
        public void setClusterSize(int size) {
            this.clusterSize = size;
        }
    }

    /**
     * Applies the DBSCAN algorithm to compute clusters.
     *
     * @throws SQLException If a database access error occurs.
     */
    public void computeClusters() throws SQLException {
        STRtree            tree   = new STRtree();
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

    /**
     * Loads the geometries and additional columns from the database.
     *
     * @param tree The spatial index tree.
     * @return The list of ClusterGeometry objects.
     * @throws SQLException If a database access error occurs.
     */
    private  ArrayList<ClusterGeometry> loadPoints(STRtree    tree) throws SQLException {
        ArrayList<ClusterGeometry> list = new ArrayList<>();
        String sql = "SELECT " + idColumn + ", " + geomColumn + " FROM " + tableLocation;
        try (Statement stmt = connection.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
            while (res.next()) {
                Object   id   = res.getObject(1);
                Geometry geom = (Geometry) res.getObject(2);
                if (geom == null) continue;
                ClusterGeometry pt = new ClusterGeometry(id, geom);
                tree.insert(geom.getEnvelopeInternal(), pt);
                list.add(pt);
            }
        }
        return list;
    }

    /**
     * Queries the spatial index for geometries within the specified distance.
     *
     * @param tree The spatial index tree.
     * @param center The center geometry.
     * @param eps The maximum distance.
     * @return The list of neighboring geometries.
     */
    @SuppressWarnings("unchecked")
    private static List<ClusterGeometry> regionQuery(
            STRtree      tree,
            ClusterGeometry center,
            double       eps) {
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

    /**
     * Expands the cluster by adding neighboring geometries.
     *
     * @param tree The spatial index tree.
     * @param seeds The seed geometries.
     * @param clusterId The ID of the cluster.
     * @param eps The maximum distance.
     * @param minPoints The minimum number of points required to form a cluster.
     */
    private static void expandCluster(
            STRtree            tree,
            List<ClusterGeometry> seeds,
            int                clusterId,
            double             eps,
            int                minPoints) {
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
        // Update cluster size
        for (ClusterGeometry pt : seeds) {
            if (pt.label == clusterId) {
                pt.setClusterSize(size+1);
            }
        }
    }
}