/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
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

import org.h2gis.drivers.utility.ReadBufferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to represent the header of a Dbase III file. Creation date: (5/15/2001
 * 5:15:30 PM)
 *
 * @see "http://svn.geotools.org/geotools/tags/2.3.1/plugin/shapefile/src/org/geotools/data/shapefile/dbf/DbaseFileHeader.java"
 */
public class DbaseFileHeader {
    private Logger log = LoggerFactory.getLogger(DbaseFileHeader.class);

    /** Encoding of String, found in Language Code Page DBF Header, use ISO-8859-1 as default value*/
    public static String DEFAULT_ENCODING = "cp1252";
    public static byte DEFAULT_ENCODING_FLAG = 0x03;

    private static final String FIELD_LENGTH_FOR = "Field Length for ";
	// Constant for the size of a record
	private static final int FILE_DESCRIPTOR_SIZE = 32;
    /** @see "https://github.com/infused/dbf/blob/master/lib/dbf/encodings.rb" */
    private static Map<Byte,String> CODE_PAGE_ENCODING = new HashMap<Byte, String>();
    static {
        CODE_PAGE_ENCODING.put((byte) 0x00,"UTF-8");          // UTF-8
        CODE_PAGE_ENCODING.put((byte) 0x01,"cp437");          // U.S. MS–DOS
        CODE_PAGE_ENCODING.put((byte) 0x02,"cp850");          // International MS–DOS
        CODE_PAGE_ENCODING.put((byte) 0x03, DEFAULT_ENCODING);// Windows ANSI
        CODE_PAGE_ENCODING.put((byte) 0x08,"cp865");          // Danish OEM
        CODE_PAGE_ENCODING.put((byte) 0x09,"cp437");          // Dutch OEM
        CODE_PAGE_ENCODING.put((byte) 0x0a,"cp850");          // Dutch OEM
        CODE_PAGE_ENCODING.put((byte) 0x0b,"cp437");          // Finnish OEM
        CODE_PAGE_ENCODING.put((byte) 0x0d,"cp437");          // French OEM
        CODE_PAGE_ENCODING.put((byte) 0x0e,"cp850");          // French OEM
        CODE_PAGE_ENCODING.put((byte) 0x0f,"cp437");          // German OEM
        CODE_PAGE_ENCODING.put((byte) 0x10,"cp850");          // German OEM
        CODE_PAGE_ENCODING.put((byte) 0x11,"cp437");          // Italian OEM
        CODE_PAGE_ENCODING.put((byte) 0x12,"cp850");          // Italian OEM
        CODE_PAGE_ENCODING.put((byte) 0x13,"cp932");          // Japanese Shift-JIS
        CODE_PAGE_ENCODING.put((byte) 0x14,"cp850");          // Spanish OEM
        CODE_PAGE_ENCODING.put((byte) 0x15,"cp437");          // Swedish OEM
        CODE_PAGE_ENCODING.put((byte) 0x16,"cp850");          // Swedish OEM
        CODE_PAGE_ENCODING.put((byte) 0x17,"cp865");          // Norwegian OEM
        CODE_PAGE_ENCODING.put((byte) 0x18,"cp437");          // Spanish OEM
        CODE_PAGE_ENCODING.put((byte) 0x19,"cp437");          // English OEM (Britain)
        CODE_PAGE_ENCODING.put((byte) 0x1a,"cp850");          // English OEM (Britain)
        CODE_PAGE_ENCODING.put((byte) 0x1b,"cp437");          // English OEM (U.S.)
        CODE_PAGE_ENCODING.put((byte) 0x1c,"cp863");          // French OEM (Canada)
        CODE_PAGE_ENCODING.put((byte) 0x1d,"cp850");          // French OEM
        CODE_PAGE_ENCODING.put((byte) 0x1f,"cp852");          // Czech OEM
        CODE_PAGE_ENCODING.put((byte) 0x22,"cp852");          // Hungarian OEM
        CODE_PAGE_ENCODING.put((byte) 0x23,"cp852");          // Polish OEM
        CODE_PAGE_ENCODING.put((byte) 0x24,"cp860");          // Portuguese OEM
        CODE_PAGE_ENCODING.put((byte) 0x25,"cp850");          // Portuguese OEM
        CODE_PAGE_ENCODING.put((byte) 0x26,"cp866");          // Russian OEM
        CODE_PAGE_ENCODING.put((byte) 0x37,"cp850");          // English OEM (U.S.)
        CODE_PAGE_ENCODING.put((byte) 0x40,"cp852");          // Romanian OEM
        CODE_PAGE_ENCODING.put((byte) 0x4d,"cp936");          // Chinese GBK (PRC)
        CODE_PAGE_ENCODING.put((byte) 0x4e,"cp949");          // Korean (ANSI/OEM)
        CODE_PAGE_ENCODING.put((byte) 0x4f,"cp950");          // Chinese Big5 (Taiwan)
        CODE_PAGE_ENCODING.put((byte) 0x50,"cp874");          // Thai (ANSI/OEM)
        CODE_PAGE_ENCODING.put((byte) 0x57, DEFAULT_ENCODING);// ANSI
        CODE_PAGE_ENCODING.put((byte) 0x58, DEFAULT_ENCODING);// Western European ANSI
        CODE_PAGE_ENCODING.put((byte) 0x59, DEFAULT_ENCODING);// Spanish ANSI
        CODE_PAGE_ENCODING.put((byte) 0x64,"cp852");          // Eastern European MS–DOS
        CODE_PAGE_ENCODING.put((byte) 0x65,"cp866");          // Russian MS–DOS
        CODE_PAGE_ENCODING.put((byte) 0x66,"cp865");          // Nordic MS–DOS
        CODE_PAGE_ENCODING.put((byte) 0x67,"cp861");          // Icelandic MS–DOS
        CODE_PAGE_ENCODING.put((byte) 0x6a,"cp737");          // Greek MS–DOS (437G)
        CODE_PAGE_ENCODING.put((byte) 0x6b,"cp857");          // Turkish MS–DOS
        CODE_PAGE_ENCODING.put((byte) 0x6c,"cp863");          // French–Canadian MS–DOS
        CODE_PAGE_ENCODING.put((byte) 0x78,"cp950");          // Taiwan Big 5
        CODE_PAGE_ENCODING.put((byte) 0x79,"cp949");          // Hangul (Wansung)
        CODE_PAGE_ENCODING.put((byte) 0x7a,"cp936");          // PRC GBK
        CODE_PAGE_ENCODING.put((byte) 0x7b,"cp932");          // Japanese Shift-JIS
        CODE_PAGE_ENCODING.put((byte) 0x7c,"cp874");          // Thai Windows/MS–DOS
        CODE_PAGE_ENCODING.put((byte) 0x86,"cp737");          // Greek OEM
        CODE_PAGE_ENCODING.put((byte) 0x87,"cp852");          // Slovenian OEM
        CODE_PAGE_ENCODING.put((byte) 0x88,"cp857");          // Turkish OEM
        CODE_PAGE_ENCODING.put((byte) 0xc8,"cp1250");         // Eastern European Windows
        CODE_PAGE_ENCODING.put((byte) 0xc9,"cp1251");         // Russian Windows
        CODE_PAGE_ENCODING.put((byte) 0xca,"cp1254");         // Turkish Windows
        CODE_PAGE_ENCODING.put((byte) 0xcb,"cp1253");         // Greek Windows
        CODE_PAGE_ENCODING.put((byte) 0xcc,"cp1257");         // Baltic Windows
    }
	// type of the file, must be 03h
	private static final byte MAGIC = 0x03;

