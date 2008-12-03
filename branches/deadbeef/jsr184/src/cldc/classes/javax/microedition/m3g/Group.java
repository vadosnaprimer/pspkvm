package javax.microedition.m3g;

import java.util.Vector; 

public class Group extends Node {

    protected Vector  children; 

    public Group()
    {
	children = new Vector();
    }

    public void addChild(Node child)
    {
	if (child == null)
	    throw new NullPointerException("child can not be null");
	if (child == this)
	    throw new IllegalArgumentException("can not add self as child");
	//		if (child instanceof World)
	//			throw new IllegalArgumentException("node of type World can not be child");
	if (child.parent != null)
	    throw new IllegalArgumentException("child already has a parent");

	// todo
	children.addElement(child);
	child.parent = this;
    }

    public Node getChild(int index)
    {
	return (Node)children.elementAt(index);
    }

    public int getChildCount()
    {
	return children.size();
    }

    public int getReferences(Object3D[] references) throws IllegalArgumentException 
    {
	int parentCount = super.getReferences(references);
	if(references != null)
	    for(int i = 0; i < children.size(); ++i)
		references[parentCount+i] = (Object3D)children.elementAt(i);
	return parentCount+children.size();
    }

    /*
       public boolean pick(int scope, float x, float y, Camera camera, RayIntersection ri)
       {
       }

       public boolean pick(int scope, float ox, float oy, float oz, float dx, float dy, float dz, RayIntersection ri)
       {
       }
       */

    public void removeChild(Node child)
    {
	children.removeElement(child);
	child.parent = null;
    }
}
