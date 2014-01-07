---

layout: docs

title: Quick-start guide

prev_section: home

next_section: spatial-indices

permalink: /docs/dev/quickstart/

---

For the impatient, the H2GIS web interface may be downloaded and launched as
follows:

{% highlight bash %}
~ $ wget http://tinyurl.com/h2gis-zip -O h2gis.zip
~ $ unzip h2gis.zip
~ $ cd h2gis-standalone
~/h2gis-standalone $ ./run.sh
# => Click on "Connect" in the web interface
{% endhighlight %}

To initialize spatial capabilities:

{% highlight sql %}
CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR
    "org.h2gis.h2spatialext.CreateSpatialExtension
        .initSpatialExtension";
CALL SPATIAL_INIT();
{% endhighlight %}

You can open a shape file as follows:

{% highlight sql %}
CALL FILE_TABLE('/home/user/myshapefile.shp', 'tablename');
{% endhighlight %}

You can then show the content:
{% highlight sql %}
SELECT * FROM TABLENAME;
{% endhighlight %}
