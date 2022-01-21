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

package org.h2gis.functions;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.functions.spatial.SpatialFunctionTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Interface to simplify the declaration of SQL test classes.
 *
 * @author Sylvain PALOMINOS
 */
public abstract class ASqlTest {
    protected static Connection connection;
    protected Statement st;
    protected static GeometryFactory FACTORY;
    protected static WKTReader WKT_READER;
    public static final double TOLERANCE = 10E-10;

    @BeforeAll
    public static void beforeAll() throws Exception {
        connection = H2GISDBFactory.createSpatialDataBase(SpatialFunctionTest.class.getSimpleName());
        FACTORY = new GeometryFactory();
        WKT_READER = new WKTReader();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void afterEach() throws Exception {
        st.close();
    }
}
