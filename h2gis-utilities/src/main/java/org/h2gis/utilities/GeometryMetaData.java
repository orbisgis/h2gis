/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.h2.value.ValueGeometry;
import org.locationtech.jts.geom.Geometry;

/**
 * Extract Geometry MetaData from various geometry signatures
 */
public class GeometryMetaData {

    private static Pattern CREATE_TABLE_PATTERN;

    /**
     * If Z Component is available
     */
    public boolean hasZ = false;

    /**
     * If M Component is available
     */
    public boolean hasM = false;

    /**
     * Geometry dimension 2 , 3 or 4
     */
    public int dimension = 2;

    /**
     * Full string representation of the geometry type include z and m dimension
     * e.g POINTZM
     */
    public String geometryType = "GEOMETRY";

    /**
     * SFS String representation of the geometry type only canonical form POINT,
     * LINESTRING...
     */
    public String sfs_geometryType = "GEOMETRY";

    /**
     * EPSG code
     */
    public int SRID = 0;

    /**
     * 0-based type names of geometries, subtract 1 from type code to get index
     * in this array.
     */
    static final String[] TYPES = { //
        "POINT", //
        "LINESTRING", //
        "POLYGON", //
        "MULTIPOINT", //
        "MULTILINESTRING", //
        "MULTIPOLYGON", //
        "GEOMETRYCOLLECTION", //
    };

    /**
     * Geometry type and dimension system in OGC geometry code format.
     */
    public int geometryTypeCode = 0;

    /**
     * Simplified geometry type code without +1000
     */
    public int sfs_geometryTypeCode = 0;

    public GeometryMetaData() {

    }

    /**
     * Linked with the H2 ValueGeometry model
     * 
     * @param valueGeometry 
     */
    private GeometryMetaData(ValueGeometry valueGeometry) {
        this.SRID = valueGeometry.getSRID();
        this.geometryTypeCode = valueGeometry.getTypeAndDimensionSystem();
        this.sfs_geometryTypeCode = geometryTypeCode % 1_000;
        initDimension();
        initGeometryType();
    }

    /**
     * Find geometry dimension properties
     *
     */
    public void initDimension() {
        switch (geometryTypeCode/1_000) {
            case 0:
                dimension = 2;
                break;
            case 1:
                dimension = 3;
                hasZ = true;
                break;
            case 2:
                dimension = 3;
                hasM = true;
                break;
            case 3:
                dimension = 4;
                hasZ = true;
                hasM = true;
                break;
            default:

        }

    }

    /**
     * Find full geometryType
     *
     */
    public void initGeometryType() {
        geometryType = TYPES[sfs_geometryTypeCode - 1];
        sfs_geometryType = geometryType;
        if (hasM && hasZ) {
            geometryType += "ZM";
        } else if (hasZ) {
            geometryType += "Z";
        } else if (hasM) {
            geometryType += "M";
        }

    }

    /**
     * Return the dimension of the geometry
     *
     * 2 if XZ 3 if XZZ or XYM 4 if XYZM
     *
     * @return
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Return a string representation of the geometry type as defined in SQL/MM
     * specification. SQL-MM 3: 5.1.4 and OGC SFS 1.2
     *
     * @return
     */
    public String getGeometryType() {
        return geometryType;
    }

    /**
     * Return an integer code representation of the geometry type as defined in
     * SQL/MM specification. SQL-MM 3: 5.1.4 and OGC SFS 1.2
     *
     * @return
     */
    public int getGeometryTypeCode() {
        return geometryTypeCode;
    }

    /**
     * Return the SRID
     *
     * @return
     */
    public int getSRID() {
        return SRID;
    }

    /**
     * true if the geometry as a M dimension
     *
     * @return
     */
    public boolean hasM() {
        return hasM;
    }

    /**
     * true if the geometry as a Z dimension
     *
     * @return
     */
    public boolean hasZ() {
        return hasZ;
    }

