import ctypes

# --- Opaque struct placeholders for GraalVM native API ---

class GraalIsolate(ctypes.Structure):
    _fields_ = []

class GraalIsolateThread(ctypes.Structure):
    _fields_ = []

class GraalIsolateParams(ctypes.Structure):
    _fields_ = []


GraalIsolate_p = ctypes.POINTER(GraalIsolate)
GraalIsolateThread_p = ctypes.POINTER(GraalIsolateThread)


class H2gis:
    """
    Python wrapper for the native H2GIS library compiled with GraalVM.

    This class handles isolate creation, connection to a database,
    query execution, and cleanup. It hides the complexity of native
    threads and memory management from the user, mimicking a SQL-like API.
    """

    def __init__(self, lib_path="./h2gis-dist/target/h2gis.so"):
        """
        Initialize the H2GIS wrapper.

        Loads the shared library and creates a new GraalVM isolate.

        :param lib_path: Path to the compiled shared library (.so file)
        """
        self.lib = ctypes.CDLL(lib_path)
        self._setup_c_function_signatures()

        # Allocate pointers for isolate and thread (initially NULL pointers)
        self.isolate = GraalIsolate_p()
        self.thread = GraalIsolateThread_p()
        self.connection = 0

        # Create isolate and thread context
        params = GraalIsolateParams()
        result = self.lib.graal_create_isolate(
            ctypes.byref(params),
            ctypes.byref(self.isolate),
            ctypes.byref(self.thread)
        )
        if result != 0:
            raise RuntimeError(f"Failed to create GraalVM isolate (code {result})")

    def _setup_c_function_signatures(self):
        """Declare argument and return types of the native functions."""
        self.lib.graal_create_isolate.argtypes = [
            ctypes.POINTER(GraalIsolateParams),
            ctypes.POINTER(GraalIsolate_p),
            ctypes.POINTER(GraalIsolateThread_p)
        ]
        self.lib.graal_create_isolate.restype = ctypes.c_int

        self.lib.graal_tear_down_isolate.argtypes = [GraalIsolateThread_p]
        self.lib.graal_tear_down_isolate.restype = ctypes.c_int

        self.lib.h2gis_connect.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_char_p,
            ctypes.c_char_p,
            ctypes.c_char_p
        ]
        self.lib.h2gis_connect.restype = ctypes.c_long

        self.lib.h2gis_execute_update.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_long,
            ctypes.c_char_p
        ]
        self.lib.h2gis_execute_update.restype = ctypes.c_int

        self.lib.h2gis_execute.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_long,
            ctypes.c_char_p
        ]
        self.lib.h2gis_execute.restype = ctypes.c_long

        self.lib.h2gis_fetch_row.argtypes = [
            GraalIsolateThread_p,
            ctypes.c_long
        ]
        self.lib.h2gis_fetch_row.restype = ctypes.c_char_p

        self.lib.h2gis_close_query.argtypes = [GraalIsolateThread_p, ctypes.c_long]
        self.lib.h2gis_close_query.restype = None

        self.lib.h2gis_close_connection.argtypes = [GraalIsolateThread_p, ctypes.c_long]
        self.lib.h2gis_close_connection.restype = None

    def connect(self, db_file: str, username: str = "sa", password: str = ""):
        """
        Connect to a H2GIS database.

        :param db_file: Path to the database file (e.g., /path/to/db)
        :param username: Username for the DB (default is "sa")
        :param password: Password for the DB (default is "")
        :raises RuntimeError: If connection fails
        """
        self.connection = self.lib.h2gis_connect(
            self.thread,
            db_file.encode("utf-8"),
            username.encode("utf-8"),
            password.encode("utf-8")
        )
        if self.connection == 0:
            raise RuntimeError("Failed to connect to H2GIS database.")

    def execute_update(self, sql: str) -> int:
        """
        Execute an INSERT, UPDATE, or DELETE SQL query.

        :param sql: SQL command as a string
        :return: Number of affected rows (or error code < 0)
        """
        return self.lib.h2gis_execute_update(
            self.thread,
            self.connection,
            sql.encode("utf-8")
        )

    def execute_query(self, sql: str) -> list[str]:
        """
        Execute a SELECT SQL query and return all rows.

        :param sql: SQL SELECT query
        :return: List of result rows as strings
        :raises RuntimeError: If query execution fails
        """
        handle = self.lib.h2gis_execute(
            self.thread,
            self.connection,
            sql.encode("utf-8")
        )
        if handle == 0:
            raise RuntimeError("Query execution failed")

        rows = []
        while True:
            row = self.lib.h2gis_fetch_row(self.thread, handle)
            if not row:
                break
            rows.append(row.decode("utf-8"))

        self.lib.h2gis_close_query(self.thread, handle)
        return rows

    def close(self):
        """
        Close the database connection, if open.
        """
        if self.connection:
            self.lib.h2gis_close_connection(self.thread, self.connection)
            self.connection = 0

    def __del__(self):
        """
        Destructor to clean up the isolate and connection.
        Automatically called when the object is garbage-collected.
        """
        try:
            self.close()
        except Exception:
            pass
        finally:
            try:
                self.lib.graal_tear_down_isolate(self.thread)
            except Exception:
                pass


# Example usage:
h2gis = H2gis(lib_path="./h2gis-dist/target/h2gis.so")
h2gis.connect("/home/mael/test", "sa", "sa")
rows = h2gis.execute_query("SELECT COUNT(*) FROM test")
for row in rows:
    print(row)
h2gis.close()
