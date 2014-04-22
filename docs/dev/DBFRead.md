---
layout: docs
title: DBFRead
category: h2drivers/DBF
description: Read DBase III files
prev_section: h2drivers/DBF
next_section: DBFWrite
permalink: /docs/dev/DBFRead/
---

### Signature

{% highlight mysql %}
DBFRead(Connection connection, String fileName, String
tableReference);
DBFRead(Connection connection, String fileName, String tableReference, String fileEncoding)
{% endhighlight %}

### Description
Read a DBase III file and copy the content into a new table in the database.
Read a DBF where the encoding is missing in header if you define
`fileEncoding`.

### Examples

{% highlight mysql %}
CALL DBFREAD('Documents/donnees_sig/Data/IGN - BD Topo/IGN - BD Topo V2.1 - 44/1_DONNEES_LIVRAISON_2012-04-00190/BDT_2-1_SHP_LAMB93_D044-ED113/H_ADMINISTRATIF/COMMUNE.DBF', 'commune44');

CALL DBFREAD('/home/mireille/Documents/donnees_sig/Data/IGN - BD Topo/IGN - BD Topo V2.1 - 44/1_DONNEES_LIVRAISON_2012-04-00190/BDT_2-1_SHP_LAMB93_D044-ED113/H_ADMINISTRATIF/COMMUNE.DBF', 'commune44utf','utf-8')
faux mais 

Editer	CALL DBFREAD('/home/mireille/Documents/donnees_sig/Data/IGN - BD Topo/IGN - BD Topo V2.1 - 44/1_DONNEES_LIVRAISON_2012-04-00190/BDT_2-1_SHP_LAMB93_D044-ED113/H_ADMINISTRATIF/COMMUNE.DBF', 'commune44iso','iso-8859-1')
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/dbf/DBFRead.java" target="_blank">Source code</a>
