/*
 *
 *
 */
package javax.microedition.m3g.opengl;

import java.nio.Buffer;
import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.GL10;

public class GL {
  // ClearBufferMask
  /** OpenGL ES 1.0 constant. */
  public static int GL_DEPTH_BUFFER_BIT                     = 0x00000100;
  /** OpenGL ES 1.0 constant. */
  public static int GL_STENCIL_BUFFER_BIT                   = 0x00000400;
  /** OpenGL ES 1.0 constant. */
  public static int GL_COLOR_BUFFER_BIT                     = 0x00004000;

  // Boolean
  /** OpenGL ES 1.0 constant. */
  public static int GL_FALSE                                = 0;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TRUE                                 = 1;

  // BeginMode
  /** OpenGL ES 1.0 constant. */
  public static int GL_POINTS                               = 0x0000;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LINES                                = 0x0001;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LINE_LOOP                            = 0x0002;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LINE_STRIP                           = 0x0003;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TRIANGLES                            = 0x0004;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TRIANGLE_STRIP                       = 0x0005;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TRIANGLE_FAN                         = 0x0006;

  // AlphaFunction
  /** OpenGL ES 1.0 constant. */
  public static int GL_NEVER                                = 0x0200;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LESS                                 = 0x0201;
  /** OpenGL ES 1.0 constant. */
  public static int GL_EQUAL                                = 0x0202;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LEQUAL                               = 0x0203;
  /** OpenGL ES 1.0 constant. */
  public static int GL_GREATER                              = 0x0204;
  /** OpenGL ES 1.0 constant. */
  public static int GL_NOTEQUAL                             = 0x0205;
  /** OpenGL ES 1.0 constant. */
  public static int GL_GEQUAL                               = 0x0206;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ALWAYS                               = 0x0207;

  // BlendingFactorDest
  /** OpenGL ES 1.0 constant. */
  public static int GL_ZERO                                 = 0;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ONE                                  = 1;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SRC_COLOR                            = 0x0300;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ONE_MINUS_SRC_COLOR                  = 0x0301;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SRC_ALPHA                            = 0x0302;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ONE_MINUS_SRC_ALPHA                  = 0x0303;
  /** OpenGL ES 1.0 constant. */
  public static int GL_DST_ALPHA                            = 0x0304;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ONE_MINUS_DST_ALPHA                  = 0x0305;

  // BlendingFactorSrc
  /** OpenGL ES 1.0 constant. */
  public static int GL_DST_COLOR                            = 0x0306;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ONE_MINUS_DST_COLOR                  = 0x0307;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SRC_ALPHA_SATURATE                   = 0x0308;

  // CullFaceMode
  /** OpenGL ES 1.0 constant. */
  public static int GL_FRONT                                = 0x0404;
  /** OpenGL ES 1.0 constant. */
  public static int GL_BACK                                 = 0x0405;
  /** OpenGL ES 1.0 constant. */
  public static int GL_FRONT_AND_BACK                       = 0x0408;

  // EnableCap
  /** OpenGL ES 1.0 constant. */
  public static int GL_FOG                                  = 0x0B60;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHTING                             = 0x0B50;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE_2D                           = 0x0DE1;
  /** OpenGL ES 1.0 constant. */
  public static int GL_CULL_FACE                            = 0x0B44;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ALPHA_TEST                           = 0x0BC0;
  /** OpenGL ES 1.0 constant. */
  public static int GL_BLEND                                = 0x0BE2;
  /** OpenGL ES 1.0 constant. */
  public static int GL_COLOR_LOGIC_OP                       = 0x0BF2;
  /** OpenGL ES 1.0 constant. */
  public static int GL_DITHER                               = 0x0BD0;
  /** OpenGL ES 1.0 constant. */
  public static int GL_STENCIL_TEST                         = 0x0B90;
  /** OpenGL ES 1.0 constant. */
  public static int GL_DEPTH_TEST                           = 0x0B71;
  /** OpenGL ES 1.0 constant. */
  public static int GL_POINT_SMOOTH                         = 0x0B10;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LINE_SMOOTH                          = 0x0B20;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SCISSOR_TEST                         = 0x0C11;
  /** OpenGL ES 1.0 constant. */
  public static int GL_COLOR_MATERIAL                       = 0x0B57;
  /** OpenGL ES 1.0 constant. */
  public static int GL_NORMALIZE                            = 0x0BA1;
  /** OpenGL ES 1.0 constant. */
  public static int GL_RESCALE_NORMAL                       = 0x803A;
  /** OpenGL ES 1.0 constant. */
  public static int GL_POLYGON_OFFSET_FILL                  = 0x8037;
  /** OpenGL ES 1.0 constant. */
  public static int GL_VERTEX_ARRAY                         = 0x8074;
  /** OpenGL ES 1.0 constant. */
  public static int GL_NORMAL_ARRAY                         = 0x8075;
  /** OpenGL ES 1.0 constant. */
  public static int GL_COLOR_ARRAY                          = 0x8076;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE_COORD_ARRAY                  = 0x8078;
  /** OpenGL ES 1.0 constant. */
  public static int GL_MULTISAMPLE                          = 0x809D;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SAMPLE_ALPHA_TO_COVERAGE             = 0x809E;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SAMPLE_ALPHA_TO_ONE                  = 0x809F;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SAMPLE_COVERAGE                      = 0x80A0;

