/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.others;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.functions.spatial.convert.ST_Holes;
import org.h2gis.functions.spatial.convert.ST_ToMultiLine;
import org.h2gis.functions.spatial.edit.ST_CollectionExtract;
import org.h2gis.functions.spatial.topology.ST_Polygonize;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Function to clip a [Multi]Polygon or [Multi]LineString geometry with another [Multi]Polygon or [Multi]LineString geometry.
 * @author Erwan Bocher, CNRS, 2023
 */
public class ST_Clip extends DeterministicScalarFunction {


    public ST_Clip() {
        addProperty(PROP_REMARKS, "Clip a [Multi]Polygon or [Multi]LineString with [Multi]Polygon or [Multi]LineString geometry.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "execute";
    }


    /**
     *
     * @param geomToClip
     * @param geomForClip
     * @return
     * @throws SQLException
     */
    public static Geometry execute(Geometry geomToClip, Geometry geomForClip) throws SQLException, ParseException {
        if (geomToClip == null) {
            return null;
        }
        if (geomToClip.isEmpty()) {
            return geomToClip;
        }

        if (geomForClip == null || geomForClip.isEmpty()) {
            return geomToClip;
        }

        if (geomToClip.getSRID() != geomForClip.getSRID()) {
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }

        if (geomToClip instanceof Polygon || geomToClip instanceof MultiPolygon) {
            Geometry geomForClipReduced = ST_CollectionExtract.execute(geomForClip, 2, 3);
            if (geomForClipReduced.isEmpty()) {
                throw new SQLException("Only support [Multi]Polygon or [Multi]LineString as input geometry for clipping");
            }
            if (geomToClip.intersects(geomForClipReduced)) {
                GeometryFactory factory = geomToClip.getFactory();
                Geometry geomNoded = factory.createGeometryCollection(new Geometry[]{ST_ToMultiLine.execute(geomToClip), ST_ToMultiLine.execute(geomForClip)}).union();
                Geometry pols = ST_Polygonize.execute(geomNoded);
                GeometryCollection holesToClip = ST_Holes.execute(geomToClip);
                GeometryCollection holesForClip = ST_Holes.execute(geomForClipReduced);
                Geometry holes = OverlayNGRobust.overlay(holesToClip, holesForClip, OverlayNG.UNION);
                List selected = new ArrayList();
                int nb = pols.getNumGeometries();
                PreparedGeometry pg_holes = new PreparedGeometryFactory().create(holes);
                PreparedGeometry pg_geomToClip = new PreparedGeometryFactory().create(geomToClip);
                for (int i = 0; i < nb; i++) {
                    Geometry g = pols.getGeometryN(i);
                    Point pt = g.getInteriorPoint();
                    if (!pg_holes.intersects(pt) && pg_geomToClip.intersects(pt)) {
                        selected.add(g);
                    }
                }
                Geometry geom = factory.buildGeometry(selected);
                geom.setSRID(geomToClip.getSRID());
                return geom;
            }

        } else if (geomToClip instanceof LineString || geomToClip instanceof MultiLineString) {
            Geometry geomForClipReduced = ST_CollectionExtract.execute(geomForClip, 2, 3);
            if (geomForClipReduced.isEmpty()) {
                throw new SQLException("Only support [Multi]Polygon or [Multi]LineString as input geometry for clipping");
            }
            if (geomToClip.intersects(geomForClipReduced)) {
                MultiLineString linesToClip = ST_ToMultiLine.execute(geomToClip);
                Geometry lines_unions = OverlayNGRobust.overlay(linesToClip, ST_ToMultiLine.execute(geomForClipReduced), OverlayNG.UNION);

                return OverlayOp.overlayOp(lines_unions, linesToClip, OverlayOp.INTERSECTION);

                /*List selected = new ArrayList();
                int nb =lines_unions.getNumGeometries();
                PreparedGeometry pg = new PreparedGeometryFactory().create(geomToClip);
                Geometry line = new WKTReader().read("LINESTRING (109.87219512195122 267.4, 109.34146341463415 264)");
                for (int i = 0; i < nb; i++ ) {
                    Geometry g = lines_unions.getGeometryN(i);

                    if(g.equals(line)){
                        Point pt = g.getInteriorPoint();
                    }
                    if(g.getDimension()>0) {
                        Point pt = g.getInteriorPoint();
                        if (pg.contains(g)) {
                            selected.add(g);
                        }
                    }
                }
                Geometry geom = geomToClip.getFactory().buildGeometry(selected);
                geom.setSRID(geomToClip.getSRID());
                return geom;*/
            }
        } else {
            throw new SQLException("Only support [Multi]Polygon or [Multi]LineString as input geometry to clip");
        }
        return geomToClip;
    }
}
