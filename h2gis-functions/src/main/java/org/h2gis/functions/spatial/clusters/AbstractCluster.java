/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.clusters;

import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;
import org.h2gis.utilities.dbtypes.DBUtils;

import java.sql.*;

/**
 * Erwan Bocher (CNRS)
 * Abstract base class for clustering algorithms.
 */
public abstract class AbstractCluster implements SimpleRowSource {

    public static final int UNVISITED = -1;
    public static final int NOISE = -2;

    protected final String idColumn;
    protected final String geomColumn;
    protected final String tableName;
    protected final Connection connection;
    protected final TableLocation tableLocation;
    protected boolean firstRow = true;
    protected Statement streamStmt;
    protected ResultSet streamRS;

    public AbstractCluster(Connection connection, String tableName,
                           String geomColumn, String idColumn) throws SQLException {
        this.connection = connection;
        this.tableName = tableName;
        this.tableLocation = TableLocation.parse(tableName, DBUtils.getDBType(connection));
        this.geomColumn = geomColumn;
        this.idColumn = idColumn;
    }

    @Override
    public void close() {
        closeStream();
    }

    public ResultSet getResultSet() throws SQLException {
        SimpleResultSet rs = new SimpleResultSet(this);
        getMetadata(rs);
        rs.addColumn("CLUSTER_ID", Types.INTEGER, 10, 0);
        rs.addColumn("CLUSTER_SIZE", Types.INTEGER, 10, 0);
        return rs;
    }

    protected void getMetadata(SimpleResultSet rs) throws SQLException {
        String sql = "SELECT " + idColumn + ", " + geomColumn + " FROM " + tableLocation + " LIMIT 0";
        try (Statement stmt = connection.createStatement();
             ResultSet res = stmt.executeQuery(sql)) {
            TableUtilities.copyFields(rs, res.getMetaData());
        }
    }

    protected abstract void computeClusters() throws SQLException;

    protected void closeStream() {
        try {
            if (streamRS != null) streamRS.close();
        } catch (SQLException ignored) {
        }
        try {
            if (streamStmt != null) streamStmt.close();
        } catch (SQLException ignored) {
        }
        streamRS = null;
        streamStmt = null;
    }
}