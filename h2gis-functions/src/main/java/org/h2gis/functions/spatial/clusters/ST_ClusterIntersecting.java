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

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *  @author Erwan Bocher (CNRS)
 *  Table function to find cluster on geometries
 */
public class ST_ClusterIntersecting extends AbstractFunction implements ScalarFunction {

    public ST_ClusterIntersecting() {
        addProperty(PROP_REMARKS, "A table function that returns a cluster number for each input geometry " +
                "based on intersection. Example: ST_ClusterIntersecting('sample_points', 'the_geom', 'id')");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Executes the ST_ClusterIntersecting function.
     *
     * @param connection Database connection.
     * @param tableName   Name of the table containing geometries.
     * @param geomColumn  Name of the geometry column.
     * @param idColumn    Name of the ID column.
     * @return ResultSet with clustered geometries.
     */
    public static ResultSet execute(Connection connection, String tableName, String geomColumn,
                                    String idColumn) throws SQLException {
        ClusterIntersecting clusterIntersecting = new ClusterIntersecting(
                connection, tableName, geomColumn, idColumn);
        return clusterIntersecting.getResultSet();
    }
}