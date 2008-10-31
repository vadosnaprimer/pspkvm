package javax.microedition.m3g;

import java.nio.*;

import javax.microedition.m3g.util.BufferUtil;

import javax.microedition.m3g.vecMath.*;

public class VertexBuffer extends Object3D {

	private int numVertices = 0;
	private VertexArray positions = null;
	private VertexArray normals = null;
	private VertexArray colors = null;
	private VertexArray[] texCoords = null;
	
	private float[] positionBias = null;
	private float positionScale = 1.0f;
	private float[][] texCoordsBias = null;
	private float[] texCoordsScale = {1,1,1};
	private int maxTextureUnitIndex = 0;
	private int defaultColor = 0xFFFFFFFF;
	
	public VertexBuffer()
	{
		int numTextureUnits = 8;
		maxTextureUnitIndex = numTextureUnits - 1;
		texCoords = new VertexArray[numTextureUnits];

		positionBias = new float[3];
		texCoordsBias = new float[numTextureUnits][3];
		texCoordsScale = new float[numTextureUnits];
	}
	
	public void setPositions(VertexArray positions, float scale, float[] bias)
	{
		if (positions != null)
		{
			if (positions.getComponentCount() != 3)
				throw new IllegalArgumentException("positions must have component count of 3");
			if ((positions.getVertexCount() != getVertexCount()) && (numVertices > 0))
				throw new IllegalArgumentException("number of vertices in positions does not match number of vertices in other arrays");
			if ((bias != null) && (bias.length < 3))
				throw new IllegalArgumentException("bias must be of length 3");
				
			this.numVertices = positions.getVertexCount();
			this.positions = positions;
			// save scale and bias, needed by setPositions
			this.positionScale = scale;

			if (bias == null)
				bias = new float[] {0,0,0};
			
			this.positionBias[0] = bias[0];
			this.positionBias[1] = bias[1];
			this.positionBias[2] = bias[2];
		}
		else
		{
			this.positions = positions;
			resetVertexCount();
		}
	}

	public void setTexCoords(int index, VertexArray texCoords, float scale, float[] bias)
	{
		if (index < 0 || index > maxTextureUnitIndex)
			throw new IndexOutOfBoundsException("index must be in [0," + maxTextureUnitIndex + "]");
		if (texCoords != null)
		{
			if ((texCoords.getComponentCount() != 2) && (texCoords.getComponentCount() != 3))
				throw new IllegalArgumentException("texcoord component count must be in [2,3]");
			if ((texCoords.getVertexCount() != getVertexCount()) && (numVertices > 0))
				throw new IllegalArgumentException("number of vertices in positions does not match number of vertices in other arrays");
			if ((bias != null) && (bias.length < texCoords.getComponentCount()))
				throw new IllegalArgumentException("bias length must match number of components");
				
			this.numVertices = texCoords.getVertexCount();
			this.texCoords[index] = texCoords; 
			// save scale and bias, needed by getTexCoords
			this.texCoordsScale[index] = scale;

			if (bias == null)
				bias = new float[] {0,0,0};
			this.texCoordsBias[index][0] = bias[0];
			this.texCoordsBias[index][1] = bias[1];
			this.texCoordsBias[index][2] = bias[2];
		}
		else
		{
			this.texCoords[index] = texCoords;
			resetVertexCount();
		}
	}
	
	public void setNormals(VertexArray normals)
	{
		if (normals != null)
		{
			if (normals.getComponentCount() != 3)
				throw new IllegalArgumentException("normals must have component count of 3");
			if ((normals.getVertexCount() != getVertexCount()) && (numVertices > 0))
				throw new IllegalArgumentException("number of vertices in normals does not match number of vertices in other arrays");
				
			numVertices = normals.getVertexCount();
			this.normals = normals;
		}
		else
		{
			this.normals = normals;
			resetVertexCount();
		}
	}
	
	public void setColors(VertexArray colors)
	{
		if (colors != null)
		{
			// todo: fill actual vertexbuffer with colors

			if (colors.getComponentType() != 1)
				throw new IllegalArgumentException("colors must of type byte");
			if ((colors.getComponentCount() != 3) && (colors.getComponentCount() != 4))
				throw new IllegalArgumentException("color component count must be in [3,4]");
			if ((colors.getVertexCount() != getVertexCount()) && (numVertices > 0))
				throw new IllegalArgumentException("number of vertices in colors does not match number of vertices in other arrays");
				
			numVertices = colors.getVertexCount();
			this.colors = colors; 
		}
		else
		{
			this.colors = colors;
			resetVertexCount();
		}
	}
	
	public VertexArray getPositions(float[] scaleBias)
	{
		if (this.positions != null && scaleBias != null)
		{
			if (scaleBias.length < 4)
				throw new IllegalArgumentException("scaleBias must be of length 4");
			scaleBias[0] = positionScale;
			scaleBias[1] = positionBias[0];
			scaleBias[2] = positionBias[1];
			scaleBias[3] = positionBias[2];
		}
		return this.positions;
	}

	public VertexArray getTexCoords(int index, float[] scaleBias)
	{
		if (index < 0 || index > maxTextureUnitIndex)
			throw new IndexOutOfBoundsException("index must be in [0," + maxTextureUnitIndex + "]");

		VertexArray texCoords = this.texCoords[index];
		if (texCoords != null && scaleBias != null)
		{
			if (scaleBias.length < (texCoords.getComponentCount() + 1))
				throw new IllegalArgumentException("scaleBias must be of length 4");
			scaleBias[0] = texCoordsScale[index];
			scaleBias[1] = texCoordsBias[index][0];
			scaleBias[2] = texCoordsBias[index][1];
			scaleBias[3] = texCoordsBias[index][2];
		}
		return texCoords;
	}
	
	public VertexArray getNormals()
	{
		return this.normals;
	}
	
	public VertexArray getColors()
	{
		return this.colors;
	}
	
	public int getVertexCount()
	{
		return this.numVertices;
	}
	
	public void setDefaultColor(int color)
	{
		this.defaultColor = color;
	}
	
	public int getDefaultColor()
	{
		return this.defaultColor;
	}
	
	private void resetVertexCount()
	{
		if (!isAnyArraySet())
			this.numVertices = 0;
	}
	
	private boolean isAnyArraySet()
	{
		boolean isTexCoordsSet = false;
		for (int i=0; i<=maxTextureUnitIndex; i++)
			isTexCoordsSet |= (texCoords[i] != null);
		
		return (positions != null) || (normals != null) || (colors != null) || isTexCoordsSet;
	}
	
	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		int parentCount = super.getReferences(references);
		
		if(positions != null)
		{
			if(references != null)
				references[parentCount] = positions;
			++parentCount;
		}
		
		if(normals != null)
		{
			if(references != null)
				references[parentCount] = normals;
			++parentCount;
		}
		
		if(colors != null)
		{
			if(references != null)
				references[parentCount] = colors;
			++parentCount;
		}
		
		for(int i = 0; i < texCoords.length; ++i)
		{
			if(texCoords[i] != null)
			{
				if(references != null)
					references[parentCount] = texCoords[i];
				++parentCount;
			}
		}
		
		return parentCount;
	}
}
