/**
 * The GDMS library (Generic Datasource Management System)
 * is a middleware dedicated to the management of various kinds of
 * data-sources such as spatial vectorial data or alphanumeric. Based
 * on the JTS library and conform to the OGC simple feature access
 * specifications, it provides a complete and robust API to manipulate
 * in a SQL way remote DBMS (PostgreSQL, H2...) or flat files (.shp,
 * .csv...).
 *
 * Gdms is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV FR CNRS 2488
 *
 * This file is part of Gdms.
 *
 * Gdms is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Gdms is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Gdms. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 *
 * or contact directly:
 * info@orbisgis.org
 */
package org.h2gis.h2spatialext.function.spatial.topography;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Triangle;

/**
 * Used by TriangleContouring.
 * Add the constraint of CCW orientation
 * Store also three double values, one fore each vertices
 * 
 * ANR EvalPDU
 * IFSTTAR 11_05_2011
 * @author Nicolas FORTIN, Judicaël PICAUT
 */
final class TriMarkers extends Triangle {

	TriMarkers() {
		super(new Coordinate(), new Coordinate(), new Coordinate());
		this.m1 = 0;
		this.m2 = 0;
		this.m3 = 0;
	}

        @Override
        public String toString() {
            return "TriMarkers{" + "p1=" + p0 + ", p2=" + p1 + ", p3=" + p2 + " m1=" + m1 + ", m2=" + m2 + ", m3=" + m3 + "}";
        }

	TriMarkers(Coordinate p0, Coordinate p1, Coordinate p2, double m1,
			double m2, double m3) {
		super(p0, p1, p2);

		if (!CGAlgorithms.isCCW(this.getRing())) {
			this.setCoordinates(p2, p1, p0);
			this.m1 = m3;
			this.m3 = m1;
		} else {
			this.m1 = m1;
			this.m3 = m3;
		}
		this.m2 = m2;
	}

	double m1, m2, m3;

	void setMarkers(double m1, double m2, double m3) {
		this.m1 = m1;
		this.m2 = m2;
		this.m3 = m3;
	}

	void setAll(Coordinate p0, Coordinate p1, Coordinate p2, double m1,
			double m2, double m3) {
		setCoordinates(p0, p1, p2);
		setMarkers(m1, m2, m3);
		if (!CGAlgorithms.isCCW(this.getRing())) {
			this.setCoordinates(p2, p1, p0);
			this.m1 = m3;
			this.m3 = m1;
		}
	}

	double getMinMarker() {
		return getMinMarker((short) -1);
	}

	double getMinMarker(short exception) {
		double minval = Double.POSITIVE_INFINITY;
		if (exception != 0) {
			minval = Math.min(minval, this.m1);
		}
		if (exception != 1) {
			minval = Math.min(minval, this.m2);
		}
		if (exception != 2) {
			minval = Math.min(minval, this.m3);
		}
		return minval;
	}

	double getMaxMarker() {
		return getMaxMarker((short) -1);
	}

	double getMaxMarker(short exception) {
		double maxval = Double.NEGATIVE_INFINITY;
		if (exception != 0) {
			maxval = Math.max(maxval, this.m1);
		}
		if (exception != 1) {
			maxval = Math.max(maxval, this.m2);
		}
		if (exception != 2) {
			maxval = Math.max(maxval, this.m3);
		}
		return maxval;
	}

	void setCoordinates(Coordinate p0, Coordinate p1, Coordinate p2) {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
	}

	Coordinate[] getRing() {
		return new Coordinate[] { p0, p1, p2, p0 };
	}

	Coordinate getVertice(short idvert) {
		if (idvert == 0) {
			return p0;
		} else if (idvert == 1) {
			return p1;
		} else {
			return p2;
		}
	}

	double getMarker(short idvert) {
		if (idvert == 0) {
			return m1;
		} else if (idvert == 1) {
			return m2;
		} else {
			return m3;
		}
	}
}