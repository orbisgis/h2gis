/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.affine_transformations;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.openjdk.jmh.annotations.*;

/**
 * Class used to benchmark the execution of the {@link ST_Rotate} methods.
 *
 * @author Sylvain PALOMINOS
 */
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 5)
@Fork(value = 3)
@BenchmarkMode(Mode.Throughput)
public class ST_RotateBenchmark {

    private static final GeometryFactory FACTORY = new GeometryFactory();

    private static final double X0 = 42.42;
    private static final double Y0 = -12.9;
    private static final Point AROUND = FACTORY.createPoint(new Coordinate(X0, Y0));
    private static final Point PT = FACTORY.createPoint(new Coordinate(65, 11));
    private static final double ANGLE = 2.0482774;

    /**
     * Rotation of a point with a specific angle.
     */
    @Benchmark
    public void pointCenterRotation() {
        ST_Rotate.rotate(PT, ANGLE);
    }

    /**
     * Rotation of a point with a specific angle around a point.
     */
    @Benchmark
    public void pointAroundRotation() {
        ST_Rotate.rotate(PT, ANGLE, AROUND);
    }

    /**
     * Rotation of a point with a specific angle around coordinates.
     */
    @Benchmark
    public void pointXYRotation() {
        ST_Rotate.rotate(PT, ANGLE, X0, Y0);
    }
}
