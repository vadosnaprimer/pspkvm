package javax.microedition.m3g;

import javax.microedition.lcdui.Graphics;
//import javax.microedition.m3g.util.Reflection;

import javax.microedition.m3g.opengl.*;
import java.util.Vector;


import java.nio.*;

public final class Graphics3D {

	private static Graphics3D instance = null;
	
	static final int MAX_LIGHT_COUNT = 8;
	
	private int numTextureUnits = 8; // TODO: get from caps
	
	private int viewportX = 0;
	private int viewportY = 0;
	private int viewportWidth = 0;
	private int viewportHeight = 0;
	
	private GL gl = null;
	private GLU glu = null;
	
	private Object renderTarget;
	private GLCanvas currentGLCanvas;
	private RenderEventListener currentRenderListener;
	
	private Camera camera;
	private Transform cameraTransform;
	
	private Vector lights = new Vector();
	
	private CompositingMode defaultCompositioningMode = new CompositingMode();
	private PolygonMode defaultPolygonMode = new PolygonMode();
	
	private Graphics3D()
	{
	}
	
	public static Graphics3D getInstance()
	{
		if (instance == null)
			instance = new Graphics3D();
		return instance;
	}
	
	public void bindTarget(Object target, RenderEventListener renderListener)
	{
		if (target instanceof Graphics)
		{
			this.renderTarget = target;
			this.currentRenderListener = renderListener;
			GLCanvas canvas = GLME.getInstance().getRenderTarget((Graphics)target);
			this.currentGLCanvas = canvas;

			canvas.addGLEventListener(renderListener);
			canvas.display();
		}
	}

	public void bindTarget(Object target)
	{
		if (target instanceof Graphics)
		{
			this.renderTarget = target;
			GLCanvas canvas = GLME.getInstance().getRenderTarget((Graphics)target);
			this.currentGLCanvas = canvas;
			
			// set the gurrent GL object
			setGL(canvas.getGL());

			// set the GLContext of the canvas to be the current context
                        canvas.getContext().makeCurrent();
            
                         // Set default viewport?
                        if(this.viewportHeight == 0 || this.viewportWidth == 0)
            	            setViewport(0, 0, canvas.getWidth(), canvas.getHeight());
		}
	}

	public void bindTarget(Object target, boolean depthBuffer, int hints)
	{
		bindTarget(target, null);
	}
	
	public Object getTarget()
	{
		return this.renderTarget;
	}
	
	public void releaseTarget()
	{
		if (this.currentGLCanvas != null)
		{
			if (this.currentRenderListener != null)
			{
				this.currentGLCanvas.removeGLEventListener(this.currentRenderListener);
				this.currentRenderListener = null;
			}
			else
			{
				// Hack into the current GLCanvas with reflection and use it's
				// internal drawable to swap the buffers
			    GLContext ctx = this.currentGLCanvas.getContext();
			    ctx.swapBuffers();
			}
		}
		this.currentGLCanvas = null;
	}
	
	public void clear(Background background)
	{
		if(background != null)
			background.setupGL(gl);
		else
		{
			// clear to black
	        gl.glClearColor(0,0,0,0);
	        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		}
	}
	
	public int addLight(Light light, Transform transform)
	{
		if (light == null)
			throw new NullPointerException("light must not be null");

		lights.addElement(light);
		int index = lights.size() - 1;

		// limit the number of lights
		if (index < MAX_LIGHT_COUNT)
		{
			gl.glPushMatrix();
			transform.multGL(gl); // TODO: should this really be setGL? I was thinking multGL...
			
			light.setupGL(gl);
			gl.glPopMatrix();
		}
		
		return index;
	}
	
	public void setLight(int index, Light light, Transform transform)
	{
		lights.setElementAt(light, index);
		// TODO: set transform and update light
	}
	
	public void resetLights()
	{
		lights.removeAllElements();
		
		for(int i = 0; i < MAX_LIGHT_COUNT; ++i)
			gl.glDisable(GL.GL_LIGHT0 + i);
	}
	
	public int getLightCount()
	{
		// TODO
		return 0;
	}
	
	public Light getLight(int index, Transform transform)
	{
		// TODO
		return null;
	}
	
	public int getHints()
	{
		// TODO
		return 0;
	}
	
