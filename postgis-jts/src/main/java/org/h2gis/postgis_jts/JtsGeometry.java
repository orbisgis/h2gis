/*
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
package org.h2gis.postgis_jts;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.WKTReader;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class JtsGeometry extends PGobject {
    private static final long serialVersionUID = 256L;
    private Geometry geom;
    private static final JtsBinaryParser bp = new JtsBinaryParser();
    private static final JtsBinaryWriter bw = new JtsBinaryWriter();
    private static final PrecisionModel prec = new PrecisionModel();
    private static final CoordinateSequenceFactory csfac;
    protected static final GeometryFactory geofac;
    private static final WKTReader reader;

    public JtsGeometry() {
        this.setType("geometry");
    }

    public JtsGeometry(Geometry geom) {
        this();
        this.geom = geom;
    }

    public JtsGeometry(String value) throws SQLException {
        this();
        this.setValue(value);
    }

    public void setValue(String value) throws SQLException {
        this.geom = geomFromString(value);
    }

    public static Geometry geomFromString(String value) throws SQLException {
        try {
            value = value.trim();
            if (!value.startsWith("00") && !value.startsWith("01")) {
                int srid = 0;
                if (value.startsWith("SRID=")) {
                    String[] temp = value.split(";");
                    value = temp[1].trim();
                    srid = Integer.parseInt(temp[0].substring(5));
                }

                Geometry result = reader.read(value);
                setSridRecurse(result, srid);
                return result;
            } else {
                return bp.parse(value);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
            throw new SQLException("Error parsing SQL data:" + var4);
        }
    }

    public static void setSridRecurse(Geometry geom, int srid) {
        geom.setSRID(srid);
        int subcnt;
        if (geom instanceof GeometryCollection) {
            int num = geom.getNumGeometries();

            for(subcnt = 0; subcnt < num; ++subcnt) {
                setSridRecurse(geom.getGeometryN(subcnt), srid);
            }
        } else if (geom instanceof Polygon) {
            Polygon poly = (Polygon)geom;
            poly.getExteriorRing().setSRID(srid);
            subcnt = poly.getNumInteriorRing();

            for(int i = 0; i < subcnt; ++i) {
                poly.getInteriorRingN(i).setSRID(srid);
            }
        }

    }

    public Geometry getGeometry() {
        return this.geom;
    }

    public String toString() {
        return this.geom.toString();
    }

    public String getValue() {
        return bw.writeHexed(this.getGeometry());
    }

    public Object clone() {
        JtsGeometry obj = new JtsGeometry(this.geom);
        obj.setType(this.type);
        return obj;
    }

    public boolean equals(Object obj) {
        if (obj instanceof JtsGeometry) {
            Geometry other = ((JtsGeometry)obj).geom;
            if (this.geom == other) {
                return true;
            }

            if (this.geom != null && other != null) {
                return other.equals(this.geom);
            }
        }

        return false;
    }

    static {
        csfac = PackedCoordinateSequenceFactory.DOUBLE_FACTORY;
        geofac = new GeometryFactory(prec, 0, csfac);
        reader = new WKTReader(geofac);
    }
}

