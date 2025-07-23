# OSMRead

## Signatures

```sql
OSMRead(VARCHAR path);

OSMRead(VARCHAR path, VARCHAR tableName);
OSMRead(VARCHAR path, VARCHAR tableName, BOOLEAN deleteTables);

OSMRead(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding);
OSMRead(VARCHAR path, VARCHAR tableName, VARCHAR fileEncoding, BOOLEAN deleteTables);
```

## Description

Reads a [OSM][wiki] file from `path` and creates several tables prefixed by `tableName` representing the file's contents. If `deleteTables` is equal to `true`, existing tables (with the same prefix) are removed.

11 tables are produced.

 * (1) table_prefix + _tag : table that contains all tag keys listed in the OSM file -> ID_TAG SERIAL PRIMARY KEY, TAG_KEY VARCHAR UNIQUE.
 * (2) table_prefix + _node : table that contains all nodes -> ID_NODE BIGINT PRIMARY KEY, THE_GEOM POINT,ELE DOUBLE PRECISION,USER_NAME VARCHAR,UID BIGINT,VISIBLE BOOLEAN,VERSION INTEGER,CHANGESET INTEGER,LAST_UPDATE TIMESTAMP, NAME VARCHAR.
 * (3) table_prefix + _node_tag : table that contains a list of tag keys (TAG_VALUE) for each node -> ID_NODE BIGINT, ID_TAG BIGINT,TAG_VALUE VARCHAR.
 * (4) table_prefix + _way : table that contains all ways -> ID_WAY BIGINT PRIMARY KEY, USER_NAME VARCHAR, UID BIGINT, VISIBLE BOOLEAN, VERSION INTEGER, CHANGESET INTEGER, LAST_UPDATE TIMESTAMP, NAME VARCHAR.
 * (5) table_prefix + _way_tag : table that contains a list of tag keys (TAG_VALUE) for each way -> ID_WAY BIGINT, ID_TAG BIGINT, VALUE VARCHAR.
 * (6) table_prefix + _way_node : table that contains the list of nodes used to represent a way -> ID_WAY BIGINT, ID_NODE BIGINT, NODE_ORDER INT.
 * (7) table_prefix + _relation: table that contains all relations -> ID_RELATION BIGINT PRIMARY KEY,USER_NAME VARCHAR, UID BIGINT,VISIBLE BOOLEAN,VERSION INTEGER,CHANGESET INTEGER,LAST_UPDATE TIMESTAMP.
 * (8) table_prefix + _relation_tag : table that contains a list of tag keys (TAG_VALUE) for each relation -> ID_RELATION BIGINT, ID_TAG BIGINT, VALUE VARCHAR.
 * (9) table_prefix + _node_member : table that stores all nodes that are referenced into a relation -> ID_RELATION BIGINT,ID_NODE BIGINT, ROLE VARCHAR, NODE_ORDER INT.
 * (10) table_prefix + _way_member : table that stores all ways that are referenced into a relation -> ID_RELATION BIGINT, ID_WAY BIGINT, ROLE VARCHAR, WAY_ORDER INT.
 * (11) table_prefix + _relation_member : table that stores all relations that are referenced into a relation -> ID_RELATION BIGINT, ID_SUB_RELATION BIGINT, ROLE VARCHAR, RELATION_ORDER INT.

By default, the `tableName` is the filename given in `path` without the extension.


<div class="note">
  <h5>Warning on the input file name</h5>
  <p>When a <code>tablename</code> is not specified, special caracters in the input file name are not allowed. The possible caracters are as follow: <code>A to Z</code>, <code>_</code> and <code>0 to 9</code>.</p>
</div>

The OSM driver supports the following zipped extension : osm.gz and osm.bz2.

Define `fileEncoding` to force encoding (useful when the header is missing encoding information) (default value is `ISO-8859-1`).

## Examples

### 1. Using `path`

```sql
CALL OSMRead('/home/user/bretagne.osm');
```

### 2. Using `path` and `tableName`

```sql
CALL OSMRead('/home/user/bretagne.osm', 'OSM_BRETAGNE');
```

### 3. Using a zipped file

```sql
CALL OSMRead('/home/user/bretagne.osm.gz', 'OSM_BRETAGNE');
```

