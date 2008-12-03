package javax.microedition.m3g.vecMath;

public class Vector4 {

    public float x, y, z, w;

    public Vector4()
    {
	x = 0;
	y = 0;
	z = 0;
	w = 0;
    }

    public Vector4(float x, float y, float z, float w)
    {
	this.x = x;
	this.y = y;
	this.z = z;
	this.w = w;
    }

    public Vector4(Vector4 v)
    {
	x = v.x;
	y = v.y;
	z = v.z;
	w = v.w;
    }

    public Vector4(float[] v)
    {
	if (v == null)
	    throw new NullPointerException();
	if (v.length < 4)
	    throw new IllegalArgumentException("v must be of lenght 4 or grater");

	x = v[0];
	y = v[1];
	z = v[2];
	w = v[3];
    }

    public void set(float x, float y, float z, float w)
    {
	this.x = x;
	this.y = y;
	this.z = z;
	this.w = w;
    }

    public void set(Vector4 v)
    {
	x = v.x;
	y = v.y;
	z = v.z;
	w = v.w;
    }

    public void set(float[] v)
    {
	if (v == null)
	    throw new NullPointerException();
	if (v.length < 4)
	    throw new IllegalArgumentException("v must be of lenght 4 or greater");

	x = v[0];
	y = v[1];
	z = v[2];
	w = v[3];
    }


    public void add(Vector4 v)
    {
	x += v.x;
	y += v.y;
	z += v.z;
	w += v.w;
    }

    public void add(float scalar)
    {
	x += scalar;
	y += scalar;
	z += scalar;
	w += scalar;
    }

    public void add(Vector4 v, float scalar)
    {
	x = v.x + scalar;
	y = v.y + scalar;
	z = v.z + scalar;
	w = v.w + scalar;
    }

    public void add(Vector4 v1, Vector4 v2)
    {
	x = v1.x + v2.x;
	y = v1.y + v2.y;
	z = v1.z + v2.z;
	w = v1.w + v2.w;
    }

    public void subtract(Vector4 v)
    {
	x -= v.x;
	y -= v.y;
	z -= v.z;
	w -= v.w;
    }

    public void subtract(float scalar)
    {
	x -= scalar;
	y -= scalar;
	z -= scalar;
	w -= scalar;
    }

    public void subtract(Vector4 v,  float scalar)
    {
	x = v.x - scalar;
	y = v.y - scalar;
	z = v.z - scalar;
	w = v.w - scalar;
    }

    public void subtract(Vector4 v1, Vector4 v2)
    {
	x = v1.x - v2.x;
	y = v1.y - v2.y;
	z = v1.z - v2.z;
	w = v1.w - v2.w;
    }

    public void multiply(float scalar)
    {
	x *= scalar;
	y *= scalar;
	z *= scalar;
	w *= scalar;
    }

    public void multiply(Vector4 v, float scalar)
    {
	x = v.x * scalar;
	y = v.y * scalar;
	z = v.z * scalar;
	w = v.w * scalar;
    }

    public float length()
    {
	return (float)Math.sqrt(x*x + y*y + z*z + w*w);
    }

    public float length2()
    {
	return x*x + y*y + z*z + w*w;
    }

    public void normalize()
    {
	float length = length();
	if (length < Constants.EPSILON)
	    throw new ArithmeticException("Can't normalize zero lenght vector.");

	multiply(1.0f/length);
    }

    public float dot(Vector4 v)
    {
	return x*v.x + y*v.y + z*v.z + w*v.w;
    }

    public boolean equals(Vector4 v)
    {
	if (v == null)
	    throw new NullPointerException();
	return (x == v.x && y == v.y && z==v.z && w == v.w);
    }

    public float[] toArray()
    {
	float[] a = {x,y,z,w};
	return a;
    }

    public String toString()
    {
	return "(" + x + ", " + y + ", " + z + ", " + w+ ")";
    }
}

