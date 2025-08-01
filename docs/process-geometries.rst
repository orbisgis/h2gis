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
   * - :doc:`ST_LineIntersector`
     - Split an input LINESTRING with another geometry
   * - :doc:`ST_LineMerge`
     - Merge a collection of linear components to form maximal-length LINESTRING
   * - :doc:`ST_MakeValid`
     - Make a Geometry valid
   * - :doc:`ST_Node`
     - Add nodes on a geometry for each intersection
   * - :doc:`ST_OffSetCurve`
     - Return an offset (collection of) line(s) at a given distance
   * - :doc:`ST_Polygonize`
     - Create a MULTIPOLYGON from edges of Geometries
   * - :doc:`ST_PrecisionReducer`
     - Reduce a Geometry's precision
   * - :doc:`ST_Project`
     - Project a point
   * - :doc:`ST_RingSideBuffer`
     - Compute a ring buffer on one side
   * - :doc:`ST_SideBuffer`
     - Compute a single buffer on one side
   * - :doc:`ST_Simplify`
     - Simplify a Geometry using Douglas-Peuker algorithm 
   * - :doc:`ST_SimplifyPreserveTopology`
     - Simplify a Geometry, preserving its topology
   * - :doc:`ST_SimplifyVW`
     - Simplify a Geometry using Visvalingam-Whyatt algorithm
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
    ST_LineIntersector
    ST_LineMerge
    ST_MakeValid
    ST_Node
    ST_OffSetCurve
    ST_Polygonize
    ST_PrecisionReducer
    ST_Project
    ST_RingSideBuffer
    ST_SideBuffer
    ST_Simplify
    ST_SimplifyPreserveTopology
    ST_SimplifyVW
    ST_Snap
    ST_SnapToSelf
    ST_Split
    ST_SubDivide
