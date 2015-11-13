package org.h2gis.h2spatialext.function.spatial.raster;

import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialext.jai.FlowAccumulationDescriptor;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ConstantDescriptor;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Flow accumulation calculation according to the flow direction until all streams are exhausted.
 * @author Nicolas Fortin
 */
public class ST_D8FlowAccumulation extends DeterministicScalarFunction {
    public ST_D8FlowAccumulation() {
        addProperty(PROP_REMARKS, "Flow accumulation calculation according to the flow direction until all streams " +
                "are exhausted. Arguments are:" +
                "(1): Flow direction (output of ST_D8FlowDirection)" +
                "(2): Optional use database as cache, slower but use less memory (true by default)");
        FlowAccumulationDescriptor.register();
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeFlow";
    }

    public static GeoRaster computeFlow(GeoRaster flowDirection) throws SQLException, IOException {
        return computeFlow(flowDirection, true);
    }

    public static GeoRaster computeFlow(GeoRaster flowDirection, boolean useCache) throws SQLException, IOException {
        // TODO store intermediate results in H2 temporary table
        if(flowDirection == null) {
            return null;
        }
        RasterUtils.RasterMetaData metadata = flowDirection.getMetaData();
        if(metadata.numBands != 1) {
            throw new SQLException("ST_D8FlowAccumulation accept only slope raster with one band");
        }
        double[] noData = null;
        if(metadata.bands[0].hasNoData) {
            noData = new double[] {metadata.bands[0].noDataValue};
        }
        // Create initial weight of 1 in each cell
        RenderedImage weight = ConstantDescriptor.create((float) metadata.width, (float) metadata.height,
                new Float[] {1.f}, null);
        RenderedImage weightAccum = ConstantDescriptor.create((float) metadata.width, (float) metadata.height,
                new Float[] {0.f}, null);
        do {
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(weight);
            pb.addSource(flowDirection);
            if (metadata.bands[0].hasNoData) {
                pb.add(noData);
            }
            // TODO use ROI/layer/rtree in order to compute only near non-zero weight cells
            RenderedImage outputWeight = JAI.create("D8FlowAccumulation", pb).getAsBufferedImage();
            // Check if the flow is still in the new weight raster
            ParameterBlock pbMaxMin = new ParameterBlock();
            pbMaxMin.addSource(outputWeight);
            RenderedOp extremaOp = JAI.create("extrema", pbMaxMin);
            double maxValue = ((double[])extremaOp.getProperty("maximum"))[0];
            if(Double.compare(maxValue, 0.d) != 0) {
                break;
            } else {
                // Add output weight to weight accum
                ParameterBlock pbAdd = new ParameterBlock();
                pbAdd.addSource(weightAccum);
                pbAdd.addSource(outputWeight);
                weightAccum = JAI.create("add", pbAdd).getAsBufferedImage();
            }
        } while (true);
        // TODO Set noData layer to weightAccum
        return GeoRasterRenderedImage
                .create(weightAccum, metadata.scaleX, metadata.scaleY, metadata.ipX, metadata.ipY, metadata.skewX,
                        metadata.skewY, metadata.srid, noData == null ? 0 : noData[0]);
    }

}
