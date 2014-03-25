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
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.GeometryEditor;
import java.sql.SQLException;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_RemovePoint extends DeterministicScalarFunction {

    public static final double PRECISION = 10E-6;

    public ST_RemovePoint() {
        addProperty(PROP_REMARKS, "Remove a point on a geometry. "
                + "A tolerance can be set to define a buffer area.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "removePoint";
    }

    /**
     * Remove all vertices that are located within an envelope based on a 10E-6
     * buffer distance.
     *
     * @param geometry
     * @param point
     * @return
     * @throws SQLException
     */
    public static Geometry removePoint(Geometry geometry, Point point) throws SQLException {
        return removePointEnvelope(geometry, point.buffer(PRECISION).getEnvelopeInternal());
    }

    /**
     * Remove all vertices that are located within an envelope based on a given
     * buffer distance.
     *
     * @param geometry
     * @param point
     * @param tolerance
     * @return
     * @throws SQLException
     */
    public static Geometry removePoint(Geometry geometry, Point point, double tolerance) throws SQLException {
        return removePointEnvelope(geometry, point.buffer(tolerance).getEnvelopeInternal());
    }

    /**
     * Remove all vertices that are located within an envelope.
     *
     * @param geometry
     * @param envelope
     * @return
     * @throws SQLException
     */
    private static Geometry removePointEnvelope(Geometry geometry, Envelope envelope) throws SQLException {
        Geometry localGeometry1 = deleteComponents(geometry, envelope);
        if (localGeometry1 != null) {
            return localGeometry1;
        }
        Geometry localGeometry2 = deleteVertices(geometry, envelope);
        if (localGeometry2 != null) {
            return localGeometry2;
        }
        return geometry;
    }

    private static Geometry deleteComponents(Geometry paramGeometry, Envelope paramEnvelope) {
        GeometryEditor localGeometryEditor = new GeometryEditor();
        BoxDeleteComponentOperation localBoxDeleteComponentOperation = new BoxDeleteComponentOperation(paramEnvelope);
        Geometry localGeometry = localGeometryEditor.edit(paramGeometry, localBoxDeleteComponentOperation);
        if (localBoxDeleteComponentOperation.isModified()) {
            return localGeometry;
        }
        return null;
    }

    private static Geometry deleteVertices(Geometry paramGeometry, Envelope paramEnvelope) {
        GeometryEditor localGeometryEditor = new GeometryEditor();
        BoxDeleteVertexOperation localBoxDeleteVertexOperation = new BoxDeleteVertexOperation(paramEnvelope);
        Geometry localGeometry = localGeometryEditor.edit(paramGeometry, localBoxDeleteVertexOperation);
        if (localBoxDeleteVertexOperation.isModified()) {
            return localGeometry;
        }
        return null;
    }

    /**
     * This class is used to remove vertexes that are contained in an envelope.
     * This class has been imported from the JTS test builder UI.
     *
     */
    private static class BoxDeleteVertexOperation extends GeometryEditor.CoordinateOperation {

        //The envelope used to select the coordinates to removed
        private Envelope env;
        private boolean isModified = false;

        public BoxDeleteVertexOperation(Envelope paramEnvelope) {
            this.env = paramEnvelope;
        }

        /**
         * Return true is the geometry has been modified
         *
         * @return
         */
        public boolean isModified() {
            return this.isModified;
        }

        @Override
        public Coordinate[] edit(Coordinate[] paramArrayOfCoordinate, Geometry paramGeometry) {
            if (this.isModified) {
                return paramArrayOfCoordinate;
            }
            if (!hasVertexInBox(paramArrayOfCoordinate)) {
                return paramArrayOfCoordinate;
            }
            int i = 2;
            if ((paramGeometry instanceof LinearRing)) {
                i = 4;
            }
            Coordinate[] arrayOfCoordinate1 = new Coordinate[paramArrayOfCoordinate.length];
            int j = 0;
            for (Coordinate coordinate : paramArrayOfCoordinate) {
                if (!this.env.contains(coordinate)) {
                    arrayOfCoordinate1[(j++)] = coordinate;
                }
            }

            Coordinate[] arrayOfCoordinate2 = CoordinateArrays.removeNull(arrayOfCoordinate1);
            Coordinate[] localObject = arrayOfCoordinate2;
            if (((paramGeometry instanceof LinearRing)) && (arrayOfCoordinate2.length > 1) && (!arrayOfCoordinate2[(arrayOfCoordinate2.length - 1)].equals2D(arrayOfCoordinate2[0]))) {
                Coordinate[] arrayOfCoordinate3 = new Coordinate[arrayOfCoordinate2.length + 1];
                CoordinateArrays.copyDeep(arrayOfCoordinate2, 0, arrayOfCoordinate3, 0, arrayOfCoordinate2.length);
                arrayOfCoordinate3[(arrayOfCoordinate3.length - 1)] = new Coordinate(arrayOfCoordinate3[0]);
                localObject = arrayOfCoordinate3;
            }
            if (localObject.length < i) {
                return paramArrayOfCoordinate;
            }
            this.isModified = true;
            return localObject;
        }

        /**
         * Return true if there is one coordinate in the input box
         *
         * @param paramArrayOfCoordinate
         * @return
         */
        private boolean hasVertexInBox(Coordinate[] paramArrayOfCoordinate) {
            for (Coordinate coordinate : paramArrayOfCoordinate) {
                if (this.env.contains(coordinate)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * This class is used to remove component that are contained in an envelope.
     * This class has been imported from the JTS test builder UI. It's usefull
     * to remove coordinates in a multi geometry.
     *
     */
    private static class BoxDeleteComponentOperation implements GeometryEditor.GeometryEditorOperation {

        private Envelope env;
        private boolean isEdited = false;

        public BoxDeleteComponentOperation(Envelope paramEnvelope) {
            this.env = paramEnvelope;
        }

        /**
         * Return true is the geometry has been modified
         *
         * @return
         */
        public boolean isModified() {
            return this.isEdited;
        }

        @Override
        public Geometry edit(Geometry paramGeometry, GeometryFactory paramGeometryFactory) {
            if (this.env.contains(paramGeometry.getEnvelopeInternal())) {
                this.isEdited = true;
                return null;
            }
            return paramGeometry;
        }
    }
}
