h2spatial
=====

A library that brings spatial capabilities to the H2 database.

In a non-OSGi environment you have to register spacial features by running the following SQL request :

CREATE ALIAS IF NOT EXISTS SPATIAL_INIT FOR "org.h2spatial.CreateSpatialExtension.initSpatialExtension";
CALL SPATIAL_INIT();

In OSGi you have to register the DataSource has an OSGi service,
 h2spatial will keep track of arrival/departure of Sql function and automatically registering them.

