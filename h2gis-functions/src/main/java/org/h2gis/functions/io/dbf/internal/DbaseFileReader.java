/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.io.dbf.internal;

import org.h2.value.*;
import org.h2gis.functions.io.utility.ReadBufferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Calendar;

/**
 * A DbaseFileReader is used to read a dbase III format file. <br>
 * The general use of this class is: <CODE><PRE>
 *
 * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
 * DbaseFileReader r = new DbaseFileReader( in ) Object[] fields = new
 * Object[r.getHeader().getNumFields()]; while (r.hasNext()) {
 * r.readEntry(fields); // do stuff } r.close();
 *
 * </PRE></CODE> For consumers who wish to be a bit more selective with their
 * reading of rows, the Row object has been added. The semantics are the same as
 * using the readEntry method, but remember that the Row object is always the
 * same. The values are parsed as they are read, so it pays to copy them out (as
 * each call to Row.read() will result in an expensive String parse). <br>
 * <b>EACH CALL TO readEntry OR readRow ADVANCES THE FILE!</b><br>
 * An example of using the Row method of reading: <CODE><PRE>
 *
 * FileChannel in = new FileInputStream("thefile.dbf").getChannel();
 * DbaseFileReader r = new DbaseFileReader( in ) int fields =
 * r.getHeader().getNumFields(); while (r.hasNext()) { DbaseFileReader.Row row =
 * r.readRow(); for (int i = 0; i < fields; i++) { // do stuff Foo.bar(
 * row.read(i) ); } } r.close();
 *
 * </PRE></CODE>
 *
 * @author Ian Schneider
 * @see
 * "http://svn.geotools.org/geotools/tags/2.3.1/plugin/shapefile/src/org/geotools/data/shapefile/dbf/DbaseFileReader.java"
 */
public class DbaseFileReader {

    private DbaseFileHeader header;
    private ReadBufferManager buffer;
    private FileChannel channel;
    private CharBuffer charBuffer;
    private CharsetDecoder decoder;
    private char[] fieldTypes;
    private int[] fieldLengths;
    private static final Logger LOG = LoggerFactory.getLogger(DbaseFileReader.class);

    /**
     * Creates a new instance of DBaseFileReader
     *
     * @param channel The readable channel to use.
     * @throws java.io.IOException If an error occurs while initializing.
     */
    public DbaseFileReader(FileChannel channel, String forceEncoding)
            throws IOException {
        this.channel = channel;

        header = new DbaseFileHeader();
        header.readHeader(channel, forceEncoding);

        init();
    }

    private void init() throws IOException {
        buffer = new ReadBufferManager(channel);

        // The entire file is in little endian
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Set up some buffers and lookups for efficiency
        fieldTypes = new char[header.getNumFields()];
        fieldLengths = new int[header.getNumFields()];
        for (int i = 0, ii = header.getNumFields(); i < ii; i++) {
            fieldTypes[i] = header.getFieldType(i);
            fieldLengths[i] = header.getFieldLength(i);
        }

        charBuffer = CharBuffer.allocate(header.getRecordLength() - 1);
        Charset chars = Charset.forName(header.getFileEncoding());
        decoder = chars.newDecoder();
    }

    /**
     * Get the header from this file. The header is read upon instantiation.
     *
     * @return The header associated with this file or null if an error
     * occurred.
     */
    public DbaseFileHeader getHeader() {
        return header;
    }

