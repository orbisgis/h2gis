/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.h2gis.h2spatialext.function.system;

import org.h2.value.ValueArray;
import org.h2.value.ValueInt;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class IntegerRange extends DeterministicScalarFunction{

    public IntegerRange() {
        addProperty(PROP_REMARKS, "Return an array of integers from start to end. \n"
                + "The default step increment is 1 but the user can set another one.");
    }

 
    
    @Override
    public String getJavaStaticMethod() {
        return "createArray";
    }
    
    /**
     * Return an array of integers with a default step of 1.
     * @param begin from start
     * @param end to end
     * @return 
     */
    public static  ValueArray createArray(int begin, int end) {
        return createArray(begin, end, 1);
    }
    
    /**
     * Return an array of integers
     * @param begin from start
     * @param end to end
     * @param step increment
     * @return 
     */
    public static ValueArray createArray(int begin, int end, int step) {
        if (end < begin) {
            throw new IllegalArgumentException("End must be greater or equal to begin");
        }
        int nbClasses = (int) ((end - begin) / step);
        ValueInt[] getArray = new ValueInt[nbClasses];
        for (int i = 0; i < nbClasses; i++) {
            getArray[i] = ValueInt.get(i * step + begin);

        }        
        return ValueArray.get(getArray);
    }
    
}
