package javax.microedition.m3g;

import javax.microedition.m3g.opengl.GL;
import javax.microedition.m3g.util.Color;
import javax.microedition.m3g.util.BufferUtil;
import java.nio.FloatBuffer;

public class Background extends Object3D {

	public static final int BORDER = 32;
	public static final int REPEAT = 33;
	private int backgroundColor = 0;
	private Image2D backgroundImage = null;
	private int backgroundImageModeX = BORDER;
	private int backgroundImageModeY = BORDER;
	private int cropX;
	private int cropY;
	private int cropWidth;
	private int cropHeight;
	private boolean colorClearEnabled = true;
	private boolean depthClearEnabled = true;
	
	private Texture2D backgroundTexture = null;
		
	public Background()
	{
	}
	
	public int getColor()
	{
		return this.backgroundColor;
	}
	
	public void setColor(int color)
	{
		this.backgroundColor = color;
	}
	
	public int getCropX()
	{
		return this.cropX;
	}
	
	public int getCropY()
	{
		return this.cropY;
	}
	
	public int getCropWidth()
	{
		return this.cropWidth;
	}
	
	public int getCropHeight()
	{
		return this.cropHeight;
	}
	
	public void setCrop(int x, int y, int width, int height)
	{
		this.cropX = x;
		this.cropY = y;
		this.cropWidth = width;
		this.cropHeight = height;
	}
	
	public void setColorClearEnable(boolean enable)
	{
		this.colorClearEnabled = enable;
	}
	
	public boolean isColorClearEnabled()
	{
		return this.colorClearEnabled;
	}
	
	public void setDepthClearEnable(boolean enable)
	{
		this.depthClearEnabled = enable;
	}
	
	public boolean isDepthClearEnabled()
	{
		return this.depthClearEnabled;
	}
	
	public void setImageMode(int modeX, int modeY)
	{
		this.backgroundImageModeX = modeX;
		this.backgroundImageModeY = modeY;
	}
	
	public int getImageModeX()
	{
		return this.backgroundImageModeX;
	}
	
	public int getImageModeY()
	{
		return this.backgroundImageModeY;
	}
	
	public void setImage(Image2D image)
	{
		this.backgroundImage = image;
		
	}
	
	public Image2D getImage()
	{
		return backgroundImage;
	}
	
	void setupGL(GL gl)
	{
        int clearBits = 0;

		Color c = new Color(backgroundColor);
        gl.glClearColor(c.r, c.g, c.b, c.a);
        
        if(isColorClearEnabled())
        	clearBits |= GL.GL_COLOR_BUFFER_BIT;
        if(isDepthClearEnabled())
        	clearBits |= GL.GL_DEPTH_BUFFER_BIT;
	        
        if(clearBits != 0)
        	gl.glClear(clearBits);		

        if (backgroundImage != null)
		{
			if (backgroundTexture == null)
			{
				backgroundTexture = new Texture2D(backgroundImage);
				backgroundTexture.setFiltering(Texture2D.FILTER_LINEAR,
		                             Texture2D.FILTER_LINEAR);
				backgroundTexture.setWrapping(Texture2D.WRAP_CLAMP,
		                            Texture2D.WRAP_CLAMP);
				backgroundTexture.setBlending(Texture2D.FUNC_REPLACE);
			}

			//Enable client state
			gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
			
			gl.glMatrixMode (GL.GL_MODELVIEW);
			gl.glPushMatrix ();
			gl.glLoadIdentity ();
			gl.glMatrixMode (GL.GL_PROJECTION);
			gl.glPushMatrix ();
			gl.glLoadIdentity ();
			
			gl.glColorMask(true, true, true, true);
			gl.glDepthMask(false);
			gl.glDisable(GL.GL_LIGHTING);
			gl.glDisable(GL.GL_CULL_FACE); 
			gl.glDisable(GL.GL_BLEND); 

			Graphics3D.getInstance().disableTextureUnits(); 

			gl.glActiveTexture(GL.GL_TEXTURE0);
			backgroundTexture.setupGL(gl, new float[] {1,0,0,0});
			
			// calc crop
			int w = Graphics3D.getInstance().getViewportWidth();
			int h = Graphics3D.getInstance().getViewportHeight();
			
			if (cropWidth <= 0)
				cropWidth = w;
			if (cropHeight <= 0)
				cropHeight = h;
			
			float u0 = (float)cropX/(float)w;
			float u1 = u0 + (float)cropWidth/(float)w;
			float v0 = (float)cropY/(float)h;
			float v1 = v0 + (float)cropHeight/(float)h;
			
	        /*gl.glBegin(GL.GL_QUADS);           	// Draw A Quad
	        gl.glTexCoord2f(u0, u0);	
	        gl.glVertex3f(-1.0f, 1.0f, 0);	// Top Left
	        gl.glTexCoord2f(u1, v0);	
	        gl.glVertex3f(1.0f, 1.0f, 0);	// Top Right
	        gl.glTexCoord2f(u1, v1);	
	        gl.glVertex3f(1.0f, -1.0f, 0);	// Bottom Right
	        gl.glTexCoord2f(u0, v1);	
	        gl.glVertex3f(-1.0f, -1.0f, 0);	// Bottom Left
	        gl.glEnd();*/
		
		//Since OpenGL ES doesn't support "glTexCoordxf" ,reimplement Texture mapping
		FloatBuffer quad_uvs = BufferUtil.newFloatBuffer(8);
		FloatBuffer quad_verts = BufferUtil.newFloatBuffer(16);

		quad_uvs.put(0, u0);
		quad_uvs.put(1, v0);
		quad_uvs.put(2, u1);
		quad_uvs.put(3, v0);
		quad_uvs.put(4, u0);
		quad_uvs.put(5, v1);
		quad_uvs.put(6, u1);
		quad_uvs.put(7, v1);

		quad_verts.put(0, -1.0f); //Top Left
		quad_verts.put(1, 1.0f);
		quad_verts.put(2, 0);

		quad_verts.put(3, 1.0f); //Top right
		quad_verts.put(4, 1.0f);
		quad_verts.put(5, 0);

		quad_verts.put(6, -1.0f); //Bottom Left 
		quad_verts.put(7, -1.0f);
		quad_verts.put(8, 0);

		quad_verts.put(9, 1.0f); //Bottom Right
		quad_verts.put(10, -1.0f);
		quad_verts.put(11, 0);

		//set data
		glVertexPointer(3, GL.GL_FLOAT, 0, quad_verts);
		glTexCoordPointer(2, GL.GL_FLOAT, 0, quad_uvs);

                glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);


	        gl.glPopMatrix();
	        gl.glMatrixMode (GL.GL_MODELVIEW);
	        gl.glPopMatrix();
	        //disable client state
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);

	        gl.glDisable(GL.GL_TEXTURE_2D);

             	}
	}

	
}
