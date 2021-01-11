/*
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
package org.h2gis.utilities.dbtypes;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class dedicated to {@link DBUtils}.
 *
 * @author Erwan Bocher (CNRS 2021)
 * @author Sylvein PALOMINOS (UBS Chaire GEOTERA 2021)
 */
public class DBUtilsTest {

    @Test
    public void getDTypeTest() throws MalformedURLException {
        String str1 = "jdbc:postgresql://localhost/test";
        String str2 = "postgresql://localhost/test";

        URI uri1 = URI.create(str1);
        URI uri2 = URI.create(str2);

        assertEquals(DBTypes.POSTGRESQL, DBUtils.getDBType(str1));
        assertEquals(DBTypes.POSTGRESQL, DBUtils.getDBType(str2));
        assertEquals(DBTypes.POSTGRESQL, DBUtils.getDBType(uri1));
        assertEquals(DBTypes.POSTGRESQL, DBUtils.getDBType(uri2));

        str1 = "jdbc:h2://localhost/test";
        str2 = "h2://localhost/test";

        uri1 = URI.create(str1);
        uri2 = URI.create(str2);

        assertEquals(DBTypes.H2, DBUtils.getDBType(str1));
        assertEquals(DBTypes.H2, DBUtils.getDBType(str2));
        assertEquals(DBTypes.H2, DBUtils.getDBType(uri1));
        assertEquals(DBTypes.H2, DBUtils.getDBType(uri2));
    }
}
