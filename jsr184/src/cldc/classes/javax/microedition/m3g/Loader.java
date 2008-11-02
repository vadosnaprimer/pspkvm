package javax.microedition.m3g;

import javax.microedition.m3g.util.ResourceRetriever;
import javax.microedition.m3g.util.Inflater;
import java.io.*;
import java.util.Vector;
import javax.microedition.m3g.vecMath.*;

public class Loader {
	private static final byte[] IDENTIFIER = { (byte)0xAB, 0x4A, 0x53, 0x52, 0x31, 0x38, 0x34, (byte)0xBB, 0x0D, 0x0A, 0x1A, 0x0A };
	
	private static DataInputStream in;
	private static Vector objects;
	
	public static Object3D[] load(String name)
	{
		objects = new Vector();
		try {
			in = new DataInputStream(ResourceRetriever.getResourceAsStream(name));
			
			byte[] identifier = new byte[12];
			int read = in.read(identifier, 0, 12);
			
			for(int i=0; i < 12; ++i)
				if(identifier[i] != IDENTIFIER[i])
					throw new Exception("Invalid format");
			
			/*
			 * Byte                        CompressionScheme
			 * UInt32                      TotalSectionLength
			 * UInt32                      UncompressedLength
			 * Byte[TotalSectionLength-13] Objects
			 * UInt32                      Checksum
			 */
			
			while(in.available() > 0)
			{
				int compressionScheme =  readByte();
				int totalSectionLength = readInt();
				int uncompressedLength =  readInt();
				//System.out.println("compressionScheme: " + compressionScheme);
				//System.out.println("totalSectionLength: " + totalSectionLength);
				//System.out.println("uncompressedLength: " + uncompressedLength);
				
				byte[] uncompressedData = new byte[uncompressedLength];
				
				if(compressionScheme == 0)
				{
					in.readFully(uncompressedData);
				}
				else if(compressionScheme == 1)
				{
					int compressedLength = totalSectionLength-13;
					byte[] compressedData = new byte[compressedLength];
					in.readFully(compressedData);
					
					Inflater decompresser = new Inflater();
					decompresser.setInput(compressedData, 0, compressedLength);
					int resultLength = decompresser.inflate(uncompressedData);
					decompresser.end();
					
					if(resultLength != uncompressedLength)
						throw new IOException("Unable to decompress data.");
				}
				else
					throw new IOException("Unknown compression scheme.");
				
				int checkSum = in.readInt();
				
				load(uncompressedData, 0);
			}
		}
		catch(Exception e)
		{
			System.out.println("EXCEPTION!" + e.getMessage());
		}
		
		Object3D[] obj = new Object3D[objects.size()];
		for(int i = 0; i < objects.size(); ++i)
			obj[i] = (Object3D)objects.elementAt(i);
		return obj;
		
		//return (Object3D[]) objects.toArray(typeof(Object3D));
		//return new Object3D[] { (Object3D)objects.get(objects.size()-1) };
		//return null;
	}
	
