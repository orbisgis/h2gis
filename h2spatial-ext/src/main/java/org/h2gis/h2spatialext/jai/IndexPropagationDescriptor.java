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
package org.h2gis.h2spatialext.jai;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.RIFRegistry;

/**
 * JAI Api, Description of the index propagation operator
 * @author Nicolas Fortin
 */
public class IndexPropagationDescriptor extends OperationDescriptorImpl {

    // A map-like array of strings with resources information.
    private static final String[][] resources =
            {
                    {"GlobalName",   "IndexPropagation"},
                    {"LocalName",    "IndexPropagation"},
                    {"Vendor",       "org.orbisgis"},
                    {"Description",  "Using flow direction raster and index raster, move index to origin flow cells"},
                    {"DocURL",       "http://www.h2gis.org/docs/dev/ST_D8Watershed"},
                    {"Version",      "1.0"},
                    {"arg0Desc", "no data value"}
            };
    // An array of strings with the supported modes for this operator.
    private static final String[] supportedModes = {"rendered"};
    // An array of strings with the parameter names for this operator.
    private static final String[] paramNames = {"bandNoData"};
    // An array of Classes with the parameters' classes for this operator.
    private static final Class[] paramClasses = {
            Double.class};
    // An array of Objects with the parameters' default values.
    private static final Object[] paramDefaults = { null };
    // An array of valid parameter values.
    public static final Object[] VALID_PARAM_VALUES = null;
    // The number of sources required for this operator.
    private static final int numSources = 2;
    // A flag that indicates whether the operator is already registered.
    private static boolean registered = false;

    /**
     * The constructor for this descriptor, which just calls the constructor
     * for its ancestral class (OperationDescriptorImpl).
     */
    public IndexPropagationDescriptor()
    {
        super(resources,supportedModes,numSources,paramNames,
                paramClasses,paramDefaults, VALID_PARAM_VALUES);
    }

    /**
     * A method to register this operator with the OperationRegistry and
     * RIFRegistry.
     */
    public static void register()
    {
        if (!registered)
        {
            // Get the OperationRegistry.
            OperationRegistry op = JAI.getDefaultInstance().getOperationRegistry();
            // Register the operator's descriptor.
            IndexPropagationDescriptor desc =
                    new IndexPropagationDescriptor();
            op.registerDescriptor(desc);
            // Register the operators's RIF.
            IndexPropagationRIF rif = new IndexPropagationRIF();
            RIFRegistry.register(op, "IndexPropagation", "h2gis", rif);
            registered = true;
        }
    }

}
