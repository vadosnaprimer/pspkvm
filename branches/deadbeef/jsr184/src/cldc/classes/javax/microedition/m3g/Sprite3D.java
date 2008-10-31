package javax.microedition.m3g;

import java.util.Hashtable;
import javax.microedition.m3g.vecMath.*;
import javax.microedition.m3g.opengl.GL;
import javax.microedition.m3g.util.BufferUtil;

public class Sprite3D extends Node {
	private static Hashtable textures = new Hashtable();
	
	private boolean scaled = false;
	private Appearance appearance;
	private Image2D image;
	private Texture2D texture;
	
	public Sprite3D(boolean scaled, Image2D image, Appearance appearance)
	{
		this.setImage(image);
		this.setAppearance(appearance);
	}
	
	public void setAppearance(Appearance appearance) {
		this.appearance = appearance;
	}

	public Appearance getAppearance() {
		return appearance;
	}

	public void setImage(Image2D image) {
		this.image = image;
		texture = (Texture2D)textures.get(image); 
		
		if (texture == null)
		{
			texture = new Texture2D(image);
			texture.setFiltering(Texture2D.FILTER_LINEAR,
	                             Texture2D.FILTER_LINEAR);
			texture.setWrapping(Texture2D.WRAP_CLAMP,
	                            Texture2D.WRAP_CLAMP);
			texture.setBlending(Texture2D.FUNC_REPLACE);

			// cache texture
			textures.put(image, texture);
		}
	}

	public Image2D getImage() {
		return image;
	}

	public boolean isScaled() {
		return scaled;
	}
	
	void render(GL gl, Transform t)
	{

		//Enable client state
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		t.multGL(gl);
		
		// get current modelview matrix
		float[] m = new float[16];
		gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, m, 0);
		
		// get up and right vector, used to create a camera-facing quad
		Vector3 up = new Vector3(m[1], m[5], m[9]);
		up.normalize();
		Vector3 right = new Vector3(m[0], m[4], m[8]);
		right.normalize();

		float size = 1;
		Vector3 rightPlusUp = new Vector3(right);
		rightPlusUp.add(up);
		rightPlusUp.multiply(size);
		Vector3 rightMinusUp = new Vector3(right);
		rightMinusUp.subtract(up);
		rightMinusUp.multiply(size);
		
		Vector3 topLeft = new Vector3(rightMinusUp);
		topLeft.multiply(-1);

		Vector3 topRight = new Vector3(rightPlusUp);
		
		Vector3 bottomLeft = new Vector3(rightPlusUp);
		bottomLeft.multiply(-1);

		Vector3 bottomRight = new Vector3(rightMinusUp);
		
		Graphics3D.getInstance().setAppearance(getAppearance());
		Graphics3D.getInstance().disableTextureUnits(); 
		gl.glActiveTexture(GL.GL_TEXTURE0);
		texture.setupGL(gl, new float[] {1,0,0,0});

		// Draw sprite
        /*gl.glBegin(GL.GL_QUADS);       
        
        gl.glTexCoord2f(0, 0);	
        gl.glVertex3f(topLeft.x, topLeft.y, topLeft.z);				// Top Left
        
        gl.glTexCoord2f(0, 1);	
        gl.glVertex3f(bottomLeft.x, bottomLeft.y, bottomLeft.z);	// Bottom Left
        
        gl.glTexCoord2f(1, 1);	
        gl.glVertex3f(bottomRight.x, bottomRight.y, bottomRight.z);	// Bottom Right

        gl.glTexCoord2f(1, 0);	
        gl.glVertex3f(topRight.x, topRight.y, topRight.z);			// Top Right

        gl.glEnd();*/			

		FloatBuffer quad_uvs = BufferUtil.newFloatBuffer(8);
		FloatBuffer quad_verts = BufferUtil.newFloatBuffer(16);

		quad_uvs.put(0, 0);
		quad_uvs.put(1, 0);
		quad_uvs.put(2, 1);
		quad_uvs.put(3, 0);
		quad_uvs.put(4, 0);
		quad_uvs.put(5, 1);
		quad_uvs.put(6, u1);
		quad_uvs.put(7, v1);

		quad_verts.put(0, topLeft.x); //Top Left
		quad_verts.put(1, topLeft.y);
		quad_verts.put(2, topLeft.z);

		quad_verts.put(3, topRight.x); //Top right
		quad_verts.put(4, topRight.y);
		quad_verts.put(5, topRight.z);

		quad_verts.put(6, bottomLeft.x); //Bottom Left 
		quad_verts.put(7, bottomLeft.y);
		quad_verts.put(8, bottomLeft.z);

		quad_verts.put(9, bottomRight.x); //Bottom Right
		quad_verts.put(10, bottomRight.y);
		quad_verts.put(11, bottomRight.z);

		//set data
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, quad_verts);
		gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, quad_uvs);

                gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);

		gl.glPopMatrix();
	        //disable client state
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        
        gl.glDisable(GL.GL_TEXTURE_2D);
        
        // HACK: for some reason, the depth write flag of other object destroyed 
        // after rendering a sprite.
        // this ensures that it's defaulted back to true
        // TODO: find error and fix it!
        gl.glDepthMask(true);
	}
}