	private static final int MINIMUM_HEADER = 33;
        private static final String SET_TO = " set to ";

	// Date the file was last updated.
	private Date date = new Date();

	private int recordCnt = 0;

	private int fieldCnt = 0;

	// set this to a default length of 1, which is enough for one "space"
	// character which signifies an empty record
	private int recordLength = 1;

	// set this to a flagged value so if no fields are added before the write,
	// we know to adjust the headerLength to MINIMUM_HEADER
	private int headerLength = -1;

	private int largestFieldSize = 0;
    private String fileEncoding = DEFAULT_ENCODING;

	/**
	 * Class for holding the information assicated with a record.
	 */
	static class DbaseField {

		// Field Name
		String fieldName;

		// Field Type (C N L D or M)
		char fieldType;

		// Field Data Address offset from the start of the record.
		int fieldDataAddress;

		// Length of the data in bytes
		int fieldLength;

		// Field decimal count in Binary, indicating where the decimal is
		int decimalCount;

	}

	// collection of header records.
	// lets start out with a zero-length array, just in case
	private DbaseField[] fields = new DbaseField[0];

	/**
	 * Add a column to this DbaseFileHeader. The type is one of (C N L or D)
	 * character, number, logical(true/false), or date. The Field length is the
	 * total length in bytes reserved for this column. The decimal count only
	 * applies to numbers(N), and floating point values (F), and refers to the
	 * number of characters to reserve after the decimal point. <B>Don't expect
	 * miracles from this...</B>                         "02" => "cp850",       # International MS–DOS
	 *
	 * <PRE>
	 *
	 * Field Type MaxLength ---------- --------- C 254 D 8 F 20 N 18
	 *
	 * </PRE>
	 *
	 * @param inFieldName
	 *            The name of the new field, must be less than 10 characters or
	 *            it gets truncated.
	 * @param inFieldType
	 *            A character representing the dBase field, ( see above ). Case
	 *            insensitive.
	 * @param inFieldLength
	 *            The length of the field, in bytes ( see above )
	 * @param inDecimalCount
	 *            For numeric fields, the number of decimal places to track.
     * @throws DbaseFileException                        "02" => "cp850",       # International MS–DOS
	 *             If the type is not recognized.
	 */
	public void addColumn(String inFieldName, char inFieldType,
			int inFieldLength, int inDecimalCount) throws DbaseFileException {
		if (inFieldLength <= 0) {
			throw new DbaseFileException("field length <= 0");
		}
		if (fields == null) {
			fields = new DbaseField[0];
		}
		int tempLength = 1; // the length is used for the offset, and there is a
		// * for deleted as the first byte
		DbaseField[] tempFieldDescriptors = new DbaseField[fields.length + 1];
		for (int i = 0; i < fields.length; i++) {
			fields[i].fieldDataAddress = tempLength;
			tempLength += fields[i].fieldLength;
			tempFieldDescriptors[i] = fields[i];
		}
		tempFieldDescriptors[fields.length] = new DbaseField();
		tempFieldDescriptors[fields.length].fieldLength = inFieldLength;
		tempFieldDescriptors[fields.length].decimalCount = inDecimalCount;
		tempFieldDescriptors[fields.length].fieldDataAddress = tempLength;

		// set the field name
		String tempFieldName = inFieldName;
		if (tempFieldName == null) {
			tempFieldName = "NoName";
		}
		// Fix for GEOT-42, ArcExplorer will not handle field names > 10 chars
		// Sorry folks.
		if (tempFieldName.length() > 10) {
			tempFieldName = tempFieldName.substring(0, 10);
			log.warn("FieldName " + inFieldName
                    + " is longer than 10 characters, truncating to "
                    + tempFieldName);
		}
		tempFieldDescriptors[fields.length].fieldName = tempFieldName;

		// the field type
		if ((inFieldType == 'C') || (inFieldType == 'c')) {
			tempFieldDescriptors[fields.length].fieldType = 'C';
			if (inFieldLength > 254) {
				log.warn(FIELD_LENGTH_FOR
                        + inFieldName
                        + SET_TO
                        + inFieldLength
                        + " Which is longer than 254, not consistent with dbase III");
			}
		} else if ((inFieldType == 'S') || (inFieldType == 's')) {
			tempFieldDescriptors[fields.length].fieldType = 'C';
			log.warn("Field type for "
                    + inFieldName
                    + " set to S which is flat out wrong people!, I am setting this to C, in the hopes you meant character.");
			if (inFieldLength > 254) {
				log.warn(FIELD_LENGTH_FOR
                        + inFieldName
                        + SET_TO
                        + inFieldLength
                        + " Which is longer than 254, not consistent with dbase III");
			}
			tempFieldDescriptors[fields.length].fieldLength = 8;
		} else if ((inFieldType == 'D') || (inFieldType == 'd')) {
			tempFieldDescriptors[fields.length].fieldType = 'D';
			if (inFieldLength != 8) {
				log.warn(FIELD_LENGTH_FOR + inFieldName
                        + SET_TO + inFieldLength
                        + " Setting to 8 digets YYYYMMDD");
			}
			tempFieldDescriptors[fields.length].fieldLength = 8;
		} else if ((inFieldType == 'F') || (inFieldType == 'f')) {
			tempFieldDescriptors[fields.length].fieldType = 'F';
			if (inFieldLength > 20) {
				log.warn(FIELD_LENGTH_FOR
                        + inFieldName
                        + SET_TO
                        + inFieldLength
                        + " Preserving length, but should be set to Max of 20 not valid for dbase IV, and UP specification, not present in dbaseIII.");
			}
		} else if ((inFieldType == 'N') || (inFieldType == 'n')) {
			tempFieldDescriptors[fields.length].fieldType = 'N';
			if (inFieldLength > 18) {
				log.warn(FIELD_LENGTH_FOR
                        + inFieldName
                        + SET_TO
                        + inFieldLength
                        + " Preserving length, but should be set to Max of 18 for dbase III specification.");
			}
			if (inDecimalCount < 0) {
				log.warn("Field Decimal Position for "
                        + inFieldName + SET_TO + inDecimalCount
                        + " Setting to 0 no decimal data will be saved.");
				tempFieldDescriptors[fields.length].decimalCount = 0;
			}
			if (inDecimalCount > inFieldLength - 1) {
				log.warn("Field Decimal Position for "
                        + inFieldName + SET_TO + inDecimalCount
                        + " Setting to " + (inFieldLength - 1)
                        + " no non decimal data will be saved.");
				tempFieldDescriptors[fields.length].decimalCount = inFieldLength - 1;
			}
		} else if ((inFieldType == 'L') || (inFieldType == 'l')) {
			tempFieldDescriptors[fields.length].fieldType = 'L';
			if (inFieldLength != 1) {
				log.warn(FIELD_LENGTH_FOR + inFieldName
                        + SET_TO + inFieldLength
                        + " Setting to length of 1 for logical fields.");
			}
			tempFieldDescriptors[fields.length].fieldLength = 1;
		} else {
			throw new DbaseFileException("Undefined field type " + inFieldType
					+ " For column " + inFieldName);
		}
		// the length of a record
		tempLength += tempFieldDescriptors[fields.length].fieldLength;

		// set the new fields.
		fields = tempFieldDescriptors;
		fieldCnt = fields.length;
		headerLength = MINIMUM_HEADER + 32 * fields.length;
		recordLength = tempLength;
	}

