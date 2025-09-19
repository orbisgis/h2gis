# ST_TESSELATE

## Signature

```sql
GEOMETRY ST_TESSELATE(GEOMETRY geom)
```

## Description
Return the tessellation of a `geometry` with adaptive triangles.

### Remark
Input geometry (`geom`) can only be a `POLYGON` or a `MULTIPOLYGON`. 

## Examples

### Case with `POLYGON`

```sql
SELECT ST_TESSELATE('POLYGON ((1 1, 1 5, 3 5, 3 4, 2 4, 
				2 3, 4 3, 4 2, 1 1))');

-- Answer: 
GEOMETRYCOLLECTION (POLYGON ((1 5, 3 5, 3 4, 1 5)), 
                    POLYGON ((2 3, 4 3, 4 2, 2 3)), 
                    POLYGON ((1 5, 3 4, 2 4, 1 5)), 
                    POLYGON ((2 3, 4 2, 1 1, 2 3)), 
                    POLYGON ((1 1, 1 5, 2 4, 1 1)), 
                     POLYGON ((2 4, 2 3, 1 1, 2 4)))
```

![](./ST_TESSELATE_1.png){align=center}

### Case with `MULTIPOLYGON`

```sql
SELECT ST_TESSELATE('MULTIPOLYGON (
			((1 1, 1 3, 2 4, 3 3, 3 2, 2 2, 1 1)), 
  			((3 5, 4 3, 4 2, 3 1, 5 1, 5 4, 3 5)))');

-- Answer: 
GEOMETRYCOLLECTION (POLYGON ((1 1, 1 3, 2 4, 1 1)), 
                    POLYGON ((2 4, 3 3, 3 2, 2 4)), 
                    POLYGON ((2 2, 1 1, 2 4, 2 2)), 
                    POLYGON ((2 4, 3 2, 2 2, 2 4)), 
                    POLYGON ((5 1, 3 1, 4 2, 5 1)), 
                    POLYGON ((4 3, 3 5, 5 4, 4 3)), 
                    POLYGON ((5 4, 5 1, 4 2, 5 4)), 
                    POLYGON ((4 2, 4 3, 5 4, 4 2)))
```

![](./ST_TESSELATE_2.png){align=center}

### Application with real data

The tessellation is made on a set of urban plots (in grey) (stored in a layer called "PLOTS")

```sql
SELECT ST_TESSELATE(THE_GEOM) as THE_GEOM FROM PLOTS;
```

The resulting triangles are displayed in pink.

![](./ST_TESSELATE_3.png){align=center}

## See also

* [`ST_Delaunay`](../ST_Delaunay), [`ST_ConstrainedDelaunay`](../ST_ConstrainedDelaunay)
* <a href="https://github.com/orbisgis/h2gis/blob/master/h2gis-functions/src/main/java/org/h2gis/functions/spatial/mesh/ST_TESSELATE.java" target="_blank">Source code</a>
