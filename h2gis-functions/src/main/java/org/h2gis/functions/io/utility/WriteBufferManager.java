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

package org.h2gis.functions.io.utility;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * Class to write files using nio.
 *
 * @author Fernando Gonzalez Cortes
 */
public final class WriteBufferManager {

	private static final int BUFFER_SIZE = 1024 * 128;

	private FileChannel channel;

	private ByteBuffer buffer;

	/**
	 * Creates a new WriteBufferManager that writes to the specified file
	 * channel
	 *
	 * @param channel {@link FileChannel}
	 */
	public WriteBufferManager(FileChannel channel) throws IOException {
		this.channel = channel;
		buffer = ByteBuffer.allocate(BUFFER_SIZE);
	}

	/**
	 * Puts the specified byte at the current position
	 *
	 * @param b put a short value
	 */
	public void putShort(byte b) throws IOException {
		prepareToAddBytes(1);
		buffer.put(b);
	}

	public int position(){
		return buffer.position();
	}

	/**
	 * Moves the window
	 *
	 * @param numBytes prepare the buffer window
	 */
	private void prepareToAddBytes(int numBytes) throws IOException {
		if (buffer.remaining() < numBytes) {
			buffer.flip();
			channel.write(buffer);

			int bufferCapacity = Math.max(BUFFER_SIZE, numBytes);
			if (bufferCapacity != buffer.capacity()) {
				ByteOrder order = buffer.order();
				buffer = ByteBuffer.allocate(bufferCapacity);
				buffer.order(order);
			} else {
				buffer.clear();
			}
		}
	}

	/**
	 * Puts the specified bytes at the current position
	 *
	 * @param bs put short value
	 */
	public void putShort(byte[] bs) throws IOException {
		prepareToAddBytes(bs.length);
		buffer.put(bs);
	}

	/**
	 * flushes the cached contents into the channel. It is mandatory to call
	 * this method to finish the writing of the channel
	 *
	 */
	public void flush() throws IOException {
		buffer.flip();
		channel.write(buffer);
	}

	/**
	 * Specifies the byte order. One of the constants in {@link java.nio.ByteBuffer}
	 *
	 * @param order
	 */
	public void order(ByteOrder order) {
		this.buffer.order(order);
	}

	/**
	 * Puts the specified int at the current position
	 *
	 * @param value put int value
	 */
	public void putInt(int value) throws IOException {
		prepareToAddBytes(4);
		buffer.putInt(value);
	}

	/**
	 * Puts the specified double at the current position
	 *
	 * @param d put double value
	 */
	public void putDouble(double d) throws IOException {
		prepareToAddBytes(8);
		buffer.putDouble(d);
	}

	public void flip() {
		buffer.flip();
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public void putShort(short i) throws IOException {
		 prepareToAddBytes(2);
		 buffer.putShort(i);
	}

	public void putFloat(float value) throws IOException {
		prepareToAddBytes(4);
		buffer.putFloat(value);
	}

	public void putLong(long value) throws IOException {
		prepareToAddBytes(8);
		buffer.putLong(value);
	}

	public void putBytes(byte[] byteArray) throws IOException {
		prepareToAddBytes(byteArray.length);
		buffer.put(byteArray);
	}

	public void putBytes(byte[] byteArray, int offset, int length) throws IOException {
		prepareToAddBytes(length);
		buffer.put(byteArray, offset, length);
	}

	public void clear() {
		buffer.clear();
	}
}
