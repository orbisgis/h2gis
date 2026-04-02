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
 * Implements ST_ClusterWithin behaviour: groups geometries where each geometry
 * is within the specified distance of at least one other geometry in the same cluster.
 * Equivalent to ST_ClusterDBSCAN with minPoints = 1 (no noise, every geometry
 * belongs to a cluster).
 */
public class ClusterWithin implements SimpleRowSource {

    private static final int UNVISITED  = -1;

    private final String idColumn;
    private final double eps;

    public boolean firstRow = true;
    public String tableName;
    public String geomColumn;
    public Connection connection;
    private final TableLocation tableLocation;
    private ArrayList<ClusterGeometry> clusterGeometries;
    private Iterator<ClusterGeometry> clusterIterators;

    /**
     * Constructs a new ClusterWithin instance.
     *
     * @param connection The database connection.
     * @param tableName  The name of the table containing the geometries.
     * @param geomColumn The name of the geometry column.
     * @param idColumn   The name of the ID column.
     * @param eps        The maximum distance between two geometries to be
     *                   considered in the same cluster (must be greater than 0).
     */
    public ClusterWithin(Connection connection, String tableName,
                         String geomColumn, String idColumn,
                         double eps) throws SQLException {
        if (eps <= 0) {
            throw new SQLException("eps must be greater than 0");
        }
        this.tableName     = tableName;
        this.tableLocation = TableLocation.parse(tableName, DBUtils.getDBType(connection));
        this.geomColumn    = geomColumn;
        this.idColumn      = idColumn;
        this.eps           = eps;
        this.connection    = connection;
    }

    @Override
    public Object[] readRow() throws SQLException {
        if (firstRow) {
            reset();
        }
        if (clusterIterators.hasNext()) {
            ClusterGeometry pt = clusterIterators.next();
            return new Object[]{pt.originalId, pt.geom, pt.label, pt.clusterSize};
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

    public ResultSet getResultSet() throws SQLException {
        SimpleResultSet rs = new SimpleResultSet(this);
        getMetadata(rs);
        rs.addColumn("CLUSTER_ID",   Types.INTEGER, 10, 0);
        rs.addColumn("CLUSTER_SIZE", Types.INTEGER, 10, 0);
        return rs;
    }

    private void getMetadata(SimpleResultSet rs) throws SQLException {
        String sql = "SELECT " + idColumn + ", " + geomColumn
                + " FROM " + tableLocation + " LIMIT 0";
        try (Statement stmt = connection.createStatement();
             ResultSet res  = stmt.executeQuery(sql)) {
            TableUtilities.copyFields(rs, res.getMetaData());
        }
    }

    private static class ClusterGeometry {
        final Object   originalId;
        final Geometry geom;
        int label       = UNVISITED;
        int clusterSize = 0;

        ClusterGeometry(Object originalId, Geometry geom) {
            this.originalId = originalId;
            this.geom       = geom;
        }

        public void setClusterSize(int size) {
            this.clusterSize = size;
        }
    }

    /**
     * Computes clusters using ST_ClusterWithin logic
     */
    public void computeClusters() throws SQLException {
        STRtree tree = new STRtree();
        clusterGeometries = loadPoints(tree);
        int nextCluster = 1;

        for (ClusterGeometry pt : clusterGeometries) {
            if (pt.label != UNVISITED) continue;
            List<ClusterGeometry> neighbors = regionQuery(tree, pt, eps);
            pt.label = nextCluster;
            expandCluster(tree, neighbors, nextCluster, eps);
            nextCluster++;
        }
    }

    private ArrayList<ClusterGeometry> loadPoints(STRtree tree) throws SQLException {
        ArrayList<ClusterGeometry> list = new ArrayList<>();
        String sql = "SELECT " + idColumn + ", " + geomColumn
                + " FROM " + tableLocation;
        try (Statement stmt = connection.createStatement();
             ResultSet res  = stmt.executeQuery(sql)) {
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

    @SuppressWarnings("unchecked")
    private static List<ClusterGeometry> regionQuery(
            STRtree tree, ClusterGeometry center, double eps) {
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
     * Expands the cluster
     */
    private static void expandCluster(
            STRtree tree, List<ClusterGeometry> seeds,
            int clusterId, double eps) {

        Queue<ClusterGeometry> queue = new LinkedList<>(seeds);
        Set<ClusterGeometry>   seen  = new HashSet<>(seeds);
        int size = 0;

        while (!queue.isEmpty()) {
            ClusterGeometry current = queue.poll();
            if (current.label != UNVISITED) continue;

            current.label = clusterId;
            size++;

            for (ClusterGeometry neighbor : regionQuery(tree, current, eps)) {
                if (neighbor.label == UNVISITED && seen.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        // Include the seed itself in the size count
        for (ClusterGeometry pt : seeds) {
            if (pt.label == clusterId) {
                pt.setClusterSize(size + 1);
            }
        }
    }
}