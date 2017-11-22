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
import org.h2gis.functions.io.utility.WriteBufferManager;

import java.io.IOException;

/** A ShapeHandler defines what is needed to construct and persist geometries
 * based upon the shapefile specification.
 * @author aaime
 * @author Ian Schneider
 * @source $URL: http://svn.geotools.org/geotools/tags/2.3.1/plugin/shapefile/src/org/geotools/data/shapefile/shp/ShapeHandler.java $
 *
 */
public interface ShapeHandler {
  /** Get the ShapeType of this handler.
   * @return The ShapeType.
   */
  ShapeType getShapeType();

  /** Read a geometry from the ByteBuffer. The buffer's position, byteOrder, and limit
   * are set to that which is needed. The record has been read as well as the shape
   * type integer. The handler need not worry about reading unused information as
   * the ShapefileReader will correctly adjust the buffer position after this call.
   * @param buffer The ByteBuffer to read from.
   * @return A geometry object.
 * @throws java.io.IOException
   */
  Geometry read(ReadBufferManager buffer, ShapeType type) throws IOException;

  /** Write the geometry into the ByteBuffer. The position, byteOrder, and limit are
   * all set. The handler is not responsible for writing the record or
   * shape type integer.
   * @param shapeBuffer The ByteBuffer to write to.
   * @param geometry The geometry to write.
   */
  void write(WriteBufferManager shapeBuffer, Object geometry) throws IOException;

  /** Get the length of the given geometry Object in <b>bytes</b> not 16-bit words.
   * This is easier to keep track of, since the ByteBuffer deals with bytes. <b>Do
   * not include the 8 bytes of record.</b>
   * @param geometry The geometry to analyze.
   * @return The number of <b>bytes</b> the shape will take up.
   */
  int getLength(Object geometry);
}