  // ErrorCode
  /** OpenGL ES 1.0 constant. */
  public static int GL_NO_ERROR                             = 0;
  /** OpenGL ES 1.0 constant. */
  public static int GL_INVALID_ENUM                         = 0x0500;
  /** OpenGL ES 1.0 constant. */
  public static int GL_INVALID_VALUE                        = 0x0501;
  /** OpenGL ES 1.0 constant. */
  public static int GL_INVALID_OPERATION                    = 0x0502;
  /** OpenGL ES 1.0 constant. */
  public static int GL_STACK_OVERFLOW                       = 0x0503;
  /** OpenGL ES 1.0 constant. */
  public static int GL_STACK_UNDERFLOW                      = 0x0504;
  /** OpenGL ES 1.0 constant. */
  public static int GL_OUT_OF_MEMORY                        = 0x0505;

  // FogMode
  /** OpenGL ES 1.0 constant. */
  public static int GL_EXP                                  = 0x0800;
  /** OpenGL ES 1.0 constant. */
  public static int GL_EXP2                                 = 0x0801;

  // FogParameter
  /** OpenGL ES 1.0 constant. */
  public static int GL_FOG_DENSITY                          = 0x0B62;
  /** OpenGL ES 1.0 constant. */
  public static int GL_FOG_START                            = 0x0B63;
  /** OpenGL ES 1.0 constant. */
  public static int GL_FOG_END                              = 0x0B64;
  /** OpenGL ES 1.0 constant. */
  public static int GL_FOG_MODE                             = 0x0B65;
  /** OpenGL ES 1.0 constant. */
  public static int GL_FOG_COLOR                            = 0x0B66;

  // FrontFaceDirection
  /** OpenGL ES 1.0 constant. */
  public static int GL_CW                                   = 0x0900;
  /** OpenGL ES 1.0 constant. */
  public static int GL_CCW                                  = 0x0901;

  // GetPName
  /** OpenGL ES 1.0 constant. */
  public static int GL_SMOOTH_POINT_SIZE_RANGE              = 0x0B12;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SMOOTH_LINE_WIDTH_RANGE              = 0x0B22;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ALIASED_POINT_SIZE_RANGE             = 0x846D;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ALIASED_LINE_WIDTH_RANGE             = 0x846E;
  /** OpenGL ES 1.0 constant. */
  public static int GL_IMPLEMENTATION_COLOR_READ_TYPE_OES   = 0x8B9A;
  /** OpenGL ES 1.0 constant. */
  public static int GL_IMPLEMENTATION_COLOR_READ_FORMAT_OES = 0x8B9B;
  /** OpenGL ES 1.0 constant. */
  public static int GL_MAX_LIGHTS                           = 0x0D31;
  /** OpenGL ES 1.0 constant. */
  public static int GL_MAX_TEXTURE_SIZE                     = 0x0D33;
  /** OpenGL ES 1.0 constant. */
  public static int GL_MAX_MODELVIEW_STACK_DEPTH            = 0x0D36;
  /** OpenGL ES 1.0 constant. */
  public static int GL_MAX_PROJECTION_STACK_DEPTH           = 0x0D38;
  /** OpenGL ES 1.0 constant. */
  public static int GL_MAX_TEXTURE_STACK_DEPTH              = 0x0D39;
  /** OpenGL ES 1.0 constant. */
  public static int GL_MAX_VIEWPORT_DIMS                    = 0x0D3A;
  /** OpenGL ES 1.0 constant. */
  public static int GL_MAX_ELEMENTS_VERTICES                = 0x80E8;
  /** OpenGL ES 1.0 constant. */
  public static int GL_MAX_ELEMENTS_INDICES                 = 0x80E9;
  /** OpenGL ES 1.0 constant. */
  public static int GL_MAX_TEXTURE_UNITS                    = 0x84E2;
  /** OpenGL ES 1.0 constant. */
  public static int GL_NUM_COMPRESSED_TEXTURE_FORMATS       = 0x86A2;
  /** OpenGL ES 1.0 constant. */
  public static int GL_COMPRESSED_TEXTURE_FORMATS           = 0x86A3;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SUBPIXEL_BITS                        = 0x0D50;
  /** OpenGL ES 1.0 constant. */
  public static int GL_RED_BITS                             = 0x0D52;
  /** OpenGL ES 1.0 constant. */
  public static int GL_GREEN_BITS                           = 0x0D53;
  /** OpenGL ES 1.0 constant. */
  public static int GL_BLUE_BITS                            = 0x0D54;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ALPHA_BITS                           = 0x0D55;
  /** OpenGL ES 1.0 constant. */
  public static int GL_DEPTH_BITS                           = 0x0D56;
  /** OpenGL ES 1.0 constant. */
  public static int GL_STENCIL_BITS                         = 0x0D57;

  // HintMode
  /** OpenGL ES 1.0 constant. */
  public static int GL_DONT_CARE                            = 0x1100;
  /** OpenGL ES 1.0 constant. */
  public static int GL_FASTEST                              = 0x1101;
  /** OpenGL ES 1.0 constant. */
  public static int GL_NICEST                               = 0x1102;

