package javax.microedtion.m3g.util;

public class Color {

	public float a = 0.0f;
	public float r = 0.0f;
	public float g = 0.0f;
	public float b = 0.0f;
	
	public Color(float a, float r, float g, float b)
	{
		this.a = a;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public Color(int color)
	{
		this.a = ((float)(color >> 24))/255.0f; // stupid java...
		this.r = ((float)((color & 0xFF0000) >> 16))/255.0f;  
		this.g = ((float)((color & 0xFF00) >> 8))/255.0f;  
		this.b = ((float)(color & 0xFF))/255.0f;  
	}
	
	public float[] toArray()
	{
		float[] c = {r,g,b,a};
		return c;
	}
	
	public static float[] intToFloatArray(int color)
	{
		Color c = new Color(color);
		return c.toArray();
	}
	
	public String toString()
	{
		return "{"+r+", "+g+", "+b+", "+a+"}";
	}
	
}
