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

import com.google.common.io.LittleEndianDataInputStream;
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
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

public class FGBDriver implements FileDriver {
    private HeaderMeta headerMeta;
    private int fieldCount;
    private int geometryFieldIndex = 0;
    private int srid = 0;
    private FileInputStream fis;
    private FileChannel fileChannel;
    private Value[] currentRow = new Value[0];
    private long rowIdPrevious = -1;

    /**
     * Address of the first feature
     */
    private long featuresOffset;

    private Map<Integer, Long> rowIndexToFileLocation;

    /**
     * Init file header for DBF File
     *
     * @param fgbFile DBF File path
     * @throws IOException
     */
    public void initDriverFromFile(File fgbFile) throws IOException {
        // Read columns from files metadata
        fis = new FileInputStream(fgbFile);
        this.fileChannel = fis.getChannel();
        headerMeta = HeaderMeta.read(fis);
        fieldCount = headerMeta.columns.size() + 1;
        long treeSize =
                headerMeta.featuresCount > 0 && headerMeta.indexNodeSize > 0
                        ?
                        PackedRTree.calcSize(
                                (int)headerMeta.featuresCount, headerMeta.indexNodeSize)
                        : 0;
        featuresOffset = headerMeta.offset + treeSize;
        srid = headerMeta.srid;
        rowIndexToFileLocation = new TreeMap<>();
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
            if(rowId == 0) {
                fileChannel.position(featuresOffset);
                rowIdPrevious = -1;
            }
            if (rowIdPrevious + 1 == rowId) {
                // Read the current row from the input stream
                rowIdPrevious = rowId;
                LittleEndianDataInputStream data = new LittleEndianDataInputStream(Channels.newInputStream(fileChannel));
                featureSize = data.readInt();
                rowIndexToFileLocation.put((int)rowId + 1, fileChannel.position());
                byte[] bytes = new byte[featureSize];
                data.readFully(bytes);
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                Feature feature = Feature.getRootAsFeature(bb);
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
                // Read columns
                int propertiesLength = feature.propertiesLength();
                if (propertiesLength > 0) {
                    List<ColumnMeta> columns = headerMeta.columns;
                    ByteBuffer propertiesBB = feature.propertiesAsByteBuffer();
                    while (propertiesBB.hasRemaining()) {
                        short propertyIndex = propertiesBB.getShort();
                        ColumnMeta columnMeta = columns.get(propertyIndex);
                        byte type = columnMeta.type;
                        switch (type) {
                            case ColumnType.Bool:
                                currentRow[propertyIndex] = ValueBoolean.get(propertiesBB.get() > 0);
                                break;
                            case ColumnType.Byte:
                                currentRow[propertyIndex] = ValueSmallint.get(propertiesBB.get());
                                break;
                            case ColumnType.Short:
                                currentRow[propertyIndex] = ValueSmallint.get(propertiesBB.getShort());
                                break;
                            case ColumnType.Int:
                                currentRow[propertyIndex] = ValueInteger.get(propertiesBB.getInt());
                                break;
                            case ColumnType.Long:
                                currentRow[propertyIndex] = ValueBigint.get(propertiesBB.getLong());
                                break;
                            case ColumnType.Double:
                                currentRow[propertyIndex] = ValueDouble.get(propertiesBB.getDouble());
                                break;
                            case ColumnType.DateTime:
                                currentRow[propertyIndex] = ValueDate.parse(readString(propertiesBB));
                                break;
                            case ColumnType.String:
                                currentRow[propertyIndex] = ValueVarchar.get(readString(propertiesBB));
                                break;
                            default:
                                throw new RuntimeException("Unknown type");
                        }
                    }
                }
            }
            return currentRow[columnId - 1];
        } catch (IOException e) {
            throw new NoSuchElementException();
        }
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
