package javax.microedition.m3g;

import javax.microedition.m3g.opengl.GL;

public class CompositingMode extends Object3D {
	
	public static final int ALPHA 		= 64;
	public static final int	ALPHA_ADD 	= 65;
	public static final int MODULATE 	= 66;
	public static final int MODULATE_X2 = 67;
	public static final int REPLACE 	= 68;
	
	private boolean depthTestEnabled	= true;
	private boolean depthWriteEnabled 	= true;
	private boolean colorWriteEnabled 	= true;
	private boolean alphaWriteEnabled 	= true;
	private int blending				= REPLACE;
	private float alphaThreshold 		= 0.0f;
	private float depthOffsetFactor 	= 0.0f;
	private float depthOffsetUnits 		= 0.0f;
	
	public CompositingMode()
	{
		depthTestEnabled	= true;
		depthWriteEnabled 	= true;
		colorWriteEnabled 	= true;
		alphaWriteEnabled 	= true;
	}
	
	public void setDepthTestEnabled(boolean depthTestEnabled) {
		this.depthTestEnabled = depthTestEnabled;
	}
	
	public boolean isDepthTestEnabled() {
		return depthTestEnabled;
	}
	
	public void setDepthWriteEnabled(boolean depthWriteEnabled) {
		this.depthWriteEnabled = depthWriteEnabled;
	}
	
	public boolean isDepthWriteEnabled() {
		return depthWriteEnabled;
	}
	
	public void setColorWriteEnabled(boolean colorWriteEnabled) {
		this.colorWriteEnabled = colorWriteEnabled;
	}
	
	public boolean isColorWriteEnabled() {
		return colorWriteEnabled;
	}
	
	public void setAlphaWriteEnabled(boolean alphaWriteEnabled) {
		this.alphaWriteEnabled = alphaWriteEnabled;
	}
	
	public boolean isAlphaWriteEnabled() {
		return alphaWriteEnabled;
	}
	
	public void setBlending(int blending) {
		this.blending = blending;
	}
	
	public int getBlending() {
		return blending;
	}
	
	public void setAlphaThreshold(float alphaThreshold) {
		this.alphaThreshold = alphaThreshold;
	}
	
	public float getAlphaThreshold() {
		return alphaThreshold;
	}
	
	public void setDepthOffsetFactor(float depthOffsetFactor) {
		this.depthOffsetFactor = depthOffsetFactor;
	}
	
	public float getDepthOffsetFactor() {
		return depthOffsetFactor;
	}
	
	public void setDepthOffsetUnits(float depthOffsetUnits) {
		this.depthOffsetUnits = depthOffsetUnits;
	}
	
	public float getDepthOffsetUnits() {
		return depthOffsetUnits;
	}
	
	void setupGL(GL gl)
	{
		// TODO: move to one-time-initilize func
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glBlendEquation(GL.GL_FUNC_ADD);

		// Setup depth testing		
		if (depthTestEnabled)
			gl.glEnable(GL.GL_DEPTH_TEST);
		else
			gl.glDisable(GL.GL_DEPTH_TEST);

		// Setup depth and color writes
		gl.glDepthMask(depthWriteEnabled);
		gl.glColorMask(colorWriteEnabled, colorWriteEnabled, colorWriteEnabled, alphaWriteEnabled);
		
		// Setup alpha testing		
		if (alphaThreshold > 0)
		{
			gl.glAlphaFunc(GL.GL_GEQUAL, alphaThreshold);
			gl.glEnable(GL.GL_ALPHA_TEST);
		}
		else
			gl.glDisable(GL.GL_ALPHA_TEST);

		// Setup blending
		if (blending != REPLACE)
		{
			switch (blending)
			{
				case ALPHA_ADD:
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
					break;
				case ALPHA:
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
					break;
				case MODULATE:
					gl.glBlendFunc(GL.GL_DST_COLOR, GL.GL_ZERO);
					break;
				case MODULATE_X2:
					gl.glBlendFunc(GL.GL_DST_COLOR, GL.GL_SRC_COLOR);
			}
			gl.glEnable(GL.GL_BLEND);
		}
		else
			gl.glDisable(GL.GL_BLEND);
		
		// Setup depth offset
		if (depthOffsetFactor != 0 || depthOffsetUnits != 0)
		{
			gl.glPolygonOffset(depthOffsetFactor, depthOffsetUnits);
			gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
		}
		else
			gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
	}
}
