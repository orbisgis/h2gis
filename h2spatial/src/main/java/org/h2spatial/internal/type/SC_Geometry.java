/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
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

package org.h2spatial.internal.type;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import org.h2.api.JavaObjectSerializer;
import org.h2.constant.SysProperties;
import org.h2.util.Utils;
import org.h2spatial.ValueGeometry;
import org.h2spatialapi.ScalarFunction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

/**
 * Space Constraint on Geometry field.
 * @author Nicolas Fortin
 */
public class SC_Geometry implements ScalarFunction {
    static {
        // Initialise H2 Object serialisation
        Utils.serializer = new GeometrySerializer();
    }
    @Override
    public String getJavaStaticMethod() {
        return "IsGeometryOrNull";
    }

    @Override
    public Object getProperty(String propertyName) {
        return null;
    }

    /**
     * @param bytes Byte array or null
     * @return True if bytes is Geometry or if bytes is null
     */
    public static Boolean IsGeometryOrNull(byte[] bytes) {
        if(bytes==null) {
            return true;
        }
        WKBReader wkbReader = new WKBReader();
        try {
            wkbReader.read(bytes);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }

    /**
     * H2 extension to (de)serialize OTHER binary data.
     */
    private static final class GeometrySerializer implements JavaObjectSerializer {
        @Override
        public byte[] serialize(Object obj) throws Exception {
            if(obj instanceof ValueGeometry) {
                return ((ValueGeometry) obj).getBytesNoCopy();
            } else if(obj instanceof Geometry) {
                return new ValueGeometry((Geometry)obj).getBytesNoCopy();
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(out);
                outputStream.writeObject(obj);
                return out.toByteArray();
            }
        }

        @Override
        public Object deserialize(byte[] bytes) throws Exception {
            WKBReader wkbReader = new WKBReader();
            try {
                try {
                    return wkbReader.read(bytes);
                } catch (ParseException ex) {
                    //Maybe the bytes are in UTF8
                    return wkbReader.read((new String(bytes, "UTF8")).getBytes());
                }
            } catch (Exception ex) {
                 /*
                 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
                 * Version 1.0, and under the Eclipse Public License, Version 1.0
                 * (http://h2database.com/html/license.html).
                 * Initial Developer: H2 Group
                 */
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                ObjectInputStream is;
                if (SysProperties.USE_THREAD_CONTEXT_CLASS_LOADER) {
                    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    is = new ObjectInputStream(in) {
                        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                            try {
                                return Class.forName(desc.getName(), true, loader);
                            } catch (ClassNotFoundException e) {
                                return super.resolveClass(desc);
                            }
                        }
                    };
                } else {
                    is = new ObjectInputStream(in);
                }
                return is.readObject();
                // end H2 Group Licensed
            }
        }
    }
}
