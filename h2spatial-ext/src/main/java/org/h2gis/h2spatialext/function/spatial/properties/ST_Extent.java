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
package org.h2gis.h2spatialext.function.spatial.properties;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.h2.api.Aggregate;
import org.h2.value.Value;
import org.h2gis.h2spatialapi.AbstractFunction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ST_Extent returns an {@link com.vividsolutions.jts.geom.Envelope} that cover all aggregated geometries.
 * @author Nicolas Fortin
 */
public class ST_Extent extends AbstractFunction implements Aggregate {
    private Envelope aggregatedEnvelope = new Envelope();

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
            aggregatedEnvelope.expandToInclude(geom.getEnvelopeInternal());
        }
    }

    @Override
    public Geometry getResult() throws SQLException {
        if(aggregatedEnvelope.isNull()) {
            return null;
        } else {
            return new GeometryFactory().toGeometry(aggregatedEnvelope);
        }
    }
}
