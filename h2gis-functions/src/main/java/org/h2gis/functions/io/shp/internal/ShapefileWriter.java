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

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.h2gis.functions.io.utility.WriteBufferManager;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * ShapefileWriter allows for the storage of geometries in esris shp format.
 * During writing, an index will also be created. To create a ShapefileWriter,
 * do something like<br>
 * <code>
 *   GeometryCollection geoms;
 *   File shp = new File("myshape.shp");
 *   File shx = new File("myshape.shx");
 *   ShapefileWriter writer = new ShapefileWriter(
 *     shp.getChannel(),shx.getChannel()
 *   );
 *   writer.write(geoms,ShapeType.ARC);
 * </code>
 * This example assumes that each shape in the collection is a LineString.
 *
 * @author jamesm
 * @author aaime
 * @author Ian Schneider
 *
 * @source $URL:
 *         http://svn.geotools.org/geotools/tags/2.3.1/plugin/shapefile/src/org/geotools/data/shapefile/shp/ShapefileWriter.java $
 */
public class ShapefileWriter {

	private FileChannel shpChannel;

	private FileChannel shxChannel;

	private WriteBufferManager shapeBuffer;

	private WriteBufferManager indexBuffer;

	private ShapeHandler handler;

	private ShapeType type;

	private int offset;

	private int cnt = 0; //number of geometries

    private Envelope bounds;


	/**
	 * Creates a new instance of ShapeFileWriter
	 *
         * @param shpChannel
         * @param shxChannel
         * @throws java.io.IOException
	 */
	public ShapefileWriter(FileChannel shpChannel, FileChannel shxChannel)
			throws IOException {
		this.shpChannel = shpChannel;
		this.shxChannel = shxChannel;
	}

    /**
     * @return The Shape File Channel
     */
    public FileChannel getShpChannel() {
        return shpChannel;
    }

    /**
	 * Write the headers for this shapefile.Use this function before inserting the first geometry, then when all geometries are inserted.
     * @param type Shape type
     * @throws java.io.IOException
     */
	public void writeHeaders(ShapeType type) throws IOException {
		try {
			handler = type.getShapeHandler();
		} catch (ShapefileException se) {
			throw new IOException("Error with type " + type, se);
		}
        if(indexBuffer != null) {
            indexBuffer.flush();
        }
        if(shapeBuffer != null) {
            shapeBuffer.flush();
        }
        long fileLength = shpChannel.position();
        shpChannel.position(0);
        shxChannel.position(0);
		ShapefileHeader header = new ShapefileHeader();
        Envelope writeBounds = bounds;
        if(writeBounds == null) {
            writeBounds = new Envelope();
        }
        indexBuffer = new WriteBufferManager(shxChannel);
        shapeBuffer = new WriteBufferManager(shpChannel);
		header.write(shapeBuffer, type, cnt, (int)(fileLength / 2),
                writeBounds.getMinX(), writeBounds.getMinY(), writeBounds.getMaxX(), writeBounds
						.getMaxY());
		header.write(indexBuffer, type, cnt,
                50 + 4 * cnt, writeBounds.getMinX(),
                writeBounds.getMinY(), writeBounds.getMaxX(), writeBounds.getMaxY());
		offset = 50;
		this.type = type;
	}

	/**
	 * Write a single Geometry to this shapefile. The Geometry must be
	 * compatable with the ShapeType assigned during the writing of the headers.
         *
         * @param g
         * @throws java.io.IOException
         */
	public void writeGeometry(Geometry g) throws IOException {
        if(type == null) {
            throw new IllegalStateException("Header must be written before writeGeometry");
        }
        if(bounds != null) {
            if(g != null) {
                bounds.expandToInclude(g.getEnvelopeInternal());
            }
        } else {
            bounds = g.getEnvelopeInternal();
        }
		int length;
		if (g == null) {
			length = 4;
		} else {
			length = handler.getLength(g);
		}

		length /= 2;

		shapeBuffer.order(ByteOrder.BIG_ENDIAN);
		shapeBuffer.putInt(++cnt);
		shapeBuffer.putInt(length);
		shapeBuffer.order(ByteOrder.LITTLE_ENDIAN);
		if (g == null) {
			shapeBuffer.putInt(0);
		} else {
			shapeBuffer.putInt(type.id);
			handler.write(shapeBuffer, g);
		}

		// write to the shx
		indexBuffer.putInt(offset);
		indexBuffer.putInt(length);
		offset += length + 4;
	}

	/**
	 * Close the underlying Channels.
         *
         * @throws java.io.IOException
         */
	public void close() throws IOException {
		indexBuffer.flush();
		shapeBuffer.flush();
		if (shpChannel != null && shpChannel.isOpen()) {
			shpChannel.close();
		}
		if (shxChannel != null && shxChannel.isOpen()) {
			shxChannel.close();
		}
		shpChannel = null;
		shxChannel = null;
		handler = null;
		indexBuffer = null;
		shapeBuffer = null;
	}

}