  // HintTarget
  /** OpenGL ES 1.0 constant. */
  public static int GL_PERSPECTIVE_CORRECTION_HINT          = 0x0C50;
  /** OpenGL ES 1.0 constant. */
  public static int GL_POINT_SMOOTH_HINT                    = 0x0C51;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LINE_SMOOTH_HINT                     = 0x0C52;
  /** OpenGL ES 1.0 constant. */
  public static int GL_POLYGON_SMOOTH_HINT                  = 0x0C53;
  /** OpenGL ES 1.0 constant. */
  public static int GL_FOG_HINT                             = 0x0C54;

  // LightModelParameter
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHT_MODEL_AMBIENT                  = 0x0B53;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHT_MODEL_TWO_SIDE                 = 0x0B52;
  /** OpenGL ES 1.0 constant. */

  // LightParameter
  /** OpenGL ES 1.0 constant. */
  public static int GL_AMBIENT                              = 0x1200;
  /** OpenGL ES 1.0 constant. */
  public static int GL_DIFFUSE                              = 0x1201;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SPECULAR                             = 0x1202;
  /** OpenGL ES 1.0 constant. */
  public static int GL_POSITION                             = 0x1203;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SPOT_DIRECTION                       = 0x1204;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SPOT_EXPONENT                        = 0x1205;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SPOT_CUTOFF                          = 0x1206;
  /** OpenGL ES 1.0 constant. */
  public static int GL_CONSTANT_ATTENUATION                 = 0x1207;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LINEAR_ATTENUATION                   = 0x1208;
  /** OpenGL ES 1.0 constant. */
  public static int GL_QUADRATIC_ATTENUATION                = 0x1209;

  // DataType
  /** OpenGL ES 1.0 constant. */
  public static int GL_BYTE                                 = 0x1400;
  /** OpenGL ES 1.0 constant. */
  public static int GL_UNSIGNED_BYTE                        = 0x1401;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SHORT                                = 0x1402;
  /** OpenGL ES 1.0 constant. */
  public static int GL_UNSIGNED_SHORT                       = 0x1403;
  /** OpenGL ES 1.0 constant. */
  public static int GL_FLOAT                                = 0x1406;
  /** OpenGL ES 1.0 constant. */
  public static int GL_FIXED                                = 0x140C;

  // LogicOp
  /** OpenGL ES 1.0 constant. */
  public static int GL_CLEAR                                = 0x1500;
  /** OpenGL ES 1.0 constant. */
  public static int GL_AND                                  = 0x1501;
  /** OpenGL ES 1.0 constant. */
  public static int GL_AND_REVERSE                          = 0x1502;
  /** OpenGL ES 1.0 constant. */
  public static int GL_COPY                                 = 0x1503;
  /** OpenGL ES 1.0 constant. */
  public static int GL_AND_INVERTED                         = 0x1504;
  /** OpenGL ES 1.0 constant. */
  public static int GL_NOOP                                 = 0x1505;
  /** OpenGL ES 1.0 constant. */
  public static int GL_XOR                                  = 0x1506;
  /** OpenGL ES 1.0 constant. */
  public static int GL_OR                                   = 0x1507;
  /** OpenGL ES 1.0 constant. */
  public static int GL_NOR                                  = 0x1508;
  /** OpenGL ES 1.0 constant. */
  public static int GL_EQUIV                                = 0x1509;
  /** OpenGL ES 1.0 constant. */
  public static int GL_INVERT                               = 0x150A;
  /** OpenGL ES 1.0 constant. */
  public static int GL_OR_REVERSE                           = 0x150B;
  /** OpenGL ES 1.0 constant. */
  public static int GL_COPY_INVERTED                        = 0x150C;
  /** OpenGL ES 1.0 constant. */
  public static int GL_OR_INVERTED                          = 0x150D;
  /** OpenGL ES 1.0 constant. */
  public static int GL_NAND                                 = 0x150E;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SET                                  = 0x150F;

  // MaterialParameters
  /** OpenGL ES 1.0 constant. */
  public static int GL_EMISSION                             = 0x1600;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SHININESS                            = 0x1601;
  /** OpenGL ES 1.0 constant. */
  public static int GL_AMBIENT_AND_DIFFUSE                  = 0x1602;

  // MatrixMode
  /** OpenGL ES 1.0 constant. */
  public static int GL_MODELVIEW                            = 0x1700;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PROJECTION                           = 0x1701;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE                              = 0x1702;

  // PixelFormat
  /** OpenGL ES 1.0 constant. */
  public static int GL_ALPHA                                = 0x1906;
  /** OpenGL ES 1.0 constant. */
  public static int GL_RGB                                  = 0x1907;
  /** OpenGL ES 1.0 constant. */
  public static int GL_RGBA                                 = 0x1908;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LUMINANCE                            = 0x1909;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LUMINANCE_ALPHA                      = 0x190A;

  // PixelStoreParameter
  /** OpenGL ES 1.0 constant. */
  public static int GL_UNPACK_ALIGNMENT                     = 0x0CF5;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PACK_ALIGNMENT                       = 0x0D05;

  // PixelType
  /** OpenGL ES 1.0 constant. */
  public static int GL_UNSIGNED_SHORT_4_4_4_4               = 0x8033;
  /** OpenGL ES 1.0 constant. */
  public static int GL_UNSIGNED_SHORT_5_5_5_1               = 0x8034;
  /** OpenGL ES 1.0 constant. */
  public static int GL_UNSIGNED_SHORT_5_6_5                 = 0x8363;

  // ShadingModel
  /** OpenGL ES 1.0 constant. */
  public static int GL_FLAT                                 = 0x1D00;
  /** OpenGL ES 1.0 constant. */
  public static int GL_SMOOTH                               = 0x1D01;

