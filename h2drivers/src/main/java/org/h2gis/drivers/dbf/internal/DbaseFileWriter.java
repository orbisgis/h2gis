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
/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This file is based on an origional contained in the GISToolkit project:
 *    http://gistoolkit.sourceforge.net/
 */
package org.h2gis.drivers.dbf.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A DbaseFileReader is used to read a dbase III format file. The general use of
 * this class is: <CODE><PRE>
 * DbaseFileHeader header = ...
 * WritableFileChannel out = new FileOutputStream(&quot;thefile.dbf&quot;).getChannel();
 * DbaseFileWriter w = new DbaseFileWriter(header,out);
 * while ( moreRecords ) {
 *   w.write( getMyRecord() );
 * }
 * w.close();
 * </PRE></CODE> You must supply the <CODE>moreRecords</CODE> and
 * <CODE>getMyRecord()</CODE> logic...
 *
 * @author Ian Schneider
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/dbf/DbaseFileWriter.java $
 */
public class DbaseFileWriter {

	private DbaseFileHeader header;
	private DbaseFileWriter.FieldFormatter formatter;
	WritableByteChannel channel;
	private ByteBuffer buffer;
	private static final Number NULL_NUMBER = Integer.valueOf(0);
	private static final String NULL_STRING = "";
	private Charset charset;

	/**
	 * Create a DbaseFileWriter using the specified header and writing to the
	 * given channel.
	 *
	 * @param header
	 *            The DbaseFileHeader to write.
	 * @param out
	 *            The Channel to write to.
	 * @throws java.io.IOException
	 *             If errors occur while initializing.
	 */
	public DbaseFileWriter(DbaseFileHeader header, WritableByteChannel out)
			throws IOException {
		this(header, out, Charset.forName(header.getFileEncoding()));
	}

    /**
     * @return The DbaseFileHeader to write.
     */
    public DbaseFileHeader getHeader() {
        return header;
    }

    /**
	 * Create a DbaseFileWriter using the specified header and writing to the
	 * given channel.
	 *
	 * @param header
	 *            The DbaseFileHeader to write.
	 * @param out
	 *            The Channel to write to.
	 * @param charset
	 *            The charset the dbf is (will be) encoded in
	 * @throws java.io.IOException
	 *             If errors occur while initializing.
	 */
	public DbaseFileWriter(DbaseFileHeader header, WritableByteChannel out,
			Charset charset) throws IOException {
		header.writeHeader(out);
		this.header = header;
		this.channel = out;
        // DBase does not support UTF-8
		this.charset = charset == null ? Charset.forName(DbaseFileHeader.DEFAULT_ENCODING) : charset;
		this.formatter = new DbaseFileWriter.FieldFormatter(this.charset);
		init();
	}

	private void init() throws IOException {
		buffer = ByteBuffer.allocateDirect(header.getRecordLength());
	}

	private void write() throws IOException {
		buffer.position(0);
		int r = buffer.remaining();
                do {
                        r -= channel.write(buffer);
                } while (r > 0);
	}

	/**
	 * Write a single dbase record.
	 *
	 * @param record
	 *            The entries to write.
	 * @throws java.io.IOException
	 *             If IO error occurs.
	 * @throws DbaseFileException
	 *             If the entry doesn't comply to the header.
	 */
	public void write(Object[] record) throws IOException, DbaseFileException {

		if (record.length != header.getNumFields()) {
			throw new DbaseFileException("Wrong number of fields "
					+ record.length + " expected " + header.getNumFields());
		}

		buffer.position(0);

		// put the 'not-deleted' marker
		buffer.put((byte) ' ');

		for (int i = 0; i < header.getNumFields(); i++) {
			String fieldString = fieldString(record[i], i);
			if (header.getFieldLength(i) != fieldString
					.getBytes(charset.name()).length) {
				buffer.put(new byte[header.getFieldLength(i)]);
			} else {
				buffer.put(fieldString.getBytes(charset.name()));
			}

		}

		write();
	}

	private String fieldString(Object obj, final int col) {
		String o;
		final int fieldLen = header.getFieldLength(col);
		switch (header.getFieldType(col)) {
		case 'C':
                case 'M':
                case 'G':
		case 'c':
			o = formatter.getFieldString(fieldLen, obj != null ? obj.toString() : NULL_STRING);
			break;
		case 'L':
		case 'l':
			o = (obj == null ? "F" : (Boolean)obj ? "T" : "F");
			break;
		case 'N':
		case 'n':
			// int?
			if (header.getFieldDecimalCount(col) == 0) {
				o = formatter.getFieldString(fieldLen, 0, (obj instanceof Number ? (Number)obj : NULL_NUMBER));
				break;
			}
		case 'F':
		case 'f':
			o = formatter.getFieldString(fieldLen, header
					.getFieldDecimalCount(col), (obj instanceof Number ? (Number)obj : NULL_NUMBER));
			break;
		case 'D':
		case 'd':
			o = formatter.getFieldString((obj instanceof Date ? (Date)obj : null));
			break;
		default:
			throw new IllegalStateException("Unknown type "
					+ header.getFieldType(col));
		}
		return o;
	}

