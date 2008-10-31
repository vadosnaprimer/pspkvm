package javax.microedition.m3g.vecMath;

public class Vector3 {

	public float x, y, z;
		
	public Vector3()
	{
		x = 0;
		y = 0;
		z = 0;
	}

	public Vector3(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3(Vector3 v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	public Vector3(float[] v)
	{
		if (v == null)
			throw new NullPointerException();
		if (v.length < 3)
			throw new IllegalArgumentException("v must be of lenght 3 or larger");

		x = v[0];
		y = v[1];
		z = v[2];
	}

	public void set(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vector3 v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public void set(float[] v)
	{
		if (v == null)
			throw new NullPointerException();
		if (v.length < 3)
			throw new IllegalArgumentException("v must be of lenght 3 or larger");

		x = v[0];
		y = v[1];
		z = v[2];
	}

	
	public void add(Vector3 v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
	}
	
	public void add(float scalar)
	{
		x += scalar;
		y += scalar;
		z += scalar;
	}
	
	public void add(Vector3 v, float scalar)
	{
		x = v.x + scalar;
		y = v.y + scalar;
		z = v.z + scalar;
	}
	
	public void add(Vector3 v1, Vector3 v2)
	{
		x = v1.x + v2.x;
		y = v1.y + v2.y;
		z = v1.z + v2.z;
	}

	public void subtract(Vector3 v)
	{
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}

	public void subtract(float scalar)
	{
		x -= scalar;
		y -= scalar;
		z -= scalar;
	}

	public void subtract(Vector3 v,  float scalar)
	{
		x = v.x - scalar;
		y = v.y - scalar;
		z = v.z - scalar;
	}

	public void subtract(Vector3 v1, Vector3 v2)
	{
		x = v1.x - v2.x;
		y = v1.y - v2.y;
		z = v1.z - v2.z;
	}
	
	public void multiply(float scalar)
	{
		x *= scalar;
		y *= scalar;
		z *= scalar;
	}

	public void multiply(Vector3 v, float scalar)
	{
		x = v.x * scalar;
		y = v.y * scalar;
		z = v.z * scalar;
	}
	
	public float length()
	{
		return (float)Math.sqrt(x*x + y*y + z*z);
	}
	
	public float length2()
	{
		return x*x + y*y + z*z;
	}

	public void normalize()
	{
		float length = length();
		if (length < Constants.EPSILON)
			throw new ArithmeticException("Can't normalize zero lenght vector.");
		
		multiply(1.0f/length);
	}
	
	public float dot(Vector3 v)
	{
		return x*v.x + y*v.y + z*v.z;
	}

	public void cross(Vector3 v)
	{
		float x, y;
		
		x = this.y*v.z - this.z*v.y;
		y = this.z*v.z - this.z*v.z;
		
		this.z = this.x*v.y - this.y*v.x;
		this.x = x;
		this.y = y;
	}

	public void cross(Vector3 v1, Vector3 v2)
	{
		this.z = v1.x*v2.y - v1.y*v2.x;
		this.x = v1.y*v2.z - v1.z*v2.y;
		this.y = v1.z*v2.z - v1.z*v2.z;
	}

	// rotate this vector angle radias around axis
	public void rotate(float angle, Vector3 axis)
	{
		Vector3 c = new Vector3();
		Vector3 a = new Vector3(axis);
		a.normalize();
		c.cross(this, a);
		
		Vector3 result = getRejection(axis);
		result.multiply((float)Math.cos(angle));

		c.multiply((float)Math.sin(angle));
		result.add(c);
		result.add(getProjection(axis));
		
		this.set(result); 
	}
	
	// get a vector perpendicular to this
	public Vector3 getPerpendicular()
	{
		Vector3 v = new Vector3();
		v.cross(this, new Vector3(1,0,0));
		if (v.length()<Constants.EPSILON)
			v.cross(this, new Vector3(0,1,0));
		return v;
	}
	
	// gets the projection of this vector on v
	public Vector3 getProjection(Vector3 v)
	{
		Vector3 e = new Vector3(v);
		e.normalize();
		e.multiply(this.dot(e));
		return e;
	}

	// gets the rejection of this vector on v
	public Vector3 getRejection(Vector3 v)
	{
		Vector3 u = new Vector3(this);
		u.subtract(getProjection(v));
		return u;
	}
	
	// gets the reflection of this vector around v
	public Vector3 getReflection(Vector3 v)
	{
		Vector3 u = new Vector3(this);
		Vector3 w = getRejection(v);
		w.multiply(2);
		u.subtract(w);
		return u;
	}
	
	public boolean equals(Vector3 v)
	{
		if (v == null)
			throw new NullPointerException();
		return (x == v.x && y == v.y && z==v.z);
	}
	
	public float[] toArray()
	{
		float[] a = {x,y,z};
		return a;
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ")";
	}
}

