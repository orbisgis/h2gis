
#@author Maël PHILIPPE CNRS (2025)
#@author Erwan BOCHER CNRS (2025 - 2026)
from package_example import H2GIS_DEMO as H2GIS

h2gis = H2GIS(dbPath="/tmp/db_geo", lib_path="/home/ebocher/Autres/codes/h2gis/h2gis-graalvm/target/h2gis.so")

h2gis.execute("DROP TABLE IF EXISTS geodata; CREATE TABLE geodata as select 'POINT(0 0)'::GEOMETRY AS THE_GEOM;")

print(h2gis.fetch("select * from geodata"))

