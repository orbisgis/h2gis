---
layout: docs
title: ST_GeomFromGML
category: geom2D/geometry-conversion
is_function: true
description: GML &rarr; Geometry
prev_section: ST_Force2D
next_section: ST_GeomFromText
permalink: /docs/dev/ST_GeomFromGML/
---

### Signature

{% highlight mysql %}
GEOMETRY ST_GeomFromGML(VARCHAR gml);
GEOMETRY ST_GeomFromGML(VARCHAR gml, INT srid);
{% endhighlight %}

### Description

Converts an input `gml` representation into a Geometry, optionally with spatial reference id `srid`.

This function supports:

* only GML 2.1.2
* 3D coordinates
* Multi-geometries

### Examples

##### For a Point, with a srid
{% highlight mysql %}
SELECT ST_GeomFromGML('
	<gml:Point>
	    <gml:coordinates>
	    	50.2,38.7
	    </gml:coordinates>
	</gml:Point>', 4326);
-- Answer: POINT (50.2 38.7)
{% endhighlight %}

##### For a 3D Point
{% highlight mysql %}
SELECT ST_GeomFromGML('
	<gml:Point>
	    <gml:coordinates>
	    	50.2,38.7,20
	    </gml:coordinates>
	</gml:Point>');
-- Answer: POINT (50.2 38.7 20)
{% endhighlight %}

##### For a Linestring
{% highlight mysql %}
SELECT ST_GeomFromGML('
	<gml:LineString srsName="EPSG:4326">
	    <gml:coordinates>
	    	-60.5,35.2 -62.3,47.4 -65.6,48.4
	    </gml:coordinates>
	</gml:LineString>');
-- Answer: LINESTRING (-60.5 35.2, -62.3 47.4, -65.6 48.4) 
{% endhighlight %}

##### For a MultiLinestring
{% highlight mysql %}
SELECT ST_GeomFromGML('
	<MultiLineString srsName="EPSG:4326">
	  <lineStringMember>
	     <LineString>
		<coordinates>56.1,0.45 67.23,0.67</coordinates>
	     </LineString>
	  </lineStringMember>
	  <lineStringMember>
	     <LineString>
		<coordinates>46.71,9.25 56.88,10.44</coordinates>
	     </LineString>
	  </lineStringMember>
	  <lineStringMember>
	     <LineString>
		<coordinates>324.1,219.7 0.45,0.56</coordinates>
	     </LineString>
	  </lineStringMember>
	</MultiLineString>');
-- Answer: MULTILINESTRING ((56.1 0.45, 67.23 0.67), 
--			    (46.71 9.25, 56.88 10.44), 
--			    (324.1 219.7, 0.45 0.56))
{% endhighlight %}

##### For a Polygon
{% highlight mysql %}
SELECT ST_GeomFromGML('
	<gml:Polygon>
	  <gml:outerBoundaryIs>
	     <gml:LinearRing>
		<gml:coordinates>
		  0,0 50,0 50,50 0,50 0,0
		</gml:coordinates>
	     </gml:LinearRing>
          </gml:outerBoundaryIs>
	</gml:Polygon>');
-- Answer: POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))
{% endhighlight %}

##### See also

* [`ST_AsGML`](../ST_AsGML)

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/convert/ST_GeomFromGML.java" target="_blank">Source code</a>
