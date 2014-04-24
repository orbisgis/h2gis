---
layout: docs
title: file_table
category: h2drivers/file_table
description: Open a specified file in a special table
prev_section: h2drivers/list-drivers
next_section: h2drivers/DBF
permalink: /docs/dev/h2drivers/file_table/
---

### Signature

{% highlight mysql %}
FILE_TABLE(varchar fileName,varchar tableName);
{% endhighlight %}

### Description
Creates a special table in database. The content of this special table will always be synchronized with the source file content.
The source file content isn't imported and copied in a table in the database.

<div class="note warning">
  <h5>If the source file is moved or removed, the special table always exists but is empty.</h5>
</div>

### Examples

{% highlight mysql %}
CALL FILE_TABLE('/home/user/myshapefile.shp', 'tableName');
CALL FILE_TABLE('/home/user/dbase.dbf', 'tableName');

CALL FILE_TABLE('donnees_sig/IGN - BD Topo/H_ADMINISTRATIF/
                 COMMUNE.DBF', 'commune');
select * from commune limit 2;
-- Answer:
-- |   NOM   | CODE_INSEE |      DEPART      |      REGION      |
-- |---------|------------|------------------|------------------|
-- | Puceul  |   44138    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |
-- | Sévérac |   44196    | LOIRE-ATLANTIQUE | PAYS DE LA LOIRE |

CALL FILE_TABLE('donnees_sig/IGN - BD Topo/H_ADMINISTRATIF/
                 COMMUNE.SHP', 'commune44');
select * from commune44 limit 2;
-- Answer:
-- |                 the_geom                  |   NOM   |
-- | ----------------------------------------- | ------- |
-- | MULTIPOLYGON(((350075.2 6719771.8,        | Puceul  |
-- |   350072.7 6719775.5, 350073 6719780.7,   |         |
-- |   350075.2 6719771.8)))                   |         |
-- | MULTIPOLYGON(((317341.5 6727021,          | Sévérac |
-- |   317309.9 6727036.8, 317193.3 6727066.5, |         |
-- |   317341.5 6727021)))                     |         |
{% endhighlight %}

##### See also

* <a href="https://github.com/irstv/H2GIS/blob/ec8fa27fcfd8474531e3b7455ff5d9941e462897/h2drivers/src/main/java/org/h2gis/drivers/DriverManager.java" target="_blank">Source code</a>