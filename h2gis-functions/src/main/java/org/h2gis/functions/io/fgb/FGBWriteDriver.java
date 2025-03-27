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
package org.h2gis.functions.io.fgb;

import com.google.common.collect.Lists;
import com.google.flatbuffers.FlatBufferBuilder;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.fgb.fileTable.GeometryConversions;
import org.h2gis.utilities.*;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.wololo.flatgeobuf.ColumnMeta;
import org.wololo.flatgeobuf.Constants;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.NodeItem;
import org.wololo.flatgeobuf.PackedRTree;
import org.wololo.flatgeobuf.generated.ColumnType;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.GeometryType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FlatGeobuffer write file
 * @author Erwan Bocher
 * @author Nicolas Fortin
 */
public class FGBWriteDriver {
    private final static int BYTEBUFFER_CACHE = 1024;

    short packedRTreeNodeSize = 16;

    boolean createIndex = true;

    private final Connection connection;

    public FGBWriteDriver(Connection connection) {
        this.connection = connection;
    }


    public short getPackedRTreeNodeSize() {
        return packedRTreeNodeSize;
    }

    public void setPackedRTreeNodeSize(short packedRTreeNodeSize) {
        this.packedRTreeNodeSize = packedRTreeNodeSize;
    }

    public boolean isCreateIndex() {
        return createIndex;
    }

    public void setCreateIndex(boolean createIndex) {
        this.createIndex = createIndex;
    }

    /**
     * Write the spatial table to a FlatGeobuf file
     *
     * @param progress Progress visitor following the execution.
     * @param tableName table to write
     * @param fileName input file
     * @param deleteFiles true to delete the output file
     */
    public String write(ProgressVisitor progress, String tableName, File fileName, boolean deleteFiles) throws IOException, SQLException {
        if (tableName == null) {
            throw new SQLException("The select query or the table name must not be null.");
        }
        String regex = ".*(?i)\\b(select|from)\\b.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableName);
        if (matcher.find()) {
            if (tableName.startsWith("(") && tableName.endsWith(")")) {
                if (FileUtilities.isExtensionWellFormated(fileName, "fgb")) {
                    if (deleteFiles) {
                        Files.deleteIfExists(fileName.toPath());
                    } else if (fileName.exists()) {
                        throw new IOException("The json file already exist.");
                    }
                    PreparedStatement ps = connection.prepareStatement(tableName, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    JDBCUtilities.attachCancelResultSet(ps, progress);
                    ResultSet rs = ps.executeQuery();
                    Tuple<String, Integer> spatialFieldNameAndIndex = GeometryTableUtilities.getFirstGeometryColumnNameAndIndex(rs);
                    int srid =0;
                    int recordCount = 0;
                    rs.last();
                    recordCount = rs.getRow();
                    Object value = rs.getObject(spatialFieldNameAndIndex.second());
                    if(value!=null){
                        Geometry geom = (Geometry) value;
                        srid = geom.getSRID();
                    }
                    rs.beforeFirst();
                    ProgressVisitor copyProgress = progress.subProcess(recordCount);
                    doExport(progress, rs,  spatialFieldNameAndIndex.first(), "GEOMETRY",srid, recordCount, new FileOutputStream(fileName), null);
                    copyProgress.endOfProgress();
                    return fileName.getAbsolutePath();
                } else {
                    throw new SQLException("Only .fgb extension is supported");
                }

            } else {
                throw new SQLException("The select query must be enclosed in parenthesis: '(SELECT * FROM ORDERS)'.");
            }
        } else {
            if (FileUtilities.isExtensionWellFormated(fileName, "fgb")) {
                if (deleteFiles) {
                    Files.deleteIfExists(fileName.toPath());
                } else if (fileName.exists()) {
                    throw new IOException("The geojson file already exist.");
                }
                String filePath = fileName.getName();
                final int dotIndex = filePath.lastIndexOf('.');
                final String fileNameWithoutExt = filePath.substring(0, dotIndex);

                fgbWrite(progress, tableName, new FileOutputStream(fileName), fileNameWithoutExt);

                return fileName.getAbsolutePath();

            } else {
                throw new SQLException("Only .fgb extension is supported");
            }
        }
    }

    private static void writeString(String value, ByteBuffer bufferManager) throws IOException {
        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);
        bufferManager.putInt(stringBytes.length);
        bufferManager.put(stringBytes);
    }

