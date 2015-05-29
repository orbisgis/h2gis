/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.drivers.utility;

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
     * 
     * @param connection
     * @param prjFile
     * @return
     * @throws SQLException
     * @throws IOException 
     */
    public static int getSRID(Connection connection, File prjFile) throws SQLException, IOException{
        int srid = 0;
        if (prjFile == null) {
            log.warn("This shapefile has no prj. \n A default srid equals to 0 will be added.");
        } else {
            PrjParser parser = new PrjParser();
            String prjString = readPRJFile(prjFile);
            if(!prjString.isEmpty()){
            Map<String, String> p = parser.getParameters(prjString);
            String authorityWithCode = p.get(PrjKeyParameters.REFNAME);
            if (authorityWithCode != null) {
                String[] authorityNameWithKey = authorityWithCode.split(":");
                String queryCheck = "SELECT count(SRID) from PUBLIC.SPATIAL_REF_SYS WHERE SRID = ?";
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = connection.prepareStatement(queryCheck);
                    srid = Integer.valueOf(authorityNameWithKey[1]);
                    ps.setInt(1, srid);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        if (rs.getInt(1) == 0) {
                            log.warn("The prj doesn't contain a valid srid code. \n A default srid equals to 0 will be added.");
                        }
                    }
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                }
            }
            }
            else{
                 log.warn("The prj is empty. \n A default srid equals to 0 will be added.");
            }

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
     * Write a prj file according a given SRID code.
     * @param connection
     * @param location
     * @param geomField
     * @param fileName
     * @throws SQLException
     * @throws FileNotFoundException 
     */   
    public static void writePRJ(Connection connection, TableLocation location, String geomField, File fileName) throws SQLException, FileNotFoundException {
        int srid = SFSUtilities.getSRID(connection, location, geomField);
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
}
