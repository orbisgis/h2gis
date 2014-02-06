/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.h2spatialext.function.spatial.edit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
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
        GeometryEditor localGeometryEditor = new GeometryEditor();
        BoxDeleteVertexOperation boxDeleteVertexOperation = new BoxDeleteVertexOperation(envelope);
        Geometry localGeometry = localGeometryEditor.edit(geometry, boxDeleteVertexOperation);
        if (boxDeleteVertexOperation.isEdited()) {
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

        private Envelope env;
        private boolean isEdited = false;

        public BoxDeleteVertexOperation(Envelope paramEnvelope) {
            this.env = paramEnvelope;
        }

        public boolean isEdited() {
            return this.isEdited;
        }

        @Override
        public Coordinate[] edit(Coordinate[] paramArrayOfCoordinate, Geometry paramGeometry) {
            if (this.isEdited) {
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
            for (int k = 0; k < paramArrayOfCoordinate.length; k++) {
                if (!this.env.contains(paramArrayOfCoordinate[k])) {
                    arrayOfCoordinate1[(j++)] = paramArrayOfCoordinate[k];
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
            this.isEdited = true;
            return localObject;
        }

        private boolean hasVertexInBox(Coordinate[] paramArrayOfCoordinate) {
            for (int i = 0; i < paramArrayOfCoordinate.length; i++) {
                if (this.env.contains(paramArrayOfCoordinate[i])) {
                    return true;
                }
            }
            return false;
        }
    }
}
