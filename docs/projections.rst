Projections
=================================

The following projection functions are available:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_FindUTMSRID`
     - Find the UTM SRID code from a geometry
   * - :doc:`ST_IsGeographicCRS`
     - Return true is the coordinate system is geographic
   * - :doc:`ST_SetSRID`
     - Return a copy of a Geometry with a new SRID
   * - :doc:`ST_Transform`
     - Transform a Geometry from one CRS to another
   * - :doc:`UpdateGeometrySRID`
     - Update the SRID of a geometry column


.. toctree::
    :maxdepth: 1
    
    ST_FindUTMSRID
    ST_IsGeographicCRS
    ST_SetSRID
    ST_Transform
    UpdateGeometrySRID
