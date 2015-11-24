package org.h2gis.h2spatialext.function.spatial.raster;

import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2.util.Utils;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.h2gis.h2spatialext.function.spatial.raster.cache.JDBCBuffer;
import org.h2gis.h2spatialext.function.spatial.raster.cache.MemoryBuffer;
import org.h2gis.h2spatialext.function.spatial.raster.cache.NoBuffer;
import org.h2gis.h2spatialext.function.spatial.raster.cache.StoredImage;
import org.h2gis.h2spatialext.jai.FlowAccumulationDescriptor;
import org.h2gis.h2spatialext.jai.FlowAccumulationOpImage;
import org.h2gis.h2spatialext.jai.RangeFilterDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.Histogram;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.ConstantDescriptor;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Flow accumulation calculation according to the flow direction until all streams are exhausted.
 * @author Nicolas Fortin
 */
public class ST_D8FlowAccumulation extends DeterministicScalarFunction {
    public static final String PROP_LOG_FLOWACCUM_STATS = "h2gis.logflowaccumstats";
    private static final Logger LOGGER = LoggerFactory.getLogger(ST_D8FlowAccumulation.class);


    public ST_D8FlowAccumulation() {
        addProperty(PROP_REMARKS, "Flow accumulation calculation according to the flow direction until all streams " +
                "are exhausted. Arguments are:" +
                "(1): Flow direction (output of ST_D8FlowDirection)");
        FlowAccumulationDescriptor.register();
        RangeFilterDescriptor.register();
    }

    @Override
    public String getJavaStaticMethod() {
        return "computeFlow";
    }

    public static GeoRaster computeFlow(Connection connection, GeoRaster flowDirection) throws SQLException, IOException {
        return doComputeFlow(connection, flowDirection, !Utils.getProperty("h2gis.RasterProcessingInMemory",
                CreateSpatialExtension.DEFAULT_RASTER_PROCESSING_IN_MEMORY));
    }

    public static GeoRaster doComputeFlow(Connection connection, GeoRaster flowDirection, boolean useCache) throws
            SQLException,
            IOException {
        if(flowDirection == null) {
            return null;
        }
        boolean printStats = Utils.getProperty(PROP_LOG_FLOWACCUM_STATS, false);
        RasterUtils.RasterMetaData metadata = flowDirection.getMetaData();
        if(metadata.numBands != 1) {
            throw new SQLException("ST_D8FlowAccumulation accept only slope raster with one band");
        }
        // External border initial weight and external border direction
        double[] noData = new double[] {1., 0};
        if(metadata.bands[0].hasNoData) {
            noData = new double[] {1.,metadata.bands[0].noDataValue};
        }
        // Create initial weight of 1 in each cell
        RenderingHints renderingHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, new ImageLayout(flowDirection));
        RenderedImage weight = ConstantDescriptor.create((float) metadata.width, (float) metadata.height,
                new Float[] {1.f}, renderingHints);
        RenderedImage weightAccum = ConstantDescriptor.create((float) metadata.width, (float) metadata.height,
                new Float[] {0.f}, renderingHints);

        StoredImage weightBuffer = new NoBuffer(weight);
        StoredImage weightAccumBuffer = new NoBuffer(weightAccum);

        try {
            int loopId = 0;
            do {
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(weight);
                pb.addSource(flowDirection);
                if (metadata.bands[0].hasNoData) {
                    pb.add(noData);
                }
                // TODO Use ROI/layer/rtree in order to compute and store only near non-zero weight cells

                PlanarImage flowAccum = JAI.create("D8FlowAccumulation", pb);
                AtomicBoolean hasRemainingFlow = (AtomicBoolean)flowAccum.getProperty(
                        FlowAccumulationOpImage.PROPERTY_NON_ZERO_FLOW_ACCUM);
                StoredImage nextWeightBuffer = createBuffer(flowAccum, connection,
                        metadata, useCache);
                //hasRemainingFlow is computed from here
                try {
                    weightBuffer.free();
                } finally {
                    weightBuffer = nextWeightBuffer;
                    // If something goes wrong, while freeing then last table is also cleared
                }
                RenderedImage outputWeight = weightBuffer.getImage();
                if(printStats) {
                    ParameterBlock pb1 = new ParameterBlock();
                    pb1.addSource(outputWeight);
                    pb1.add(null);//region of interest
                    pb1.add(1); // Sampling
                    pb1.add(1); // Sampling
                    pb1.add(new int[]{2}); // Bins
                    pb1.add(new double[]{0});
                    pb1.add(new double[]{0.1}); // Range for inclusion
                    PlanarImage dummyImage1 = JAI.create("histogram", pb1);
                    Histogram histo1 = (Histogram)dummyImage1.getProperty("histogram");
                    int pixelCount = weight.getWidth() * weight.getHeight();
                    LOGGER.info("Step " + (loopId + 1) + " non-zero output weight pixel " + (pixelCount - histo1
                            .getSubTotal(0, 0, 0)) + "/" + pixelCount+ " pixels");
                }
                // Check if the flow is still in the new weight raster
                if(!hasRemainingFlow.get()) {
                    // The flow has stopped, accumulation is complete
                    break;
                } else {
                    // Add output weight to weight accum
                    ParameterBlock pbAdd = new ParameterBlock();
                    pbAdd.addSource(weightAccum);
                    pbAdd.addSource(outputWeight);
                    StoredImage nextWeightAccumBuffer = createBuffer(JAI.create("add", pbAdd), connection,
                            metadata, useCache);
                    try {
                        weightAccumBuffer.free();
                    } finally {
                        weightAccumBuffer = nextWeightAccumBuffer;
                    }
                    weightAccum = weightAccumBuffer.getImage();
                    weight = outputWeight;
                    loopId++;
                }
            } while (true);
        } finally {
            weightBuffer.free();
        }
        // Set noData layer on weightAccum
        ParameterBlock filterParam = new ParameterBlock();
        filterParam.addSource(weightAccum); // Source to copy
        filterParam.addSource(flowDirection); // source with nodata
        double[][] filterNodata = new double[metadata.numBands][2];
        for(int idBand = 0; idBand < filterNodata.length; idBand++) {
            final RasterUtils.RasterBandMetaData bandMetaData = metadata.bands[idBand];
            if(bandMetaData.hasNoData) {
                filterNodata[idBand] = new double[]{bandMetaData.noDataValue, bandMetaData.noDataValue};
            } else {
                filterNodata[idBand] = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
            }
        }
        filterParam.add(filterNodata);
        filterParam.add(true); // Return nodata when dir is nodata
        return GeoRasterRenderedImage
                .create(JAI.create("RangeFilter", filterParam), metadata);
    }

    private static StoredImage createBuffer(PlanarImage image, Connection connection, RasterUtils.RasterMetaData
            metaData, boolean cacheRaster)
            throws
            SQLException, IOException {
        if(cacheRaster) {
            return new JDBCBuffer(image, connection, metaData);
        } else {
            return new MemoryBuffer(image);
        }
    }
}