  // StencilOp
  /** OpenGL ES 1.0 constant. */
  public static int GL_KEEP                                 = 0x1E00;
  /** OpenGL ES 1.0 constant. */
  public static int GL_REPLACE                              = 0x1E01;
  /** OpenGL ES 1.0 constant. */
  public static int GL_INCR                                 = 0x1E02;
  /** OpenGL ES 1.0 constant. */
  public static int GL_DECR                                 = 0x1E03;

  // StringName
  /** OpenGL ES 1.0 constant. */
  public static int GL_VENDOR                               = 0x1F00;
  /** OpenGL ES 1.0 constant. */
  public static int GL_RENDERER                             = 0x1F01;
  /** OpenGL ES 1.0 constant. */
  public static int GL_VERSION                              = 0x1F02;
  /** OpenGL ES 1.0 constant. */
  public static int GL_EXTENSIONS                           = 0x1F03;

  // TextureEnvMode
  /** OpenGL ES 1.0 constant. */
  public static int GL_MODULATE                             = 0x2100;
  /** OpenGL ES 1.0 constant. */
  public static int GL_DECAL                                = 0x2101;
  /** OpenGL ES 1.0 constant. */
  public static int GL_ADD                                  = 0x0104;

  // TextureEnvParameter
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE_ENV_MODE                     = 0x2200;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE_ENV_COLOR                    = 0x2201;

  // TextureEnvTarget
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE_ENV                          = 0x2300;

  // TextureMagFilter
  /** OpenGL ES 1.0 constant. */
  public static int GL_NEAREST                              = 0x2600;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LINEAR                               = 0x2601;

  // TextureMinFilter
  /** OpenGL ES 1.0 constant. */
  public static int GL_NEAREST_MIPMAP_NEAREST               = 0x2700;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LINEAR_MIPMAP_NEAREST                = 0x2701;
  /** OpenGL ES 1.0 constant. */
  public static int GL_NEAREST_MIPMAP_LINEAR                = 0x2702;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LINEAR_MIPMAP_LINEAR                 = 0x2703;

  // TextureParameterName
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE_MAG_FILTER                   = 0x2800;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE_MIN_FILTER                   = 0x2801;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE_WRAP_S                       = 0x2802;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE_WRAP_T                       = 0x2803;

  // TextureUnit
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE0                             = 0x84C0;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE1                             = 0x84C1;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE2                             = 0x84C2;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE3                             = 0x84C3;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE4                             = 0x84C4;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE5                             = 0x84C5;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE6                             = 0x84C6;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE7                             = 0x84C7;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE8                             = 0x84C8;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE9                             = 0x84C9;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE10                            = 0x84CA;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE11                            = 0x84CB;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE12                            = 0x84CC;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE13                            = 0x84CD;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE14                            = 0x84CE;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE15                            = 0x84CF;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE16                            = 0x84D0;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE17                            = 0x84D1;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE18                            = 0x84D2;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE19                            = 0x84D3;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE20                            = 0x84D4;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE21                            = 0x84D5;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE22                            = 0x84D6;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE23                            = 0x84D7;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE24                            = 0x84D8;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE25                            = 0x84D9;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE26                            = 0x84DA;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE27                            = 0x84DB;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE28                            = 0x84DC;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE29                            = 0x84DD;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE30                            = 0x84DE;
  /** OpenGL ES 1.0 constant. */
  public static int GL_TEXTURE31                            = 0x84DF;

  // TextureWrapMode
  /** OpenGL ES 1.0 constant. */
  public static int GL_REPEAT                               = 0x2901;
  /** OpenGL ES 1.0 constant. */
  public static int GL_CLAMP_TO_EDGE                        = 0x812F;

  // PixelInternalFormat
  /** OpenGL ES 1.0 constant. */
  public static int GL_PALETTE4_RGB8_OES                    = 0x8B90;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PALETTE4_RGBA8_OES                   = 0x8B91;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PALETTE4_R5_G6_B5_OES                = 0x8B92;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PALETTE4_RGBA4_OES                   = 0x8B93;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PALETTE4_RGB5_A1_OES                 = 0x8B94;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PALETTE8_RGB8_OES                    = 0x8B95;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PALETTE8_RGBA8_OES                   = 0x8B96;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PALETTE8_R5_G6_B5_OES                = 0x8B97;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PALETTE8_RGBA4_OES                   = 0x8B98;
  /** OpenGL ES 1.0 constant. */
  public static int GL_PALETTE8_RGB5_A1_OES                 = 0x8B99;

  // LightName
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHT0                               = 0x4000;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHT1                               = 0x4001;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHT2                               = 0x4002;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHT3                               = 0x4003;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHT4                               = 0x4004;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHT5                               = 0x4005;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHT6                               = 0x4006;
  /** OpenGL ES 1.0 constant. */
  public static int GL_LIGHT7                               = 0x4007;

// Begin GL 1.1

  // ClipPlaneName

