package org.h2spatial;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class BasicTest extends TestCase{


	public void test3DPoints() throws Exception {
		
		
		WKTReader wktReader = new WKTReader();
		
		Geometry geom = wktReader.read("POINT(0 1 3)");
		
		Coordinate coord = geom.getCoordinates()[0];	
			
		assertTrue(3 == coord.z);
		
			
		
	}
	
	
	

}
