/*
 * Copyright (C) 2014 IRSTV CNRS-FR-2488
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

package org.h2gis.drivers.osm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.xml.sax.Attributes;

/**
 *
 * @author ebocher
 */
public class OSMElement {
    
    private final Attributes attributes;
    private final SimpleDateFormat dataFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final SimpleDateFormat dataFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    public OSMElement(Attributes attributes){
        this.attributes = attributes;
    }
    
    /**
     * 
     * @return 
     */
    public long getID(){
        return Long.valueOf(attributes.getValue("id"));
    }
    
    /**
     * 
     * @return 
     */
    public String getUser(){
        return attributes.getValue("user");
    }
    
    public long getUID(){
        return Long.valueOf(attributes.getValue("uid"));
    }
    
    /**
     * 
     * @return 
     */
    public boolean geVisible(){
        return Boolean.valueOf(attributes.getValue("visible"));
    }
    
    /**
     * 
     * @return 
     */
    public int getVersion(){
        return Integer.valueOf(attributes.getValue("version"));
    }
    
    /**
     * 
     * @return 
     */
    public int getChangeSet(){
        return Integer.valueOf(attributes.getValue("changeset"));
    }
    
    /**
     * 
     * @return
     * @throws ParseException 
     */
    public Date getTimeStamp() throws ParseException {
        String time = attributes.getValue("timestamp");
        Date date = null;
        try {
            dataFormat1.parse(time);
        } catch (ParseException ex) {
            dataFormat2.parse(time);
        }
        return date;
    }
    
    /**
     * 
     * @param key
     * @return 
     */
    public String getValue(String key){
        return attributes.getValue(key);
    }
    
}
