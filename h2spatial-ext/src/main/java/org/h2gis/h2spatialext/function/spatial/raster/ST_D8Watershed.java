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
import org.h2gis.h2spatialext.jai.IndexOutletDescriptor;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Extract all watersheds or one based on a pixel location.
 * @author Nicolas Fortin
 */
public class ST_D8Watershed extends DeterministicScalarFunction {

    public ST_D8Watershed() {
        addProperty(PROP_REMARKS, "Extract all watersheds or one based on a pixel location.");
        IndexOutletDescriptor.register();
    }

    @Override
    public String getJavaStaticMethod() {
        return "watershed";
    }

    /**
     * @param flowDirection Flow direction
     * @return Watershed raster
     * @throws SQLException Error while computing raster
     * @throws IOException IO Error while computing raster
     */
    public static GeoRaster watershed(GeoRaster flowDirection) throws SQLException, IOException {
        if(flowDirection == null) {
            return null;
        }
        RasterUtils.RasterMetaData metadata = flowDirection.getMetaData();
        return GeoRasterRenderedImage
                .create(flowDirection, metadata);
    }
}