	/**
	 * Remove a column from this DbaseFileHeader.
	 *
	 * @todo This is really ugly, don't know who wrote it, but it needs fixin...
	 * @param inFieldName
	 *            The name of the field, will ignore case and trim.
	 * @return index of the removed column, -1 if no found
	 */
	public int removeColumn(String inFieldName) {

		int retCol = -1;
		int tempLength = 1;
		DbaseField[] tempFieldDescriptors = new DbaseField[fields.length - 1];
		for (int i = 0, j = 0; i < fields.length; i++) {
			if (!inFieldName.equalsIgnoreCase(fields[i].fieldName.trim())) {
				// if this is the last field and we still haven't found the
				// named field
				if (i == j && i == fields.length - 1) {
					throw new IllegalArgumentException("Could not find a field named '"
							+ inFieldName + "' for removal");
				}
				tempFieldDescriptors[j] = fields[i];
				tempFieldDescriptors[j].fieldDataAddress = tempLength;
				tempLength += tempFieldDescriptors[j].fieldLength;
				// only increment j on non-matching fields
				j++;
			} else {
				retCol = i;
			}
		}

		// set the new fields.
		fields = tempFieldDescriptors;
		headerLength = 33 + 32 * fields.length;
		recordLength = tempLength;

		return retCol;
	}