  /** OpenGL ES 1.1 constant. */
  public static int GL_CLIP_PLANE0                          = 0x3000;
  /** OpenGL ES 1.1 constant. */
  public static int GL_CLIP_PLANE1                          = 0x3001;
  /** OpenGL ES 1.1 constant. */
  public static int GL_CLIP_PLANE2                          = 0x3002;
  /** OpenGL ES 1.1 constant. */
  /** OpenGL ES 1.1 constant. */
  public static int GL_CLIP_PLANE3                          = 0x3003;
  /** OpenGL ES 1.1 constant. */
  public static int GL_CLIP_PLANE4                          = 0x3004;
  /** OpenGL ES 1.1 constant. */
  public static int GL_CLIP_PLANE5                          = 0x3005;

  // GetPName

  /** OpenGL ES 1.1 constant. */
  public static int GL_CURRENT_COLOR                        = 0x0B00;
  /** OpenGL ES 1.1 constant. */
  public static int GL_CURRENT_NORMAL                       = 0x0B02;
  /** OpenGL ES 1.1 constant. */
  public static int GL_CURRENT_TEXTURE_COORDS               = 0x0B03;
  /** OpenGL ES 1.1 constant. */
  public static int GL_POINT_SIZE                           = 0x0B11;
  /** OpenGL ES 1.1 constant. */
  public static int GL_POINT_SIZE_MIN                       = 0x8126;
  /** OpenGL ES 1.1 constant. */
  public static int GL_POINT_SIZE_MAX                       = 0x8127;
  /** OpenGL ES 1.1 constant. */
  public static int GL_POINT_FADE_THRESHOLD_SIZE            = 0x8128;
  /** OpenGL ES 1.1 constant. */
  public static int GL_POINT_DISTANCE_ATTENUATION           = 0x8129;
  /** OpenGL ES 1.1 constant. */
  public static int GL_LINE_WIDTH                           = 0x0B21;
  /** OpenGL ES 1.1 constant. */
  public static int GL_CULL_FACE_MODE                       = 0x0B45;
  /** OpenGL ES 1.1 constant. */
  public static int GL_FRONT_FACE                           = 0x0B46;
  /** OpenGL ES 1.1 constant. */
  public static int GL_SHADE_MODEL                          = 0x0B54;
  /** OpenGL ES 1.1 constant. */
  public static int GL_DEPTH_RANGE                          = 0x0B70;
  /** OpenGL ES 1.1 constant. */
  public static int GL_DEPTH_WRITEMASK                      = 0x0B72;
  /** OpenGL ES 1.1 constant. */
  public static int GL_DEPTH_CLEAR_VALUE                    = 0x0B73;
  /** OpenGL ES 1.1 constant. */
  public static int GL_DEPTH_FUNC                           = 0x0B74;
  /** OpenGL ES 1.1 constant. */
  public static int GL_STENCIL_CLEAR_VALUE                  = 0x0B91;
  /** OpenGL ES 1.1 constant. */
  public static int GL_STENCIL_FUNC                         = 0x0B92;
  /** OpenGL ES 1.1 constant. */
  public static int GL_STENCIL_VALUE_MASK                   = 0x0B93;
  /** OpenGL ES 1.1 constant. */
  public static int GL_STENCIL_FAIL                         = 0x0B94;
  /** OpenGL ES 1.1 constant. */
  public static int GL_STENCIL_PASS_DEPTH_FAIL              = 0x0B95;
  /** OpenGL ES 1.1 constant. */
  public static int GL_STENCIL_PASS_DEPTH_PASS              = 0x0B96;
  /** OpenGL ES 1.1 constant. */
  public static int GL_STENCIL_REF                          = 0x0B97;
  /** OpenGL ES 1.1 constant. */
  public static int GL_STENCIL_WRITEMASK                    = 0x0B98;
  /** OpenGL ES 1.1 constant. */
  public static int GL_MATRIX_MODE                          = 0x0BA0;
  /** OpenGL ES 1.1 constant. */
  public static int GL_VIEWPORT                             = 0x0BA2;
  /** OpenGL ES 1.1 constant. */
  public static int GL_MODELVIEW_STACK_DEPTH                = 0x0BA3;
  /** OpenGL ES 1.1 constant. */
  public static int GL_PROJECTION_STACK_DEPTH               = 0x0BA4;
  /** OpenGL ES 1.1 constant. */
  public static int GL_TEXTURE_STACK_DEPTH                  = 0x0BA5;
  /** OpenGL ES 1.1 constant. */
  public static int GL_MODELVIEW_MATRIX                     = 0x0BA6;
  /** OpenGL ES 1.1 constant. */
  public static int GL_PROJECTION_MATRIX                    = 0x0BA7;
  /** OpenGL ES 1.1 constant. */
  public static int GL_TEXTURE_MATRIX                       = 0x0BA8;
  /** OpenGL ES 1.1 constant. */
  public static int GL_ALPHA_TEST_FUNC                      = 0x0BC1;
  /** OpenGL ES 1.1 constant. */
  public static int GL_ALPHA_TEST_REF                       = 0x0BC2;
  /** OpenGL ES 1.1 constant. */
  public static int GL_BLEND_DST                            = 0x0BE0;
  /** OpenGL ES 1.1 constant. */
  public static int GL_BLEND_SRC                            = 0x0BE1;
  /** OpenGL ES 1.1 constant. */
  public static int GL_LOGIC_OP_MODE                        = 0x0BF0;
  /** OpenGL ES 1.1 constant. */
  public static int GL_SCISSOR_BOX                          = 0x0C10;
  /** OpenGL ES 1.1 constant. */
  public static int GL_COLOR_CLEAR_VALUE                    = 0x0C22;
  /** OpenGL ES 1.1 constant. */
  public static int GL_COLOR_WRITEMASK                      = 0x0C23;
  /** OpenGL ES 1.1 constant. */
  public static int GL_MAX_CLIP_PLANES                      = 0x0D32;
  /** OpenGL ES 1.1 constant. */
  public static int GL_POLYGON_OFFSET_UNITS                 = 0x2A00;
  /** OpenGL ES 1.1 constant. */
  public static int GL_POLYGON_OFFSET_FACTOR                = 0x8038;
  /** OpenGL ES 1.1 constant. */
  public static int GL_TEXTURE_BINDING_2D                   = 0x8069;
  /** OpenGL ES 1.1 constant. */
  public static int GL_VERTEX_ARRAY_SIZE                    = 0x807A;
  /** OpenGL ES 1.1 constant. */
  public static int GL_VERTEX_ARRAY_TYPE                    = 0x807B;
  /** OpenGL ES 1.1 constant. */
  public static int GL_VERTEX_ARRAY_STRIDE                  = 0x807C;
  /** OpenGL ES 1.1 constant. */
  public static int GL_NORMAL_ARRAY_TYPE                    = 0x807E;
  /** OpenGL ES 1.1 constant. */
  public static int GL_NORMAL_ARRAY_STRIDE                  = 0x807F;
  /** OpenGL ES 1.1 constant. */
  public static int GL_COLOR_ARRAY_SIZE                     = 0x8081;
  /** OpenGL ES 1.1 constant. */
  public static int GL_COLOR_ARRAY_TYPE                     = 0x8082;
  /** OpenGL ES 1.1 constant. */
  public static int GL_COLOR_ARRAY_STRIDE                   = 0x8083;
  /** OpenGL ES 1.1 constant. */
  public static int GL_TEXTURE_COORD_ARRAY_SIZE             = 0x8088;
  /** OpenGL ES 1.1 constant. */
  public static int GL_TEXTURE_COORD_ARRAY_TYPE             = 0x8089;
  /** OpenGL ES 1.1 constant. */
  public static int GL_TEXTURE_COORD_ARRAY_STRIDE           = 0x808A;

