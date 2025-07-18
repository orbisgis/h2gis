
#@author Maël PHILIPPE CNRS
import ctypes
import json
import os
import platform

# --- Opaque structs ---
class GraalIsolate(ctypes.Structure):
    _fields_ = []

class GraalIsolateThread(ctypes.Structure):
    _fields_ = []

class GraalIsolateParams(ctypes.Structure):
    _fields_ = []

GraalIsolate_p = ctypes.POINTER(GraalIsolate)
GraalIsolateThread_p = ctypes.POINTER(GraalIsolateThread)

# Charge la lib .so
lib = ctypes.CDLL("/home/mael/Documents/programmes/h2gis-python/h2gis/lib/h2gis.so");

# Définit les signatures des fonctions natives
lib.graal_create_isolate.argtypes = [ctypes.POINTER(GraalIsolateParams), ctypes.POINTER(GraalIsolate_p), ctypes.POINTER(GraalIsolateThread_p)]
lib.graal_create_isolate.restype = ctypes.c_int

lib.graal_tear_down_isolate.argtypes = [GraalIsolateThread_p]
lib.graal_tear_down_isolate.restype = ctypes.c_int

lib.h2gis_connect.argtypes = [GraalIsolateThread_p, ctypes.c_char_p, ctypes.c_char_p, ctypes.c_char_p]
lib.h2gis_connect.restype = ctypes.c_long

lib.h2gis_execute.argtypes = [GraalIsolateThread_p, ctypes.c_long, ctypes.c_char_p]
lib.h2gis_execute.restype = ctypes.c_int

lib.h2gis_fetch.argtypes = [GraalIsolateThread_p, ctypes.c_long, ctypes.c_char_p]
lib.h2gis_fetch.restype = ctypes.c_long

lib.h2gis_fetch_rows.argtypes = [GraalIsolateThread_p, ctypes.c_long]
lib.h2gis_fetch_rows.restype = ctypes.c_char_p


lib.h2gis_free_result_buffer.argtypes = [GraalIsolateThread_p, ]
lib.h2gis_free_result_buffer.restype = ctypes.c_char_p

lib.h2gis_get_fetch_result.argtypes = [GraalIsolateThread_p, ctypes.c_long]
lib.h2gis_get_fetch_result.restype = ctypes.c_char_p


lib.h2gis_close_query.argtypes = [GraalIsolateThread_p, ctypes.c_long]
lib.h2gis_close_query.restype = None

lib.h2gis_close_connection.argtypes = [GraalIsolateThread_p, ctypes.c_long]
lib.h2gis_close_connection.restype = None

# Création de l'isolate
params = GraalIsolateParams()
isolate = GraalIsolate_p()
thread = GraalIsolateThread_p()

ret = lib.graal_create_isolate(ctypes.byref(params), ctypes.byref(isolate), ctypes.byref(thread))
if ret != 0:
    raise RuntimeError(f"Failed to create GraalVM isolate (code {ret})")

try:
    # Connexion à la base H2GIS
    db_path = b"/home/mael/test"  # chemin vers ta base H2GIS
    username = b"sa"
    password = b"sa"
    connection = lib.h2gis_connect(thread, db_path, username, password)
    if connection == 0:
        raise RuntimeError("Failed to connect to H2GIS database")

    # Exemple : suppression d'une table
    sql_create = b"DROP TABLE TEST IF EXISTS;"
    res = lib.h2gis_execute(thread, connection, sql_create)
    print(f"CREATE TABLE affected rows: {res}")

    # Exemple : création d'une table
    sql_create = b"CREATE TABLE IF NOT EXISTS TEST(id INT PRIMARY KEY, name VARCHAR(255));"
    res = lib.h2gis_execute(thread, connection, sql_create)
    print(f"CREATE TABLE affected rows: {res}")

    # Exemple : insertion
    sql_insert = b"INSERT INTO TEST(id, name) VALUES(1, 'Alice');"
    res = lib.h2gis_execute(thread, connection, sql_insert)
    print(f"INSERT affected rows: {res}")

    # Exemple : requête SELECT
    sql_select = b"SELECT * FROM test;"
    handle = lib.h2gis_fetch(thread, connection, sql_select)
    if handle == 0:
        raise RuntimeError("Failed to execute SELECT query")

    all_rows = []
    while True:
        chunk = lib.h2gis_fetch_rows(thread, handle)
        if not chunk:
            break
        chunk_str = chunk.decode("utf-8").strip()
        if chunk_str == "":
            continue
        if chunk_str.startswith("Error:"):
            lib.h2gis_close_query(thread, handle)
            raise RuntimeError(chunk_str)
        # JSON decode
        all_rows.extend(json.loads(chunk_str))

    lib.h2gis_close_query(thread, handle)

    print("Query results:", all_rows)

    # Fermeture de la connexion
    lib.h2gis_close_connection(thread, connection)

finally:
    # Destruction de l'isolate
    lib.graal_tear_down_isolate(thread)