	// Retrieve the length of the field at the given index
	/**
	 * Returns the field length in bytes.
	 *
	 * @param inIndex
	 *            The field index.
	 * @return The length in bytes.
	 */
	public int getFieldLength(int inIndex) {
		return fields[inIndex].fieldLength;
	}

    /**
     * @return File Encoding, ISO-8859-1 if the file encoding is not recognized
     */
    public String getFileEncoding() {
        return fileEncoding;
    }
// Retrieve the location of the decimal point within the field.
	/**
	 * Get the decimal count of this field.
	 *
	 * @param inIndex
	 *            The field index.
	 * @return The decimal count.
	 */
	public int getFieldDecimalCount(int inIndex) {
		return fields[inIndex].decimalCount;
	}

	// Retrieve the Name of the field at the given index
	/**
	 * Get the field name.
	 *
	 * @param inIndex
	 *            The field index.
	 * @return The name of the field.
	 */
	public String getFieldName(int inIndex) {
		return fields[inIndex].fieldName;
	}

	// Retrieve the type of field at the given index
	/**
	 * Get the character class of the field.
	 *
	 * @param inIndex
	 *            The field index.
	 * @return The dbase character representing this field.
	 */
	public char getFieldType(int inIndex) {
		return fields[inIndex].fieldType;
	}

