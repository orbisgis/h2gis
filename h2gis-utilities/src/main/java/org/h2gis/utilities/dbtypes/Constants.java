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

package org.h2gis.utilities.dbtypes;

import java.util.*;
import java.util.regex.Pattern;

import static org.h2gis.utilities.dbtypes.DBTypes.*;

/**
 * SQL reserved words.
 *
 * @author Nicolas Fortin
 * @author Erwan Bocher (CNRS 2021)
 * @author Sylvain Palominos (UBS Chaire GEOTERA 2021)
 */
public abstract class Constants {
    /** H2 JDBC protocol. */
    public static final String H2_JDBC_PROTOCOL = "h2";
    /** PostgreSQL JDBC protocol. */
    public static final String POSTGRESQL_JDBC_PROTOCOL = "postgresql";
    /** PostGIS JDBC protocol. */
    public static final String POSTGIS_JDBC_PROTOCOL = "postgresql_postGIS";

    /** H2 JDBC driver name. */
    public static final String H2_JDBC_NAME = "H2 JDBC Driver";
    /** POSTGRESQL JDBC driver name. */
    public static final String POSTGRESQL_JDBC_NAME = "PostgreSQL JDBC Driver";

    /**
     * Map used to convert URI scheme to DBType
     */
    public static final Map<String, DBTypes> SCHEME_DBTYPE_MAP = new HashMap<>();

    static {
        SCHEME_DBTYPE_MAP.put(H2_JDBC_PROTOCOL, H2);
        SCHEME_DBTYPE_MAP.put(POSTGRESQL_JDBC_PROTOCOL, POSTGRESQL);
        SCHEME_DBTYPE_MAP.put(POSTGIS_JDBC_PROTOCOL, POSTGIS);
    }

    /**
     * Map used to convert URI scheme to DBType
     */
    public static final Map<String, DBTypes> DB_NAME_TYPE_MAP = new HashMap<>();

    static {
        DB_NAME_TYPE_MAP.put(H2_JDBC_NAME, H2);
        DB_NAME_TYPE_MAP.put(POSTGRESQL_JDBC_NAME, POSTGRESQL);
    }

    /**
     * Map used to convert URI scheme to DBType
     */
    public static final Map<String, DBTypes> driverDBTypeMap = new HashMap<>();

    static {
        driverDBTypeMap.put("org.h2.jdbc.JdbcConnection", H2);
        driverDBTypeMap.put("org.h2gis.utilities.wrapper.ConnectionWrapper", H2GIS);
        driverDBTypeMap.put("org.h2gis.postgis_jts.ConnectionWrapper", POSTGIS);
        driverDBTypeMap.put("org.postgresql.PGConnection", POSTGRESQL);
    }

    public static final Pattern H2_SPECIAL_NAME_PATTERN = Pattern.compile("^[A-Z]{1,1}[A-Z0-9_]*$");
    public static final Pattern POSTGRESQL_SPECIAL_NAME_PATTERN = Pattern.compile("^[a-z]{1,1}[a-z0-9_]*$");

    /**
     * H2 reserved keywords.
     *
     * @see <a href="http://www.h2database.com/html/advanced.html#compatibility">Keywords source</a>
     */
    public static final Set<String> H2_RESERVED_WORDS = new HashSet<>(Arrays.asList("ALL", "AND", "ARRAY", "AS",
            "BETWEEN", "BOTH", "CASE", "CHECK", "CONSTRAINT", "CROSS", "CURRENT_CATALOG", "CURRENT_DATE",
            "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DISTINCT", "EXCEPT", "EXISTS",
            "FALSE", "FETCH", "FILTER", "FOR", "FOREIGN", "FROM", "FULL", "GROUP", "GROUPS", "HAVING", "IF", "ILIKE",
            "IN", "INNER", "INTERSECT", "INTERSECTS", "INTERVAL", "IS", "JOIN", "LEADING", "LEFT", "LIKE", "LIMIT",
            "LOCALTIME", "LOCALTIMESTAMP", "MINUS", "NATURAL", "NOT", "NULL", "OFFSET", "ON", "OR", "ORDER", "OVER",
            "PARTITION", "PRIMARY", "QUALIFY", "RANGE", "REGEXP", "RIGHT", "ROW", "_ROWID_", "ROWNUM", "ROWS",
            "SELECT", "SYSDATE", "SYSTIME", "SYSTIMESTAMP", "TABLE", "TODAY", "TOP", "TRAILING", "TRUE", "UNION",
            "UNIQUE", "UNKNOWN", "USING", "VALUES", "WHERE", "WINDOW", "WITH"));
    /**
     * @see <a href="http://www.postgresql.org/docs/9.3/static/sql-keywords-appendix.html#KEYWORDS-TABLE">Keywords source</a>
     */
    public static final Set<String> POSTGRESQL_RESERVED_WORDS = new HashSet<>(Arrays.asList("ALL", "ANALYSE",
            "ANALYZE", "AND", "ANY", "AS", "ASC", "AUTHORIZATION", "BETWEEN", "BINARY", "BOTH", "CASE", "CAST",
            "CHECK", "COLLATE", "COLUMN", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME",
            "CURRENT_TIMESTAMP", "CURRENT_USER", "DEFAULT", "DEFERRABLE", "DESC", "DISTINCT", "DO", "ELSE", "END",
            "EXCEPT", "FALSE", "FOR", "FOREIGN", "FREEZE", "FROM", "FULL", "GRANT", "GROUP", "HAVING", "ILIKE",
            "IN", "INITIALLY", "INNER", "INTERSECT", "INTO", "IS", "ISNULL", "JOIN", "LEADING", "LEFT", "LIKE",
            "LIMIT", "LOCALTIME", "LOCALTIMESTAMP", "NATURAL", "NEW", "NOT", "NOTNULL", "NULL", "OFF", "OFFSET",
            "OLD", "ON", "ONLY", "OR", "ORDER", "OUTER", "OVERLAPS", "PLACING", "PRIMARY", "REFERENCES", "RIGHT",
            "SELECT", "SESSION_USER", "SIMILAR", "SOME", "TABLE", "THEN", "TO", "TRAILING", "TRUE", "UNION", "UNIQUE",
            "USER", "USING", "VERBOSE", "WHEN", "WHERE"));
}
