/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Erwan Bocher, CNRS, 2020
 */
public class FileUtilitiesTest {

    @Test
    public void deleteFiles() throws Exception {
        assertThrows(IOException.class, () -> {
            FileUtilities.deleteFiles(null);
        });
        File directory = new File("./target/directory");
        directory.mkdir();
        File tmpFile = File.createTempFile("test", ".txt", directory);
        assertTrue(tmpFile.exists());
        assertTrue(FileUtilities.deleteFiles(directory));
        tmpFile = File.createTempFile("test", ".txt", directory);
        assertTrue(tmpFile.exists());
        assertFalse(FileUtilities.deleteFiles(directory, true));
        directory = new File("./target/directory");
        directory.mkdir();
        File subdirectory = new File(directory.getAbsolutePath() + File.separator + "subdirectory");
        subdirectory.mkdir();
        tmpFile = File.createTempFile("test", ".txt", subdirectory);
        assertTrue(tmpFile.exists());
        assertTrue(FileUtilities.deleteFiles(directory));
        assertFalse(subdirectory.exists());
        assertThrows(IOException.class, () -> {
            File dir = new File("./target/directory");
            dir.mkdir();
            File tmpFileTest = File.createTempFile("test", ".txt", dir);
            assertTrue(tmpFileTest.exists());
            FileUtilities.deleteFiles(tmpFileTest);
        });
    }

    @Test
    public void listFiles() throws Exception {
        assertThrows(IOException.class, () -> {
            FileUtilities.listFiles(null);
        });
        File directory = new File("./target/directory");
        if (directory.exists()) {
            FileUtilities.deleteFiles(directory);
        } else {
            directory.mkdir();
        }
        File tmpFile = File.createTempFile("test", ".txt", directory);
        assertTrue(tmpFile.exists());
        List<String> files = FileUtilities.listFiles(directory);
        assertEquals(1, files.size());
        assertEquals(tmpFile.getAbsolutePath(), files.get(0));
        directory = new File("./target/directory");
        if (directory.exists()) {
            FileUtilities.deleteFiles(directory);
        } else {
            directory.mkdir();
        }
        File subdirectory = new File(directory.getAbsolutePath() + File.separator + "subdirectory");
        subdirectory.mkdir();
        tmpFile = File.createTempFile("test", ".txt", subdirectory);
        assertTrue(tmpFile.exists());
        assertTrue(subdirectory.exists());
        files = FileUtilities.listFiles(directory);
        assertEquals(1, files.size());
        assertEquals(tmpFile.getAbsolutePath(), files.get(0));
    }

    @Test
    public void listFilesExtension() throws Exception {
        assertThrows(IOException.class, () -> {
            FileUtilities.listFiles(null);
        });
        File directory = new File("./target/directory");
        if (directory.exists()) {
            FileUtilities.deleteFiles(directory);
        } else {
            directory.mkdir();
        }
        File tmpFile = File.createTempFile("test", ".txt", directory);
        assertTrue(tmpFile.exists());
        List<String> files = FileUtilities.listFiles(directory, "txt");
        assertEquals(1, files.size());
        assertEquals(tmpFile.getAbsolutePath(), files.get(0));
        directory = new File("./target/directory");
        if (directory.exists()) {
            FileUtilities.deleteFiles(directory);
        } else {
            directory.mkdir();
        }
        File subdirectory = new File(directory.getAbsolutePath() + File.separator + "subdirectory");
        subdirectory.mkdir();
        tmpFile = File.createTempFile("test", ".txt", subdirectory);
        assertTrue(tmpFile.exists());
        assertTrue(subdirectory.exists());
        files = FileUtilities.listFiles(directory, "txt");
        assertEquals(1, files.size());
        assertEquals(tmpFile.getAbsolutePath(), files.get(0));
        files = FileUtilities.listFiles(directory, "shp");
        assertTrue(files.isEmpty());

        directory = new File("./target/directory");
        if (directory.exists()) {
            FileUtilities.deleteFiles(directory);
        } else {
            directory.mkdir();
        }
        tmpFile = File.createTempFile("test", ".TXT", directory);
        assertTrue(tmpFile.exists());
        files = FileUtilities.listFiles(directory, "txt");
        assertEquals(1, files.size());
        assertEquals(tmpFile.getAbsolutePath(), files.get(0));
    }
    
    @Test
    public void zipFolder() throws Exception {
        File directory = new File("./target/directory");
        if (directory.exists()) {
            FileUtilities.deleteFiles(directory);
        } else {
            directory.mkdir();
        }
        File tmpFile = File.createTempFile("test", ".txt", directory);
        assertTrue(tmpFile.exists());
        File outPutZip = new File("./target/output.zip");
        outPutZip.delete();
        FileUtilities.zip(directory, outPutZip);
        assertTrue(outPutZip.exists());
        
        outPutZip = new File("./target/output.zip");
        outPutZip.delete();
        FileUtilities.zip(tmpFile, outPutZip);
        assertTrue(outPutZip.exists());
    }
    
    @Test
    public void zipUnZipFiles() throws Exception {
        File directory = new File("./target/directory");
        if (directory.exists()) {
            FileUtilities.deleteFiles(directory);
        } else {
            directory.mkdir();
        }
        File tmpFile = File.createTempFile("test", ".txt", directory);
        assertTrue(tmpFile.exists());
        File outPutZip = new File("./target/output.zip");
        outPutZip.delete();
        FileUtilities.zip(new File[]{tmpFile}, outPutZip);
        tmpFile.delete();
        assertTrue(outPutZip.exists());        
        FileUtilities.unzipFile(outPutZip, directory);
        assertTrue(tmpFile.exists()); 
    }   
    
}
