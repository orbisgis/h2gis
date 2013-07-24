/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.drivers.shp.internal;

import org.h2gis.drivers.dbf.internal.DbaseFileHeader;
import org.h2gis.drivers.dbf.internal.DbaseFileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Merge ShapeFileReader and DBFReader
 * @author Nicolas Fortin
 */
public class SHPDriver {
    private File shpFile;
    private File shxFile;
    private File dbfFile;
    private DbaseFileReader dbaseFileReader;
    private ShapefileReader shapefileReader;
    private IndexFile shxFileReader;
    private int geometryFieldIndex = 0;


    public void initDriverFromFile(File shpFile) throws IOException {             // Read columns from files metadata
        String path = shpFile.getAbsolutePath();
        this.shpFile = shpFile;
        shxFile = new File(path.substring(0,path.lastIndexOf('.'))+".shx");
        dbfFile = new File(path.substring(0,path.lastIndexOf('.'))+".dbf");
        FileInputStream fis = new FileInputStream(dbfFile);
        dbaseFileReader = new DbaseFileReader(fis.getChannel());
        FileInputStream shpFis = new FileInputStream(shpFile);
        shapefileReader = new ShapefileReader(shpFis.getChannel());
        FileInputStream shxFis = new FileInputStream(shxFile);
        shxFileReader = new IndexFile(shxFis.getChannel());
    }

    /**
     * @return The DBF file header
     */
    public DbaseFileHeader getDbaseFileHeader() {
        return dbaseFileReader.getHeader();
    }

    public void close() throws IOException {
        dbaseFileReader.close();
        shapefileReader.close();
        shxFileReader.close();
    }
    /**
     * @return Row count
     */
    public long getRowCount() {
        return dbaseFileReader.getRecordCount();
    }
    public int getFieldCount() {
        return dbaseFileReader.getFieldCount() + 1;
    }
    /**
     * @param rowId Row index
     * @return The row content
     * @throws IOException
     */
    public Object[] getRow(long rowId) throws IOException {
        final int fieldCount = getFieldCount();
        Object[] values = new Object[fieldCount];
        // Copy dbf values
        for(int fieldId=0;fieldId<geometryFieldIndex;fieldId++) {
            values[fieldId] = dbaseFileReader.getFieldValue((int)rowId, fieldId);
        }
        values[geometryFieldIndex] = shapefileReader.geomAt(shxFileReader.getOffset((int)rowId));
        for(int fieldId=geometryFieldIndex;fieldId<fieldCount - 1;fieldId++) {
            values[fieldId + 1] = dbaseFileReader.getFieldValue((int)rowId, fieldId);
        }
        return values;
    }
}
