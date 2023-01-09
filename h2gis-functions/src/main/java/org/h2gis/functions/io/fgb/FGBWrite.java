package org.h2gis.functions.io.fgb;

import org.h2gis.api.AbstractFunction;
import org.h2gis.api.EmptyProgressVisitor;
import org.h2gis.api.ScalarFunction;
import org.h2gis.utilities.URIUtilities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class FGBWrite extends AbstractFunction implements ScalarFunction {

    public FGBWrite() {
        addProperty(PROP_REMARKS, "Export a spatial table to a FlatGeobuf file.\n "
                + "\nFGBWrite(..."
                + "\n Supported arguments :"
                + "\n path of the file, table name"
                + "\n path of the file, table name, true to delete the file if exists");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Read a table and write it into a FlatGeobuf file.
     *
     * @param connection     Active connection
     * @param fileName       FlatGeobuf file name or URI
     * @param tableReference Table name or select query Note : The select query
     *                       must be enclosed in parenthesis
     * @param deleteFile     true to delete output file
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public static void execute(Connection connection, String fileName, String tableReference, boolean deleteFile) throws SQLException, IOException {
        FGBDriverFunction geobufDriverFunction = new FGBDriverFunction();
        geobufDriverFunction.exportTable(connection, tableReference, URIUtilities.fileFromString(fileName), deleteFile, new EmptyProgressVisitor());
    }

    /**
     * Read a table and write it into a FlatGeobuf file.
     *
     * @param connection     Active connection
     * @param fileName       FlatGeobuf file name or URI
     * @param tableReference Table name or select query Note : The select query
     *                       must be enclosed in parenthesis
     * @throws IOException
     * @throws SQLException
     */
    public static void exportTable(Connection connection, String fileName, String tableReference) throws IOException, SQLException {
        execute(connection, fileName, tableReference, false);
    }
}
