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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.h2gis.functions.io.utility.ReadBufferManager;
import org.h2gis.functions.io.utility.WriteBufferManager;

import java.io.IOException;


/**
 * Wrapper for a Shapefile point.
 *
 * @author aaime
 * @author Ian Schneider
 * @source $URL: http://svn.geotools.org/geotools/tags/2.3.1/plugin/shapefile/src/org/geotools/data/shapefile/shp/PointHandler.java $
 *
 */
public class PointHandler implements ShapeHandler {

  final ShapeType shapeType;
  GeometryFactory geometryFactory = new GeometryFactory();

  public PointHandler(ShapeType type) throws ShapefileException {
    if ((type != ShapeType.POINT) && (type != ShapeType.POINTM) && (type != ShapeType.POINTZ)) { // 2d, 2d+m, 3d+m
      throw new ShapefileException(
      "PointHandler constructor: expected a type of 1, 11 or 21");
    }

    shapeType = type;
  }

  public PointHandler() {
    shapeType = ShapeType.POINT; //2d
  }

  /**
   * Returns the shapefile shape type value for a point
   * @return int Shapefile.POINT
   */
        @Override
  public ShapeType getShapeType() {
    return shapeType;
  }


        @Override
  public int getLength(Object geometry) {
    int length;
    if (shapeType == ShapeType.POINT) {
      length = 20;
    } else if (shapeType == ShapeType.POINTM) {
      length = 28;
    } else if (shapeType == ShapeType.POINTZ) {
      length = 36;
    } else {
      throw new IllegalStateException("Expected ShapeType of Point, got" + shapeType);
    }
    return length;
  }

        @Override
  public Geometry read(ReadBufferManager buffer, ShapeType type) throws IOException {
    if (type == ShapeType.NULL) {
      return null;
    }

    double x = buffer.getDouble();
    double y = buffer.getDouble();
    double z = Double.NaN;

    if (shapeType == ShapeType.POINTM) {
      buffer.getDouble();
    }

    if (shapeType == ShapeType.POINTZ) {
      z = buffer.getDouble();
    }

    return geometryFactory.createPoint(new Coordinate(x, y, z));
  }

        @Override
  public void write(WriteBufferManager buffer, Object geometry) throws IOException {
    Coordinate c = ((Point) geometry).getCoordinate();

    buffer.putDouble(c.x);
    buffer.putDouble(c.y);

    if (shapeType == ShapeType.POINTZ) {
      if (Double.isNaN(c.z)) { // nan means not defined
        buffer.putDouble(0.0);
      } else {
        buffer.putDouble(c.z);
      }
    }

    if ((shapeType == ShapeType.POINTZ) || (shapeType == ShapeType.POINTM)) {
      buffer.putDouble(-10E40); //M
    }
  }

}
