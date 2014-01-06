---

layout: docs

title: Custom function aliases

prev_section: dev/spatial-jdbc

next_section: dev/embedded-spatial-db

permalink: /docs/dev/function-aliases/

---

You can define Java functions in SQL.

{% highlight sql %}
CREATE ALIAS PRINT AS $$ void print(String s) { System.out.println(s); } $$;
{% endhighlight %}

