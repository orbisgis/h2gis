/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.graalvm;

import org.h2gis.api.Function;
import org.h2gis.functions.factory.H2GISFunctions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * H2 creates the H2GIS functions by reflection: in the native library, a
 * function whose class is not declared in reflect-config.json fails with
 * "Class ... not found".
 *
 * @author Erwan Bocher, CNRS
 */
public class ReflectConfigTest {

    @Test
    public void testAllFunctionsInReflectConfig() throws IOException {
        Set<String> declared = new HashSet<>();
        try (InputStream in = ReflectConfigTest.class.getResourceAsStream("/META-INF/native-image/reflect-config.json")) {
            assertNotNull(in, "reflect-config.json not found");
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            while (matcher.find()) {
                declared.add(matcher.group(1));
            }
        }
        List<String> missing = new ArrayList<>();
        for (Function function : H2GISFunctions.getBuiltInsFunctions()) {
            String className = function.getClass().getName();
            if (!declared.contains(className)) {
                missing.add(className);
            }
        }
        assertTrue(missing.isEmpty(), "Functions missing in h2gis-graalvm/src/main/resources/META-INF/native-image/reflect-config.json: " + missing);
    }
}