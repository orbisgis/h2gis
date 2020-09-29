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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.h2.value.ValueGeometry;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ByteArrayInStream;
import org.locationtech.jts.io.ByteOrderDataInStream;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.WKBConstants;

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
     * Geometry type mask that indicates presence of dimension Z.
     */
    public static final int EWKB_Z = 0x8000_0000;

    /**
     * Geometry type mask that indicates presence of dimension M.
     */
    public static final int EWKB_M = 0x4000_0000;

    /**
     * Geometry type mask that indicates presence of SRID.
     */
    public static final int EWKB_SRID = 0x2000_0000;

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
        switch (geometryTypeCode / 1_000) {
            case 0:
                dimension = 2;
                break;
            case 1:
                dimension = 3;
                hasZ = true;
                hasM = false;
                break;
            case 2:
                dimension = 3;
                hasM = true;
                hasZ = false;
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
        if (sfs_geometryTypeCode != 0) {
            geometryType = TYPES[sfs_geometryTypeCode - 1];
            sfs_geometryType = geometryType;
            if (hasM && hasZ) {
                geometryType += "ZM";
                dimension = 4;
            } else if (hasZ) {
                geometryType += "Z";
                dimension = 3;
            } else if (hasM) {
                geometryType += "M";
                dimension = 3;
            }
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

    /**
     * Set SFS type code
     * @param sfs_geometryTypeCode
     */
    public void setSfs_geometryTypeCode(int sfs_geometryTypeCode) {
        this.sfs_geometryTypeCode = sfs_geometryTypeCode;
    }

    /**
     * Get SFS type code
     * @return
     */
    public String getSfs_geometryType() {
        return sfs_geometryType;
    }

    /**
     * Set the dimension of the geometry
     * @param dimension
     */
    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    /**
     * Set full geometry type with +1000
     * @param geometryTypeCode
     */
    public void setGeometryTypeCode(int geometryTypeCode) {
        this.geometryTypeCode = geometryTypeCode;
        this.sfs_geometryTypeCode = geometryTypeCode % 1_000;
    }

    /**
     * Set the geometry type name
     * @param geometryType
     */
    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    /**
     * Set the SRID
     * @param SRID
     */
    public void setSRID(int SRID) {
        this.SRID = SRID;
    }

    /**
     * Set the SFS geometry type name
     * @param sfs_geometryType
     */
    public void setSfs_geometryType(String sfs_geometryType) {
        this.sfs_geometryType = sfs_geometryType;
    }

    /**
     * True is geometry has M
     * @param hasM
     */
    public void setHasM(boolean hasM) {
        this.hasM = hasM;
    }
    /**
     * True is geometry has Z
     * @param hasZ
     */
    public void setHasZ(boolean hasZ) {
        this.hasZ = hasZ;
    }

    /**
     * Return the SQL representation of the geometry signature
     *
     * @return
     */
    public String getSQL() {
        StringBuilder sb = new StringBuilder("GEOMETRY");
        if (geometryTypeCode != GeometryTypeCodes.GEOMETRY) {
            sb.append("(").append(geometryType);
            if (SRID != 0) {
                sb.append(",").append(SRID);
            }
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Read the first bytes of Geometry WKB.
     * Note this method read the SRID from the EWKB mask
     * It's better to use getMetaData(geometry) to get all metadata
     * @param bytes WKB Bytes
     * @return Geometry MetaData
     * @throws IOException If WKB meta is invalid (do not check the Geometry)
     */
    public static GeometryMetaData getMetaData(byte[] bytes)  {
        try {
            ByteOrderDataInStream dis = new ByteOrderDataInStream();
            dis.setInStream(new ByteArrayInStream(bytes));
            // determine byte order
            byte byteOrderWKB = dis.readByte();
            // always set byte order, since it may change from geometry to geometry
            int byteOrder = byteOrderWKB == WKBConstants.wkbNDR ? ByteOrderValues.LITTLE_ENDIAN : ByteOrderValues.BIG_ENDIAN;
            dis.setOrder(byteOrder);
            int typeInt = dis.readInt();
            int geometryType = typeInt & 0xff;
            //From H2
            boolean hasZ = (typeInt & EWKB_Z) != 0;
            boolean hasM = (typeInt & EWKB_M) != 0;
            int srid = (typeInt & EWKB_SRID) != 0 ? dis.readInt() : 0;
            GeometryMetaData geomMet = new GeometryMetaData();
            geomMet.setHasM(hasM);
            geomMet.setHasZ(hasZ);
            geomMet.setSRID(srid);
            geomMet.setSfs_geometryTypeCode(geometryType);
            geomMet.initGeometryType();

            return geomMet;
        }catch (IOException ex) {
            throw new RuntimeException("Cannot read the geometry metadata");
        }
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
     * @return GeometryMetaData
     */
    public static GeometryMetaData createMetadataFromGeometryType(String type) {
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
    public static GeometryMetaData createMetadataFromGeometryType(String type, int srid) {
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
                hasz_ = false;
                hasm_ = false;
                break;
            case "LINESTRING":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.LINESTRING;
                sfs_geometry_type = "LINESTRING";
                geometry_type = "LINESTRING";
                hasz_ = false;
                hasm_ = false;
                break;
            case "POLYGON":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.POLYGON;
                sfs_geometry_type = "POLYGON";
                geometry_type = "POLYGON";
                hasz_ = false;
                hasm_ = false;
                break;
            case "MULTIPOINT":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.MULTIPOINT;
                sfs_geometry_type = "MULTIPOINT";
                geometry_type = "MULTIPOINT";
                hasz_ = false;
                hasm_ = false;
                break;
            case "MULTILINESTRING":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.MULTILINESTRING;
                sfs_geometry_type = "MULTILINESTRING";
                geometry_type = "MULTILINESTRING";
                hasz_ = false;
                hasm_ = false;
                break;
            case "MULTIPOLYGON":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.MULTIPOLYGON;
                sfs_geometry_type = "MULTIPOLYGON";
                geometry_type = "MULTIPOLYGON";
                hasz_ = false;
                hasm_ = false;
                break;
            case "GEOMETRYCOLLECTION":
                dimension_ = 2;
                geometry_code = GeometryTypeCodes.GEOMCOLLECTION;
                sfs_geometry_type = "GEOMCOLLECTION";
                geometry_type = "GEOMCOLLECTION";
                hasz_ = false;
                hasm_ = false;
                break;
            case "POINTZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.POINTZ;
                sfs_geometry_type = "POINTZ";
                geometry_type = "POINTZ";
                hasz_ = true;
                hasm_ = false;
                break;
            case "LINESTRINGZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.LINESTRINGZ;
                sfs_geometry_type = "LINESTRINGZ";
                geometry_type = "LINESTRINGZ";
                hasz_ = true;
                hasm_ = false;
                break;
            case "POLYGONZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.POLYGONZ;
                sfs_geometry_type = "POLYGONZ";
                geometry_type = "POLYGONZ";
                hasz_ = true;
                hasm_ = false;
                ;
                break;
            case "MULTIPOINTZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTIPOINTZ;
                sfs_geometry_type = "MULTIPOINTZ";
                geometry_type = "MULTIPOINTZ";
                hasz_ = true;
                hasm_ = false;
                break;
            case "MULTILINESTRINGZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTILINESTRINGZ;
                sfs_geometry_type = "MULTILINESTRINGZ";
                geometry_type = "MULTILINESTRINGZ";
                hasz_ = true;
                hasm_ = false;
                break;
            case "MULTIPOLYGONZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTIPOLYGONZ;
                sfs_geometry_type = "MULTIPOLYGONZ";
                geometry_type = "MULTIPOLYGONZ";
                hasz_ = true;
                hasm_ = false;
                break;
            case "GEOMETRYCOLLECTIONZ":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.GEOMCOLLECTIONZ;
                sfs_geometry_type = "GEOMETRYCOLLECTIONZ";
                geometry_type = "GEOMETRYCOLLECTIONZ";
                hasz_ = true;
                hasm_ = false;
                break;
            case "POINTM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.POINTM;
                sfs_geometry_type = "POINTM";
                geometry_type = "POINTM";
                hasz_ = false;
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
                hasz_ = false;
                hasm_ = true;
                break;
            case "MULTIPOINTM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTIPOINTM;
                sfs_geometry_type = "MULTIPOINTM";
                geometry_type = "MULTIPOINTM";
                hasz_ = false;
                hasm_ = true;
                break;
            case "MULTILINESTRINGM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTILINESTRINGM;
                sfs_geometry_type = "MULTILINESTRINGM";
                geometry_type = "MULTILINESTRINGM";
                hasz_ = false;
                hasm_ = true;
                break;
            case "MULTIPOLYGONM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.MULTIPOLYGONM;
                sfs_geometry_type = "MULTIPOLYGONM";
                geometry_type = "MULTIPOLYGONM";
                hasz_ = false;
                hasm_ = true;
                break;
            case "GEOMETRYCOLLECTIONM":
                dimension_ = 3;
                geometry_code = GeometryTypeCodes.GEOMCOLLECTIONM;
                sfs_geometry_type = "GEOMETRYCOLLECTIONM";
                geometry_type = "GEOMETRYCOLLECTIONM";
                hasz_ = false;
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
