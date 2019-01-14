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
import org.postgis.binary.ByteGetter;
import org.postgis.binary.ValueGetter;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence.Double;

/**
 * Parser class able to convert binary data into a JTS {@link org.locationtech.jts.geom.Geometry}.
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public class JtsBinaryParser {
    /**
     * Default empty constructor.
     */
    public JtsBinaryParser() {}

    /**
     * Return the {@link org.postgis.binary.ValueGetter} for the endian from the given
     * {@link org.postgis.binary.ByteGetter}.
     *
     * @param bytes {@link org.postgis.binary.ByteGetter} to read.
     *
     * @return The {@link org.postgis.binary.ValueGetter} for the endian
     */
    public static ValueGetter valueGetterForEndian(ByteGetter bytes) {
        if (bytes.get(0) == 0) {
            return new ValueGetter.XDR(bytes);
        } else if (bytes.get(0) == 1) {
            return new ValueGetter.NDR(bytes);
        } else {
            throw new IllegalArgumentException("Unknown Endian type:" + bytes.get(0));
        }
    }

    /**
     * Parse the given {@link String} into a JTS {@link org.locationtech.jts.geom.Geometry}.
     *
     * @param value {@link String} to parse.
     *
     * @return Parsed JTS {@link org.locationtech.jts.geom.Geometry}.
     */
    public Geometry parse(String value) {
        ByteGetter.StringByteGetter bytes = new ByteGetter.StringByteGetter(value);
        return this.parseGeometry(valueGetterForEndian(bytes));
    }

    /**
     * Parse the given byte array into a JTS {@link org.locationtech.jts.geom.Geometry}.
     *
     * @param value byte array to parse.
     *
     * @return Parsed JTS {@link org.locationtech.jts.geom.Geometry}.
     */
    public Geometry parse(byte[] value) {
        ByteGetter.BinaryByteGetter bytes = new ByteGetter.BinaryByteGetter(value);
        return this.parseGeometry(valueGetterForEndian(bytes));
    }

    /**
     * Parse data from the given {@link org.postgis.binary.ValueGetter} into a JTS
     * {@link org.locationtech.jts.geom.Geometry}.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     *
     * @return Parsed JTS {@link org.locationtech.jts.geom.Geometry}.
     */
    protected Geometry parseGeometry(ValueGetter data) {
        return this.parseGeometry(data, 0, false);
    }


    /**
     * Parse data from the given {@link org.postgis.binary.ValueGetter} into a JTS
     * {@link org.locationtech.jts.geom.Geometry} with the given SRID.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param srid SRID to give to the parsed geometry (different of the inherited SRID).
     * @param inheritSrid Make the new {@link org.locationtech.jts.geom.Geometry} inherit its SRID if set to true,
     *                    otherwise use the parameter given SRID.
     *
     * @return Parsed JTS {@link org.locationtech.jts.geom.Geometry} with SRID.
     */
    protected Geometry parseGeometry(ValueGetter data, int srid, boolean inheritSrid) {
        byte endian = data.getByte();
        if (endian != data.endian) {
            throw new IllegalArgumentException("Endian inconsistency!");
        } else {
            int typeword = data.getInt();
            int realtype = typeword & 536870911;
            boolean haveZ = (typeword & -2147483648) != 0;
            boolean haveM = (typeword & 1073741824) != 0;
            boolean haveS = (typeword & 536870912) != 0;
            if (haveS) {
                int newsrid = org.postgis.Geometry.parseSRID(data.getInt());
                if (inheritSrid && newsrid != srid) {
                    throw new IllegalArgumentException("Inconsistent srids in complex geometry: " + srid + ", " + newsrid);
                }

                srid = newsrid;
            } else if (!inheritSrid) {
                srid = 0;
            }

            Geometry result;
            switch(realtype) {
                case 1:
                    result = this.parsePoint(data, haveZ, haveM);
                    break;
                case 2:
                    result = this.parseLineString(data, haveZ, haveM);
                    break;
                case 3:
                    result = this.parsePolygon(data, haveZ, haveM, srid);
                    break;
                case 4:
                    result = this.parseMultiPoint(data, srid);
                    break;
                case 5:
                    result = this.parseMultiLineString(data, srid);
                    break;
                case 6:
                    result = this.parseMultiPolygon(data, srid);
                    break;
                case 7:
                    result = this.parseCollection(data, srid);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Geometry Type!");
            }

            result.setSRID(srid);
            return result;
        }
    }

    /**
     * Parse the given {@link org.postgis.binary.ValueGetter} into a JTS {@link org.locationtech.jts.geom.Point}.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param haveZ True if the {@link org.locationtech.jts.geom.Point} has a Z component.
     * @param haveM True if the {@link org.locationtech.jts.geom.Point} has a M component.
     *
     * @return The parsed {@link org.locationtech.jts.geom.Point}.
     */
    private Point parsePoint(ValueGetter data, boolean haveZ, boolean haveM) {
        double X = data.getDouble();
        double Y = data.getDouble();
        Point result;
        if (haveZ) {
            double Z = data.getDouble();
            result = JtsGeometry.geofac.createPoint(new Coordinate(X, Y, Z));
        } else {
            result = JtsGeometry.geofac.createPoint(new Coordinate(X, Y));
        }

        if (haveM) {
            data.getDouble();
        }

        return result;
    }

    /**
     * Parse the given {@link org.postgis.binary.ValueGetter} into an array of JTS
     * {@link org.locationtech.jts.geom.Geometry} with the given SRID.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param container Array of {@link org.locationtech.jts.geom.Geometry} which will contains the parsed ones.
     * @param srid SRID of the parsed geometries.
     */
    private void parseGeometryArray(ValueGetter data, Geometry[] container, int srid) {
        for(int i = 0; i < container.length; ++i) {
            container[i] = this.parseGeometry(data, srid, true);
        }

    }

    /**
     * Parse the given {@link org.postgis.binary.ValueGetter} into a JTS
     * {@link org.locationtech.jts.geom.CoordinateSequence}.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param haveZ True if the {@link org.locationtech.jts.geom.CoordinateSequence} has a Z component.
     * @param haveM True if the {@link org.locationtech.jts.geom.CoordinateSequence} has a M component.
     *
     * @return The parsed {@link org.locationtech.jts.geom.CoordinateSequence}.
     */
    private CoordinateSequence parseCS(ValueGetter data, boolean haveZ, boolean haveM) {
        int count = data.getInt();
        int dims = haveZ ? 3 : 2;
        CoordinateSequence cs = new Double(count, dims);

        for(int i = 0; i < count; ++i) {
            for(int d = 0; d < dims; ++d) {
                cs.setOrdinate(i, d, data.getDouble());
            }

            if (haveM) {
                data.getDouble();
            }
        }

        return cs;
    }

    /**
     * Parse the given {@link org.postgis.binary.ValueGetter} into a JTS
     * {@link org.locationtech.jts.geom.MultiPoint}.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param srid SRID of the parsed geometries.
     *
     * @return The parsed {@link org.locationtech.jts.geom.MultiPoint}.
     */
    private MultiPoint parseMultiPoint(ValueGetter data, int srid) {
        Point[] points = new Point[data.getInt()];
        this.parseGeometryArray(data, points, srid);
        return JtsGeometry.geofac.createMultiPoint(points);
    }

    /**
     * Parse the given {@link org.postgis.binary.ValueGetter} into a JTS
     * {@link org.locationtech.jts.geom.LineString}.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param haveZ True if the {@link org.locationtech.jts.geom.LineString} has a Z component.
     * @param haveM True if the {@link org.locationtech.jts.geom.LineString} has a M component.
     *
     * @return The parsed {@link org.locationtech.jts.geom.LineString}.
     */
    private LineString parseLineString(ValueGetter data, boolean haveZ, boolean haveM) {
        return JtsGeometry.geofac.createLineString(this.parseCS(data, haveZ, haveM));
    }

    /**
     * Parse the given {@link org.postgis.binary.ValueGetter} into a JTS
     * {@link org.locationtech.jts.geom.LinearRing}.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param haveZ True if the {@link org.locationtech.jts.geom.LinearRing} has a Z component.
     * @param haveM True if the {@link org.locationtech.jts.geom.LinearRing} has a M component.
     *
     * @return The parsed {@link org.locationtech.jts.geom.LinearRing}.
     */
    private LinearRing parseLinearRing(ValueGetter data, boolean haveZ, boolean haveM) {
        return JtsGeometry.geofac.createLinearRing(this.parseCS(data, haveZ, haveM));
    }


    /**
     * Parse the given {@link org.postgis.binary.ValueGetter} into a JTS
     * {@link org.locationtech.jts.geom.Polygon}.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param haveZ True if the {@link org.locationtech.jts.geom.Polygon} has a Z component.
     * @param haveM True if the {@link org.locationtech.jts.geom.Polygon} has a M component.
     *
     * @return The parsed {@link org.locationtech.jts.geom.Polygon}.
     */
    private Polygon parsePolygon(ValueGetter data, boolean haveZ, boolean haveM, int srid) {
        int holecount = data.getInt() - 1;
        LinearRing[] rings = new LinearRing[holecount];
        LinearRing shell = this.parseLinearRing(data, haveZ, haveM);
        shell.setSRID(srid);

        for(int i = 0; i < holecount; ++i) {
            rings[i] = this.parseLinearRing(data, haveZ, haveM);
            rings[i].setSRID(srid);
        }

        return JtsGeometry.geofac.createPolygon(shell, rings);
    }

    /**
     * Parse the given {@link org.postgis.binary.ValueGetter} into a JTS
     * {@link org.locationtech.jts.geom.MultiLineString}.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param srid SRID of the parsed geometries.
     *
     * @return The parsed {@link org.locationtech.jts.geom.MultiLineString}.
     */
    private MultiLineString parseMultiLineString(ValueGetter data, int srid) {
        int count = data.getInt();
        LineString[] strings = new LineString[count];
        this.parseGeometryArray(data, strings, srid);
        return JtsGeometry.geofac.createMultiLineString(strings);
    }

    /**
     * Parse the given {@link org.postgis.binary.ValueGetter} into a JTS
     * {@link org.locationtech.jts.geom.MultiPolygon}.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param srid SRID of the parsed geometries.
     *
     * @return The parsed {@link org.locationtech.jts.geom.MultiPolygon}.
     */
    private MultiPolygon parseMultiPolygon(ValueGetter data, int srid) {
        int count = data.getInt();
        Polygon[] polys = new Polygon[count];
        this.parseGeometryArray(data, polys, srid);
        return JtsGeometry.geofac.createMultiPolygon(polys);
    }

    /**
     * Parse the given {@link org.postgis.binary.ValueGetter} into a JTS
     * {@link org.locationtech.jts.geom.GeometryCollection}.
     *
     * @param data {@link org.postgis.binary.ValueGetter} to parse.
     * @param srid SRID of the parsed geometries.
     *
     * @return The parsed {@link org.locationtech.jts.geom.GeometryCollection}.
     */
    private GeometryCollection parseCollection(ValueGetter data, int srid) {
        int count = data.getInt();
        Geometry[] geoms = new Geometry[count];
        this.parseGeometryArray(data, geoms, srid);
        return JtsGeometry.geofac.createGeometryCollection(geoms);
    }
}

