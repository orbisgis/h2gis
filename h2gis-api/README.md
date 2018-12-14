# H2GIS API

This H2GIS module defines the API for the whole project, it describes
the different kind of functions supported and some utilities classes.

### Function API

##### [![I](https://img.shields.io/badge/Function-Interface-green.svg)](src/main/java/org/h2gis/api/Function.java)

Root interface for the SQL function definition.

##### [![I](https://img.shields.io/badge/ScalarFunction-Interface-green.svg)](src/main/java/org/h2gis/api/ScalarFunction.java)

Scalar function in H2 can be defined through CREATE ALIAS, but in an
OSGi context the class java name is not sufficient.

The full declaration of java name in H2 through osgi is
BundleSymbolicName:BundleVersion:BinaryJavaName.

Registering this interface as an OSGi service will add this function in
H2GIS linked with a DataSource service.

##### [![C](https://img.shields.io/badge/AbstractFunction-Class-blue.svg)](src/main/java/org/h2gis/api/AbstractFunction.java)

Abstract implementation of the Function interface which is able to
handle properties into a map.

##### [![C](https://img.shields.io/badge/DeterministicScalarFunction-Class-blue.svg)](src/main/java/org/h2gis/api/DeterministicScalarFunction.java)

Extended by Scalar function which return always the same value for the same arguments.

##### [![I](https://img.shields.io/badge/DriverFunction-Interface-green.svg)](src/main/java/org/h2gis/api/DriverFunction.java)

This function can import/export a file into/from a table.
Connection may be on a remote H2/Postgres database.

The file can be linked to the database or copied into the database.

### ProgressVisitor API

##### [![I](https://img.shields.io/badge/ProgressVisitor-Interface-green.svg)](src/main/java/org/h2gis/api/ProgressVisitor.java)

Progression information.

##### [![C](https://img.shields.io/badge/EmptyProgressVisitor-Class-blue.svg)](src/main/java/org/h2gis/api/EmptyProgressVisitor.java)

A progress visitor that do nothing.

### File API

##### [![I](https://img.shields.io/badge/FileDriver-Interface-green.svg)](src/main/java/org/h2gis/api/FileDriver.java)

Implement this interface in order to create a `org.h2.table.TableBase` in
 your `org.h2.api.TableEngine` implementation.

How to use:
 * Implement this interface with your file driver</li>
 * Implement a FileEngine</li>
 * Add your FileEngine implementation into the DriverManager</li>