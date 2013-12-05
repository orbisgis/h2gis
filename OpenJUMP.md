```sql
select the_geom::varbinary the_geom from BATI_INDIFFERENCIE where the_geom && ${view:0}
```
```sql
SELECT ST_Extent(the_geom)::varbinary from BATI_INDIFFERENCIE;
```