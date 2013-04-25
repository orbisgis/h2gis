/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2spatial.internal;

import java.io.IOException;
import java.sql.SQLException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * @author Bocher Erwan, Docteur en g�ographie, chercheur
 * 
 * http://www.irstv.cnrs.fr/
 * 
 * Note
 * 
 * Cette extension pour la base de données H2 a été créée entre septembre et
 * novembre 2006 pendant une courte periode de chômage ou le docteur est nulle
 * part :-(
 * 
 * Aujourd'hui en poste à l'IRSTV, H2-spatial s'officialise :-)
 * 
 * http://www.projet-sigle.org
 * 
 * @contact erwan.bocher@ec-nantes.fr
 * @version 1.0
 * @date 13/11/2006
 * @licence CeCILL
 * @see <a href="http://www.cecill.info">http://www.cecill.info</a>
 * 
 * Introduction
 * 
 * This class allows to add spatial functions to H2 database. H2 is the free SQL
 * database under Mozilla Public Licence 1.1. See :
 * http://www.h2database.com/html/frame.html and http://www.mozilla.org/MPL.
 * 
 * This class is based on an original method presented by David Blasby and Chris
 * Holmes for Hsql adn Derby databases. See
 * http://docs.codehaus.org/display/GEOS/SpatialDBBox
 * 
 * 
 * 1. Why
 * 
 * In some case we need to use a simple and portable spatial database.
 * 
 * 
 * eSDI (embedded Spatial Data Infrastructure) es un nuevo concepto y solución
 * para construir y administrar una IDE. Se basa en una observación simple. A
 * veces, especialmente en las pequeñas administraciones locales, se necesita de
 * disponer de un dispositivo mas flexible y mas portátil. Esta solución ya
 * existe en partido con la Geodatabase del software ArcGIS. Sin 6embargo su
 * utilización se limita principalmente al almacenamiento de datos o entonces
 * deben comprar extensiones especializadas como ArcSDE. Si el mundo del GIS
 * libre dispone de base de datos espaciales robustos y potentes (PostgreSQL,
 * MySQL) en cambio en muchas situaciones estas herramientas están demasiado
 * completas con relación a las necesidades y a las utilizaciones que se hacen.
 * Por eso, presentaremos un nuevo tipo de base de datos espaciales quien esta
 * basada sobre la base de datos libre en Java H2-Database. Acoplada con el SIG
 * gvSIG, H2-Espacial permite disponer de un primer nivel de IDE totalmente
 * móvil : eSDI.
 * 
 * H2-spatial is build for multiscale SDI.
 * 
 * 
 * 2.Method
 * 
 * Idea is to bind the JTS library with H2 database. See
 * http://jump-project.org/project.php?PID=JTS&SID=OVER
 * 
 * 
 * 
 * The database spatial architecture was definied by David Blasby and Chris
 * Holmes.
 * 
 * Technical architecture (Java for spatial DB) JAVA Spatial Algorthims (JTS) | |
 * GeoSpatialFunctions (java) | | CreateSpatialExtension : auto-Generated spatial DB
 * Bindings and SQL CREATE FUNCTION Bindings (java) | | JAVA DB
 * 
 * 
 * 3.Spatial storage
 * 
 * Spatial data are stored in WKb format in a BLOB datatype
 * 
 * 
 * 
 * 4.Database schema
 * 
 * Database schema is basic. A table corresponds to a geographical layer.
 * Geometry is stored in clob data types.
 * 
 * In the futur work, I'd like to implement OGC simple feature SQL
 * specifications. Database schema will be definied by : - a GEOMETRY_COLUMNS
 * table which describes the available feature tables and their Geometry
 * properties, - a SPATIAL_REF_SYS table which describes the coordinate system
 * and transformations for Geometry.
 * 
 * See : Figure 1 - Schema for features tables using predefinied data types in
 * "Implementation Specification for Geographic information - Simple feature
 * access - Part 2: SQL option"
 * 
 * 
 * 5.Technical choices
 * 
 * Database constraints - Easy to use - Easy to move - Easy to customize - Fast -
 * Written in Java - Good documentation - Clear roadmap - Dynamic community -
 * Licence GPL or compatible
 * 
 * Databases tested
 * 
 * Derby : http://db.apache.org/derby/ HSQLDB : http://www.hsqldb.org/ McKoi :
 * http://mckoi.com/database/ H2 database :
 * http://www.h2database.com/html/frame.html
 * 
 * 
 * 6.Installation
 * 
 * Unzip H2-Spatial
 * 
 * Run h2spatial.bat
 * 
 * 
 * 
 * 7.Using
 * 
 * Create a database see H2 documentation.
 * 
 * Create a table like
 * 
 * CREATE TABLE mySpatialTable(gid INT primary key, the_geom geometry);
 * 
 * Populate table
 * 
 * INSERT INTO mySpatialTable VALUES(1, GeomFromText('POINT(12,1)','1')
 * 
 * Where GeomFromText(arg0,arg1)
 * 
 * arg0 = geometry in WKT format arg1 = EPSG code
 * 
 * Load geospatialFunctions files (copy-paste) and execute it.
 * 
 * Test buffer function using :
 * 
 * SELECT buffer(the_geom, 20) FROM mySpatialTable;
 * 
 * Display available functions :
 * 
 * SELECT * FROM INFORMATION_SCHEMA.FUNCTION_ALIASES
 * 
 * 
 * 
 * 8.Work in progress
 * 
 * Create an independant tool to load gml file into H2 spatial. Currently you
 * can use geoSQLBuilder.
 * 
 * Improve spatial queries using spatial indexes.
 * 
 * Add geometry datatype in H2 database. Geometry in eWKB format.
 * 
 * 
 * 
 * 
 * 9.License
 * 
 * Don't forget this library is under CeCILL license see http://www.cecill.info/
 * 
 * 
 * 10. Download
 * 
 * Go to http://r1.bocher.free.fr
 * 
 * 
 * 11. References
 * 
 * http://docs.codehaus.org/display/GEOS/SpatialDBBox H2 database :
 * http://h2database.com PostGIS : http://postgis.refractions.net/ GeoTools :
 * http://geotools.codehaus.org/ JTS :
 * http://www.vividsolutions.com/jts/jtshome.htm Spécifications pour le stockage
 * de la géométrie dans une base de données :
 * http://www.opengeospatial.org/standards/sfs
 * 
 * 
 */

public class GeoSpatialFunctions {

	private GeoSpatialFunctions() {
	}

    /**
     * @param bytes Byte array or null
     * @return True if bytes is Geometry or if bytes is null
     */
    public static Boolean IsGeometryOrNull(byte[] bytes) {
        if(bytes==null) {
            return true;
        }
        WKBReader wkbReader = new WKBReader();
        try {
            wkbReader.read(bytes);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }
}