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
package org.h2gis.h2spatialext.function.spatial.create;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Creates a rectangular POLYGON formed from the given x and y minima.  The user may specify an SRID; if no SRID is specified the unknown spatial reference system is assumed.
 *
 * @author Erwan Bocher
 */
public class ST_MakeEnvelope extends DeterministicScalarFunction {

    private static final GeometryFactory GF = new GeometryFactory();

    public ST_MakeEnvelope() {
        addProperty(PROP_REMARKS,
                "Creates a rectangular POLYGON formed from the given x and y minima.\n"
                + " The user may specify an SRID; if no SRID is specified the unknown\n"
                + " spatial reference system is assumed.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "makeEnvelope";
    }

    /**
     * Creates a rectangular Polygon formed from the minima and maxima by the
     * given shell.
     *
     * @param xmin X min
     * @param ymin Y min
     * @param xmax X max
     * @param ymax Y max
     * @return Envelope as a POLYGON
     */
    public static Polygon makeEnvelope(double xmin, double ymin, double xmax, double ymax) {
        Coordinate[] coordinates = new Coordinate[]{
            new Coordinate(xmin, ymin),
            new Coordinate(xmax, ymin),
            new Coordinate(xmax, ymax),
            new Coordinate(xmin, ymax),
            new Coordinate(xmin, ymin)
        };
        return GF.createPolygon(GF.createLinearRing(coordinates), null);
    }

    /**
     * Creates a rectangular Polygon formed from the minima and maxima by the
     * given shell.
     * The user can set a srid.
     * @param xmin X min
     * @param ymin Y min
     * @param xmax X max
     * @param ymax Y max
     * @param srid SRID
     * @return Envelope as a POLYGON
     */
    public static Polygon makeEnvelope(double xmin, double ymin, double xmax, double ymax, int srid) {
        Polygon geom = makeEnvelope(xmin, ymin, xmax, ymax);
        geom.setSRID(srid);
        return geom;
    }
}
