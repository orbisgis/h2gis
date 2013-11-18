package org.h2gis.drivers.gpx.model;

import com.vividsolutions.jts.geom.Geometry;


import org.xml.sax.Attributes;

/**
 * Abstract class giving basis for every types of line (route and track). All
 * setters for attributes are defined here.
 *
 * @author Antonin
 */
public abstract class AbstractLine {

    // This represents a row containing informations about a route or a track
    private Object[] lineValues;
    //The id of the line
    private int id = 0;

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
        if (currentElement.compareToIgnoreCase(GPXTags.NAME) == 0) {

            setName(contentBuffer);

        } else if (currentElement.compareToIgnoreCase(GPXTags.CMT) == 0) {

            setCmt(contentBuffer);

        } else if (currentElement.compareToIgnoreCase(GPXTags.DESC) == 0) {

            setDesc(contentBuffer);

        } else if (currentElement.compareToIgnoreCase(GPXTags.SRC) == 0) {

            setSrc(contentBuffer);

        } else if (currentElement.compareToIgnoreCase(GPXTags.NUMBER) == 0) {

            setNumber(contentBuffer);

        } else if (currentElement.compareToIgnoreCase(GPXTags.TYPE) == 0) {

            setType(contentBuffer);

        } else if (currentElement.compareToIgnoreCase(GPXTags.EXTENSIONS) == 0) {

            setExtensions();

        }
    }

    /**
     * Set attributes about link (the url and an optionnal description) for a
     * line. This method is only used in parsers for GPX 1.0.
     *
     * @param currentElement a string presenting the text of the current markup.
     * @param contentBuffer it contains all informations about the current
     * element.
     */
    public final void setFullLinkOld(String currentElement, StringBuilder contentBuffer) {
        if (currentElement.compareToIgnoreCase(GPXTags.URL) == 0) {

            setLink(contentBuffer);

        } else if (currentElement.compareToIgnoreCase(GPXTags.URLNAME) == 0) {

            setLinkText(contentBuffer);

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
        lineValues[GpxMetadata.LINELINK] = attributes.getValue(GPXTags.HREF);
    }

    /**
     * Set a link to additional information about the route or the track.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setLink(StringBuilder contentBuffer) {
        lineValues[GpxMetadata.LINELINK] = contentBuffer.toString();
    }

    /**
     * Set a text of hyperlink given in link.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setLinkText(StringBuilder contentBuffer) {
        lineValues[GpxMetadata.LINELINKTEXT] = contentBuffer.toString();
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

    /**
     * Set the size of the table corresponding to a point. Sizes are in
     * GpxMetadata class.
     *
     * @param i the size of the table
     */
    public final void setFieldCount(int i) {
        lineValues = new Object[i];
    }
}