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

public final class ReadBufferManager {

        private int bufferSize;
        private ByteBuffer buffer;
        private FileChannel channel;
        private long windowStart;
        private long positionInFile;

        /**
         * Instantiates a ReadBufferManager to read the specified channel
         *
         * @param channel {@link FileChannel}
         */
        public ReadBufferManager(FileChannel channel) throws IOException {
                this(channel, 1024 * 32);
        }

        /**
         * Instantiates a ReadBufferManager to read the specified channel. The
         * specified bufferSize is the size of the channel content cached in memory
         *
         * @param channel {@link FileChannel}
         * @param bufferSize buffer size
         */
        public ReadBufferManager(FileChannel channel, int bufferSize)
                throws IOException {
                this.channel = channel;
                buffer = ByteBuffer.allocate(0);
                windowStart = 0;
                this.bufferSize = bufferSize;
                getWindowOffset(0, bufferSize);
        }

        /**
         * Moves the window if necessary to contain the desired byte and returns the
         * position of the byte in the window
         *
         * @param bytePos byte position
         * @param length byte length
         */
        private int getWindowOffset(long bytePos, int length) throws IOException {
                long desiredMax = bytePos + length - 1;
                if ((bytePos >= windowStart)
                        && (desiredMax < windowStart + buffer.capacity())) {
                        long res = bytePos - windowStart;
                        if (res < Integer.MAX_VALUE) {
                                return (int) res;
                        } else {
                                throw new IOException("This buffer is quite large...");
                        }
                } else {
                        long bufferCapacity = Math.max(bufferSize, length);
                        long size = channel.size();

                        bufferCapacity = Math.min(bufferCapacity, size - bytePos);
                        if (bufferCapacity > Integer.MAX_VALUE) {
                                throw new IOException("Woaw ! You want to have a REALLY LARGE buffer !");
                        }
                        windowStart = bytePos;

                        channel.position(windowStart);
                        if (buffer.capacity() != bufferCapacity) {
                                ByteOrder order = buffer.order();
                                buffer = ByteBuffer.allocate((int)bufferCapacity);
                                buffer.order(order);
                        } else {
                                buffer.clear();
                        }
                        channel.read(buffer);
                        buffer.flip();
                        return (int) (bytePos - windowStart);
                }
        }

        /**
         * Gets the byte value at the specified position
         *
         * @param bytePos byte position
         * @return byte value
         */
        public byte getByte(long bytePos) throws IOException {
                int windowOffset = getWindowOffset(bytePos, 1);
                return buffer.get(windowOffset);
        }

        /**
         * Gets the size of the channel
         *
         * @return channel size
         */
        public long getLength() throws IOException {
                return channel.size();
        }

        /**
         * Specifies the byte order. One of the constants in {@link java.nio.ByteBuffer}
         *
         * @param order {@link ByteOrder}
         */
        public void order(ByteOrder order) {
                buffer.order(order);
        }

        /**
         * Gets the int value at the specified position
         *
         * @param bytePos byte position
         * @return int value
         */
        public int getInt(long bytePos) throws IOException {
                int windowOffset = getWindowOffset(bytePos, 4);
                return buffer.getInt(windowOffset);
        }

        /**
         * Gets the long value at the specified position
         *
         * @param bytePos byte position
         * @return long value
         */
        public long getLong(long bytePos) throws IOException {
                int windowOffset = getWindowOffset(bytePos, 8);
                return buffer.getLong(windowOffset);
        }

        /**
         * Gets the long value at the current position
         *
         * @return long value
         */
        public long getLong() throws IOException {
                long ret = getLong(positionInFile);
                positionInFile += 8;
                return ret;
        }

        /**
         * Gets the byte value at the current position
         *
         * @return byte value
         */
        public byte get() throws IOException {
                byte ret = getByte(positionInFile);
                positionInFile += 1;
                return ret;
        }

        /**
         * Gets the int value at the current position
         *
         * @return int value
         */
        public int getInt() throws IOException {
                int ret = getInt(positionInFile);
                positionInFile += 4;
                return ret;
        }

        /**
         * skips the specified number of bytes from the current position in the
         * channel
         *
         * @param numBytes numBytes to move the buffer to a new position
         */
        public void skip(int numBytes) throws IOException {
                positionInFile += numBytes;
        }

        /**
         * Gets the byte[] value at the current position
         *
         * @param buffer byte array
         * @return ByteBuffer
         */
        public ByteBuffer get(byte[] buffer) throws IOException {
                int windowOffset = getWindowOffset(positionInFile, buffer.length);
                this.buffer.position(windowOffset);
                positionInFile += buffer.length;
                return this.buffer.get(buffer);
        }

        /**
         * Gets the byte[] value at the specified position
         *
         * @param pos buffer position
         * @param buffer bytes
         * @return ByteBuffer
         */
        public ByteBuffer get(long pos, byte[] buffer) throws IOException {
                int windowOffset = getWindowOffset(pos, buffer.length);
                this.buffer.position(windowOffset);
                return this.buffer.get(buffer);
        }

        /**
         * Moves the current position to the specified one
         *
         * @param position buffer position
         */
        public void position(long position) {
                this.positionInFile = position;
        }

        /**
         * Gets the position of this buffer in the file it's reading.
         *
         * @return index of the position
         */
        public long getPosition() {
                return positionInFile;
        }

        /**
         * Gets the double value at the specified position
         *
         * @return double value
         */
        public double getDouble() throws IOException {
                double ret = getDouble(positionInFile);
                positionInFile += 8;
                return ret;
        }

        /**
         * Gets the double value at the specified position
         *
         * @param bytePos buffer position
         * @return double value
         */
        public double getDouble(long bytePos) throws IOException {
                int windowOffset = getWindowOffset(bytePos, 8);
                return buffer.getDouble(windowOffset);
        }

        /**
         * If the current position is at the end of the channel
         *
         * @return true if the current position is at the end of the channel
         */
        public boolean isEOF() throws IOException {
                return (buffer.remaining() == 0)
                        && (windowStart + buffer.capacity() >= channel.size());
        }

        /**
         * Gets the number of remaining bytes in the current file, starting from the
         * current position
         *
         * @return a number of bytes &gt;=0
         */
        public long remaining() throws IOException {
                return channel.size() - windowStart - buffer.position();
        }
}
