# Spatial JDBC

One of H2GIS's big-picture goals is to provide a common interface to H2 and PostGIS for Geometry data.

For convenience, the `spatial-utilities` package provides wrappers that facilitate using the [Java Database Connectivity][] (JDBC) API in the presence
of Geometry fields.

## Example

`DataSource`s and `Connection`s must be wrapped:

```java
DataSource wrappedDataSource =
    SFSUtilities.wrapSpatialDataSource(originalDataSource);
Connection wrappedConnection
    SFSUtilities.wrapSpatialConnection(originalConnection);
```

`ResultSet`s must be unwrapped into `SpatialResultSet`s in order to access Geometry fields:

```java
SpatialResultSet rs = st.executeQuery(
    "SELECT the_geom FROM mygeomtable").
        unWrap(SpatialResultSet.class);
rs.next();
Geometry myGeom = rs.getGeometry("the_geom");
```

[Java Database Connectivity]: http://www.oracle.com/technetwork/java/javase/jdbc/index.html
