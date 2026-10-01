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

import org.h2.util.geometry.GeometryUtils;
import org.h2.value.ValueGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Class to cache the prepared geometries used by the spatial predicates
 *
 * <p>When the same geometry is given to a predicate on successive calls (a
 * constant argument, or the outer row of a nested loop join), it is prepared
 * with {@link PreparedGeometryFactory} at its second occurrence, and the
 * prepared version is reused for the following calls.</p>
 *
 * <ul>
 *     <li>Each predicate owns its cache instance, so that different predicates do not share entries.</li>
 *     <li>Each thread has its own entries: a SQL function call runs on the thread of the
 *     calling session, so there is no lock and no sharing between H2 sessions.</li>
 *     <li>Few geometries are kept, the least recently used one is replaced. Several calls of the same
 *     predicate in one query (e.g. {@code ST_Intersects(a, zone1) AND ST_Intersects(b, zone2)})
 *     each keep their prepared geometry.</li>
 *     <li>A geometry is identified by comparing its EWKB bytes,
 *     which is much faster than building a JTS geometry.</li>
 *     <li>Small geometries are never cached.</li>
 * </ul>
 */
public final class PreparedGeometryCache {

    /**
     * Geometries whose EWKB is smaller than this size (about 100 points in 2D) are not cached.
     */
    static final int MIN_BYTES = 1600;

    /**
     * Number of geometries kept per thread and per predicate.
     */
    static final int SIZE = 4;

    private static final class Feature {
        // Just to compare the feature
        WeakReference<ValueGeometry> value;
        byte[] bytes;
        // SoftReference uses a  garbage collector when memory is needed. This is better
        SoftReference<PreparedGeometry> prepared;
        long lastUse;

        boolean matches(ValueGeometry v, byte[] b) {
            return (value != null && value.get() == v) || Arrays.equals(bytes, b);
        }
    }

    /**
     * Status of the feature
     */
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
     * Register the geometry given to the predicate.
     *
     * @param value geometry argument
     * @return the prepared geometry if this geometry was already given to the
     * predicate recently, null otherwise
     */
    public PreparedGeometry get(ValueGeometry value) {
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
                PreparedGeometry prepared = e.prepared == null ? null : e.prepared.get();
                if (prepared == null) {
                    // Second occurrence, or released by the garbage collector: prepare it
                    prepared = PreparedGeometryFactory.prepare(value.getGeometry());
                    e.prepared = new SoftReference<>(prepared);
                }
                return prepared;
            }
            if (oldest == null || e.lastUse < oldest.lastUse) {
                oldest = e;
            }
        }
        // First occurrence: remember it, in place of the least recently used entry
        oldest.value = new WeakReference<>(value);
        oldest.bytes = bytes;
        oldest.prepared = null;
        oldest.lastUse = now;
        return null;
    }

    /**
     * Release the geometries kept by the current thread.
     */
    public void clear() {
        state.remove();
    }

    /**
     * @param envelopeA H2 envelope (minX, maxX, minY, maxY)
     * @param envelopeB H2 envelope (minX, maxX, minY, maxY)
     * @return true if envelopeA contains envelopeB
     */
    static boolean envelopeContains(double[] envelopeA, double[] envelopeB) {
        return envelopeA[GeometryUtils.MIN_X] <= envelopeB[GeometryUtils.MIN_X]
                && envelopeA[GeometryUtils.MAX_X] >= envelopeB[GeometryUtils.MAX_X]
                && envelopeA[GeometryUtils.MIN_Y] <= envelopeB[GeometryUtils.MIN_Y]
                && envelopeA[GeometryUtils.MAX_Y] >= envelopeB[GeometryUtils.MAX_Y];
    }

    /**
     * @throws SQLException if the two geometries do not have the same SRID
     */
    static void checkSRID(ValueGeometry geomA, ValueGeometry geomB) throws SQLException {
        if (geomA.getSRID() != geomB.getSRID()) {
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }
    }
}
