package org.h2gis.h2spatialapi;

/**
 * Extended by Scalar function whose return always the same value for the same arguments.
 * @author Nicolas Fortin
 */
public abstract class DeterministicScalarFunction extends AbstractFunction implements ScalarFunction {
    public DeterministicScalarFunction() {
        addProperty(ScalarFunction.PROP_DETERMINISTIC,true);
    }
}
