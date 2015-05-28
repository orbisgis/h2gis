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
package org.h2gis.h2spatialext.function.system;

import org.h2.value.ValueArray;
import org.h2.value.ValueDouble;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

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
     * @return 
     */
    public static ValueArray createArray(double begin, double end) {
        return createArray(begin, end, 1);
    }
    
    /**
     * Return an array of doubles
     * @param begin from start
     * @param end to end
     * @param step increment
     * @return 
     */
    public static ValueArray createArray(double begin, double end, double step) {
        if (end < begin) {
            throw new IllegalArgumentException("End must be greater or equal to begin");
        }
        int nbClasses = (int) ((end - begin) / step);
        ValueDouble[] getArray = new ValueDouble[nbClasses];
        for (int i = 0; i < nbClasses; i++) {
            getArray[i] = ValueDouble.get(i * step + begin);

        }        
        return ValueArray.get(getArray);
    }
    
}
