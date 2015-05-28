/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.drivers.dbf.internal;

import org.h2gis.drivers.FileDriver;

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
    private DbaseFileReader dbaseFileReader;
    private DbaseFileWriter dbaseFileWriter;

    /**
     * Init file header for DBF File
     * @param dbfFile DBF File path
     * @throws IOException
     */
    public void initDriverFromFile(File dbfFile) throws IOException {
        initDriverFromFile(dbfFile, null);
    }

    /**
     * Init file header for DBF File
     * @param dbfFile DBF File path
     * @param forceEncoding File encoding to use, null will use the file encoding provided in the file header
     * @throws IOException
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
        if(values.length != getDbaseFileHeader().getNumFields()) {
            throw new IllegalArgumentException("Incorrect field count "+values.length+" expected "+getFieldCount());
        }
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
    public int getFieldCount() {
        return getDbaseFileHeader().getNumFields();
    }

    @Override
    public Object[] getRow(long rowId) throws IOException {
        final int fieldCount = dbaseFileReader.getFieldCount();
        Object[] values = new Object[fieldCount];
        for(int fieldId=0;fieldId<fieldCount;fieldId++) {
            values[fieldId] = dbaseFileReader.getFieldValue((int)rowId, fieldId);
        }
        return values;
    }
}
