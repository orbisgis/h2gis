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
import org.postgis.binary.ByteSetter;
import org.postgis.binary.ValueSetter;

/**
 * Parser class able to convert a JTS {@link org.locationtech.jts.geom.Geometry} into binary or literal data.
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public class JtsBinaryWriter {
    /**
     * Default empty constructor.
     */
    public JtsBinaryWriter() {}

    public static ValueSetter valueSetterForEndian(ByteSetter bytes, byte endian) {
        if (endian == 0) {
            return new ValueSetter.XDR(bytes);
        } else if (endian == 1) {
            return new ValueSetter.NDR(bytes);
        } else {
            throw new IllegalArgumentException("Unknown Endian type:" + endian);
        }
    }

    public String writeHexed(Geometry geom, byte REP) {
        int length = this.estimateBytes(geom);
        ByteSetter.StringByteSetter bytes = new ByteSetter.StringByteSetter(length);
        this.writeGeometry(geom, valueSetterForEndian(bytes, REP));
        return bytes.result();
    }

    public String writeHexed(Geometry geom) {
        return this.writeHexed(geom, (byte)1);
    }

    public byte[] writeBinary(Geometry geom, byte REP) {
        int length = this.estimateBytes(geom);
        ByteSetter.BinaryByteSetter bytes = new ByteSetter.BinaryByteSetter(length);
        this.writeGeometry(geom, valueSetterForEndian(bytes, REP));
        return bytes.result();
    }

    public byte[] writeBinary(Geometry geom) {
        return this.writeBinary(geom, (byte)1);
    }

    protected void writeGeometry(Geometry geom, ValueSetter dest) {
        if (geom == null) {
            throw new NullPointerException();
        } else {
            int dimension;
            if (geom.isEmpty()) {
                dimension = 0;
            } else {
                dimension = getCoordDim(geom);
                if (dimension < 2 || dimension > 4) {
                    throw new IllegalArgumentException("Unsupported geometry dimensionality: " + dimension);
                }
            }

            dest.setByte(dest.endian);
            int plaintype = getWKBType(geom);
            int typeword = plaintype;
            if (dimension == 3 || dimension == 4) {
                typeword = plaintype | -2147483648;
            }

            if (dimension == 4) {
                typeword |= 1073741824;
            }

            boolean haveSrid = this.checkSrid(geom);
            if (haveSrid) {
                typeword |= 536870912;
            }

            dest.setInt(typeword);
            if (haveSrid) {
                dest.setInt(geom.getSRID());
            }

            switch(plaintype) {
                case 1:
                    this.writePoint((Point)geom, dest);
                    break;
                case 2:
                    this.writeLineString((LineString)geom, dest);
                    break;
                case 3:
                    this.writePolygon((Polygon)geom, dest);
                    break;
                case 4:
                    this.writeMultiPoint((MultiPoint)geom, dest);
                    break;
                case 5:
                    this.writeMultiLineString((MultiLineString)geom, dest);
                    break;
                case 6:
                    this.writeMultiPolygon((MultiPolygon)geom, dest);
                    break;
                case 7:
                    this.writeCollection((GeometryCollection)geom, dest);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Geometry Type: " + plaintype);
            }

        }
    }

    public static int getWKBType(Geometry geom) {
        if (geom.isEmpty()) {
            return 7;
        } else if (geom instanceof Point) {
            return 1;
        } else if (geom instanceof LineString) {
            return 2;
        } else if (geom instanceof Polygon) {
            return 3;
        } else if (geom instanceof MultiPoint) {
            return 4;
        } else if (geom instanceof MultiLineString) {
            return 5;
        } else if (geom instanceof MultiPolygon) {
            return 6;
        } else if (geom instanceof GeometryCollection) {
            return 7;
        } else {
            throw new IllegalArgumentException("Unknown Geometry Type: " + geom.getClass().getName());
        }
    }

    private void writePoint(Point geom, ValueSetter dest) {
        this.writeCoordinates(geom.getCoordinateSequence(), getCoordDim(geom), dest);
    }

    private void writeCoordinates(CoordinateSequence seq, int dims, ValueSetter dest) {
        for(int i = 0; i < seq.size(); ++i) {
            for(int d = 0; d < dims; ++d) {
                dest.setDouble(seq.getOrdinate(i, d));
            }
        }

    }

    private void writeMultiPoint(MultiPoint geom, ValueSetter dest) {
        dest.setInt(geom.getNumPoints());

        for(int i = 0; i < geom.getNumPoints(); ++i) {
            this.writeGeometry(geom.getGeometryN(i), dest);
        }

    }

    private void writeLineString(LineString geom, ValueSetter dest) {
        dest.setInt(geom.getNumPoints());
        this.writeCoordinates(geom.getCoordinateSequence(), getCoordDim(geom), dest);
    }

    private void writePolygon(Polygon geom, ValueSetter dest) {
        dest.setInt(geom.getNumInteriorRing() + 1);
        this.writeLineString(geom.getExteriorRing(), dest);

        for(int i = 0; i < geom.getNumInteriorRing(); ++i) {
            this.writeLineString(geom.getInteriorRingN(i), dest);
        }

    }

    private void writeMultiLineString(MultiLineString geom, ValueSetter dest) {
        this.writeGeometryArray(geom, dest);
    }

    private void writeMultiPolygon(MultiPolygon geom, ValueSetter dest) {
        this.writeGeometryArray(geom, dest);
    }

    private void writeCollection(GeometryCollection geom, ValueSetter dest) {
        this.writeGeometryArray(geom, dest);
    }

    private void writeGeometryArray(Geometry geom, ValueSetter dest) {
        dest.setInt(geom.getNumGeometries());

        for(int i = 0; i < geom.getNumGeometries(); ++i) {
            this.writeGeometry(geom.getGeometryN(i), dest);
        }

    }

    protected int estimateBytes(Geometry geom) {
        int result = 1;
        result += 4;
        if (this.checkSrid(geom)) {
            result += 4;
        }

        switch(getWKBType(geom)) {
            case 1:
                result += this.estimatePoint((Point)geom);
                break;
            case 2:
                result += this.estimateLineString((LineString)geom);
                break;
            case 3:
                result += this.estimatePolygon((Polygon)geom);
                break;
            case 4:
                result += this.estimateMultiPoint((MultiPoint)geom);
                break;
            case 5:
                result += this.estimateMultiLineString((MultiLineString)geom);
                break;
            case 6:
                result += this.estimateMultiPolygon((MultiPolygon)geom);
                break;
            case 7:
                result += this.estimateCollection((GeometryCollection)geom);
                break;
            default:
                throw new IllegalArgumentException("Unknown Geometry Type: " + getWKBType(geom));
        }

        return result;
    }

    private boolean checkSrid(Geometry geom) {
        int srid = geom.getSRID();
        return srid > 0;
    }

    private int estimatePoint(Point geom) {
        return 8 * getCoordDim(geom);
    }

    private int estimateGeometryArray(Geometry container) {
        int result = 0;

        for(int i = 0; i < container.getNumGeometries(); ++i) {
            result += this.estimateBytes(container.getGeometryN(i));
        }

        return result;
    }

    private int estimateMultiPoint(MultiPoint geom) {
        int result = 4;
        if (geom.getNumGeometries() > 0) {
            result += geom.getNumGeometries() * this.estimateBytes(geom.getGeometryN(0));
        }

        return result;
    }

    private int estimateLineString(LineString geom) {
        return geom != null && geom.getNumGeometries() != 0 ? 4 + 8 * getCoordSequenceDim(geom.getCoordinateSequence()) * geom.getCoordinateSequence().size() : 0;
    }

    private int estimatePolygon(Polygon geom) {
        int result = 4 + this.estimateLineString(geom.getExteriorRing());

        for(int i = 0; i < geom.getNumInteriorRing(); ++i) {
            result += this.estimateLineString(geom.getInteriorRingN(i));
        }

        return result;
    }

    private int estimateMultiLineString(MultiLineString geom) {
        return 4 + this.estimateGeometryArray(geom);
    }

    private int estimateMultiPolygon(MultiPolygon geom) {
        return 4 + this.estimateGeometryArray(geom);
    }

    private int estimateCollection(GeometryCollection geom) {
        return 4 + this.estimateGeometryArray(geom);
    }

    public static int getCoordDim(Geometry geom) {
        if (geom.isEmpty()) {
            return 0;
        } else if (geom instanceof Point) {
            return getCoordSequenceDim(((Point)geom).getCoordinateSequence());
        } else if (geom instanceof LineString) {
            return getCoordSequenceDim(((LineString)geom).getCoordinateSequence());
        } else {
            return geom instanceof Polygon ? getCoordSequenceDim(((Polygon)geom).getExteriorRing().getCoordinateSequence()) : getCoordDim(geom.getGeometryN(0));
        }
    }

    public static int getCoordSequenceDim(CoordinateSequence coords) {
        if (coords != null && coords.size() != 0) {
            int dimensions = coords.getDimension();
            if (dimensions == 3) {
                return Double.isNaN(coords.getOrdinate(0, 2)) ? 2 : 3;
            } else {
                return dimensions;
            }
        } else {
            return 0;
        }
    }
}

