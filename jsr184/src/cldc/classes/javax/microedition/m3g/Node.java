package javax.microedition.m3g;

public abstract class Node extends Transformable {

    public static final int NONE	= 144;
    public static final int ORIGIN 	= 145;
    public static final int X_AXIS 	= 146;
    public static final int Y_AXIS	= 147;
    public static final int Z_AXIS 	= 148;

    protected float alpha 				= 1.0f;
    protected Node parent 				= null;
    protected int scope 				= -1;
    protected boolean pickingEnabled 	= true;
    protected boolean renderingEnabled 	= true;

    public final void align(Node reference)
    {
	// todo
    }

    public float getAlphaFactor()
    {
	return alpha;
    }

    public Node getParent()
    {
	return parent;
    }

    public int getScope()
    {
	return scope;
    }

    public boolean getTransformTo(Node target, Transform transform)
    {
	if (target == null)
	    throw new NullPointerException("target can not be null");
	if (transform == null)
	    throw new NullPointerException("transform can not be null");

	Node node = this;

	System.out.println("Node - getTransformTo "+target+" from "+this);
	int nodeDepth = node.getDepth();
	int targetDepth = target.getDepth();

	Transform tmp = new Transform();
	Transform targetTransform = new Transform();
	Transform nodeTransform = new Transform();

	// Iterate up in the tree until the paths merge
	while(node != target) {
	    int nd = nodeDepth;
	    if(nodeDepth >= targetDepth) {
		node.getCompositeTransform(tmp);
		nodeTransform.postMultiply(tmp);

		node = node.getParent();
		--nodeDepth;
	    }
	    if(targetDepth >= nd) {
		target.getCompositeTransform(tmp);
		tmp.postMultiply(targetTransform);
		targetTransform.set(tmp);

		target = target.getParent();
		--targetDepth;
	    }
	}
	// did we find a path? (if one is null, actually both should be null)
	if(node == null || target == null)
	    return false;

	//nodeTransform.invert();
	transform.set(nodeTransform);
	transform.postMultiply(targetTransform);
	return true;
    }

    public boolean isPickingEnabled()
    {
	return pickingEnabled;
    }

    public boolean isRenderingEnabled()
    {
	return renderingEnabled;
    }

    public void setAlignment(Node zRef, int zTarget, Node yRef, int yTarget)
    {
	if (zTarget < 0 || zTarget > 4 || yTarget < 0 || yTarget > 4)
	    throw new IllegalArgumentException();
	if ((zRef == yRef) && (zTarget != NONE || yTarget != NONE))
	    throw new IllegalArgumentException();
	if (zRef == this || yRef == this)
	    throw new IllegalArgumentException("can not use this as refnode");
	// todo
    }

    public void setAlphaFactor(float alphaFactor)
    {
	if (alphaFactor < 0 || alphaFactor > 1)
	    throw new IllegalArgumentException("alphaFactor must be in [0,1]");
	alpha = alphaFactor;
    }

    public void setPickingEnable(boolean enable)
    {
	pickingEnabled = enable;		
    }

    public void setRenderingEnable(boolean enable)
    {
	renderingEnabled = enable;
    }

    public void setScope(int scope)
    {
	this.scope = scope;
    }

    private int getDepth()
    {
	int depth = 0;
	Node node = this;
	while(node != null) {
	    ++depth;
	    node = node.getParent();
	}
	return depth;
    }
}
