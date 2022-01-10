# Changelog for v2.0.0

+ Add changelog, contributing, header markdown files.
+ Upgrade Java to `11`
+ Upgrade H2 to `2.0.202`
+ Upgrade to JTS 1.18.2
+ Add new function ST_VariableBuffer
+ Add new function ST_SubDivide
+ Update H2 from 2.0.202 to 2.0.204
+ Added a workaround due to the new column type name returned for geometry data type
eg GEOMETRY(POINT) instead of only GEOMETRY
+ Update H2 from 2.0.204 to 2.0.206
+ Add a new module to run sql script tests (disable by default)
+ Update ST_Force3D to be inline with PostGIS
+ Update ST_UpdateZ to force the dimension when the z value is updated.
