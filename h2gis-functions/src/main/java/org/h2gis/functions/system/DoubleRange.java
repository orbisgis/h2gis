/**
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

package org.h2gis.functions.system;
import org.h2gis.api.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class DoubleRange extends DeterministicScalarFunction{

    public DoubleRange() {
        addProperty(PROP_REMARKS, "Return an array of doubles within the range [start-end). \n"
                + "The default step increment is 1 but the user can set another one.");
    }

 
    
    @Override
    public String getJavaStaticMethod() {
        return "createArray";
    }
    
    /**
     * Return an array of doubles with a default step of 1.
     * @param begin from start
     * @param end to end
     * @return a double array
     */
    public static Double[] createArray(double begin, double end) {
        return createArray(begin, end, 1);
    }
    
    /**
     * Return an array of doubles
     * @param begin from start
     * @param end to end
     * @param step increment
     * @return a double array
     */
    public static Double[]createArray(double begin, double end, double step) {
        if (end < begin) {
            throw new IllegalArgumentException("End must be greater or equal to begin");
        }
        int nbClasses = (int) ((end - begin) / step);
        Double[] getArray = new Double[nbClasses];
        for (int i = 0; i < nbClasses; i++) {
            getArray[i] = i * step + begin;

        }        
        return getArray;
    }
    
}
