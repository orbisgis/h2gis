/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.mesh;

import org.tinfour.common.SimpleTriangle;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * A triangle built from the combination of the 3 vertices index.
 * 
 * @author Nicolas Fortin
 */
public class Triangle {
	private int a = 0;
	private int b = 0;
	private int c = 0;
	private int attribute =-1;

	public int getA() {
		return a;
	}

	public int get(int id) {
		switch (id) {
		case 0:
			return a;
		case 1:
			return b;
		default:
			return c;
		}
	}
        public int getAttribute(){
                return this.attribute;
        }
        

	public void set(int id,int index) {
		switch (id) {
		case 0:
			a=index;
			break;
		case 1:
			b=index;
			break;
		default:
			c=index;
		}
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	public Triangle(int a, int b, int c, int attribute) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
                this.attribute = attribute;
	}
        
        public Triangle(int a, int b, int c) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
             
	}

	public static class TriangleBuilder implements Consumer<SimpleTriangle> {
		ArrayList<SimpleTriangle> triangles;

		public TriangleBuilder(ArrayList<SimpleTriangle> triangles) {
			this.triangles = triangles;
		}

		@Override
		public void accept(SimpleTriangle triangle) {
			triangles.add(triangle);
		}
	}
}
