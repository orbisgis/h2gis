/*
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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
package org.h2gis.h2spatialext.drivers.osm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.xml.sax.SAXException;

/**
 * A class to manage all common element properties.
 *
 * @author Erwan Bocher
 */
public class OSMElement {

    private final SimpleDateFormat dataFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final SimpleDateFormat dataFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private final HashMap<String, String> tags;
    private long id, uid;
    private String user;
    private int version, changeset;
    private boolean visible;
    private Date timestamp;

    public OSMElement() {
        tags = new HashMap<String, String>();
    }

    /**
     * The id of the element
     *
     * @return
     */
    public long getID() {
        return id;
    }

    /**
     * Set an id to the element
     *
     * @param id
     */
    public void setId(String id) {
        this.id = Long.valueOf(id);
    }

    /**
     * The user
     *
     * @return
     */
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getUID() {
        return uid;
    }

    public void setUid(String uid) {
        if (uid != null) {
            this.uid = Long.valueOf(uid);
        }
    }

    /**
     *
     * @return
     */
    public boolean getVisible() {
        return visible;
    }

    public void setVisible(String visible) {
        if(visible!=null){
        this.visible = Boolean.valueOf(visible);
        }
    }

    /**
     *
     * @return
     */
    public int getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = Integer.valueOf(version);
    }

    /**
     *
     * @return
     */
    public int getChangeSet() {
        return changeset;
    }

    public void setChangeset(String changeset) {
        if(changeset!=null){
        this.changeset = Integer.valueOf(changeset);
        }
    }

    /**
     *
     * @return
     */
    public Date getTimeStamp() {
        return timestamp;

    }

    public void setTimestamp(String OSMtime) throws SAXException {
        if(OSMtime!=null){
        try {
            timestamp = dataFormat1.parse(OSMtime);
        } catch (ParseException ex) {
            try {
                timestamp = dataFormat2.parse(OSMtime);
            } catch (ParseException ex1) {
                throw new SAXException("Cannot parse the timestamp for the node  :  " + getID(), ex);
            }
        }}
    }

    /**
     *
     * @param key
     * @param value
     */
    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    public HashMap<String, String> getTags() {
        return tags;
    }

}
