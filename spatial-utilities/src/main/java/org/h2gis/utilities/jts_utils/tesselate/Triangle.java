package org.h2gis.utilities.jts_utils.tesselate;

/**
 * @author Michael Bedward
 */
public class Triangle{

    private final int[] vertices;

    /**
     * Constructor. No checking is done on the values supplied.
     *
     * @param v0 first vertex
     * @param v1 second vertex
     * @param v2 third vertex
     */
    public Triangle(int v0, int v1, int v2) {
        vertices = new int[3];
        setVertices(v0, v1, v2);
    }

    /**
     * Set the vertex indices for this Triangle. No checking is done
     * on the values supplied.
     *
     * @param v0 first vertex
     * @param v1 second vertex
     * @param v2 third vertex
     */
    public void setVertices(int v0, int v1, int v2) {
        vertices[0] = v0;
        vertices[1] = v1;
        vertices[2] = v2;
    }

    /**
     * Get this Triangle's vertex indices
     *
     * @return a new array with the vertex indices
     */
    public int[] getVertices() {
        int[] copy = new int[3];
        for (int i = 0; i < 3; i++) {
            copy[i] = vertices[i];
        }
        return copy;
    }

    /**
     * Return vertex indices shared with another triangle.

     * @param other other Triangle
     * @return {@code null} if no vertices are shared; otherwise an
     *         array containing 1, 2 or 3 vertex indices
     */
    public int[] getSharedVertices(Triangle other) {
        int count = 0;
        boolean[] shared = new boolean[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (vertices[i] == other.vertices[j]) {
                    count++;
                    shared[i] = true;
                }
            }
        }
        int[] common = null;
        if (count > 0) {
            common = new int[count];
            for (int i = 0, k = 0; i < 3; i++) {
                if (shared[i]) {
                    common[k++] = vertices[i];
                }
            }
        }
        return common;
    }

    /**
     * Return a string representation of this Triangle
     *
     * @return string of the form "Triangle(%d %d %d)"
     */
    @Override
    public String toString() {
        return String.format("Triangle(%d %d %d)", vertices[0], vertices[1], vertices[2]);
    }
}
