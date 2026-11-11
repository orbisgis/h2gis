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
package org.h2gis.graalvm;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Versions returned by h2gis_get_versions, and the explanation of the errors
 * of a database written by another version of H2, tested on a JVM.
 *
 * @author Erwan Bocher, CNRS
 */
public class NativeInfoTest {

    @Test
    public void testVersionsJson() {
        String json = NativeInfo.versionsJson(GraalCInterface.C_API_VERSION);
        assertTrue(json.startsWith("{\"cApi\":" + GraalCInterface.C_API_VERSION + ","), json);
        assertTrue(json.contains("\"h2\":\"" + org.h2.engine.Constants.VERSION + "\""), json);
        assertTrue(json.contains("\"jts\":\""), json);
        assertTrue(json.endsWith("}"), json);
        // H2GISversion ends its version with a space
        String h2gis = NativeInfo.h2gisVersion();
        assertFalse(h2gis.isEmpty());
        assertEquals(h2gis.trim(), h2gis);
        assertTrue(json.contains("\"h2gis\":\"" + h2gis + "\""), json);
    }

    @Test
    public void testConnectionHint() {
        // 90030: the pages of a file of another version read as corrupted
        SQLException corrupted = new SQLException("File corrupted while reading record [90030-252]", "90030", 90030);
        String hint = NativeInfo.connectionHint(corrupted);
        assertTrue(hint.startsWith(" "), hint);
        assertTrue(hint.contains("another version of H2"), hint);
        assertTrue(hint.contains("H2 " + org.h2.engine.Constants.VERSION), hint);
        // 90048: unsupported file format, and an H2 error wrapped in another exception
        SQLException format = new SQLException("Unsupported database file version", "90048", 90048);
        assertFalse(NativeInfo.connectionHint(new RuntimeException("wrapped", format)).isEmpty());
        // Other errors: no explanation
        assertEquals("", NativeInfo.connectionHint(new SQLException("Wrong user name or password", "28000", 28000)));
        assertEquals("", NativeInfo.connectionHint(new RuntimeException("no SQL error")));
    }
}