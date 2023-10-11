package org.h2gis.functions.io.fgb;

import com.google.flatbuffers.ArrayReadWriteBuf;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.FlexBuffers;
import com.google.flatbuffers.ReadBuf;
import org.h2.util.geometry.EWKTUtils;
import org.h2.util.geometry.JTSUtils;
import org.h2gis.functions.io.fgb.fileTable.GeometryConversions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTWriter;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.GeometryType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jared Erickson
 * @author Nicolas Fortin // Z M Tests
 */
public class GeometryConversionsTest {

    @Test
    public void toGeometryType() {
        assertEquals(GeometryType.Unknown, GeometryConversions.toGeometryType(Geometry.class));
        assertEquals(GeometryType.Point, GeometryConversions.toGeometryType(Point.class));
        assertEquals(GeometryType.LineString, GeometryConversions.toGeometryType(LineString.class));
        assertEquals(GeometryType.Polygon, GeometryConversions.toGeometryType(Polygon.class));
        assertEquals(GeometryType.MultiPoint, GeometryConversions.toGeometryType(MultiPoint.class));
        assertEquals(GeometryType.MultiLineString, GeometryConversions.toGeometryType(MultiLineString.class));
        assertEquals(GeometryType.MultiPolygon, GeometryConversions.toGeometryType(MultiPolygon.class));
    }

    @Test
    public void unknowGeometryShouldThrow() {
        assertThrows(RuntimeException.class, () -> GeometryConversions.toGeometryType(Date.class));
    }

    @Test
    public void toGeometryTypeWithGeometrySubClass() {
        assertEquals(GeometryType.Point, GeometryConversions.toGeometryType(MyPoint.class));
    }

    private static String serializeDeserializeRound(String ewkt) throws IOException {
        Geometry geom = JTSUtils.ewkb2geometry(EWKTUtils.ewkt2ewkb(ewkt));
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();
        byte GeomType = GeometryConversions.toGeometryType(geom.getClass());
        int geometryOffset = GeometryConversions.serialize(flatBufferBuilder, geom, GeomType);
        flatBufferBuilder.finish(geometryOffset);
        org.wololo.flatgeobuf.generated.Geometry fgbGeom = org.wololo.flatgeobuf.generated.Geometry.
                getRootAsGeometry(flatBufferBuilder.dataBuffer());
        Geometry geomJTSOutput = GeometryConversions.deserialize(fgbGeom, GeomType);
        if(geomJTSOutput == null) {
            throw new IOException("Null geometry");
        }
        try {
            return EWKTUtils.ewkb2ewkt(JTSUtils.geometry2ewkb(geomJTSOutput));
        } catch (IllegalArgumentException ex) {
            throw new IOException("Can't ewkb JTS geometry:\n"+new WKTWriter(4).write(geomJTSOutput), ex);
        }
    }

    @Test
    public void testXYZ() throws IOException {
        String expectedWKT = "POINT Z (3 5 7)";
        assertEquals(expectedWKT, serializeDeserializeRound(expectedWKT));

        expectedWKT = "LINESTRING Z (3 5 7, 4 8 8, 6 9 10)";
        assertEquals(expectedWKT, serializeDeserializeRound(expectedWKT));

        expectedWKT = "POLYGON Z ((10 5 1, 10 10 2, 8 10 3, 8 5 4, 10 5 1))";
        assertEquals(expectedWKT, serializeDeserializeRound(expectedWKT));

        expectedWKT = "MULTIPOINT Z ((3 5 1), (4 8 2), (6 9 3))";
        assertEquals(expectedWKT, serializeDeserializeRound(expectedWKT));

        expectedWKT = "MULTILINESTRING Z ((3 5 1, 4 8 1, 6 9 2), (9 2 9, 1 2 8, 6 6 7), (10 1 2, 9 2 1, 8 3 3))";
        assertEquals(expectedWKT, serializeDeserializeRound(expectedWKT));

        expectedWKT = "MULTIPOLYGON Z (((10 5 1, 10 10 2, 8 10 3, 8 5 4, 10 5 1)), ((5 5 1, 5 5 2, 4 5 3, 4 2 4, 5 5 1)))";
        assertEquals(expectedWKT, serializeDeserializeRound(expectedWKT));

        expectedWKT = "POINT Z EMPTY";
        assertEquals(expectedWKT, serializeDeserializeRound(expectedWKT));

        expectedWKT = "LINESTRING Z EMPTY";
        assertEquals(expectedWKT, serializeDeserializeRound(expectedWKT));

        expectedWKT = "POLYGON Z EMPTY";
        assertEquals(expectedWKT, serializeDeserializeRound(expectedWKT));

    }

    private static class MyPoint extends Point {
        public MyPoint(CoordinateSequence coordinates, GeometryFactory factory) {
            super(coordinates, factory);
        }
    }

}