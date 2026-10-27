# ASCTable

## Signatures

```sql
ASCTable(VARCHAR path);
ASCTable(VARCHAR path, INTEGER zType);
ASCTable(VARCHAR path, GEOMETRY geomFilter, INTEGER downScaleInt, BOOLEAN asPolygons);
ASCTable(VARCHAR path, GEOMETRY geomFilter, INTEGER downScaleInt, BOOLEAN asPolygons, INTEGER zType);
```

## Description

Reads the cells of an Esri ASCII Raster file as a table, without importing it. The file may be zipped in a `.gz` file.

`ASCTable` is used in the `FROM` clause of a query, and returns the rows [`ASCRead`](../ASCRead) imports with the same options: `PK`, `THE_GEOM` (a `POINTZ` at the center of the cell, or the `POLYGONZ` of the cell, with the SRID of the `.prj` file) and `Z`.

Where:

- `path` : address of the `.asc` or `.gz` file,
- `zType` : `1` for an `INTEGER` Z, `2` for a `DOUBLE` Z (default value),
- `geomFilter` : only the cells that intersect the envelope of the geometry (`NULL` for all the cells),
- `downScaleInt` : one cell out of `downScaleInt` in each direction (`1` for all the cells, `2` for a quarter of them...),
- `asPolygons` : `true` for the polygons of the cells, `false` for their centers (default value).

The envelope and the downscale are applied while the file is read: they are faster than a `WHERE` condition on the geometry.

:::{note}
**The file is read at each query**

For repeated queries, import the file with [`ASCRead`](../ASCRead).
:::

## Examples

```sql
SELECT MIN(Z), AVG(Z), MAX(Z) FROM ASCTable('/home/user/dem.asc');
```

```sql
SELECT * FROM ASCTable('/home/user/dem.asc.gz', ST_MakeEnvelope(10, 10, 20, 20), 2, true);
```

Import only some cells:

```sql
CREATE TABLE HIGH_CELLS AS SELECT * FROM ASCTable('/home/user/dem.asc', 1) WHERE Z > 25;
```

## See also

* [`ASCRead`](../ASCRead), [`GeoJsonTable`](../GeoJsonTable), [`SHPTable`](../SHPTable)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/io/asc/ASCTable.java" target="_blank">Source code</a>
