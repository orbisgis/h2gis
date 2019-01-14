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
package org.h2gis.postgis_jts;

import org.postgresql.util.PGobject;

/**
 * PostGRE driver forgets to override hashCode
 * Necessary until https://github.com/pgjdbc/pgjdbc/pull/181 is merged
 */
public class PGObjectWrapper {
    private PGobject pGobject;

    public PGObjectWrapper(PGobject pGobject) {
        this.pGobject = pGobject;
    }

    @Override
    public int hashCode() {
        return pGobject.getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PGObjectWrapper && pGobject.equals(((PGObjectWrapper) obj).getPGobject());
    }

    @Override
    public String toString() {
        return pGobject.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new PGObjectWrapper((PGobject)pGobject.clone());
    }

    /**
     * @return Wrapped PGObject
     */
    public PGobject getPGobject() {
        return pGobject;
    }
}
