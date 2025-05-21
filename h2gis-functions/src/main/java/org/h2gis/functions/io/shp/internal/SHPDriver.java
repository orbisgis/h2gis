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

package org.h2gis.functions.io.shp.internal;

import org.h2.value.Value;
import org.h2.value.ValueGeometry;
import org.h2.value.ValueNull;
import org.h2gis.api.FileDriver;
import org.h2gis.functions.io.dbf.internal.DBFDriver;
import org.h2gis.functions.io.dbf.internal.DbaseFileHeader;
import org.locationtech.jts.geom.Geometry;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Merge ShapeFileReader and DBFReader.
 * TODO Handle SHP without SHX and/or DBF
 *
 * How to use:
 *
 * In Write mode,
 * Declare fields by calling {@link SHPDriver#initDriver(File, ShapeType, DbaseFileHeader)} Driver(java.io.File, ShapeType, org.h2gis.drivers.dbf.internal.DbaseFileHeader)}
 * then write row using
 *
 *
 * @author Nicolas Fortin
 */
public class SHPDriver implements FileDriver {
    private DBFDriver dbfDriver = new DBFDriver();
    public File shpFile;
    public File shxFile;
    public File dbfFile ;
    private ShapefileReader shapefileReader;
    private ShapefileWriter shapefileWriter;
    private IndexFile shxFileReader;
    private int geometryFieldIndex = 0;
    private ShapeType shapeType;
    public File prjFile;
    public File cpgFile;
    private int srid =0;


    /**
     * @param geometryFieldIndex The geometry field index in getRow() array.
     */
    public void setGeometryFieldIndex(int geometryFieldIndex) {
        this.geometryFieldIndex = geometryFieldIndex;
    }

    /**
     * Insert values in the row
     * @param values values to insert
     */
    @Override
    public void insertRow(Object[] values) throws IOException {
        Object geomValue = values[geometryFieldIndex];
        if(!(geomValue instanceof Geometry)) {
            if(geomValue==null) {
                throw new IOException("Shape files do not support NULL Geometry values.");
            } else {
                throw new IllegalArgumentException("Field at "+geometryFieldIndex+" should be an instance of Geometry," +
                        " found "+geomValue.getClass()+" instead.");
            }
        }
        shapefileWriter.writeGeometry((Geometry)geomValue);
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
     * @param shpFile shp file
     * @param shapeType type of the shape
     * @param dbaseHeader dbase header
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
     */
    public void initDriverFromFile(File shpFile) throws IOException {
        initDriverFromFile(shpFile, null);
    }

    /**
     * Init this driver from existing files, then open theses files.
     * @param shpFile Shape file path.
     * @param forceEncoding If defined use this encoding instead of the one defined in dbf header.
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
                } else if(path.equals(fileNamePrefix+".cpg")){
                    cpgFile = entry.toFile();
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
            //Read the CPG file if exists
            if(cpgFile!=null){
                BufferedReader br = Files.newBufferedReader(cpgFile.toPath());
                String codePage;
                if ((codePage = br.readLine()) != null && forceEncoding==null) {
                    forceEncoding = codePage.trim();
                }
            }
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

    @Override
    public int getFieldCount() {
        return dbfDriver.getFieldCount() + 1;
    }

    @Override
    public int getEstimatedRowSize(long rowId) {
        int totalSize = 0;
        totalSize += dbfDriver.getEstimatedRowSize(rowId);
        try {
            totalSize += shxFileReader.getContentLength((int) rowId);
        } catch (IOException ex) {
            // Ignore
        }
        return totalSize;
    }

    @Override
    public Value getField(long rowId, int column) throws IOException {
        if (column == geometryFieldIndex) {
            Geometry geom = shapefileReader.geomAt(shxFileReader.getOffset((int) rowId));
            if (geom != null) {
                geom.setSRID(getSrid());
                return ValueGeometry.getFromGeometry(geom);
            } else {
                return ValueNull.INSTANCE;
            }
        } else {
            if(geometryFieldIndex < column) {
                return dbfDriver.getDbaseFileReader().getFieldValue((int) rowId, column - 1);
            } else {
                return dbfDriver.getDbaseFileReader().getFieldValue((int) rowId, column);
            }
        }
    }

    /**
     * Set a SRID code that will be used for geometries.
     * @param srid int value
     */
    public void setSRID(int srid) {
        this.srid=srid;
    }

    /**
     * Get the SRID code
     * @return get the SRID
     */
    public int getSrid() {
        return srid;
    }
    
    
}
