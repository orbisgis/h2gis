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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.h2gis.api.DeterministicScalarFunction;

/**
 * Return the current version of H2GIS stored in the manifest
 * 
 * @author Erwan Bocher
 */
public class H2GISversion extends DeterministicScalarFunction{

    
    public H2GISversion(){
        addProperty(PROP_REMARKS, "Returns H2GISGIS version number");
    }
    
    
    @Override
    public String getJavaStaticMethod() {
        return "geth2gisVersion";
    }
    
    /**
     * Return the H2GIS version available in the MANISFEST file
     * Otherwise return unknown
     * 
     * @return 
     */
    public static String geth2gisVersion() {
        URLClassLoader cl = (URLClassLoader) H2GISversion.class.getClassLoader();
        try {
            URL url = cl.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(url.openStream());
            Attributes att = manifest.getMainAttributes();
            return att.getValue("bundle-version");

        } catch (IOException e) {
            return "unknown";
        }
    }
    
    
}
