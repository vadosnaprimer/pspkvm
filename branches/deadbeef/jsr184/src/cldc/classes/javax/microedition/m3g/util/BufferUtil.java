package javax.microedition.m3g.util;

import java.nio.*;

public class BufferUtil {

    public static ByteBuffer newByteBuffer(int capacity){
        return ByteBuffer.allocateDirect(capacity);
    }

    public static IntBuffer newIntBuffer(int numelements) {
        int[] array = new int[numelements];
        return IntBuffer.wrap(array);
    }

    public static FloatBuffer newFloatBuffer(int capacity){
        float[] array = new float[capacity];
        return FloatBuffer.wrap(array);
    }

    public static ShortBuffer newShortBuffer(int capacity){
        short[] array = new short[capacity];
        return ShortBuffer.wrap(array);
    }

}
