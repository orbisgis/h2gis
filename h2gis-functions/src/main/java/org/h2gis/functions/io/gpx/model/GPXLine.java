/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
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
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.io.gpx.model;

import org.locationtech.jts.geom.Geometry;


import org.xml.sax.Attributes;

/**
 * This class giving is used to manage route and track data
 *
 * @author Erwan Bocher
 */
public class GPXLine {

    // This represents a row containing informations about a route or a track
    private Object[] lineValues;

    public GPXLine(int numberOfValues) {
        this.lineValues = new Object[numberOfValues];
    }

    /**
     * Set the geometry of a route or a track.
     *
     * @param geometry a geoetry (linestring or MultiLineString) representing
     * the route or the track
     */
    public final void setGeometry(Geometry geometry) {
        lineValues[GpxMetadata.THE_GEOM] = geometry;
    }

    /**
     * Set an attribute for a line. The String currentElement gives the
     * information of which attribute have to be setted. The attribute to set is
     * given by the StringBuilder contentBuffer.
     *
     * @param currentElement a string presenting the text of the current markup.
     * @param contentBuffer it contains all informations about the current
     * element.
     */
    public final void setAttribute(String currentElement, StringBuilder contentBuffer) {
        if (currentElement.equalsIgnoreCase(GPXTags.NAME)) {
            setName(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.CMT)) {
            setCmt(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.DESC)) {
            setDesc(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.SRC)) {
            setSrc(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.NUMBER)) {
            setNumber(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.TYPE)) {
            setType(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.EXTENSIONS)) {
            setExtensions();
        }
    }

    /**
     * *****************************
     ***** GETTERS AND SETTERS ***** *****************************
     */
    /**
     * Set the name of a route or a track.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setName(StringBuilder contentBuffer) {
        lineValues[GpxMetadata.LINENAME] = contentBuffer.toString();
    }

    /**
     * Set the comment of a route or a track.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setCmt(StringBuilder contentBuffer) {
        lineValues[GpxMetadata.LINECMT] = contentBuffer.toString();
    }

    /**
     * Set the description of a route or a track.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setDesc(StringBuilder contentBuffer) {
        lineValues[GpxMetadata.LINEDESC] = contentBuffer.toString();
    }

    /**
     * Set the source of data of a route or a track.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setSrc(StringBuilder contentBuffer) {
        lineValues[GpxMetadata.LINESRC] = contentBuffer.toString();
    }

    /**
     * Set a link to additional information about the route or the track.
     *
     * @param attributes The current attributes being parsed
     */
    public final void setLink(Attributes attributes) {
        lineValues[GpxMetadata.LINELINK_HREF] = attributes.getValue(GPXTags.HREF);
    }

    /**
     * Set a link to additional information about the route or the track.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setLink(StringBuilder contentBuffer) {
        lineValues[GpxMetadata.LINELINK_HREF] = contentBuffer.toString();
    }

    /**
     * Set a text of hyperlink given in link.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setLinkText(StringBuilder contentBuffer) {
     lineValues[GpxMetadata.LINELINK_HREFTITLE] = contentBuffer.toString();
     }
    /**
     * Set the GPS number to additional information about the route or the
     * track.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setNumber(StringBuilder contentBuffer) {
        lineValues[GpxMetadata.LINENUMBER] = Integer.parseInt(contentBuffer.toString());
    }

    /**
     * Set the type (classification) of a route or a track.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setType(StringBuilder contentBuffer) {
        lineValues[GpxMetadata.LINETYPE] = contentBuffer.toString();
    }

    /**
     * Set extentions of a route or a track to true.
     */
    public final void setExtensions() {
        lineValues[GpxMetadata.LINEEXTENSIONS] = true;
    }

    /**
     * Gives access to the route or track's values
     *
     * @return a row containing informations about the route or track
     */
    public final Object[] getValues() {
        return lineValues;
    }

    /**
     * Set an object in corresponding index.
     *
     * @param i the index
     * @param value the value to insert
     */
    public final void setValue(int i, Object value) {
        lineValues[i] = value;
    }
}
