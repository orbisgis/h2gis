import ctypes
import os
import platform
import json



# --- Opaque struct placeholders for GraalVM native API ---
class GraalIsolate(ctypes.Structure):
    _fields_ = []


class GraalIsolateThread(ctypes.Structure):
    _fields_ = []


class GraalIsolateParams(ctypes.Structure):
    _fields_ = []


GraalIsolate_p = ctypes.POINTER(GraalIsolate)
GraalIsolateThread_p = ctypes.POINTER(GraalIsolateThread)


class H2GIS:
    """
    Python wrapper for the native H2GIS library compiled with GraalVM.

    This class provides a high-level Python API for interacting with a H2GIS
    spatial database through a native interface exposed via GraalVM's C API.

    Typical usage:

        h2 = H2GIS()
        h2.connect("mydb.mv.db")
        h2.execute("CREATE TABLE test(id INT PRIMARY KEY);")
        rows = h2.fetch("SELECT * FROM test;")
        h2.close()

    Attributes:
        lib (CDLL): The loaded native shared library.
        isolate (GraalIsolate*): GraalVM isolate instance.
        thread (GraalIsolateThread*): GraalVM isolate thread.
        connection (int): Native handle for the database connection.
    """

    def __init__(self, dbPath = None, username="sa", password="", lib_path=None):
        """
        Initialize the H2GIS connector and create a GraalVM isolate.

        Args:
            lib_path (str, optional): Path to the native H2GIS shared library
                (.so/.dll/.dylib). If not provided, a platform-specific default
                path will be used.

        Raises:
            RuntimeError: If the GraalVM isolate fails to initialize.
        """
        if lib_path is None:
            lib_path = self._default_library_path()

        self.lib = ctypes.CDLL(lib_path)
        self._setup_c_function_signatures()

        self.isolate = GraalIsolate_p()
        self.thread = GraalIsolateThread_p()
        self.connection = 0

        params = GraalIsolateParams()
        result = self.lib.graal_create_isolate(
            ctypes.byref(params),
            ctypes.byref(self.isolate),
            ctypes.byref(self.thread)
        )
        if result != 0:
            raise RuntimeError(f"Failed to create GraalVM isolate (code {result})")
        if dbPath != None:
            self.connect(dbPath, username, password)



    def _default_library_path(self):
        """
        Determine the default shared library path based on the current platform.

        Returns:
           str: Path to the expected native shared library file.
        """
        base_dir = os.path.dirname(__file__)
        system = platform.system()
        if system == "Windows":
            libname = "h2gis.dll"
        else:
            libname = "h2gis.so"
        return os.path.join(base_dir, "lib", libname)

    def _setup_c_function_signatures(self):
        """
        Define argument and return types for all native C functions
        exposed by the H2GIS native library.
        """
        self.lib.graal_create_isolate.argtypes = [
            ctypes.POINTER(GraalIsolateParams),
            ctypes.POINTER(GraalIsolate_p),
            ctypes.POINTER(GraalIsolateThread_p),
        ]
        self.lib.graal_create_isolate.restype = ctypes.c_int

        self.lib.graal_tear_down_isolate.argtypes = [GraalIsolateThread_p]
        self.lib.graal_tear_down_isolate.restype = ctypes.c_int

        self.lib.h2gis_connect.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_char_p,
            ctypes.c_char_p,
            ctypes.c_char_p,
        ]
        self.lib.h2gis_connect.restype = ctypes.c_long

        self.lib.h2gis_execute.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_long,
            ctypes.c_char_p,
        ]
        self.lib.h2gis_execute.restype = ctypes.c_int

        self.lib.h2gis_fetch.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_long,
            ctypes.c_char_p,
        ]
        self.lib.h2gis_fetch.restype = ctypes.c_long

        self.lib.h2gis_fetch_rows.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_long,
        ]
        self.lib.h2gis_fetch_rows.restype = ctypes.c_char_p

        self.lib.h2gis_close_query.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_long,
        ]
        self.lib.h2gis_close_query.restype = None


        self.lib.h2gis_close_connection.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_long,
        ]
        self.lib.h2gis_close_connection.restype = None

        self.lib.h2gis_delete_database_and_close.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_long,
        ]
        self.lib.h2gis_delete_database_and_close.restype = None

    def connect(self, dbPath: str, username="sa", password=""):
        """
        Connect to a H2GIS database file.

        Args:
            dbPath (str): Path to the H2GIS database file (.mv.db).
            username (str): Username for the database (default is "sa").
            password (str): Password for the database (default is empty).

        Raises:
            ValueError: If any argument is None.
            RuntimeError: If the connection to the database fails.
        """
        if(dbPath == None):
            raise ValueError("dbPath should not be equal to None")

        if(username == None or password == None):
            raise ValueError("Username and password should not be equal to None")
        self.connection = self.lib.h2gis_connect(
            self.thread,
            dbPath.encode("utf-8"),
            username.encode("utf-8"),
            password.encode("utf-8"),
        )

        if self.connection == 0:
            raise RuntimeError("Failed to connect to H2GIS database.")

    def execute(self, sql: str) -> int:
        """
        Execute a non-query SQL statement (e.g., INSERT, UPDATE, DELETE).

        Args:
            sql (str): The SQL statement to execute.

        Returns:
            int: Number of rows affected.

        Raises:
            RuntimeError: If the query fails to execute.
        """
        return self.lib.h2gis_execute(
            self.thread,
            self.connection,
            sql.encode("utf-8"),
        )

    def commit(self) -> int:
        """
        Execute a commit on the database.

        Returns:
            int: Number of rows affected.

        Raises:
            RuntimeError: If the query fails to execute.
        """
        return self.lib.h2gis_execute("COMMIT;")

    def rollback(self) -> int:
        """
        Execute a commit on the database.

        Returns:
            int: Number of rows affected.

        Raises:
            RuntimeError: If the query fails to execute.
        """
        return self.lib.h2gis_execute("ROLLBACK;")

    def fetch(self, sql: str) -> str:
        """
        Execute a SELECT query and return the result as a JSON string
        ready to be consumed by pandas.read_json().

        Args:
            sql (str): The SELECT SQL query to execute.

        Returns:
            str: The result as a JSON string.

        Raises:
            RuntimeError: If the query fails or JSON decoding fails.
        """
        handle = self.lib.h2gis_fetch(self.thread, self.connection, sql.encode("utf-8"))
        if handle == 0:
            raise RuntimeError("Query execution failed")

        all_rows = []

        while True:
            result = self.lib.h2gis_fetch_rows(self.thread, handle)
            if not result:
                break
            decoded = result.decode("utf-8").strip()
            if decoded == "":
                continue
            if decoded.startswith("Error:"):
                self.lib.h2gis_close_query(self.thread, handle)
                raise RuntimeError(decoded)
            try:
                chunk_rows = json.loads(decoded)
                all_rows.extend(chunk_rows)
            except json.JSONDecodeError as e:
                self.lib.h2gis_close_query(self.thread, handle)
                raise RuntimeError(f"Failed to decode JSON result: {decoded}") from e

        self.lib.h2gis_close_query(self.thread, handle)
        return all_rows






    def isConnected(self) -> bool:
        """Returns true if there is an active connction."""
        return (self.connection != 0) and (self.ping() == True)


    def ping(self) -> bool:
        """Pings the database"""
        try:
            self.fetch("SELECT 1;")
            return True
        except Exception:
            return False



    def close(self):
        """
        Close the database connection if one is open.
        """
        if self.connection:
            self.lib.h2gis_close_connection(self.thread, self.connection)
            self.connection = 0

    def deleteDatabase(self):
        """
        Delete the database and close the database connection if one is open.
        """
        if self.connection:
            self.lib.h2gis_delete_database_and_close(self.thread, self.connection)
            self.connection = 0

    def __del__(self):
        """
        Destructor to ensure resources are released and the GraalVM isolate is destroyed.
        """
        try:
            self.close()
        except Exception:
            pass
        try:
            self.lib.graal_tear_down_isolate(self.thread)
        except Exception:
            pass
