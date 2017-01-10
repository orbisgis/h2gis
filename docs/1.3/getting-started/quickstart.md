---
layout: docs
title: Quick-start guide
prev_section: home
next_section: h2drivers
permalink: /docs/1.3/quickstart/
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

## Initialize the H2GIS extension

If the user needs only the basic spatial functions it must apply the SQL syntax:

{% highlight mysql %}
CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR "org.h2gis.functions.factory.H2GISFunctions.load";
CALL H2GIS_SPATIAL();
{% endhighlight %}

Otherwise please run the command:
{% highlight mysql %}
CREATE ALIAS IF NOT EXISTS H2GIS_EXTENSION FOR "org.h2gis.ext.H2GISExtension.load";
CALL H2GIS_EXTENSION();
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
