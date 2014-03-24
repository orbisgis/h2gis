/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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

package org.h2gis.drivers.dbf;

import org.h2.command.ddl.CreateTableData;
import org.h2.table.Column;
import org.h2.value.Value;
import org.h2gis.drivers.file_table.FileEngine;
import org.h2gis.drivers.dbf.internal.DBFDriver;
import org.h2gis.drivers.dbf.internal.DbaseFileHeader;
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
     * @param data Data to initialise
     * @throws java.io.IOException
     */
    public static void feedTableDataFromHeader(DbaseFileHeader header, CreateTableData data) throws IOException {
        for (int i = 0; i < header.getNumFields(); i++) {
            String fieldsName = header.getFieldName(i);
            final int type = dbfTypeToH2Type(header,i);
            Column column = new Column(fieldsName.toUpperCase(), type);
            column.setPrecision(header.getFieldLength(i)); // set string length
            data.columns.add(column);
        }
    }

    /**
     * @see "http://www.clicketyclick.dk/databases/xbase/format/data_types.html"
     * @param header DBF File Header
     * @param i DBF Type identifier
     * @return H2 {@see Value}
     * @throws java.io.IOException
     */
    private static int dbfTypeToH2Type(DbaseFileHeader header, int i) throws IOException {
        switch (header.getFieldType(i)) {
            // (L)logical (T,t,F,f,Y,y,N,n)
            case 'l':
            case 'L':
                return Value.BOOLEAN;
            // (C)character (String)
            case 'c':
            case 'C':
                return Value.STRING_FIXED;
            // (D)date (Date)
            case 'd':
            case 'D':
                return Value.DATE;
            // (F)floating (Double)
            case 'n':
            case 'N':
                if ((header.getFieldDecimalCount(i) == 0)) {
                    if ((header.getFieldLength(i) >= 0)
                            && (header.getFieldLength(i) < 10)) {
                        return Value.INT;
                    } else {
                        return Value.LONG;
                    }
                }
            case 'f':
            case 'F': // floating point number
            case 'o':
            case 'O': // floating point number
                return Value.DOUBLE;
            default:
                throw new IOException("Unknown DBF field type "+header.getFieldType(i));
        }
    }
}
