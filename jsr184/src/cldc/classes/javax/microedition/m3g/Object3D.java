package javax.microedition.m3g;

//import java.util.ArrayList; 

public abstract class Object3D {

	protected int userID = 0;
	protected Object userObject = null;
	//ArrayList animationTracks = new ArrayList();
	
	public final Object3D duplicate() 
	{
		Object3D copy = null;
		try {
			copy = (Object3D)this.clone();
		}
		catch (Exception e) {
		}
		return copy;
	}

	public Object3D find(int userID)
	{
		// TODO: 
		return null;
	}

	public int getReferences(Object3D[] references) throws IllegalArgumentException 
	{
		return 0;
	}

	public int getUserID()
	{
		return userID;
	}
	
	public void setUserID(int userID)
	{
		this.userID = userID;
	}
	
	public Object getUserObject()
	{
		return this.userObject;
	}
	
	public void setUserObject(Object userObject)
	{
		this.userObject = userObject;
	}
	
	/*
	public void addAnimationTrack(AnimationTrack animationTrack)
	{
		this.animationTracks.add(animationTrack);
	}
	*/
}
