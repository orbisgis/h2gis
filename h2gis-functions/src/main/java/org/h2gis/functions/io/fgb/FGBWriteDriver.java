package org.h2gis.functions.io.fgb;

import com.google.flatbuffers.FlatBufferBuilder;
import org.h2gis.api.ProgressVisitor;
import org.h2gis.functions.io.utility.WriteBufferManager;
import org.h2gis.utilities.*;
import org.h2gis.utilities.dbtypes.DBTypes;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.Geometry;
import org.wololo.flatgeobuf.ColumnMeta;
import org.wololo.flatgeobuf.Constants;
import org.wololo.flatgeobuf.GeometryConversions;
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
import java.nio.file.Files;
import java.sql.*;
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
        this.connection=connection;
    }

    /**
     * Write the spatial table to a FlatGeobuf file
     * @param progress
     * @param tableName
     * @param fileName
     * @param options
     * @param deleteFiles
     */
    public void write(ProgressVisitor progress, String tableName, File fileName, String options, boolean deleteFiles) throws IOException, SQLException {
        if(tableName == null) {
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
        }

        else {
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
                    try {
                        ResultSetMetaData rsmd = rs.getMetaData();
                    FlatBufferBuilder bufferBuilder = new FlatBufferBuilder(16*1024);
                    outputStream.write(Constants.MAGIC_BYTES);
                        FileChannel channel = outputStream.getChannel();
                        WriteBufferManager bufferManager = new WriteBufferManager(channel);
                        bufferManager.order(ByteOrder.LITTLE_ENDIAN);
                    //Write the header
                        HeaderMeta header = writeHeader(recordCount, geomMetadata.second(), rsmd);
                        HeaderMeta.write(header, outputStream, bufferBuilder);
                        bufferBuilder.clear();
                        //Columns numbers
                        int columnCount = header.columns.size();
                    //Let's iterate the resulset
                    while (rs.next()){
                        //Let's serialize the attributes
                        for (int i = 0; i < columnCount; i++) {
                            ColumnMeta column = header.columns.get(i);

                            Object value =  rs.getObject(column.name);
                            if (value == null) {
                                continue;
                            }
                            bufferManager.putShort((short) i);
                            byte type = column.type;
                            switch (type) {
                                case ColumnType.Bool:
                                    bufferManager.putShort((byte) ((boolean) value ? 1 : 0));
                                    return;
                                case ColumnType.Byte:
                                    bufferManager.putShort((byte)  value );
                                    return;
                                case ColumnType.Short:
                                    bufferManager.putShort((short)  value );
                                    return;
                                case ColumnType.Int:
                                    bufferManager.putInt((int)  value );
                                    return;
                                case ColumnType.Float:
                                    bufferManager.putFloat((float)  value );
                                    return;
                                case ColumnType.Double:
                                    bufferManager.putDouble((float)  value );
                                    return;
                                case ColumnType.Long:
                                    if(value instanceof BigInteger) {
                                        bufferManager.putLong(((BigInteger) value).longValue());
                                    }else {
                                        bufferManager.putLong((Long) value);
                                    }
                                    return;
                                case ColumnType.String:
                                    //TODO
                                    return;
                                case ColumnType.DateTime:
                                    //TODO
                                    return;
                                default:
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
                        int geometryOffset=0;
                        Geometry geom = (Geometry)  rs.getObject(geomCol);
                        if(geom!=null) {
                            geometryOffset = GeometryConversions.serialize(bufferBuilder, geom, header.geometryType);
                        }
                        int featureOffset = Feature.createFeature(bufferBuilder, geometryOffset, propertiesOffset, 0);
                        bufferBuilder.finishSizePrefixed(featureOffset);

                        // Closing is caller responsibility
                        @SuppressWarnings("PMD.CloseResource")
                        WritableByteChannel channel_ = Channels.newChannel(outputStream);
                        ByteBuffer dataBuffer = bufferBuilder.dataBuffer();
                        while (dataBuffer.hasRemaining()) {
                            channel_.write(dataBuffer);
                        }
                    }
                        bufferManager.flush();

                    } finally {
                        rs.close();
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

    private HeaderMeta writeHeader(long rowCount, GeometryMetaData geometryMetaData,ResultSetMetaData metadata ) throws SQLException {
        //Get the column informations
        List<ColumnMeta> columns = new ArrayList<>();
        int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String typeName = metadata.getColumnTypeName(i);
            if(metadata.getColumnTypeName(i).toLowerCase().startsWith("geometry")){
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
        headerMeta.srid=geometryMetaData.getSRID();

        return headerMeta;
    }

    private byte geometryType(String geometryTypeName) throws SQLException{
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

    private byte columnType(int sqlTypeId, String sqlType) throws SQLException{
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
