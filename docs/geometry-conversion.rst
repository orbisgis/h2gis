Convert geometries
=================================

The following geometry conversion functions are available:


.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_AsBinary`
     - Geometry → Well Known Binary
   * - :doc:`ST_AsEWKB`
     - Geometry → Extended Well Known Binary
   * - :doc:`ST_AsGML`
     - Geometry → GML
   * - :doc:`ST_AsText`
     - Alias for ST_AsWKT
   * - :doc:`ST_AsWKT`
     - Geometry → Well Known Text
   * - :doc:`ST_Force2D`
     - 3D or 4D Geometry → 2D Geometry
   * - :doc:`ST_GeomFromGML`
     - GML → Geometry
   * - :doc:`ST_GeomFromText`
     - Well Known Text → Geometry
   * - :doc:`ST_GeomFromWKB`
     - Well Known Binary → Geometry
   * - :doc:`ST_GeometryTypeCode`
     - Return the OGC SFS type code of a Geometry
   * - :doc:`ST_GoogleMapLink`
     - Geometry → Google map link
   * - :doc:`ST_Holes`
     - Return a Geometry's holes
   * - :doc:`ST_LineFromText`
     - Well Known Text → LINESTRING
   * - :doc:`ST_LineFromWKB`
     - Well Known Binary → LINESTRING
   * - :doc:`ST_MLineFromText`
     - Well Known Text → MULTILINESTRING
   * - :doc:`ST_MPointFromText`
     - Well Known Text → MULTIPOINT
   * - :doc:`ST_MPolyFromText`
     - Well Known Text → MULTIPOLYGON
   * - :doc:`ST_Multi`
     - SIMPLE → MULTI geometry
   * - :doc:`ST_OSMMapLink`
     - Geometry → OSM map link
   * - :doc:`ST_PointFromText`
     - Well Known Text → POINT
   * - :doc:`ST_PointFromWKB`
     - Well Known Binary → POINT
   * - :doc:`ST_PolyFromText`
     - Well Known Text → POLYGON
   * - :doc:`ST_PolyFromWKB`
     - Well Known Binary → POLYGON
   * - :doc:`ST_ToMultiLine`
     - Geometry's coordinates → MULTILINESTRING
   * - :doc:`ST_ToMultiPoint`
     - Geometry's coordinates → MULTIPOINT
   * - :doc:`ST_ToMultiSegments`
     - GEOMETRY(COLLECTION) → MULTILINESTRING


.. toctree::
    :maxdepth: 1
    
    ST_AsBinary
    ST_AsEWKB
    ST_AsGML
    ST_AsWKT
    ST_AsText
    ST_Force2D
    ST_GeomFromGML
    ST_GeomFromText
    ST_GeomFromWKB
    ST_GeometryTypeCode
    ST_GoogleMapLink
    ST_Holes
    ST_LineFromText
    ST_LineFromWKB
    ST_MLineFromText
    ST_MPointFromText
    ST_MPolyFromText
    ST_Multi
    ST_OSMMapLink
    ST_PointFromText
    ST_PointFromWKB
    ST_PolyFromText
    ST_PolyFromWKB
    ST_ToMultiLine
    ST_ToMultiPoint
    ST_ToMultiSegments