	public boolean isDepthBufferEnabled()
	{
		// TODO
		return true;
	}
	
	public void setViewport(int x, int y, int width, int height)
	{
		this.viewportX = x;
		this.viewportY = y;
		this.viewportWidth = width;
		this.viewportHeight = height;

		gl.glViewport(x, y, width, height);	
	}
	
	public int getViewportX()
	{
		return this.viewportX;
	}
	
	public int getViewportY()
	{
		return this.viewportY;
	}
	
	public int getViewportWidth()
	{
		return this.viewportWidth;
	}
	
	public int getViewportHeight()
	{
		return this.viewportHeight;
	}
	
	public void setDepthRange(float near, float far)
	{
		// TODO
	}
	
	public void setCamera(Camera camera, Transform transform)
	{
		this.camera = camera;
		this.cameraTransform = transform;
		
		Transform t = new Transform();
		
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    camera.getProjection(t);
	    t.setGL(gl);
	    
	    gl.glMatrixMode(GL.GL_MODELVIEW);
		t.set(transform);
		t.invert();
		t.setGL(gl);
	}
	
	public Camera getCamera(Transform transform)
	{
		if(transform != null)
			transform.set(this.cameraTransform);
		return camera;
	}
	
	public void render(Node node, Transform transform)
	{
		if(node instanceof Mesh) {
			Mesh mesh = (Mesh)node;
			int subMeshes = mesh.getSubMeshCount();
			VertexBuffer vertices = mesh.getVertexBuffer();
			for(int i = 0; i < subMeshes; ++i)
				render(vertices, mesh.getIndexBuffer(i), mesh.getAppearance(i), transform);
		}
		else if (node instanceof Sprite3D)
		{
			Sprite3D sprite = (Sprite3D)node;
			sprite.render(gl, transform);
		}
	}
	
