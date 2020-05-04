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

import org.h2.value.ValueGeometry;
import org.locationtech.jts.geom.Geometry;

/**
 * Extract Geometry MetaData from WKB. WKB Conversion source from
 * {@link org.locationtech.jts.io.WKBReader}
 */
public class GeometryMetaData {

    /**
     * If Z Component is available
     */
    public boolean hasZ;

    /**
     * If M Component is available
     */
    private boolean hasM;

    /**
     * Geometry type code
     */
    public int geometryTypeCode =0;

    /**
     * Geometry dimension 2 , 3 or 4
     */
    public int dimension;

    /**
     * String representation of the geometry type
     */
    public String geometryType;

    /**
     * EPSG code
     */
    public int SRID =-1;

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

    private GeometryMetaData(ValueGeometry valueGeometry) {
        this.SRID = valueGeometry.getSRID();
        this.geometryTypeCode = valueGeometry.getGeometryType();
        int dimSystem = valueGeometry.getDimensionSystem();
        init(dimSystem);
    }

    /**
     * Find geometry properties
     *
     * @param dimSystem
     */
    private void init(int dimSystem) {
        switch (dimSystem) {
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
        geometryType = TYPES[geometryTypeCode - 1];
        if (hasM && hasZ) {
            geometryType += "ZM";
        } else if (hasZ) {
            geometryType += "Z";
        } else if (hasM) {
            geometryType += "M";
        }

    }

    /**
     * Return the dimension of the geometry 2 if XZ 3 if XZZ or XYM 4 if XYZM
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
}
