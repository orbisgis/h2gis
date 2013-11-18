/*
 * Copyright (C) 2013 IRSTV CNRS-FR-2488
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.h2gis.drivers.gpx.model;

/**
 * Class to manage all GPX exceptions
 * @author ebocher
 */
public class GPXException extends Exception{

    public GPXException(String message) {
        super(message);
    }

    public GPXException(String message, Throwable cause) {
        super(message, cause);
    }

    public GPXException(Throwable cause) {
        super(cause);
    }   
    
    
}