	public void render(VertexBuffer vertices, IndexBuffer triangles, Appearance appearance, Transform transform)
	{
		if(vertices == null)
			throw new NullPointerException("vertices == null");
		if(triangles == null)
			throw new NullPointerException("triangles == null");
		if(appearance == null)
			throw new NullPointerException("appearance == null");

		float[] scaleBias = new float[4];
		VertexArray positions = vertices.getPositions(scaleBias);
		FloatBuffer pos = positions.getFloatBuffer();
		pos.position(0);
		gl.glVertexPointer(positions.getComponentCount(), GL.GL_FLOAT, 0, pos);
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		
		VertexArray normals = vertices.getNormals();
		if(normals != null) {
			FloatBuffer norm = normals.getFloatBuffer();
			norm.position(0);

			gl.glEnable(GL.GL_NORMALIZE);
			gl.glNormalPointer(GL.GL_FLOAT, 0, norm);
			gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
		} else {
			gl.glDisable(GL.GL_NORMALIZE);
			gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
		}
		
		VertexArray colors = vertices.getColors();
		if(colors != null) {
			Buffer buffer = colors.getBuffer();
			buffer.position(0);
			gl.glColorPointer(colors.getComponentCount(), colors.getComponentTypeGL(), 0, buffer);
			gl.glEnableClientState(GL.GL_COLOR_ARRAY);
		} else 
			gl.glDisableClientState(GL.GL_COLOR_ARRAY);
		
		for(int i = 0; i < 8; ++i) {
			float[] texScaleBias = new float[4];
			VertexArray texcoords = vertices.getTexCoords(i, texScaleBias);
			gl.glActiveTexture(GL.GL_TEXTURE0 + i);
			gl.glClientActiveTexture(GL.GL_TEXTURE0 + i);
			if(texcoords != null) {
				FloatBuffer tex = texcoords.getFloatBuffer();
				tex.position(0);

				if (appearance.getTexture(i) != null)
					appearance.getTexture(i).setupGL(gl, texScaleBias);
				else
					gl.glDisable(GL.GL_TEXTURE_2D);
				
				gl.glTexCoordPointer(texcoords.getComponentCount(), GL.GL_FLOAT, 0, tex);
				gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);

			} else {
				gl.glDisable(GL.GL_TEXTURE_2D);
				gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY); 
			}
		}
		
		setAppearance(appearance);			
		
		gl.glPushMatrix();
		transform.multGL(gl);
		
		gl.glTranslatef(scaleBias[1], scaleBias[2], scaleBias[3]);
		gl.glScalef(scaleBias[0], scaleBias[0], scaleBias[0]);

        if(triangles instanceof TriangleStripArray)
		{	
			IntBuffer indices = triangles.getBuffer();
			indices.position(0);
			gl.glDrawElements(GL.GL_TRIANGLE_STRIP, triangles.getIndexCount(), GL.GL_UNSIGNED_INT, indices);
		}
		else
			gl.glDrawElements(GL.GL_TRIANGLES, triangles.getIndexCount(), GL.GL_UNSIGNED_INT, triangles.getBuffer());

        gl.glPopMatrix();
	}
	
	public void render(VertexBuffer vertices, IndexBuffer triangles, Appearance appearance, Transform transform, int scope)
	{
		// TODO: check scope
		render(vertices, triangles, appearance, transform);
	}
	
	public void render(World world)
	{
		clear(world.getBackground());
		
		Transform t = new Transform();
		
		// Setup camera
		Camera c = world.getActiveCamera();
		if(c == null)
			throw new IllegalStateException("World has no active camera.");
		if(!c.getTransformTo(world, t))
			throw new IllegalStateException("Camera is not in world.");
		setCamera(c, t);
		
		// Setup lights
		resetLights();
		populateLights(world, world);
		
		// Begin traversal of scene graph
		renderDescendants(world, world);
	}
	
	private void populateLights(World world, Object3D obj)
	{
		int numReferences = obj.getReferences(null);
		if(numReferences > 0)
		{
			Object3D[] objArray = new Object3D[numReferences];
			obj.getReferences(objArray);
			for(int i = 0; i < numReferences; ++i)
			{
				if(objArray[i] instanceof Light)
				{
					Transform t = new Transform();
					Light light = (Light)objArray[i];
					if(light.isRenderingEnabled() && light.getTransformTo(world, t))
						addLight(light, t);
				}
				populateLights(world, objArray[i]);
	    	}
	 	}		
	}
	
	private void renderDescendants(World world, Object3D obj)
	{
		int numReferences = obj.getReferences(null);
		if(numReferences > 0)
		{
			Object3D[] objArray = new Object3D[numReferences];
			obj.getReferences(objArray);
			for(int i = 0; i < numReferences; ++i)
			{
				if(objArray[i] instanceof Node)
				{
					Transform t = new Transform();
					Node node = (Node)objArray[i];
					node.getTransformTo(world, t);
					render(node, t);
				}
	       		renderDescendants(world, objArray[i]);
	    	}
	 	}		
	}
	
	void setAppearance(Appearance appearance)
	{
		if (appearance == null)
			throw new NullPointerException("appearance must not be null");

		// Polygon mode
		PolygonMode polyMode = appearance.getPolygonMode();
		if (polyMode == null)
			polyMode = defaultPolygonMode;
		polyMode.setupGL(gl);
		
		// Material
		if (appearance.getMaterial() != null)
			appearance.getMaterial().setupGL(gl, polyMode.getLightTarget());
		else
			gl.glDisable(GL.GL_LIGHTING);
			
		// Fog
		if (appearance.getFog() != null)
			appearance.getFog().setupGL(gl);
		else
			gl.glDisable(GL.GL_FOG);
		
		// Comp mode
		if (appearance.getCompositingMode() != null)
			appearance.getCompositingMode().setupGL(gl);
		else
			defaultCompositioningMode.setupGL(gl);
	}
	
	void setGL(GL gl)
	{
		this.gl = gl;
	}
	
	GL getGL()
	{
		if (this.gl != null)
			return this.gl;

		// try to fecth a gl object from the Emulator eneviroment instead!
		// NOTE: experimental, not working
		return GLME.getInstance().getGL();
	}
	
	GLU getGLU()
	{
		if (glu == null)
			glu = new GLU();
		return glu;
	}
	
	int getTextureUnitCount()
	{
		return numTextureUnits;
	}
	
	void disableTextureUnits()
	{
		for (int i=0; i<numTextureUnits; i++)
		{
			gl.glActiveTexture(GL.GL_TEXTURE0+i);
			gl.glDisable(GL.GL_TEXTURE_2D);
		}
	}
}
