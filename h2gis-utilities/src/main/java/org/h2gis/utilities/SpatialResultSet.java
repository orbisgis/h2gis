/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.utilities;

import org.locationtech.jts.geom.Geometry;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * In order to provide a common API with H2 Spatial and PostGIS,
 * this result set manage the conversion of column to JTS geometry.
 * Usage:
 * SpatialResultSet rs = myStatement.executeQuery().unwrap(SpatialResultSet.class);
 *
 * @author Nicolas Fortin
 */
public interface SpatialResultSet extends ResultSet {
    /**
     * Retrieves Geometry value of the specified column.
     *
     * @param columnIndex Column index [1-n]
     *
     * @return Geometry value or null
     *
     * @throws SQLException If the specified column is not a Geometry.
     */
    Geometry getGeometry(int columnIndex) throws SQLException;

    /**
     * Retrieves Geometry value of the specified column.
     *
     * @param columnLabel Column label
     *
     * @return Geometry value or null
     *
     * @throws SQLException If the specified column is not a Geometry.
     */
    Geometry getGeometry(String columnLabel) throws SQLException;

    /**
     * Retrieves Geometry value of the first geometry column.
     *
     * @return Geometry value or null
     *
     * @throws SQLException If there is no Geometry columns.
     */
    Geometry getGeometry() throws SQLException;

    /**
     * Update the geometry value
     *
     * @param columnIndex Field index
     * @param geometry Geometry instance
     *
     * @throws SQLException
     */
    void updateGeometry(int columnIndex, Geometry geometry) throws SQLException;

    /**
     * Update the geometry value
     *
     * @param columnLabel Field name
     * @param geometry Geometry instance
     *
     * @throws SQLException
     */
    void updateGeometry(String columnLabel, Geometry geometry) throws SQLException;

}
