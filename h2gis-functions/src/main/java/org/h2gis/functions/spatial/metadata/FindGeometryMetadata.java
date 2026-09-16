/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.metadata;

import org.h2.util.StringUtils;
import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.utilities.GeometryMetaData;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FindGeometryMetadata extends DeterministicScalarFunction{

    public FindGeometryMetadata() {
        addProperty(PROP_REMARKS, "Extract geometry metadata from its create table signature."
                + "eg : GEOMETRY; GEOMETRY(POINT); GEOMETRY(POINT Z); GEOMETRY(POINTZ, 4326)...");
    }

    @Override
    public String getJavaStaticMethod() {
        return "extractMetadata";
    }

    /**
     * Extract the geometry metadata from its OGC signature
     *
     * Examples:
     *
     * POINT
     * POINT Z
     * POINT ZM
     *
     *
     * @return an array of values with the following values order
     * values[0] = GEOMETRY_TYPE
     * values[1] = COORD_DIMENSION
     * values[2] = SRID
     * values[3] = SFS TYPE
     * @throws SQLException Exception get on executing wrong SQL query.
     */
    public static String[] extractMetadata(Connection connection,
                                           String catalogName, String schemaName, String tableName,
                                           String columnName, String data_type, String geometry_type, String srid)
            throws SQLException {
        if(geometry_type==null){
            geometry_type=data_type;
        }
        String[] values = new String[4];
        if(srid==null) {
            try ( ResultSet rs = connection.createStatement()
                    .executeQuery(String.format("select ST_SRID(%s) from %s LIMIT 1;",
                            StringUtils.quoteJavaString(columnName),
                            new TableLocation(catalogName, schemaName, tableName, DBUtils.getDBType(connection))))) {
                if (rs.next()) {
                    srid = rs.getString(1);
                }
            }
        }
        GeometryMetaData geomMeta = GeometryMetaData.createMetadataFromGeometryType(geometry_type);
        values[0] = String.valueOf(geomMeta.getGeometryTypeCode());
        values[1] = String.valueOf(geomMeta.getDimension());
        values[2] = srid;
        values[3] = geomMeta.getSfs_geometryType();

        return values;
    }

}