  /** OpenGL ES 1.1 constant. */
  public static int GL_VERTEX_ARRAY_POINTER                 = 0x808E;
  /** OpenGL ES 1.1 constant. */
  public static int GL_NORMAL_ARRAY_POINTER                 = 0x808F;
  /** OpenGL ES 1.1 constant. */
  public static int GL_COLOR_ARRAY_POINTER                  = 0x8090;
  /** OpenGL ES 1.1 constant. */
  public static int GL_TEXTURE_COORD_ARRAY_POINTER          = 0x8092;

  /** OpenGL ES 1.1 constant. */
  public static int GL_SAMPLE_BUFFERS                       = 0x80A8;
  /** OpenGL ES 1.1 constant. */
  public static int GL_SAMPLES                              = 0x80A9;
  /** OpenGL ES 1.1 constant. */
  public static int GL_SAMPLE_COVERAGE_VALUE                = 0x80AA;
  /** OpenGL ES 1.1 constant. */
  public static int GL_SAMPLE_COVERAGE_INVERT               = 0x80AB;

  // HintTarget

  /** OpenGL ES 1.1 constant. */
  public static int GL_GENERATE_MIPMAP_HINT                 = 0x8192;

  // TextureParameterName

  /** OpenGL ES 1.1 constant. */
  public static int GL_GENERATE_MIPMAP                      = 0x8191;

  // TextureUnit

  /** OpenGL ES 1.1 constant. */
  public static int GL_ACTIVE_TEXTURE                       = 0x84E0;
  /** OpenGL ES 1.1 constant. */
  public static int GL_CLIENT_ACTIVE_TEXTURE                = 0x84E1;

  // Buffer Objects

  /** OpenGL ES 1.1 constant. */
  public static int GL_ARRAY_BUFFER                         = 0x8892;
  /** OpenGL ES 1.1 constant. */
  public static int GL_ELEMENT_ARRAY_BUFFER                 = 0x8893;

  /** OpenGL ES 1.1 constant. */
  public static int GL_ARRAY_BUFFER_BINDING                 = 0x8894;
  /** OpenGL ES 1.1 constant. */
  public static int GL_ELEMENT_ARRAY_BUFFER_BINDING         = 0x8895;
  /** OpenGL ES 1.1 constant. */
  public static int GL_VERTEX_ARRAY_BUFFER_BINDING          = 0x8896;
  /** OpenGL ES 1.1 constant. */
  public static int GL_NORMAL_ARRAY_BUFFER_BINDING          = 0x8897;
  /** OpenGL ES 1.1 constant. */
  public static int GL_COLOR_ARRAY_BUFFER_BINDING           = 0x8898;
  /** OpenGL ES 1.1 constant. */
  public static int GL_TEXTURE_COORD_ARRAY_BUFFER_BINDING   = 0x889A;

  /** OpenGL ES 1.1 constant. */
  public static int GL_STATIC_DRAW                          = 0x88E4;
  /** OpenGL ES 1.1 constant. */
  public static int GL_DYNAMIC_DRAW                         = 0x88E8;

  /** OpenGL ES 1.1 constant. */
  public static int GL_WRITE_ONLY                           = 0x88B9;

