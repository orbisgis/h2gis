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

package org.h2gis.functions.spatial.properties;

import org.h2.api.Aggregate;
import org.h2.value.Value;
import org.h2gis.api.AbstractFunction;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ST_Extent returns an {@link org.locationtech.jts.geom.Envelope} that cover all aggregated geometries.
 * @author Nicolas Fortin
 */
public class ST_Extent extends AbstractFunction implements Aggregate {
    private Envelope aggregatedEnvelope = new Envelope();
    private int srid = 0;

    public ST_Extent() {
        addProperty(PROP_REMARKS, "Return an envelope of the aggregation of all geometries in the table.");
    }

    @Override
    public void init(Connection connection) throws SQLException {
        aggregatedEnvelope = new Envelope();
    }

    @Override
    public int getInternalType(int[] inputTypes) throws SQLException {
        if(inputTypes.length!=1) {
            throw new SQLException(ST_Extent.class.getSimpleName()+" expect 1 argument.");
        }
        if(inputTypes[0]!=Value.GEOMETRY) {
            throw new SQLException(ST_Extent.class.getSimpleName()+" expect a geometry argument");
        }
        return Value.GEOMETRY;
    }

    @Override
    public void add(Object o) throws SQLException {
        if (o instanceof Geometry) {
            Geometry geom = (Geometry) o;
            if(!geom.isEmpty()) {
                int currentSRID = geom.getSRID();
                if (srid == 0) {
                    srid = currentSRID;
                } else if (srid != currentSRID) {
                    throw new SQLException("Operation on mixed SRID geometries not supported");
                }
                aggregatedEnvelope.expandToInclude(geom.getEnvelopeInternal());
            }
        }
    }

    @Override
    public Geometry getResult() throws SQLException {
        if(aggregatedEnvelope.isNull()) {
            return null;
        } else {
            Geometry geom = new GeometryFactory().toGeometry(aggregatedEnvelope);
            geom.setSRID(srid);
            return geom;
        }
    }
}