	/**
	 * Release resources associated with this writer. <B>Highly recommended</B>
	 *
	 * @throws java.io.IOException
	 *             If errors occur.
	 */
	public void close() throws IOException {
		// IANS - GEOT 193, bogus 0x00 written. According to dbf spec, optional
		// eof 0x1a marker is, well, optional. Since the original code wrote a
		// 0x00 (which is wrong anyway) lets just do away with this :)
		// - produced dbf works in OpenOffice and ArcExplorer java, so it must
		// be okay.
		// buffer.position(0);
		// buffer.put((byte) 0).position(0).limit(1);
		// write();
		if (channel.isOpen()) {
			channel.close();
		}

		buffer = null;
		channel = null;
		formatter = null;
	}

	/** Utility for formatting Dbase fields. */
	public static class FieldFormatter {
		private StringBuffer buffer = new StringBuffer(255);
		private NumberFormat numFormat = NumberFormat
				.getNumberInstance(Locale.US);
		private Calendar calendar = Calendar.getInstance(Locale.US);
		private String emptyString;
		private static final int MAXCHARS = 255;
		private Charset charset;

		public FieldFormatter(Charset charset) {
			// Avoid grouping on number format
			numFormat.setGroupingUsed(false);

			// build a 255 white spaces string
			StringBuilder sb = new StringBuilder(MAXCHARS);
			sb.setLength(MAXCHARS);
			for (int i = 0; i < MAXCHARS; i++) {
				sb.setCharAt(i, ' ');
			}

			this.charset = charset;

			emptyString = sb.toString();
		}

		public String getFieldString(int size, String s) {
			try {
				buffer.replace(0, size, emptyString);
				buffer.setLength(size);
				// international characters must be accounted for so size !=
				// length.
				int maxSize = size;
				if (s != null) {
					buffer.replace(0, size, s);
					int currentBytes = s.substring(0,
							Math.min(size, s.length()))
							.getBytes(charset.name()).length;
					if (currentBytes > size) {
						char[] c = new char[1];
						for (int index = size - 1; currentBytes > size; index--) {
							if (buffer.length() > index) {
								c[0] = buffer.charAt(index);
								String string = new String(c);
								buffer.deleteCharAt(index);
								currentBytes -= string.getBytes().length;
								maxSize--;
							}
						}
					} else {
						if (s.length() < size) {
							maxSize = size - (currentBytes - s.length());
							for (int i = s.length(); i < size; i++) {
								buffer.append(' ');
							}
						}
					}
				}

				buffer.setLength(maxSize);

				return buffer.toString();
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("This error should never happen...", e);
			}
		}

		public String getFieldString(Date d) {

			if (d != null) {
				buffer.delete(0, buffer.length());

				calendar.setTime(d);
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH) + 1; // returns 0
				// based month?
				int day = calendar.get(Calendar.DAY_OF_MONTH);

				if (year < 1000) {
					if (year >= 100) {
						buffer.append('0');
					} else if (year >= 10) {
						buffer.append("00");
					} else {
						buffer.append("000");
					}
				}
				buffer.append(year);

				if (month < 10) {
					buffer.append('0');
				}
				buffer.append(month);

				if (day < 10) {
					buffer.append('0');
				}
				buffer.append(day);
			} else {
				buffer.setLength(8);
				buffer.replace(0, 8, emptyString);
			}

			buffer.setLength(8);
			return buffer.toString();
		}

		public String getFieldString(int size, int decimalPlaces, Number n) {
			buffer.delete(0, buffer.length());

			if (n != null) {
				numFormat.setMaximumFractionDigits(decimalPlaces);
				numFormat.setMinimumFractionDigits(decimalPlaces);
				numFormat.format(n, buffer, new FieldPosition(
						NumberFormat.INTEGER_FIELD));
			}

			int diff = size - buffer.length();
			if (diff >= 0) {
				while (diff-- > 0) {
					buffer.insert(0, ' ');
				}
			} else {
				buffer.setLength(size);
			}
			return buffer.toString();
		}
	}

}
