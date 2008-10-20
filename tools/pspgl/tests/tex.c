#include <stdlib.h>
#include <GL/glut.h>

//#define LOGME 1
#include "glchk.h"

static void dumpmat(GLenum mat, const char *s)
{
	float m[16];

	glGetFloatv(mat, m);
	psp_log("%s = \n"
		"  [ %f %f %f %f\n"
		"    %f %f %f %f\n"
		"    %f %f %f %f\n"
		"    %f %f %f %f ]\n",
		s,
		m[0], m[4], m[8], m[12],
		m[1], m[5], m[9], m[13],
		m[2], m[6], m[10], m[14],
		m[3], m[7], m[11], m[15]);
}

static
void reshape (int w, int h)
{
	GLCHK(glViewport(0, 0, w, h));
	GLCHK(glMatrixMode(GL_PROJECTION));
	dumpmat(GL_PROJECTION_MATRIX, "fresh proj");
	GLCHK(glLoadIdentity());
	dumpmat(GL_PROJECTION_MATRIX, "ident proj");
	GLCHK(glOrtho(-2*1.7, 2*1.7, 2, -2, -2, 2));
	dumpmat(GL_PROJECTION_MATRIX, "ortho proj");

	GLCHK(glMatrixMode(GL_MODELVIEW));
	GLCHK(glLoadIdentity());
	dumpmat(GL_MODELVIEW_MATRIX, "ident modelview");
	dumpmat(GL_PROJECTION_MATRIX, "non-current proj");

	gluLookAt(0.0f, 0.0f, 2.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
	dumpmat(GL_MODELVIEW_MATRIX, "lookat modelview");
	glLoadIdentity();
}


static float delta = 1.0;

#define NTEX	7
static GLuint texid[NTEX];

extern unsigned char firefox_start[];
extern unsigned char logo_start[];

static
void display (void)
{
	int i;
	static GLfloat angle;
	angle += delta;

	GLCHK(glShadeModel(GL_SMOOTH));

	GLCHK(glClear(GL_COLOR_BUFFER_BIT));

	GLCHK(glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE));
	GLCHK(glEnable(GL_BLEND));
	GLCHK(glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));

	for(i = 0; i < NTEX; i++) {
		int x = i % 4;
		int y = i / 4;

		GLCHK(glMatrixMode(GL_MODELVIEW));
		GLCHK(glLoadIdentity());
		GLCHK(glTranslatef(1. * (x - 2), 1. * (y - .5), 0));
		//dumpmat(GL_MODELVIEW_MATRIX, "trans modelview");
		GLCHK(glRotatef(angle * 1.32f, 0.0f, 0.0f, 1.0f));
		dumpmat(GL_MODELVIEW_MATRIX, "rot modelview");

		GLCHK(glBindTexture(GL_TEXTURE_2D, texid[i]));

		glBegin(GL_TRIANGLE_FAN);
		  glColor3f(1, 0, 0);
		  glTexCoord2f(0, 0);
		  glVertex3f(0, 0, 0);

		  glColor3f(0, 1 ,0);
		  glTexCoord2f(0, 1);
		  glVertex3f(0, 1, 0);

		  glColor3f(0, 0, 1);
		  glTexCoord2f(1, 1);
		  glVertex3f(1, 1, 0);

		  glColor3f(1, 1, 1);
		  glTexCoord2f(1, 0);
		  glVertex3f(1, 0, 0);
		GLCHK(glEnd());
	}
	glutSwapBuffers();
	glutPostRedisplay();
#if SYS
	usleep(1000000/30);
#endif
}


static
void keydown (unsigned char key, int x, int y)
{
	switch (key) {
	case 'd':			/* delta, triangle */
		break;
	case 'o':			/* round */
		delta = 0.0f;
		break;
	case 'q':			/* square*/
		break;
	case 'x':			/* cross button */
		exit(0);
		break;
	default:
		;
	}
}


static
void keyup (unsigned char key, int x, int y)
{
	switch (key) {
	case 'o':
		delta = 1.0f;
		break;
	default:
		;
	}
}


static
void joystick (unsigned int buttonMask, int x, int y, int z)
{
	GLCHK(glClearColor(x * 1.0f/2000.0f + 0.5f, y * 1.0f/2000.0f + 0.5f, 1.0f, 1.0f));
}

