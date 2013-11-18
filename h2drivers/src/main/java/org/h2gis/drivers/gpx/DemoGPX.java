/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.h2gis.drivers.gpx;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2gis.drivers.gpx.model.GpxParser;
import org.h2gis.h2spatial.ut.SpatialH2UT;

/**
 *
 * @author ebocher
 */
public class DemoGPX {

    public static void main(String[] s) throws Exception {
        String DB_NAME = "_testgpx";
        Connection connection = null;
        try {
            connection = SpatialH2UT.createSpatialDataBase(DB_NAME);
            GpxParser gpd = new GpxParser();
            boolean response = gpd.read(new File("/home/ebocher/Documents/data/europe.poi.gpx/europe_Shopping.gpx"), "test", connection);
            System.out.println("Etat de la lecture du fichier : " + response);
        } catch (SQLException ex) {
            throw new SQLException(ex);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
