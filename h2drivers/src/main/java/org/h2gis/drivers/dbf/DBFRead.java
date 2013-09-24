package org.h2gis.drivers.dbf;

import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Nicolas Fortin
 */
public class DBFRead  extends AbstractFunction implements ScalarFunction {
    public DBFRead() {
        addProperty(PROP_REMARKS, "Read a DBase III file and copy the content into a new table in the database");
    }

    @Override
    public String getJavaStaticMethod() {
        return "read";
    }

    public static void read(Connection connection, String fileName, String tableReference) throws IOException, SQLException {
        DBFDriverFunction dbfDriverFunction = new DBFDriverFunction();
        dbfDriverFunction.importFile(connection, tableReference, new File(fileName));
    }
}
