/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.h2gis.drivers.shp.internal;

import com.vividsolutions.jts.geom.Geometry;
import org.h2gis.drivers.utility.ReadBufferManager;
import org.h2gis.drivers.utility.WriteBufferManager;

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
