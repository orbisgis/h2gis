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
package org.h2gis.utilities.dbtypes;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Enumeration of database types.
 *
 * @author Erwan Bocher (CNRS 2021)
 * @author Sylvain Palominos (UBS Chaire GEOTERA 2021)
 */
public enum DBTypes {
    POSTGRESQL, POSTGIS, H2, H2GIS, UNKNOWN;

    /**
     * Return the list of the reserved keywords.
     * @return The list of reserved keywords.
     */
    public Set<String> getReservedWords() {
        switch(this) {
            case H2:
            case H2GIS:
                return Constants.H2_RESERVED_WORDS;
            case POSTGRESQL:
            case POSTGIS:
                return Constants.POSTGRESQL_RESERVED_WORDS;
            default:
                return new HashSet<>();
        }
    }

    /**
     * Return the special name pattern.
     * @return The spacial name pattern.
     */
    public Pattern specialNamePattern() {
        switch(this) {
            case H2:
            case H2GIS:
                return Constants.H2_SPECIAL_NAME_PATTERN;
            case POSTGRESQL:
            case POSTGIS:
                return Constants.POSTGRESQL_SPECIAL_NAME_PATTERN;
            default:
                return null;
        }
    }
}
