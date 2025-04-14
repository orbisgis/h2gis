H2Network
=================================

`H2Network` extends the `H2` / `H2GIS` geospatial database to provide graph routing functionalities. 

`H2Network` offers a collection of SQL functions on top of the `Java Network Analyzer <https://github.com/orbisgis/java-network-analyzer>`_ (JNA) library. 

JNA provides a collection of graph theory and social network analysis algorithms. These algorithms are implemented on mathematical graphs using the `JGraphT <https://jgrapht.org/>`_ library.



Install H2Network
-------------------------------

`H2Network` is delivered with the `H2GIS` binaries. So to install `H2Network` just run the following instructions.

.. code-block:: sql

  CREATE ALIAS IF NOT EXISTS H2GIS_NETWORK FOR "org.h2gis.network.functions.NetworkFunctions.load";
  CALL H2GIS_NETWORK();



H2Network functions
-------------------------------

The following SQL functions are available:

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ST_Accessibility`
     - Calculate, from each vertex, the (distance to the) closest destination
   * - :doc:`ST_ConnectedComponents`
     - Calculate the (strongly) connected components of a graph
   * - :doc:`ST_Graph`
     - Produce nodes and edges tables from an input table containing (MULTI)LINESTRINGS
   * - :doc:`ST_GraphAnalysis`
     - Calculate closeness/betweenness centrality of vertices and edges
   * - :doc:`ST_ShortestPath`
     - Calculate shortest path(s) between vertices in a graph
   * - :doc:`ST_ShortestPathLength`
     - Calculate length(s) of shortest path(s) and distance matrices
   * - :doc:`ST_ShortestPathTree`
     - Calculate shortest path tree from a vertex

.. toctree::
    :maxdepth: 1
    
    ST_Accessibility
    ST_ConnectedComponents
    ST_Graph
    ST_GraphAnalysis
    ST_ShortestPath
    ST_ShortestPathLength
    ST_ShortestPathTree

Bibliography
-------------------------------

* *Erwan Bocher, Gwendall Petit, Mireille Lecoeuvre*. **H2Network : un outil pour la modélisation et l’analyse de graphes dans le Système d’Information Géographique OrbisGIS**. [Rapport de recherche] IRSTV FR CNRS 2488; IFSTTAR. 2014. `See <https://halshs.archives-ouvertes.fr/halshs-01133333>`_ *(in french)*
* *Adam Gouge, Erwan Bocher, Nicolas Fortin, Gwendall Petit*. **H2Network: A tool for understanding the influence of urban mobility plans (UMP) on spatial accessibility**. Open Source Geospatial Research and Education Symposium 2014, Jun 2014, Espoo, Finland. ISBN: 978-952-60-5706-4 (electronic) / 978-952-60-5707-1 (printed), 2014, Proceedings of the 3rd Open Source Geospatial Research & Education Symposium OGRS 2014. `See <https://halshs.archives-ouvertes.fr/halshs-01093330/>`_



