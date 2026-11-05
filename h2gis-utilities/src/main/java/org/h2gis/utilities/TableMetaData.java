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
package org.h2gis.utilities;

import org.h2gis.utilities.JDBCUtilities.TABLE_TYPE;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description of a table and of its geometry columns, as returned by
 * {@link GeometryTableUtilities#getTablesMetaData(java.sql.Connection)}.
 * <p>
 * All the values come from the database catalog: building it never reads
 * the data of the table. When the SRID of a geometry column is not declared
 * (a plain {@code GEOMETRY} column), {@link GeometryColumn#isSRIDDeclared()}
 * is false and the SRID can be found later from the data with
 * {@link GeometryTableUtilities#findSRIDFromData}.
 *
 * @author Erwan Bocher, CNRS (2026)
 */
public class TableMetaData {

    private final TableLocation tableLocation;
    private final TABLE_TYPE tableType;
    private String primaryKeyName;
    private int primaryKeyIndex;
    private final LinkedHashMap<String, GeometryColumn> geometryColumns = new LinkedHashMap<>();

    /**
     * @param tableLocation location of the table
     * @param tableType     type of the table
     */
    public TableMetaData(TableLocation tableLocation, TABLE_TYPE tableType) {
        this.tableLocation = tableLocation;
        this.tableType = tableType;
    }

    /**
     * @return the location (catalog, schema, name) of the table
     */
    public TableLocation getTableLocation() {
        return tableLocation;
    }

    /**
     * @return the type of the table (table, view, temporary, linked table...)
     */
    public TABLE_TYPE getTableType() {
        return tableType;
    }

    /**
     * @return the name of the single column integer primary key, null if the
     * table has none (no primary key, a primary key of several columns or of a
     * non integer type)
     */
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    /**
     * @return the 1-based index of the integer primary key column, 0 if none
     */
    public int getPrimaryKeyIndex() {
        return primaryKeyIndex;
    }

    void setPrimaryKey(String name, int index) {
        this.primaryKeyName = name;
        this.primaryKeyIndex = index;
    }

    /**
     * @return the geometry columns, by name, in the order of the table
     */
    public Map<String, GeometryColumn> getGeometryColumns() {
        return Collections.unmodifiableMap(geometryColumns);
    }

    void addGeometryColumn(GeometryColumn column) {
        geometryColumns.put(column.getName(), column);
    }

    /**
     * @return true if the table has at least one geometry column
     */
    public boolean hasGeometry() {
        return !geometryColumns.isEmpty();
    }

    @Override
    public String toString() {
        return tableLocation + " " + tableType + " pk=" + primaryKeyName + " " + geometryColumns.values();
    }

    /**
     * Description of a geometry column.
     */
    public static class GeometryColumn {

        private final String name;
        private final int index;
        private final GeometryMetaData metaData;
        private final boolean sridDeclared;
        private final String authorityName;
        private final int authoritySRID;
        private final boolean spatialIndexed;

        /**
         * @param name           column name
         * @param index          1-based index of the column in the table
         * @param metaData       geometry type, dimension and SRID (0 when not declared)
         * @param sridDeclared   true if the SRID is part of the column type
         * @param authorityName  authority of the SRID in SPATIAL_REF_SYS, null if unknown
         * @param authoritySRID  code of the SRID for this authority, 0 if unknown
         * @param spatialIndexed true if the column has a spatial index
         */
        public GeometryColumn(String name, int index, GeometryMetaData metaData, boolean sridDeclared,
                              String authorityName, int authoritySRID, boolean spatialIndexed) {
            this.name = name;
            this.index = index;
            this.metaData = metaData;
            this.sridDeclared = sridDeclared;
            this.authorityName = authorityName;
            this.authoritySRID = authoritySRID;
            this.spatialIndexed = spatialIndexed;
        }

        /**
         * @return the column name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the 1-based index of the column in the table
         */
        public int getIndex() {
            return index;
        }

        /**
         * @return the geometry type, dimension and SRID of the column. The
         * type is GEOMETRY when the column does not declare one.
         */
        public GeometryMetaData getMetaData() {
            return metaData;
        }

        /**
         * @return the declared SRID, 0 when the column does not declare one
         */
        public int getSRID() {
            return metaData.getSRID();
        }

        /**
         * @return true if the SRID is part of the column type, e.g. GEOMETRY(POINT, 4326)
         */
        public boolean isSRIDDeclared() {
            return sridDeclared;
        }

        /**
         * @return the authority of the SRID (e.g. EPSG) found in SPATIAL_REF_SYS,
         * null if the SRID is not declared or unknown
         */
        public String getAuthorityName() {
            return authorityName;
        }

        /**
         * @return the code of the SRID for its authority, 0 if unknown
         */
        public int getAuthoritySRID() {
            return authoritySRID;
        }

        /**
         * @return true if the column has a spatial index
         */
        public boolean isSpatialIndexed() {
            return spatialIndexed;
        }

        @Override
        public String toString() {
            return name + "(" + metaData.getGeometryType() + ", " + metaData.getSRID()
                    + (authorityName == null ? "" : " " + authorityName + ":" + authoritySRID)
                    + (spatialIndexed ? ", spatial index" : "") + ")";
        }
    }
}