    private void fgbWrite(ProgressVisitor progress, String tableName, FileOutputStream outputStream, String fileNameWithoutExt) throws IOException, SQLException {
        try {
            DBTypes dbTypes = DBUtils.getDBType(connection);
            final TableLocation parse = TableLocation.parse(tableName, dbTypes);
            String outputTable = parse.toString();
            int recordCount = JDBCUtilities.getRowCount(connection, outputTable);
                try ( // Read table content
                      Statement st = connection.createStatement()) {
                    Tuple<String, GeometryMetaData> geomMetadata = GeometryTableUtilities.getFirstColumnMetaData(connection, parse);
                    String geomCol = geomMetadata.first();
                    geomMetadata.second();
                    ResultSet rs = st.executeQuery(String.format("select * from %s", outputTable));
                    doExport(progress, rs, geomCol, geomMetadata.second().sfs_geometryType, geomMetadata.second().SRID, recordCount, outputStream, fileNameWithoutExt);
                }

        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ex) {
                throw new SQLException(ex);
            }
        }
    }

    private String doExport(ProgressVisitor progress, ResultSet rs, String geometryColumn, String geometryType, int srid,  int recordCount, FileOutputStream outputStream, String fileName) throws SQLException, IOException {

        FlatBufferBuilder bufferBuilder = new FlatBufferBuilder();

        //Write the header
        HeaderMeta header = writeHeader(outputStream, fileName, bufferBuilder, recordCount, geometryType, srid, rs.getMetaData());

        long endHeaderPosition = outputStream.getChannel().position();
        //Columns numbers
        int columnCount = header.columns.size();
        List<PackedRTree.Item> envelopes = null;
        if(header.featuresCount>0 && createIndex && header.featuresCount < Integer.MAX_VALUE) {
            envelopes = new ArrayList<>((int) header.featuresCount);
            long indexSize = PackedRTree.calcSize((int) header.featuresCount, packedRTreeNodeSize);
            byte[] buffer = new byte[512];
            long written = 0;
            while (written < indexSize) {
                if(buffer.length > indexSize - written) {
                    buffer = new byte[(int)(indexSize - written)];
                }
                outputStream.write(buffer);
                written += buffer.length;
            }
        }
        //Let's iterate the resultset
        long featureAddressPointer = 0;
        ByteBuffer bufferManager = ByteBuffer.allocate(BYTEBUFFER_CACHE);
        bufferManager.order(ByteOrder.LITTLE_ENDIAN);
        ProgressVisitor copyProgress = progress.subProcess(recordCount);
        while (rs.next()) {
            bufferManager.clear();
            //Let's serialize the attributes
            while (true) {
                try {
                    for (int i = 0; i < columnCount; i++) {
                        ColumnMeta column = header.columns.get(i);
                        Object value = rs.getObject(column.name);
                        if (value == null) {
                            continue;
                        }
                        bufferManager.putShort((short) i);
                        byte type = column.type;
                        switch (type) {
                            case ColumnType.Bool:
                                bufferManager.putShort((byte) ((boolean) value ? 1 : 0));
                                break;
                            case ColumnType.Byte:
                                bufferManager.putShort((byte) value);
                                break;
                            case ColumnType.Short:
                                bufferManager.putShort(((Integer) value).shortValue());
                                break;
                            case ColumnType.Int:
                                bufferManager.putInt((int) value);
                                break;
                            case ColumnType.Float:
                                if(value instanceof Float){
                                    bufferManager.putFloat((Float) value);
                                }
                                else {
                                    bufferManager.putFloat(((Double) value).floatValue());
                                }
                                break;
                            case ColumnType.Double:
                                if(value instanceof BigDecimal){
                                    bufferManager.putDouble(((BigDecimal) value).doubleValue());
                                }else {
                                    bufferManager.putDouble((Double) value);
                                }
                                break;
                            case ColumnType.Long:
                                if (value instanceof BigInteger) {
                                    bufferManager.putLong(((BigInteger) value).longValue());
                                } else {
                                    bufferManager.putLong((Long) value);
                                }
                                break;
                            case ColumnType.String:
                                writeString(value.toString(), bufferManager);
                                break;
                            case ColumnType.DateTime:
                                if (value instanceof ZonedDateTime) {
                                    // ISO 8601
                                    String iso8601Date = ((ZonedDateTime) value).format(DateTimeFormatter.ISO_INSTANT);
                                    writeString(iso8601Date, bufferManager);
                                } else {
                                    throw new RuntimeException(
                                            "Cannot handle type " + value.getClass().getName()+ " with "
                                                    + ColumnType.names[column.type]);
                                }
                                break;
                            default:
                                throw new RuntimeException(
                                        "Cannot handle type " + value.getClass().getName()+ " with "
                                                + ColumnType.names[column.type]);
                        }
                    }
                    break;
                } catch (BufferOverflowException ex) {
                    // Not enough cache, increase it
                    bufferManager = ByteBuffer.allocate(bufferManager.capacity() * 2);
                    bufferManager.order(ByteOrder.LITTLE_ENDIAN);
                }
            }

            int propertiesOffset = 0;
            if (bufferManager.position() > 0) {
                bufferManager.flip();
                propertiesOffset = Feature.createPropertiesVector(bufferBuilder, bufferManager);
                bufferManager.clear();
            }

            //Let's serialize the geometry
            int geometryOffset = 0;
            Geometry geom = (Geometry) rs.getObject(geometryColumn);
            if (geom != null) {
                geometryOffset = GeometryConversions.serialize(bufferBuilder, geom, header.geometryType);
            }
            int featureOffset = Feature.createFeature(bufferBuilder, geometryOffset, propertiesOffset, 0);
            if (envelopes != null) {
                PackedRTree.FeatureItem featureItem = new PackedRTree.FeatureItem();
                Envelope geomEnvelope;
                if(geom != null && !geom.isEmpty()) {
                    geomEnvelope = geom.getEnvelopeInternal();
                }  else {
                    geomEnvelope = new Envelope(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
                }
                featureItem.nodeItem = new NodeItem(geomEnvelope.getMinX(), geomEnvelope.getMinY(),
                        geomEnvelope.getMaxX(), geomEnvelope.getMaxY(), featureAddressPointer);
                envelopes.add(featureItem);
            }
            bufferBuilder.finishSizePrefixed(featureOffset);
            byte[] featureData = bufferBuilder.sizedByteArray();
            featureAddressPointer += featureData.length;
            outputStream.write(featureData);
            bufferBuilder.clear();
            copyProgress.endStep();
        }
        if(envelopes != null) {
            // Write spatial index after the header and before the first feature
            NodeItem extend = new NodeItem(0);
            envelopes.forEach(x -> extend.expand(x.nodeItem));
            PackedRTree.hilbertSort(envelopes, extend);
            PackedRTree packedRTree = new PackedRTree(Lists.reverse(envelopes), packedRTreeNodeSize);
            FileChannel fileChannel = outputStream.getChannel();
            fileChannel.position(endHeaderPosition);
            packedRTree.write(outputStream);
        }

        return "";
    }

    /**
     * Write the header
     *
     * @param outputStream output file
     * @param fileName name of the file
     * @param rowCount number of rows
     * @param geometryType type of geometry
     * @param srid table srid
     * @param metadata flatbuffer metadata
     * @return flatbuffer header object
     */
    private HeaderMeta writeHeader(FileOutputStream outputStream, String fileName, FlatBufferBuilder bufferBuilder, long rowCount, String geometryType, int srid, ResultSetMetaData metadata) throws SQLException, IOException {
        outputStream.write(Constants.MAGIC_BYTES);
        //Get the column information
        List<ColumnMeta> columns = new ArrayList<>();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String typeName = metadata.getColumnTypeName(i);
            if (metadata.getColumnTypeName(i).toLowerCase().startsWith("geometry")) {
                continue;
            }
            ColumnMeta column = new ColumnMeta();
            column.name = metadata.getColumnName(i);
            column.type = columnType(metadata.getColumnType(i), typeName);
            column.width=metadata.getPrecision(i);
            column.scale=metadata.getScale(i);
            column.precision=metadata.getPrecision(i);
            columns.add(column);
        }
        HeaderMeta headerMeta = new HeaderMeta();
        headerMeta.name=fileName;
        headerMeta.featuresCount = rowCount;
        headerMeta.geometryType = geometryType(geometryType);
        headerMeta.columns = columns;
        headerMeta.srid = srid;
        if(createIndex) {
            headerMeta.indexNodeSize = packedRTreeNodeSize;
        } else {
            headerMeta.indexNodeSize = 0;
        }
        HeaderMeta.write(headerMeta, outputStream, bufferBuilder);
        bufferBuilder.clear();
        return headerMeta;
    }

    private byte geometryType(String geometryTypeName) throws SQLException {
        switch (geometryTypeName) {
            case "POINT":
                return GeometryType.Point;
            case "LINESTRING":
                return GeometryType.LineString;
            case "POLYGON":
                return GeometryType.Polygon;
            case "MULTIPOINT":
                return GeometryType.MultiPoint;
            case "MULTILINESTRING":
                return GeometryType.MultiLineString;
            case "MULTIPOLYGON":
                return GeometryType.MultiPolygon;
            case "GEOMETRYCOLLECTION":
                return GeometryType.GeometryCollection;
            case "GEOMETRY":
                return GeometryType.Unknown;
            default:
                throw new RuntimeException("SQL type not supported : " + geometryTypeName);
        }
    }

    private byte columnType(int sqlTypeId, String sqlType) throws SQLException {
        switch (sqlTypeId) {
            case Types.BIT:
            case Types.BOOLEAN:
                return ColumnType.Bool;
            case Types.DATE:
                return ColumnType.DateTime;
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.DOUBLE:
                return ColumnType.Double;
            case Types.FLOAT:
            case Types.REAL:
                return ColumnType.Float;
            case Types.INTEGER:
                return ColumnType.Int;
            case Types.BIGINT:
                return ColumnType.Long;
            case Types.TINYINT:
            case Types.SMALLINT:
                return ColumnType.Short;
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.CHAR:
                return ColumnType.String;
            default:
                throw new RuntimeException("SQL type not supported : " + sqlType);
        }
    }


}
