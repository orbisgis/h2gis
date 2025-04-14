# ST_AsGML

## Signature

```sql
GEOMETRY ST_AsGML(GEOMETRY geom);
```

## Description

Store a geometry (`geom`) as a GML representation.

This function supports:

* only GML 2.1.2
* 3D coordinates
* Multi-geometries

## Examples

### For a Point
```sql
SELECT ST_AsGML('POINT(-2.070365 47.643713)');
```

Answer: 
```
--	<gml:Point srsName='EPSG:0'>
--	  <gml:coordinates>
--	    -2.070365,47.643713 
--	  </gml:coordinates>
--	</gml:Point>
```

### For a (3D) Linestring
```sql
SELECT ST_AsGML('LINESTRING (12 25 10, 100 20 5, 56 65 8)');
```

Answer: 
```
--	<gml:LineString srsName='EPSG:0'>
--	  <gml:coordinates>
--	    12.0,25.0,10.0 100.0,20.0,5.0 56.0,65.0,8.0 
--	  </gml:coordinates>
--	</gml:LineString>
```

### For a Linestring with a srid (here in WGS84)
```sql
SELECT ST_AsGML(ST_SetSRID('LINESTRING (-47.8 56.3, -44.2 57.3)', 4326));
```

Answer: 
```
--	<gml:LineString srsName='EPSG:4326'>
--	  <gml:coordinates>
--	    -47.8,56.3 -44.2,57.3 
--	  </gml:coordinates>
--	</gml:LineString>
```

### For a Polygon
```sql
SELECT ST_AsGML('POLYGON ((20 20, 40 20, 40 10, 20 10, 20 20))');
```

Answer: 
```
--	<gml:Polygon srsName='EPSG:0'>
--	  <gml:outerBoundaryIs>
--	    <gml:LinearRing>
--	      <gml:coordinates>
--		20.0,20.0 40.0,20.0 40.0,10.0 20.0,10.0 20.0,20.0
--	      </gml:coordinates>
--	    </gml:LinearRing>
--	  </gml:outerBoundaryIs>
--	</gml:Polygon>
```

### For a MultiPolygon
```sql
SELECT ST_AsGML('MULTIPOLYGON (((1 1, 1 3, 3 3, 3 1, 1 1)), 
  			       ((4 2, 4 4, 6 4, 6 2, 4 2)))');
```

Answer: 
```
--	<gml:MultiPolygon srsName='EPSG:0'>
--	  <gml:polygonMember>
--	    <gml:Polygon>
--	      <gml:outerBoundaryIs>
--		<gml:LinearRing>
--		  <gml:coordinates>
--		    1.0,1.0 1.0,3.0 3.0,3.0 3.0,1.0 1.0,1.0 
--		  </gml:coordinates>
--		</gml:LinearRing>
--	      </gml:outerBoundaryIs>
--	    </gml:Polygon>
--	  </gml:polygonMember>
--	  <gml:polygonMember>
--	    <gml:Polygon>
--	      <gml:outerBoundaryIs>
--		<gml:LinearRing>
--		  <gml:coordinates>
--		    4.0,2.0 4.0,4.0 6.0,4.0 6.0,2.0 4.0,2.0 
--		  </gml:coordinates>
--		</gml:LinearRing>
--	      </gml:outerBoundaryIs>
--	    </gml:Polygon>
--	  </gml:polygonMember>
--	</gml:MultiPolygon>
```

## See also

* [`ST_GeomFromGML`](../ST_GeomFromGML)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_AsGML.java" target="_blank">Source code</a>
