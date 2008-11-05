package javax.microedition.m3g.util;

import java.nio.*;

public class BufferUtil {

    public static ByteBuffer newByteBuffer(int capacity){
        return ByteBuffer.allocateDirect(capacity);
    }

    public static IntBuffer newIntBuffer(int numelements) {
        ByteBuffer bb = ByteBuffer.allocateDirect(numelements*4);
        return bb.asIntBuffer();
    }

    public static FloatBuffer newFloatBuffer(int capacity){
        ByteBuffer bb = ByteBuffer.allocateDirect(capacity*4);
        return bb.asFloatBuffer();
    }

    public static ShortBuffer newShortBuffer(int capacity){
        ByteBuffer bb = ByteBuffer.allocateDirect(capacity*2);
        return bb.asShortBuffer();
    }

}
