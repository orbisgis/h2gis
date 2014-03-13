package org.h2gis.drivers;

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
}
