---
layout: docs
title: SHPRead
category: h2drivers
is_function: true
description: Read shape files
prev_section: KMLWrite
next_section: SHPWrite
permalink: /docs/dev/SHPRead/
---

### Signatures

{% highlight mysql %}
SHPRead(VARCHAR fileName);
SHPRead(VARCHAR fileName, VARCHAR tableReference);
SHPRead(VARCHAR fileName, VARCHAR tableReference,
        varchar forceEncoding);
{% endhighlight %}

### Description
Reads a shape file and copy the content in the specified table in the
database.

### Examples

{% highlight mysql %}
CALL SHPRead('/home/user/data/file.shp',
             'database.schema.tableName');

CALL SHPRead('donnees_sig/IGN - BD Topo/SHP_LAMB93_D044-ED113/
              H_ADMINISTRATIF/COMMUNE.SHP');

CALL SHPRead('donnees_sig/IGN - BD Topo/SHP_LAMB93_D044-ED113/
              H_ADMINISTRATIF/COMMUNE.SHP', 'public.commune44iso');

CALL SHPRead('donnees_sig/IGN - BD Topo/SHP_LAMB93_D044-ED113/
              H_ADMINISTRATIF/COMMUNE.SHP', 'commune44iso',
             'iso-8859-1');
select * from commune44iso limit 2;
-- Answer:
-- |                 the_geom                  |   NOM   |
-- | ----------------------------------------- | ------- |
-- | MULTIPOLYGON(((350075.2 6719771.8,        | Puceul  |
-- |   350072.7 6719775.5, 350073 6719780.7,   |         |
-- |   350075.2 6719771.8)))                   |         |
-- | MULTIPOLYGON(((317341.5 6727021,          | Sévérac |
-- |   317309.9 6727036.8, 317193.3 6727066.5, |         |
-- |   317341.5 6727021)))                     |         |

CALL SHPRead('donnees_sig/IGN - BD Topo/SHP_LAMB93_D044-ED113/
              H_ADMINISTRATIF/COMMUNE.SHP', 'commune44utf',
             'utf-8');
select * from commune44utf limit 2;
-- Answer:
-- |                 the_geom                  |   NOM   |
-- | ----------------------------------------- | ------- |
-- | MULTIPOLYGON(((350075.2 6719771.8,        | Puceul  |
-- |   350072.7 6719775.5, 350073 6719780.7,   |         |
-- |   350075.2 6719771.8)))                   |         |
-- | MULTIPOLYGON(((317341.5 6727021,          | S       |
-- |   317309.9 6727036.8, 317193.3 6727066.5, |         |
-- |   317341.5 6727021)))                     |         |

-- Note:  Encoding UTF-8 is not the good encoding for this file.
-- Some characters are not written correctly like the name Sévérac
-- which became S. Encoding UTF-8 doesn't know the character é
-- so doesn't translate in this encoding.
{% endhighlight %}

##### See also

* [`SHPWrite`](../SHPWrite)
* <a href="https://github.com/irstv/H2GIS/blob/a8e61ea7f1953d1bad194af926a568f7bc9aac96/h2drivers/src/main/java/org/h2gis/drivers/shp/SHPRead.java" target="_blank">Source code</a>
