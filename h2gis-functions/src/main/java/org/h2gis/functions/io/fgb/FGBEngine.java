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
package org.h2gis.functions.io.fgb;

import org.h2.command.ddl.CreateTableData;
import org.h2.engine.Constants;
import org.h2.table.Column;
import org.h2.value.ExtTypeInfo;
import org.h2.value.ExtTypeInfoGeometry;
import org.h2.value.TypeInfo;
import org.h2.value.Value;
import org.h2gis.functions.io.fgb.fileTable.FGBDriver;
import org.h2gis.functions.io.file_table.FileEngine;
import org.h2gis.utilities.GeometryTypeCodes;
import org.wololo.flatgeobuf.ColumnMeta;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.generated.ColumnType;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * FlatGeobuffer engine for link with file instead of copy
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class FGBEngine extends FileEngine<FGBDriver> {


    @Override
    protected FGBDriver createDriver(File filePath, List<String> args) throws IOException {
        FGBDriver driver = new FGBDriver();
        driver.initDriverFromFile(filePath);
        return driver;
    }

    @Override
    protected void feedCreateTableData(FGBDriver driver, CreateTableData data) throws IOException {
        HeaderMeta header = driver.getHeader();
        byte geometryType = header.geometryType;
        boolean hasM = header.hasM;
        boolean hasZ = header.hasZ;
        int srid = header.srid;
        int geomType = GeometryTypeCodes.GEOMETRY;
        if (geometryType==1){
            if(hasM && hasM){
                geomType = GeometryTypeCodes.POINTZM;
            }else if(hasM){
                geomType = GeometryTypeCodes.POINTM;
            } else if (hasZ) {
                geomType = GeometryTypeCodes.POINTZ;
            }else{
                geomType = GeometryTypeCodes.POINT;
            }
        } else if (geometryType==2) {
            if(hasM && hasM){
                geomType = GeometryTypeCodes.LINESTRINGZM;
            }else if(hasM){
                geomType = GeometryTypeCodes.LINESTRINGM;
            } else if (hasZ) {
                geomType = GeometryTypeCodes.LINESTRINGZ;
            }else{
                geomType = GeometryTypeCodes.LINESTRING;
            }
        }  else if (geometryType==3) {
            if(hasM && hasM){
                geomType = GeometryTypeCodes.POLYGONZM;
            }else if(hasM){
                geomType = GeometryTypeCodes.POLYGONM;
            } else if (hasZ) {
                geomType = GeometryTypeCodes.POLYGONZ;
            }else{
                geomType = GeometryTypeCodes.POLYGON;
            }
        }  else if (geometryType==4) {
            if(hasM && hasM){
                geomType = GeometryTypeCodes.MULTIPOINTZM;
            }else if(hasM){
                geomType = GeometryTypeCodes.MULTIPOINTM;
            } else if (hasZ) {
                geomType = GeometryTypeCodes.MULTIPOINTZ;
            }else{
                geomType = GeometryTypeCodes.MULTIPOINT;
            }
        }  else if (geometryType==5) {
            if(hasM && hasM){
                geomType = GeometryTypeCodes.MULTILINESTRINGZM;
            }else if(hasM){
                geomType = GeometryTypeCodes.MULTILINESTRINGM;
            } else if (hasZ) {
                geomType = GeometryTypeCodes.MULTILINESTRINGZ;
            }else{
                geomType = GeometryTypeCodes.MULTILINESTRING;
            }
        }  else if (geometryType==6) {
            if(hasM && hasM){
                geomType = GeometryTypeCodes.MULTIPOLYGONZM;
            }else if(hasM){
                geomType = GeometryTypeCodes.MULTIPOLYGONM;
            } else if (hasZ) {
                geomType = GeometryTypeCodes.MULTIPOLYGONZ;
            }else{
                geomType = GeometryTypeCodes.MULTIPOLYGON;
            }
        }  else if (geometryType==7) {
            if(hasM && hasM){
                geomType = GeometryTypeCodes.GEOMCOLLECTIONZM;
            }else if(hasM){
                geomType = GeometryTypeCodes.GEOMCOLLECTIONM;
            } else if (hasZ) {
                geomType = GeometryTypeCodes.GEOMCOLLECTIONZ;
            }else{
                geomType = GeometryTypeCodes.GEOMCOLLECTION;
            }
        }
        ExtTypeInfo extTypeInfo = new ExtTypeInfoGeometry(geomType, srid);
        TypeInfo typeInfo = TypeInfo.getTypeInfo(
                TypeInfo.TYPE_GEOMETRY.getValueType(),
                TypeInfo.TYPE_GEOMETRY.getPrecision(),
                TypeInfo.TYPE_GEOMETRY.getScale(),
                extTypeInfo);
        Column geometryColumn = new Column("THE_GEOM", typeInfo);
        data.columns.add(geometryColumn);
        List<ColumnMeta> columns = header.columns;
        for (ColumnMeta col: columns ) {
            Column column = new Column(col.name.toUpperCase(), fgbTypeToH2Type(col));
            column.setComment(col.description);
            data.columns.add(column);
        }
    }

    /**
     * @param columnMeta
     * @return H2 {@see Value}
     * @see "https://github.com/flatgeobuf/flatgeobuf/blob/master/src/fbs/header.fbs"
     */
    private static TypeInfo fgbTypeToH2Type(ColumnMeta columnMeta) throws IOException {
        byte type = columnMeta.type;
        int precision = columnMeta.precision;
        int scale = columnMeta.scale;
        switch (type) {
            case ColumnType.Bool:
                return TypeInfo.TYPE_BOOLEAN;
            case ColumnType.String:
                int width = columnMeta.width;
                if(width==0){
                    width = Constants.MAX_STRING_LENGTH;
                }
                return TypeInfo.getTypeInfo(Value.VARCHAR, width, 0, null);
            case ColumnType.DateTime:
                return TypeInfo.TYPE_TIMESTAMP;
            case ColumnType.Byte:
            case ColumnType.Short :
            case ColumnType.UShort:
                return TypeInfo.TYPE_SMALLINT;
            case ColumnType.Int:
            case ColumnType.UInt:
            case ColumnType.UByte:
                return TypeInfo.TYPE_INTEGER;
            case ColumnType.Long:
            case ColumnType.ULong:
                return TypeInfo.TYPE_BIGINT;
            case ColumnType.Float:
            case ColumnType.Double:
                if (precision == 0 || precision >= 25 && precision <= 53) {
                    return new TypeInfo(Value.DOUBLE, precision, scale, null);
                }
                return TypeInfo.TYPE_DOUBLE;
            case ColumnType.Json:
                return TypeInfo.TYPE_JSON;
            case ColumnType.Binary :
                return TypeInfo.TYPE_BINARY;
            default:
                throw new IOException("Unknown FGB field type "+ type);
        }
    }
}
