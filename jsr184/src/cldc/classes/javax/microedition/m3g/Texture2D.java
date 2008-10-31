package javax.microedition.m3g;

import javax.microedition.m3g.opengl.GL;
import javax.microedition.m3g.util.Color;

public class Texture2D extends Transformable {
	public static final int FILTER_BASE_LEVEL = 208;
	public static final int FILTER_LINEAR = 209;
	public static final int FILTER_NEAREST = 210;
	public static final int FUNC_ADD = 224;
	public static final int FUNC_BLEND = 225;
	public static final int FUNC_DECAL = 226;
	public static final int FUNC_MODULATE = 227;
	public static final int FUNC_REPLACE = 228;
	public static final int WRAP_CLAMP = 240;
	public static final int WRAP_REPEAT = 241;
	
	private Image2D image;
	private int blendColor = 0;
	private int blending = FUNC_MODULATE;
	private int wrappingS = WRAP_REPEAT;
	private int wrappingT = WRAP_REPEAT;
	private int levelFilter = FILTER_BASE_LEVEL;
	private int imageFilter = FILTER_NEAREST;
	int[] id = {0};
	
	public Texture2D(Image2D image) {
		setImage(image);
	}

	public void setBlendColor(int blendColor) {
		this.blendColor = blendColor;
	}

	public int getBlendColor() {
		return blendColor;
	}

	public void setBlending(int blending) {
		this.blending = blending;
	}

	public int getBlending() {
		return blending;
	}

	public void setImage(Image2D image) {
		this.image = image;
		
		GL gl = Graphics3D.getInstance().getGL();
		
		if(gl != null) {
			gl.glGenTextures(1, id, 0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, id[0]);

			/*Graphics3D.getInstance().getGLU().gluBuild2DMipmaps(
					GL.GL_TEXTURE_2D, 
					image.getBytesPerPixel(),
					image.getWidth(), 
					image.getHeight(), 
					image.getGLFormat(), 
					GL.GL_UNSIGNED_BYTE,
					image.getPixels());*/
		        //FIXME: egl1.1 doesn't support build mipmaps	
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 4, image.getWidth(), image.getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, image.getPixels());
		}
	}

	public Image2D getImage() {
		return image;
	}

	public void setFiltering(int levelFilter, int imageFilter) {
		this.levelFilter = levelFilter;
		this.imageFilter = imageFilter;
	}

	public int getImageFilter() {
		return imageFilter;
	}

	public int getLevelFilter() {
		return levelFilter;
	}

	public void setWrapping(int wrappingS, int wrappingT) {
		this.wrappingS = wrappingS;
		this.wrappingT = wrappingT;
	}

	public int getWrappingS() {
		return wrappingS;
	}
	
	public int getWrappingT() {
		return wrappingT;
	}

	void setupGL(GL gl, float[] scaleBias)
	{
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, id[0]);
		
		// Set filtering
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, getGLFilter(this.imageFilter));	// Linear Filtering
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, getGLFilter(this.imageFilter));	// Linear Filtering

		// Set wrap mode
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, getGLWrap(this.wrappingS));
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, getGLWrap(this.wrappingT));

		// Set blendmode
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, getGLBlend());
		gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, Color.intToFloatArray(blendColor),0);

		// set texture scale
		Transform t = new Transform();
		getCompositeTransform(t);

		gl.glMatrixMode(GL.GL_TEXTURE);
		t.setGL(gl);
		gl.glTranslatef(scaleBias[1], scaleBias[2], scaleBias[3]);
		gl.glScalef(scaleBias[0], scaleBias[0], scaleBias[0]);
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}
	
	int getGLFilter(int filter)
	{
		switch (filter) {
			case FILTER_LINEAR:
				return GL.GL_LINEAR_MIPMAP_LINEAR ;
			case FILTER_NEAREST:
				return GL.GL_NEAREST_MIPMAP_LINEAR;
		    default:
		    	return GL.GL_NEAREST;
		}
	}
	
	int getGLWrap(int wrap)
	{
		switch (wrap)
		{
			case Texture2D.WRAP_CLAMP:
				return GL.GL_CLAMP_TO_EDGE; //GL_CLAMP_TO_EDGE is just fine, since JSR184 regard 
				                                                 //WRAP_CLAMP as CLAMP mode with a border width of zero
			default:
				return GL.GL_REPEAT;
		}
	}
	
	int getGLBlend()
	{
		switch (blending)
		{
			case FUNC_ADD:
				return GL.GL_ADD;
			case FUNC_MODULATE:
				return GL.GL_MODULATE;
			case FUNC_BLEND:
				return GL.GL_BLEND;
			case FUNC_REPLACE:
				return GL.GL_REPLACE;
			default:
				return GL.GL_DECAL;
		}
	}
}