	/**
	 * Get the date this file was last updated.
	 *
	 * @return The Date last modified.
	 */
	public Date getLastUpdateDate() {
		return date;
	}

	/**
	 * Return the number of fields in the records.
	 *
	 * @return The number of fields in this table.
	 */
	public int getNumFields() {
		return fields.length;
	}

	/**
	 * Return the number of records in the file
	 *
	 * @return The number of records in this table.
	 */
	public int getNumRecords() {
		return recordCnt;
	}

	/**
	 * Get the length of the records in bytes.
	 *
	 * @return The number of bytes per record.
	 */
	public int getRecordLength() {
		return recordLength;
	}

	/**
	 * Get the length of the header
	 *
	 * @return The length of the header in bytes.
	 */
	public int getHeaderLength() {
		return headerLength;
	}

	/**
	 * Read the header data from the DBF file.
	 *
	 * @param channel
	 *            A readable byte channel. If you have an InputStream you need
	 *            to use, you can call java.nio.Channels.getChannel(InputStream
	 *            in).
         * @throws java.io.IOException
	 *             If errors occur while reading.
	 */
	public void readHeader(FileChannel channel,String forceEncoding) throws IOException {
        if(forceEncoding != null) {
            fileEncoding = forceEncoding;
        }
		// we'll read in chunks of 1K
		ReadBufferManager in = new ReadBufferManager(channel);
		// do this or GO CRAZY
		// ByteBuffers come preset to BIG_ENDIAN !
		in.order(ByteOrder.LITTLE_ENDIAN);

		// type of file.
		byte magic = in.get();
		if (magic != MAGIC) {
			log.warn("Unsupported DBF file Type "
                    + Integer.toHexString(magic));
		}

		// parse the update date information.
		int tempUpdateYear = in.get();
		int tempUpdateMonth = in.get();
		int tempUpdateDay = in.get();
		// ouch Y2K uncompliant
		if (tempUpdateYear > 90) {
			tempUpdateYear += 1900;
		} else {
			tempUpdateYear += 2000;
		}
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, tempUpdateYear);
		c.set(Calendar.MONTH, tempUpdateMonth - 1);
		c.set(Calendar.DATE, tempUpdateDay);
		date = c.getTime();

		// read the number of records.
		recordCnt = in.getInt();

		// read the length of the header structure.
		// ahhh.. unsigned little-endian shorts
		// mask out the byte and or it with shifted 2nd byte
		headerLength = (in.get() & 0xff) | ((in.get() & 0xff) << 8);

		// read the length of a record
		// ahhh.. unsigned little-endian shorts
		recordLength = (in.get() & 0xff) | ((in.get() & 0xff) << 8);

		// skip / skip thesreserved bytes in the header.
		in.skip(17);
        // read Language driver
        byte lngDriver = in.get();
        String encoding = CODE_PAGE_ENCODING.get(lngDriver);
        if(encoding!=null && forceEncoding == null) {
            this.fileEncoding = encoding;
        }
        // skip reserved
        in.skip(2);

		// calculate the number of Fields in the header
		fieldCnt = (headerLength - FILE_DESCRIPTOR_SIZE - 1)
				/ FILE_DESCRIPTOR_SIZE;

		// read all of the header records
		List<DbaseField> lfields = new ArrayList<DbaseField>();
		for (int i = 0; i < fieldCnt; i++) {
			DbaseField field = new DbaseField();

			// read the field name
			byte[] buffer = new byte[11];
			in.get(buffer);
			String name = new String(buffer, fileEncoding);
			int nullPoint = name.indexOf(0);
			if (nullPoint != -1) {
				name = name.substring(0, nullPoint);
			}
			field.fieldName = name.trim();

			// read the field type
			field.fieldType = (char) in.get();

			// read the field data address, offset from the start of the record.
			field.fieldDataAddress = in.getInt();

			// read the field length in bytes
			int length = (int) in.get();
			if (length < 0) {
				length += 256;
			}
			field.fieldLength = length;

			if (length > largestFieldSize) {
				largestFieldSize = length;
			}

			// read the field decimal count in bytes
			field.decimalCount = (int) in.get();

			// rreservedvededved bytes.
			// in.skipBytes(14);
			in.skip(14);

			// some broken shapefiles have 0-length attributes. The reference
			// implementation
			// (ArcExplorer 2.0, built with MapObjects) just ignores them.
			if (field.fieldLength > 0) {
				lfields.add(field);
			}
		}

		// Last byte is a marker for the end of the field definitions.
		// in.skipBytes(1);
		in.skip(1);


