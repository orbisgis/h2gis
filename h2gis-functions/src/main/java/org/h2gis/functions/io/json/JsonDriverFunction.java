/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.h2gis.functions.io.json;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;

/**
 *
 * @author Erwan Bocher (CNRS)
 */
public class  JsonDriverFunction implements DriverFunction{

    @Override
    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[0];
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"json"};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("json")) {
            return "JSON";
        } else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
        return false;
    }

    @Override
    public void exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        JsonWriteDriver jsonDriver = new JsonWriteDriver(connection, tableReference, fileName);
        jsonDriver.write(progress);
    }

    @Override
    public void importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
