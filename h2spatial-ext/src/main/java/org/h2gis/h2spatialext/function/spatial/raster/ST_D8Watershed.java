/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.h2spatialext.function.spatial.raster;

import com.vividsolutions.jts.geom.*;
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
import org.h2gis.h2spatialext.jai.IndexOutletDescriptor;
import org.h2gis.h2spatialext.jai.IndexPropagationDescriptor;
import org.h2gis.h2spatialext.jai.IndexPropagationOpImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.jai.Histogram;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.TiledImage;
import javax.media.jai.operator.ConstantDescriptor;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Extract all watersheds or one based on a list of pixels location.
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class ST_D8Watershed extends DeterministicScalarFunction {
    public static final String PROP_LOG_INDEX_PROPAGATION_STATS = "h2gis.logindexpropagationstats";
    private static final Logger LOGGER = LoggerFactory.getLogger(ST_D8Watershed.class);

    public ST_D8Watershed() {
        addProperty(PROP_REMARKS, "Extract all watersheds or one based on a pixel location.");
        IndexOutletDescriptor.register();
        IndexPropagationDescriptor.register();
    }

    @Override
    public String getJavaStaticMethod() {
        return "watershed";
    }

    public static GeoRaster doWatershed(Connection connection, GeoRaster flowDirection, boolean useCache,
            Coordinate[] coordinates) throws
            SQLException, IOException {
        if(flowDirection == null) {
            return null;
        }
        boolean printStats = Utils.getProperty(PROP_LOG_INDEX_PROPAGATION_STATS, false);
        RasterUtils.RasterMetaData metadata = flowDirection.getMetaData();
        if(metadata.numBands != 1) {
            throw new SQLException("ST_D8FlowAccumulation accept only slope raster with one band");
        }
        // Compute outlets to propagate
        StoredImage outletsPropaBuffer;
        RenderedImage outletsPropa;
        StoredImage outletsBuffer;
        RenderedImage outlets;
        if(coordinates.length == 0) {
            // Find all outlets
            PlanarImage compOutlets = JAI.create("IndexOutlet", flowDirection);
            outletsBuffer = createBuffer(compOutlets, connection, metadata, useCache);
            outlets = outletsBuffer.getImage();
            outletsPropa = outlets;
            outletsPropaBuffer = new NoBuffer(outlets);
        } else {
            // Use TiledImage as an empty matrix. Only tiles that will store an outlet will be kept in memory (until
            // next loop)
            RenderingHints renderingHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, new ImageLayout(flowDirection));
            RenderedImage nonOutlet = ConstantDescriptor
                    .create((float) metadata.width, (float) metadata.height, new Byte[]{0}, renderingHints);
            TiledImage tiledImage = new TiledImage(nonOutlet, flowDirection.getTileWidth() ,
                    flowDirection.getTileHeight());
            int outletIndex = 1;
            for(Coordinate coordinate : coordinates) {
                // Convert coordinates to pixel index
                int[] pixelIndex = metadata.getPixelFromCoordinate(coordinate);
                if(pixelIndex[0] >= tiledImage.getMinX() && pixelIndex[0] < tiledImage.getMaxX() &&
                        pixelIndex[1] >= tiledImage.getMinY() && pixelIndex[1] < tiledImage.getMaxY())
                tiledImage.setSample(pixelIndex[0], pixelIndex[1], 0, outletIndex++);
            }
            outlets = tiledImage;
            outletsBuffer = new NoBuffer(outlets);
            outletsPropa = tiledImage;
            outletsPropaBuffer = new NoBuffer(outletsPropa);
        }

        try {
            int loopId = 0;
            do {
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(outlets);
                pb.addSource(flowDirection);
                if (metadata.bands[0].hasNoData) {
                    pb.add(metadata.bands[0].noDataValue);
                }
                // TODO Use ROI/layer/rtree in order to compute and store only near non-zero outlet index cells

                PlanarImage outletsImage = JAI.create("IndexPropagation", pb);
                AtomicBoolean hasDoneIndexCopy = (AtomicBoolean)outletsImage.getProperty(
                        IndexPropagationOpImage.PROPERTY_EFFECTIVE_INDEX_COPY);
                StoredImage nextOutletsBuffer = createBuffer(outletsImage, connection,
                        metadata, useCache);
                //hasRemainingFlow is computed from here
                try {
                    outletsBuffer.free();
                } finally {
                    outletsBuffer = nextOutletsBuffer;
                    // If something goes wrong, while freeing then last table is also cleared
                }
                RenderedImage outputOutlets = outletsBuffer.getImage();
                if(printStats) {
                    ParameterBlock pb1 = new ParameterBlock();
                    pb1.addSource(outputOutlets);
                    pb1.add(null);//region of interest
                    pb1.add(1); // Sampling
                    pb1.add(1); // Sampling
                    pb1.add(new int[]{2}); // Bins
                    pb1.add(new double[]{0});
                    pb1.add(new double[]{0.1}); // Range for inclusion
                    PlanarImage dummyImage1 = JAI.create("histogram", pb1);
                    Histogram histo1 = (Histogram)dummyImage1.getProperty("histogram");
                    int pixelCount = outlets.getWidth() * outlets.getHeight();
                    LOGGER.info("Step " + (loopId + 1) + " non-zero output weight pixel " + (pixelCount - histo1
                            .getSubTotal(0, 0, 0)) + "/" + pixelCount+ " pixels");
                }
                // Check if the flow is still in the new weight raster
                if(!hasDoneIndexCopy.get()) {
                    // The flow has stopped, accumulation is complete
                    break;
                } else {
                    // Add output weight to weight accum
                    ParameterBlock pbAdd = new ParameterBlock();
                    pbAdd.addSource(outletsPropa);
                    pbAdd.addSource(outputOutlets);
                    StoredImage nextOutletsPropaBuffer = createBuffer(JAI.create("add", pbAdd), connection,
                            metadata, useCache);
                    try {
                        outletsPropaBuffer.free();
                    } finally {
                        outletsPropaBuffer = nextOutletsPropaBuffer;
                    }
                    outletsPropa = outletsPropaBuffer.getImage();
                    outlets = outputOutlets;
                    loopId++;
                }
            } while (true);
        } finally {
            outletsBuffer.free();
        }
        // Set noData layer on watershed raster
        ParameterBlock filterParam = new ParameterBlock();
        filterParam.addSource(outletsPropa); // Source to copy
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

    /**
     * @param flowDirection Flow direction
     * @return Watershed raster
     * @throws SQLException Error while computing raster
     * @throws IOException IO Error while computing raster
     */
    public static GeoRaster watershed(Connection connection, GeoRaster flowDirection) throws SQLException, IOException {
        return doWatershed(connection, flowDirection, !Utils.getProperty("h2gis.RasterProcessingInMemory",
                CreateSpatialExtension.DEFAULT_RASTER_PROCESSING_IN_MEMORY), new Coordinate[0]);
    }

    /**
     * @param connection JDBC Connection
     * @param flowDirection Flow direction
     * @param geometry Point or MultiPoint
     * @return Watershed raster
     * @throws SQLException Error while computing raster
     * @throws IOException IO Error while computing raster
     */
    public static GeoRaster watershed(Connection connection, GeoRaster flowDirection, Geometry geometry)
            throws SQLException, IOException {
        if(geometry == null) {
            return null;
        }
        List<Coordinate> coordinates = new ArrayList<Coordinate>(geometry.getNumPoints());
        if(geometry instanceof com.vividsolutions.jts.geom.Point) {
            coordinates.add(geometry.getCoordinate());
        } else if(geometry instanceof GeometryCollection) {
            for(int geomId = 0; geomId < geometry.getNumGeometries(); geomId++) {
                Geometry geom = geometry.getGeometryN(geomId);
                if(geom instanceof com.vividsolutions.jts.geom.Point) {
                    coordinates.add(geom.getCoordinate());
                } else {
                    throw new SQLException("Unsupported geometry type "+geom.getGeometryType());
                }
            }
        } else {
            throw new SQLException("Unsupported geometry type "+geometry.getGeometryType());
        }
        return doWatershed(connection, flowDirection, !Utils.getProperty("h2gis.RasterProcessingInMemory",
                CreateSpatialExtension.DEFAULT_RASTER_PROCESSING_IN_MEMORY), coordinates.toArray(new
                Coordinate[coordinates.size()]));
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
