# Native Compilation of H2GIS with GraalVM and C Interface

This document explains how H2GIS was compiled as a native binary using GraalVM and how a C interface is used to communicate with the database through a Python API.

## Prerequisites
### Linux
On Linux, to manipulate the native compilation you need to install [graalvm EE 22.3.3](https://download.oracle.com/otn/utilities_drivers/oracle-labs/graalvm-ee-java11-linux-amd64-22.3.3.tar.gz).
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
On windows, you need to install maven, [graalvm EE 22.3.3]https://download.oracle.com/otn/utilities_drivers/oracle-labs/graalvm-ee-java11-windows-amd64-22.3.3.zip) and visual studio with c++ for desktop.
Then, you need to add this to you path :
```bash
C:\Program Files\Microsoft Visual Studio\<vs_version>\Community\VC\Tools\MSVC\<vs_version>\bin\Hostx64\x64
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

- Currently, only the Linux version is compiled in github actions. Cross-compilation for Windows/macOS is possible.
- Support for complex return types could be improved using a shared C struct.
- Currently, the performances of GraalVm are not very good (x2 time taken to do the same computations as the java version)
- Currently, we use Java 11 with graalvm which limits the version of GraalVM to the 22.3.3, so we cannot use recent functionnalities. 

---

## License

The project retains the original H2GIS license.