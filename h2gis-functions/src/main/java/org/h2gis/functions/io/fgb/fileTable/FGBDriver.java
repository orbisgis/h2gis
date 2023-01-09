package org.h2gis.functions.io.fgb.fileTable;

import org.h2gis.api.FileDriver;
import org.h2gis.functions.io.dbf.internal.DbaseFileReader;
import org.wololo.flatgeobuf.HeaderMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FGBDriver implements FileDriver {


    private File fgbFile;
    private HeaderMeta headerMeta;
    private int fieldCount;

    /**
     * Init file header for DBF File
     * @param fgbFile DBF File path
     * @throws IOException
     */
    public void initDriverFromFile(File fgbFile) throws IOException {
        // Read columns from files metadata
        this.fgbFile = fgbFile;
        FileInputStream fis = new FileInputStream(fgbFile);
        headerMeta = HeaderMeta.read(fis);
        fieldCount = headerMeta.columns.size();
    }

    @Override
    public long getRowCount() {
        return headerMeta.featuresCount;
    }

    @Override
    public int getEstimatedRowSize(long rowId) {
        /*int totalSize = 0;
        int fieldCount = getFieldCount();
        for(int column = 0; column < fieldCount; column++) {


            totalSize += dbaseFileReader.getLengthFor(column);
        }
        return totalSize;*/
        return 0;
    }

    @Override
    public int getFieldCount() {
        return fieldCount;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public Object getField(long rowId, int columnId) throws IOException {
        return null;
    }

    @Override
    public void insertRow(Object[] values) throws IOException {

    }
}
