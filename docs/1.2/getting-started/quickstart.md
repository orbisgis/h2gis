---
layout: docs
title: Quick-start guide
prev_section: home
next_section: h2drivers
permalink: /docs/1.2/quickstart/
---

Download the latest H2GIS web interface on the home page, unzip it and finally run the jar called h2-dist. Then click on **Connect** in the web interface. You
will of course need a working [Java][] runtime environment.

Command-line gurus, just execute the following commands:

{% highlight bash %}
~ $ wget https://github.com/orbisgis/h2gis/releases/download/v1.2.3/h2-dist-1.2.3-bin.zip -O h2gis.zip
~ $ unzip h2gis.zip
~ $ cd h2gis-standalone
~/h2gis-standalone $ java -jar h2-dist-1.2.3.jar
{% endhighlight %}

## Initialize the spatial extension

Spatial functions will not be available until the following request is
executed:

{% highlight mysql %}
CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR
    "org.h2gis.h2spatialext.CreateSpatialExtension.initSpatialExtension";
CALL SPATIAL_INIT();
{% endhighlight %}

To open a shape file and show its contents:

{% highlight mysql %}
CALL FILE_TABLE('/home/user/myshapefile.shp', 'tablename');
SELECT * FROM TABLENAME;
{% endhighlight %}

## Web interface

This is the built-in web interface of the H2 Database:

<img class="displayed" src="../getting-started/screenshot_h2gui.png"/>

## Graphical user interface

Software GUI that use H2GIS:

<a href="http://www.orbisgis.org" target="_blank">OrbisGIS 5.1
<img class="displayed" src="../getting-started/screenshot_wms.png"/></a>


[Java]: http://java.com/en/download/index.jsp
