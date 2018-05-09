/**
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

package org.h2gis.utilities.jts_utils;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.math.Vector2D;
import org.locationtech.jts.math.Vector3D;

/**
 * Used by TriangleContouring.
 * Add the constraint of CCW orientation
 * Store also three double values, one fore each vertices
 *
 * ANR EvalPDU
 * IFSTTAR 11_05_2011
 * @author Nicolas FORTIN, Judicaël PICAUT
 */
public class TriMarkers extends Triangle {

    private static double epsilon = 1e-12;

    /**
     * Default constructor
     */
    public TriMarkers() {
        super(new Coordinate(), new Coordinate(), new Coordinate());
        this.m1 = 0;
        this.m2 = 0;
        this.m3 = 0;
    }

    @Override
    public String toString() {
        return "TriMarkers{" + "p1=" + p0 + ", p2=" + p1 + ", p3=" + p2 + " m1=" + m1 + ", m2=" + m2 + ", m3=" + m3 + "}";
    }

    /**
     * Constructor
     * @param p0 First vertex
     * @param p1 Second vertex
     * @param p2 Third vertex
     * @param m1 First vertex attribute
     * @param m2 Second vertex attribute
     * @param m3 Third vertex attribute
     */
    public TriMarkers(Coordinate p0, Coordinate p1, Coordinate p2, double m1,
               double m2, double m3) {
        super(p0, p1, p2);

        if (!CGAlgorithms.isCCW(this.getRing())) {
            this.setCoordinates(p2, p1, p0);
            this.m1 = m3;
            this.m3 = m1;
        } else {
            this.m1 = m1;
            this.m3 = m3;
        }
        this.m2 = m2;
    }

    double m1, m2, m3;

    void setMarkers(double m1, double m2, double m3) {
        this.m1 = m1;
        this.m2 = m2;
        this.m3 = m3;
    }

    void setAll(Coordinate p0, Coordinate p1, Coordinate p2, double m1,
                double m2, double m3) {
        setCoordinates(p0, p1, p2);
        setMarkers(m1, m2, m3);
        if (!CGAlgorithms.isCCW(this.getRing())) {
            this.setCoordinates(p2, p1, p0);
            this.m1 = m3;
            this.m3 = m1;
        }
    }

    double getMinMarker() {
        return getMinMarker((short) -1);
    }

    double getMinMarker(int exception) {
        double minval = Double.POSITIVE_INFINITY;
        if (exception != 0) {
            minval = Math.min(minval, this.m1);
        }
        if (exception != 1) {
            minval = Math.min(minval, this.m2);
        }
        if (exception != 2) {
            minval = Math.min(minval, this.m3);
        }
        return minval;
    }

    double getMaxMarker() {
        return getMaxMarker((short) -1);
    }

    double getMaxMarker(int exception) {
        double maxval = Double.NEGATIVE_INFINITY;
        if (exception != 0) {
            maxval = Math.max(maxval, this.m1);
        }
        if (exception != 1) {
            maxval = Math.max(maxval, this.m2);
        }
        if (exception != 2) {
            maxval = Math.max(maxval, this.m3);
        }
        return maxval;
    }

    void setCoordinates(Coordinate p0, Coordinate p1, Coordinate p2) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
    }

    Coordinate[] getRing() {
        return new Coordinate[] { p0, p1, p2, p0 };
    }

    Coordinate getVertice(int idvert) {
        if (idvert == 0) {
            return p0;
        } else if (idvert == 1) {
            return p1;
        } else {
            return p2;
        }
    }

    double getMarker(int idvert) {
        if (idvert == 0) {
            return m1;
        } else if (idvert == 1) {
            return m2;
        } else {
            return m3;
        }
    }

    /**
     * Get the normal vector to this triangle, of length 1.
     * @param t input triangle
     * @return vector normal to the triangle.
     */
    public static Vector3D getNormalVector(Triangle t) throws IllegalArgumentException {
        if(Double.isNaN(t.p0.z) || Double.isNaN(t.p1.z) ||  Double.isNaN(t.p2.z)) {
            throw new IllegalArgumentException("Z is required, cannot compute triangle normal of "+t);
        }
        double dx1 = t.p0.x - t.p1.x;
        double dy1 = t.p0.y - t.p1.y;
        double dz1 = t.p0.z - t.p1.z;
        double dx2 = t.p1.x - t.p2.x;
        double dy2 = t.p1.y - t.p2.y;
        double dz2 = t.p1.z - t.p2.z;
        return Vector3D.create(dy1*dz2 - dz1*dy2, dz1 * dx2 - dx1 * dz2, dx1 * dy2 - dy1 * dx2).normalize();
    }

    /**
     * Get the vector with the highest down slope in the plan.
     * @param normal
     * @param epsilon
     * @return the steepest vector.
     */
    public static Vector3D getSteepestVector(final Vector3D normal, final double epsilon) {
        if (Math.abs(normal.getX()) < epsilon && Math.abs(normal.getY()) < epsilon) {
            return new Vector3D(0, 0, 0);
        }
        Vector3D slope;
        if (Math.abs(normal.getX()) < epsilon) {
            slope = new Vector3D(0, 1, -normal.getY() / normal.getZ());
        } else if (Math.abs(normal.getY()) < epsilon) {
            slope = new Vector3D(1, 0, -normal.getX() / normal.getZ());
        } else {
            slope = new Vector3D(normal.getX() / normal.getY(), 1,
                    -1 / normal.getZ() * (normal.getX() * normal.getX() / normal.getY() + normal.getY()));
        }
        //We want the vector to be low-oriented.
        if (slope.getZ() > epsilon) {
            slope = new Vector3D(-slope.getX(), -slope.getY(), -slope.getZ());
        }
        //We normalize it
        return slope.normalize();
    }

    /**
     *
     * @param normal Plane normal
     * @param epsilon Epsilon value ex:1e-12
     * @return The steepest slope of this plane in degree.
     */
    public static double getSlopeInPercent(final Vector3D normal, final double epsilon) {
        Vector3D vector = getSteepestVector(normal, epsilon);
        if(Math.abs(vector.getZ()) < epsilon) {
            return 0;
        } else {
            return (Math.abs(vector.getZ()) / new Vector2D(vector.getX(), vector.getY()).length()) * 100;
        }
    }
    
    /**
     * Test if a coordinate intersects the triangle
     * <p/>
     * {@see http://www.blackpawn.com/texts/pointinpoly/default.html}
     *
     * @param p Coordinate of the point
     * @param t input triangle
     * @return True if the coordinate is in triangle
     */
    public static boolean intersects(Coordinate p, Triangle t) {
        Vector2D v0 = new Vector2D(t.p0.x - t.p2.x, t.p0.y - t.p2.y);
        Vector2D v1 = new Vector2D(t.p1.x - t.p2.x, t.p1.y - t.p2.y);
        Vector2D v2 = new Vector2D(p.x - t.p2.x, p.y - t.p2.y);

        // Compute dot products
        double dot00 = v0.dot(v0);
        double dot01 = v0.dot(v1);
        double dot02 = v0.dot(v2);
        double dot11 = v1.dot(v1);
        double dot12 = v1.dot(v2);

        // Compute barycentric coordinates
        double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        // Check if point is in triangle
        return (u > (0. - epsilon)) && (v > (0. - epsilon))
                && (u + v < (1. + epsilon));

}
}
