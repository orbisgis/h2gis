---
layout: docs
title: ST_OSMDownloader
category: h2drivers
is_function: true
description: OSM &rarr; File
prev_section: ST_GeomFromGeoJson
next_section: TSVRead
permalink: /docs/dev/ST_OSMDownloader/
---

### Signatures

{% highlight mysql %}
ST_OSMDownloader(GEOMETRY geom, VARCHAR path);
ST_OSMDownloader(GEOMETRY geom, VARCHAR path, BOOLEAN delete);
{% endhighlight %}

### Description

Download data from the [OSM][wiki] API using a bounding box (`geom`). The result is stored in an `.osm` file which the place and the name are defined in the `path`. If the `delete` parameter is equal to `true`, then the `.osm` file will be overwritten if it already exists.

### Examples

Download OSM data:
{% highlight mysql %}
CALL ST_OSMDownloader('POLYGON((-1.55 47.24, -1.55 47.25, 
                                -1.54 47.25, -1.54 47.24, 
                                -1.55 47.24))'::geometry, 
                      '/your_url/test.osm');
{% endhighlight %}


Read OSM file:

{% highlight mysql %}
CALL OSMRead('/your_url/test.osm');
{% endhighlight %}

Note that the OSMRead function is described [HERE](../OSMRead).

Result:

<img class="displayed" src="../ST_OSMDownloader.png"/>

On the left a screenshot from the [OpenStreetMap](http://www.openstreetmap.org) website and on the right the result, with the building reconstruction (see [OSMRead](../OSMRead)).


##### See also

* [`OSMRead`](../OSMRead), [`ST_OSMMapLink`](../ST_OSMMapLink)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/osm/ST_OSMDownloader.java" target="_blank">Source code</a>

[wiki]: http://wiki.openstreetmap.org/wiki/OSM_XML

