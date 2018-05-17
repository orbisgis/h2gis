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

package org.h2gis.utilities;

import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test of URI utilities
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public class URIUtilitiesTest {

    @Test
    public void testGetQueryKeyValuePairs() throws Exception {
        URI uri = URI.create("http://services.orbisgis.org/wms/wms?REQUEST=GetMap&SERVICE=WMS&VERSION=1.3.0" +
                "&LAYERS=cantons_dep44&CRS=EPSG:27572" +
                "&BBOX=259555.01152073737,2218274.7695852537,342561.9239631337,2287024.7695852537&WIDTH=524&HEIGHT=434" +
                "&FORMAT=image/png&STYLES=");
        Map<String,String> query = URIUtilities.getQueryKeyValuePairs(uri);
        assertEquals(10,query.size());
        assertEquals("GetMap",query.get("request"));
        assertEquals("WMS",query.get("service"));
        assertEquals("cantons_dep44",query.get("layers"));
        assertEquals("EPSG:27572",query.get("crs"));
        assertEquals("259555.01152073737,2218274.7695852537,342561.9239631337,2287024.7695852537",query.get("bbox"));
        assertEquals("524",query.get("width"));
        assertEquals("434",query.get("height"));
        assertEquals("image/png",query.get("format"));
        assertEquals("",query.get("styles"));
    }

    @Test
    public void testGetQueryKeyValuePairsJDBC() throws Exception {
        URI uri = URI.create("h2:target/test-resources/dbH2OwsMapContextTest?catalog=&schema=PUBLIC&table=LANDCOVER2000");
        Map<String,String> query = URIUtilities.getQueryKeyValuePairs(uri);
        assertEquals(3,query.size());
        assertEquals("",query.get("catalog"));
        assertEquals("PUBLIC",query.get("schema"));
        assertEquals("LANDCOVER2000",query.get("table"));
        URI uriNoParam = URI.create("h2:target/test-resources/dbH2OwsMapContextTest");
        query = URIUtilities.getQueryKeyValuePairs(uriNoParam);
        assertTrue(query.isEmpty());
    }

    @Test
    public void testRelativize() throws Exception {
        URI rel = new URI("file:///home/user/OrbisGIS/maps/landcover/bla/text.txt");
        URI folder = new URI("file:///home/user/OrbisGIS/maps/landcover/folder/");
        assertEquals("../bla/text.txt", URIUtilities.relativize(folder, rel).toString());
        rel = new URI("file:///home/user/OrbisGIS/maps/landcover/text.txt");
        assertEquals("../text.txt", URIUtilities.relativize(folder, rel).toString());
        rel = new URI("file:///home/user/OrbisGIS/maps/text.txt");
        assertEquals("../../text.txt", URIUtilities.relativize(folder, rel).toString());
        rel = new URI("file:///home/user/OrbisGIS/maps/landcover/folder/text.txt");
        assertEquals("text.txt", URIUtilities.relativize(folder, rel).toString());
        rel = new URI("file:///home/user/OrbisGIS/maps/landcover/folder/sub/text.txt");
        assertEquals("sub/text.txt", URIUtilities.relativize(folder, rel).toString());
        rel = new URI("file:///home/user/OrbisGIS/text.txt");
        assertEquals("../../../text.txt", URIUtilities.relativize(folder, rel).toString());
        rel = new URI("file:///home/user/OrbisGIS/maps/landcover/test/folder/text.txt");
        assertEquals("../test/folder/text.txt", URIUtilities.relativize(folder, rel).toString());
        rel = new URI("file:///");
        assertEquals("../../../../../../", URIUtilities.relativize(folder, rel).toString());
        // This with a file in the base part, file is ignored by relativize
        folder = new URI("file:///home/user/OrbisGIS/maps/landcover/folder/bla.ows");
        rel = new URI("file:///home/user/OrbisGIS/maps/landcover/data/data.shp");
        assertEquals("../data/data.shp", URIUtilities.relativize(folder, rel).toString());
    }

    @Test
    public void testRelativizeSpace() throws Exception {
        URI rel = new URI("file:///home/user/OrbisGIS/maps/landcover/bla%20bla/text.txt");
        URI folder = new URI("file:///home/user/OrbisGIS/maps/landcover/folder/");
        URI wrongFolder = new URI("badscheme:///home/user/OrbisGIS/maps/landcover/folder/");
        assertEquals("../bla%20bla/text.txt", URIUtilities.relativize(folder, rel).toString());
        assertEquals(rel.toString(), URIUtilities.relativize(wrongFolder, rel).toString());
    }

    @Test
    public void testFileFromURI() {
        assertEquals(new File("/mnt/stock/hello.png"),
                URIUtilities.fileFromString(new File("/mnt/stock/hello.png").toString()));
        assertEquals(new File("/mnt/stock/hello world.png"),
                URIUtilities.fileFromString("/mnt/stock/hello world.png"));
    }

    @Test
    public void testConcatenatedParameters() {
        Map<String, String> map = new HashMap<>();
        map.put("query", "GetCapabilities");
        map.put("lang", "en");
        map.put("version", "1.0.0");
        assertEquals("QUERY=GetCapabilities&VERSION=1.0.0", URIUtilities.getConcatenatedParameters(map, "QuerY", "VeRsIoN"));
    }
}
