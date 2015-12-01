/*
 * Copyright (C) 2015 CNRS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.h2gis.h2spatialext.function.spatial.raster;

import com.vividsolutions.jts.geom.Polygon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatialext.jai.VectorizeDescriptor;

/**
 * VectorizeRowSet is used to populate a table with polygons extract from a GeoRaster.
 * 
 * @author Erwan Bocher
 */
public class VectorizeRowSet implements SimpleRowSource{
    private final GeoRasterRenderedImage raster;
    private final boolean excludeNodata;
    private boolean firstRow = true;
    private int id = 0;
    private List<Polygon> polygons;

    public VectorizeRowSet(GeoRasterRenderedImage raster, boolean excludeNodata) {
            this.raster=raster;
            this.excludeNodata =excludeNodata;
    }

    @Override
    public Object[] readRow() throws SQLException {
        if (firstRow) {
            reset();
        }     
        Polygon poly = polygons.get(id);
        return new Object[]{poly, id++, (Double) poly.getUserData()};        
    }

    @Override
    public void close() {
    }

    @Override
    public void reset() throws SQLException {
        firstRow = false;
        if (raster == null) {
            throw new SQLException("The input raster cannot be null.");
        } else {
            vectorize();
        }

    }

    public ResultSet getResultSet() {
        SimpleResultSet srs = new SimpleResultSet(this);
        srs.addColumn("THE_GEOM", Types.JAVA_OBJECT, "GEOMETRY", 0, 0);
        srs.addColumn("ID", Types.INTEGER, 10, 0);
        srs.addColumn("VALUE", Types.DOUBLE, 0, 0);
        return srs;
    }

    /**
     * Use vectorize JAI operator
     */
    private void vectorize() {
        ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
        pb.setSource("source0", raster);       
        if (excludeNodata) {
            RasterUtils.RasterBandMetaData bandInfo = raster.getMetaData().bands[0];
            if (bandInfo.hasNoData) {
                pb.setParameter("outsideValues", bandInfo.noDataValue);
            }
        }
        RenderedOp resultVectorize = JAI.create("Vectorize", pb);
        polygons = (List<Polygon>) resultVectorize.getProperty(VectorizeDescriptor.VECTOR_PROPERTY_NAME);
    }
    
}