### 4. Using `fileEncoding`

```sql
CALL OSMRead('/home/user/bretagne.osm', 'OSM_BRETAGNE', 'utf-8');
```

### 5. Using `deleteTables`

```sql
CALL OSMRead('/home/user/bretagne.osm', 'OSM_BRETAGNE', true);
```


### 6. Build OSM data

Based on the created tables, the user can build other OSM data. 

H2GIS extract buildings:

```sql
DROP TABLE IF EXISTS OSM_BRETAGNE_BUILDINGS;

CREATE TABLE OSM_BRETAGNE_BUILDINGS(ID_WAY BIGINT PRIMARY KEY) AS SELECT DISTINCT ID_WAY FROM OSM_BRETAGNE_WAY_TAG WT, OSM_BRETAGNE_TAG T WHERE WT.ID_TAG = T.ID_TAG AND T.TAG_KEY IN ('building');

DROP TABLE IF EXISTS OSM_BRETAGNE_BUILDINGS_GEOM;

CREATE TABLE OSM_BRETAGNE_BUILDINGS_GEOM AS SELECT ID_WAY, ST_MAKEPOLYGON(ST_MAKELINE(THE_GEOM)) THE_GEOM FROM (SELECT (SELECT ST_ACCUM(THE_GEOM) THE_GEOM FROM (SELECT N.ID_NODE, N.THE_GEOM,WN.ID_WAY IDWAY FROM OSM_BRETAGNE_NODE N,OSM_BRETAGNE_WAY_NODE WN WHERE N.ID_NODE = WN.ID_NODE ORDER BY WN.NODE_ORDER) WHERE  IDWAY = W.ID_WAY) THE_GEOM ,W.ID_WAY FROM OSM_BRETAGNE_WAY W,OSM_BRETAGNE_BUILDINGS B WHERE W.ID_WAY = B.ID_WAY) GEOM_TABLE WHERE ST_GEOMETRYN(THE_GEOM,1) = ST_GEOMETRYN(THE_GEOM, ST_NUMGEOMETRIES(THE_GEOM)) AND ST_NUMGEOMETRIES(THE_GEOM) > 2;
```

PostGIS extract buildings:

```sql
DROP TABLE IF EXISTS OSM_BRETAGNE_BUILDINGS;

CREATE TABLE OSM_BRETAGNE_BUILDINGS AS SELECT DISTINCT ID_WAY FROM OSM_BRETAGNE_WAY_TAG WT, OSM_BRETAGNE_TAG T WHERE WT.ID_TAG = T.ID_TAG AND T.TAG_KEY IN ('building');

ALTER TABLE OSM_BRETAGNE_BUILDINGS ADD PRIMARY KEY(ID_WAY);

DROP TABLE IF EXISTS OSM_BRETAGNE_BUILDINGS_GEOM;

CREATE TABLE OSM_BRETAGNE_BUILDINGS_GEOM AS SELECT ID_WAY,
ST_MAKEPOLYGON(ST_MAKELINE(THE_GEOM)) THE_GEOM FROM (SELECT (SELECT ST_ACCUM(THE_GEOM) THE_GEOM FROM
 (SELECT N.ID_NODE, N.THE_GEOM,WN.ID_WAY IDWAY FROM OSM_BRETAGNE_NODE N,OSM_BRETAGNE_WAY_NODE WN WHERE N.ID_NODE = WN.ID_NODE ORDER BY WN.NODE_ORDER)  ORDEREDNODES WHERE  ORDEREDNODES.IDWAY = W.ID_WAY) THE_GEOM ,W.ID_WAY FROM OSM_BRETAGNE_WAY W,OSM_BRETAGNE_BUILDINGS B WHERE W.ID_WAY = B.ID_WAY) GEOM_TABLE WHERE ARRAY_LENGTH(THE_GEOM, 1) > 2 AND ST_EQUALS(THE_GEOM[1],THE_GEOM[ARRAY_LENGTH(THE_GEOM, 1)]);
```

## See also

* [`ST_OSMDownloader`](../ST_OSMDownloader), [`ST_OSMMapLink`](../ST_OSMMapLink)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/osm/OSMRead.java" target="_blank">Source code</a>

[wiki]: http://wiki.openstreetmap.org/wiki/OSM_XML

