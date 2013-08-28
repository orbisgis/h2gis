package org.h2gis.h2spatial;

import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 * Function for Unit Test.
 * @author Nicolas Fortin
 */
public class DummyFunction extends DeterministicScalarFunction {

    public static final String REMARKS = "DummyFunction description";

    public DummyFunction() {
        addProperty(PROP_REMARKS, REMARKS);
    }

    @Override
    public String getJavaStaticMethod() {
        return "test";
    }

    public static int test(int value) {
        return value;
    }
}
