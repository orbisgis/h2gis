/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.shp.internal;

import org.locationtech.jts.geom.Geometry;
import org.h2gis.functions.io.FileDriver;
import org.h2gis.functions.io.dbf.internal.DBFDriver;
import org.h2gis.functions.io.dbf.internal.DbaseFileHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Merge ShapeFileReader and DBFReader.
 * TODO Handle SHP without SHX and/or DBF
 *
 * How to use:
 *
 * In Write mode,
 * Declare fields by calling {@link SHPDriver#initDriver(java.io.File, ShapeType, org.h2gis.drivers.dbf.internal.DbaseFileHeader)}
 * then write row using
 *
 *
 * @author Nicolas Fortin
 */
public class SHPDriver implements FileDriver {
    private DBFDriver dbfDriver = new DBFDriver();
    private File shpFile;
    private File shxFile;
    private File dbfFile ;
    private ShapefileReader shapefileReader;
    private ShapefileWriter shapefileWriter;
    private IndexFile shxFileReader;
    private int geometryFieldIndex = 0;
    private ShapeType shapeType;
    public File prjFile;
    private int srid =0;

    /**
     * @param geometryFieldIndex The geometry field index in getRow() array.
     */
    public void setGeometryFieldIndex(int geometryFieldIndex) {
        this.geometryFieldIndex = geometryFieldIndex;
    }

    /**
     * Insert values in the row
     * @param values
     * @throws IOException 
     */
    @Override
    public void insertRow(Object[] values) throws IOException {
        if(!(values[geometryFieldIndex] instanceof Geometry)) {
            if(values[geometryFieldIndex]==null) {
                throw new IOException("Shape files do not support NULL Geometry values.");
            } else {
                throw new IllegalArgumentException("Field at "+geometryFieldIndex+" should be an instance of Geometry," +
                        " found "+values[geometryFieldIndex].getClass()+" instead.");
            }
        }
        shapefileWriter.writeGeometry((Geometry)values[geometryFieldIndex]);
        // Extract the DBF part of the row
        Object[] dbfValues = new Object[values.length - 1];
        // Copy DBF data before geometryFieldIndex
        if(geometryFieldIndex > 0) {
            System.arraycopy(values, 0, dbfValues, 0, geometryFieldIndex);
        }
        // Copy DBF data after geometryFieldIndex
        if(geometryFieldIndex + 1 < values.length) {
            System.arraycopy(values, geometryFieldIndex + 1, dbfValues, geometryFieldIndex, dbfValues.length - geometryFieldIndex);
        }
        dbfDriver.insertRow(dbfValues);
    }

    /**
     * @return The geometry field index in getRow() array.
     */
    public int getGeometryFieldIndex() {
        return geometryFieldIndex;
    }

    /**
     * Init Driver for Write mode
     * @param shpFile
     * @param shapeType
     * @param dbaseHeader
     * @throws IOException
     */
    public void initDriver(File shpFile, ShapeType shapeType, DbaseFileHeader dbaseHeader) throws IOException {
        String path = shpFile.getAbsolutePath();
        String nameWithoutExt = path.substring(0,path.lastIndexOf('.'));
        this.shpFile = new File(nameWithoutExt+".shp");
        this.shxFile = new File(nameWithoutExt+".shx");
        this.dbfFile = new File(nameWithoutExt+".dbf");
        FileOutputStream shpFos = new FileOutputStream(shpFile);
        FileOutputStream shxFos = new FileOutputStream(shxFile);
        shapefileWriter = new ShapefileWriter(shpFos.getChannel(), shxFos.getChannel());
        this.shapeType = shapeType;
        shapefileWriter.writeHeaders(shapeType);
        dbfDriver.initDriver(dbfFile, dbaseHeader);
    }

    /**
     * Init this driver from existing files, then open theses files.
     * @param shpFile Shape file path.
     * @throws IOException
     */
    public void initDriverFromFile(File shpFile) throws IOException {
        initDriverFromFile(shpFile, null);
    }

    /**
     * Init this driver from existing files, then open theses files.
     * @param shpFile Shape file path.
     * @param forceEncoding If defined use this encoding instead of the one defined in dbf header.
     * @throws IOException
     */
    public void initDriverFromFile(File shpFile, String forceEncoding) throws IOException {             // Read columns from files metadata
        this.shpFile = shpFile;        
        
        if(!shpFile.exists()){
            throw new FileNotFoundException("The following file does not exists: " + shpFile.getPath());
        }        
        // Find appropriate file extension for shx and dbf, maybe SHX or Shx..
        //String shxFileName = shpFile.getName();
        
        String fileName = shpFile.getAbsolutePath();
        final int dotIndex = fileName.lastIndexOf('.');
        final String fileNamePrefix = fileName.substring(0, dotIndex).toLowerCase();
                     
        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {            
            @Override
            public boolean accept(Path entry) throws IOException {
                String path = entry.toAbsolutePath().toString().toLowerCase();
                if(path.equals(fileNamePrefix+".shx")){
                    shxFile = entry.toFile();
                    return true;
                }
                else if(path.equals(fileNamePrefix+".dbf")){
                    dbfFile = entry.toFile();
                    return true;
                }
                else if(path.equals(fileNamePrefix+".prj")){
                    prjFile = entry.toFile();
                    return true;
                }
                return false;
            }

        };
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(shpFile.getParentFile().toPath(), filter)) {            
            for (Path pathDir : stream) {                
               //Do nothing   
            } 
        }
        if(dbfFile != null) {
            dbfDriver.initDriverFromFile(dbfFile, forceEncoding);
        } else {
            throw new IllegalArgumentException("DBF File not found");
        }
        if(shxFile==null){
            throw new IllegalArgumentException("SHX File not found");
        }
        FileInputStream shpFis = new FileInputStream(shpFile);
        shapefileReader = new ShapefileReader(shpFis.getChannel());
        FileInputStream shxFis = new FileInputStream(shxFile);
        shxFileReader = new IndexFile(shxFis.getChannel());
    }

    /**
     * @return Dbase file header
     */
    public DbaseFileHeader getDbaseFileHeader() {
        return dbfDriver.getDbaseFileHeader();
    }

    @Override
    public long getRowCount() {
        return dbfDriver.getRowCount();
    }

    /**
     * @return ShapeFile header
     */
    public ShapefileHeader getShapeFileHeader() {
        return shapefileReader.getHeader();
    }

    @Override
    public void close() throws IOException {
        dbfDriver.close();
        if(shapefileReader != null) {
            shapefileReader.close();
            shxFileReader.close();
        } else if(shapefileWriter != null) {
            // Update header
            shapefileWriter.writeHeaders(shapeType);
            shapefileWriter.close();
        }
    }

    public int getFieldCount() {
        return dbfDriver.getFieldCount() + 1;
    }

    @Override
    public Object[] getRow(long rowId) throws IOException {
        final int fieldCount = getFieldCount();
        Object[] values = new Object[fieldCount];
        // Copy dbf values
        Object[] dbfValues = dbfDriver.getRow(rowId);
        // Copy dbf values before geometryFieldIndex
        if(geometryFieldIndex > 0) {
            System.arraycopy(dbfValues, 0, values, 0, geometryFieldIndex);
        }
        Geometry geom = shapefileReader.geomAt(shxFileReader.getOffset((int)rowId));
        if(geom!=null){
        geom.setSRID(getSrid());
        }
        values[geometryFieldIndex] = geom;
        // Copy dbf values after geometryFieldIndex
        if(geometryFieldIndex < dbfValues.length) {
            System.arraycopy(dbfValues, geometryFieldIndex, values, geometryFieldIndex + 1, dbfValues.length);
        }
        return values;
    }

    /**
     * Set a SRID code that will be used for geometries.
     * @param srid 
     */
    public void setSRID(int srid) {
        this.srid=srid;
    }

    /**
     * Get the SRID code
     * @return 
     */
    public int getSrid() {
        return srid;
    }
    
    
}
