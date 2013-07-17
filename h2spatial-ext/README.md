h2spatial-ext
=====

A library that brings extra spatial capabilities to the H2 database.

In a non-OSGi environment you have to register spatial features by running the following SQL request :

```sql
CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR "org.h2gis.h2spatialext.CreateSpatialExtension.initSpatialExtension";
CALL SPATIAL_INIT();
```

In OSGi you have to register the h2 DataSource has an OSGi service,
 h2spatial will keep track of arrival/departure of Sql function and automatically registering them.

