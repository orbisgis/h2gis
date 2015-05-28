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
package org.h2gis.drivers.gpx.model;

/**
 * This class is a stack to keep in memory the structure of the GPX file.
 *
 * @author Erwan Bocher and Antonin Piasco
 */
public final class StringStack {

    private String[] stack;
    // Indicates the level of the stack
    private int stackTop = 0;

    /**
     * Instantiates a StringStack with chosen max size.
     *
     * @param capacity max size
     */
    public StringStack(int capacity) {
        stack = new String[capacity];
    }

    /**
     * Puts string in the stack.
     *
     * @param newText A new string to put in the stack
     * @return false if the stack is full
     */
    public boolean push(String newText) {
        if (stackTop < stack.length - 1) {
            stack[stackTop++] = newText;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the last string of the stack.
     *
     * @return the last string of the stack, null if the stack is empty
     */
    public String pop() {
        if (stackTop == 0) {
            return (null);
        } else {
            return (stack[--stackTop]);
        }
    }
}