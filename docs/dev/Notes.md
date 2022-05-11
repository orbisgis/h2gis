some notes about h2gis usage.

# Alternative jdbc driver
PostGIS has a jdbc:postgres_jts: Driver that let the user the choice to switch between OrbisGIS embedded H2 database or Remote/Local postgres database.

# Create spatial table from csv
```sql
create table roads(geom MultiLineString) as select ST_GeomFromText(geom) from CSVREAD('/home/user/roads.csv');
```