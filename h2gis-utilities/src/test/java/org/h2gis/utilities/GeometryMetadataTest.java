/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import static org.junit.jupiter.api.Assertions.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.CoordinateXYZM;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;

/**
 *
 * @author Erwan Bocher, CNRS (2020)
 */
public class GeometryMetadataTest {

    @Test
    public void testJTSParserGeometryDimensionAndType() throws Exception {
        WKTReader wKTReader = new WKTReader();
        Geometry geom = wKTReader.read("POINT(0 0)");
        GeometryMetaData geomMetadata = GeometryMetaData.getMetaData(geom);
        assertEquals("POINT", geomMetadata.geometryType);
        assertEquals(2, geomMetadata.dimension);
        geom = wKTReader.read("POINT(0 0 0)");
        geomMetadata = GeometryMetaData.getMetaData(geom);
        assertEquals("POINTZ", geomMetadata.geometryType);
        assertEquals(3, geomMetadata.dimension);
        geom = wKTReader.read("LINESTRING(20 10,20 20)");
        geomMetadata = GeometryMetaData.getMetaData(geom);
        assertEquals("LINESTRING", geomMetadata.geometryType);
        assertEquals(2, geomMetadata.dimension);
        assertFalse(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        geom = wKTReader.read("LINESTRINGZ(20 10 0,20 20 0)");
        geomMetadata = GeometryMetaData.getMetaData(geom);
        assertEquals("LINESTRINGZ", geomMetadata.geometryType);
        assertEquals(3, geomMetadata.dimension);
        assertTrue(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
    }

    @Test
    public void testJTSGeometryDimensionAndType() throws Exception {
        GeometryFactory gf = new GeometryFactory();
        Coordinate coordinate = new CoordinateXY(0, 0);
        Geometry geom = gf.createPoint(coordinate);
        GeometryMetaData geomMetadata = GeometryMetaData.getMetaData(geom);
        assertEquals("POINT", geomMetadata.geometryType);
        assertEquals(2, geomMetadata.dimension);
        coordinate = new Coordinate(0, 0, 0);
        geom = gf.createPoint(coordinate);
        geomMetadata = GeometryMetaData.getMetaData(geom);
        assertEquals("POINTZ", geomMetadata.geometryType);
        assertEquals(3, geomMetadata.dimension);
        coordinate = new CoordinateXYM(0, 0, 0);
        geom = gf.createPoint(coordinate);
        geomMetadata = GeometryMetaData.getMetaData(geom);
        assertEquals("POINTM", geomMetadata.geometryType);
        assertEquals(3, geomMetadata.dimension);
        assertFalse(geomMetadata.hasZ);
        assertTrue(geomMetadata.hasM);
        coordinate = new CoordinateXYZM(0, 0, 0, 0);
        geom = gf.createPoint(coordinate);
        geomMetadata = GeometryMetaData.getMetaData(geom);
        assertEquals("POINTZM", geomMetadata.geometryType);
        assertEquals(4, geomMetadata.dimension);
        assertTrue(geomMetadata.hasZ);
        assertTrue(geomMetadata.hasM);
    }

    @Test
    public void testParseEWKT() throws Exception {
        GeometryMetaData geomMetadata = GeometryMetaData.getMetaData("SRID=4326;POINT(0 0)");
        assertEquals("POINT", geomMetadata.geometryType);
        assertEquals(2, geomMetadata.dimension);
        assertEquals(4326, geomMetadata.SRID);
        geomMetadata = GeometryMetaData.getMetaData("SRID=4326;POINTZM(0 0 0 0)");
        assertEquals("POINTZM", geomMetadata.geometryType);
        assertEquals(4, geomMetadata.dimension);
        assertEquals(4326, geomMetadata.SRID);
        geomMetadata = GeometryMetaData.getMetaData("POINT(0 0)");
        assertEquals("POINT", geomMetadata.geometryType);
        assertEquals(2, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
    }

    @Test
    public void testParseCreateTable() throws Exception {
        GeometryMetaData geomMetadata = GeometryMetaData.getMetaData("GEOMETRY");
        assertEquals("GEOMETRY", geomMetadata.geometryType);
        assertEquals(2, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
        assertFalse(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        geomMetadata = GeometryMetaData.getMetaData("GEOMETRY(POINT)");
        assertEquals("POINT", geomMetadata.geometryType);
        assertEquals(2, geomMetadata.dimension);
        assertEquals(0, geomMetadata.SRID);
        assertFalse(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        geomMetadata = GeometryMetaData.getMetaData("GEOMETRY(POINTZ, 4326)");
        assertEquals("POINTZ", geomMetadata.geometryType);
        assertEquals(3, geomMetadata.dimension);
        assertEquals(4326, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        geomMetadata = GeometryMetaData.getMetaData("GEOMETRY(POINT Z, 4326)");
        assertEquals("POINTZ", geomMetadata.geometryType);
        assertEquals(3, geomMetadata.dimension);
        assertEquals(4326, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertFalse(geomMetadata.hasM);
        geomMetadata = GeometryMetaData.getMetaData("GEOMETRY(POINT M, 4326)");
        assertEquals("POINTM", geomMetadata.geometryType);
        assertEquals(3, geomMetadata.dimension);
        assertEquals(4326, geomMetadata.SRID);
        assertFalse(geomMetadata.hasZ);
        assertTrue(geomMetadata.hasM);
        geomMetadata = GeometryMetaData.getMetaData("GEOMETRY(POINTZM, 4326)");
        assertEquals("POINTZM", geomMetadata.geometryType);
        assertEquals(4, geomMetadata.dimension);
        assertEquals(4326, geomMetadata.SRID);
        assertTrue(geomMetadata.hasZ);
        assertTrue(geomMetadata.hasM);
    }    
   
}
