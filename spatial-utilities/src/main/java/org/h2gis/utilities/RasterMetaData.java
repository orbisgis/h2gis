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
import com.vividsolutions.jts.geom.util.NoninvertibleTransformationException;

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

    /**
     * @return Matrix form of raster attributes
     */
    double[] getMatrix() {
        return new double[]{upperLeftX, scaleX, skewY, upperLeftY, skewY, scaleY};
    }

    private static double getDouble(Object obj) {
        return ((Number) obj).doubleValue();
    }

    private static int getInt(Object obj) {
        return ((Number) obj).intValue();
    }

    /**
     * Apply GeoTransform to x/y coordinate.
     * convert a (pixel,line) coordinate into a georeferenced (geo_x,geo_y) location.
     * Converted from GDAL sources (gdaltransformer.cpp::GDALApplyGeoTransform)
     * @param geoTransform Six coefficient GeoTransform to apply.
     * @param x pixel position.
     * @param y line position.
     * @return World coordinate
     */
    private static double[] applyGeoTransform( double[] geoTransform, double x, double y)
    {
        return new double[] {
                geoTransform[0] + x * geoTransform[1]
            + y  * geoTransform[2],
                geoTransform[3] + x * geoTransform[4]
            + y  * geoTransform[5]};
    }

    /**
     * 3x2 Matrix inversion. Converted from GDAL sources (gdaltransformer.cpp::GDALInvGeoTransform)
     * @param matrixIn Six coefficient GeoTransform to invert.
     * @return Inverted 3x2 matrix
     * @throws NoninvertibleTransformationException If the matrix is not invertible (det=0)
     */
    private static double[] invGeoTransform( double[] matrixIn ) throws NoninvertibleTransformationException
    {
        double[] matrixOut = new double[6];
        /* Special case - no rotation - to avoid computing determinate */
        /* and potential precision issues. */
        if( Double.compare(matrixIn[2], 0.0) == 0 && Double.compare(matrixIn[4],0.0) == 0 &&
                Double.compare(matrixIn[1], 0.0) == 0 && Double.compare(matrixIn[5], 0.0) == 0 )
        {
            matrixOut[0] = -matrixIn[0] / matrixIn[1];
            matrixOut[1] = 1.0 / matrixIn[1];
            matrixOut[2] = 0.0;
            matrixOut[3] = -matrixIn[3] / matrixIn[5];
            matrixOut[4] = 0.0;
            matrixOut[5] = 1.0 / matrixIn[5];
            return matrixOut;
        }

        /* we assume a 3rd row that is [1 0 0] */

        /* Compute determinate */

        double det = matrixIn[1] * matrixIn[5] - matrixIn[2] * matrixIn[4];

        if( Math.abs(det) <= Double.MIN_NORMAL ) {
            throw new NoninvertibleTransformationException("Transformation is non-invertible");
        }

        double inv_det = 1.0 / det;

        /* compute adjoint, and divide by determinate */

        matrixOut[1] =  matrixIn[5] * inv_det;
        matrixOut[4] = -matrixIn[4] * inv_det;

        matrixOut[2] = -matrixIn[2] * inv_det;
        matrixOut[5] =  matrixIn[1] * inv_det;

        matrixOut[0] = ( matrixIn[2] * matrixIn[3] - matrixIn[0] * matrixIn[5]) * inv_det;
        matrixOut[3] = (-matrixIn[1] * matrixIn[3] + matrixIn[0] * matrixIn[4]) * inv_det;

        return matrixOut;
    }

    /**
     * Compute row-column position from world coordinate
     * @param coordinate world coordinate.
     * @return raster row-column (0-based)
     */
    public int[] getPixelFromCoordinate(Coordinate coordinate) {
        try {
            double[] inv = invGeoTransform(getMatrix());
            double[] res = applyGeoTransform(inv, coordinate.x, coordinate.y);
            return new int[]{(int)Math.floor(res[0]), (int)Math.floor(res[1])};
        } catch (NoninvertibleTransformationException ex) {
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
        double[] res = applyGeoTransform(getMatrix(), x, y);
        return new Coordinate(res[0], res[1]);
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
