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
package org.h2gis.graalvm;

import org.h2gis.utilities.GeometryMetaData;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableMetaData;

import java.util.List;

/**
 * JSON form of {@link TableMetaData}, returned by {@code h2gis_get_tables_json}.
 * <p>
 * Kept apart from {@link GraalCInterface} so that it can be tested on a JVM.
 * The format is described in C_API.md:
 * <pre>
 * [{"catalog":"DB","schema":"PUBLIC","table":"ROADS","tableType":"TABLE",
 *   "fid":"ID","fidIndex":1,
 *   "geometryColumns":[{"name":"THE_GEOM","index":3,"geometryType":"LINESTRING",
 *     "geometryTypeCode":2,"dimension":2,"hasZ":false,"hasM":false,
 *     "srid":2154,"sridDeclared":true,"authName":"EPSG","authSrid":2154,
 *     "spatialIndex":true}]}]
 * </pre>
 * Names are returned as stored in the catalog, without quotes.
 *
 * @author Erwan Bocher, CNRS (2026)
 */
public final class TablesJson {

    private TablesJson() {
    }

    /**
     * @param tables tables to serialize
     * @return the JSON array of the tables
     */
    public static String toJson(List<TableMetaData> tables) {
        StringBuilder sb = new StringBuilder(256 * Math.max(1, tables.size()));
        sb.append('[');
        boolean firstTable = true;
        for (TableMetaData table : tables) {
            if (!firstTable) {
                sb.append(',');
            }
            firstTable = false;
            TableLocation location = table.getTableLocation();
            sb.append("{\"catalog\":");
            appendString(sb, location.getCatalog().isEmpty() ? null : location.getCatalog());
            sb.append(",\"schema\":");
            appendString(sb, location.getSchema().isEmpty() ? null : location.getSchema());
            sb.append(",\"table\":");
            appendString(sb, location.getTable());
            sb.append(",\"tableType\":");
            appendString(sb, tableType(table.getTableType()));
            sb.append(",\"fid\":");
            appendString(sb, table.getPrimaryKeyName());
            sb.append(",\"fidIndex\":").append(table.getPrimaryKeyIndex());
            sb.append(",\"geometryColumns\":[");
            boolean firstColumn = true;
            for (TableMetaData.GeometryColumn column : table.getGeometryColumns().values()) {
                if (!firstColumn) {
                    sb.append(',');
                }
                firstColumn = false;
                GeometryMetaData meta = column.getMetaData();
                sb.append("{\"name\":");
                appendString(sb, column.getName());
                sb.append(",\"index\":").append(column.getIndex());
                sb.append(",\"geometryType\":");
                appendString(sb, meta.getGeometryType());
                sb.append(",\"geometryTypeCode\":").append(meta.getGeometryTypeCode());
                sb.append(",\"dimension\":").append(meta.getDimension());
                sb.append(",\"hasZ\":").append(meta.hasZ());
                sb.append(",\"hasM\":").append(meta.hasM());
                sb.append(",\"srid\":").append(column.getSRID());
                sb.append(",\"sridDeclared\":").append(column.isSRIDDeclared());
                sb.append(",\"authName\":");
                appendString(sb, column.getAuthorityName());
                sb.append(",\"authSrid\":").append(column.getAuthoritySRID());
                sb.append(",\"spatialIndex\":").append(column.isSpatialIndexed());
                sb.append('}');
            }
            sb.append("]}");
        }
        return sb.append(']').toString();
    }

    /**
     * Name of a table type. TABLE_TYPE.UNKOWN (a typo kept for the
     * compatibility of the Java API) is written UNKNOWN.
     */
    static String tableType(JDBCUtilities.TABLE_TYPE type) {
        return type == JDBCUtilities.TABLE_TYPE.UNKOWN ? "UNKNOWN" : type.name();
    }

    /**
     * Append a JSON string, or {@code null} when {@code s} is null.
     */
    static void appendString(StringBuilder sb, String s) {
        if (s == null) {
            sb.append("null");
            return;
        }
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }
}