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
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.ProjectedCRS;
import org.cts.op.CoordinateOperation;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Method to check if the CRS of the geometry is projected
 * @author Erwan Bocher, CNRS
 */
public class ST_IsProjectedCRS extends DeterministicScalarFunction {

    private static CRSFactory crsf;
    private static SpatialRefRegistry srr = new SpatialRefRegistry();
    private static Map<EPSGTuple, CoordinateOperation> copPool = new ST_Transform.CopCache(5);

    public ST_IsProjectedCRS() {
        addProperty(PROP_REMARKS, "ST_IsProjectedCRS takes a geometry and \n"
                + "return true is the coordinate system is projected, otherwise false. False is the geometry is null \n" +
                "and or if its srid is equals to 0.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }

    /**
     * Return true if the CRS is a projected one
     *
     * @param connection database
     * @param geometry {@link Geometry}
     * @return true if the CRS is a projected one
     */
    public static Boolean execute(Connection connection, Geometry geometry) throws SQLException {
        if (geometry == null) {
            return false;
        }
        int inputSRID = geometry.getSRID();
        if (inputSRID == 0) {
            return false;
        } else {
            if (crsf == null) {
                crsf = new CRSFactory();
                //Activate the CRSFactory and the internal H2 spatial_ref_sys registry to
                // manage Coordinate Reference Systems.
                crsf.getRegistryManager().addRegistry(srr);
            }
            srr.setConnection(connection);
            try {
                CoordinateReferenceSystem inputCRS = crsf.getCRS(srr.getRegistryName() + ":" + inputSRID);
                if(inputCRS instanceof ProjectedCRS){
                    return true;
                }

            } catch (CRSException ex) {
                throw new SQLException("Cannot create the CRS", ex);
            } finally {
                srr.setConnection(null);
            }
        }
        return false;
    }
}
