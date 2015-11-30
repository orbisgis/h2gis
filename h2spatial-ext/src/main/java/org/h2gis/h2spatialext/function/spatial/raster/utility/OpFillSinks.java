/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 * <p/>
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 * <p/>
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatialext.function.spatial.raster.utility;

import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2gis.h2spatialapi.ProgressVisitor;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

/**
 * Memory FillSink operation done historically on OrbisGIS Grap module.
 * Method from Olivier Planchon & Frederic Darboux (2001)
 * TODO. Find alternative using iterative D8
 * TODO  as done in {@link org.h2gis.h2spatialext.function.spatial.raster.ST_D8FlowAccumulation}
 *
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class OpFillSinks {

    private double dEpsilon[] = new double[8];

    private int R, C;

    private int[] R0 = new int[8];

    private int[] C0 = new int[8];

    private int[] dR = new int[8];

    private int[] dC = new int[8];

    private int[] fR = new int[8];

    private int[] fC = new int[8];

    private int depth;

    private int ncols;

    private int nrows;

    private WritableRaster m_DEM;

    private WritableRaster m_Border;

    private WritableRaster m_PreprocessedDEM;

    private final float minSlope;

    private WritableRaster m_mAsk;

    private GeoRaster geoRaster;

    private final static int m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };

    private final static int m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

    private final static double INIT_ELEVATION = 50000D;

    public OpFillSinks(float minSlope) {
        this.minSlope = minSlope;
    }

    public GeoRaster execute(final GeoRaster geoRaster, ProgressVisitor pm)
            throws IOException {
        this.geoRaster = geoRaster;
        return processAlgorithm(geoRaster, minSlope, pm);
    }

    /**
     *
     * @param geoRaster
     *            the DEM to be processed.
     * @param pm Progress monitor
     * @param minSlope
     *            is a slope parameters used to fill the sink, to find an
     *            outlet. Method from Olivier Planchon & Frederic Darboux (2001)
     * @throws IOException
     */

    private GeoRaster processAlgorithm(final GeoRaster geoRaster,
            final double minSlope, ProgressVisitor pm)
            throws IOException {
        m_DEM = geoRaster.copyData(null);

        int i;
        double iValue;
        int x, y;
        int scan;
        int it;
        int ix, iy;
        boolean something_done = false;
        float z, z2, wz, wzn;

        double dMinSlope = Math.tan(Math.toRadians(minSlope));

        double cellSize = geoRaster.getMetaData().scaleX;
        nrows = geoRaster.getHeight();
        ncols = geoRaster.getWidth();
        depth = 0;

        for (i = 0; i < 8; i++) {
            dEpsilon[i] = dMinSlope * getDistToNeighborInDir(i, (float)cellSize);
        }

        R0[0] = 0;
        R0[1] = nrows - 1;
        R0[2] = 0;
        R0[3] = nrows - 1;
        R0[4] = 0;
        R0[5] = nrows - 1;
        R0[6] = 0;
        R0[7] = nrows - 1;
        C0[0] = 0;
        C0[1] = ncols - 1;
        C0[2] = ncols - 1;
        C0[3] = 0;
        C0[4] = ncols - 1;
        C0[5] = 0;
        C0[6] = 0;
        C0[7] = ncols - 1;
        dR[0] = 0;
        dR[1] = 0;
        dR[2] = 1;
        dR[3] = -1;
        dR[4] = 0;
        dR[5] = 0;
        dR[6] = 1;
        dR[7] = -1;
        dC[0] = 1;
        dC[1] = -1;
        dC[2] = 0;
        dC[3] = 0;
        dC[4] = -1;
        dC[5] = 1;
        dC[6] = 0;
        dC[7] = 0;
        fR[0] = 1;
        fR[1] = -1;
        fR[2] = -nrows + 1;
        fR[3] = nrows - 1;
        fR[4] = 1;
        fR[5] = -1;
        fR[6] = -nrows + 1;
        fR[7] = nrows - 1;
        fC[0] = -ncols + 1;
        fC[1] = ncols - 1;
        fC[2] = -1;
        fC[3] = 1;
        fC[4] = ncols - 1;
        fC[5] = -ncols + 1;
        fC[6] = 1;
        fC[7] = -1;

        initAltitude();

        for (x = 0; x < ncols; x++) {

            if (x / 100 == x / 100.0) {
                if (pm.isCanceled()) {
                    break;
                } else {
                    pm.setStep((100 * x / ncols));
                }
            }

            for (y = 0; y < nrows; y++) {

                iValue = m_Border.getSampleFloat(x, y, 0);

                if (iValue == 1) {
                    dryUpwardCell(x, y);
                }
            }

        }

        for (it = 0; it < 1000; it++) {
            for (scan = 0; scan < 8; scan++) {
                R = R0[scan];
                C = C0[scan];
                something_done = false;

                do {
                    z = m_DEM.getSampleFloat(C, R, 0);
                    wz = m_PreprocessedDEM.getSampleFloat(C, R, 0);
                    if (!Float.isNaN(z) && (wz > z)) {
                        for (i = 0; i < 8; i++) {
                            ix = C + m_iOffsetX[i];
                            iy = R + m_iOffsetY[i];
                            z2 = m_DEM.getSampleFloat(ix, iy, 0);
                            if (!Float.isNaN(z2)) {
                                wzn = m_PreprocessedDEM.getSampleFloat(ix, iy, 0)
                                        + (float) dEpsilon[i];
                                if (z >= wzn) {
                                    m_PreprocessedDEM
                                            .setSample(C, R, 0, z);
                                    something_done = true;
                                    dryUpwardCell(C, R);
                                    break;
                                }
                                if (wz > wzn) {
                                    m_PreprocessedDEM.setSample(C, R, 0, wzn);
                                    something_done = true;
                                }
                            }
                        }
                    }
                } while (nextCell(scan));

                if (!something_done) {
                    break;
                }
            }
            if (!something_done) {
                break;
            }
        }
        return GeoRasterRenderedImage.create(new BufferedImage(geoRaster.getColorModel(), m_PreprocessedDEM,
                    false, null), geoRaster.getMetaData());
    }

    private void initAltitude() throws IOException {
        int x, y;
        m_PreprocessedDEM = WritableRaster.createWritableRaster(geoRaster.getSampleModel(), null);
        m_Border = WritableRaster.createWritableRaster(geoRaster.getSampleModel(), null);
        for (x = 0; x < ncols; x++) {
            for (y = 0; y < nrows; y++) {
                float dValue = m_DEM.getSampleFloat(x, y, 0);
                if (x == 0 || x == ncols - 1 || y == 0 || y == nrows - 1) {
                    m_Border.setSample(x, y,0, 1);
                    m_PreprocessedDEM.setSample(x, y,0, m_DEM.getSampleFloat(
                            x, y, 0));
                } else {
                    m_Border.setSample(x, y, 0, geoRaster.getMetaData().bands[0].noDataValue);

                    if (dValue != geoRaster.getMetaData().bands[0].noDataValue) {
                        m_PreprocessedDEM.setSample(x, y, 0, INIT_ELEVATION);
                    } else {
                        m_PreprocessedDEM.setSample(x, y, 0, geoRaster.getMetaData().bands[0].noDataValue);
                    }
                }
            }
        }
    }

    private void dryUpwardCell(int x, int y) throws IOException {
        final int MAX_DEPTH = 32000;
        int ix, iy, i;
        float zn, zw;

        depth += 1;

        if (depth <= MAX_DEPTH) {
            for (i = 0; i < 8; i++) {
                ix = x + m_iOffsetX[i];
                iy = y + m_iOffsetY[i];
                if(ix > 0 && iy > 0 && ix < ncols && iy < nrows) {
                    zw = m_PreprocessedDEM.getSampleFloat(ix, iy, 0);

                    zn = m_DEM.getSampleFloat(ix, iy, 0);
                    if ((zn != geoRaster.getMetaData().bands[0].noDataValue) && zw == INIT_ELEVATION) {
                        zw = m_PreprocessedDEM.getSampleFloat(x, y, 0) + (float) dEpsilon[i];
                        if (zn >= zw) {
                            m_PreprocessedDEM.setSample(ix, iy, 0, zn);
                            dryUpwardCell(ix, iy);
                        }
                    }
                }
            }
        }
        depth -= 1;

    }

    private boolean nextCell(int i) {

        R = R + dR[i];
        C = C + dC[i];

        if (R < 0 || C < 0 || R >= nrows || C >= ncols) {
            R = R + fR[i];
            C = C + fC[i];

            if (R < 0 || C < 0 || R >= nrows || C >= ncols) {
                return false;
            }
        }

        return true;
    }

    /**
     * This method must be extracted into a global class. It is used to
     * calculate the distance for each pixels around a 3X3 matrix.
     *
     * @param iDir
     * @return
     */

    private static double getDistToNeighborInDir(int iDir, float cellSize) {
        final int m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
        final int m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };
        final double m_dDist[] = new double[8];

        for (int i = 0; i < 8; i++) {
            m_dDist[i] = Math.sqrt(m_iOffsetX[i] * cellSize * m_iOffsetX[i]
                    * cellSize + m_iOffsetY[i] * cellSize * m_iOffsetY[i]
                    * cellSize);
        }
        return m_dDist[iDir];
    }
}
