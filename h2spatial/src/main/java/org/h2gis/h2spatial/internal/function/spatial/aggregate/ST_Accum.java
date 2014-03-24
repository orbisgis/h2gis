/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
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
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.h2spatial.internal.function.spatial.aggregate;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.h2.api.Aggregate;
import org.h2.value.Value;
import org.h2gis.h2spatialapi.AbstractFunction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Construct an array of Geometry.
 * @author Nicolas Fortin
 */
public class ST_Accum extends AbstractFunction implements Aggregate {
    private List<Geometry> toUnite = new LinkedList<Geometry>();

    public ST_Accum() {
        addProperty(PROP_REMARKS, "This aggregate function returns a GeometryCollection.");
    }

    @Override
    public void init(Connection connection) throws SQLException {
    }

    @Override
    public int getInternalType(int[] inputTypes) throws SQLException {
        if(inputTypes.length!=1) {
            throw new SQLException(ST_Accum.class.getSimpleName()+" expect 1 argument.");
        }
        if(inputTypes[0]!= Value.GEOMETRY) {
            throw new SQLException(ST_Accum.class.getSimpleName()+" expect a geometry argument");
        }
        return Value.GEOMETRY;
    }

    private void addGeometry(Geometry geom) {
        if (geom.getGeometryType().equals("GeometryCollection")) {
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                addGeometry(geom.getGeometryN(i));
            }
        } else {
            toUnite.add(geom);
        }
    }

    @Override
    public void add(Object o) throws SQLException {
        if(o instanceof Geometry) {
            Geometry geom = (Geometry)o;
            addGeometry(geom);
        } else {
            throw new SQLException();
        }
    }

    @Override
    public GeometryCollection getResult() throws SQLException {
        GeometryFactory factory = new GeometryFactory();
        return factory.createGeometryCollection(toUnite.toArray(new Geometry[toUnite.size()]));
    }
}
