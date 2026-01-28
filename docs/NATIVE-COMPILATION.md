# Native Compilation of H2GIS with GraalVM and C Interface

This document explains how to compile H2GIS as a native binary using GraalVM and how a C interface is used to communicate with the database.

## Prerequisites

Install GraalVM, go to  https://www.graalvm.org/downloads/#

Note : H2GIS has been tested with 25+ version.

## Launch native compilation

Go to the project's root folder H2GIS, then run:
```bash
mvn clean package -DskipTests && mvn native:compile -Pnative -pl h2gis-graalvm -DskipTests
```

## Goal

- Compile **H2GIS** natively to enable integration in environments without a JVM.
- Expose SQL-related functions through a C interface (`@CEntryPoint`) for use in Python via `ctypes` or `cffi`.
- Publish the binary and native library in a GitHub release and on Maven Central.

---


## Process Steps

### 1. h2gis-graalvm module

The Maven module `h2gis-graalvm ` contains:

- A `GraalCInterface` class with methods annotated using `@CEntryPoint` to make them callable from C. 
Update this class if you want to expose more methods.
- Two config files reflect-config.json and resource-config.json to declare to GraalVN compiler the H2GIS resources
- A `native` profile to compile h2gis with maven.

### 1. The `GraalCInterface` Class

This class exposes several native functions:

```java
@CEntryPoint(name = "h2gis_connect")
public static IsolateThread connect(...) { ... }

@CEntryPoint(name = "h2gis_execute_query")
public static int executeQuery(...) { ... }

@CEntryPoint(name = "h2gis_fetch_results")
public static int fetchResults(...) { ... }

@CEntryPoint(name = "h2gis_close_connection")
public static void closeConnection(...) { ... }
...
```

It handles:
- JDBC connections to the H2 database.
- Execution of SQL queries.
- Storage of results in an internal structure.
- Releasing of resources.

A handle-based system is used to uniquely isolate each connection or result.

### 2. Config files

To ensure H2GIS runs correctly in native mode with GraalVM, any new class added to the codebase must also be registered for reflection. This is done by updating the reflection configuration file located at `h2gis-dist/src/main/resources/META-INF/native-image/reflect-config.json`
Add an entry for each new class to make sure it is detected and included in the native executable or library during the build process. Below is an example entry for a newly added class:
```json
  {
    "name": "com.package.ClassnNme",
    "allDeclaredConstructors": true,
    "allDeclaredMethods": true,
    "allDeclaredFields": true
  },
```

### 3. Native Compilation with GraalVM

The native binary is built using GraalVM’s `native-image`:

```bash
native-image \
  --no-fallback \
  --shared \ #(if lib mode, if executable mode, remove this)
  --initialize-at-run-time=org.h2,org.h2gis \
  --verbose \
  --report-unsupported-elements-at-runtime \
  --enable-url-protocols=http,https \
  --initialize-at-build-time \
  -H:Name=h2gis_native \
  -H:+SourceLevelDebug \
  -H:+AllowVMInspection \
  -H:+PrintAnalysisCallTree \
  -H:GenerateDebugInfo=2 \
  -H:+ReportExceptionStackTraces \
  -H:IncludeResources=.*\.sql \
  -H:Class=h2gis.native.GraalCInterface \
  -H:ResourceConfigurationFiles=h2gis-dist/src/main/resources/META-INF/native-image/resource-config.json \
  -H:ReflectionConfigurationResources=META-INF/native-image/reflect-config.json
```

---

## Limitations and Future Improvements

- Support for complex return types could be improved using a shared C struct.
- Currently, the performances of GraalVm are not very good (x2 time taken to do the same computations as the java version)

---

## License

The project retains the original H2GIS license.