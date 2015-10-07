/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MetaData of raster field
 * @author Nicolas Fortin
 */
public class RasterMetaData {
    public static final int META_UPPER_LEFT_X = 0;
    public static final int META_UPPER_LEFT_Y = 1;
    public static final int META_WIDTH = 2;
    public static final int META_HEIGHT = 3;
    public static final int META_SCALE_X = 4;
    public static final int META_SCALE_Y = 5;
    public static final int META_SKEW_X = 6;
    public static final int META_SKEW_Y = 7;
    public static final int META_SRID = 8;
    public static final int META_NUM_BANDS = 9;
    private final double upperLeftX;
    private final double upperLeftY;
    private final int width;
    private final int height;
    private final double scaleX;
    private final double scaleY;
    private final double skewX;
    private final double skewY;
    private final int srid;
    private final int numBands;

    /**
     * Constructor of raster metadata
     * @param upperLeftX insertion point X
     * @param upperLeftY insertion point Y
     * @param width image width
     * @param height image height
     * @param scaleX pixel size x
     * @param scaleY pixel size y
     * @param skewX pixel displacement x
     * @param skewY pixel displacement y
     * @param srid projection identifier
     * @param numBands Number of bands
     */
    public RasterMetaData(double upperLeftX, double upperLeftY, int width, int height, double scaleX, double scaleY,
            double skewX, double skewY, int srid, int numBands) {
        this.upperLeftX = upperLeftX;
        this.upperLeftY = upperLeftY;
        this.width = width;
        this.height = height;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.skewX = skewX;
        this.skewY = skewY;
        this.srid = srid;
        this.numBands = numBands;
    }

    /**
     * Read metadata from the result of ST_METADATA function.
     * @param rs ResultSet
     * @param metaDataField MetaData field name
     * @return Instance of RasterMetadata or null if provided input is not correct
     * @throws SQLException
     */
    public static RasterMetaData fetchRasterMetaData(ResultSet rs, String metaDataField) throws SQLException {
         Object ar = rs.getObject(metaDataField);
         if(!(ar instanceof Object[]) || ((Object[]) ar).length != 10) {
             return null;
         }
         Object[] metaArray = (Object[])ar;
         return new RasterMetaData(
                 getDouble(metaArray[META_UPPER_LEFT_X]),
                 getDouble(metaArray[META_UPPER_LEFT_Y]),
                 getInt(metaArray[META_WIDTH]),
                 getInt(metaArray[META_HEIGHT]),
                 getDouble(metaArray[META_SCALE_X]),
                 getDouble(metaArray[META_SCALE_Y]),
                 getDouble(metaArray[META_SKEW_X]),
                 getDouble(metaArray[META_SKEW_Y]),
                 getInt(metaArray[META_SRID]),
                 getInt(metaArray[META_NUM_BANDS]));
    }

    public AffineTransform getTransform() {
        return new AffineTransform(scaleX, skewY, skewX, scaleY, upperLeftX, upperLeftY);
    }

    private static double getDouble(Object obj) {
        return ((Number) obj).doubleValue();
    }

    private static int getInt(Object obj) {
        return ((Number) obj).intValue();
    }

    /**
     * Compute row-column position from world coordinate
     * @param coordinate world coordinate.
     * @return raster row-column (0-based)
     */
    public int[] getPixelFromCoordinate(Coordinate coordinate) {
        try {
            AffineTransform  inv = getTransform().createInverse();
            Point2D pt = inv.transform(new Point2D.Double(coordinate.x, coordinate.y), null);
            return new int[]{(int)pt.getX(), (int)pt.getY()};
        } catch (NoninvertibleTransformException ex) {
            //todo
            return null;
        }
    }


    /**
     * Translate the pixel row,column into map coordinate
     *
     * @param x Column (0-based)
     * @param y Row (0-based)
     * @return Pixel world coordinate
     */
    public Coordinate getPixelCoordinate(int x, int y) {
        AffineTransform pixelTransform = getTransform();
        Point2D pt = pixelTransform.transform(new Point2D.Double(x, y), null);
        return new Coordinate(pt.getX(), pt.getY());
    }

    /**
     * @return The envelope of the raster, take account of the rotation
     * of the raster
     */
    public Polygon convexHull() {
        GeometryFactory geometryFactory =
                new GeometryFactory(new PrecisionModel(), srid);
        Coordinate bottomLeft = getPixelCoordinate(0, 0);
        Coordinate bottomRight = getPixelCoordinate(width, 0);
        Coordinate topRight = getPixelCoordinate(width, height);
        Coordinate topLeft = getPixelCoordinate(0, height);
        return geometryFactory.createPolygon(
                new Coordinate[]{bottomLeft, bottomRight, topRight, topLeft,
                        bottomLeft});
    }

    /**
     * @return The envelope of the raster. This envelope is larger than
     * the convex hull as
     */
    public Envelope getEnvelope() {
        Envelope env = new Envelope(getPixelCoordinate(0, 0));
        env.expandToInclude(getPixelCoordinate(width, 0));
        env.expandToInclude(getPixelCoordinate(width, height));
        env.expandToInclude(getPixelCoordinate(0, height));
        return env;
    }

    /**
     * @return Insertion X coordinate of the raster
     */
    public double getUpperLeftX() {
        return upperLeftX;
    }

    /**
     * @return Insertion Y coordinate of the raster
     */
    public double getUpperLeftY() {
        return upperLeftY;
    }

    /**
     * @return Pixels width of the raster
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return Pixels height of the raster
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return Scale X of the pixel
     */
    public double getScaleX() {
        return scaleX;
    }

    /**
     * @return Scale Y of the raster
     */
    public double getScaleY() {
        return scaleY;
    }

    /**
     * @return Rotation X of the raster
     */
    public double getSkewX() {
        return skewX;
    }

    /**
     * @return Rotation Y of the raster
     */
    public double getSkewY() {
        return skewY;
    }

    /**
     * @return SRID of the raster
     */
    public int getSrid() {
        return srid;
    }

    /**
     * @return Number of bands of the raster
     */
    public int getNumBands() {
        return numBands;
    }
}
