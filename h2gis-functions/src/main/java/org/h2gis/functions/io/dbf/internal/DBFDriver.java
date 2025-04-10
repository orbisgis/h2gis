/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.dbf.internal;

import org.h2.value.Value;
import org.h2gis.api.FileDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Manage DBFReader and DBFWriter
 * @author Nicolas Fortin
 */
public class DBFDriver implements FileDriver {
    private File dbfFile;
    protected DbaseFileReader dbaseFileReader;
    protected DbaseFileWriter dbaseFileWriter;

    /**
     * Init file header for DBF File
     * @param dbfFile DBF File path
     */
    public void initDriverFromFile(File dbfFile) throws IOException {
        initDriverFromFile(dbfFile, null);
    }

    /**
     * Init file header for DBF File
     * @param dbfFile DBF File path
     * @param forceEncoding File encoding to use, null will use the file encoding provided in the file header
     */
    public void initDriverFromFile(File dbfFile, String forceEncoding) throws IOException {
        // Read columns from files metadata
        this.dbfFile = dbfFile;
        FileInputStream fis = new FileInputStream(dbfFile);
        dbaseFileReader = new DbaseFileReader(fis.getChannel(), forceEncoding);
    }

    public void initDriver(File dbfFile, DbaseFileHeader dbaseHeader) throws IOException {
        this.dbfFile = dbfFile;
        FileOutputStream dbfFos = new FileOutputStream(dbfFile);
        dbaseFileWriter = new DbaseFileWriter(dbaseHeader,dbfFos.getChannel());
    }


    /**
     * Write a row
     * @param values Content, must be of the same type as declared in the header
     */
    @Override
    public void insertRow(Object[] values) throws IOException {
        checkWriter();
        try {
            dbaseFileWriter.write(values);
        } catch (DbaseFileException ex) {
            throw new IOException(ex.getLocalizedMessage(), ex);
        }
    }

    private void checkReader() {
        if(dbaseFileReader == null) {
            throw new IllegalStateException("The driver is not in read mode");
        }
    }

    private void checkWriter() {
        if(dbaseFileWriter == null) {
            throw new IllegalStateException("The driver is not in write mode");
        }
    }

    /**
     * @return DBF File path
     */
    public File getDbfFile() {
        return dbfFile;
    }

    /**
     * @return The DBF file header
     */
    public DbaseFileHeader getDbaseFileHeader() {
        if(dbaseFileReader != null) {
            return dbaseFileReader.getHeader();
        } else if(dbaseFileWriter != null) {
            return dbaseFileWriter.getHeader();
        } else {
            throw new IllegalStateException("The driver is not initialised");
        }
    }

    @Override
    public void close() throws IOException {
        if(dbaseFileReader != null) {
            dbaseFileReader.close();
        } else if(dbaseFileWriter != null) {
            dbaseFileWriter.close();
        }
    }

    @Override
    public long getRowCount() {
        return dbaseFileReader.getRecordCount();
    }

    /**
     * @return Column count
     */
    @Override
    public int getFieldCount() {
        return getDbaseFileHeader().getNumFields();
    }

    @Override
    public int getEstimatedRowSize(long rowId) {
        int totalSize = 0;
        int fieldCount = getFieldCount();
        for(int column = 0; column < fieldCount; column++) {
            totalSize += dbaseFileReader.getLengthFor(column);
        }
        return totalSize;
    }

    @Override
    public Value getField(long rowId, int columnId) throws IOException {
        return dbaseFileReader.getFieldValue((int)rowId, columnId);
    }

    /**
     * Get the file reader
     * @return {@link DbaseFileReader}
     */
    public DbaseFileReader getDbaseFileReader() {
        return dbaseFileReader;
    }

}