  /** OpenGL ES 1.1 constant. */
  public static int GL_BUFFER_SIZE                          = 0x8764;
  /** OpenGL ES 1.1 constant. */
  public static int GL_BUFFER_USAGE                         = 0x8765;
  /** OpenGL ES 1.1 constant. */
  public static int GL_BUFFER_ACCESS                        = 0x88BB;

  // Texture combine + dot3

  /** OpenGL ES 1.1 constant. */
  public static int GL_SUBTRACT                             = 0x84E7;
  /** OpenGL ES 1.1 constant. */
  public static int GL_COMBINE                              = 0x8570;
  /** OpenGL ES 1.1 constant. */
  public static int GL_COMBINE_RGB                          = 0x8571;
  /** OpenGL ES 1.1 constant. */
  public static int GL_COMBINE_ALPHA                        = 0x8572;
  /** OpenGL ES 1.1 constant. */
  public static int GL_RGB_SCALE                            = 0x8573;
  /** OpenGL ES 1.1 constant. */
  public static int GL_ADD_SIGNED                           = 0x8574;
  /** OpenGL ES 1.1 constant. */
  public static int GL_INTERPOLATE                          = 0x8575;
  /** OpenGL ES 1.1 constant. */
  public static int GL_CONSTANT                             = 0x8576;
  /** OpenGL ES 1.1 constant. */
  public static int GL_PRIMARY_COLOR                        = 0x8577;
  /** OpenGL ES 1.1 constant. */
  public static int GL_PREVIOUS                             = 0x8578;
  /** OpenGL ES 1.1 constant. */
  public static int GL_OPERAND0_RGB                         = 0x8590;
  /** OpenGL ES 1.1 constant. */
  public static int GL_OPERAND1_RGB                         = 0x8591;
  /** OpenGL ES 1.1 constant. */
  public static int GL_OPERAND2_RGB                         = 0x8592;
  /** OpenGL ES 1.1 constant. */
  public static int GL_OPERAND0_ALPHA                       = 0x8598;
  /** OpenGL ES 1.1 constant. */
  public static int GL_OPERAND1_ALPHA                       = 0x8599;
  /** OpenGL ES 1.1 constant. */
  public static int GL_OPERAND2_ALPHA                       = 0x859A;

  /** OpenGL ES 1.1 constant. */
  public static int GL_ALPHA_SCALE                          = 0x0D1C;

  /** OpenGL ES 1.1 constant. */
  public static int GL_SRC0_RGB                             = 0x8580;
  /** OpenGL ES 1.1 constant. */
  public static int GL_SRC1_RGB                             = 0x8581;
  /** OpenGL ES 1.1 constant. */
  public static int GL_SRC2_RGB                             = 0x8582;
  /** OpenGL ES 1.1 constant. */
  public static int GL_SRC0_ALPHA                           = 0x8588;
  /** OpenGL ES 1.1 constant. */
  public static int GL_SRC1_ALPHA                           = 0x8589;
  /** OpenGL ES 1.1 constant. */
  public static int GL_SRC2_ALPHA                           = 0x858A;

  /** OpenGL ES 1.1 constant. */
  public static int GL_DOT3_RGB                             = 0x86AE;
  /** OpenGL ES 1.1 constant. */
  public static int GL_DOT3_RGBA                            = 0x86AF;

    private javax.microedition.khronos.opengles.GL11 jsr239_gl;

    /*FIXME: manage Light sources by ourselves*/
    //private boolean[] lights_enable = new boolean[8];

    public GL(javax.microedition.khronos.opengles.GL jsr239GL){
        jsr239_gl = (javax.microedition.khronos.opengles.GL11)jsr239GL;
    }

    public void glClearColor(float red, float green, float blue, float alpha){
        jsr239_gl.glClearColor(red, green, blue, alpha);
    }

    public void glPushMatrix() {
        jsr239_gl.glPushMatrix();
    }

    public void glPopMatrix() {
        jsr239_gl.glPopMatrix();
    }


    public void glViewport(int x, int y, int width, int height) {
        jsr239_gl.glViewport(x, y, width, height);
    }

    public void glMatrixMode(int mode) {
        jsr239_gl.glMatrixMode(mode);
    }

    public void glVertexPointer(int size, int type, int stride, Buffer ptr){
        jsr239_gl.glVertexPointer(size, type, stride, ptr);
    }

    public void glEnableClientState(int cap){
        jsr239_gl.glEnableClientState(cap);
    }

    public void glEnable(int cap) {
        jsr239_gl.glEnable(cap);
    }

    public void glNormalPointer(int type, int stride, Buffer ptr) {
        jsr239_gl.glNormalPointer(type ,stride, ptr);
    }

    public void glDisable(int cap) {
       jsr239_gl.glDisable(cap);
    }
   
    public void glDisableClientState(int cap) {
        jsr239_gl.glDisableClientState(cap);
    }

    public void glColorPointer(int size, int type, int stride, Buffer ptr) {
        jsr239_gl.glColorPointer(size, type, stride, ptr);
    }

    public void glActiveTexture(int mode) {
        jsr239_gl.glActiveTexture(mode);
    }

    public void glClientActiveTexture(int mode) {
        jsr239_gl.glClientActiveTexture(mode);
    }

    public void glTexCoordPointer(int size, int type, int stride, Buffer ptr){
        jsr239_gl.glTexCoordPointer(size, type, stride ,ptr);
    }

    public void glTranslatef(float x, float y, float z) {
        jsr239_gl.glTranslatef(x, y ,z);
    }

