package org.h2gis.h2spatialext.function.spatial.raster;

import org.h2.api.GeoRaster;
import org.h2.util.GeoRasterRenderedImage;
import org.h2.util.RasterUtils;
import org.h2.util.Utils;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialext.CreateSpatialExtension;
import org.h2gis.h2spatialext.jai.FlowAccumulationDescriptor;
import org.h2gis.utilities.JDBCUtilities;


import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
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

/**
 * Flow accumulation calculation according to the flow direction until all streams are exhausted.
 * @author Nicolas Fortin
 */
public class ST_D8FlowAccumulation extends DeterministicScalarFunction {
    public ST_D8FlowAccumulation() {
        addProperty(PROP_REMARKS, "Flow accumulation calculation according to the flow direction until all streams " +
                "are exhausted. Arguments are:" +
                "(1): Flow direction (output of ST_D8FlowDirection)");
        FlowAccumulationDescriptor.register();
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
        // TODO store intermediate results in H2 temporary table
        if(flowDirection == null) {
            return null;
        }

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
            final int maxLoop = (int)Math.sqrt(metadata.width * metadata.width + metadata.height * metadata.height);
            do {
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(weight);
                pb.addSource(flowDirection);
                if (metadata.bands[0].hasNoData) {
                    pb.add(noData);
                }
                // TODO use ROI/layer/rtree in order to compute only near non-zero weight cells
                StoredImage nextWeightBuffer = createBuffer(JAI.create("D8FlowAccumulation", pb), connection,
                        metadata, useCache);
                try {
                    weightBuffer.free();
                } finally {
                    weightBuffer = nextWeightBuffer;
                    // If something goes wrong, while freeing then last table is also cleared
                }
                RenderedImage outputWeight = weightBuffer.getImage();
                // Check if the flow is still in the new weight raster
                ParameterBlock pbMaxMin = new ParameterBlock();
                pbMaxMin.addSource(outputWeight);
                RenderedOp extremaOp = JAI.create("extrema", pbMaxMin);
                double maxValue = ((double[])extremaOp.getProperty("maximum"))[0];
                if(Double.compare(maxValue, 0.d) == 0) {
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
                }
            } while (loopId++ < maxLoop);
        } finally {
            weightBuffer.free();
            weightAccumBuffer.free();
        }
        // TODO Set noData layer to weightAccum
        return GeoRasterRenderedImage
                .create(weightAccum, metadata);
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

        public JDBCBuffer(RenderedImage image, Connection connection, RasterUtils.RasterMetaData metaData) throws
                SQLException, IOException {
            globalCpt++;
            this.connection = connection;
            tempTableName = findUniqueName();
            Statement st = connection.createStatement();
            try {
                st.execute("DROP TABLE IF EXISTS " + tempTableName);
                PreparedStatement pst = connection.prepareStatement(
                        "CREATE TABLE " + tempTableName + "(the_raster raster) as select ?;");
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
                st.execute("DROP TABLE IF EXISTS "+tempTableName);
            } finally {
                st.close();
            }
        }
    }
}
