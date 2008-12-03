package javax.microedition.m3g;

import javax.microedition.m3g.util.BufferUtil;
import java.nio.*;

public abstract class IndexBuffer extends Object3D {
    protected ByteBuffer buffer = null;

    public int getIndexCount()
    {
	return buffer.limit();
    }

    public abstract void getIndices(int[] indices);

    protected void allocate(int numElements)
    {
	buffer = BufferUtil.newByteBuffer(numElements);
    }

    ByteBuffer getBuffer() {
	return buffer;
    }
}
