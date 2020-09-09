/*
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

package org.h2gis.api;

import java.io.IOException;

/**
 * Implement this interface in order to create a {@link org.h2.table.TableBase} in your
 * {@link org.h2.api.TableEngine} implementation.
 * How to use:
 * <ul>
 * <li>Implement this interface with your file driver</li>
 * <li>Implement a FileEngine</li>
 * <li>Add your FileEngine implementation into the DriverManager</li>
 * </ul>
 *
 * @author Nicolas Fortin
 * @author Sylvain PALOMINOS (UBS 2018)
 */
public interface FileDriver {

    /**
     * Returns row count.
     *
     * @return Row count.
     */
    long getRowCount();

    /**
     * @return Estimated row length in bytes
     */
    int getEstimatedRowSize(long rowId);

    /**
     *
     * @return Column count
     */
    int getFieldCount();

    /**
     * Close the file, free resources.
     *
     * @throws IOException Closing error.
     */
    void close() throws IOException;

    /**
     * Return the content of the given row.
     *
     * @param rowId Row index [0-getRowCount()].
     * @return The row content.
     * @throws java.io.IOException Read error.
     */
    Object getField(long rowId, int columnId) throws IOException;

    /**
     * Insert values to the current row.
     *
     * @param values Values to insert.
     * @throws IOException Write error.
     */
    void insertRow(Object[] values) throws IOException;
}
