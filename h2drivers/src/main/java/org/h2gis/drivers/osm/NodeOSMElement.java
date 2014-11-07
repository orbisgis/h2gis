/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.drivers.osm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 *
 * @author Erwan Bocher
 */
public class NodeOSMElement extends OSMElement {

    private Point point;

    public NodeOSMElement() {
        super();
    }

    /**
     *
     * @return
     */
    public Point getPoint() {
        return point;
    }

    /**
     *
     * @param gf
     * @param lon
     * @param lat
     */
    public void createPoint(GeometryFactory gf, String lon, String lat) {
        point = gf.createPoint(new Coordinate(Double.valueOf(lon),
                Double.valueOf(lat)));
    }

}
