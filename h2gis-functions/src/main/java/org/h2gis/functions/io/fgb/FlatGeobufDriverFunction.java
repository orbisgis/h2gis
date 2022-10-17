package org.h2gis.functions.io.fgb;

import org.h2gis.api.DriverFunction;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.DriverManager;
import org.h2gis.functions.io.geojson.GeoJsonWriteDriver;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class FlatGeobufDriverFunction implements DriverFunction {

    public IMPORT_DRIVER_TYPE getImportDriverType() {
        return IMPORT_DRIVER_TYPE.COPY;
    }

    @Override
    public String[] getImportFormats() {
        return new String[0];
    }

    @Override
    public String[] getExportFormats() {
        return new String[]{"fgb"};
    }

    @Override
    public String getFormatDescription(String format) {
        if (format.equalsIgnoreCase("fgb")) {
            return "FlatGeobuf";
        } else {
            return "";
        }
    }

    @Override
    public boolean isSpatialFormat(String extension) {
         return extension.equals("fgb");
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, null, false,progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, null, deleteFiles, progress);
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String options, boolean deleteFiles, ProgressVisitor progress) throws SQLException, IOException {
        progress  = DriverManager.check(connection, tableReference, fileName, progress);
        FGBWriteDriver fgbWriteDriver = new FGBWriteDriver(connection);
        try {
            fgbWriteDriver.write(progress, tableReference, fileName, options, deleteFiles);
            return new String[]{fileName.getAbsolutePath()};
        }catch (SQLException|IOException ex){
            throw new SQLException(ex);
        }
    }

    @Override
    public String[] exportTable(Connection connection, String tableReference, File fileName, String options, ProgressVisitor progress) throws SQLException, IOException {
        return exportTable(connection, tableReference, fileName, options, false, progress);
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String options, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] importFile(Connection connection, String tableReference, File fileName, String options, boolean deleteTables, ProgressVisitor progress) throws SQLException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
