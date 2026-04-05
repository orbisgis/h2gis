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

package org.h2gis.functions.spatial.clusters;

/**
 * Erwan Bocher (CNRS)
 * Utility class for Union-Find operations.
 */
public final class UnionFind {

    private UnionFind() {} // Prevent instantiation

    public static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]]; // Path compression
            i = parent[i];
        }
        return i;
    }

    public static void union(int[] parent, int[] rank, int i, int j) {
        int ri = find(parent, i);
        int rj = find(parent, j);
        if (ri == rj) return;
        if (rank[ri] < rank[rj]) {
            parent[ri] = rj;
        } else if (rank[ri] > rank[rj]) {
            parent[rj] = ri;
        } else {
            parent[rj] = ri;
            rank[ri]++;
        }
    }
}