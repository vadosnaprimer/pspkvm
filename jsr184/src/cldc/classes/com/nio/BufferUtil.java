package com.nio;

public class BufferUtil {

    public static ByteBuffer newByteBuffer(int capacity){
        return ByteBuffer.allocateDirect(capacity);
    }

    public static IntBuffer newIntBuffer(int numelements) {
        return IntBuffer.allocate(numelements);
    }

    public static FloatBuffer newFloatBuffer(int capacity){
        return FloatBuffer.allocate(capacity);
    }

    public static ShortBuffer newShortBuffer(int capacity){
        return ShortBuffer.allocate(capacity);
    }

}
