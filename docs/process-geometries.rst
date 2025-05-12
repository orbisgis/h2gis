Process geometries
=================================

The following functions are available to process geometries:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_Clip`
     - Clip one geometry with another one
   * - :doc:`ST_ISOVist`
     - Compute the visibility from a point
   * - :doc:`ST_LineInterpolatePoint`
     - Return a point along (MULTI)LINESTRING based on starting fraction
   * - :doc:`ST_LineIntersector`
     - Split an input LINESTRING with another geometry
   * - :doc:`ST_LineMerge`
     - Merge a collection of linear components to form maximal-length LINESTRING
   * - :doc:`ST_LineSubstring`
     - Return a Linestring along (MULTI)LINESTRING based on starting & ending fractions
   * - :doc:`ST_MakeValid`
     - Make a Geometry valid
   * - :doc:`ST_Node`
     - Add nodes on a geometry for each intersection
   * - :doc:`ST_Polygonize`
     - Create a MULTIPOLYGON from edges of Geometries
   * - :doc:`ST_PrecisionReducer`
     - Reduce a Geometry's precision
   * - :doc:`ST_RingSideBuffer`
     - Compute a ring buffer on one side
   * - :doc:`ST_SideBuffer`
     - Compute a single buffer on one side
   * - :doc:`ST_Simplify`
     - Simplify a Geometry
   * - :doc:`ST_SimplifyPreserveTopology`
     - Simplify a Geometry, preserving its topology
   * - :doc:`ST_Snap`
     - Snap two Geometries together
   * - :doc:`ST_SnapToSelf`
     - Snap a Geometries to itself
   * - :doc:`ST_Split`
     - Split Geometry A by Geometry B
   * - :doc:`ST_SubDivide`
     - Divides geometry into parts

.. toctree::
    :maxdepth: 1
    
    ST_Clip
    ST_ISOVist
    ST_LineInterpolatePoint
    ST_LineIntersector
    ST_LineMerge
    ST_LineSubstring
    ST_MakeValid
    ST_Node
    ST_Polygonize
    ST_PrecisionReducer
    ST_RingSideBuffer
    ST_SideBuffer
    ST_Simplify
    ST_SimplifyPreserveTopology
    ST_Snap
    ST_SnapToSelf
    ST_Split
    ST_SubDivide
