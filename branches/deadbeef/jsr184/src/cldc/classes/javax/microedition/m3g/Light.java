package javax.microedition.m3g;

import javax.microedition.m3g.opengl.GL;
import javax.microedition.m3g.util.Color;

public class Light extends Node {
    public static final int AMBIENT 	= 128;
    public static final int DIRECTIONAL = 129;
    public static final int OMNI 		= 130;
    public static final int SPOT 		= 131;

    private float attenuationConstant 	= 1;
    private float attenuationLinear 	= 0;
    private float attenuationQuadratic 	= 0;
    private int color 					= 0x00FFFFFF;
    private int mode 					= DIRECTIONAL;
    private float intensity 			= 1.0f;
    private float spotAngle 			= 45.0f;
    private float spotExponent 			= 0.0f;

    private int lightId = -1;

    public Light()
    {

    }

    public int getColor()
    {
	return color;
    }

    public float getConstantAttenuation()
    {
	return attenuationConstant;
    }

    public float getIntensity()
    {
	return intensity;
    }

    public float getLinearAttenuation()
    {
	return attenuationLinear;
    }

    public int getMode()
    {
	return mode;
    }

    public float getQuadraticAttenuation()
    {
	return attenuationQuadratic;
    }

    public float getSpotAngle()
    {
	return spotAngle;
    }

    public float getSpotExponent()
    {
	return spotExponent;
    }

    public void setAttenuation(float constant, float linear, float quadratic)
    {
	attenuationConstant = constant;
	attenuationLinear = linear;
	attenuationQuadratic = quadratic;
    }

    public void setColor(int color)
    {
	this.color = color;
    }

    public void setIntensity(float intensity)
    {
	this.intensity = intensity;
    }

    public void setMode(int mode)
    {
	this.mode = mode;
    }

    public void setSpotAngle(float angle)
    {
	spotAngle = angle;
    }

    public void setSpotExponent(float exponent)
    {
	spotExponent = exponent;
    }

    void setupGL(GL gl)
    {
	// TODO: color and intensity
	float[] col = (new Color(color)).toArray();

	col[0] *= intensity;
	col[1] *= intensity;
	col[2] *= intensity;
	col[3] *= intensity;

	if (mode == Light.AMBIENT)
	    gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, col, 0);
	else
	{
	    getFreeLightId(gl);

	    if(lightId == -1)
		return;

	    gl.glLightfv(lightId, GL.GL_DIFFUSE, col, 0);
	    gl.glLightfv(lightId, GL.GL_AMBIENT, new float[] { 0,0,0,0 }, 0);
	    gl.glLightfv(lightId, GL.GL_SPECULAR, col, 0);

	    if (mode == Light.OMNI)
	    {
		// set position
		gl.glLightfv(lightId, GL.GL_POSITION, new float[] { 0,0,0,1 }, 0);

		// Set default values for cutoff/exponent
		gl.glLightf(lightId, GL.GL_SPOT_CUTOFF, 180.0f); // 0..90, 180
		gl.glLightf(lightId, GL.GL_SPOT_EXPONENT, 0.0f); // 0..128				
	    }
	    else if(mode == Light.SPOT)
	    {
		// set position
		gl.glLightfv(lightId, GL.GL_POSITION, new float[] { 0,0,0,1 }, 0);

		// Set cutoff/exponent
		gl.glLightf(lightId, GL.GL_SPOT_CUTOFF, spotAngle);
		gl.glLightf(lightId, GL.GL_SPOT_EXPONENT, spotExponent);

		// Set default spot direction
		gl.glLightfv(lightId, GL.GL_SPOT_DIRECTION, new float[] { 0,0,1 }, 0);
	    }
	    else if(mode == Light.DIRECTIONAL)
	    {
		// set direction (w=0 meaning directional instead of positional)
		gl.glLightfv(lightId, GL.GL_POSITION, new float[] { 0,0,1,0 }, 0);

		// Set default values for cutoff/exponent
		gl.glLightf(lightId, GL.GL_SPOT_CUTOFF, 180.0f);
		gl.glLightf(lightId, GL.GL_SPOT_EXPONENT, 0.0f);
	    }

	    gl.glLightf(lightId, GL.GL_CONSTANT_ATTENUATION, attenuationConstant);
	    gl.glLightf(lightId, GL.GL_LINEAR_ATTENUATION, attenuationLinear);
	    gl.glLightf(lightId, GL.GL_QUADRATIC_ATTENUATION, attenuationQuadratic);
	}
    }

    private void getFreeLightId(GL gl) {
	lightId = -1;
	for(int i = 0; i < Graphics3D.MAX_LIGHT_COUNT; i++) {
	    if(!gl.glIsEnabled(GL.GL_LIGHT0 + i)) {
		lightId = GL.GL_LIGHT0 + i;
		gl.glEnable(lightId);
		return;
	    }
	}
    }
}
