/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.h2spatial.osgi;

import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatialapi.Function;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Keep connection open and track arrival and departure of h2spatial OSGi functions
 * @author Nicolas Fortin
 */
public class FunctionTracker extends ServiceTracker<Function, Function> {
    private DataSource dataSource;

    /**
     * Constructor
     * @param dataSource Active datasource
     * @param bundleContext BundleContext
     * @throws SQLException
     */
    public FunctionTracker(DataSource dataSource, BundleContext bundleContext) throws SQLException {
        super(bundleContext,Function.class,null);
        this.dataSource = dataSource;
    }

    @Override
    public void open() {
        super.open();
    }

    @Override
    public Function addingService(ServiceReference<Function> reference) {
        Function function = super.addingService(reference);
        try {
            Connection connection = dataSource.getConnection();
            try {
                CreateSpatialExtension.registerFunction(connection.createStatement(), function, ""); //bundle.getSymbolicName() + ":" + bundle.getVersion().toString() + ":"
            }finally {
                connection.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        return function;
    }

    @Override
    public void removedService(ServiceReference<Function> reference, Function service) {
        try {
            Connection connection = dataSource.getConnection();
            try {
                CreateSpatialExtension.unRegisterFunction(connection.createStatement(), service);
            }finally {
                connection.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        super.removedService(reference, service);
    }
}
