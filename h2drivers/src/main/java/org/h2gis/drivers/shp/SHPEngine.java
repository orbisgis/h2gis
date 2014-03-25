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

package org.h2gis.drivers.shp;

import org.h2.command.Parser;
import org.h2.command.ddl.CreateTableData;
import org.h2.table.Column;
import org.h2.value.Value;
import org.h2gis.drivers.dbf.DBFEngine;
import org.h2gis.drivers.file_table.FileEngine;
import org.h2gis.drivers.shp.internal.SHPDriver;
import org.h2gis.drivers.shp.internal.ShapeType;
import org.h2gis.utilities.GeometryTypeCodes;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * SHP Table factory.
 * @author Nicolas Fortin
 */
public class SHPEngine extends FileEngine<SHPDriver> {

    @Override
    protected SHPDriver createDriver(File filePath, List<String> args) throws IOException {
        SHPDriver driver = new SHPDriver();
        driver.initDriverFromFile(filePath, args.size() > 1 ? args.get(1) : null);
        return driver;
    }

    private static int getGeometryTypeCodeFromShapeType(ShapeType shapeType) {
        if(shapeType.isPointType()) {
            return GeometryTypeCodes.MULTIPOINT;
        } else if(shapeType.isLineType()) {
            return GeometryTypeCodes.MULTILINESTRING;
        } else {
            return GeometryTypeCodes.MULTIPOLYGON;
        }
    }

    @Override
    protected void feedCreateTableData(SHPDriver driver, CreateTableData data) throws IOException {
        if(data.columns.isEmpty()) {
            Column geometryColumn = new Column("THE_GEOM", Value.GEOMETRY);
            Parser parser = new Parser(data.session);
            geometryColumn.addCheckConstraint(data.session,
                    parser.parseExpression("ST_GeometryTypeCode(THE_GEOM) = "+getGeometryTypeCodeFromShapeType(driver.getShapeFileHeader().getShapeType())));
            data.columns.add(geometryColumn);
            DBFEngine.feedTableDataFromHeader(driver.getDbaseFileHeader(), data);
        }
    }
}
