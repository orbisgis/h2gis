/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.drivers.kml;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2gis.drivers.DriverManager;
import org.h2gis.drivers.shp.SHPRead;

import org.h2gis.h2spatial.CreateSpatialExtension;
import org.h2gis.h2spatial.ut.SpatialH2UT;

/**
 *
 * @author ebocher
 */
public class DemoKml {

    public static void main(String[] args) {
        // Keep a connection alive to not close the DataBase on each unit test
        Connection connection = null;
        try {
            connection = SpatialH2UT.createSpatialDataBase("kmlWriter_test");
            CreateSpatialExtension.registerFunction(connection.createStatement(), new DriverManager(), "");
            CreateSpatialExtension.registerFunction(connection.createStatement(), new SHPRead(), "");
            CreateSpatialExtension.registerFunction(connection.createStatement(), new KMLWrite(), "");
            Statement stat = connection.createStatement();
            stat.execute("CALL FILE_TABLE('/tmp/route_4326.shp', 'route_4326'); ");
            stat.execute("CREATE TABLE route as select st_setsrid(the_geom, 4326) as the_geom,"
                    + "ID, PREC_PLANI, PREC_ALTI, NATURE, NUMERO,  IMPORTANCE, "
                    + "CL_ADMIN, GESTION, MISE_SERV, IT_VERT, IT_EUROP, FICTIF, FRANCHISST, "
                    + "LARGEUR, NOM_ITI, NB_VOIES, POS_SOL, SENS, INSEECOM_G, "
                    + "INSEECOM_D, CODEVOIE_G, CODEVOIE_D,  TYP_ADRES, BORNEDEB_G, "
                    + "BORNEDEB_D, BORNEFIN_G, BORNEFIN_D, ETAT, Z_INI, Z_FIN from route_4326; ");
            stat.execute("CALL KMLWRITE('/tmp/route_4326.kml', 'route'); ");
            stat.close();
        } catch (SQLException ex) {
            Logger.getLogger(DemoKml.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DemoKml.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(DemoKml.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
