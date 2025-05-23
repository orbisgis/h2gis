/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.crs;


import org.cts.parser.prj.PrjParser;
import org.cts.parser.proj.ProjKeyParameters;
import org.cts.registry.AbstractProjRegistry;
import org.cts.registry.Registry;
import org.cts.registry.RegistryException;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * This class builds a registry based on a spatial_ref_sys table stored in the
 * H2 database.
 *
 * @author Erwan Bocher
 */
public class SpatialRefRegistry  extends AbstractProjRegistry implements Registry {

    private Connection connection;
    private static final Pattern regex = Pattern.compile("\\s+");
    PrjParser prjParser = null;

    @Override
    public String getRegistryName() {
        return "epsg";
    }

    @Override
    public Map<String, String> getParameters(String code) throws RegistryException {
        try {
            PreparedStatement prepStmt = connection.prepareStatement("SELECT proj4text, auth_name FROM SPATIAL_REF_SYS where srid=?");
            prepStmt.setInt(1, Integer.valueOf(code));
            ResultSet rs = prepStmt.executeQuery();
            if (rs.next()) {
                String proj4Text = rs.getString(1);
                String authcode = rs.getString(2) + ":" + code;
                if(proj4Text.isEmpty()){
                    throw new RegistryException("No translation for "+  authcode +" to PROJ format is known");
                }
                String[] tokens = regex.split(proj4Text);
                Map<String, String> v = new HashMap<String, String>();
                for (String token : tokens) {
                    String[] keyValue = token.split("=");
                    if (keyValue.length == 2) {
                        String key = formatKey(keyValue[0]);
                        ProjKeyParameters.checkUnsupported(key);
                        v.put(key, keyValue[1]);
                    } else {
                        String key = formatKey(token);
                        ProjKeyParameters.checkUnsupported(key);
                        v.put(key, null);
                    }
                }
                if (!v.containsKey(ProjKeyParameters.title)) {
                    v.put(ProjKeyParameters.title, authcode);
                }
                prepStmt.close();
                
                return v;
            }
        } catch (SQLException ex) {
            throw new RegistryException("Cannot obtain the CRS parameters", ex);
        }
        return null;
    }

    /**
     * Remove + char if exists
     *
     * @param prjKey represents a proj key parameter
     * @return a new string without + char
     */
    private static String formatKey(String prjKey) {
        String formatKey = prjKey;
        if (prjKey.startsWith("+")) {
            formatKey = prjKey.substring(1);
        }
        return formatKey;
    }

    @Override
    public Set<String> getSupportedCodes() throws RegistryException {
        Statement st;
        try {
            st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT srid from SPATIAL_REF_SYS;");
            Set<String> codes = new HashSet<String>();
            while (rs.next()) {
                codes.add(rs.getString(1));
            }
            st.close();
            return codes;
        } catch (SQLException ex) {
            throw new RegistryException("Cannot load the EPSG registry", ex);
        }
    }

    /**
     * Set the database connection
     *
     * @param connection database     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
