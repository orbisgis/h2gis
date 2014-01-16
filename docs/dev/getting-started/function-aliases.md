---
layout: docs
title: Custom function aliases
prev_section: spatial-jdbc
next_section: embedded-spatial-db
permalink: /docs/dev/function-aliases/
---

It is possible to define custom function aliases by surrounding Java code by
double dollar signs.  So the proverbial "Hello world!" program looks like the
following in H2:

{% highlight mysql %}
CREATE ALIAS PRINT AS $$ void print(String s) {
    System.out.println(s); } $$;
CALL PRINT('Hello world!');
{% endhighlight %}

If you launched H2GIS from the command line, you should see "Hello world!"
printed in your console.
