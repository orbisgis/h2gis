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

package org.h2gis.functions.io.dbf;

import org.h2.command.ddl.CreateTableData;
import org.h2.table.Column;
import org.h2.value.TypeInfo;
import org.h2.value.Value;
import org.h2gis.functions.io.dbf.internal.DBFDriver;
import org.h2gis.functions.io.dbf.internal.DbaseFileHeader;
import org.h2gis.functions.io.file_table.FileEngine;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * SHP Table factory.
 * @author Nicolas Fortin
 */
public class DBFEngine extends FileEngine<DBFDriver> {

    @Override
    protected DBFDriver createDriver(File filePath, List<String> args) throws IOException {
        DBFDriver driver = new DBFDriver();
        driver.initDriverFromFile(filePath,  args.size() > 1 ? args.get(1) : null);
        return driver;
    }

    @Override
    protected void feedCreateTableData(DBFDriver driver, CreateTableData data) throws IOException {
        DbaseFileHeader header = driver.getDbaseFileHeader();
        feedTableDataFromHeader(header, data);
    }

    /**
     * Parse the DBF file then init the provided data structure
     * @param header dbf header
     * @param data Data to initialise
     */
    public static void feedTableDataFromHeader(DbaseFileHeader header, CreateTableData data) throws IOException {
        int numFields = header.getNumFields();
        for (int i = 0; i < numFields; i++) {
            String fieldsName = header.getFieldName(i);
            Column column = new Column(fieldsName.toUpperCase(), dbfTypeToH2Type(header,i));
            data.columns.add(column);
        }
    }

    /**
     * @see "http://www.clicketyclick.dk/databases/xbase/format/data_types.html"
     * @param header DBF File Header
     * @param i DBF Type identifier
     * @return H2 {@see Value}
     */
    private static TypeInfo dbfTypeToH2Type(DbaseFileHeader header, int i) throws IOException {
        switch (header.getFieldType(i)) {
            // (L)logical (T,t,F,f,Y,y,N,n)
            case 'l':
            case 'L':
                return TypeInfo.TYPE_BOOLEAN;
            // (C)character (String)
            case 'c':
            case 'C':
                return TypeInfo.getTypeInfo(Value.VARCHAR, header.getFieldLength(i), 0, null);
            // (D)date (Date)
            case 'd':
            case 'D':
                return TypeInfo.TYPE_DATE;
            // (F)floating (Double)
            case 'n':
            case 'N':
                if ((header.getFieldDecimalCount(i) == 0)) {
                    int fieldLength = header.getFieldLength(i);
                    if ((fieldLength >= 0)
                            && (fieldLength < 10)) {
                        return TypeInfo.TYPE_INTEGER;
                    } else {
                        return TypeInfo.TYPE_BIGINT;
                    }
                } else {
                   return new TypeInfo(Value.DOUBLE, header.getFieldLength(i), 0, null);
                }
            case 'f':
            case 'F': // floating point number
            case 'o':
            case 'O': // floating point number
                return new TypeInfo(Value.DOUBLE, header.getFieldLength(i), 0, null);
            default:
                throw new IOException("Unknown DBF field type "+header.getFieldType(i));
        }
    }
}
