.. H2GIS Documentation documentation master file, created by
   sphinx-quickstart on Fri Apr  4 10:52:34 2025.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

H2GIS Documentation
=================================

Welcome on the H2GIS's documentation, a spatial extension of the `H2 database <http://www.h2database.com/>`_ engine.

What is H2GIS?
-----------------

In a nutshell: H2GIS is to H2 as `PostGIS <http://postgis.net/>`_ is to `PostgreSQL <http://www.postgresql.org/>`_.

H2GIS adds support for:

* ``(MULTI)POINT``, ``(MULTI)LINESTRING`` and ``(MULTI)POLYGON`` types
* the functions specified by the `OpenGIS <http://www.opengeospatial.org/>`_ `Simple Features Implementation Specification for SQL <http://www.opengeospatial.org/standards/sfs>`_
* additional spatial functions we develop, including our graph analysis package `H2Network <./h2network.html>`_

Download the last unstable version
----------------------------------
You can download the last release of H2GIS `here <https://github.com/orbisgis/h2gis/releases>`_ 

Get help
-----------------

If you get stuck or just have a question, please `file an issue <http://github.com/orbisgis/h2gis/issues/new>`_ on our GitHub repo.


.. toctree::
    :maxdepth: 1
    :caption: Getting started
    
    quickstart
    drivers
    spatial-indices
    spatial-jdbc
    function-aliases
    embedded-spatial-db

.. toctree::
    :maxdepth: 1
    :caption: Geometry 2D
    
    affine-transformations
    geometry-conversion
    geometry-creation
    edit-geometries
    distance-functions
    operators
    predicates
    process-geometries
    projections
    properties
    trigonometry
    
.. toctree::
    :maxdepth: 1
    :caption: Geometry 3D
    
    geometry-conversion-3D
    geometry-creation-3D
    edit-geometries-3D
    distance-functions-3D
    properties-3D
    topography-3D
    triangulation-3D

.. toctree::
    :maxdepth: 1
    :caption: System
    
    system

.. toctree::
    :maxdepth: 1
    :caption: Applications
    
    h2network
