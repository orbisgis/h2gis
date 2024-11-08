/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.utilities;

/**
 * Basic tuple class
 * @author Erwan Bocher
 * @param <T> First tuple object
 * @param <U> Second tuple object
 */
public class Tuple<T, U> {

    private final T _1;
    private final U _2;

    public Tuple(T arg1, U arg2) {
        super();
        this._1 = arg1;
        this._2 = arg2;
    }

    public T first() {
        return _1;
    }

    public U second() {
        return _2;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", _1, _2);
    }

}
