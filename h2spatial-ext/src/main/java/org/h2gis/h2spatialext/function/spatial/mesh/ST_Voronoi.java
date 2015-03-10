package org.h2gis.h2spatialext.function.spatial.mesh;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.utilities.jts_utils.Voronoi;

/**
 * @author Nicolas Fortin
 */
public class ST_Voronoi extends DeterministicScalarFunction {
    private static final int DEFAULT_DIMENSION = 2;

    public ST_Voronoi() {
        addProperty(PROP_REMARKS, "Construct a voronoi diagram from a delaunay triangulation.\n" +
                "ST_VORONOI(THE_GEOM MULTIPOLYGON)\n" +
                "ST_VORONOI(THE_GEOM MULTIPOLYGON,OUT_DIMENSION INTEGER)\n" +
                "ST_VORONOI(THE_GEOM MULTIPOLYGON,OUT_DIMENSION INTEGER,ENVELOPE POLYGON)\n" +
                "Ex:\n" +
                "SELECT ST_VORONOI(ST_DELAUNAY('MULTIPOINT(2 2,6 3,4 7,2 8,1 6,3 5)'))\n" +
                "SELECT ST_VORONOI(ST_DELAUNAY('MULTIPOINT(2 2,6 3,4 7,2 8,1 6,3 5)'), 1)\n" +
                "SELECT ST_VORONOI(ST_DELAUNAY('MULTIPOINT(2 2,6 3,4 7,2 8,1 6,3 5)'), 1, ST_EXPAND('POINT(3 5)', 10, 10))");
    }

    @Override
    public String getJavaStaticMethod() {
        return "voronoi";
    }

    public static GeometryCollection voronoi(Geometry triangles) {
        return voronoi(triangles, DEFAULT_DIMENSION);
    }

    public static GeometryCollection voronoi(Geometry triangles, int outputDimension) {
        return voronoi(triangles, outputDimension, null);
    }

    public static GeometryCollection voronoi(Geometry triangles, int outputDimension, Geometry envelope) {
        if(triangles == null) {
            return new GeometryFactory().createGeometryCollection(new Geometry[0]);
        }
        Voronoi voronoi = new Voronoi();
        if(envelope != null) {
            voronoi.setEnvelope(envelope);
        }
        voronoi.generateTriangleNeighbors(triangles);
        return voronoi.generateVoronoi(outputDimension);
    }
}
