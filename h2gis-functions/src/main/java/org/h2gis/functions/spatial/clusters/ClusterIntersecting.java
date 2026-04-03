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
 * Implements ST_ClusterIntersecting, similar to PostGIS's ST_ClusterIntersecting.
 * Groups geometries that intersect each other into clusters.
 */
public class ClusterIntersecting implements SimpleRowSource {

    private static final int UNVISITED = -1;
    private final String idColumn;
    public boolean firstRow = true;
    public String tableName;
    public String geomColumn;
    public Connection connection;
    private final TableLocation tableLocation;
    private ArrayList<ClusterGeometry> clusterGeometries;
    private Iterator<ClusterGeometry> clusterIterators;

    /**
     * Constructs a new ClusterIntersecting instance.
     *
     * @param connection   Database connection.
     * @param tableName    Name of the table containing geometries.
     * @param geomColumn   Name of the geometry column.
     * @param idColumn     Name of the ID column.
     */
    public ClusterIntersecting(Connection connection, String tableName, String geomColumn,
                               String idColumn) throws SQLException {
        this.tableName = tableName;
        this.tableLocation = TableLocation.parse(tableName, DBUtils.getDBType(connection));
        this.geomColumn = geomColumn;
        this.idColumn = idColumn;
        this.connection = connection;
    }

    @Override
    public Object[] readRow() throws SQLException {
        if (firstRow) {
            reset();
        }
        if (clusterIterators.hasNext()) {
            ClusterGeometry pt = clusterIterators.next();
            Integer clusterId = (pt.label == UNVISITED) ? null : pt.label;
            Integer clusterSize = (pt.label == UNVISITED) ? null : pt.clusterSize;
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
     * @return ResultSet with clustered geometries.
     */
    public ResultSet getResultSet() throws SQLException {
        SimpleResultSet rs = new SimpleResultSet(this);
        getMetadata(rs);
        rs.addColumn("CLUSTER_ID", Types.INTEGER, 10, 0);
        rs.addColumn("CLUSTER_SIZE", Types.INTEGER, 10, 0);
        return rs;
    }

    private void getMetadata(SimpleResultSet rs) throws SQLException {
        String sql = "SELECT " + idColumn + ", " + geomColumn + " FROM " + tableLocation + " LIMIT 0";
        try (Statement stmt = connection.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
            TableUtilities.copyFields(rs, res.getMetaData());
        }
    }

    /**
     * Class to manage geometry and identifier.
     */
    private static class ClusterGeometry {
        final Object originalId;
        final Geometry geom;
        int label = UNVISITED;
        int clusterSize = 0;

        ClusterGeometry(Object originalId, Geometry geom) {
            this.originalId = originalId;
            this.geom = geom;
        }

        public void setClusterSize(int size) {
            this.clusterSize = size;
        }
    }

    /**
     * Computes clusters of intersecting geometries.
     *
     */
    public void computeClusters() throws SQLException {
        STRtree tree = new STRtree();
        clusterGeometries = loadPoints(tree);

        int nextCluster = 1;

        for (ClusterGeometry pt : clusterGeometries) {
            if (pt.label != UNVISITED) continue;

            // Start a new cluster
            List<ClusterGeometry> clusterMembers = findIntersectingGeometries(tree, pt);
            int clusterSize = clusterMembers.size();

            // Assign cluster ID and size
            for (ClusterGeometry member : clusterMembers) {
                member.label = nextCluster;
                member.setClusterSize(clusterSize);
            }

            nextCluster++;
        }
    }

    /**
     * Loads geometries from the database.
     *
     * @param tree Spatial index tree.
     * @return List of ClusterGeometry objects.
     */
    private ArrayList<ClusterGeometry> loadPoints(STRtree tree) throws SQLException {
        ArrayList<ClusterGeometry> list = new ArrayList<>();
        String sql = "SELECT " + idColumn + ", " + geomColumn + " FROM " + tableLocation;

        try (Statement stmt = connection.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {

            while (res.next()) {
                Object id = res.getObject(1);
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
     * Finds all geometries that intersect with the given geometry.
     *
     * @param tree  Spatial index tree.
     * @param center Center geometry.
     * @return List of intersecting geometries.
     */
    @SuppressWarnings("unchecked")
    private List<ClusterGeometry> findIntersectingGeometries(STRtree tree, ClusterGeometry center) {
        Envelope searchEnv = center.geom.getEnvelopeInternal();
        List<ClusterGeometry> candidates = tree.query(searchEnv);

        List<ClusterGeometry> result = new ArrayList<>();
        for (ClusterGeometry candidate : candidates) {
            if (candidate.label == UNVISITED && center.geom.intersects(candidate.geom)) {
                result.add(candidate);
            }
        }
        return result;
    }
}