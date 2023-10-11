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

import com.google.flatbuffers.FlatBufferBuilder;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.fgb.fileTable.GeometryConversions;
import org.h2gis.functions.io.utility.WriteBufferManager;
import org.h2gis.utilities.*;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.Geometry;
import org.wololo.flatgeobuf.ColumnMeta;
import org.wololo.flatgeobuf.Constants;
import org.wololo.flatgeobuf.HeaderMeta;
import org.wololo.flatgeobuf.generated.ColumnType;
import org.wololo.flatgeobuf.generated.Feature;
import org.wololo.flatgeobuf.generated.GeometryType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class FGBWriteDriver {
    private final Connection connection;

    public FGBWriteDriver(Connection connection) {
        this.connection = connection;
    }

    /**
     * Write the spatial table to a FlatGeobuf file
     *
     * @param progress
     * @param tableName
     * @param fileName
     * @param options
     * @param deleteFiles
     */
    public void write(ProgressVisitor progress, String tableName, File fileName, String options, boolean deleteFiles) throws IOException, SQLException {
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
                    ResultSet rs = ps.executeQuery();
                    fgbWrite(progress, rs, new FileOutputStream(fileName));
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
                fgbWrite(progress, tableName, new FileOutputStream(fileName));

            } else {
                throw new SQLException("Only .fgb extension is supported");
            }
        }


    }

    private static void writeString(String value, WriteBufferManager bufferManager) throws IOException {
        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);
        bufferManager.putInt(stringBytes.length);
        bufferManager.putBytes(stringBytes);
    }

    private void fgbWrite(ProgressVisitor progress, String tableName, FileOutputStream outputStream) throws IOException, SQLException {
        try {
            DBTypes dbTypes = DBUtils.getDBType(connection);
            final TableLocation parse = TableLocation.parse(tableName, dbTypes);
            String outputTable = parse.toString();
            int recordCount = JDBCUtilities.getRowCount(connection, outputTable);
            if (recordCount > 0) {
                ProgressVisitor copyProgress = progress.subProcess(recordCount);
                try ( // Read table content
                      Statement st = connection.createStatement()) {
                    Tuple<String, GeometryMetaData> geomMetadata = GeometryTableUtilities.getFirstColumnMetaData(connection, parse);
                    String geomCol = geomMetadata.first();
                    ResultSet rs = st.executeQuery(String.format("select * from %s", outputTable));

                    ResultSetMetaData rsmd = rs.getMetaData();
                    FlatBufferBuilder bufferBuilder = new FlatBufferBuilder();

                    FileChannel channel = outputStream.getChannel();

                    //Write the header
                    HeaderMeta header = writeHeader(outputStream, bufferBuilder, recordCount, geomMetadata.second(), rsmd);
                    //Columns numbers
                    int columnCount = header.columns.size();
                    //Let's iterate the resultset
                    while (rs.next()) {
                        WriteBufferManager bufferManager = new WriteBufferManager(channel);
                        bufferManager.order(ByteOrder.LITTLE_ENDIAN);

                        //Let's serialize the attributes
                        for (int i = 0; i < columnCount; i++) {
                            ColumnMeta column = header.columns.get(i);
                            Object value = rs.getObject(column.name);
                            if (value == null) {
                                continue;
                            }
                            bufferManager.putShort((short) i);
                            byte type = column.type;
                            if (type == ColumnType.Bool) {
                                bufferManager.putShort((byte) ((boolean) value ? 1 : 0));
                            } else if (type == ColumnType.Byte) {
                                bufferManager.putShort((byte) value);
                            } else if (type == ColumnType.Short) {
                                bufferManager.putShort((short) value);
                            } else if (type == ColumnType.Int) {
                                bufferManager.putInt((int) value);
                            } else if (type == ColumnType.Float) {
                                bufferManager.putFloat((float) value);
                            } else if (type == ColumnType.Double) {
                                bufferManager.putDouble((float) value);
                            } else if (type == ColumnType.Long) {
                                if (value instanceof BigInteger) {
                                    bufferManager.putLong(((BigInteger) value).longValue());
                                } else {
                                    bufferManager.putLong((Long) value);
                                }
                            } else if (type == ColumnType.String) {
                                writeString(value.toString(), bufferManager);
                            } else if (type == ColumnType.DateTime) {
                                if(value instanceof ZonedDateTime) {
                                    // ISO 8601
                                    String iso8601Date = ((ZonedDateTime)value).format(DateTimeFormatter.ISO_INSTANT);
                                    writeString(iso8601Date, bufferManager);
                                } else {
                                    bufferManager.putInt(0);
                                }
                            } else {
                                throw new RuntimeException(
                                        "Cannot handle type " + value.getClass().getName());
                            }
                        }

                        int propertiesOffset = 0;
                        if (bufferManager.position() > 0) {
                            bufferManager.flip();
                            propertiesOffset = Feature.createPropertiesVector(bufferBuilder, bufferManager.getBuffer());
                        }


                        //Let's serialize the geometry
                        int geometryOffset = 0;
                        Geometry geom = (Geometry) rs.getObject(geomCol);
                        if (geom != null) {
                            geometryOffset = GeometryConversions.serialize(bufferBuilder, geom, header.geometryType);
                        }
                        int featureOffset = Feature.createFeature(bufferBuilder, geometryOffset, propertiesOffset, 0);
                        bufferBuilder.finishSizePrefixed(featureOffset);
                        WritableByteChannel channel_ = Channels.newChannel(outputStream);
                        ByteBuffer dataBuffer = bufferBuilder.dataBuffer();
                        while (dataBuffer.hasRemaining()) {
                            channel_.write(dataBuffer);
                        }
                        bufferManager.clear();
                    }


                }
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

    private void fgbWrite(ProgressVisitor progress, ResultSet rs, FileOutputStream fileOutputStream) {
    }

    /**
     * Write the header
     *
     * @param outputStream
     * @param rowCount
     * @param geometryMetaData
     * @param metadata
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private HeaderMeta writeHeader(FileOutputStream outputStream, FlatBufferBuilder bufferBuilder, long rowCount, GeometryMetaData geometryMetaData, ResultSetMetaData metadata) throws SQLException, IOException {
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
            columns.add(column);
        }
        HeaderMeta headerMeta = new HeaderMeta();
        headerMeta.featuresCount = rowCount;
        headerMeta.geometryType = geometryType(geometryMetaData.getSfs_geometryType());
        headerMeta.columns = columns;
        headerMeta.srid = geometryMetaData.getSRID();
        HeaderMeta.write(headerMeta, outputStream, bufferBuilder);
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
