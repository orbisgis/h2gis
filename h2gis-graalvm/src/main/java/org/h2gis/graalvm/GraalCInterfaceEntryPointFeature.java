package org.h2gis.graalvm;

import org.graalvm.nativeimage.hosted.Feature;

/**
 * Class needed to expose the C API as it is not used in any other class.
*/
public class GraalCInterfaceEntryPointFeature implements  Feature{
    @Override
    public void beforeAnalysis(Feature.BeforeAnalysisAccess access) {
        access.findClassByName("org.h2gis.graalvm.GraalCInterface");
    }

}