	public static Object3D[] load(byte[] data, int offset)
	{
		DataInputStream old = in;
		in = new DataInputStream(new ByteArrayInputStream(data));
		
		try {
			while(in.available() > 0) {
				int objectType = readByte();
				int length = readInt();
				
				//System.out.println("objectType: " + objectType);
				//System.out.println("length: " + length);
				
				in.mark(Integer.MAX_VALUE);
				
				if(objectType == 0)
				{
					int versionHigh = readByte();
					int versionLow = readByte();
					boolean hasExternalReferences = readBoolean();
					int totolFileSize = readInt();
					int approximateContentSize = readInt();
					String authoringField = readString();
					
					objects.addElement(new Group()); // dummy
				}
				else if(objectType == 255)
				{
					// TODO: load external resource
					System.out.println("Loader: Loading external resources not implemented.");
					String uri = readString();
				}
				else if(objectType == 1)
				{
					System.out.println("Loader: AnimationController not implemented.");
					objects.addElement(new Group()); // dummy
				}
				else if(objectType == 2)
				{
					System.out.println("Loader: AnimationTrack not implemented.");
					objects.addElement(new Group()); // dummy
				}
				else if(objectType == 3)
				{
					//System.out.println("Appearance");
					Appearance appearance = new Appearance();
					loadObject3D(appearance);
					appearance.setLayer(readByte());
					appearance.setCompositingMode((CompositingMode)getObject(readInt()));
					appearance.setFog((Fog)getObject(readInt()));
					appearance.setPolygonMode((PolygonMode)getObject(readInt()));
					appearance.setMaterial((Material)getObject(readInt()));
					int numTextures = readInt();
					for(int i = 0; i < numTextures; ++i)
						appearance.setTexture(i, (Texture2D)getObject(readInt()));
					objects.addElement(appearance);
				}
				else if(objectType == 4)
				{
					//System.out.println("Background");
					Background background = new Background();
					loadObject3D(background);
					background.setColor(readRGBA());
					background.setImage((Image2D)getObject(readInt()));
					int modeX = readByte();
					int modeY = readByte();
					background.setImageMode(modeX, modeY);
					int cropX = readInt();
					int cropY = readInt();
					int cropWidth = readInt();
					int cropHeight = readInt();
					background.setCrop(cropX, cropY, cropWidth, cropHeight);
					background.setDepthClearEnable(readBoolean());
					background.setColorClearEnable(readBoolean());
					objects.addElement(background); // dummy
				}
				else if(objectType == 5)
				{
					//System.out.println("Camera");
					
					Camera camera = new Camera();
					loadNode(camera);
					
					int projectionType = readByte();
					if(projectionType == Camera.GENERIC) {
						Transform t = new Transform();
						t.set(readMatrix());
						camera.setGeneric(t);
					} else {
						float fovy = readFloat();
						float aspect = readFloat();
						float near = readFloat();
						float far = readFloat();
						if(projectionType == Camera.PARALLEL)
							camera.setParallel(fovy, aspect, near, far);
						else
							camera.setPerspective(fovy, aspect, near, far);
					}
					objects.addElement(camera);
				}
				else if(objectType == 6)
				{
					//System.out.println("CompositingMode");
					CompositingMode compositingMode = new CompositingMode();
					loadObject3D(compositingMode);
					compositingMode.setDepthTestEnabled(readBoolean());
					compositingMode.setDepthWriteEnabled(readBoolean());
					compositingMode.setColorWriteEnabled(readBoolean());
					compositingMode.setAlphaWriteEnabled(readBoolean());
					compositingMode.setBlending(readByte());
					compositingMode.setAlphaThreshold((float)readByte() / 255.0f);
					compositingMode.setDepthOffsetFactor(readFloat());
					compositingMode.setDepthOffsetUnits(readFloat());
					objects.addElement(compositingMode);
				}
				else if(objectType == 7)
				{
					//System.out.println("Fog");
					Fog fog = new Fog();
					loadObject3D(fog);
					fog.setColor(readRGB());
					fog.setMode(readByte());
					if(fog.getMode() == Fog.EXPONENTIAL)
						fog.setDensity(readFloat());
					else
					{
						fog.setNearDistance(readFloat());
						fog.setFarDistance(readFloat());
					}
					objects.addElement(fog);
				}
				else if(objectType == 9)
				{
					//System.out.println("Group");
					Group group = new Group();
					loadGroup(group);
					objects.addElement(group);
				}
				else if(objectType == 10)
				{
					//System.out.println("Image2D");
					Image2D image = null;
					loadObject3D(new Group()); // dummy
					int format = readByte();
					boolean isMutable = readBoolean();
					int width = readInt();
					int height = readInt();
					if(!isMutable) {
						// Read palette
						int paletteSize = readInt();
						byte[] palette = null;
						if(paletteSize > 0) {
							palette = new byte[paletteSize];
							in.readFully(palette);
						}
						// Read pixels
						int pixelSize = readInt();
						byte[] pixel = new byte[pixelSize];
						in.readFully(pixel);
						// Create image
						if(palette != null)
							image = new Image2D(format, width, height, pixel, palette);
						else
							image = new Image2D(format, width, height, pixel);
					}
					else
						image = new Image2D(format, width, height);
					
					in.reset();
					loadObject3D(image);
					
					objects.addElement(image);
				}
				else if(objectType == 19)
				{
					System.out.println("Loader: KeyframeSequence not implemented.");
					/*
					Byte          interpolation;
					Byte          repeatMode;
					Byte          encoding;
					UInt32        duration;
					UInt32        validRangeFirst;
					UInt32        validRangeLast;
					
					UInt32        componentCount;
					UInt32        keyframeCount;
					
					IF encoding == 0
					
					    FOR each key frame...
					
					        UInt32                  time;
					        Float32[componentCount] vectorValue;
					
					    END
					
					ELSE IF encoding == 1
					
					    Float32[componentCount] vectorBias;
					    Float32[componentCount] vectorScale;
					
					    FOR each key frame...
					
					        UInt32               time;
					        Byte[componentCount] vectorValue;
					
					    END
					
					ELSE IF encoding == 2
					
					    Float32[componentCount] vectorBias;
					    Float32[componentCount] vectorScale;
					
					    FOR each key frame...
					
					        UInt32                 time;
					        UInt16[componentCount] vectorValue;
					
					    END
					
					END
					*/
					objects.addElement(new Group()); // dummy
				}
				else if(objectType == 12)
				{
					//System.out.println("Light");
					Light light = new Light();
					loadNode(light);
					float constant = readFloat();
					float linear = readFloat();
					float quadratic = readFloat();
					light.setAttenuation(constant, linear, quadratic);
					light.setColor(readRGB());
					light.setMode(readByte());
					light.setIntensity(readFloat());
					light.setSpotAngle(readFloat());
					light.setSpotExponent(readFloat());
					objects.addElement(light);
				}
				else if(objectType == 13)
				{
					//System.out.println("Material");
					Material material = new Material();
					loadObject3D(material);
					material.setColor(Material.AMBIENT, readRGB());
					material.setColor(Material.DIFFUSE, readRGBA());
					material.setColor(Material.EMISSIVE, readRGB());
					material.setColor(Material.SPECULAR, readRGB());
					material.setShininess(readFloat());
					material.setVertexColorTrackingEnabled(readBoolean());
					objects.addElement(material);
				}
				else if(objectType == 14)
				{
					//System.out.println("Mesh");

					loadNode(new Group()); // dummy
					
					VertexBuffer vertices = (VertexBuffer)getObject(readInt());
					int submeshCount = readInt();
					
					IndexBuffer[] submeshes = new IndexBuffer[submeshCount];
					Appearance[] appearances = new Appearance[submeshCount];
					for(int i = 0; i < submeshCount; ++i)
					{
						submeshes[i] = (IndexBuffer)getObject(readInt());
						appearances[i] = (Appearance)getObject(readInt());
					}
					Mesh mesh = new Mesh(vertices, submeshes, appearances);
					
					in.reset();
					loadNode(mesh);
					
					objects.addElement(mesh);
				}
				else if(objectType == 15)
				{
					System.out.println("Loader: MorphingMesh not implemented.");
					/*
					UInt32        morphTargetCount;
					
					FOR each target buffer...
					
					    ObjectIndex   morphTarget;
					    Float32       initialWeight;
					
					END
					*/
					objects.addElement(new Group()); // dummy
				}
				else if(objectType == 8)
				{
					//System.out.println("PolygonMode");
					PolygonMode polygonMode = new PolygonMode();
					loadObject3D(polygonMode);
					polygonMode.setCulling(readByte());
					polygonMode.setShading(readByte());
					polygonMode.setWinding(readByte());
					polygonMode.setTwoSidedLightingEnabled(readBoolean());
					polygonMode.setLocalCameraLightingEnabled(readBoolean());
					polygonMode.setPerspectiveCorrectionEnabled(readBoolean());
					objects.addElement(polygonMode);
				}
				else if(objectType == 16)
				{
					System.out.println("Loader: SkinnedMesh not implemented.");
					/*
					ObjectIndex   skeleton;
					
					UInt32        transformReferenceCount;
					
					FOR each bone reference...
					
					    ObjectIndex   transformNode;
					    UInt32        firstVertex;
					    UInt32        vertexCount;
					    Int32         weight;
					
					END
					*/
					objects.addElement(new Group()); // dummy
				}
				else if(objectType == 18)
				{
					System.out.println("Loader: Sprite not implemented.");
					/*
					ObjectIndex   image;
					ObjectIndex   appearance;
					
					Boolean       isScaled;
					
					Int32         cropX;
					Int32         cropY;
					Int32         cropWidth;
					Int32         cropHeight;
					*/
					objects.addElement(new Group()); // dummy
				}
				else if(objectType == 17)
				{
					//System.out.println("Texture2D");

					loadTransformable(new Group()); // dummy
					
					Texture2D texture = new Texture2D((Image2D)getObject(readInt()));
					texture.setBlendColor(readRGB());
					texture.setBlending(readByte());
					int wrapS = readByte();
					int wrapT = readByte();
					texture.setWrapping(wrapS, wrapT);
					int levelFilter = readByte();
					int imageFilter = readByte();
					texture.setFiltering(levelFilter, imageFilter);
					
					in.reset();
					loadTransformable(texture);
					
					objects.addElement(texture);
				}
				else if(objectType == 11)
				{
					//System.out.println("TriangleStripArray");
					
					loadObject3D(new Group()); // dummy
					
					int encoding = readByte();
					int firstIndex = 0;
					int[] indices = null;
					if(encoding == 0)
						firstIndex = readInt();
					else if(encoding == 1)
						firstIndex = readByte();
					else if(encoding == 2)
						firstIndex = readShort();
					else if(encoding == 128)
					{
						int numIndices = readInt();
						indices = new int[numIndices];
						for(int i = 0; i < numIndices; ++i)
							indices[i] = readInt();
					}
					else if(encoding == 129)
					{
						int numIndices = readInt();
						indices = new int[numIndices];
						for(int i = 0; i < numIndices; ++i)
							indices[i] = readByte();
					}
					else if(encoding == 130)
					{
						int numIndices = readInt();
						indices = new int[numIndices];
						for(int i = 0; i < numIndices; ++i)
							indices[i] = readShort();
					}
					
					int numStripLengths = readInt();
					int[] stripLengths = new int[numStripLengths];
					for(int i = 0; i < numStripLengths; ++i)
						stripLengths[i] = readInt();
					
					in.reset();
					
					TriangleStripArray triStrip = null;
					if(indices == null)
						triStrip = new TriangleStripArray(firstIndex, stripLengths);
					else
						triStrip = new TriangleStripArray(indices, stripLengths);
					
					loadObject3D(triStrip);
					
					objects.addElement(triStrip);
				}
				else if(objectType == 20)
				{
					//System.out.println("VertexArray");
					
					loadObject3D(new Group()); // dummy
					
					int componentSize = readByte();
					int componentCount = readByte();
					int encoding = readByte();
					int vertexCount = readShort();
					
					VertexArray vertices = new VertexArray(vertexCount, componentCount, componentSize);
					
					if(componentSize == 1)
					{
						byte[] values = new byte[componentCount*vertexCount];
						if(encoding == 0)
							in.readFully(values);
						else
						{
							byte last = 0;
							for(int i = 0; i < vertexCount*componentCount; ++i)
							{								
								last += readByte();
								values[i] = last;
							}
						}
						vertices.set(0, vertexCount, values);
					}
					else
					{
						short last = 0;
						short[] values = new short[componentCount*vertexCount];
						for(int i = 0; i < componentCount*vertexCount; ++i)
						{
							if(encoding == 0)
								values[i] = (short)readShort();
							else {
								last += (short)readShort();
								values[i] = last;
							}
						}
						vertices.set(0, vertexCount, values);						
					}
					
					in.reset();
					loadObject3D(vertices);
					
					objects.addElement(vertices);
				}
				else if(objectType == 21)
				{
					//System.out.println("VertexBuffer");

					VertexBuffer vertices = new VertexBuffer();
					loadObject3D(vertices);
					
					vertices.setDefaultColor(readRGBA());
					
					VertexArray positions = (VertexArray)getObject(readInt());
					float[] bias = new float[3];
					bias[0] = readFloat();
					bias[1] = readFloat();
					bias[2] = readFloat();
					float scale = readFloat();
					vertices.setPositions(positions, scale, bias);
					
					vertices.setNormals((VertexArray)getObject(readInt()));
					vertices.setColors((VertexArray)getObject(readInt()));
					
					int texCoordArrayCount = readInt();
					for(int i = 0; i < texCoordArrayCount; ++i)
					{
						VertexArray texcoords = (VertexArray)getObject(readInt());
						bias[0] = readFloat();
						bias[1] = readFloat();
						bias[2] = readFloat();
						scale = readFloat();
						vertices.setTexCoords(i, texcoords, scale, bias);
					}
					
					objects.addElement(vertices);
				}
				else if(objectType == 22)
				{
					//System.out.println("World");

					World world = new World();					
					loadGroup(world);
					
					world.setActiveCamera((Camera)getObject(readInt()));
					world.setBackground((Background)getObject(readInt()));
					objects.addElement(world);
				}
				else
				{
					System.out.println("Loader: unsupported objectType " + objectType + ".");
				}
				
				in.reset();
				in.skipBytes(length);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
		}
		
		in = old;
		return null;
	}
	
	// Fundamental data types
	
	private static int readByte() throws IOException
	{
		return in.readUnsignedByte();
	}
	
	private static int readShort() throws IOException
	{
		int a = readByte();
		int b = readByte();
		return (b<<8) + a;
	}
	
	private static int readRGB() throws IOException
	{
		byte r = in.readByte();
		byte g = in.readByte();
		byte b = in.readByte();
		return (r<<16) + (g<<8) + b;
	}
	
	private static int readRGBA() throws IOException
	{
		byte r = in.readByte();
		byte g = in.readByte();
		byte b = in.readByte();
		byte a = in.readByte();
		return (a<<24) + (r<<16) + (g<<8) + b;
	}
	
	private static float readFloat() throws IOException
	{
		return Float.intBitsToFloat(readInt());
	}
	
	private static int readInt() throws IOException
	{
		int a = in.readUnsignedByte();
		int b = in.readUnsignedByte();
		int c = in.readUnsignedByte();
		int d = in.readUnsignedByte();
		int i = (d<<24) | (c<<16) | (b<<8) | a; 
		return i;
	}
	
	private static boolean readBoolean() throws IOException
	{
		return readByte() == 1;
	}
	
	private static String readString() throws IOException
	{
		// TODO:
		return "";
	}
	
	// Compound data types
	
	private static float[] readMatrix() throws IOException
	{
		float[] m = new float[16];
		for(int i = 0; i < 16; ++i)
			m[i] = readFloat();
		return m;
	}
	
	// Other
	
	private static Object getObject(int index)
	{
		if(index == 0)
			return null;
		return objects.elementAt(index-1);
	}
	
	private static void loadObject3D(Object3D object) throws IOException
	{
		object.setUserID(readInt());
		
		int animationTracks = readInt();
		for(int i = 0; i < animationTracks; ++i)
			readInt();//object.addAnimationTrack((AnimationTrack)getObject(readInt()));
		
		int userParameterCount = readInt();
		for(int i = 0; i < userParameterCount; ++i) {
			int parameterID = readInt();
			int numBytes = readInt();
			byte[] parameterBytes = new byte[numBytes];
			in.readFully(parameterBytes);
		}
	}
	
	private static void loadTransformable(Transformable transformable) throws IOException
	{
		loadObject3D(transformable);
		if(readBoolean()) // hasComponentTransform
		{
			float tx = readFloat();
			float ty = readFloat();
			float tz = readFloat();
			transformable.setTranslation(tx, ty, tz);
			float sx = readFloat();
			float sy = readFloat();
			float sz = readFloat();
			transformable.setScale(sx, sy, sz);
			float angle = readFloat();
			float ax = readFloat();
			float ay = readFloat();
			float az = readFloat();
			transformable.setOrientation(angle, ax, ay, az);
		}
		if(readBoolean()) // hasGeneralTransform
		{
			Transform t = new Transform();
			t.set(readMatrix());
			transformable.setTransform(t);
		}
	}
	
	private static void loadNode(Node node) throws IOException
	{
		loadTransformable(node);
		node.setRenderingEnable(readBoolean());
		node.setPickingEnable(readBoolean());
		int alpha = readByte();
		node.setAlphaFactor( (float)alpha / 255.0f );
		node.setScope(readInt());
		if(readBoolean()) // hasAlignment
		{
			// TODO: set node alignment
			int zTarget = readByte();
			int yTarget = readByte();
			readInt(); // zReference
			readInt(); // yReference
		}
	}
	
	private static void loadGroup(Group group) throws IOException
	{
		loadNode(group);
		int count = readInt();
		for(int i = 0; i < count; ++i)
			group.addChild((Node)getObject(readInt()));
	}
}
