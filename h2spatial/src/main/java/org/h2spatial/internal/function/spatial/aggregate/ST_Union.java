/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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

package org.h2spatial.internal.function.spatial.aggregate;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;
import org.h2.api.AggregateFunction;
import org.h2spatialapi.Function;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

/**
 * Aggregate function
 *  Compute the union of current and all previous geometries
 * @author Nicolas Fortin
 */
public class ST_Union implements AggregateFunction, Function {
    private List<Geometry> toUnite = new LinkedList<Geometry>();
    private int srid=0;

    @Override
    public void init(Connection connection) throws SQLException {
        srid=0;
    }

    @Override
    public int getType(int[] inputTypes) throws SQLException {
        if(inputTypes.length!=1) {
            throw new SQLException(ST_Union.class.getSimpleName()+" expect 1 argument.");
        }
        if(inputTypes[0]!=Types.OTHER && inputTypes[0]!=Types.JAVA_OBJECT) {
            throw new SQLException(ST_Union.class.getSimpleName()+" expect a geometry argument");
        }
        return Types.OTHER;
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
            if(srid==0) {
                int geomSRID = geom.getSRID();
                if(geomSRID>=1) {
                    srid = geomSRID;
                }
            }
            addGeometry(geom);
        } else {
            throw new SQLException();
        }
    }

    @Override
    public Object getResult() throws SQLException {
        if(srid!=0) {
            PrecisionModel precisionModel = new PrecisionModel();
            GeometryFactory geometryFactory = new GeometryFactory(precisionModel,srid);
            return UnaryUnionOp.union(toUnite, geometryFactory);
        } else {
            return UnaryUnionOp.union(toUnite);
        }
    }

    @Override
    public Object getProperty(String propertyName) {
        return null;
    }
}
