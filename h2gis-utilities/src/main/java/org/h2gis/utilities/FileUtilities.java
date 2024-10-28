/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utilities for file(s) and directory
 *
 * @author Erwan Bocher, CNRS, 2020
 */
public class FileUtilities {

    /**
     * Use this method to delete all files in directory recursively without
     * deleting the root directory
     *
     * @param directory the directory
     * @return true if the directory already exists
     * @throws IOException Throw an exception if the files cannot be deleted
     */
    public static boolean deleteFiles(File directory) throws IOException {
        return deleteFiles(directory, false);
    }

    /**
     * Use this method to delete all files in directory recursively. The root
     * directory can be deleted
     *
     * @param directory the directory
     * @param delete true to delete the root directory
     * @return true if the directory already exists
     * @throws IOException Throw an exception if the files cannot be deleted
     */
    public static boolean deleteFiles(File directory, boolean delete) throws IOException {
        if (directory == null) {
            throw new IOException("The directory cannot be null");
        }
        if (!directory.isDirectory()) {
            throw new IOException("The input path must be a directory");
        }
        Path pathToBeDeleted = directory.toPath();
        try (Stream<Path> walk = Files.walk(pathToBeDeleted)) {
            if (delete) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } else {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .filter(item -> !item.toPath().equals(pathToBeDeleted))
                        .forEach(File::delete);
            }
        }
        return Files.exists(pathToBeDeleted);
    }

    /**
     * List all files
     *
     * @param directory the directory
     * @param extension file extension, txt, geojson, shapefile
     * @return path of the files
     * @throws IOException Throw an exception if the files cannot be listed
     */
    public static List<String> listFiles(File directory, String extension) throws IOException {
        if (directory == null) {
            throw new IOException("The directory cannot be null");
        }
        if (!directory.isDirectory()) {
            throw new IOException("The input path must be a directory");
        }

        if (extension == null || extension.isEmpty()) {
            throw new IOException("The file extension cannot be null or empty");
        }
        Path pathToBeDeleted = directory.toPath();
        List<String> result = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(pathToBeDeleted)) {
            result = walk.map(x -> x.toAbsolutePath().toString())
                    .filter(f -> f.toLowerCase().endsWith("." + extension)).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * List all files in a directory
     *
     * @param directory the directory
     * @return path of the files
     * @throws IOException Throw an exception if the files cannot be listed
     */
    public static List<String> listFiles(File directory) throws IOException {
        if (directory == null) {
            throw new IOException("The directory cannot be null");
        }
        if (!directory.isDirectory()) {
            throw new IOException("The input path must be a directory");
        }
        Path pathToBeDeleted = directory.toPath();
        List<String> result = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(pathToBeDeleted)) {
            result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toAbsolutePath().toString()).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * Unzip to a directory
     *
     * @param zipFile the zipped file
     * @throws FileNotFoundException
     * @throws IOException Throw an exception if the parent file fails
     */
    public static void unzip(File zipFile) throws IOException {
        Path parentDir = zipFile.toPath().getParent();
        String fileName = zipFile.getName();
        String withoutExtension = fileName.substring(0, fileName.lastIndexOf(".")).replace(".", "_");
        File targetDir = parentDir.resolve(withoutExtension).toFile();
        if(!targetDir.exists()){
            targetDir.mkdir();
        }
        unzip(zipFile, targetDir);
    }
    
    /**
     * Unzip to a directory
     *
     * @param zipFile the zipped file
     * @param directory the directory to unzi the file
     * @throws FileNotFoundException
     * @throws IOException Throw an exception if the file directory is not accessible
     */
    public static void unzip(File zipFile, File directory) throws IOException {
        if (directory == null) {
            throw new IOException("The directory cannot be null");
        }
        if (!directory.isDirectory()) {
            throw new IOException("The input path must be a directory");
        }
        
        if (zipFile == null) {
            throw new IOException("The file to zip cannot be null");
        }
        else if (!zipFile.exists()){
            throw new IOException("The file to zip cannot doesn't exist");
        }
        
        if(!isExtensionWellFormated(zipFile, "zip")){
            throw new IOException("The extension of the file to zip must be .zip");
        }
        if(zipFile.equals(directory)){
            throw new IOException("The destination file must be different than the zip file");
        }
        ZipInputStream zis = null;
        try {
            byte[] buffer = new byte[1024];
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(directory, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
        }
    }

    /**
     *
     * @param destinationDir target directory
     * @param zipEntry zipentry
     * @return the zipentry content
     * @throws IOException Throw an exception if the file directory is not accessible
     */
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target directory: " + zipEntry.getName());
        }

        return destFile;
    }
    
     /**
     * Zips the specified files
     *
     * @param filesToZip
     * @param outFile
     */
    public static void zip(File[] filesToZip, File outFile) throws IOException {
        if (filesToZip == null) {
            throw new IOException("The file to zip cannot be null");
        }
        if (outFile == null ) {
            throw new IOException("The destination file to zip cannot be null");
        }
        else if (outFile.exists()){
            throw new IOException("The destination file to zip already exist");
        }      
        if(!isExtensionWellFormated(outFile, "zip")){
            throw new IOException("The extension of the file to zip must be .zip");
        }
        
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(
                    new FileOutputStream(outFile)));
            int BUF_SIZE = 1024 * 64;
            byte[] data = new byte[BUF_SIZE];
            for (File file : filesToZip) {
                if (file.exists()) {
                    BufferedInputStream in = null;
                    try {
                        in = new BufferedInputStream(new FileInputStream(file),
                                BUF_SIZE);
                        out.putNextEntry(new ZipEntry(file.getName()));
                        int count = in.read(data, 0, BUF_SIZE);
                        while (count != -1) {
                            out.write(data, 0, count);
                            count = in.read(data, 0, BUF_SIZE);
                        }
                        out.closeEntry(); // close each entry
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                }
            }
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Zips the specified file or folder
     *
     * @param toZip
     */
    public static void zip(File toZip) throws IOException {
        Path parentDir = toZip.toPath().getParent();
        String fileName = toZip.getName();
        int lastIndex = fileName.lastIndexOf(".");
        if(lastIndex!=-1) {
            fileName = fileName.substring(0, lastIndex).replace(".", "_");
        }
        Path targetDir = parentDir.resolve(fileName+".zip");
        zip(toZip, targetDir.toFile());
    }

    /**
     * Zips the specified file or folder
     *
     * @param toZip
     * @param outFile
     */
    public static void zip(File toZip, File outFile) throws IOException {
        if (toZip == null || !toZip.exists()) {
            throw new IOException("The file to zip cannot be null");
        }
        else if (!toZip.exists()){
            throw new IOException("The file to zip cannot doesn't exist");
        }
        if (outFile == null ) {
            throw new IOException("The destination file to zip cannot be null");
        }
        else if (outFile.exists()){
            throw new IOException("The destination file to zip already exist");
        }      
        if(!isExtensionWellFormated(outFile, "zip")){
            throw new IOException("The extension of the file to zip must be .zip");
        }
        if(toZip.equals(outFile)){
            throw new IOException("The destination file must be different than the input file");
        }
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(
                    new FileOutputStream(outFile)));
            int BUF_SIZE = 1024 * 64;
            byte[] data = new byte[BUF_SIZE];
            ArrayList<File> listToZip = new ArrayList<File>();
            listToZip.add(toZip);
            while (listToZip.size() > 0) {
                File file = listToZip.remove(0);
                if (file.isDirectory()) {
                    File[] children = file.listFiles();
                    if(children!=null) {
                        listToZip.addAll(Arrays.asList(children));
                    }
                } else {
                    BufferedInputStream in = null;
                    try {
                        in = new BufferedInputStream(new FileInputStream(file),
                                BUF_SIZE);
                        out.putNextEntry(new ZipEntry(getRelativePath(toZip, file)));
                        int count = in.read(data, 0, BUF_SIZE);
                        while (count != -1) {
                            out.write(data, 0, count);
                            count = in.read(data, 0, BUF_SIZE);
                        }
                        out.closeEntry(); // close each entry
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                }
            }
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Get the relative path to file, according to the path to base
     *
     * @param base parent file
     * @param file file to locate according the parent file
     * @return path of the file
     */
    public static String getRelativePath(File base, File file) {
        String absolutePath = file.getAbsolutePath();
        String path = absolutePath.substring(base.getAbsolutePath().length());
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
    
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

}
