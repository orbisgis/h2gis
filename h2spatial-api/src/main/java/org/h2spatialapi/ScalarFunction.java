/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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

package org.h2spatialapi;

/**
 * Scalar function interface.
 * Scalar function in H2 can be defined through CREATE ALIAS, but in an OSGi context the class java name is not sufficient.
 * The full declaration of java name in H2 through osgi is BundleSymbolicName:BundleVersion:BinaryJavaName
 * Registering this interface as an OSGi service will add this function in h2spatial linked with a DataSource service.
 * @author Nicolas Fortin
 */
public interface ScalarFunction extends Function {
    /**
     * Returns Java name of static methods in this class to expose in database,
     * theses methods are under the same alias but with different number of arguments.
     * @return The Java name of static methods
     */
    String getJavaStaticMethod();
}
