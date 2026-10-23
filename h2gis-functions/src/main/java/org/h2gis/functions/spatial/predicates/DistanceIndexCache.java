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

package org.h2gis.functions.spatial.predicates;

import org.h2.value.ValueGeometry;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * Cache of the distance indexes used by {@link ST_DWithin} and ST_Distance.
 *
 * <p>When the same geometry is given to these functions on successive calls (a
 * constant argument, or the outer row of a nested loop join), a
 * {@link DistanceIndex} is built at its second occurrence and reused for the
 * following calls.</p>
 *
 * <ul>
 *     <li>One instance, {@link #INSTANCE}, is shared by the distance functions,
 *     so that a geometry used by several of them is indexed only once.</li>
 *     <li>Each thread has its own entries: a SQL function call runs on the thread of the
 *     calling session, so there is no lock and no sharing between H2 sessions.</li>
 *     <li>Few geometries are kept, the least recently used one is replaced.</li>
 *     <li>A geometry is identified by comparing its EWKB bytes,
 *     which is much faster than building a JTS geometry.</li>
 *     <li>Small geometries are never cached.</li>
 * </ul>
 */
public final class DistanceIndexCache {

    /**
     * Geometries whose EWKB is smaller than this size (about 6 or 7 points in 2D) are not cached.
     */
    static final int MIN_BYTES = 128;

    /**
     * Number of geometries kept per thread.
     */
    static final int SIZE = 4;

    /**
     * Cache shared by the distance functions (ST_DWithin, ST_Distance).
     */
    public static final DistanceIndexCache INSTANCE = new DistanceIndexCache();

    /**
     * Index on a geometry to compute the distance to other geometries.
     *
     * <p>The distance between two geometries is 0 when they intersect, otherwise
     * it is the distance between their facets (segments and vertices). So
     * {@code dwithin(A, B, d) = facetDistance(A, B) <= d OR intersects(A, B)}.
     * The intersection test is only needed when one of the geometries is areal:
     * for points and lines the facets are the whole geometry.</p>
     *
     * <p>Both indexes are built only when they are first needed.
     * An instance is only used by one thread.</p>
     */
    public static final class DistanceIndex {
        private final Geometry geometry;
        private final boolean areal;
        private PreparedGeometry prepared;
        private IndexedFacetDistance facets;

        DistanceIndex(Geometry geometry) {
            this.geometry = geometry;
            this.areal = geometry.getDimension() == 2;
        }

        /**
         * @param other    other geometry, not empty, same SRID
         * @param distance distance
         * @return true if the indexed geometry is within the distance of the other geometry
         */
        public boolean isWithinDistance(Geometry other, double distance) {
            if (distance == 0) {
                //If distance is equal to 0 use intersects
                return prepared().intersects(other);
            }
            if (facets().isWithinDistance(other, distance)) {
                return true;
            }
            return (areal || other.getDimension() == 2) && prepared().intersects(other);
        }

        /**
         * @param other other geometry, not empty, same SRID
         * @return the distance between the indexed geometry and the other geometry
         */
        public double distance(Geometry other) {
            double d = facets().distance(other);
            if (d == 0) {
                // The boundaries touch: no need to test the containment
                return 0;
            }
            // The facets are apart: the distance is 0 only if one geometry is inside an areal one
            if ((areal || other.getDimension() == 2) && prepared().intersects(other)) {
                return 0;
            }
            return d;
        }

        private PreparedGeometry prepared() {
            if (prepared == null) {
                prepared = PreparedGeometryFactory.prepare(geometry);
            }
            return prepared;
        }

        private IndexedFacetDistance facets() {
            if (facets == null) {
                facets = new IndexedFacetDistance(geometry);
            }
            return facets;
        }
    }

    private static final class Feature {
        WeakReference<ValueGeometry> value;
        byte[] bytes;
        // SoftReference uses a  garbage collector when memory is needed. This is better
        SoftReference<DistanceIndex> index;
        long lastUse;
        boolean matches(ValueGeometry v, byte[] b) {
            return (value != null && value.get() == v) || Arrays.equals(bytes, b);
        }
    }

    private static final class State {
        final Feature[] entries = new Feature[SIZE];
        long clock;

        State() {
            for (int i = 0; i < SIZE; i++) {
                entries[i] = new Feature();
            }
        }
    }

    private final ThreadLocal<State> state = ThreadLocal.withInitial(State::new);

    /**
     * Register the geometry given to a distance function.
     *
     * @param value geometry argument
     * @return the distance index if this geometry was already given
     * recently, null otherwise
     */
    public DistanceIndex get(ValueGeometry value) {
        byte[] bytes = value.getBytesNoCopy();
        if (bytes.length < MIN_BYTES) {
            return null;
        }
        State s = state.get();
        long now = ++s.clock;
        Feature oldest = null;
        for (Feature e : s.entries) {
            if (e.matches(value, bytes)) {
                if (e.value == null || e.value.get() != value) {
                    e.value = new WeakReference<>(value);
                }
                e.lastUse = now;
                DistanceIndex index = e.index == null ? null : e.index.get();
                if (index == null) {
                    // Second occurrence, or released by the garbage collector
                    index = new DistanceIndex(value.getGeometry());
                    e.index = new SoftReference<>(index);
                }
                return index;
            }
            if (oldest == null || e.lastUse < oldest.lastUse) {
                oldest = e;
            }
        }
        // First occurrence: remember it, in place of the least recently used entry
        oldest.value = new WeakReference<>(value);
        oldest.bytes = bytes;
        oldest.index = null;
        oldest.lastUse = now;
        return null;
    }

    /**
     * Release the geometries kept by the current thread.
     */
    public void clear() {
        state.remove();
    }
}