/*
   F E D C B A 9 8|7 6 5 4 3 2 1 0
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |R R R R R|G G G G G G|B B B B B| 565
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |R R R R R|G G G G G|B B B B B|A| 5551
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |R R R R|G G G G|B B B B|A A A A| 4444
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
#define RGB565(r,g,b)		((((r) << 8) & 0xf800) | (((g) << 3) & 0x07E0) | (((b) >> 3) & 0x001f))
#define RGBA5551(r,g,b,a)	((((r) << 8) & 0xf800) | (((g) << 3) & 0x07C0) | (((b) >> 2) & 0x003e) | (((a) >> 7) & 0x0001))
#define RGBA4444(r,g,b,a)	((((r) << 8) & 0xf000) | (((g) << 4) & 0x0f00) | (((b)     ) & 0x00f0) | (((a) >> 4) & 0x000f))

int main(int argc, char* argv[])
{
	int i;
	unsigned short tex16[64*64];
	unsigned char tex24[64*64*3];

	psp_log("main starting\n");

	glutInit(&argc, argv);
        glutInitDisplayMode(GLUT_RGB | GLUT_DOUBLE | GLUT_DEPTH);
        glutInitWindowSize(480, 272);
	glutCreateWindow( __FILE__ );
	glutKeyboardFunc(keydown);
	glutKeyboardUpFunc(keyup);
	glutJoystickFunc(joystick, 0);
	glutReshapeFunc(reshape);
	glutDisplayFunc(display);

	psp_log("generating %d textures\n", NTEX);

	GLCHK(glGenTextures(NTEX, texid));

	psp_log("binding to %d\n", texid[4]);

	GLCHK(glBindTexture(GL_TEXTURE_2D, texid[4]));

	psp_log("uploading texture data for %d\n", texid[4]);
	GLCHK(glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 64, 64, 0, GL_RGBA, GL_UNSIGNED_BYTE, firefox_start));

	psp_log("setting linear filtering for %d\n", texid[4]);
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR));

	for(i = 0; i < 64*64; i++)
		tex16[i] = RGB565(firefox_start[i*4+0],
				  firefox_start[i*4+1],
				  firefox_start[i*4+2]);
	GLCHK(glBindTexture(GL_TEXTURE_2D, texid[2]));
	GLCHK(glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 64, 64, 0, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, tex16));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR));

	for(i = 0; i < 64*64; i++)
		tex16[i] = RGBA5551(firefox_start[i*4+0],
				    firefox_start[i*4+1],
				    firefox_start[i*4+2],
				    firefox_start[i*4+3]);
	GLCHK(glBindTexture(GL_TEXTURE_2D, texid[5]));
	GLCHK(glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 64, 64, 0, GL_RGBA, GL_UNSIGNED_SHORT_5_5_5_1, tex16));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR));

	GLCHK(glBindTexture(GL_TEXTURE_2D, texid[1]));
	GLCHK(glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 64, 64, 0, GL_RGB, GL_UNSIGNED_SHORT_5_5_5_1, tex16));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR));

	for(i = 0; i < 64*64; i++)
		tex16[i] = RGBA4444(firefox_start[i*4+0],
				    firefox_start[i*4+1],
				    firefox_start[i*4+2],
				    firefox_start[i*4+3]);
	GLCHK(glBindTexture(GL_TEXTURE_2D, texid[6]));
	GLCHK(glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 64, 64, 0, GL_RGBA, GL_UNSIGNED_SHORT_4_4_4_4, tex16));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR));

	GLCHK(glBindTexture(GL_TEXTURE_2D, texid[3]));
	GLCHK(glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 64, 64, 0, GL_RGB, GL_UNSIGNED_SHORT_4_4_4_4, tex16));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR));

	for(i = 0; i < 64*64; i++) {
		tex24[i*3+0] = firefox_start[i*4+0];
		tex24[i*3+1] = firefox_start[i*4+1];
		tex24[i*3+2] = firefox_start[i*4+2];
	}
	GLCHK(glBindTexture(GL_TEXTURE_2D, texid[0]));
	GLCHK(glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 64, 64, 0, GL_RGB, GL_UNSIGNED_BYTE, tex24));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR));
	GLCHK(glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR));

	GLCHK(glEnable(GL_TEXTURE_2D));

	glutMainLoop();
	return 0;
}
