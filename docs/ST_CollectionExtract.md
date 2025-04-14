# ST_CollectionExtract

## Signatures

```sql
GEOMETRY ST_CollectionExtract(GEOMETRY geom, int dimension);
```

## Description

Given a `(multi)geometry`, returns a `(multi)geometry` consisting only of elements of the specified `dimension`. Dimension numbers are 1 for `POINT`, 2 for `LINESTRING` and 3 for `POLYGON`.

Sub-geometries that are not the specified type are ignored.

If there are no sub-geometries of the right type, an `EMPTY geometry` will be returned. 

Only `POINT`s, `LINE`s and `POLYGON`s are returned.

## Examples

```sql
SELECT ST_CollectionExtract('POLYGON ((1 3, 1 0, 4 0, 4 2, 5 5, 1 3))', 3) as THE_GEOM;
-- Answer: POLYGON ((1 3, 1 0, 4 0, 4 2, 5 5, 1 3)) 
```
<img class="displayed" src="../ST_CollectionExtract_1.png"/>

```sql
SELECT ST_CollectionExtract('MULTILINESTRING ((0 0, 4 1), (1 1, 5 5))', 2) as THE_GEOM;
-- Answer: MULTILINESTRING ((0 0, 4 1), (1 1, 5 5))
```
<img class="displayed" src="../ST_CollectionExtract_2.png"/>

```sql
SELECT ST_CollectionExtract('GEOMETRYCOLLECTION(
                       MULTIPOINT((4 4), (1 1), (1 0), (0 3)),
                       LINESTRING(2 6, 6 2),
                       POLYGON((1 2, 4 2, 4 6, 1 6, 1 2)))', 2) 
                       as THE_GEOM;
-- Answer: LINESTRING (2 6, 6 2)  
```
<img class="displayed" src="../ST_CollectionExtract_3.png"/>

```sql
SELECT ST_CollectionExtract('POLYGON ((1 3, 1 0, 4 0, 4 2, 5 5, 1 3))', 2) as THE_GEOM;
-- Answer: GEOMETRYCOLLECTION EMPTY
```
<img class="displayed" src="../ST_CollectionExtract_4.png"/>


## See also

* [`ST_Dimension`](../ST_Dimension), [`ST_GeometryType`](../ST_GeometryType), [`ST_CoordDim`](../ST_CoordDim)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/edit/ST_CollectionExtract.java" target="_blank">Source code</a>
