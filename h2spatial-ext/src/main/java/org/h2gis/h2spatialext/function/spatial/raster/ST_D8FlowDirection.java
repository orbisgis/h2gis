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
