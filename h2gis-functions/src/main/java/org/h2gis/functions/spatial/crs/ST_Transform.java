/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.crs;

import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationException;
import org.cts.op.CoordinateOperationFactory;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class is used to transform a geometry from one CRS to another.
 * Only integer codes available in the spatial_ref_sys table are allowed.
 * The default source CRS is the input geometry's internal CRS.
 *
 * <p>Coordinate operations are resolved once per (source SRID, target SRID)
 * pair and then kept in a thread-safe cache, so the per-row cost of the
 * function is limited to the coordinate transformation itself.</p>
 *
 * @author Erwan Bocher, CNRS
 */
public class ST_Transform extends AbstractFunction implements ScalarFunction {

    /**
     * Guards {@link #crsf} and {@link #srr}: the registry holds the connection
     * used to read SPATIAL_REF_SYS and must never be shared by two sessions
     * at the same time.
     */
    private static final Object CRS_LOCK = new Object();
    private static CRSFactory crsf;
    private static final SpatialRefRegistry srr = new SpatialRefRegistry();

    /**
     * Maximum number of (source, target) pairs kept in the cache.
     */
    private static final int CACHE_LIMIT = 64;

    /**
     * Cache of coordinate operations. An empty Optional means that both
     * SRIDs describe the same CRS: only the SRID has to be changed.
     */
    private static final Map<EPSGTuple, Optional<CoordinateOperation>> copPool = new ConcurrentHashMap<>();

    public ST_Transform() {
        addProperty(PROP_REMARKS, "Transform a geometry from one CRS to another " +
                "using integer codes from the SPATIAL_REF_SYS table.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "ST_Transform";
    }

    /**
     * Returns a new geometry transformed to the SRID referenced by the integer
     * parameter available in the spatial_ref_sys table
     * @param connection database
     * @param geom Geometry
     * @param codeEpsg srid code
     * @return reprojected geometry
     */
    public static Geometry ST_Transform(Connection connection, Geometry geom, Integer codeEpsg) throws SQLException, CoordinateOperationException {
        if (geom == null) {
            return null;
        }
        if (codeEpsg == null) {
            throw new IllegalArgumentException("The SRID code cannot be null.");
        }
        int inputSRID = geom.getSRID();
        if (inputSRID == 0) {
            throw new SQLException("Cannot find a CRS");
        }
        // Fast path 1: nothing to do
        if (inputSRID == codeEpsg) {
            return geom;
        }
        // Fast path 2: the operation is already known, no CRS lookup needed
        EPSGTuple key = new EPSGTuple(inputSRID, codeEpsg);
        Optional<CoordinateOperation> op = copPool.get(key);
        if (op == null) {
            op = findCoordinateOperation(connection, inputSRID, codeEpsg);
            if (copPool.size() >= CACHE_LIMIT) {
                copPool.clear();
            }
            copPool.put(key, op);
        }
        Geometry outPutGeom = geom.copy();
        if (op.isPresent()) {
            CRSTransformFilter filter = new CRSTransformFilter(op.get());
            outPutGeom.apply(filter);
            outPutGeom.geometryChanged();
            if (filter.getError() != null) {
                throw new SQLException("Cannot transform the geometry from SRID " + inputSRID
                        + " to SRID " + codeEpsg, filter.getError());
            }
        }
        outPutGeom.setSRID(codeEpsg);
        return outPutGeom;
    }

    /**
     * Resolve the coordinate operation between two SRIDs. This is the slow
     * path: it reads SPATIAL_REF_SYS and builds the CRS, so it is executed
     * once per pair of SRIDs.
     *
     * @return the operation, or an empty Optional if both SRIDs describe the same CRS
     */
    private static Optional<CoordinateOperation> findCoordinateOperation(Connection connection,
                                                                         int inputSRID, int targetSRID) throws SQLException {
        synchronized (CRS_LOCK) {
            if (crsf == null) {
                crsf = new CRSFactory();
                //Activate the CRSFactory and the internal H2 spatial_ref_sys registry to
                // manage Coordinate Reference Systems.
                crsf.getRegistryManager().addRegistry(srr);
            }
            srr.setConnection(connection);
            try {
                CoordinateReferenceSystem inputCRS = crsf.getCRS(srr.getRegistryName() + ":" + inputSRID);
                CoordinateReferenceSystem targetCRS = crsf.getCRS(srr.getRegistryName() + ":" + targetSRID);
                if (inputCRS.equals(targetCRS)) {
                    return Optional.empty();
                }
                if (inputCRS instanceof GeodeticCRS && targetCRS instanceof GeodeticCRS) {
                    Set<CoordinateOperation> ops = CoordinateOperationFactory
                            .createCoordinateOperations((GeodeticCRS) inputCRS, (GeodeticCRS) targetCRS);
                    if (ops.isEmpty()) {
                        throw new SQLException("No coordinate operation found from SRID "
                                + inputSRID + " to SRID " + targetSRID);
                    }
                    return Optional.of(CoordinateOperationFactory.getMostPrecise(ops));
                }
                throw new SQLException("The transformation from "
                        + inputCRS + " to " + targetSRID + " is not yet supported.");
            } catch (CRSException | CoordinateOperationException ex) {
                throw new SQLException("Cannot create the CRS", ex);
            } finally {
                srr.setConnection(null);
            }
        }
    }

    /**
     * Remove all the cached coordinate operations. To be called when the
     * content of SPATIAL_REF_SYS is modified.
     */
    public static void clearCache() {
        copPool.clear();
    }

    /**
     * This method is used to apply a {@link CoordinateOperation} to a geometry.
     * The transformation loops on each coordinate.
     * The first error met is kept and can be read with {@link #getError()}.
     */
    public static class CRSTransformFilter implements CoordinateFilter{
        private final CoordinateOperation coordinateOperation;
        private Exception error;

        /**
         * @param coordinateOperation CoordinateOperation
         */
        public CRSTransformFilter(final CoordinateOperation coordinateOperation){
            this.coordinateOperation=coordinateOperation;
        }

        @Override
        public void filter(Coordinate coord) {
            if (error != null) {
                return;
            }
            try {
                if (Double.isNaN(coord.z)) {
                    coord.z = 0;
                }
                double[] xyz = coordinateOperation
                        .transform(new double[]{coord.x, coord.y, coord.z});
                coord.x = xyz[0];
                coord.y = xyz[1];
                if (xyz.length > 2) {
                    coord.z = xyz[2];
                } else {
                    coord.z = Double.NaN;
                }
            } catch (CoordinateOperationException |IllegalCoordinateException ex) {
                error = ex;
            }
        }

        /**
         * @return the first error met during the transformation, null if none
         */
        public Exception getError() {
            return error;
        }
    }

    /**
     * A simple cache to manage {@link CoordinateOperation}
     */
    public static class CopCache extends LinkedHashMap<EPSGTuple, CoordinateOperation> {

        private final int limit;

        /**
         * @param limit size of the cache
         */
        public CopCache(int limit) {
            super(16, 0.75f, true);
            this.limit = limit;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<EPSGTuple, CoordinateOperation> eldest) {
            return size() > limit;
        }
    }
}