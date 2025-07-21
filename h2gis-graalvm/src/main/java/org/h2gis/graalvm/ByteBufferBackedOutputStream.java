package org.h2gis.graalvm;

import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferBackedOutputStream extends OutputStream {
    private ByteBuffer buffer;

    public ByteBufferBackedOutputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int b) {
        if (buffer.remaining() < 1) {
            buffer = growBuffer(buffer, 1);
        }
        buffer.put((byte) b);
    }

    @Override
    public void write(byte[] bytes, int off, int len) {
        if (buffer.remaining() < len) {
            buffer = growBuffer(buffer, len);
        }
        buffer.put(bytes, off, len);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    private ByteBuffer growBuffer(ByteBuffer buffer, int additionalBytes) {
        int newCapacity = Math.max(buffer.capacity() * 2, buffer.position() + additionalBytes);
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        this.buffer = newBuffer;
        return newBuffer;
    }
}

