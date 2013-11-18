package org.h2gis.drivers.gpx.model;

/**
 * This class represents routes.
 *
 * @author Antonin
 */
public final class Route extends AbstractLine {

    /**
     * Create a new route full of null values.
     */
    public Route() {
        setFieldCount(GpxMetadata.RTEFIELDCOUNT);
        clearRteValues();
    }

    /**
     * Set null values for all the values of the route.
     */
    private void clearRteValues() {
        for (int i = 0; i < GpxMetadata.RTEFIELDCOUNT; i++) {
            setValue(i, null);
        }
    }

    /**
     * Makes the initialisation of a route by giving its an ID.
     *
     * @param rteID An ID for the route
     */
    public void rteInit(int rteID) {
        setValue(GpxMetadata.LINEID,rteID);
    }
}