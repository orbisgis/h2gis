package org.h2gis.h2spatialext.jai;

import javax.media.jai.EnumeratedParameter;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.registry.RIFRegistry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Nicolas Fortin
 */
public class SlopeDescriptor extends OperationDescriptorImpl {
    public static final EnumeratedParameter SLOPE_PERCENT = new EnumeratedParameter("PERCENT", 0);
    public static final EnumeratedParameter SLOPE_DEGREE = new EnumeratedParameter("DEGREE", 1);
    public static final EnumeratedParameter SLOPE_RADIANT = new EnumeratedParameter("RADIANT", 2);

    // A map-like array of strings with resources information.
    private static final String[][] resources =
            {
                    {"GlobalName",   "D8Slope"},
                    {"LocalName",    "D8Slope"},
                    {"Vendor",       "org.orbisgis"},
                    {"Description",  "Slope computation operator"},
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
