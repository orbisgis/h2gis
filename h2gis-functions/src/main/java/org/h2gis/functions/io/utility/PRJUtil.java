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

package org.h2gis.functions.io.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.cts.parser.prj.PrjKeyParameters;
import org.cts.parser.prj.PrjParser;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to manage PRJ file
 * 
 * @author Erwan Bocher
 */
public class PRJUtil {
    private static final Logger log = LoggerFactory.getLogger(PRJUtil.class);
    
    
    /**
     * Return the SRID value stored in a prj file
     * 
     * If the the prj file 
     * - is null,
     * - is empty
     * then a default srid equals to 0 is added.
     * 
     * @param prjFile
     * @return
     * @throws IOException 
     */
    public static int getSRID(File prjFile) throws IOException {
        int srid = 0;
        if (prjFile == null) {
            log.debug("This prj file is null. \n A default srid equals to 0 will be added.");
        } else {
            PrjParser parser = new PrjParser();
            String prjString = readPRJFile(prjFile);
            if (!prjString.isEmpty()) {
                Map<String, String> p = parser.getParameters(prjString);
                String authorityWithCode = p.get(PrjKeyParameters.REFNAME);
                if (authorityWithCode != null) {
                    String[] authorityNameWithKey = authorityWithCode.split(":");
                    srid = Integer.valueOf(authorityNameWithKey[1]);
                }
            }
            else{
                log.debug("The prj is empty. \n A default srid equals to 0 will be added.");
            }
        }
        return srid;
    }
   
    /**
     * Get a valid SRID value from a prj file.
     * If the the prj file 
     * - is null,
     * - doesn't contain a valid srid code,
     * - is empty     * 
     * then a default srid equals to 0 is added.
     * 
     * @param connection
     * @param prjFile
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static int getValidSRID(Connection connection, File prjFile) throws SQLException, IOException {
        int srid = getSRID(prjFile);
        if(!isSRIDValid(srid, connection)){
            srid = 0;
        }
        return srid;
    }
    
    /**
     * Return the content of the PRJ file as a single string
     *
     * @param prjFile
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static String readPRJFile(File prjFile) throws FileNotFoundException, IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(prjFile);
            BufferedReader r = new BufferedReader(new InputStreamReader(fis, Charset.defaultCharset()));
            StringBuilder b = new StringBuilder();
            while (r.ready()) {
                b.append(r.readLine());
            }
            return b.toString();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
    
    
    /**
     * Write a prj file according the SRID code of an input table
     * @param connection database connection
     * @param location input table name
     * @param geomField geometry field name
     * @param fileName path of the prj file
     * @throws SQLException
     * @throws FileNotFoundException 
     */   
    public static void writePRJ(Connection connection, TableLocation location, String geomField, File fileName) throws SQLException, FileNotFoundException {
        int srid = SFSUtilities.getSRID(connection, location, geomField);
        writePRJ(connection, srid, fileName);
    }
    
    
     /**
     * Write a prj file according a given SRID code.
     * @param connection database connection
     * @param srid srid code
     * @param fileName path of the prj file
     * @throws SQLException
     * @throws FileNotFoundException 
     */   
    public static void writePRJ(Connection connection, int srid, File fileName) throws SQLException, FileNotFoundException {
        if (srid != 0) {
            StringBuilder sb = new StringBuilder("SELECT SRTEXT FROM ");
            sb.append("PUBLIC.SPATIAL_REF_SYS ").append(" WHERE SRID = ?");
            PreparedStatement ps = connection.prepareStatement(sb.toString());
            ps.setInt(1, srid);
            PrintWriter printWriter = null;
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
                if (rs.next()) {
                    printWriter = new PrintWriter(fileName);
                    printWriter.println(rs.getString(1));
                } else {
                    log.warn("This SRID { "+ srid +" } is not supported. \n The PRJ file won't be created.");
                }
            } finally {
                if (printWriter != null) {
                    printWriter.close();
                }
                if (rs != null) {
                    rs.close();
                }
                ps.close();
            }
        }
    }
    
    
    /**
     * This method checks if a SRID value is valid according a list of SRID's
     * avalaible on spatial_ref table of the datababase.
     * 
     * @param srid
     * @param connection
     * @return 
     * @throws java.sql.SQLException 
     */
    public static boolean isSRIDValid(int srid, Connection connection) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String queryCheck = "SELECT count(SRID) from PUBLIC.SPATIAL_REF_SYS WHERE SRID = ?";
        try {
            ps = connection.prepareStatement(queryCheck);
            ps.setInt(1, srid);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) != 0;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
        return false;
    }
    
}
