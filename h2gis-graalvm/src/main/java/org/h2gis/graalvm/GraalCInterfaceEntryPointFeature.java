package org.h2gis.graalvm;

import org.graalvm.nativeimage.hosted.Feature;

public class GraalCInterfaceEntryPointFeature implements  Feature{
    @Override
    public void beforeAnalysis(Feature.BeforeAnalysisAccess access) {
        System.out.println(">> GraalCEntryPointFeature applied");
        access.findClassByName("org.h2gis.graalvm.GraalCInterface");
    }

}
