package org.h2gis.h2spatial.internal.type;

import org.h2gis.h2spatial.internal.function.spatial.properties._ColumnSRID;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_HasZ;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Check column constraint for Z constraint. Has M is not supported yet by JTS Topology Suite WKTReader.
 * @author Nicolas Fortin
 */
public class DimensionFromConstraint extends DeterministicScalarFunction {
    private static final String Z_CONSTRAINT_FUNCTION_NAME = ST_HasZ.class.getSimpleName();
    private static final Pattern Z_CONSTRAINT_PATTERN = Pattern.compile("(!)?"+Z_CONSTRAINT_FUNCTION_NAME+"\\s*\\(\\s*((([\"`][^\"`]+[\"`])|(\\w+)))\\s*\\)\\s*((=|<>|!=)\\s*(true|false))?", Pattern.CASE_INSENSITIVE);

    /**
     * Check column constraint for Z constraint. Has M is not supported yet by JTS Topology Suite WKTReader.
     */
    public DimensionFromConstraint() {
        addProperty(PROP_REMARKS, "Check column constraint for Z constraint.");
        addProperty(PROP_NAME, "_DimensionFromConstraint");
    }

    @Override
    public String getJavaStaticMethod() {
        return "dimensionFromConnection";
    }

    public static int dimensionFromConstraint(String constraint, String columnName) {
        Matcher matcher = Z_CONSTRAINT_PATTERN.matcher(constraint);
        if(matcher.find()) {
            boolean zConstraint = !"false".equalsIgnoreCase(matcher.group(8));
            if("<>".equals(matcher.group(7)) || "!=".equals(matcher.group(7)) ) {
                zConstraint = !zConstraint;
            }
            if("!".equals(matcher.group(1))) {
                zConstraint = !zConstraint;
            }
            String extractedColumnName = matcher.group(2).replace("\"","").replace("`", "");
            if(extractedColumnName.equalsIgnoreCase(columnName)) {
                return zConstraint ? 3 : 2;
            }
        }
        return 2;
    }

    public static int dimensionFromConnection(Connection connection, String catalogName, String schemaName, String tableName, String columnName,String constraint) {
        try {
            Statement st = connection.createStatement();
            // Merge column constraint and table constraint
            constraint+= _ColumnSRID.fetchConstraint(connection, catalogName, schemaName, tableName);
            return dimensionFromConstraint(constraint, columnName);
        } catch (SQLException ex) {
            return 2;
        }
    }
}
