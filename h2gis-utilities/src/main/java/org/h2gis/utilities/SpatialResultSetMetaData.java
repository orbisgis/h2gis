/*
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

package org.h2gis.utilities;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * In order to provide a common API with H2 Spatial and PostGIS this MetaData give type information on Geometry fields.
 *
 * @author Nicolas Fortin
 */
public interface SpatialResultSetMetaData extends ResultSetMetaData {

    /**
     * @param column column index
     *
     * @return {@link GeometryTypeCodes} of the provided column.
     *
     */
    int getGeometryType(int column) throws SQLException;

    /**
     * @return {@link GeometryTypeCodes} of the first geometry column.
     *
     * @throws SQLException if this meta data does not contains a geometry field.
     */
    int getGeometryType() throws SQLException;

    /**
     * @return Column index of the first geometry in this result set.
     *
     */
    int getFirstGeometryFieldIndex() throws SQLException;
}
