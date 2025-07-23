Edit geometries
=================================

The following functions are available to edit 3D geometries:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_AddZ`
     - Add a value to the z-coordinate of a Geometry
   * - :doc:`ST_Interpolate3DLine`
     - Return a Geometry with a interpolation of z values.
   * - :doc:`ST_MultiplyZ`
     - Return a Geometry's z-values by a factor
   * - :doc:`ST_Reverse3DLine`
     - Reverse a Geometry thanks to the z-values of its first & last coordinates
   * - :doc:`ST_UpdateZ`
     - Update the z-values of a Geometry
   * - :doc:`ST_ZUpdateLineExtremities`
     - Update the start and end z-values of a Geometry

.. toctree::
    :maxdepth: 1
    
    ST_AddZ
    ST_Interpolate3DLine
    ST_MultiplyZ
    ST_Reverse3DLine
    ST_UpdateZ
    ST_ZUpdateLineExtremities