    public String getSfs_geometryType() {
        return sfs_geometryType;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public void setGeometryTypeCode(int geometryTypeCode) {
        this.geometryTypeCode = geometryTypeCode;
        this.sfs_geometryTypeCode = geometryTypeCode % 1_000;
    }

    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    public void setSRID(int SRID) {
        this.SRID = SRID;
    }

    public void setSfs_geometryType(String sfs_geometryType) {
        this.sfs_geometryType = sfs_geometryType;
    }

    public void setHasM(boolean hasM) {
        this.hasM = hasM;
    }

    public void setHasZ(boolean hasZ) {
        this.hasZ = hasZ;
    }

    /**
     * Read the first bytes of Geometry WKB.
     *
     * @param bytes WKB Bytes
     * @return Geometry MetaData
     */
    public static GeometryMetaData getMetaData(byte[] bytes) {
        ValueGeometry valueGeometry = ValueGeometry.get(bytes);
        return new GeometryMetaData(valueGeometry);
    }

    /**
     * Read the first bytes of Geometry.
     *
     * @param geometry
     * @return Geometry MetaData
     */
    public static GeometryMetaData getMetaData(Geometry geometry) {
        ValueGeometry valueGeometry = ValueGeometry.getFromGeometry(geometry);
        return new GeometryMetaData(valueGeometry);
    }
   

    /**
     * Read the metadata from its string representation The folowing signatures
     * are allowed :
     *
     * SRID=4326, POINT(0 0) POINT(0 0) GEOMETRY GEOMETRY(POINTZ, 4326)
     *
     * @param geometry string representation
     *
     * @return Geometry MetaData
     */
    public static GeometryMetaData getMetaData(String geometry) {
        if (geometry != null && !geometry.isEmpty()) {
            if (geometry.toUpperCase().startsWith("GEOMETRY")) {
                CREATE_TABLE_PATTERN = Pattern.compile("(?:(?:GEOMETRY\\s*\\(\\s*([a-zA-Z]+\\s*(?:[ZM]+)?)\\s*(?:,\\s*([\\d]+))?\\))|^\\s*([a-zA-Z]+\\s*(?:[ZM]+)?))", Pattern.CASE_INSENSITIVE);
                Matcher matcher = CREATE_TABLE_PATTERN.matcher(geometry);
                if (matcher.find()) {
                    String type = matcher.group(1);
                    if (type == null) {
                        return new GeometryMetaData();
                    } else {
                        String srid = matcher.group(2);
                        if (srid != null && !srid.isEmpty()) {
                            return createMetadataFromGeometryType(type, Integer.valueOf(srid));
                        } else {
                            return createMetadataFromGeometryType(type);
                        }
                    }
                }
            } else {
                ValueGeometry valueGeometry = ValueGeometry.get(geometry);
                return new GeometryMetaData(valueGeometry);
            }
        }
        return null;
    }

    /**
     * Find geometry metadata according the EWKT canonical form
     *
     * as defined in SQL/MM specification. SQL-MM 3: 5.1.4 and OGC SFS 1.2
     *
     * @param type : geometry type
     * @param srid : srid value
     * @return GeometryMetaData
     */
    private static GeometryMetaData createMetadataFromGeometryType(String type) {
        return createMetadataFromGeometryType(type, 0);
    }

    /**
     * Find geometry metadata according the EWKT canonical form and a SRID
     *
     * as defined in SQL/MM specification. SQL-MM 3: 5.1.4 and OGC SFS 1.2
     *
     * @param type : geometry type
     * @param srid : srid value
     * @return GeometryMetaData
     */
    private static GeometryMetaData createMetadataFromGeometryType(String type, int srid) {
        GeometryMetaData geometryMetaData = new GeometryMetaData();
        geometryMetaData.setSRID(srid);
        if (type == null) {
            return geometryMetaData;
        }
        int dimension_ = 0;
        int geometry_code = GeometryTypeCodes.GEOMETRY;
        String sfs_geometry_type = "GEOMETRY";
        String geometry_type = "GEOMETRY";
        boolean hasz_ = false;
        boolean hasm_ = false;
        type = type.replaceAll(" ", "").replaceAll("\"", "");
        switch (type) {
            case "POINT":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.POINT;
                sfs_geometry_type = "POINT";
                geometry_type = "POINT";
                break;
            case "LINESTRING":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.LINESTRING;
                sfs_geometry_type = "LINESTRING";
                geometry_type = "LINESTRING";
                break;
            case "POLYGON":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.POLYGON;
                sfs_geometry_type = "POLYGON";
                geometry_type = "POLYGON";
                break;
            case "MULTIPOINT":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.MULTIPOINT;
                sfs_geometry_type = "MULTIPOINT";
                geometry_type = "MULTIPOINT";
                break;
            case "MULTILINESTRING":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.MULTILINESTRING;
                sfs_geometry_type = "MULTILINESTRING";
                geometry_type = "MULTILINESTRING";
                break;
            case "MULTIPOLYGON":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.MULTIPOLYGON;
                sfs_geometry_type = "MULTIPOLYGON";
                geometry_type = "MULTIPOLYGON";
                break;
            case "GEOMETRYCOLLECTION":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.GEOMCOLLECTION;
                sfs_geometry_type = "GEOMCOLLECTION";
                geometry_type = "GEOMCOLLECTION";
                break;
            case "POINTZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.POINTZ;
                sfs_geometry_type = "POINTZ";
                geometry_type = "POINTZ";
                hasz_ = true;
                break;
            case "LINESTRINGZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.LINESTRINGZ;
                sfs_geometry_type = "LINESTRINGZ";
                geometry_type = "LINESTRINGZ";
                hasz_ = true;
                break;
            case "POLYGONZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.POLYGONZ;
                sfs_geometry_type = "POLYGONZ";
                geometry_type = "POLYGONZ";
                hasz_ = true;
                break;
            case "MULTIPOINTZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTIPOINTZ;
                sfs_geometry_type = "MULTIPOINTZ";
                geometry_type = "MULTIPOINTZ";
                hasz_ = true;
                break;
            case "MULTILINESTRINGZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTILINESTRINGZ;
                sfs_geometry_type = "MULTILINESTRINGZ";
                geometry_type = "MULTILINESTRINGZ";
                hasz_ = true;
                break;
            case "MULTIPOLYGONZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTIPOLYGONZ;
                sfs_geometry_type = "MULTIPOLYGONZ";
                geometry_type = "MULTIPOLYGONZ";
                hasz_ = true;
                break;
            case "GEOMETRYCOLLECTIONZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.GEOMCOLLECTIONZ;
                sfs_geometry_type = "GEOMETRYCOLLECTIONZ";
                geometry_type = "GEOMETRYCOLLECTIONZ";
                hasz_ = true;
                break;
            case "POINTM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.POINTM;
                sfs_geometry_type = "POINTM";
                geometry_type = "POINTM";
                hasm_ = true;
                break;
            case "LINESTRINGM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.LINESTRINGM;
                sfs_geometry_type = "LINESTRINGM";
                geometry_type = "LINESTRINGM";
                hasm_ = true;
                break;
            case "POLYGONM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.POLYGONM;
                sfs_geometry_type = "POLYGONM";
                geometry_type = "POLYGONM";
                hasm_ = true;
                break;
            case "MULTIPOINTM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTIPOINTM;
                sfs_geometry_type = "MULTIPOINTM";
                geometry_type = "MULTIPOINTM";
                hasm_ = true;
                break;
            case "MULTILINESTRINGM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTILINESTRINGM;
                sfs_geometry_type = "MULTILINESTRINGM";
                geometry_type = "MULTILINESTRINGM";
                hasm_ = true;
                break;
            case "MULTIPOLYGONM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTIPOLYGONM;
                sfs_geometry_type = "MULTIPOLYGONM";
                geometry_type = "MULTIPOLYGONM";
                hasm_ = true;
                break;
            case "GEOMETRYCOLLECTIONM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.GEOMCOLLECTIONM;
                sfs_geometry_type = "GEOMETRYCOLLECTIONM";
                geometry_type = "GEOMETRYCOLLECTIONM";
                hasm_ = true;
                break;
            case "POINTZM":
                dimension_ = 4;
                geometry_code = GeometryTypeCodes.POINTZM;
                sfs_geometry_type = "POINTZM";
                geometry_type = "POINTZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "LINESTRINGZM":
                dimension_ = 4;
                geometry_code = GeometryTypeCodes.LINESTRINGZM;
                sfs_geometry_type = "LINESTRINGZM";
                geometry_type = "LINESTRINGZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "POLYGONZM":
                dimension_ = 4;
                geometry_code = GeometryTypeCodes.POLYGONZM;
                sfs_geometry_type = "POLYGONZM";
                geometry_type = "POLYGONZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "MULTIPOINTZM":
                dimension_ = 4;
                geometry_code = GeometryTypeCodes.MULTIPOINTZM;
                sfs_geometry_type = "MULTIPOINTZM";
                geometry_type = "MULTIPOINTZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "MULTILINESTRINGZM":
                dimension_ = 4;
                geometry_code = GeometryTypeCodes.MULTILINESTRINGZM;
                sfs_geometry_type = "MULTILINESTRINGZM";
                geometry_type = "MULTILINESTRINGZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "MULTIPOLYGONZM":
                dimension_ = 4;
                geometry_code = GeometryTypeCodes.MULTIPOLYGONZM;
                sfs_geometry_type = "MULTIPOLYGONZM";
                geometry_type = "MULTIPOLYGONZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "GEOMETRYCOLLECTIONZM":
                dimension_ = 4;
                geometry_code = GeometryTypeCodes.GEOMCOLLECTIONZM;
                sfs_geometry_type = "GEOMETRYCOLLECTIONZM";
                geometry_type = "GEOMETRYCOLLECTIONZM";
                hasz_ = true;
                hasm_ = true;
                break;
            case "GEOMETRY":
            default:
        }
        geometryMetaData.setDimension(dimension_);
        geometryMetaData.setGeometryTypeCode(geometry_code);
        geometryMetaData.setSfs_geometryType(sfs_geometry_type);
        geometryMetaData.setGeometryType(geometry_type);
        geometryMetaData.setHasM(hasm_);
        geometryMetaData.setHasZ(hasz_);
        return geometryMetaData;
    }
}