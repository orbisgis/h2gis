Predicates
=================================

The following predicate functions are available:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_Contains`
     - Return true if Geometry A contains Geometry B
   * - :doc:`ST_CoveredBy`
     - eturns true if Geometry A is covered by Geometry B
   * - :doc:`ST_Covers`
     - Return true if no point in Geometry B is outside Geometry A
   * - :doc:`ST_Crosses`
     - Return true if Geometry A crosses Geometry B
   * - :doc:`ST_DWithin`
     - Return true if the Geometries are within the specified distance of one another
   * - :doc:`ST_Disjoint`
     - Return true Geometries A and B are disjoint
   * - :doc:`ST_EnvelopesIntersect`
     - Return true if the envelope of Geometry A intersects the envelope of Geometry B
   * - :doc:`ST_Equals`
     - Return true if Geometry A equals Geometry B
   * - :doc:`ST_Intersects`
     - Return true if Geometry A intersects Geometry B
   * - :doc:`ST_OrderingEquals`
     - Returns TRUE if Geometry A equals Geometry B and their coordinates and component Geometries are listed in the same order
   * - :doc:`ST_Overlaps`
     - Return true if Geometry A overlaps Geometry B
   * - :doc:`ST_Relate`
     - Return the DE-9IM intersection matrix of the two Geometries or true if they are related by the given intersection matrix
   * - :doc:`ST_Touches`
     - Return true if Geometry A touches Geometry B
   * - :doc:`ST_Within`
     - Return true if Geometry A is within Geometry B


.. toctree::
    :maxdepth: 1
    
    ST_Contains
    ST_CoveredBy
    ST_Covers
    ST_Crosses
    ST_DWithin
    ST_Disjoint
    ST_EnvelopesIntersect
    ST_Equals
    ST_Intersects
    ST_OrderingEquals
    ST_Overlaps
    ST_Relate
    ST_Touches
    ST_Within