    public void glScalef(float x, float y, float z) {
        jsr239_gl.glScalef(x, y ,z);
    }

    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        jsr239_gl.glDrawElements(mode, count, type, indices);
    }

    public void glClear(int mask) {
        jsr239_gl.glClear(mask);
    }

    public void glLoadIdentity( ) {
        jsr239_gl.glLoadIdentity();
    }

    public void glBegin(int mode) {
        //jsr239_gl.glBegin(mode);
    }


    public void glVertex2f(float x, float y){

    }

    public void glEnd(){
        //jsr239_gl.glEnd();
    }

    public void glDepthFunc(int func){
        jsr239_gl.glDepthFunc(func);
    }

    public void glBlendEquation(int mode){

    }

    public void glDepthMask(boolean flag){
        jsr239_gl.glDepthMask(flag);
    }

    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha){
        jsr239_gl.glColorMask(red, green, blue, alpha);
    }

    public void glAlphaFunc(int func, float ref){
        jsr239_gl.glAlphaFunc(func, ref);
    }

    public void glBlendFunc(int sfactor, int dfactor){
        jsr239_gl.glBlendFunc(sfactor, dfactor);
    }
    
    public void glPolygonOffset(float factor, float units){
        jsr239_gl.glPolygonOffset(factor, units);
    }

    public void glFogi(int pname, int param){
        glFogf(pname, (float)param);
    }

    public void glFogfv(int pname, float[] params, int param_offset){
        jsr239_gl.glFogfv(pname, params, param_offset);
    }

    public void glFogf(int pname, float param){
        jsr239_gl.glFogf(pname, param); 
    }

    public void glLightModelf(int pname, float param){
        jsr239_gl.glLightModelf(pname, param);
    }
    
    public void glLightModelfv(int pname, float[] params, int offset){
        jsr239_gl.glLightModelfv(pname, params, offset);
    }
    
    public void glLightfv(int light, int pname, float[] params, int params_offset) {
        jsr239_gl.glLightfv(light, pname, params, params_offset);
    }

    public void glLightf(int light, int pname, float param){
        jsr239_gl.glLightf(light, pname, param);
    }

    public boolean glIsEnabled(int cap){
	return jsr239_gl.glIsEnabled(cap);
    }
   
    public void glMaterialfv(int face, int pname, float[] params, int params_offset){
        jsr239_gl.glMaterialfv(face, pname, params, params_offset);
    }

    public void glMaterialf(int face, int pname, float param){
        jsr239_gl.glMaterialf(face, pname, param);
    }

    public void glShadeModel(int mode){
        jsr239_gl.glShadeModel(mode);
    }

    public void glFrontFace(int mode){
        jsr239_gl.glFrontFace(mode);
    }

    public void glCullFace(int mode){
        jsr239_gl.glCullFace(mode);
    }

    public void glGetFloatv(int pname, float[] params, int params_offset){
        //OpenGL ES1.1 supports this
	jsr239_gl.glGetFloatv(pname, params, params_offset);
    }

    public void glGenTextures(int n, int[] textures, int texture_offset){
        jsr239_gl.glGenTextures(n, textures, texture_offset);
    }

    public void glBindTexture(int target, int texture){
        jsr239_gl.glBindTexture(target, texture);
    }

    public void glTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, Buffer pixels){
        jsr239_gl.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
    }

    public void glTexParameteri(int target, int pname, int param){
        //FIXME: force converting to float 
        jsr239_gl.glTexParameterf(target, pname, (float)param);
    }

    public void glTexEnvi(int target, int pname, int param){
        //FIXME: force converting to float
	jsr239_gl.glTexEnvf(target, pname, (float)param);
    }

    public void glTexEnvfv(int target, int pname ,float[] params, int params_offset){
        jsr239_gl.glTexEnvfv(target, pname, params, params_offset);
    }

    public void glLoadTransposeMatrixf(float[] m, int m_offset){
        float[] v = new float[m.length];
	System.arraycopy(m, 0, v, 0, m.length);
        //restore the transpose matrix to original
	//FIXME: assume containing only 16 elements
	for(int i=0,n=3; i < 16 && n > 0; i += 5, n--){
            for(int j = 1; j <= n; j++){
                float temp = v[i + j];
		v[i + j] = v[i + j + 3*j];
		v[i + j + 3*j] = temp;
	    }
	}
	int col = m_offset % 4;
	int row = m_offset / 4;
	int m_offset1 = col * 4 + row;
	jsr239_gl.glLoadMatrixf(v, m_offset1);
    }

    public void glDrawArrays(int mode, int first, int count) {
    	jsr239_gl.glDrawArrays(mode, first, count);
    }

    public void glMultTransposeMatrixf(float[] m, int m_offset){
        float[] v = new float[m.length];
	System.arraycopy(m, 0, v, 0, m.length);
        //restore the transpose matrix to original
	//FIXME: assume containing only 16 elements
	for(int i=0, n=3; i < 16 && n > 0; i += 5, n--){
            for(int j = 1; j <= n; j++){
                float temp = v[i + j];
		v[i + j] = v[i + j + 3*j];
		v[i + j + 3*j] = temp;
	    }
	}
	int col = m_offset % 4;
	int row = m_offset / 4;
	int m_offset1 = col * 4 + row;
        jsr239_gl.glMultMatrixf(v, m_offset1);
    }

}
