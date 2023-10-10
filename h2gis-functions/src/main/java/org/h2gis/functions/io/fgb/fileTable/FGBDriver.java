/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
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
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.io.fgb.fileTable;

import org.h2.value.*;
import org.h2gis.api.FileDriver;
import org.wololo.flatgeobuf.*;
import org.wololo.flatgeobuf.generated.ColumnType;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.Geometry;
import org.wololo.flatgeobuf.generated.GeometryType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

public class FGBDriver implements FileDriver {


    private File fgbFile;
    private HeaderMeta headerMeta;
    private int fieldCount;
    private LittleEndianDataInputStream data;
    private int geometryFieldIndex = 0;
    private int srid = 0;
    private FileInputStream fis;
    private Feature feature;
    private long rowdid_previous = -1;

    /**
     * Init file header for DBF File
     *
     * @param fgbFile DBF File path
     * @throws IOException
     */
    public void initDriverFromFile(File fgbFile) throws IOException {
        // Read columns from files metadata
        this.fgbFile = fgbFile;
        fis = new FileInputStream(fgbFile);
        headerMeta = HeaderMeta.read(fis);
        fieldCount = headerMeta.columns.size() + 1;
        int treeSize =
                headerMeta.featuresCount > 0 && headerMeta.indexNodeSize > 0
                        ? (int)
                        PackedRTree.calcSize(
                                (int) headerMeta.featuresCount, headerMeta.indexNodeSize)
                        : 0;
        int featuresOffset = headerMeta.offset + treeSize;
        srid = headerMeta.srid;
        data = new LittleEndianDataInputStream(fis);
        if (treeSize > 0) {
            skipNBytes(data, treeSize);
        }
    }

    public static String getGeometryFieldType(HeaderMeta headerMeta) throws SQLException {
        int fgbGeometryType = headerMeta.geometryType;
        StringBuilder sfsGeometryType = new StringBuilder("GEOMETRY(");
        if(fgbGeometryType > GeometryType.GeometryCollection) {
            throw new SQLException("Unsupported geometry type: " +
                    GeometryType.name(fgbGeometryType));
        } else {
            sfsGeometryType.append(GeometryType.names[fgbGeometryType]);
        }
        if(fgbGeometryType > GeometryType.Unknown && fgbGeometryType < GeometryType.GeometryCollection) {
            // Z or ZM
            if(headerMeta.hasZ) {
                sfsGeometryType.append("Z");
            }
            if(headerMeta.hasM) {
                sfsGeometryType.append("M");
            }
        }
        // SRID
        sfsGeometryType.append(",");
        sfsGeometryType.append(headerMeta.srid);
        sfsGeometryType.append(")");
        return sfsGeometryType.toString();
    }

    public HeaderMeta getHeader() {
        return headerMeta;
    }

    private void skipNBytes(InputStream stream, long skip) throws IOException {
        long actual = 0;
        long remaining = skip;
        while (actual < remaining) {
            remaining -= stream.skip(remaining);
        }
    }

    @Override
    public long getRowCount() {
        return headerMeta.featuresCount;
    }

    @Override
    public int getEstimatedRowSize(long rowId) {
        //TODO evaluate the row size
        /*int totalSize = 0;
        int fieldCount = getFieldCount();
        for(int column = 0; column < fieldCount; column++) {


            totalSize += dbaseFileReader.getLengthFor(column);
        }
        return totalSize;*/
        return 0;
    }

    @Override
    public int getFieldCount() {
        return fieldCount;
    }

    @Override
    public void close() throws IOException {
        if (fis != null) fis.close();
    }

    @Override
    public Value getField(long rowId, int columnId) throws IOException {
        int featureSize;
        try {
            if (rowdid_previous == -1 || rowdid_previous != rowId) {
                rowdid_previous = rowId;
                featureSize = data.readInt();
                byte[] bytes = new byte[featureSize];
                data.readFully(bytes);
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                feature = Feature.getRootAsFeature(bb);
            }
            if (columnId == geometryFieldIndex) {
                Geometry geometry = feature.geometry();
                byte geometryType = headerMeta.geometryType;
                if (geometry != null) {
                    if (geometryType == GeometryType.Unknown) {
                        geometryType = (byte) geometry.type();
                    }
                    org.locationtech.jts.geom.Geometry jtsGeometry =
                            GeometryConversions.deserialize(geometry, geometryType);
                    if (jtsGeometry != null) {
                        jtsGeometry.setSRID(srid);
                        return ValueGeometry.getFromGeometry(jtsGeometry);
                    } else {
                        return ValueNull.INSTANCE;
                    }
                }
            }
            int propertiesLength = feature.propertiesLength();
            if (propertiesLength > 0) {
                List<ColumnMeta> columns = headerMeta.columns;
                ByteBuffer propertiesBB = feature.propertiesAsByteBuffer();
                while (propertiesBB.hasRemaining()) {
                    short i = propertiesBB.getShort();
                    if (columnId - 1 == i) {
                        ColumnMeta columnMeta = columns.get(i);
                        byte type = columnMeta.type;
                        if (type == ColumnType.Bool) return ValueBoolean.get(propertiesBB.get() > 0 ? true : false);
                        else if (type == ColumnType.Byte) return ValueSmallint.get(propertiesBB.get());
                        else if (type == ColumnType.Short) return ValueSmallint.get(propertiesBB.getShort());
                        else if (type == ColumnType.Int) return ValueInteger.get(propertiesBB.getInt());
                        else if (type == ColumnType.Long) return ValueBigint.get(propertiesBB.getLong());
                        else if (type == ColumnType.Double) return ValueDouble.get(propertiesBB.getDouble());
                        else if (type == ColumnType.DateTime) return ValueDate.parse(readString(propertiesBB));
                        else if (type == ColumnType.String) return ValueVarchar.get(readString(propertiesBB));
                        else throw new RuntimeException("Unknown type");
                    }
                }
            }
        } catch (IOException e) {
            throw new NoSuchElementException();
        }
        return ValueNull.INSTANCE;
    }

    @Override
    public void insertRow(Object[] values) throws IOException {
        throw new IOException("Unsupported write operation");
    }

    private String readString(ByteBuffer bb) {
            int length = bb.getInt();
            byte[] stringBytes = new byte[length];
            bb.get(stringBytes, 0, length);
            return  new String(stringBytes, StandardCharsets.UTF_8);
    }
}
