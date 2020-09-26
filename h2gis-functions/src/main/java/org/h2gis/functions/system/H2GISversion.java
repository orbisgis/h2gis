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
package org.h2gis.functions.system;

import org.h2gis.api.DeterministicScalarFunction;
import org.h2gis.functions.factory.H2GISFunctions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Return the current version of H2GIS stored in the manifest
 * 
 * @author Erwan Bocher
 */
public class H2GISversion extends DeterministicScalarFunction{

    
    public H2GISversion(){
        addProperty(PROP_REMARKS, "Returns H2GIS version number");
    }
    
    
    @Override
    public String getJavaStaticMethod() {
        return "geth2gisVersion";
    }
    
    /**
     * Return the H2GIS version available in the version txt file Otherwise
     * return unknown
     *
     * @return
     */
    public static String geth2gisVersion() {
        try (InputStream fs = H2GISFunctions.class.getResourceAsStream("/org/h2gis/functions/system/version.txt")) {
            BufferedReader bufRead = new BufferedReader(new InputStreamReader(fs));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = bufRead.readLine()) != null) {
                builder.append(line).append(" ");
            }
            return builder.toString();

        } catch (IOException ex) {
            return "unknown";
        }
    }
    
    
}
