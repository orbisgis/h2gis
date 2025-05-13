Drivers
=================================

H2GIS provides several drivers for importing from and exporting to various file types.

.. list-table:: 
   :widths: 25 75
   :header-rows: 1
   
   * - Function
     - Description
   * - :doc:`ASCRead`
     - ASC → Table
   * - :doc:`CSVRead`
     - CSV → Table
   * - :doc:`CSVWrite`
     - Table → CSV
   * - :doc:`DBFRead`
     - DBF → Table
   * - :doc:`DBFWrite`
     - Table → DBF
   * - :doc:`FILE_TABLE`
     - Link a table to a file
   * - :doc:`FGBRead`
     - FlatGeobuf → Table
   * - :doc:`FGBWrite`
     - Table → FlatGeobuf
   * - :doc:`GPXRead`
     - GPX → Table
   * - :doc:`GeoJsonRead`
     - GeoJSON → Table
   * - :doc:`GeoJsonWrite`
     - Table → GeoJSON
   * - :doc:`JsonWrite`
     - Table → JSON
   * - :doc:`KMLWrite`
     - KML, KMZ → Table
   * - :doc:`OSMRead`
     - OSM → Table
   * - :doc:`SHPRead`
     - SHP → Table
   * - :doc:`SHPWrite`
     - Table → SHP
   * - :doc:`ST_AsGeoJson`
     - Geometry → GeoJSON
   * - :doc:`ST_AsKML`
     - Geometry → KML
   * - :doc:`ST_GeomFromGeoJson`
     - GeoJSON → Geometry
   * - :doc:`ST_OSMDownloader`
     - OSM → File
   * - :doc:`TSVRead`
     - TSV → Table
   * - :doc:`TSVWrite`
     - Table → TSV

.. toctree::
    :maxdepth: 1

    ASCRead
    CSVRead
    CSVWrite
    DBFRead
    DBFWrite
    FILE_TABLE
    FGBRead
    FGBWrite
    GPXRead    
    GeoJsonRead
    GeoJsonWrite
    JsonWrite
    KMLWrite
    OSMRead
    SHPRead
    SHPWrite
    ST_AsGeoJson
    ST_AsKML
    ST_GeomFromGeoJson
    ST_OSMDownloader
    TSVRead
    TSVWrite
