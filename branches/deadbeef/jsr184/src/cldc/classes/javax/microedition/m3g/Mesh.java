package javax.microedition.m3g;

import java.util.Vector;

public class Mesh extends Node {
    private VertexBuffer vertices;
    private Vector submeshes = new Vector();
    private Vector appearances = new Vector();

    public Mesh(VertexBuffer vertices,
	    IndexBuffer submesh,
	    Appearance appearance)
    {
	this.vertices = vertices;
	this.submeshes.addElement(submesh);
	this.appearances.addElement(appearance);
    }

    public Mesh(VertexBuffer vertices,
	    IndexBuffer[] submeshes,
	    Appearance[] appearances)
    {
	this.vertices = vertices;
	for(int i=0; i < submeshes.length; ++i)
	    this.submeshes.addElement(submeshes[i]);
	for(int i=0; i < appearances.length; ++i)
	    this.appearances.addElement(appearances[i]);
    }

    public Appearance getAppearance(int index)
    {
	return (Appearance)appearances.elementAt(index);
    }

    public IndexBuffer getIndexBuffer(int index)
    {
	return (IndexBuffer)submeshes.elementAt(index);
    }

    public int getSubMeshCount()
    {
	return submeshes.size();
    }

    public VertexBuffer getVertexBuffer()
    {
	return vertices;
    }

    public void setAppearance(int index, Appearance appearance)
    {
	appearances.setElementAt(appearance, index);
    }

    public int getReferences(Object3D[] references) throws IllegalArgumentException 
    {
	int parentCount = super.getReferences(references);

	if(vertices != null)
	{
	    if(references != null)
		references[parentCount] = vertices;
	    ++parentCount;
	}

	for(int i = 0; i < submeshes.size(); ++i)
	{
	    if(references != null)
		references[parentCount] = (Object3D)submeshes.elementAt(i);
	    ++parentCount;
	}

	for(int i = 0; i < appearances.size(); ++i)
	{
	    if(references != null)
		references[parentCount] = (Object3D)appearances.elementAt(i);
	    ++parentCount;
	}

	return parentCount;
    }
}
