/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.utilities;

import com.vividsolutions.jts.geom.Geometry;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * In order to provide a common API with H2 Spatial and PostGIS,
 * this result set manage the conversion of column to JTS geometry.
 * Usage:
 * SpatialResultSet rs = myStatement.executeQuery().unwrap(SpatialResultSet.class);
 * @author Nicolas Fortin
 */
public interface SpatialResultSet extends ResultSet {
    /**
     * Retrieves Geometry value of the specified column.
     * @param columnIndex Column index [1-n]
     * @return Geometry value or null
     * @throws SQLException If the specified column is not a Geometry.
     */
    Geometry getGeometry(int columnIndex) throws SQLException;

    /**
     * Retrieves Geometry value of the specified column.
     * @param columnLabel Column label
     * @return Geometry value or null
     * @throws SQLException If the specified column is not a Geometry.
     */
    Geometry getGeometry(String columnLabel) throws SQLException;

    /**
     * Retrieves Geometry value of the first geometry column.
     * @return Geometry value or null
     * @throws SQLException If there is no Geometry columns.
     */
    Geometry getGeometry() throws SQLException;

    /**
     * Update the geometry value
     * @param columnIndex Field index
     * @param geometry Geometry instance
     * @throws SQLException
     */
    void updateGeometry(int columnIndex, Geometry geometry) throws SQLException;

    /**
     * Update the geometry value
     * @param columnLabel Field name
     * @param geometry Geometry instance
     * @throws SQLException
     */
    void updateGeometry(String columnLabel, Geometry geometry) throws SQLException;

}
