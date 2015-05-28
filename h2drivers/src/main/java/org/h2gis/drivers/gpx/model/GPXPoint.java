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


import org.xml.sax.Attributes;

/**
 * This class gives bvalues for every types of points (waypoint, routepoint
 * and trackpoint).
 *
 * @author Erwan Bocher
 */
public class GPXPoint {

    // This represents a row containing informations about a point
    private Object[] ptValues;

    public GPXPoint(int numberOfValues) {
        this.ptValues = new Object[numberOfValues];
    }

    /**
     * This method is used to create a new array of values
     *
     * @param valuesCount
     */
    public void clearValues(int valuesCount) {
        ptValues = new Object[valuesCount];
    }

    

    /**
     * Set an attribute for a point. The String currentElement gives the
     * information of which attribute have to be setted. The attribute to set is
     * given by the StringBuilder contentBuffer.
     *
     * @param currentElement a string presenting the text of the current markup.
     * @param contentBuffer it contains all informations about the current
     * element.
     */
    public final void setAttribute(String currentElement, StringBuilder contentBuffer) {
        if (currentElement.equalsIgnoreCase(GPXTags.TIME)) {
            setTime(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.MAGVAR)) {
            setMagvar(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.GEOIDHEIGHT)) {
            setGeoidheight(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.NAME)) {
            setName(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.CMT)) {
            setCmt(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.DESC)) {
            setDesc(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.SRC)) {
            setSrc(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.SYM)) {
            setSym(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.TYPE)) {
            setType(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.FIX)) {
            setFix(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.SAT)) {
            setSat(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.HDOP)) {
            setHdop(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.VDOP)) {
            setVdop(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.PDOP)) {
            setPdop(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.AGEOFDGPSDATA)) {
            setAgeofdgpsdata(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.DGPSID)) {
            setDgpsid(contentBuffer);
        } else if (currentElement.equalsIgnoreCase(GPXTags.EXTENSIONS)) {
            setExtensions();
        }
    }

    /**
     * Set the elevation (in meters) of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setElevation(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTELE] = Double.parseDouble(contentBuffer.toString());
    }

    /**
     * Creation/modification timestamp for element. Date and time in are in
     * Univeral Coordinated Time (UTC), not local time! Conforms to ISO 8601
     * specification for date/time representation. Fractional seconds are
     * allowed for millisecond timing in tracklogs.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setTime(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTTIME] = contentBuffer.toString();
    }

    /**
     * Set the Magnetic variation (in degrees) of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setMagvar(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTMAGVAR] = Double.parseDouble(contentBuffer.toString());
    }

    /**
     * Set the height (in meters) of geoid (mean sea level) above WGS84 earth
     * ellipsoid of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setGeoidheight(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTGEOIDWEIGHT] = Double.parseDouble(contentBuffer.toString());
    }

    /**
     * Set the name of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setName(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTNAME] = contentBuffer.toString();
    }

    /**
     * Set the comment of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setCmt(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTCMT] = contentBuffer.toString();
    }

    /**
     * Set the description of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setDesc(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTDESC] = contentBuffer.toString();
    }

    /**
     * Set the source of data of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setSrc(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTSRC] = contentBuffer.toString();
    }

    /**
     * Set a link to additional information about the point.
     *
     * @param attributes The current attributes being parsed
     */
    public final void setLink(Attributes attributes) {
        ptValues[GpxMetadata.PTLINK] = attributes.getValue(GPXTags.HREF);
    }

    /**
     * Set a link to additional information about the point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setLink(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTLINK] = contentBuffer.toString();
    }

    /**
     * Set a text of hyperlink given in link.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setLinkText(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTLINKTEXT] = contentBuffer.toString();
    }

    /**
     * Set the GPS symbol name of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setSym(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTSYM] = contentBuffer.toString();
    }

    /**
     * Set the type (classification) of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setType(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTTYPE] = contentBuffer.toString();
    }

    /**
     * Set the type of GPX fix used for a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setFix(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTFIX] = contentBuffer.toString();
    }

    /**
     * Set the number of satellites used to calculate the GPX fix for a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setSat(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTSAT] = Integer.parseInt(contentBuffer.toString());
    }

    /**
     * Set the horizontal dilution of precision of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setHdop(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTHDOP] = Double.parseDouble(contentBuffer.toString());
    }

    /**
     * Set the vertical dilution of precision of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setVdop(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTVDOP] = Double.parseDouble(contentBuffer.toString());
    }

    /**
     * Set the position dilution of precision of a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setPdop(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTPDOP] = Double.parseDouble(contentBuffer.toString());
    }

    /**
     * Set number of seconds since last DGPS update for a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setAgeofdgpsdata(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTAGEOFDGPSDATA] = Double.parseDouble(contentBuffer.toString());
    }

    /**
     * Set ID of DGPS station used in differential correction for a point.
     *
     * @param contentBuffer Contains the information to put in the table
     */
    public final void setDgpsid(StringBuilder contentBuffer) {
        ptValues[GpxMetadata.PTDGPSID] = Integer.parseInt(contentBuffer.toString());
    }

    /**
     * Set extentions of a point to true.
     */
    public final void setExtensions() {
        ptValues[GpxMetadata.PTEXTENSIONS] = true;
    }    
    

    /**
     * Gives access to the point's values
     *
     * @return a row containing informations about the point
     */
    public final Object[] getValues() {
        return ptValues;
    }

    /**
     * Set a Value in corresponding index. It is used to set the corresponding
     * id.
     *
     * @param i the index
     * @param value the value to insert
     */
    public final void setValue(int i, Object value) {
        ptValues[i] = value;
    }
}