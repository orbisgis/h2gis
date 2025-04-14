Properties
=================================

The following 3D property functions are available:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_Is3D`
     - Return ``1`` if a Geometry has at least one z-coordinate; ``0`` otherwise
   * - :doc:`ST_Z`
     - Return the z-value of the first coordinate of a Geometry
   * - :doc:`ST_ZMax`
     - Return the maximum z-value of a Geometry
   * - :doc:`ST_ZMin`
     - Return the minimum z-value of a Geometry

.. toctree::
    :maxdepth: 1
    
    ST_Is3D
    ST_Z
    ST_ZMax
    ST_ZMin
