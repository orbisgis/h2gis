---
layout: docs
title: DBFWrite
category: h2drivers
is_function: true
description: Write DBase III files
prev_section: DBFRead
next_section: GPXRead
permalink: /docs/dev/DBFWrite/
---

### Signatures

{% highlight mysql %}
DBFWrite(varchar fileName, varchar tableReference);
DBFWrite(varchar fileName, varchar tableReference, 
         varchar fileEncoding);
{% endhighlight %}

### Description
Transfers the content of a table into a DBF file.

### Examples

{% highlight mysql %}
CALL DBFWrite('/home/user/data/file.DBF', 
              'database.schema.tableName');

CALL DBFWrite('/home/user/Data/COMMUNE44.DBF', 'COMMUNE44iso-8859-1', 
              'utf-8');

CALL DBFRead('/home/user/Data/COMMUNE44.DBF', 'commune44');
select * from commune44 limit 2;
-- Answer:
-- |   NOM   | CODE_INSEE |      DEPART      |      REGION      |
-- |---------|------------|------------------|------------------|
-- | Puceul  |   44138    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
-- | Sévérac |   44196    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
{% endhighlight %}

##### See also

* [`DBFRead`](../DBFRead)
* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/dbf/DBFWrite.java" target="_blank">Source code</a>