    /**
     * Clean up all resources associated with this reader.<B>Highly
     * recomended.</B>
     *
     * @throws java.io.IOException If an error occurs.
     */
    public void close() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }

        buffer = null;
        channel = null;
        charBuffer = null;
        decoder = null;
        header = null;
    }

    /**
     * @param pos Pos index in the buffer
     * @param length Array length to extract
     * @return byte array extracted from the buffer
     * @throws IOException
     */
    private byte[] getBytes(long pos, int length) throws IOException {
        byte[] bytes = new byte[length];
        buffer.get(pos, bytes);
        return bytes;
    }

    public Value getFieldValue(int row, int column) throws IOException {
        long fieldPosition = getPositionFor(row, column);
        int fieldLength = getLengthFor(column);
        byte[] fieldBytes = getBytes(fieldPosition, fieldLength);
        ByteBuffer field = ByteBuffer.wrap(fieldBytes);

        charBuffer.clear();
        decoder.decode(field, charBuffer, true);
        charBuffer.flip();

        return readObject(0, column);

    }

    public int getLengthFor(int column) {
        return header.getFieldLength(column);
    }

    protected long getPositionFor(int row, int column) {
        long recordOffset = header.getHeaderLength() + (long) row
                * header.getRecordLength() + 1;
        long fieldOffset = 0;
        for (int i = 0; i < column; i++) {
            fieldOffset += header.getFieldLength(i);
        }

        return fieldOffset + recordOffset;
    }

    private Value readObject(final int fieldOffset, final int fieldNum) throws IOException {
        final char type = fieldTypes[fieldNum];
        final int fieldLen = fieldLengths[fieldNum];
        Value object = null;

        if (fieldLen > 0) {
            switch (type) {
                // (L)logical (T,t,F,f,Y,y,N,n)
                case 'l':
                case 'L':
                    final char cBool = charBuffer.charAt(fieldOffset);
                    switch (cBool) {
                        case 't':
                        case 'T':
                        case 'Y':
                        case 'y':
                            object = ValueBoolean.TRUE;
                            break;
                        case 'f':
                        case 'F':
                        case 'N':
                        case 'n':
                            object = ValueBoolean.FALSE;
                            break;
                        default:
                            //Should be interpreted as null
                            object = ValueNull.INSTANCE;
                    }
                    break;
                // (C)character (String)
                case 'c':
                case 'C':
                    //Null String
                    if (charBuffer.charAt(fieldOffset) != '\0') {
                        // oh, this seems like a lot of work to parse strings...but,
                        // For some reason if zero characters ( (int) char == 0 ) are
                        // allowed
                        // in these strings, they do not compare correctly later on down
                        // the
                        // line....
                        int start = fieldOffset;
                        int end = Math.min(fieldOffset + fieldLen - 1, charBuffer.length() - 1);
                        // trim off whitespace and 'zero' chars
                        while (start < end) {
                            char c = charBuffer.get(start);
                            if (c == 0 || Character.isWhitespace(c)) {
                                start++;
                            } else {
                                break;
                            }
                        }
                        while (end > start) {
                            try {
                                char c = charBuffer.get(end);
                                if (c == 0 || Character.isWhitespace(c)) {
                                    end--;
                                } else {
                                    break;
                                }
                            } catch (IndexOutOfBoundsException ex) {
                                throw new IndexOutOfBoundsException();
                            }
                        }
                        // set up the new indexes for start and end
                        // this prevents one array copy (the one made by String)
                        object = ValueVarchar.get(new String(charBuffer.array(), start, end + 1 - start));
                    }else{
                         object = ValueNull.INSTANCE;
                    }
                    break;
                // (D)date (Date)
                case 'd':
                case 'D':
                    if (charBuffer.toString().equals("00000000")) {
                        object = ValueNull.INSTANCE;
                    } else {
                        try {
                            String tempString = charBuffer.subSequence(fieldOffset,
                                    fieldOffset + 4).toString();
                            if (!tempString.trim().isEmpty()) {
                                int tempYear = Integer.parseInt(tempString);
                                tempString = charBuffer.subSequence(fieldOffset + 4,
                                        fieldOffset + 6).toString();
                                int tempMonth = Integer.parseInt(tempString) - 1;
                                tempString = charBuffer.subSequence(fieldOffset + 6,
                                        fieldOffset + 8).toString();
                                int tempDay = Integer.parseInt(tempString);
                                Calendar cal = Calendar.getInstance();
                                cal.clear();
                                cal.set(Calendar.YEAR, tempYear);
                                cal.set(Calendar.MONTH, tempMonth);
                                cal.set(Calendar.DAY_OF_MONTH, tempDay);
                                object = ValueNull.INSTANCE;//ValueDate.get(cal.getTime());
                            } else {
                                object = ValueNull.INSTANCE;
                            }
                        } catch (NumberFormatException nfe) {
                            // todo: use progresslistener, this isn't a grave error.
                            LOG.warn("There was an error parsing a date. Ignoring it.", nfe);
                        }
                    }
                    break;
                case 'n':
                case 'N':
                    // numbers that begin with '*' are considered null
                    if (charBuffer.charAt(fieldOffset) == '*') {
                        object = ValueNull.INSTANCE;
                        break;
                    } else {
                        try {
                            if (header.getFieldDecimalCount(fieldNum) == 0) {
                                String numberString = extractNumberString(charBuffer, fieldOffset,
                                        fieldLen);
                                object = ValueInteger.get(Integer.parseInt(numberString));
                                // parsing successful --> exit
                                break;
                            }
                            // else will fall through to the floating point number
                        } catch (NumberFormatException e) {

                            // todo: use progresslistener, this isn't a grave error.
                            // don't do this!!! the Double parse will be attemted as we
                            // fall
                            // through, so no need to create a new Object. -IanS
                            // object = new Integer(0);
                            // Lets try parsing a long instead...
                            try {

                                String numberString = extractNumberString(charBuffer, fieldOffset,
                                        fieldLen);
                                object = ValueBigint.get(Long.parseLong(numberString));
                                // parsing successful --> exit
                                break;
                            } catch (NumberFormatException e2) {
                                // it is not a long either
                                // so we do nothing.
                                // this whole method screams for refactoring...
                            }
                        }
                        // no break!!
                        // this case falls through the following one if there is decimal count
                        // I know, this is ugly...
                    }

                case 'f':
                case 'F': // floating point number
                    //Null float
                    if (charBuffer.charAt(fieldOffset) == '*') {
                        object = ValueNull.INSTANCE;
                        break;
                    } else {
                        String numberString = extractNumberString(charBuffer, fieldOffset,
                                fieldLen);
                        try {
                              object = ValueDouble.get(Double.parseDouble(numberString));                            
                        } catch (NumberFormatException e) {
                            // May be the decimal operator is exotic
                            if (numberString.contains(",")) {
                                object = ValueDouble.get(Double.parseDouble(numberString.replace(",", ".")));
                            }else{
                                object = ValueNull.INSTANCE;
                            }
                        }
                        break;
                    }
                default:
                    throw new IOException("Invalid field type : " + type);
            }

        }
        return object;
    }

    /**
     * @param charBuffer2
     * @param fieldOffset
     * @param fieldLen
     */
    private String extractNumberString(final CharBuffer charBuffer2,
            final int fieldOffset, final int fieldLen) {
        return charBuffer2.subSequence(fieldOffset,
                fieldOffset + fieldLen).toString().trim();
    }

    public int getRecordCount() {
        return header.getNumRecords();
    }

    /**
     * @return The number of columns
     */
    public int getFieldCount() {
        return header.getNumFields();
    }
}
