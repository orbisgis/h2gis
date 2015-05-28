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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * SQL reserved words
 * @author Nicolas Fortin
 */
public class Constants {
    /***
     * @see <a href="http://www.h2database.com/html/advanced.html#compatibility">Keywords source</a>
     */
    public static final Set<String> H2_RESERVED_WORDS = new HashSet<String>(Arrays.asList("CROSS", "CURRENT_DATE",
            "CURRENT_TIME", "CURRENT_TIMESTAMP", "DISTINCT", "EXCEPT", "EXISTS", "FALSE", "FETCH", "FOR", "FROM", "FULL",
            "GROUP", "HAVING", "INNER", "INTERSECT", "IS", "JOIN", "LIKE", "LIMIT", "MINUS", "NATURAL", "NOT",
            "NULL", "OFFSET", "ON", "ORDER", "PRIMARY", "ROWNUM", "SELECT", "SYSDATE", "SYSTIME", "SYSTIMESTAMP", "TODAY",
            "TRUE", "UNION", "UNIQUE", "WHERE"));
    /***
     * @see <a href="http://www.postgresql.org/docs/9.3/static/sql-keywords-appendix.html#KEYWORDS-TABLE">Keywords source</a>
     */
    public static final Set<String> POSTGIS_RESERVED_WORDS = new HashSet<String>(Arrays.asList("ALL", "ANALYSE",
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
