/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.overpass;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author E. Bocher, CNRS
 */
public class OverpassFunctionsTest {

    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    static File folder;

    private static Connection connection;
    private static final String DB_NAME = "OverpassTest";
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        connection = H2GISDBFactory.createSpatialDataBase(folder.getAbsolutePath() + File.separator + DB_NAME);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void queryOverpass() throws Exception {
        OverpassTool getOverpass = new OverpassTool();
        String overpassQuery = "[out:csv(\"name\")];\n" +
                "area[name=\"Paimpol\"];\n" +
                "nwr(area)[railway=station];\n" +
                "out;";
        File outputFile = File.createTempFile("myoverpassfile", ".csv", folder);
        getOverpass.downloadFile(overpassQuery, outputFile.getAbsolutePath(), true);
        assertTrue(outputFile.exists());
        ResultSet res = st.executeQuery("SELECT * FROM CSVREAD('" + outputFile.getAbsolutePath() + "')");
        ArrayList data = new ArrayList();
        while (res.next()) {
            data.add(res.getString(1));
        }
        res.close();
        assertEquals(Arrays.asList("Paimpol", "Lancerf"), data);
    }

    @Test
    public void queryOverpass2() throws Exception {
        OverpassTool getOverpass = new OverpassTool();
        String overpassQuery = "[out:csv(::id,::type,\"name\")];\n" +
                "area[name=\"Paimpol\"];\n" +
                "nwr(area)[railway=station];\n" +
                "out;";
        File outputFile = File.createTempFile("myoverpassfile", ".csv", folder);
        getOverpass.downloadFile(overpassQuery, outputFile.getAbsolutePath(), true);
        assertTrue(outputFile.exists());
        ResultSet res = st.executeQuery("SELECT name FROM CSVREAD('" + outputFile.getAbsolutePath() + "', null, 'fieldSeparator=\t')");
        ArrayList data = new ArrayList();
        while (res.next()) {
            data.add(res.getString(1));
        }
        res.close();
        assertEquals(Arrays.asList("Paimpol", "Lancerf"), data);
    }

    @Test
    public void queryOverpass3() throws Exception {
        OverpassTool getOverpass = new OverpassTool();
        String overpassQuery = "[out: csv(\"name\")];\n" +
                "area[name=\"Paimpol\"];\n" +
                "nwr(area)[railway=station];\n" +
                "out;";
        File outputFile = File.createTempFile("myoverpassfile", ".json", folder);
        assertThrows(Exception.class, () ->
                getOverpass.downloadFile(overpassQuery, outputFile.getAbsolutePath(), true));
    }

    @Test
    public void queryOverpass4() throws Exception {
        OverpassTool getOverpass = new OverpassTool();
        String overpassQuery = "[out: json];\n" +
                "area[name=\"Paimpol\"];\n" +
                "nwr(area)[railway=station];\n" +
                "out;";
        File outputFile = File.createTempFile("myoverpassfile", ".json", folder);
        getOverpass.downloadFile(overpassQuery, outputFile.getAbsolutePath(), true);
        assertTrue(outputFile.exists());
    }

    @Test
    public void ST_Overpass1() throws Exception {
        String overpassQuery = "[out: csv(\"name\")];\n" +
                "area[name=\"Paimpol\"];\n" +
                "nwr(area)[railway=station];\n" +
                "out;";
        File outputFile = File.createTempFile("myoverpassfile", ".csv", folder);
        st.execute("SELECT ST_OverpassDownloader('" + overpassQuery + "', '" + outputFile.getAbsolutePath() + "', true)");
        assertTrue(outputFile.exists());
        ResultSet res = st.executeQuery("SELECT name FROM CSVREAD('" + outputFile.getAbsolutePath() + "', null, 'fieldSeparator=\t')");
        ArrayList data = new ArrayList();
        while (res.next()) {
            data.add(res.getString(1));
        }
        res.close();
        assertEquals(Arrays.asList("Paimpol", "Lancerf"), data);
    }

    @Test
    public void ST_Overpass2() throws Exception {
        st.execute("DROP TABLE IF EXISTS names;" +
                "CREATE TABLE names (name VARCHAR);" +
                "INSERT INTO names VALUES('Paimpol'), ('Redon');");
        st.execute("SELECT ST_OverpassDownloader(CONCAT('[out: csv(''name'')];area[name=','\"',name,'\"', ']; nwr(area)[railway=station];out;')," +
                "concat('" + folder.getAbsolutePath() + File.separator + "file_', name, '.csv')) FROM names");
        assertTrue(new File(folder.getAbsolutePath() + File.separator + "file_Paimpol.csv").exists());
        assertTrue(new File(folder.getAbsolutePath() + File.separator + "file_Redon.csv").exists());
    }

    @Disabled
    @Test
    public void ST_Overpass3() throws Exception {
       st.execute("select ST_OverpassDownloader(CONCAT('[bbox:', ST_AsOverpassBbox(st_Expand('SRID=4326;POINT(-2.781140 47.643182)'::GEOMETRY, 0.001)), " +
                "']', '[out:csv(::count, ::\"count:nodes\", ::\"count:ways\", ::\"count:relations\")][timeout:25];\n" +
                "(\n" +
                "  node[building=yes];\n" +
                "  way[building=yes];\n" +
                "  relation[building=yes];\n" +
                ");\n" +
                "out count;'), '/tmp/count_building', true )");
    }

    @Test
    public void ST_AsOverpassBbox1() throws Exception {
        ResultSet res = st.executeQuery("select ST_AsOverpassBbox(st_Expand('SRID=4326;POINT(-2.781140 47.643182)'::GEOMETRY, 0.001))");
        res.next();
        assertEquals("47.642182000000005,-2.78214,47.644182,-2.7801400000000003", res.getString(1));
        res.close();
    }
}
