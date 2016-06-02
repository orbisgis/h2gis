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

package org.h2gis.functions.io;

import java.io.IOException;

/**
 * Implement this interface in order to create a {@link org.h2gis.drivers.file_table.H2Table} in your
 * {@link org.h2.api.TableEngine} implementation.
 * How to use:
 * <ul>
 * <li>Implement this interface with your file driver</li>
 * <li>Implement {@link org.h2gis.drivers.file_table.FileEngine}</li>
 * <li>Add your {@link org.h2gis.drivers.file_table.FileEngine} implementation into the {@link org.h2gis.drivers.DriverManager}</li>
 * </ul>
 * @author Nicolas Fortin
 */
public interface FileDriver {

    /**
     * @return Row count
     */
    long getRowCount();

    /**
     * Close the file, free resources.
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * @param rowId Row index [0-getRowCount()[
     * @return The row content
     * @throws java.io.IOException Read error
     */
    public Object[] getRow(long rowId) throws IOException;
   
    /**
     * Insert values to the current row
     * @param values
     * @throws IOException 
     */
    public void insertRow(Object[] values) throws IOException;
}
