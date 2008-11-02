package javax.microedition.m3g.util;

/**
 *
 * Use native ZLib functions to implement inflate method
 */
public class Inflater {

    private byte[] compressed_data;

    private int mOffset;

    private int mLength;


    private native void initNativeInflater();
    private native int inflate(byte[] b, int offset, int length, byte[] uncompressed);
    private native void destroyNativeInflater();

    public Inflater(){
       initNativeInflater(); 
    }

    public void setInput(byte[] b, int offset, int length){
        compressed_data = b;
	mOffset = offset;
	mLength = length;
    }

    public int inflate(byte[] uncompressed){
        return inflate(compressed_data, mOffset, mLength, uncompressed);
    }

    public void end(){
        destroyNativeInflater();
    }

}
