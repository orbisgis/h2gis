package org.h2gis.h2spatialext.function.spatial.raster;

import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2.util.Utils;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.h2gis.h2spatialext.jai.FlowAccumulationDescriptor;
import org.h2gis.h2spatialext.jai.FlowAccumulationOpImage;
import org.h2gis.h2spatialext.jai.RangeFilterDescriptor;
import org.h2gis.utilities.JDBCUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.operator.ConstantDescriptor;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        RenderedImage weight = ConstantDescriptor.create((float) metadata.width, (float) metadata.height,
                new Float[] {1.f}, null);
        RenderedImage weightAccum = ConstantDescriptor.create((float) metadata.width, (float) metadata.height,
                new Float[] {0.f}, null);

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

    private interface StoredImage {
        RenderedImage getImage() throws SQLException;
        void free() throws SQLException;
    }

    private static class NoBuffer implements StoredImage {
        private RenderedImage image;

        public NoBuffer(RenderedImage image) {
            this.image = image;
        }

        @Override
        public RenderedImage getImage() throws SQLException {
            return image;
        }

        @Override
        public void free() throws SQLException {
            image = null;
        }
    }

    private static class MemoryBuffer implements StoredImage {
        private RenderedImage imageCopy;
        public MemoryBuffer(PlanarImage image) {
            imageCopy = image.getAsBufferedImage();
        }

        @Override
        public RenderedImage getImage() {
            return imageCopy;
        }

        @Override
        public void free() {
            imageCopy = null;
        }
    }

    private static class JDBCBuffer implements StoredImage {
        private String tempTableName;
        private static int globalCpt = 0;
        private Connection connection;
        private ResultSet rs = null;
        private RenderedImage storedImage = null;
        private boolean dropIntermediate;

        public JDBCBuffer(RenderedImage image, Connection connection, RasterUtils.RasterMetaData metaData) throws
                SQLException, IOException {
            globalCpt++;
            dropIntermediate = Utils.getProperty("h2gis.dropTableCache", true);
            this.connection = connection;
            tempTableName = findUniqueName();
            Statement st = connection.createStatement();
            try {
                String tmp = "TEMPORARY";
                if(!dropIntermediate) {
                    tmp = "";
                }
                PreparedStatement pst = connection.prepareStatement(
                        "CREATE "+tmp+" TABLE " + tempTableName + "(the_raster raster) as select ?::raster;");
                try {
                    InputStream inputStream = GeoRasterRenderedImage.create(image, metaData).asWKBRaster();
                    try {
                        pst.setBinaryStream(1, inputStream);
                        pst.execute();
                    } finally {
                        inputStream.close();
                    }
                } finally {
                    pst.close();
                }
            } finally {
                st.close();
            }
        }

        @Override
        public RenderedImage getImage() throws SQLException {
            if(storedImage == null) {
                Statement st = connection.createStatement();
                rs = st.executeQuery("SELECT THE_RASTER FROM "+tempTableName);
                try {
                    rs.next();
                    storedImage = (RenderedImage)rs.getObject(1);
                } finally {
                    rs.close();
                }
            }
            return storedImage;
        }

        private String findUniqueName() throws SQLException {
            int cpt = globalCpt;
            String testName = "TMP_FLOWACCUM_"+ cpt;
            while(JDBCUtilities.tableExists(connection, testName)) {
                testName = "TMP_FLOWACCUM_"+ ++cpt;
            }
            return testName;
        }

        @Override
        public void free() throws SQLException {
            if(rs != null) {
                rs.close();
            }
            Statement st = connection.createStatement();
            try {
                if(dropIntermediate) {
                    st.execute("DROP TABLE IF EXISTS " + tempTableName);
                }
            } finally {
                st.close();
            }
        }
    }
}
