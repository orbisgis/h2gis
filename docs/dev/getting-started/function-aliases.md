---
layout: docs
title: Custom function aliases
prev_section: spatial-jdbc
next_section: embedded-spatial-db
permalink: /docs/dev/function-aliases/
---

You can define Java functions in SQL.

{% highlight sql %}
CREATE ALIAS PRINT AS $$ void print(String s) { System.out.println(s); } $$;
{% endhighlight %}

