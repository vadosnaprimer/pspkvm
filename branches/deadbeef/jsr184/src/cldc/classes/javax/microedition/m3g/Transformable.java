package javax.microedition.m3g;

import javax.microedition.m3g.vecMath.Vector3;

public abstract class Transformable extends Object3D {
    //private boolean hasComponentTransform;
    private Vector3 translation = new Vector3();
    private Vector3 scale = new Vector3(1.f,1.f,1.f);
    private Transform orientation = new Transform(); 
    private Transform transform = new Transform();

    /** Square root from 3 */
    final static public double SQRT3 = 1.732050807568877294;

    //Copy from henson's J2ME Float11 class
    private double acos(double x)
    {
	double f=asin(x);
	if(f==Double.NaN)
	    return f;
	return Math.PI/2-f;
    }

    private double asin(double x)
    {
	if( x<-1. || x>1. ) return Double.NaN;
	if( x==-1. ) return -Math.PI/2;
	if( x==1 ) return Math.PI/2;
	return atan(x/Math.sqrt(1-x*x));
    }

    private  double atan(double x)
    { 
	boolean signChange=false;
	boolean Invert=false;
	int sp=0;
	double x2, a;
	// check up the sign change
	if(x<0.)
	{
	    x=-x;
	    signChange=true;
	}
	// check up the invertation
	if(x>1.)
	{
	    x=1/x;
	    Invert=true;
	}
	// process shrinking the domain until x<PI/12
	while(x>Math.PI/12)
	{
	    sp++;
	    a=x+SQRT3;
	    a=1/a;
	    x=x*SQRT3;
	    x=x-1;
	    x=x*a;
	}
	// calculation core
	x2=x*x;
	a=x2+1.4087812;
	a=0.55913709/a;
	a=a+0.60310579;
	a=a-(x2*0.05160454);
	a=a*x;
	// process until sp=0
	while(sp>0)
	{
	    a=a+Math.PI/6;
	    sp--;
	}
	// invertation took place
	if(Invert) a=Math.PI/2-a;
	// sign change took place
	if(signChange) a=-a;

	return a;
    }



    public void getCompositeTransform(Transform transform)
    {
	if (transform == null)
	    throw new NullPointerException("transform can not be null");

	// transform = T R S M

	// Combine translation and rotation (TR)
	float[] m = new float[16];
	orientation.get(m);
	m[3] = translation.x;
	m[7] = translation.y;
	m[11] = translation.z;
	//System.out.println("Tranformable - getCompositeTransform - translation :"+m[3]+"/"+m[7]+"/"+m[11]);
	transform.set(m);

	// Apply scale (S)
	//System.out.println("Transformable - getCompositeTransform - scale :"+scale.x+"/"+scale.y+"/"+scale.z);
	transform.postScale(scale.x, scale.y, scale.z);

	// Apply custom (M)
	transform.postMultiply(this.transform);
    }

    public void getOrientation(float[] angleAxis)
    {
	if (angleAxis == null)
	    throw new NullPointerException("angleAxis can not be null");
	if (angleAxis.length < 4)
	    throw new IllegalArgumentException("length must be greater than 3");

	float[] m = new float[16];
	orientation.get(m);

	Vector3 axis = new Vector3(m[6]-m[9], m[8]-m[2], m[1]-m[4]);
	axis.normalize();

	float angle = (float)acos(0.5*(m[0] + m[5] + m[10] - 1)); 

	angleAxis[0] = angle;
	angleAxis[1] = axis.x;
	angleAxis[2] = axis.y;
	angleAxis[3] = axis.z;

	// TODO: Handle singularities for angle = 0 and angle = 180 degrees
    }

    public void getScale(float[] xyz)
    {
	if (xyz == null)
	    throw new NullPointerException("xyz can not be null");
	if (xyz.length < 3)
	    throw new IllegalArgumentException("length must be greater than 2");

	xyz[0] = scale.x;
	xyz[1] = scale.y;
	xyz[2] = scale.z;
    }

    public void getTransform(Transform transform)
    {
	if (transform == null)
	    throw new NullPointerException("transform can not be null");

	transform.set(this.transform);
    }

    public void getTranslation(float[] xyz)
    {
	if (xyz == null)
	    throw new NullPointerException("xyz can not be null");
	if (xyz.length < 3)
	    throw new IllegalArgumentException("length must be greater than 2");

	xyz[0] = translation.x;
	xyz[1] = translation.y;
	xyz[2] = translation.z;
    }

    public void postRotate(float angle, float ax, float ay, float az)
    {
	orientation.postRotate(angle, ax, ay, az);
    }

    public void preRotate(float angle, float ax, float ay, float az)
    {
	Transform t = new Transform();
	t.postRotate(angle, ax, ay, az);
	t.postMultiply(orientation);
	orientation.set(t);
    }

    public void scale(float sx, float sy, float sz)
    {
	scale.x *= sx;
	scale.y *= sy;
	scale.z *= sz;
    }

    public void setOrientation(float angle, float ax, float ay, float az)
    {
	orientation.setIdentity();
	orientation.postRotate(angle, ax, ay, az);
    }

    public void setScale(float sx, float sy, float sz)
    {
	scale.set(sx, sy, sz);
    }

    public void setTransform(Transform transform)
    {
	if (transform == null)
	    throw new NullPointerException("transform can not be null");

	this.transform.set(transform);
    }

    public void setTranslation(float tx, float ty, float tz)
    {
	translation.set(tx, ty, tz);
    }

    public void translate(float tx, float ty, float tz)
    {
	translation.x += tx;
	translation.y += ty;
	translation.z += tz;
    }
}
