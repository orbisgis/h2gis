package org.h2gis.functions.io.fgb;

import org.h2.command.ddl.CreateTableData;
import org.h2gis.functions.io.fgb.fileTable.FGBDriver;
import org.h2gis.functions.io.file_table.FileEngine;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FGBEngine extends FileEngine<FGBDriver> {


    @Override
    protected FGBDriver createDriver(File filePath, List<String> args) throws IOException {
        return null;
    }

    @Override
    protected void feedCreateTableData(FGBDriver driver, CreateTableData data) throws IOException {

    }
}
