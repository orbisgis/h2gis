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
public class DBFWrite  extends AbstractFunction implements ScalarFunction {

    public DBFWrite() {
        addProperty(PROP_REMARKS, "Transfer the content of a table into a DBF");
    }

    @Override
    public String getJavaStaticMethod() {
        return "exportTable";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void exportTable(Connection connection, String fileName, String tableReference) throws IOException, SQLException {
        DBFDriverFunction driverFunction = new DBFDriverFunction();
        driverFunction.exportTable(connection, tableReference, new File(fileName));
    }
}
