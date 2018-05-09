/**
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

package org.h2gis.utilities.trigger;

import org.h2.api.Trigger;
import org.h2gis.utilities.TableLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This trigger is used to track modifications on tables by inserting notifications into a temporary table.
 * The table H2GIS_SCHEMA.UPDATE_TRIGGERS contain the list of created triggers.
 * The table H2GIS_SCHEMA.UPDATE_NOTIFICATIONS contain the list of updates related to triggers.
 * When this trigger is attached to a table, a line is inserted in H2GIS_SCHEMA.UPDATE_NOTIFICATIONS each time this table is updated.
 * @author Nicolas Fortin
 */
public class UpdateTrigger implements Trigger {
    private int idTrigger;
    public static final String TRIGGER_SCHEMA = "H2GIS_SCHEMA";
    public static final String TRIGGER_TABLE = "UPDATE_TRIGGERS";
    public static final String NOTIFICATION_TABLE = "UPDATE_NOTIFICATIONS";

    @Override
    public void close() throws SQLException {
    }

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException {
        Statement st = conn.createStatement();
        final TableLocation triggerTable = new TableLocation(TRIGGER_SCHEMA, TRIGGER_TABLE);
        final TableLocation notificationTable = new TableLocation(TRIGGER_SCHEMA, NOTIFICATION_TABLE);
        try {
            st.execute("create schema if not exists "+TRIGGER_SCHEMA);
            st.execute("create temporary table if not exists "+notificationTable+" ( id BIGINT PRIMARY KEY" +
                    " AUTO_INCREMENT, idtrigger int)");
            st.execute("create index if not exists triggerindex on "+notificationTable+"(idtrigger)");
            st.execute("create temporary table if not exists "+triggerTable+"(idtrigger int primary key" +
                    " auto_increment, trigger_name varchar unique, schema_name VARCHAR, table_name varchar)");
            PreparedStatement preparedStatement = conn.prepareStatement("select idtrigger, trigger_name from "+triggerTable+" where trigger_name = ?");
            preparedStatement.setString(1, triggerName);
            ResultSet rs = preparedStatement.executeQuery();
            try {
                if(rs.next()) {
                    idTrigger = rs.getInt(1);
                } else {
                    // Insert this new trigger
                    preparedStatement.close();
                    preparedStatement = conn.prepareStatement("insert into "+triggerTable+"(trigger_name, schema_name,table_name) VALUES (?,?,?)");
                    preparedStatement.setString(1, triggerName);
                    preparedStatement.setString(2, schemaName);
                    preparedStatement.setString(3, tableName);
                    preparedStatement.execute();

                    preparedStatement.close();
                    preparedStatement = conn.prepareStatement("select idtrigger, trigger_name from "+triggerTable+" where trigger_name = ?");
                    preparedStatement.setString(1, triggerName);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if(resultSet.next()) {
                            idTrigger = resultSet.getInt(1);
                        } else {
                            throw new SQLException("Cannot get inserted trigger id");
                        }
                    }finally {
                        resultSet.close();
                    }
                }
            } finally {
                rs.close();
                preparedStatement.close();
            }
        } finally {
            st.close();
        }
    }

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        final TableLocation notificationTable = new TableLocation(TRIGGER_SCHEMA, NOTIFICATION_TABLE);
        try {
            PreparedStatement st = conn.prepareStatement("INSERT INTO "+notificationTable+"(idtrigger) VALUES(?)");
            st.setInt(1, idTrigger);
            try {
                st.execute();
            } finally {
                st.close();
            }
        } catch (Exception ex) {
            // Ignore exception to not interfere with database
        }
    }

    @Override
    public void remove() throws SQLException {
    }
}
