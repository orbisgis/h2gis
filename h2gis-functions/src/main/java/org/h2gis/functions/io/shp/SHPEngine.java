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

package org.h2gis.functions.io.shp;

import org.h2.command.Parser;
import org.h2.command.ddl.CreateTableData;
import org.h2.table.Column;
import org.h2.value.Value;
import org.h2gis.functions.io.dbf.DBFEngine;
import org.h2gis.functions.io.file_table.FileEngine;
import org.h2gis.functions.io.shp.internal.SHPDriver;
import org.h2gis.functions.io.shp.internal.ShapeType;
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
        Column geometryColumn = new Column("THE_GEOM", Value.GEOMETRY);
        Parser parser = new Parser(data.session);
        geometryColumn.addCheckConstraint(data.session,
                parser.parseExpression("ST_GeometryTypeCode(THE_GEOM) = "+getGeometryTypeCodeFromShapeType(driver.getShapeFileHeader().getShapeType())));
        data.columns.add(geometryColumn);
        DBFEngine.feedTableDataFromHeader(driver.getDbaseFileHeader(), data);
    }
}
