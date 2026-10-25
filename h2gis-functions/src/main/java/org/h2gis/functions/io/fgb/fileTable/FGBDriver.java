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
import org.h2.index.Cursor;
import org.h2.result.DefaultRow;
import org.h2.result.Row;
import org.h2.result.SearchRow;
import org.h2.value.*;
import org.h2gis.api.FileDriver;
import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.flatgeobuf.ColumnMeta;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.PackedRTree;
import org.wololo.flatgeobuf.generated.ColumnType;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.Geometry;
import org.wololo.flatgeobuf.generated.GeometryType;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * FlatGeobuffer reader driver
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class FGBDriver implements FileDriver {
    private HeaderMeta headerMeta;
    private int fieldCount;
    private int geometryFieldIndex = 0;
    private int srid = 0;
    private FileInputStream fis;
    private FileChannel fileChannel;
    private Value[] currentRow = new Value[0];
    /** Feature of the current row: its geometry is decoded only when asked for */
    private Feature currentFeature;
    private boolean currentGeometryDecoded;
    /** Position in the file of the feature that follows the current row */
    private long nextFeaturePosition;
    /** Part of the file read in memory: one system call for many features */
    private ByteBuffer window;
    private long windowStart;
    private static final int WINDOW_SIZE = 1 << 20;
    private long rowIdPrevious = Long.MIN_VALUE;

    private boolean cacheRowAddress = true;

    /**
     * Address of the first feature
     */
    private long featuresOffset;

    private NavigableMap<Integer, Long> rowIndexToFileLocation;

    /**
     * Init file header for DBF File
     *
     * @param fgbFile DBF File path
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
        currentRow = new Value[fieldCount];
    }

    public boolean isCacheRowAddress() {
        return cacheRowAddress;
    }

    /**
     * @param cacheRowAddress If true the feature file address will be cached in order to reduce random access time.
     *                      If the file will be read sequentially only you can disable the cache in order
     *                      to reduce the memory footprint
     */
    public void setCacheRowAddress(boolean cacheRowAddress) {
        this.cacheRowAddress = cacheRowAddress;
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
        int totalSize = 0;
        for(ColumnMeta column : headerMeta.columns) {
            totalSize += column.width;
        }
        return totalSize;
    }

    @Override
    public int getFieldCount() {
        return fieldCount;
    }

    @Override
    public void close() throws IOException {
        if (fis != null) fis.close();
    }

    public Cursor queryIndex(Envelope queryEnvelope) throws IOException {
        fileChannel.position(headerMeta.offset);
        PackedRTree.SearchResult searchResult = PackedRTree.search(fis, 0, (int)headerMeta.featuresCount,
                headerMeta.indexNodeSize, queryEnvelope);
        return new FGBDriverCursor(searchResult, this);
    }

    /**
     * Using the Spatial index it is possible to quickly cache the file address of all features.
     * Using this function before doing a random access should reduce the access time.
     */
    public void cacheFeatureAddressFromIndex() throws IOException {
        if(headerMeta.indexNodeSize > 0) {
            fileChannel.position(headerMeta.offset);
            LittleEndianDataInputStream data = new LittleEndianDataInputStream(Channels.newInputStream(fileChannel));
            long[] fids = new long[(int) headerMeta.featuresCount];
            for (long id = 0; id < fids.length; id++) {
                fids[(int) id] = id;
            }
            long[] featuresAddress = PackedRTree.readFeatureOffsets(data, fids, headerMeta);
            for (int i = 0, featuresAddressLength = featuresAddress.length; i < featuresAddressLength; i++) {
                long address = featuresAddress[i];
                rowIndexToFileLocation.put(i, address + featuresOffset);
            }
        }
    }

    /**
     * @param featureAddress Feature address in the file relative to the first feature
     * @return values from the a flatgeobuffer feature
     */
    public static Value[] getFieldsFromFileLocation(FileChannel fileChannel, long featureAddress, long featuresOffset,
                                                    HeaderMeta headerMeta, int geometryFieldIndex) throws IOException {
        Feature feature = readFeature(fileChannel, featureAddress, featuresOffset);
        Value[] values = readProperties(feature, headerMeta, geometryFieldIndex);
        values[geometryFieldIndex] = readGeometry(feature, headerMeta);
        return values;
    }

    /**
     * Reads bytes of the file through a window of {@link #WINDOW_SIZE} bytes, so
     * that reading the features in order needs few system calls.
     *
     * @param position position in the file
     * @param length number of bytes
     * @return a little endian buffer with the bytes, valid until the next call
     */
    private ByteBuffer read(long position, int length) throws IOException {
        if (window == null || position < windowStart || position + length > windowStart + window.limit()) {
            int size = Math.max(WINDOW_SIZE, length);
            if (window == null || window.capacity() < size) {
                window = ByteBuffer.allocate(size);
            }
            window.clear();
            long filePosition = position;
            while (window.hasRemaining()) {
                int count = fileChannel.read(window, filePosition);
                if (count < 0) {
                    break;
                }
                filePosition += count;
            }
            window.flip();
            windowStart = position;
            if (window.limit() < length) {
                throw new EOFException("Unexpected end of the FlatGeobuf file");
            }
        }
        ByteBuffer bytes = window.duplicate();
        bytes.position((int) (position - windowStart));
        bytes.limit(bytes.position() + length);
        return bytes.slice().order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Reads the feature stored at an address of the file.
     *
     * @param fileChannel file
     * @param featureAddress address of the feature, from the first feature
     * @param featuresOffset address of the first feature
     * @return the feature
     */
    private static Feature readFeature(FileChannel fileChannel, long featureAddress, long featuresOffset) throws IOException {
        fileChannel.position(featuresOffset + featureAddress);
        LittleEndianDataInputStream data = new LittleEndianDataInputStream(Channels.newInputStream(fileChannel));
        int featureSize = data.readInt();
        byte[] bytes = new byte[featureSize];
        data.readFully(bytes);
        return Feature.getRootAsFeature(ByteBuffer.wrap(bytes));
    }

    /**
     * @param feature a feature
     * @param headerMeta header of the file
     * @return the geometry of the feature, ValueNull if it cannot be read, null when the feature has no geometry
     */
    private static Value readGeometry(Feature feature, HeaderMeta headerMeta) {
        Geometry geometry = feature.geometry();
        if (geometry == null) {
            return null;
        }
        byte geometryType = headerMeta.geometryType;
        if (geometryType == GeometryType.Unknown) {
            geometryType = (byte) geometry.type();
        }
        org.locationtech.jts.geom.Geometry jtsGeometry =
                GeometryConversions.deserialize(geometry, geometryType);
        if (jtsGeometry == null) {
            return ValueNull.INSTANCE;
        }
        jtsGeometry.setSRID(headerMeta.srid);
        return ValueGeometry.getFromGeometry(jtsGeometry);
    }

    /**
     * @param feature a feature
     * @param headerMeta header of the file
     * @param geometryFieldIndex index of the geometry column
     * @return the values of the columns, without the geometry
     */
    private static Value[] readProperties(Feature feature, HeaderMeta headerMeta, int geometryFieldIndex) {
        Value[] values = new Value[headerMeta.columns.size()+1];
        // Read columns
        int propertiesLength = feature.propertiesLength();
        if (propertiesLength > 0) {
            List<ColumnMeta> columns = headerMeta.columns;
            ByteBuffer propertiesBB = feature.propertiesAsByteBuffer();
            while (propertiesBB.hasRemaining()) {
                short propertyIndex = propertiesBB.getShort();
                ColumnMeta columnMeta = columns.get(propertyIndex);
                byte type = columnMeta.type;
                if(propertyIndex >= geometryFieldIndex) {
                    propertyIndex += 1;
                }
                switch (type) {
                    case ColumnType.Bool:
                        values[propertyIndex] = ValueBoolean.get(propertiesBB.get() > 0);
                        break;
                    case ColumnType.Byte:
                        values[propertyIndex] = ValueSmallint.get(propertiesBB.get());
                        break;
                    case ColumnType.Short:
                        values[propertyIndex] = ValueSmallint.get(propertiesBB.getShort());
                        break;
                    case ColumnType.Int:
                        values[propertyIndex] = ValueInteger.get(propertiesBB.getInt());
                        break;
                    case ColumnType.Long:
                        values[propertyIndex] = ValueBigint.get(propertiesBB.getLong());
                        break;
                    case ColumnType.Float:
                        values[propertyIndex] = ValueReal.get(propertiesBB.getFloat());
                        break;
                    case ColumnType.Double:
                        values[propertyIndex] = ValueDouble.get(propertiesBB.getDouble());
                        break;
                    case ColumnType.DateTime:
                        values[propertyIndex] = ValueTimestampTimeZone.parse(readString(propertiesBB), null);
                        break;
                    case ColumnType.String:
                        values[propertyIndex] = ValueVarchar.get(readString(propertiesBB));
                        break;
                    default:
                        throw new RuntimeException("Unknown type");
                }
            }
        }
        return values;
    }

    @Override
    public Value getField(long rowId, int columnId) throws IOException {
        try {
            if (rowId > rowIdPrevious + 1 || rowId < rowIdPrevious) {
                // We have to seek to the desired location
                Integer lowerKey = rowIndexToFileLocation.floorKey((int)rowId);
                if(lowerKey == null) {
                    nextFeaturePosition = featuresOffset;
                    rowIdPrevious = -1;
                } else {
                    nextFeaturePosition = rowIndexToFileLocation.get(lowerKey);
                    rowIdPrevious = lowerKey - 1;
                }
                // Make our way until rowId
                while (rowIdPrevious + 1 < rowId) {
                    int featureSize = read(nextFeaturePosition, 4).getInt();
                    nextFeaturePosition += 4 + featureSize;
                    rowIdPrevious++;
                    if(cacheRowAddress) {
                        rowIndexToFileLocation.put((int) rowIdPrevious + 1, nextFeaturePosition);
                    }
                }
            }
            if (rowIdPrevious + 1 == rowId) {
                // Read the current row, the geometry is decoded on demand
                rowIdPrevious = rowId;
                int featureSize = read(nextFeaturePosition, 4).getInt();
                byte[] bytes = new byte[featureSize];
                read(nextFeaturePosition + 4, featureSize).get(bytes);
                nextFeaturePosition += 4 + featureSize;
                currentFeature = Feature.getRootAsFeature(ByteBuffer.wrap(bytes));
                currentRow = readProperties(currentFeature, headerMeta, geometryFieldIndex);
                currentGeometryDecoded = false;

                if(cacheRowAddress) {
                    rowIndexToFileLocation.put((int) rowId + 1, nextFeaturePosition);
                }
            }
            if (columnId == geometryFieldIndex && !currentGeometryDecoded) {
                currentRow[geometryFieldIndex] = readGeometry(currentFeature, headerMeta);
                currentGeometryDecoded = true;
            }
            return currentRow[columnId];
        } catch (IOException e) {
            throw new NoSuchElementException();
        }
    }

    public long getFeaturesOffset() {
        return featuresOffset;
    }

    @Override
    public void insertRow(Object[] values) throws IOException {
        throw new IOException("Unsupported write operation");
    }

    private static String readString(ByteBuffer bb) {
        int length = bb.getInt();
        byte[] stringBytes = new byte[length];
        bb.get(stringBytes, 0, length);
        return  new String(stringBytes, StandardCharsets.UTF_8);
    }

    /**
     * @return Field index of the geometry when using the method getField
     */
    public int getGeometryFieldIndex() {
        return geometryFieldIndex;
    }

    public static class FGBDriverCursor implements Cursor {
        static final Logger LOGGER = LoggerFactory.getLogger(FGBDriver.class);
        PackedRTree.SearchResult searchResult;
        FGBDriver fgbDriver;

        int position = -1;

        Row currentRow = null;

        public FGBDriverCursor(PackedRTree.SearchResult searchResult, FGBDriver fgbDriver) {
            this.searchResult = searchResult;
            this.fgbDriver = fgbDriver;
        }

        @Override
        public Row get() {
            return currentRow;
        }

        @Override
        public SearchRow getSearchRow() {
            return null;
        }

        @Override
        public boolean next() {
            if(position < searchResult.hits.size() - 1) {
                position++;
                return fetchRow();
            } else {
                return false;
            }
        }

        private boolean fetchRow() {
            try {
                Value[] values = FGBDriver.getFieldsFromFileLocation(fgbDriver.fileChannel,
                        searchResult.hits.get(position).offset, fgbDriver.getFeaturesOffset(), fgbDriver.getHeader(),
                        fgbDriver.getGeometryFieldIndex());
                currentRow = new DefaultRow(values);
            } catch (IOException ex) {
                LOGGER.warn("Issue when fetching record", ex);
                return false;
            }
            return true;
        }

        @Override
        public boolean previous() {
            if(position > 0) {
                position--;
                return fetchRow();
            } else {
                return false;
            }
        }
    }
}