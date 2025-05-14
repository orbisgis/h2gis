Properties
=================================

The following property functions are available:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_Boundary`
     - Return a Geometry's boundary
   * - :doc:`ST_Centroid`
     - Return the centroid of a Geometry
   * - :doc:`ST_CompactnessRatio`
     - Return the square root of a POLYGON's area divided by the area of the circle with circumference equal to its perimeter
   * - :doc:`ST_CoordDim`
     - Return the dimension of the coordinates of a Geometry
   * - :doc:`ST_Dimension`
     - Return the dimension of a Geometry
   * - :doc:`ST_Distance`
     - Return the distance between two Geometries
   * - :doc:`ST_DistanceSphere`
     - Return the minimum distance between two points
   * - :doc:`ST_EndPoint`
     - Return the last coordinate of a LINESTRING
   * - :doc:`ST_Envelope`
     - Return a Geometry's envelope as a Geometry
   * - :doc:`ST_Explode`
     - Explode GEOMETRYCOLLECTIONs into multiple Geometries
   * - :doc:`ST_Extent`
     - Return the minimum bounding box of a GEOMETRYCOLLECTION
   * - :doc:`ST_ExteriorRing`
     - Return the exterior ring of a POLYGON
   * - :doc:`ST_GeometryN`
     - Return the nth Geometry of a GEOMETRYCOLLECTION
   * - :doc:`ST_GeometryType`
     - Return the type of a Geometry
   * - :doc:`ST_InteriorRingN`
     - Return the nth interior ring of a POLYGON
   * - :doc:`ST_IsClosed`
     - Return true if a Geometry is a closed LINESTRING or MULTILINESTRING
   * - :doc:`ST_IsEmpty`
     - Return true if a Geometry is empty
   * - :doc:`ST_IsRectangle`
     - Return true if the Geometry is a rectangle
   * - :doc:`ST_IsRing`
     - Return true if a LINESTRING is ring
   * - :doc:`ST_IsSimple`
     - Return true if a Geometry is simple
   * - :doc:`ST_IsValid`
     - Return true if the Geometry is valid
   * - :doc:`ST_IsValidDetail`
     - Return a valid detail as an array of objects
   * - :doc:`ST_IsValidReason`
     - Return text stating if a geometry is valid or not and if not valid, a reason why
   * - :doc:`ST_NPoints`
     - Return the number of points in a Geometry
   * - :doc:`ST_NumGeometries`
     - Return the number of Geometries in a GEOMETRYCOLLECTION
   * - :doc:`ST_NumInteriorRing`
     - Alias for ST_NumInteriorRings
   * - :doc:`ST_NumInteriorRings`
     - Return the number of interior rings of a Geometry
   * - :doc:`ST_NumPoints`
     - Return the number of points in a Linestring
   * - :doc:`ST_PointN`
     - Return the nth point of a LINESTRING
   * - :doc:`ST_PointOnSurface`
     - Return an interior or boundary point of a Geometry
   * - :doc:`ST_SRID`
     - Return a SRID value
   * - :doc:`ST_StartPoint`
     - Return the first coordinate of a LINESTRING
   * - :doc:`ST_X`
     - Return the x-value of the first coordinate of a Geometry
   * - :doc:`ST_XMax`
     - Return the maximum x-value of a Geometry
   * - :doc:`ST_XMin`
     - Return the minimum x-value of a Geometry
   * - :doc:`ST_Y`
     - Return the y-value of the first coordinate of a Geometry
   * - :doc:`ST_YMax`
     - Return the maximum y-value of a Geometry
   * - :doc:`ST_YMin`
     - Return the minimum y-value of a Geometry

.. toctree::
    :maxdepth: 1
    
    ST_Boundary
    ST_Centroid
    ST_CompactnessRatio
    ST_CoordDim
    ST_Dimension
    ST_Distance
    ST_DistanceSphere
    ST_EndPoint
    ST_Envelope
    ST_Explode
    ST_Extent
    ST_ExteriorRing
    ST_GeometryN
    ST_GeometryType
    ST_InteriorRingN
    ST_IsClosed
    ST_IsEmpty
    ST_IsRectangle
    ST_IsRing
    ST_IsSimple
    ST_IsValid
    ST_IsValidDetail
    ST_IsValidReason
    ST_NPoints
    ST_NumGeometries
    ST_NumInteriorRing
    ST_NumInteriorRings
    ST_NumPoints
    ST_PointN
    ST_PointOnSurface
    ST_SRID
    ST_StartPoint
    ST_X
    ST_XMax
    ST_XMin
    ST_Y
    ST_YMax
    ST_YMin
