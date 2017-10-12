---
layout: docs
title: H2GISversion
category: system/version
is_function: true
description: Return H2GIS version
prev_section: DoubleRange
next_section: IntegerRange
permalink: /docs/1.3.2/H2GISversion/
---

### Signature

{% highlight mysql %}
STRING H2GISversion();
{% endhighlight %}

### Description

Return the current version of H2GIS stored in the manifest, otherwise return `unknown`.


### Example

{% highlight mysql %}
SELECT H2GISversion();

-- Answer:
	1.3.1-SNAPSHOT
{% endhighlight %}

##### See also

* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/system/H2GISversion.java" target="_blank">Source code</a>
