.. H2GIS Documentation documentation master file, created by
   sphinx-quickstart on Fri Apr  4 10:52:34 2025.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

H2GIS Documentation
=================================

Welcome on the H2GIS's documentation, a spatial extension for the `H2 database <http://www.h2database.com/>`_ engine.

.. image:: images/banner/laptop_documentation.png
  :align: center
  :alt: H2GIS banner


What is H2GIS?
-----------------

In a nutshell: H2GIS is to `H2 <http://www.h2database.com/>`_ as `PostGIS <http://postgis.net/>`_ is to `PostgreSQL <http://www.postgresql.org/>`_.

H2GIS adds support for:

* ``(MULTI)POINT``, ``(MULTI)LINESTRING`` and ``(MULTI)POLYGON`` types
* the functions specified by the `OpenGIS <http://www.opengeospatial.org/>`_ `Simple Features Implementation Specification for SQL <http://www.opengeospatial.org/standards/sfs>`_
* additional spatial functions we develop, including our graph analysis package `H2Network <./h2network.html>`_

H2GIS in action
----------------

The following is just a small sample of what H2GIS has to offer

.. |buffer| image:: ./ST_Buffer_5.png
   :align: middle

.. |isovist| image:: ./st_isovist_vannes.gif
   :align: middle
   
.. |svf| image:: ./ST_SVF_grid.png
   :align: middle

.. list-table::
   :widths: 15 75
   :align: center
   :header-rows: 0

   * - `Buffer <./ST_Buffer.html>`_
     - |buffer|
   * - `Sky View Factor <./ST_SVF.html>`_
     - |svf|
   * - `ISOvist <./ST_ISOVist.html>`_
     - |isovist|

 
Why using H2GIS?
-----------------

* Free and open-source (`LGPL v3.0 <https://github.com/orbisgis/h2gis/?tab=LGPL-3.0-1-ov-file>`_)
* Standalone: no installation or configuration needed
* Very lightweight (less than 15mb once unzipped)
* Cross-platform
* Easy to embed in (mobile) apps
* `Simple Feature SQL <https://www.ogc.org/standards/sfs/>`_ compliant
* Made by french public researchers from `CNRS <https://www.cnrs.fr>`_

Download the last version
--------------------------
You can download the last release of H2GIS `here <https://github.com/orbisgis/h2gis/releases>`_ 

Get help
-----------------

If you get stuck or just have a question, please `fill an issue <http://github.com/orbisgis/h2gis/issues/new>`_ on our GitHub repo.

Authors
--------------------------
H2GIS is leaded by scientists and engineers in GIS and computer science from `CNRS <https://cnrs.fr/fr>`_ within the French `Lab-STICC <https://labsticc.fr/fr>`_ research laboratory (DECIDE team of `Vannes <https://www.openstreetmap.org/relation/192306>`_). H2GIS is funded by (public) research projects.

**Team** : `Erwan Bocher <https://github.com/ebocher>`_, `Gwendall Petit <https://github.com/gpetit>`_, `Nicolas Fortin <https://github.com/nicolas-f>`_

**Former contributors** : Sylvain Palominos, Adam Gouge, Mireille Lecoeuvre, Alexis Guéganno

Acknowledgements
--------------------------
The H2GIS team would like to extend its warmest thanks to:

* `Thomas Mueller <https://github.com/thomasmueller>`_, `Noel Grandin <https://github.com/grandinj>`_ and `Evgenij Ryazanov <https://github.com/katzyn>`_ from the `H2 database <http://www.h2database.com/>`_ community
* Martin Davis from the `JTS Topology Suite <https://github.com/locationtech/jts>`_ community
* Michaël Michaud from `IGN <https://www.ign.fr/>`_ and `OpenJump <https://github.com/openjump-gis>`_ community, for creating JTransfoCoord, the father of `CTS <https://github.com/orbisgis/cts>`_ used to manage projections in H2GIS


--------------------------------------------------------------------------

Documentation
----------------
.. toctree::
    :maxdepth: 1
    :caption: Getting started
    
    quickstart
    data-management
    drivers
    CHANGELOG

.. toctree::
    :maxdepth: 1
    :caption: Geometry 2D functions
    
    affine-transformations
    geometry-conversion
    geometry-creation
    edit-geometries
    linear-referencing
    distance-functions
    operators
    predicates
    process-geometries
    projections
    properties
    trigonometry
    
.. toctree::
    :maxdepth: 1
    :caption: Geometry 3D functions
    
    geometry-conversion-3D
    geometry-creation-3D
    edit-geometries-3D
    distance-functions-3D
    properties-3D
    topography-3D
    triangulation-3D

.. toctree::
    :maxdepth: 1
    :caption: System functions
    
    system

.. toctree::
    :maxdepth: 1
    :caption: Applications
    
    h2network
    
.. toctree::
    :maxdepth: 1
    :caption: For developers
    
    spatial-jdbc
    function-aliases
    embedded-spatial-db
