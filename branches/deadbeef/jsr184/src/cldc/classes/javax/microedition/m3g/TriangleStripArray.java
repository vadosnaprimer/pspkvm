package javax.microedition.m3g;

public class TriangleStripArray extends IndexBuffer {

	public TriangleStripArray(int firstIndex, int[] stripLengths)
	{
		int sum = checkInput(stripLengths);
		if (firstIndex < 0 || sum > 65535)
			throw new IndexOutOfBoundsException("firstIndex must be in [0, sum(stripLengths)]");

		// fill indexbuffer
		allocate(sum + (stripLengths.length-1)*3);
		short index = (short)firstIndex;
		for(int i=0; i<stripLengths.length; i++)
		{
			if(i != 0)
			{
				// if this is not the first strip,
				// we need to connect the strips
				buffer.put((short)(index-1));
				buffer.put(index);
				if((buffer.position() % 2)==1) // may need extra index for correct winding
					buffer.put(index);
			}
			for(int s=0; s<stripLengths[i]; ++s)
			{
				buffer.put(index++);
			}
		}
		// reset position and set limit
		buffer.flip();
	}
	
	public TriangleStripArray(int[] indices, int[] stripLengths)
	{
		if (indices == null)
			throw new NullPointerException("indices can not be null");

		for (int i=0; i<indices.length; i++)
		{
			if (indices[i] < 0 || indices[i] > 65535)
				throw new IndexOutOfBoundsException("all elements in indices must be in [0,65535]");
		}

		int sum = checkInput(stripLengths);
		if (indices.length < sum)
			throw new IllegalArgumentException("length of indices must be greater or equal to sum(stripLengths)]");
		
		// fill index buffer
		allocate(sum + (stripLengths.length-1)*3);
		int index = 0;
		for(int i=0; i<stripLengths.length; i++)
		{
			if(i != 0)
			{
				// if this is not the first strip,
				// we need to connect the strips
				buffer.put((short)indices[index-1]);
				buffer.put((short)indices[index]);
				if((buffer.position() % 2)==1) // may need extra index for correct winding
					buffer.put((short)indices[index]);
			}
			for(int s=0; s<stripLengths[i]; ++s)
			{
				buffer.put((short)indices[index++]);
			}
		}
		// reset position and set limit
		buffer.flip();
	}
	
	public void getIndices(int[] indices)
	{
		if (indices != null)
			throw new NullPointerException("indices can not be null");
		if (indices.length < getIndexCount())
			throw new IllegalArgumentException("length of indices array must be " + getIndexCount());
		// TODO: fill indices with triangle-data
	}
	
	private int checkInput(int[] stripLengths)
	{
		int sum = 0;
		if (stripLengths == null)
			throw new NullPointerException("stripLegths can not be null");
		int l = stripLengths.length;
		if (l == 0) 
			throw new IllegalArgumentException("stripLenghts can not be empty");
		for (int i=0; i<l; i++)
		{
			if (stripLengths[i] < 3)
				throw new IllegalArgumentException("stripLengths must not contain elemets less than 3");
			
			sum += stripLengths[i];
		}
		return sum;
	}

}
