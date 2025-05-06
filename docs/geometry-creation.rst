Create geometries
=================================

The following geometry creation functions are available:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_Accum`
     - Construct an array of Geometries
   * - :doc:`ST_BoundingCircle`
     - Return the minimum bounding circle of a Geometry
   * - :doc:`ST_Collect`
     - Construct an array of Geometries
   * - :doc:`ST_Expand`
     - Expand a Geometry's envelope
   * - :doc:`ST_GeneratePoints`
     - (MULTI)POLYGON → random points   
   * - :doc:`ST_GeneratePointsInGrid`
     - (MULTI)POLYGON → grided points         
   * - :doc:`ST_MakeEllipse`
     - Construct an ellipse
   * - :doc:`ST_MakeEnvelope`
     - Create a rectangular Polygon
   * - :doc:`ST_MakeGrid`
     - Calculate a regular grid of POLYGONs based on a Geometry or a table
   * - :doc:`ST_MakeGridPoints`
     - Calculate a regular grid of POINTs based on a Geometry or a table
   * - :doc:`ST_MaximumInscribedCircle`
     - Calculate the largest circle contained within a Geometry
   * - :doc:`ST_MakeLine`
     - (MULTI)POINT → LINESTRING
   * - :doc:`ST_MakePoint`
     - Construct a POINT from two or three coordinates
   * - :doc:`ST_MakePolygon`
     - LINESTRING → POLYGON
   * - :doc:`ST_MinimumDiameter`
     - Return the minimum diameter of a Geometry
   * - :doc:`ST_MinimumRectangle`
     - Return the minimum rectangle enclosing a Geometry
   * - :doc:`ST_OctogonalEnvelope`
     - Return the octogonal envelope of a Geometry
   * - :doc:`ST_Point`
     - 	Construct a POINT from two or three coordinates
   * - :doc:`ST_RingBuffer`
     - Return a MULTIPOLYGON of buffers centered at a Geometry and of increasing buffer Size

.. toctree::
    :maxdepth: 1
    
    ST_Accum
    ST_BoundingCircle
    ST_Collect    
    ST_Expand
    ST_GeneratePoints
    ST_GeneratePointsInGrid
    ST_MakeEllipse    
    ST_MakeEnvelope    
    ST_MakeGrid    
    ST_MakeGridPoints
    ST_MaximumInscribedCircle
    ST_MakeLine    
    ST_MakePoint    
    ST_MakePolygon    
    ST_MinimumDiameter
    ST_MinimumRectangle
    ST_OctogonalEnvelope    
    ST_Point
    ST_RingBuffer






