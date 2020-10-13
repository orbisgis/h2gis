---
layout: docs
title: ST_AsKML
category: h2drivers
is_function: true
description: Geometry &rarr; KML
prev_section: ST_AsGeoJson
next_section: ST_GeomFromGeoJson
permalink: /docs/dev/ST_AsKML/
---

### Signatures

{% highlight mysql %}
VARCHAR ST_AsKML(GEOMETRY geom);
VARCHAR ST_AsKML(GEOMETRY geom, BOOLEAN extrude, INT altitudeMode);
{% endhighlight %}

### Description

Converts `geom` to its [KML][wiki] representation.


The `extrude` parameter *"specifies whether to connect the LinearRing to the ground. To extrude this geometry, the altitude mode must be either `relativeToGround`, `relativeToSeaFloor`, or `absolute`. Only the vertices of the LinearRing are extruded, not the center of the geometry. The vertices are extruded toward the center of the Earth's sphere."* ([Source](https://developers.google.com/kml/documentation/kmlreference))


The `altitudeMode` parameter is used to specify a distance above the ground level, sea level, or sea floor ([See more](https://developers.google.com/kml/documentation/kmlreference)). The supported values of `altitudeMode` are:

| Value | Meaning              |
|:-----:|:--------------------:|
|   0   | none                 |
|   1   | `clampToGround`      |
|   2   | `relativeToGround`   |
|   4   | `absolute`           |
|   8   | `clampToSeaFloor`    |
|  16   | `relativeToSeaFloor` |

### Examples

#### 1. Case with `geom`

{% highlight mysql %}
SELECT ST_AsKML(ST_GeomFromText('POINT(2.19 47.58), 4326'));
{% endhighlight %}

Answer:

{% highlight xml %}
<Point>
   <coordinates>2.19,47.58</coordinates>
</Point>
{% endhighlight %}

#### 2. Case with `altitudeMode` = `clampToGround`

{% highlight mysql %}
SELECT ST_AsKML(ST_GeomFromText('POINT(2.19 47.58), 4326'),
                TRUE, 1);
{% endhighlight %}

Answer:

{% highlight xml %}
<Point>
   <extrude>1</extrude>
   <kml:altitudeMode>clampToGround</kml:altitudeMode>
   <coordinates>2.19,47.58</coordinates>
</Point>
{% endhighlight %}

#### 3. Case with `altitudeMode` = `relativeToSeaFloor`

{% highlight mysql %}
SELECT ST_AsKML(ST_GeomFromText('POINT(2.19 47.58), 4326'),
                FALSE, 16);
{% endhighlight %}

Answer:

{% highlight xml %}
<Point>
   <extrude>0</extrude>
   <gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>
   <coordinates>2.19,47.58</coordinates>
</Point>
{% endhighlight %}

#### 4. Case with `altitudeMode` = `relativeToGround`

{% highlight mysql %}
SELECT ST_AsKML(
  ST_GeomFromText('LINESTRING(-1.53 47.24 100, -1.51 47.22 100,
                              -1.50 47.19 100, -1.49 47.17 100)',
                    4326),
    TRUE, 2);
{% endhighlight %}

Answer:

{% highlight xml %}
<LineString>
   <extrude>1</extrude>
   <kml:altitudeMode>relativeToGround</kml:altitudeMode>
   <coordinates>
    -1.53,47.24,100.0 -1.51,47.22,100.0 -1.5,
    47.19,100.0 -1.49,47.17,100.0
   </coordinates>
</LineString>
{% endhighlight %}

##### See also

* [`KMLWrite`](../KMLWrite)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/kml/ST_AsKml.java" target="_blank">Source code</a>

[wiki]: http://en.wikipedia.org/wiki/Keyhole_Markup_Language
