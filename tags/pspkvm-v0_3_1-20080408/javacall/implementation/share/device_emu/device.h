#ifndef _DEVICE_H_
#define _DEVICE_H_

typedef struct {
	char *	DEVICE_NAME;
	int		SCREEN_WIDTH;
	int		SCREEN_HEIGHT;
	int		KEY_UP;
	int		KEY_DOWN;
	int		KEY_LEFT;
	int		KEY_RIGHT;
	int		KEY_SELECT;
	int		KEY_SOFT1;
	int		KEY_SOFT2;
	int		KEY_CLEAR;
	int		ROTATE_ANGLE;
} DEVICE_INFO;
#endif
