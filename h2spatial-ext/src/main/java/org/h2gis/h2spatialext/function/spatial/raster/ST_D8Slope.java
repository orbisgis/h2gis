package org.h2gis.h2spatialext.function.spatial.raster;

import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialext.jai.SlopeDescriptor;

import javax.media.jai.EnumeratedParameter;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

/**
 * Compute the steepest downward slope towards one of the eight adjacent or diagonal neighbors.
 * @author Nicolas Fortin
 */
public class ST_D8Slope extends DeterministicScalarFunction {
    public ST_D8Slope() {
        addProperty(PROP_REMARKS, "Compute the steepest downward slope towards one of the eight adjacent or diagonal" +
                " neighbors.");
        SlopeDescriptor.register();
    }

    @Override
    public String getJavaStaticMethod() {
        return "slope";
    }


    private static GeoRaster computeSlope(GeoRaster geoRaster, EnumeratedParameter unitType) throws SQLException, IOException {
        RasterUtils.RasterMetaData metadata = geoRaster.getMetaData();
        if(geoRaster.getMetaData().numBands != 1) {
            throw new SQLException("ST_Slope accept only raster with one band");
        }
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(geoRaster);
        pb.add(unitType);
        PlanarImage output = JAI.create("D8Slope", pb);
        return GeoRasterRenderedImage.create(output, metadata.scaleX,
                metadata.scaleY, metadata.ipX, metadata.ipY, metadata.skewX, metadata.skewY, metadata.srid,
                0);
    }

    public static GeoRaster slope(GeoRaster geoRaster) throws SQLException, IOException {
        if(geoRaster == null) {
            return null;
        }
        return computeSlope(geoRaster, SlopeDescriptor.SLOPE_DEGREE);
    }

    public static GeoRaster slope(GeoRaster geoRaster, String unit) throws SQLException, IOException {
        if(geoRaster == null) {
            return null;
        }
        EnumeratedParameter unitType = null;
        for(EnumeratedParameter param : (Set<EnumeratedParameter>)SlopeDescriptor.VALID_PARAM_VALUES[0]) {
            if(param.getName().equalsIgnoreCase(unit)) {
                unitType = param;
                break;
            }
        }
        if(unitType == null) {
            throw new SQLException("Unknown unit "+unit);
        }
        return computeSlope(geoRaster, unitType);
    }
}
