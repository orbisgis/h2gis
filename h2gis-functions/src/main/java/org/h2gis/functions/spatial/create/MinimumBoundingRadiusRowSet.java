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

package org.h2gis.functions.spatial.create;

import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.geom.Geometry;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to compute the center and the radius of multiple geometries stored in a table.
 *
 * @author Erwan Bocher, CNRS 2023
 */
public class MinimumBoundingRadiusRowSet implements SimpleRowSource {

    private final Connection connection;
    private String tableName;
    private boolean firstRow = true;
    public ResultSet tableQuery;
    public int spatialFieldIndex;
    public int explodeId = 1;

    public MinimumBoundingRadiusRowSet(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    @Override
    public Object[] readRow() throws SQLException {
        if(firstRow) {
            reset();
        }
        if(tableQuery.next()){
            Geometry geomTable = (Geometry) tableQuery.getObject(spatialFieldIndex);
            if(geomTable!=null) {
                MinimumBoundingCircle mbc = new MinimumBoundingCircle(geomTable);
                Geometry geom = geomTable.getFactory().createPoint(mbc.getCentre());
                geom.setSRID(geomTable.getSRID());
                return new Object[]{explodeId++, geom, mbc.getRadius()};
            }
            else{
                return new Object[]{explodeId++, null, null};
            }
        }
        return null;
    }

    @Override
    public void close() {
        if(tableQuery!=null) {
            try {
                tableQuery.close();
                tableQuery = null;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void reset() throws SQLException {
        if (tableQuery != null && !tableQuery.isClosed()) {
            close();
        }
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableName);
        if (matcher.find()) {
            if (tableName.startsWith("(") && tableName.endsWith(")")) {
                Statement st = connection.createStatement();
                tableQuery = st.executeQuery(tableName);
                spatialFieldIndex = GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(tableQuery.getMetaData()).second();
            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM ORDERS)'.");
            }
        }else{
            TableLocation tableLocation = TableLocation.parse(tableName, DBUtils.getDBType(connection));
            spatialFieldIndex = GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(connection, tableLocation).second();
            Statement st = connection.createStatement();
            tableQuery = st.executeQuery("select * from "+ tableName);
        }
        firstRow = false;
    }

    public ResultSet getResultSet()  {
        SimpleResultSet srs = new SimpleResultSet(this);
        srs.addColumn("ID", Types.INTEGER,10,0);
        srs.addColumn("CENTER", Types.OTHER, "GEOMETRY", 0, 0);
        srs.addColumn("RADIUS", Types.DOUBLE, 10, 0);
        return srs;
    }
}
