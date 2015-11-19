package org.h2gis.h2spatialext.function.spatial.raster;

import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialext.jai.FlowDirectionDescriptor;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Compute the steepest downward slope towards one of the eight adjacent or diagonal neighbors.
 * @author Nicolas Fortin
 */
public class ST_D8FlowDirection extends DeterministicScalarFunction {
    public ST_D8FlowDirection() {
        addProperty(PROP_REMARKS, "Compute the steepest downward slope direction towards one of the eight adjacent or" +
                " diagonal neighbors.");
        FlowDirectionDescriptor.register();
    }

    @Override
    public String getJavaStaticMethod() {
        return "flowDirection";
    }

    /**
     * @param geoRaster GeoRaster instance or null
     * @return Slope raster
     * @throws SQLException Error while computing raster
     * @throws IOException IO Error while computing raster
     */
    public static GeoRaster flowDirection(GeoRaster geoRaster) throws SQLException, IOException {
        if(geoRaster == null) {
            return null;
        }
        RasterUtils.RasterMetaData metadata = geoRaster.getMetaData();
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(geoRaster);
        PlanarImage output = JAI.create("D8FlowDirection", pb);
        return GeoRasterRenderedImage.create(output, metadata.scaleX, metadata.scaleY, metadata.ipX, metadata.ipY,
                metadata.skewX, metadata.skewY, metadata.srid, 0);
    }
}
