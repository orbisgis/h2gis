package org.h2gis.graalvm;

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * An OutputStream that writes directly into a ByteBuffer.
 *
 * If the buffer's capacity is exceeded, it automatically grows to accommodate more data.
 * This is useful for writing binary data into a dynamically resizable buffer,
 * for example in native interop or serialization scenarios.
 */
public class ByteBufferBackedOutputStream extends OutputStream {

    /** The underlying ByteBuffer that stores written data. */
    private ByteBuffer buffer;

    /**
     * Constructs a new ByteBufferBackedOutputStream with the given buffer.
     *
     * @param buffer The initial ByteBuffer to write to.
     */
    public ByteBufferBackedOutputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Writes a single byte to the buffer.
     * Automatically expands the buffer if there is not enough space.
     *
     * @param b The byte to write.
     */
    @Override
    public void write(int b) {
        if (buffer.remaining() < 1) {
            buffer = growBuffer(buffer, 1);
        }
        buffer.put((byte) b);
    }

    /**
     * Writes a portion of a byte array to the buffer.
     * Automatically expands the buffer if necessary.
     *
     * @param bytes The source byte array.
     * @param off   The offset within the array to start reading from.
     * @param len   The number of bytes to write.
     */
    @Override
    public void write(byte[] bytes, int off, int len) {
        if (buffer.remaining() < len) {
            buffer = growBuffer(buffer, len);
        }
        buffer.put(bytes, off, len);
    }

    /**
     * Returns the current ByteBuffer.
     * The buffer may have been replaced if it was grown.
     *
     * @return The current ByteBuffer in use.
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Expands the buffer to fit the required number of additional bytes.
     * The new capacity is either double the old capacity, or large enough to fit the required data.
     *
     * @param buffer The original ByteBuffer.
     * @param additionalBytes The number of extra bytes needed.
     * @return A new ByteBuffer with the existing data copied over.
     */
    private ByteBuffer growBuffer(ByteBuffer buffer, int additionalBytes) {
        int newCapacity = Math.max(buffer.capacity() * 2, buffer.position() + additionalBytes);
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        buffer.flip(); // Prepare old buffer for reading
        newBuffer.put(buffer); // Copy data to new buffer
        this.buffer = newBuffer;
        return newBuffer;
    }
}
