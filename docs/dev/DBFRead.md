---
layout: docs
title: DBFRead
category: h2drivers/DBF
description: Read DBase III files
prev_section: h2drivers/DBF
next_section: DBFWrite
permalink: /docs/dev/DBFRead/
---

### Signatures

{% highlight mysql %}
DBFRead(Connection connection, varchar fileName, 
        varchar tableReference);
DBFRead(Connection connection, varchar fileName, 
        varchar tableReference, varchar fileEncoding)
{% endhighlight %}

### Description
Read a DBase III file and copy the content into a new table in the database.
If you define `fileEncoding`, you can read a DBF where the encoding is missing in header.

### Examples

{% highlight mysql %}
CALL FILE_TABLE('/home/user/data/file.DBF', 'tableName');
CALL DBFREAD('/home/user/data/file.DBF', 'tableName');

CALL DBFREAD('donnees_sig/IGN - BD Topo/SHP_LAMB93_D044-ED113/
              H_ADMINISTRATIF/COMMUNE.DBF', 'commune44iso',
             'iso-8859-1');
select * from commune44iso limit 2;
-- Answer:
-- |   NOM   | CODE_INSEE |      DEPART      |      REGION      |
-- |---------|------------|------------------|------------------|
-- | Puceul  |   44138    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
-- | Sévérac |   44196    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |

CALL DBFREAD('donnees_sig/IGN - BD Topo/SHP_LAMB93_D044-ED113/
              H_ADMINISTRATIF/COMMUNE.DBF', 'commune44utf',
             'utf-8');
select * from commune44utf limit 2;
-- Answer: Encoding UTF-8 is not the good encoding for this file. 
-- Some characters are not written correctly like the name Sévérac 
-- which became S. Encoding UTF-8 doesn't know the character é  
-- so doesn't translate in this encoding.
-- |  NOM   | CODE_INSEE |      DEPART      |      REGION      |
-- |--------|------------|------------------|------------------|
-- | Puceul |   44138    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
-- | S      |   44196    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
{% endhighlight %}

##### See also

* [`DBFWrite`](../DBFWrite)
* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/dbf/DBFRead.java" target="_blank">Source code</a>
