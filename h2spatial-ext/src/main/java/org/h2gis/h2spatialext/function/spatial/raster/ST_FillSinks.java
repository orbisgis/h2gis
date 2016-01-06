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
import org.h2gis.h2spatialapi.DeterministicScalarFunction;
import org.h2gis.h2spatialapi.EmptyProgressVisitor;
import org.h2gis.h2spatialext.function.spatial.raster.utility.OpFillSinks;
import org.h2gis.h2spatialext.jai.FlowDirectionDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Compute the steepest downward slope towards one of the eight adjacent or diagonal neighbors.
 * @author Nicolas Fortin
 * @author Erwan Bocher
 */
public class ST_FillSinks extends DeterministicScalarFunction {
    private static final AtomicBoolean warnedPrinted = new AtomicBoolean(false);
    private static final Logger LOGGER = LoggerFactory.getLogger(ST_FillSinks.class);

    public ST_FillSinks() {
        addProperty(PROP_REMARKS, "Fill all sinks in a DEM and return it");
        FlowDirectionDescriptor.register();
    }

    @Override
    public String getJavaStaticMethod() {
        return "fillSink";
    }

    /**
     * @param geoRaster GeoRaster instance or null
     * @return minSlope min slope
     * @throws SQLException Error while computing raster
     * @throws IOException IO Error while computing raster
     */
    public static GeoRaster fillSink(GeoRaster geoRaster, float minSlope) throws SQLException, IOException {
        if(geoRaster == null) {
            return null;
        }
        if(geoRaster.getWidth() > 1000 && geoRaster.getHeight() > 1000 && !warnedPrinted.getAndSet(true)) {
            // TODO remove when iterative D8 of Fill Sinks will be done.
            LOGGER.warn("ST_FillSinks is currently only in memory mode and may require more memory than available in " +
                    "the virtual machine. If you experience issues you should increase the allocated java virtual " +
                    "machine memory.");
        }
        return new OpFillSinks(minSlope).execute(geoRaster, new EmptyProgressVisitor());
    }
}