		fields = new DbaseField[lfields.size()];
		fields = lfields.toArray(fields);
	}

	/**
	 * Get the largest field size of this table.
	 *
	 * @return The largt field size iiin bytes.
	 */
	public int getLargestFieldSize() {
		return largestFieldSize;
	}

	/**
	 * Set the number of records in the file
	 *
	 * @param inNumRecords
	 *            The number of records.
	 */
	public void setNumRecords(int inNumRecords) {
		recordCnt = inNumRecords;
	}

    /**
     * Set file encoding
     * @param encoding Encoding to set
     * @return True if the encoding can be set to DBF file
     */
    public boolean setEncoding(String encoding) {
        for(Map.Entry<Byte, String> entry : CODE_PAGE_ENCODING.entrySet()) {
            if(entry.getValue().equalsIgnoreCase(encoding)) {
                this.fileEncoding = entry.getValue();
                return true;
            }
        }
        return false;
    }

    private byte getEncodingByte() {
        for(Map.Entry<Byte, String> entry : CODE_PAGE_ENCODING.entrySet()) {
            if(entry.getValue().equalsIgnoreCase(fileEncoding)) {
                return entry.getKey();
            }
        }
        return DEFAULT_ENCODING_FLAG;
    }

	/**
	 * Write the header data to the DBF file.
	 *
	 * @param out
	 *            A channel to write to. If you have an OutputStream you can
	 *            obtain the correct channel by using
	 *            java.nio.Channels.newChannel(OutputStream out).
	 * @throws java.io.IOException
	 *             If errors occur.
	 */
	public void writeHeader(WritableByteChannel out) throws IOException {
		// take care of the annoying case where no records have been added...
		if (headerLength == -1) {
			headerLength = MINIMUM_HEADER;
		}
		ByteBuffer buffer = ByteBuffer.allocateDirect(headerLength);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		// write the output file type.
		buffer.put(MAGIC);

		// write the date stuff
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		buffer.put((byte) (c.get(Calendar.YEAR) % 100));
		buffer.put((byte) (c.get(Calendar.MONTH) + 1));
		buffer.put((byte) (c.get(Calendar.DAY_OF_MONTH)));

		// write the number of records in the datafile.
		buffer.putInt(recordCnt);

		// write the length of the header structure.
		buffer.putShort((short) headerLength);

		// write the length of a record
		buffer.putShort((short) recordLength);

		// // write the reserved bytes in the header
		buffer.position(buffer.position() + 17);
        // skip / skip the reserved bytes in the header.
        // write Language driver
        buffer.put(getEncodingByte());
        // skip reserved
        buffer.position(buffer.position() + 2);

		// write all of the header records
		int tempOffset = 0;
        for (DbaseField field : fields) {

            // write the field name
            byte[] fieldName = field.fieldName.getBytes(fileEncoding);
            for (int j = 0; j < 11; j++) {
                if (fieldName.length > j) {
                    buffer.put(fieldName[j]);
                } else {
                    buffer.put((byte) 0);
                }
            }

            // write the field type
            buffer.put((byte) field.fieldType);
            // // write the field data address, offset from the start of the
            // record.
            buffer.putInt(tempOffset);
            tempOffset += field.fieldLength;

            // write the length of the field.
            buffer.put((byte) field.fieldLength);

            // write the decimal count.
            buffer.put((byte) field.decimalCount);

            // write the reserved bytes.
            // for (in j=0; jj<14; j++) out.writeByteLE(0);
            buffer.position(buffer.position() + 14);
        }

		// write the end of the field definitions marker
		buffer.put((byte) 0x0D);

		buffer.position(0);

		int r = buffer.remaining();
                do {
                        r -= out.write(buffer);
                } while (r > 0);
	}

	/**
	 * Get a simple representation of this header.
	 *
	 * @return A String representing the state of the header.
	 */
        @Override
	public String toString() {
		StringBuilder fs = new StringBuilder();
		for (int i = 0, ii = fields.length; i < ii; i++) {
			DbaseField f = fields[i];
			fs.append(f.fieldName).append(" ").append(f.fieldType).append(" ").append(f.fieldLength).append(" ").append(f.decimalCount).append(" ").append(f.fieldDataAddress).append("\n");
		}

		return "DB3 Header\n" + "Date : " + date + "\n" + "Records : "
				+ recordCnt + "\n" + "Fields : " + fieldCnt + "\n" + fs;

	}

}
