package org.h2gis.drivers.gpx.model;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import org.xml.sax.Attributes;

/**
 * This class represents routepoints.
 *
 * @author Antonin
 */
public final class RoutePoint extends AbstractPoint {

    //The route points id
    int rteID = 0;

    /**
     * Create a new routepoint full of null values.
     */
    public RoutePoint() {
        setFieldCount(GpxMetadata.RTEPTFIELDCOUNT);
        clearPtValues();
    }

    /**
     * Set null values for all the values of the routepoint.
     */
    private void clearPtValues() {
        for (int i = 0; i < GpxMetadata.RTEPTFIELDCOUNT; i++) {
            setValue(i, null);
        }
    }

    /**
     * Makes the initialisation of a routepoint by giving its an ID, a foreign
     * key, a latitude, a longitude and a geometry.
     *
     * @param ptID An ID for the point
     * @param rteID The foreign key corresponding to the point's route
     * @param attributes Attributes of the point. Here it is latitude and
     * longitude
     * @param wktr A WKTReader
     * @throws ParseException
     */
    public void rteptInit(Attributes attributes, WKTReader wktr) throws GPXException {
        ptInit(attributes, wktr);
        // Set the foreign key corresponding to the point's route
        setValue(GpxMetadata.RTEPT_RTEID, rteID++);
    }
}