/**
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

package org.h2gis.functions.spatial.crs;

import org.locationtech.jts.geom.*;
import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationFactory;
import org.h2gis.api.AbstractFunction;
import org.h2gis.api.ScalarFunction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is used to transform a geometry from one CRS to another. 
 * Only integer codes available in the spatial_ref_sys table are allowed.
 * The default source CRS is the input geometry's internal CRS.
 *
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class ST_Transform extends AbstractFunction implements ScalarFunction {

    private static CRSFactory crsf;
    private static SpatialRefRegistry srr = new SpatialRefRegistry();
    private static Map<EPSGTuple, CoordinateOperation> copPool = new CopCache(5);

    /**
     * Constructor
     */
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
     * @param connection
     * @param geom
     * @param codeEpsg
     * @return
     * @throws SQLException 
     */
    public static Geometry ST_Transform(Connection connection, Geometry geom, Integer codeEpsg) throws SQLException {
        if (geom == null) {
            return null;
        }
        if (codeEpsg == null) {
            throw new IllegalArgumentException("The SRID code cannot be null.");
        }
        if (crsf == null) {
            crsf = new CRSFactory();
            //Activate the CRSFactory and the internal H2 spatial_ref_sys registry to
            // manage Coordinate Reference Systems.
            crsf.getRegistryManager().addRegistry(srr);
        }
        srr.setConnection(connection);
        try {
            int inputSRID = geom.getSRID();
            if (inputSRID == 0) {
                throw new SQLException("Cannot find a CRS");
            } else {
                CoordinateReferenceSystem inputCRS = crsf.getCRS(srr.getRegistryName() + ":" + String.valueOf(inputSRID));
                CoordinateReferenceSystem targetCRS = crsf.getCRS(srr.getRegistryName() + ":" + String.valueOf(codeEpsg));
                if (inputCRS.equals(targetCRS)) {
                    return geom;
                }
                EPSGTuple epsg = new EPSGTuple(inputSRID, codeEpsg);
                CoordinateOperation op = copPool.get(epsg);
                if (op != null) {
                    Geometry outPutGeom = geom.copy();
                    outPutGeom.geometryChanged();
                    outPutGeom.apply(new CRSTransformFilter(op));
                    outPutGeom.setSRID(codeEpsg);
                    return outPutGeom;
                } else {
                    if (inputCRS instanceof GeodeticCRS && targetCRS instanceof GeodeticCRS) {
                        List<CoordinateOperation> ops = CoordinateOperationFactory
                                .createCoordinateOperations((GeodeticCRS) inputCRS, (GeodeticCRS) targetCRS);
                        if (!ops.isEmpty()) {
                            op = ops.get(0);
                            Geometry outPutGeom = (Geometry) geom.copy();
                            outPutGeom.geometryChanged();
                            outPutGeom.apply(new CRSTransformFilter(op));
                            copPool.put(epsg, op);
                            outPutGeom.setSRID(codeEpsg);
                            return outPutGeom;
                        }
                    } else {
                        throw new SQLException("The transformation from "
                                + inputCRS + " to " + codeEpsg + " is not yet supported.");
                    }
                }
            }
        } catch (CRSException ex) {
            throw new SQLException("Cannot create the CRS", ex);
        } finally {
            srr.setConnection(null);
        }
        return null;

    }

  
    /**
     * This method is used to apply a {@link CoordinateOperation} to a geometry.
     * The transformation loops on each coordinate. 
     */
    public static class CRSTransformFilter implements CoordinateFilter{
        private final CoordinateOperation coordinateOperation;

      
        public CRSTransformFilter(final CoordinateOperation coordinateOperation){
            this.coordinateOperation=coordinateOperation;            
        }
       
        @Override
        public void filter(Coordinate coord) {
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
            } catch (IllegalCoordinateException ice) {
                throw new RuntimeException("Cannot transform the coordinate" + coord.toString(), ice);
            }

        }
        
    
    }

    /**
     * A simple cache to manage {@link CoordinateOperation}
     */
    public static class CopCache extends LinkedHashMap<EPSGTuple, CoordinateOperation> {

        private final int limit;

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
