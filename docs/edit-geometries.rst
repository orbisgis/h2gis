Edit geometries
=================================

The following functions are available to edit geometries:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_AddPoint`
     - Add a point to a Geometry
   * - :doc:`ST_CollectionExtract`
     - Filter a Geometry with its dimension
   * - :doc:`ST_Densify`
     - Insert extra vertices along the line segments of a Geometry
   * - :doc:`ST_FlipCoordinates`
     - Flip the X and Y coordinates of a Geometry
   * - :doc:`ST_ForcePolygonCW`
     - Forces (Multi)Polygons to use a clockwise orientation
   * - :doc:`ST_ForcePolygonCCW`
     - Forces (Multi)Polygons to use a counter-clockwise orientation
   * - :doc:`ST_Normalize`
     - Return a Geometry with a normal form
   * - :doc:`ST_RemoveDuplicatedCoordinates`
     - Remove duplicated coordinates from a Geometry
   * - :doc:`ST_RemoveHoles`
     - Remove a Geometry's holes
   * - :doc:`ST_RemovePoints`
     - Return a Geometry with vertices less
   * - :doc:`ST_RemoveRepeatedPoints`
     - Remove repeated points from a Geometry
   * - :doc:`ST_Reverse`
     - Reverse the vertex order of a Geometry

.. toctree::
    :maxdepth: 1
    
    ST_AddPoint
    ST_CollectionExtract
    ST_Densify
    ST_FlipCoordinates
    ST_ForcePolygonCW
    ST_ForcePolygonCCW
    ST_Normalize
    ST_RemoveDuplicatedCoordinates
    ST_RemoveHoles
    ST_RemovePoints
    ST_RemoveRepeatedPoints
    ST_Reverse
