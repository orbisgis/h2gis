# Native Compilation of H2GIS with GraalVM and C Interface

This document explains how to compile H2GIS as a native binary using GraalVM and how a C interface is used to communicate with the database.

## Prerequisites
### Linux
On Linux, to compile H2GIS natively you need to install maven, [graalvm EE Core 22.3.5 for java 11 or 17](https://www.oracle.com/downloads/graalvm-downloads.html).
Then you need to get a token with 
```bash
bash <(curl -sL https://get.graalvm.org/ee-token)
```
You will receive a mail to activate your token.

Then run this to install the native-image graalvm plugin.
```bash
gu install native-image
```

### Windows
On windows,  to compile H2GIS natively, you need to install maven, [graalvm EE Core 22.3.5 for java 11 or 17](https://www.oracle.com/downloads/graalvm-downloads.html) and visual studio with c++ for desktop.
Then, you need to add this to you path :
```bash
C:\Program Files\Microsoft Visual Studio\<vs_version>\Community\VC\Tools\MSVC\<vs_version>\bin\Hostx64\x64
```
Then run this to install the native-image graalvm plugin.
```bash
path\to\your\graalvm\install\gu install native-image
```

## Launch native compilation

### Linux
To launch the native compilation you can run at the project's root:
```bash
mvn clean install -P native
```

### Windows
To launch the native compilation, run this in a powershell terminal:
```bash
Import-Module "C:\Program Files\Microsoft Visual Studio\<vs_version>\Community\Common7\Tools\Microsoft.VisualStudio.DevShell.dll"
Enter-VsDevShell -VsInstallPath "C:\Program Files\Microsoft Visual Studio\<vs_version>\Community" -DevCmdArguments '-arch=x64'
```

Then use `cd` to go to the project's root file, then run:
```bash
mvn clean install -P native
```


## Goal

- Compile **H2GIS** natively to enable integration in environments without a JVM.
- Expose SQL-related functions through a C interface (`@CEntryPoint`) for use in Python via `ctypes` or `cffi`.
- Publish the binary and native library in a GitHub release and on Maven Central.

---



## Process Steps

### 1. Project Preparation

The Maven project `H2GIS` was modified to include:

- The `native` profile now builds the native lib, executable and a .deb if on linux.
- A new module `h2gis-graalvm` that contains a `GraalCInterface` class with methods annotated using `@CEntryPoint` to make them callable from C.

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

### 2. The `GraalCInterface` Class

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

- Currently, only the Linux version is compiled in github actions. Cross-compilation for Windows/macOS is possible and will be done in the future.
- Support for complex return types could be improved using a shared C struct.
- Currently, the performances of GraalVm are not very good (x2 time taken to do the same computations as the java version)
- Currently, we use Java 11 (ot 17) with graalvm which limits the version of GraalVM to the 22.3.5, so we cannot use recent functionnalities. 

---

## License

The project retains the original H2GIS license.