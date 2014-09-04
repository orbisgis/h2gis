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
