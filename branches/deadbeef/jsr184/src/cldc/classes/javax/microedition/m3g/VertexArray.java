package javax.microedition.m3g;

import javax.microedition.m3g.opengl.GL;
import java.nio.*; // BufferUtils
import javax.microedition.m3g.util.BufferUtil;

public class VertexArray extends Object3D {

	private int numVertices;
	private int numComponents;
	private int componentSize;
	private int numElements;

	private Buffer buffer;
	private FloatBuffer floatBuffer;
	
	public VertexArray(int numVertices, int numComponents, int componentSize)
	{
		if (numVertices < 1 || numVertices > 65535)
			throw new IllegalArgumentException("numVertices must be in [1,65535]");
		if (numComponents < 2 || numComponents > 4)
			throw new IllegalArgumentException("numComponents must be in [2,4]");
		if (componentSize < 1 || componentSize > 2)
			throw new IllegalArgumentException("componentSize must be in [1,2]");

		this.numVertices = numVertices;
		this.numComponents = numComponents;
		this.componentSize = componentSize;
		
		numElements = numVertices * numComponents;

		if (componentSize == 1)
			buffer = BufferUtil.newByteBuffer(numElements);
		else
			buffer = BufferUtil.newShortBuffer(numElements);

		floatBuffer = BufferUtil.newFloatBuffer(numElements);
	}
	
	public void set(int firstVertex, int numVertices, short[] values)
	{
		int numElements = numVertices * numComponents;
		checkShortInput(firstVertex, numVertices, numElements, values);
		
		ShortBuffer shortBuffer = (ShortBuffer)buffer;
		shortBuffer.position(firstVertex);
		shortBuffer.put(values, 0, numElements);

		floatBuffer.position(firstVertex);
		for (int i=0; i<numElements; i++)
			floatBuffer.put((float)values[i]);
		
	}

	public void set(int firstVertex, int numVertices, byte[] values)
	{
		int numElements = numVertices * numComponents;
		checkByteInput(firstVertex, numVertices, numElements, values);
		
		ByteBuffer byteBuffer = (ByteBuffer)buffer;
		byteBuffer.position(firstVertex);
		byteBuffer.put(values, 0, numElements);

		floatBuffer.position(firstVertex);
		for (int i=0; i<numElements; i++)
			floatBuffer.put((float)values[i]);
	}
	
	public int getVertexCount()
	{
		return this.numVertices;		
	}
	
	public int getComponentCount()
	{
		return this.numComponents; 
	}
	
	public int getComponentType()
	{
		return this.componentSize; 
	}
	
	public void get(int firstVertex, int numVertices, short[] values)
	{
		int numElements = numVertices * numComponents;
		checkShortInput(firstVertex, numVertices, numElements, values);
		
		ShortBuffer shortBuffer = (ShortBuffer)buffer;
		shortBuffer.position(firstVertex);
		shortBuffer.get(values, 0, numElements);
	}

	public void get(int firstVertex, int numVertices, byte[] values)
	{
		int numElements = numVertices * numComponents;
		checkByteInput(firstVertex, numVertices, numElements, values);

		ByteBuffer byteBuffer = (ByteBuffer)buffer;
		byteBuffer.position(firstVertex);
		byteBuffer.get(values, 0, numElements);
	}

	private void checkShortInput(int firstVertex, int numVertices, int numElements, short[] values)
	{
		if (values == null)
			throw new NullPointerException("values can not be null");
		if (componentSize != 2)
			throw new IllegalStateException("vertexarray created as short array. can not get byte values");
		checkInput(firstVertex, numVertices, numElements, values.length);
	}

	private void checkByteInput(int firstVertex, int numVertices, int numElements, byte[] values)
	{
		if (values == null)
			throw new NullPointerException("values can not be null");
		if (componentSize != 1)
			throw new IllegalStateException("vertexarray created as short array. can not set byte values");
		checkInput(firstVertex, numVertices, numElements, values.length);
	}
	
	private void checkInput(int firstVertex, int numVertices, int numElements, int arrayLength)
	{
		if (numVertices < 0)
			throw new IllegalArgumentException("numVertices must be > 0");
		if (arrayLength < numElements)
			throw new IllegalArgumentException("number of elements i values does not match numVertices");
		if (firstVertex < 0 || firstVertex + numVertices > this.numVertices)
			throw new IndexOutOfBoundsException("index out of bounds");
	}

	int getComponentTypeGL()
	{
		if(componentSize == 1)
			return GL.GL_BYTE;
		else
			return GL.GL_SHORT; 
	}
	
	Buffer getBuffer()
	{
		return buffer;
	}

	FloatBuffer getFloatBuffer()
	{
		return floatBuffer;
	}
}
