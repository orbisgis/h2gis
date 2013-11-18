package org.h2gis.drivers.gpx.model;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.xml.sax.Attributes;

/**
 * This class represents waypoints.
 *
 * @author Antonin Piasco, Erwan Bocher
 */
public final class WayPoint extends AbstractPoint {

    /**
     * Create a new waypoint full of null values.
     */
    public WayPoint() {
        setFieldCount(GpxMetadata.WPTFIELDCOUNT);
        clearPtValues();
    }

    /**
     * Set null values for all the values of the waypoint.
     */
    private void clearPtValues() {
        for (int i = 0; i < GpxMetadata.WPTFIELDCOUNT; i++) {
            setValue(i, null);
        }
    }

    /**
     * Makes the initialisation of a waypoint by giving its an ID, a latitude, a
     * longitude and a geometry.     *
     * 
     * @param attributes Attributes of the point. Here it is latitude and
     * longitude
     * @param wktr A WKTReader
     * @throws GPXException
     */
    public void wptInit(Attributes attributes, WKTReader wktr) throws  GPXException {
        ptInit(attributes, wktr);
    }
}