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

import javax.media.jai.EnumeratedParameter;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.RIFRegistry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * JAI Api, Description of the slope operator
 * @author Nicolas Fortin
 */
public class SlopeDescriptor extends OperationDescriptorImpl {
    public static final EnumeratedParameter SLOPE_PERCENT = new EnumeratedParameter("PERCENT", 0);
    public static final EnumeratedParameter SLOPE_DEGREE = new EnumeratedParameter("DEGREE", 1);
    public static final EnumeratedParameter SLOPE_RADIANT = new EnumeratedParameter("RADIAN", 2);

    // A map-like array of strings with resources information.
    private static final String[][] resources =
            {
                    {"GlobalName",   "D8Slope"},
                    {"LocalName",    "D8Slope"},
                    {"Vendor",       "org.orbisgis"},
                    {"Description",  "Compute the steepest downward slope towards one of the eight adjacent or" +
                            " diagonal neighbors."},
                    {"DocURL",       "http://www.h2gis.org/docs/dev/ST_SLOPE"},
                    {"Version",      "1.0"},
                    {"arg0Desc",     "Slope unit, in degree, radian or percent"}
            };
    // An array of strings with the supported modes for this operator.
    private static final String[] supportedModes = {"rendered"};
    // An array of strings with the parameter names for this operator.
    private static final String[] paramNames = {"Slope unit"};
    // An array of Classes with the parameters' classes for this operator.
    private static final Class[] paramClasses = {EnumeratedParameter.class};
    // An array of Objects with the parameters' default values.
    private static final Object[] paramDefaults =
            {SLOPE_DEGREE};
    // An array of valid parameter values.
    public static final Object[] VALID_PARAM_VALUES =
            {Collections.unmodifiableSet(new HashSet<EnumeratedParameter>(Arrays.asList(SLOPE_DEGREE, SLOPE_PERCENT,
                    SLOPE_RADIANT)))};
    // The number of sources required for this operator.
    private static final int numSources = 1;
    // A flag that indicates whether the operator is already registered.
    private static boolean registered = false;

    /**
     * The constructor for this descriptor, which just calls the constructor
     * for its ancestral class (OperationDescriptorImpl).
     */
    public SlopeDescriptor()
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
            SlopeDescriptor desc =
                    new SlopeDescriptor();
            op.registerDescriptor(desc);
            // Register the operators's RIF.
            SlopeRIF rif = new SlopeRIF();
            RIFRegistry.register(op, "D8Slope", "h2gis", rif);
            registered = true;
        }
    }

}
