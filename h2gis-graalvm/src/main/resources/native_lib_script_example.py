
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

# Loading .so lib
lib = ctypes.CDLL("/home/mael/Documents/programmes/h2gis-python/h2gis/lib/h2gis.so");

# Define methods signatures
elf.lib.graal_create_isolate.argtypes = [
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
    GraalIsolateThread_p, ctypes.c_long, ctypes.c_char_p
]
self.lib.h2gis_fetch.restype = ctypes.c_long

self.lib.h2gis_fetch_all.argtypes = [
    GraalIsolateThread_p,
    ctypes.c_long,
    ctypes.c_void_p,
]
self.lib.h2gis_fetch_all.restype = ctypes.c_void_p

self.lib.h2gis_fetch_one.argtypes = [
    GraalIsolateThread_p,
    ctypes.c_long,
    ctypes.c_void_p,
]
self.lib.h2gis_fetch_one.restype = ctypes.c_void_p

self.lib.h2gis_free_result_set.argtypes = [GraalIsolateThread_p, ctypes.c_long]
self.lib.h2gis_free_result_set.restype = ctypes.c_long

self.lib.h2gis_free_result_buffer.argtypes = [GraalIsolateThread_p, ctypes.c_void_p]
self.lib.h2gis_free_result_buffer.restype = None

self.lib.h2gis_close_connection.argtypes = [GraalIsolateThread_p, ctypes.c_long]
self.lib.h2gis_close_connection.restype = None

self.lib.h2gis_delete_database_and_close.argtypes = [GraalIsolateThread_p, ctypes.c_long]
self.lib.h2gis_delete_database_and_close.restype = None

self.lib.h2gis_get_column_types.argtypes = [GraalIsolateThread_p, ctypes.c_long, ctypes.c_void_p]
self.lib.h2gis_get_column_types.restype = ctypes.c_void_p

# Creating the isolate
params = GraalIsolateParams()
isolate = GraalIsolate_p()
thread = GraalIsolateThread_p()

ret = lib.graal_create_isolate(ctypes.byref(params), ctypes.byref(isolate), ctypes.byref(thread))
if ret != 0:
    raise RuntimeError(f"Failed to create GraalVM isolate (code {ret})")

try:
    # Connection to the h2gis database
    db_path = b"/home/mael/test"  # path to h2gis database
    username = b"sa"
    password = b"sa"
    connection = lib.h2gis_connect(thread, db_path, username, password)
    if connection == 0:
        raise RuntimeError("Failed to connect to H2GIS database")

    # Exemple : delete table
    sql_create = b"DROP TABLE TEST IF EXISTS;"
    res = lib.h2gis_execute(thread, connection, sql_create)
    print(f"CREATE TABLE affected rows: {res}")

    # Exemple : table creation
    sql_create = b"CREATE TABLE IF NOT EXISTS TEST(id INT PRIMARY KEY, name VARCHAR(255));"
    res = lib.h2gis_execute(thread, connection, sql_create)
    print(f"CREATE TABLE affected rows: {res}")

    # Exemple : insertion
    sql_insert = b"INSERT INTO TEST(id, name) VALUES(1, 'Alice');"
    res = lib.h2gis_execute(thread, connection, sql_insert)
    print(f"INSERT affected rows: {res}")

finally:
    # Destruction de l'isolate
    lib.graal_tear_down_isolate(thread)