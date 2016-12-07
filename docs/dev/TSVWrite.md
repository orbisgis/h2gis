---
layout: docs
title: TSVWrite
category: h2drivers
is_function: true
description: Table &rarr; TSV
prev_section: TSVRead
next_section: spatial-indices
permalink: /docs/dev/TSVWrite/
---

### Signatures

{% highlight mysql %}
TSVWrite(VARCHAR path, VARCHAR tableName);
{% endhighlight %}

### Description

Save a table (`tablename`) into a Tab-Separated Values ([TSV][wiki]) file specified by `path`.

### Example

In following example, we have a table called `GameOfThrones` and structured as follow.

{% highlight mysql %}
SELECT * FROM GameOfThrones ;
-- Answer:
-- |   NAME    | FIRSTNAME |     PLACE      |
-- |-----------|-----------|----------------|
-- | Stark     | Arya      | Winterfell     |
-- | Lannister | Tyrion    | Westeros       |
-- | Snow      | Jon       | Castle Black   |
-- | Baelish   | Peter     | King's Landing |
{% endhighlight %}

Now we save this table into a .tsv file ...
{% highlight mysql %}
CALL TSVWrite('/home/user/GoT.tsv', 'GameOfThrones');
{% endhighlight %}

... and we can open this `GoT.tsv` file in a text editor

{% highlight mysql %}
NAME	FIRSTNAME	PLACE
Stark	Arya	Winterfell
Lannister	Tyrion	Westeros
Snow	Jon	Castle Black
Baelish	Peter	King's Landing
{% endhighlight mysql %}

##### See also

* [`TSVRead`](../TSVRead)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/tsv/TSVWrite.java" target="_blank">Source code</a>

[wiki]: https://en.wikipedia.org/wiki/Tab-separated_values
