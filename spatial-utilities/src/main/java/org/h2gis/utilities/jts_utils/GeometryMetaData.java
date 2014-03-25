/**
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
package org.h2gis.utilities.jts_utils;

import com.vividsolutions.jts.io.ByteArrayInStream;
import com.vividsolutions.jts.io.ByteOrderDataInStream;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.WKBConstants;

import java.io.IOException;

/**
 * Extract Geometry MetaData from WKB.
 * WKB Conversion source from {@link com.vividsolutions.jts.io.WKBReader}
 */
public class GeometryMetaData {
    /** If SRID is available */
    public final boolean hasSRID;
    /** If Z Component is available */
    public final boolean hasZ;
    /** Geometry type code */
    public final int geometryType;
    /** Geometry dimension 2 or 3 */
    public final int dimension;
    /** Projection code */
    public final int SRID;

    private GeometryMetaData(int dimension, boolean hasSRID, boolean hasZ, int geometryType, int SRID) {
        this.dimension = dimension;
        this.hasSRID = hasSRID;
        this.hasZ = hasZ;
        this.geometryType = geometryType;
        this.SRID = SRID;
    }

    /**
     * Read the first bytes of Geometry WKB.
     * @param bytes WKB Bytes
     * @return Geometry MetaData
     * @throws IOException If WKB meta is invalid (do not check the Geometry)
     */
    public static GeometryMetaData getMetaDataFromWKB(byte[] bytes) throws IOException {
        ByteOrderDataInStream dis = new ByteOrderDataInStream();
        dis.setInStream(new ByteArrayInStream(bytes));
        // determine byte order
        byte byteOrderWKB = dis.readByte();
        // always set byte order, since it may change from geometry to geometry
        int byteOrder = byteOrderWKB == WKBConstants.wkbNDR ? ByteOrderValues.LITTLE_ENDIAN : ByteOrderValues.BIG_ENDIAN;
        dis.setOrder(byteOrder);

        int typeInt = dis.readInt();
        int geometryType = typeInt & 0xff;
        // determine if Z values are present
        boolean hasZ = (typeInt & 0x80000000) != 0;
        int inputDimension =  hasZ ? 3 : 2;
        // determine if SRIDs are present
        boolean hasSRID = (typeInt & 0x20000000) != 0;

        int SRID = 0;
        if (hasSRID) {
            SRID = dis.readInt();
        }
        return new GeometryMetaData(inputDimension, hasSRID, hasZ, geometryType, SRID);
    }
}
