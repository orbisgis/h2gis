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

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Some utilities
 * 
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class FileUtil {
    
    /**
     * Check if the file is well formatted regarding an extension prefix.
     * Check also if the file doesn't exist.
     * 
     * @param file
     * @param prefix
     * @return
     * @throws SQLException 
     * @throws java.io.FileNotFoundException 
     */
    public static boolean isFileImportable(File file, String prefix) throws SQLException, FileNotFoundException{
        if (isExtensionWellFormated(file, prefix)) {
            if (file.exists()) {
                return true;
            } else {
                throw new FileNotFoundException("The following file does not exists:\n" + file.getPath());
            }
        } else {
            throw new SQLException("Please use " + prefix + " extension.");
        }
    }
    
    /**
     * Check if the file has the good extension
     * @param file
     * @param prefix
     * @return 
     */
    public static boolean isExtensionWellFormated(File file, String prefix) {
        String path = file.getAbsolutePath();
        String extension = "";
        int i = path.lastIndexOf('.');
        if (i >= 0) {
            extension = path.substring(i + 1);
        }
        return extension.equalsIgnoreCase(prefix);
    }

    /**
     * @param file File, may not exists
     * @return File name, without extension
     */
    public static String getBaseName(File file) {
        String name = file.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }

    /**
     * Given a folder, a base file name and a set of extension. Return all files that match the base file name and
     * one of the extension ignoring case. Not recursive.
     * @param parentFolder Parent folder that contain all files.
     * @param nameWithoutExt Base file name (without ext) {@link #getBaseName(File)}
     * @param extensions Extensions to search
     * @return Map, key is extension lowercase, value is file that match the base file name
     */
    public static Map<String, File> fetchFileByIgnoreCaseExt(File parentFolder, String nameWithoutExt,String...
            extensions) {
        Set<String>
                lookingExt = new HashSet<String>(extensions.length);
        for(String ext : extensions) {
            lookingExt.add(ext.toLowerCase());
        }
        Map<String, File> result = new HashMap<String, File>(lookingExt.size());
        File[] filesInParentFolder = parentFolder.listFiles();
        if(filesInParentFolder != null) {
            for(File otherFile : filesInParentFolder) {
                String otherFileName = otherFile.getName();
                if(otherFileName.startsWith(nameWithoutExt + ".")) {
                    String fileExt =  otherFileName.substring(otherFileName.lastIndexOf(".") + 1);
                    if(lookingExt.contains(fileExt.toLowerCase())) {
                        result.put(fileExt.toLowerCase(), otherFile);
                    }
                }
            }
        }
        return result;
    }
}
