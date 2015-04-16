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

package org.h2gis.h2spatialext.function.spatial.mesh;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.h2gis.h2spatial.internal.function.spatial.properties.ST_CoordDim;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.CoordinateSequenceDimensionFilter;
import org.h2gis.utilities.jts_utils.CoordinateUtils;
import org.h2gis.utilities.jts_utils.tesselate.EarClipper;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.geometry.primitives.Point;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.TriangulationProcess;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.delaunay.sweep.DTSweep;
import org.poly2tri.triangulation.delaunay.sweep.DTSweepContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Tessellate a set of Polygon with adaptive triangles.
 * @author Nicolas Fortin
 */
public class ST_Tessellate extends DeterministicScalarFunction {

    public ST_Tessellate() {
        addProperty(PROP_REMARKS, "Return the tessellation of a (multi)polygon surface with adaptive triangles\n" +
                "Ex:\n" +
                "```sql\n" +
                "SELECT ST_TESSELLATE('POLYGON ((-6 -2, -8 2, 0 8, -8 -7, -10 -1, -6 -2))') the_geom" +
                "```");
    }

    @Override
    public String getJavaStaticMethod() {
        return "tessellate";
    }

    private static org.poly2tri.geometry.polygon.Polygon makePolygon(LineString lineString) {
        PolygonPoint[] points = new PolygonPoint[lineString.getNumPoints() - 1];
        for(int idPoint=0; idPoint < points.length; idPoint++) {
            Coordinate point = lineString.getCoordinateN(idPoint);
            points[idPoint] = new PolygonPoint(point.x, point.y, Double.isNaN(point.z) ? 0 : point.z);
        }
        return new org.poly2tri.geometry.polygon.Polygon(points);
    }

    private static Coordinate toJts(boolean is2d, Point pt) {
        if(is2d) {
            return new Coordinate(pt.getX(), pt.getY());
        } else {
            return new Coordinate(pt.getX(), pt.getY(), pt.getZ());
        }
    }

    private static MultiPolygon tessellatePolygon(Polygon polygon) {
        boolean is2D = CoordinateSequenceDimensionFilter.apply(polygon).is2D();
        GeometryFactory gf = polygon.getFactory();
        org.poly2tri.geometry.polygon.Polygon poly = makePolygon(polygon.getExteriorRing());
        // Add holes
        for(int idHole = 0; idHole < polygon.getNumInteriorRing(); idHole++) {
            poly.addHole(makePolygon(polygon.getInteriorRingN(idHole)));
        }
        // Do triangulation
        Poly2Tri.triangulate(poly);
        List<DelaunayTriangle> delaunayTriangle = poly.getTriangles();
        // Convert into multi polygon
        Polygon[] polygons = new Polygon[delaunayTriangle.size()];
        for(int idTriangle=0; idTriangle < polygons.length; idTriangle++) {
            TriangulationPoint[] pts = delaunayTriangle.get(idTriangle).points;
            polygons[idTriangle] = gf.createPolygon(new Coordinate[]{toJts(is2D, pts[0]),toJts(is2D, pts[1]),
                    toJts(is2D, pts[2]), toJts(is2D, pts[0])});
        }
        return gf.createMultiPolygon(polygons);
    }

    private static MultiPolygon tessellatePolygonEar(Polygon polygon) {
        EarClipper earClipper = new EarClipper(polygon);
        return earClipper.getResult(true);
    }

    public static MultiPolygon tessellate(Geometry geometry) throws IllegalArgumentException {
        if(geometry == null) {
            return null;
        }
        if(geometry instanceof Polygon) {
            return tessellatePolygon((Polygon) geometry);
        } else if (geometry instanceof MultiPolygon) {
            ArrayList<Polygon> polygons = new ArrayList<Polygon>(geometry.getNumGeometries() * 2);
            for(int idPoly = 0; idPoly < geometry.getNumGeometries(); idPoly++) {
                MultiPolygon triangles = tessellatePolygon((Polygon)geometry.getGeometryN(idPoly));
                polygons.ensureCapacity(triangles.getNumGeometries());
                for(int idTri=0; idTri < triangles.getNumGeometries(); idTri++) {
                    polygons.add((Polygon)triangles.getGeometryN(idTri));
                }
            }
            return geometry.getFactory().createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
        } else {
            throw new IllegalArgumentException("ST_Tessellate accept only Polygon and MultiPolygon types not instance" +
                    " of "+geometry.getClass().getSimpleName());
        }
    }
}
