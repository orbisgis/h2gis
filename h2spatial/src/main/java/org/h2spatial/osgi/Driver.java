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

package org.h2spatial.osgi;

import org.h2spatial.CreateSpatialExtension;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * H2 driver that register spatial type and functions when connecting to a local database.
 * Used only by the OSGi side
 * @author Nicolas Fortin
 */
public class Driver  extends org.h2.Driver {
    private final static AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final Driver INSTANCE = new Driver();
    private static String bundleSymbolicName = "";
    private static String bundleVersion = "";

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        Connection h2Connection = super.connect(url, info);
        if(h2Connection!=null) {
            try {
                if(bundleSymbolicName.isEmpty()) {
                    CreateSpatialExtension.InitSpatialExtension(h2Connection);
                } else {
                    CreateSpatialExtension.InitSpatialExtension(h2Connection,bundleSymbolicName,bundleVersion);
                }
            } catch (SQLException ex) {
                // Maybe user right is insufficient, log only the error
                System.err.println("Spatial features cannot be installed due to the following error :\n"+ex.toString());
            }
        }
        return h2Connection;
    }

    /**
     * @param bundleSymbolicName The new bundle symbolic name value
     */
    public static void setBundleSymbolicName(String bundleSymbolicName) {
        Driver.bundleSymbolicName = bundleSymbolicName;
    }

    /**
     * @param bundleVersion The new bundle version value
     */
    public static void setBundleVersion(String bundleVersion) {
        Driver.bundleVersion = bundleVersion;
    }

    /**
     * Register this Driver into the DriverManager.
     * @return Static driver
     */
    public static synchronized Driver loadSpatial() {
        try {
            if (!REGISTERED.getAndSet(true)) {
                // Remove non-spatial instance from driver manager
                DriverManager.registerDriver(INSTANCE);
                DriverManager.deregisterDriver(org.h2.Driver.load());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return INSTANCE;
    }

    /**
     * Unregister this driver of the DriverManager
     */
    public static synchronized void unloadSpatial() {
        try {
            if (REGISTERED.getAndSet(false)) {
                DriverManager.deregisterDriver(INSTANCE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean acceptsURL(String s) {
        return super.acceptsURL(s);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String s, Properties properties) {
        return super.getPropertyInfo(s,properties);
    }

    @Override
    public int getMajorVersion() {
        return super.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return super.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return super.jdbcCompliant();
    }
}
