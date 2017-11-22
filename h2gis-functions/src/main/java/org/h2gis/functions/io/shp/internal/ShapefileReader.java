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

package org.h2gis.functions.io.shp.internal;

import org.locationtech.jts.geom.Geometry;
import org.h2gis.functions.io.utility.ReadBufferManager;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * The general use of this class is: <CODE><PRE>
 *
 * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
 * ShapefileReader r = new ShapefileReader( in ) while (r.hasNext()) { Geometry
 * shape = (Geometry) r.nextRecord().shape() // do stuff } r.close();
 *
 * </PRE></CODE> You don't have to immediately ask for the shape from the record. The
 * record will contain the bounds of the shape and will only read the shape when
 * the shape() method is called. This ShapefileReader.Record is the same object
 * every time, so if you need data from the Record, be sure to copy it.
 *
 * @author jamesm
 * @author aaime
 * @author Ian Schneider
 * @source $URL:
 *         http://svn.geotools.org/geotools/tags/2.3.1/plugin/shapefile/src/org/geotools/data/shapefile/shp/ShapefileReader.java $
 */
public class ShapefileReader {

        private ShapeHandler handler;
        private ShapefileHeader header;
        private FileChannel channel;
        private ReadBufferManager buffer;
        private ShapeType fileShapeType = ShapeType.UNDEFINED;

        /**
         * Creates a new instance of ShapeFile.
         *
         * @param channel
         *            The ReadableByteChannel this reader will use.
         * @throws java.io.IOException
         *             If problems arise.
         * @throws ShapefileException
         *             If for some reason the file contains invalid records.
         */
        public ShapefileReader(FileChannel channel) throws IOException,
                ShapefileException {
                this.channel = channel;
                init();
        }

        // convenience to peak at a header
        /**
         * A short cut for reading the header from the given channel.
         *
         * @param channel
         *            The channel to read from.
         * @throws java.io.IOException
         *             If problems arise.
         * @return A ShapefileHeader object.
         */
        public static ShapefileHeader readHeader(ReadableByteChannel channel) throws IOException {
                ByteBuffer buffer = ByteBuffer.allocateDirect(100);
                if (channel.read(buffer) != 100) {
                        throw new EOFException("Premature end of header");
                }
                buffer.flip();
                ShapefileHeader header = new ShapefileHeader();
                header.read(buffer);
                return header;
        }

        private void init() throws IOException, ShapefileException {
                header = readHeader(channel);
                fileShapeType = header.getShapeType();
                handler = fileShapeType.getShapeHandler();

                // recordHeader = ByteBuffer.allocateDirect(8);
                // recordHeader.order(ByteOrder.BIG_ENDIAN);

                if (handler == null) {
                        throw new IOException("Unsuported shape type:" + fileShapeType);
                }
                buffer = new ReadBufferManager(channel);
        }

        /**
         * Get the header. Its parsed in the constructor.
         *
         * @return The header that is associated with this file.
         */
        public ShapefileHeader getHeader() {
                return header;
        }

        // do important cleanup stuff.
        // Closes channel !
        /**
         * Clean up any resources. Closes the channel.
         *
         * @throws java.io.IOException
         *             If errors occur while closing the channel.
         */
        public void close() throws IOException {
                if (channel != null && channel.isOpen()) {
                        channel.close();
                }
                channel = null;
                header = null;
        }

        /**
         * Fetch the next record information.
         *
         * @param offset
         * @throws java.io.IOException
         * @return The record instance associated with this reader.
         */
        public Geometry geomAt(int offset) throws IOException {

                // need to update position
                buffer.position(offset);

                // record header
                buffer.skip(8);

                // shape record is all little endian
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                // read the type, handlers don't need it
                ShapeType recordType = ShapeType.forID(buffer.getInt());

                // this usually happens if the handler logic is bunk,
                // but bad files could exist as well...
                if (recordType != ShapeType.NULL && recordType != fileShapeType) {
                        throw new IllegalStateException("ShapeType changed illegally from "
                                + fileShapeType + " to " + recordType);
                }

                return handler.read(buffer, recordType);
        }

        /**
         * @param handler
         *            The handler to set.
         */
        public void setHandler(ShapeHandler handler) {
                this.handler = handler;
        }
}
