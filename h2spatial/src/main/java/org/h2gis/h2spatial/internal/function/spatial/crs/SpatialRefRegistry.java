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
package org.h2gis.h2spatial.internal.function.spatial.crs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.cts.parser.proj.ProjKeyParameters;
import org.cts.registry.Registry;
import org.cts.registry.RegistryException;

/**
 * This class builds a registry based on a spatial_ref_sys table stored in the
 * H2 database.
 *
 * @author Erwan Bocher
 */
public class SpatialRefRegistry implements Registry {

    private Connection connection;
    private static final Pattern regex = Pattern.compile("\\s+");

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
                    v.put(ProjKeyParameters.title, rs.getString(2) + ":" + code);
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
     * @param connection
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
