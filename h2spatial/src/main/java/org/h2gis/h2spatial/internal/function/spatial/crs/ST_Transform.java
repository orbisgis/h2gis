/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier
 * SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * or contact directly: info_at_ orbisgis.org
 */
package org.h2gis.h2spatial.internal.function.spatial.crs;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationFactory;
import org.h2gis.h2spatialapi.AbstractFunction;
import org.h2gis.h2spatialapi.ScalarFunction;


/**
 * This class is used to transform a geometry from one CRS to another. 
 * Only integer codes available in the spatial_ref_sys table are allowed.
 * The default source CRS is the input geometry's internal CRS.
 * 
 * @author Erwan Bocher
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
    public static Geometry ST_Transform(Connection connection, Geometry geom, int codeEpsg) throws SQLException {
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
                EPSGTuple epsg = new EPSGTuple(inputSRID, codeEpsg);
                CoordinateOperation op = copPool.get(epsg);
                if (op != null) {
                    Geometry g = getGeometryTransformer(op).transform(geom);
                    g.setSRID(codeEpsg);
                    return g;
                } else {
                    if(inputCRS instanceof GeodeticCRS && targetCRS instanceof GeodeticCRS){
                    List<CoordinateOperation> ops = CoordinateOperationFactory.createCoordinateOperations((GeodeticCRS) inputCRS, (GeodeticCRS) targetCRS);
                    if (!ops.isEmpty()) {
                        op = ops.get(0);
                        Geometry g = getGeometryTransformer(op).transform(geom);
                        g.setSRID(codeEpsg);
                        copPool.put(epsg, op);
                        return g;
                    }
                    }
                    else{
                        throw new SQLException("This transformation from : "+ inputCRS + " to "+ codeEpsg+ " is not yet supported.");
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
     *
     * @param coordinateOperation The CoordinateOperation to apply
     * @return {@link GeometryTransformer}
     * @throws SQLException
     */
    public static GeometryTransformer getGeometryTransformer(final CoordinateOperation coordinateOperation) throws SQLException {
        GeometryTransformer gt = new GeometryTransformer() {
                @Override
                protected CoordinateSequence transformCoordinates(
                        CoordinateSequence cs, Geometry geom) {
                    Coordinate[] cc = geom.getCoordinates();
                    CoordinateSequence newcs = new CoordinateArraySequence(cc);
                    for (int i = 0; i < cc.length; i++) {
                        Coordinate c = cc[i];
                        try {
                            if (Double.isNaN(c.z)) {
                                c.z = 0;
                            }
                            double[] xyz = coordinateOperation
                                    .transform(new double[]{c.x, c.y, c.z});
                            newcs.setOrdinate(i, 0, xyz[0]);
                            newcs.setOrdinate(i, 1, xyz[1]);
                            if (xyz.length > 2) {
                                newcs.setOrdinate(i, 2, xyz[2]);
                            } else {
                                newcs.setOrdinate(i, 2, Double.NaN);
                            }
                        } catch (IllegalCoordinateException ice) {
                            throw new RuntimeException("Cannot transform the coordinate" + c.toString(), ice);
                        }
                    }
                    return newcs;
                }
            };        
        return gt;

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
