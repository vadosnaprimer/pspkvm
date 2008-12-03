package javax.microedition.m3g.util;

import java.nio.*;

public class BufferUtil {

    public static ByteBuffer newByteBuffer(int capacity){
	return ByteBuffer.allocateDirect(capacity);
    }

    public static IntBuffer newIntBuffer(int numelements) {
	return newByteBuffer(numelements * 4).asIntBuffer();
    }

    public static FloatBuffer newFloatBuffer(int capacity){
	return newByteBuffer(capacity * 4).asFloatBuffer();
    }


    public static ShortBuffer newShortBuffer(int capacity){
	return newByteBuffer(capacity *4).asShortBuffer();
    }

}
