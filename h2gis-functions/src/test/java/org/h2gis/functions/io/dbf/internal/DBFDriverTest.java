package org.h2gis.functions.io.dbf.internal;

import org.h2gis.functions.io.shp.SHPEngineTest;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class DBFDriverTest {


    @Test
    public void test64bitsDbf() throws IOException {
        DBFDriver dbfDriver = new DBFDriver();
        dbfDriver.initDriverFromFile(new File(SHPEngineTest.class.getResource("waternetwork.dbf").getFile()));
        assertTrue(dbfDriver.dbaseFileReader.getPositionFor(11000000, 0) > Integer.MAX_VALUE);
    }
}
