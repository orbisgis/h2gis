Measures
=================================

The following measurements functions are available:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_Area`
     - Return a Geometry's area
   * - :doc:`ST_ClosestCoordinate`
     - Return the coordinate(s) of a Geometry closest to a POINT
   * - :doc:`ST_ClosestPoint`
     - Return the point of Geometry A closest to Geometry B
   * - :doc:`ST_FurthestCoordinate`
     - Compute the furthest coordinate(s) contained in a Geometry starting from a POINT
   * - :doc:`ST_Length`
     - Return the length of a Linestring
   * - :doc:`ST_LocateAlong`
     - Return a MULTIPOINT containing points along the line segments of a Geometry at a given segment length fraction and offset distance
   * - :doc:`ST_LongestLine`
     - Returns the 2-dimensional longest LINESTRING between the points of two geometries
   * - :doc:`ST_MaxDistance`
     - Compute the maximum distance between two geometries
   * - :doc:`ST_Perimeter`
     - Return the perimeter of a (multi)polygon
   * - :doc:`ST_ProjectPoint`
     - Project a POINT on a (MULTI)LINESTRING
   * - :doc:`ST_ShortestLine`
     - Returns the 2-dimensional shortest LINESTRING between two geometries

.. toctree::
    :maxdepth: 1
    
    ST_Area
    ST_ClosestCoordinate
    ST_ClosestPoint
    ST_FurthestCoordinate 
    ST_Length
    ST_LocateAlong
    ST_LongestLine
    ST_MaxDistance
    ST_Perimeter
    ST_ProjectPoint
    ST_ShortestLine
