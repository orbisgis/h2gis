/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * In order to provide a common API with H2 Spatial and PostGIS this MetaData give type information on Geometry fields.
 * @author Nicolas Fortin
 */
public interface SpatialResultSetMetaData extends ResultSetMetaData {

    /**
     * @param column
     * @return {@link GeometryTypeCodes} of the provided column.
     * @throws SQLException
     */
    int getGeometryType(int column) throws SQLException;

    /**
     * @return {@link GeometryTypeCodes} of the first geometry column.
     * @throws SQLException if this meta data does not contains a geometry field.
     */
    int getGeometryType() throws SQLException;

    /**
     * @return Column index of the first geometry in this result set.
     * @throws SQLException
     */
    public int getFirstGeometryFieldIndex() throws SQLException;
}
