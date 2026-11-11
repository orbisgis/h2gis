/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.graalvm;

import org.h2gis.functions.system.H2GISversion;
import org.locationtech.jts.JTSVersion;

import java.sql.SQLException;

/**
 * Versions of the components built into the native library, and the
 * explanation of the errors that come from a database written by another
 * version of H2. Kept apart from {@link GraalCInterface} to be tested on a JVM.
 *
 * @author Erwan Bocher, CNRS
 */
final class NativeInfo {

    /**
     * H2 error codes of a database file that this version of H2 cannot read:
     * 90030 FILE_CORRUPTED_1 (the pages of some other versions are reported
     * as corrupted, e.g. a database written by H2 2.4.240 read by 2.5.252,
     * as both write the same file format number) and 90048
     * FILE_VERSION_ERROR_1 (unsupported file format).
     */
    private static final int[] OTHER_VERSION_ERRORS = {90030, 90048};

    private NativeInfo() {
    }

    /**
     * @return the version of H2, e.g. "2.5.252"
     */
    static String h2Version() {
        return org.h2.engine.Constants.VERSION;
    }

    /**
     * @return the version of H2GIS, e.g. "2.2.6", "unknown" if not found
     */
    static String h2gisVersion() {
        String version = H2GISversion.geth2gisVersion();
        return version == null || version.trim().isEmpty() ? "unknown" : version.trim();
    }

    /**
     * @return the version of JTS, e.g. "1.20.0"
     */
    static String jtsVersion() {
        return JTSVersion.CURRENT_VERSION.toString();
    }

    /**
     * Versions returned by h2gis_get_versions:
     * {"cApi":4,"h2gis":"2.2.6","h2":"2.5.252","jts":"1.20.0"}.
     *
     * @param cApiVersion the version of the C interface
     * @return a JSON object
     */
    static String versionsJson(int cApiVersion) {
        return "{\"cApi\":" + cApiVersion
                + ",\"h2gis\":" + jsonString(h2gisVersion())
                + ",\"h2\":" + jsonString(h2Version())
                + ",\"jts\":" + jsonString(jtsVersion()) + "}";
    }

    /**
     * Explanation to add to the message of a failed connection, when the
     * database may have been written by another version of H2.
     *
     * @param e the exception of the connection
     * @return the explanation, starting with a space, or "" if it does not apply
     */
    static String connectionHint(Throwable e) {
        if (!isOtherVersionError(e)) {
            return "";
        }
        return " The database may have been written by another version of H2: this library embeds H2 "
                + h2Version() + " (H2GIS " + h2gisVersion() + "), which cannot read the files of some other "
                + "versions. Open it with the version of H2 that wrote it and migrate it: SCRIPT TO a file, "
                + "then RUNSCRIPT FROM it in a database created by this library (or use H2MigrationTool).";
    }

    /**
     * @param e an exception
     * @return true if it, or one of its causes, is an H2 error of a file of another version
     */
    static boolean isOtherVersionError(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof SQLException) {
                int code = ((SQLException) t).getErrorCode();
                for (int otherVersionError : OTHER_VERSION_ERRORS) {
                    if (code == otherVersionError) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String jsonString(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') {
                sb.append('\\').append(c);
            } else if (c < 0x20) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}