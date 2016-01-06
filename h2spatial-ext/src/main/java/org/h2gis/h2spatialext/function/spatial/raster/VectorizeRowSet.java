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
import com.vividsolutions.jts.geom.util.AffineTransformation;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import org.h2.api.GeoRaster;
import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatialext.jai.VectorizeDescriptor;

/**
 * VectorizeRowSet is used to populate a table with polygons extract from a GeoRaster.
 * 
 * @author Erwan Bocher
 */
public class VectorizeRowSet implements SimpleRowSource{
    private final GeoRaster raster;
    private boolean firstRow = true;
    private int id = 0;   
    final AffineTransformation pixelToGeoTrans;
    private final int bandIndice;
    private final List<Number> outsideValues;
    private List<Polygon> polygons;


    /**
     * 
     * @param raster the input raster
     * @param bandIndice the band indice
     * @param excludeNodata true is nodata must be excluded
     * @throws java.io.IOException
     */
    public VectorizeRowSet(GeoRaster raster, int bandIndice, boolean excludeNodata) throws IOException {
        this.raster = raster;
        this.bandIndice = bandIndice;
        RasterUtils.RasterMetaData metadata = raster.getMetaData();
        AffineTransform trans = metadata.getTransform();
        pixelToGeoTrans = new AffineTransformation(trans.getScaleX(),
                trans.getShearX(),
                trans.getTranslateX(),
                trans.getShearY(),
                trans.getScaleY(),
                trans.getTranslateY());
        RasterUtils.RasterBandMetaData band = metadata.bands[bandIndice];
        outsideValues = new ArrayList<Number>();
        if (excludeNodata) {
            if (band.hasNoData) {
                outsideValues.add(band.noDataValue);
            } else {
                outsideValues.add(0);
            }
        }
        
    }

    @Override
    public Object[] readRow() throws SQLException {
        if (firstRow) {
            reset();
        }     
        if(id>=polygons.size()){
            return null;
        }
        Polygon poly = polygons.get(id);
        poly.apply(pixelToGeoTrans);
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

    /**
     * Return the resulset
     * @return 
     * @throws java.sql.SQLException 
     */
    public ResultSet getResultSet() throws SQLException {
        SimpleResultSet srs = new SimpleResultSet(this);
        srs.addColumn("THE_GEOM", Types.JAVA_OBJECT, "GEOMETRY", 0, 0);
        srs.addColumn("ID", Types.INTEGER, 10, 0);
        srs.addColumn("VALUE", Types.DOUBLE, 10, 0);
        return srs;
    }

    /**
     * Use the vectorize JAI operator
     */
    public void vectorize() {
        ParameterBlockJAI pb = new ParameterBlockJAI("Vectorize");
        pb.setSource("source0", raster);
        pb.setParameter("band", bandIndice);
        pb.setParameter("outsideValues", outsideValues);
        RenderedOp resultVectorize = JAI.create("Vectorize", pb);
        polygons = (List<Polygon>) resultVectorize.getProperty(VectorizeDescriptor.VECTOR_PROPERTY_NAME);
    }

}
