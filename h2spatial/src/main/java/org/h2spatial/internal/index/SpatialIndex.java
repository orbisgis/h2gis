/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2spatial.internal.index;

import org.h2.engine.Database;
import org.h2.engine.Session;
import org.h2.engine.SessionInterface;
import org.h2.jdbc.JdbcConnection;
import org.h2.mvstore.db.MVTable;
import org.h2.mvstore.rtree.MVRTreeMap;
import org.h2.table.Table;

import java.sql.Connection;

/**
 * Spatial Index utility.
 * @author Nicolas Fortin
 */
public class SpatialIndex {
    /** Utility class */
    private SpatialIndex() {}
    /** While index object cannot be used
     *  We store index in a separate schema */
    public static String SPATIAL_INDEX_SCHEMA = "SPATIAL_INDEX";
    public static String SPATIAL_INDEX_PREFIX = "SI_";

    /*
    public static MVRTreeMap<Long> getRTree(Connection connection, String schema, String table) {
        if(!(connection instanceof JdbcConnection)) {
            throw new IllegalArgumentException("Only for h2 database");
        }
        SessionInterface sess = ((JdbcConnection) connection).getSession();
        if(!(sess instanceof Session)) {
            throw new IllegalArgumentException("Only for local h2 database");
        }
        Session session = (Session)sess;
        Database db = ((Session) sess).getDatabase();
        if(!schema.isEmpty()) {
            schema = schema + "_";
        }
        Table mvTable = db.getSchema(SPATIAL_INDEX_SCHEMA).findTableOrView(session, SPATIAL_INDEX_PREFIX + schema + table);
        if(mvTable instanceof MVTable) {
            //((MVTable)mvTable).getTransaction(sess);
        }
        // open an R-tree map
        //MVRTreeMap<Long> r = ()mvTable.openMap("data",
        //        new MVRTreeMap.Builder<Long>());

    }
    */
}
