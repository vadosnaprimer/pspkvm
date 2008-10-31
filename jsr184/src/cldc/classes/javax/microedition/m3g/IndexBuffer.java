package javax.microedition.m3g;

import java.nio.*;

public abstract class IndexBuffer extends Object3D {
	protected IntBuffer buffer = null;

	public int getIndexCount()
	{
		return buffer.limit();
	}
	
	public abstract void getIndices(int[] indices);
	
	protected void allocate(int numElements)
	{
		buffer = BufferUtil.newIntBuffer(numElements);
	}
	
	IntBuffer getBuffer() {
		return buffer;
	}
}
