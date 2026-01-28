/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.graalvm;

import org.graalvm.nativeimage.hosted.Feature;

/**
 * @author Maël PHILIPPE, CNRS
 * @author Erwan BOCHER, CNRS
 * Class needed to expose the C API as it is not used in any other class.
 */
public class GraalCInterfaceEntryPointFeature implements Feature {
    @Override
    public void beforeAnalysis(Feature.BeforeAnalysisAccess access) {
        access.findClassByName("org.h2gis.graalvm.GraalCInterface");
    }
}
