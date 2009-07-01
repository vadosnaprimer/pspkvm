#include "javacall_devemu.h"
#include "device.h"

#include <string.h>

#define STD_KEY_CLEAR -8
#define UNKNOW_KEY_VALUE 0

static DEVICE_INFO _devices[] = { 
	{"J2ME Standard(480*272)",
	480, 272, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 0, 0, "j2me-generic"},
	{"J2ME Standard(320*240)",
	320, 240, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 0, 1, "j2me-generic"},
	{"J2ME Standard(240*320)",
	240, 320, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 0, 2, "j2me-generic"},
	{"Nokia(240*320)",
	240, 320, -1, -2, -3, -4, -5, -6, -7, -8, 0, 3, "nokia-generic"},
	{"Nokia(320*240)",
	320, 240, -1, -2, -3, -4, -5, -6, -7, -8, 0, 4, "nokia-generic"},
	{"Nokia(480*272)",
	480, 272, -1, -2, -3, -4, -5, -6, -7, -8, 0, 5, "nokia-generic"},
	{"Motorola(240*320)",
	240, 320, -1, -6, -2, -5, -20, -21, -22, -9, 0, 6, "motorola-generic"},
	{"Motorola(320*240)",
	320, 240, -1, -6, -2, -5, -20, -21, -22, -9, 0, 7, "motorola-generic"},
	{"Motorola(480*272)",
	480, 272, -1, -6, -2, -5, -20, -21, -22, -9, 0, 8, "motorola-generic"},
	//{"SPRINT",
	//204, 272, -1, -2, -3, -4, -5, -8, -7, UNKNOW_KEY_VALUE},
	{"XBOX Demo",
	320, 240, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 0, 12, "XBOXDEMO"},

	//nokia
	{"Nokia S30(96*65)",
	96, 65, -1, -2, -3, -4, -5, -6, -7, -8, 0, 13, "nokia-s30"},
	//{"NOKIA 3510/3595",
	//96, 65, -1, -2, -3, -4, -5, -6, -7, -8},
	{"Nokia S40(128*128)",
	128, 128, -1, -2, -3, -4, -5, -6, -7, -8, 0, 14, "nokia-s40"},
	//{"NOKIA 3220/6610i/6030/6230/7250/7260",
	//128, 128, -1, -2, -3, -4, -5, -6, -7, -8},
	{"Nokia 6101(128*160)",//"NOKIA 6061/6101/6102/6102i",
	128, 160, -1, -2, -3, -4, -5, -6, -7, -8, 0, 15, "nokia-6101"},
	{"Nokia S60(176*208)",
	176, 208, -1, -2, -3, -4, -5, -6, -7, -8, 0, 16, "nokia-s60"},
	//{"NOKIA 6600/6630/6670/6680/7610/7650",
	//176, 208, -1, -2, -3, -4, -5, -6, -7, -8},
	{"Nokia 8801(208*208)",
	208, 208, -1, -2, -3, -4, -5, -6, -7, -8, 0, 17, "nokia-8801"},
	{"Nokia N73(240*320)",//"NOKIA 6270/6280/6282/N71/N73",
	240, 320, -1, -2, -3, -4, -5, -6, -7, -8, 0, 18, "nokia-n73"},

	//sonyericsson
	{"SonyEricsson K300(128*128)",//"SONYERICSSON K300a/J300i/J300a",
	128, 128, -1, -2, -3, -4, -5, -6, -7, -8, 0, 19, "se-k300"},
	{"SonyEricsson K500(128*160)",//"SONYERICSSON K500/K500i/K510i/Z500a/Z520i/Z525/F500i",
	128, 160, -1, -2, -3, -4, -5, -6, -7, -8, 0, 20, "se-k500"},
	{"SonyEricsson K700(176*220)",//,"SONYERICSSON K700/K700i/K750/K750i/V800/W810i/W600",
	176, 220, -1, -2, -3, -4, -5, -6, -7, -8, 0, 21, "se-k700"},
	{"SonyEricsson K800(240*320)",//"SONYERICSSON K800/K800i/S700/S700i/W900i",
	240, 320, -1, -2, -3, -4, -5, -6, -7, -8, 0, 22, "se-k800"},

	//motorola
	{"Motorola C650(128*116)",//"Motorola C650/V180",
	128, 116, -1, -6, -2, -5, -20, -21, -22, -9, 0, 23, "motorola-c650"},
	{"Motorola Triplets(176*204)",
	176, 204, -1, -6, -2, -5, -8, -21, -22, -9, 0, 24, "motorola-triplets"},
	{"Motorola E398(176*204)",//"Motorola E398/E790/V330/V360/V365/V550/V551/V557/V635/V980/SLVR L7/Royale V8/RAZR V3/RAZR V3i/ROKR E1/PEBL U6/V6",
	176, 204, -1, -6, -2, -5, -20, -21, -22, -9, 0, 25, "motorola-e398"},
	{"Motorola V600(176*204)",//"Motorola V300/V400/V600",
	176, 204, 1, 6, 2, 5, 20, 21, 22, UNKNOW_KEY_VALUE, 0, 26, "motorola-v600"},
	//{"MOTOROLA i860/V3/U6/V500/V545/V547",
	//176, 204, -1, -6, -2, -5, -8, -21, -22, -9},
	//{"MOTOROLA E1000",
	//240, 320, -1, -6, -2, -5, -8, -21, -22, -9},
	{"Motorola E1000(240*320)",
	240, 320, -1, -6, -2, -5, -20, -21, -22, -9, 0, 27, "motorola-e1000"},
	//{"Motorola V220",
	//128, 116, 1, 6, 2, 5, 20, 21, 22, UNKNOW_KEY_VALUE},
	//{"Motorola V262",
	//128, 116, -1, -2, -3, -4, -5, -7, -8, -9},
	//{"Motorola Capri",
	//176, 205, -1, -6, -2, -5, -20, -21, -22, -9},
	//{"Motorola L2/L6/V190/V270",
	//128, 149, -1, -6, -2, -5, -20, -21, -22, -9},
	//{"Motorola i670",
	//130, 130, -10, -11, -13, -12, -23, -20, -21, UNKNOW_KEY_VALUE},

	//samsung
	{"Samsung E700(128*144)",
	128, 144, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 0, 28, "samsung-e700"},
	{"Samsung A620(128*146)",
	128, 146, -1, -2, -3, -4, -5, -8, -7, UNKNOW_KEY_VALUE, 0, 29, "samsung-a620"},
	{"Samsung E360(128*160)",//"SAMSUNG SGH-E360/X640",
	128, 160, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 0, 30, "samsung-e360"},
	//{"SAMSUNG C417/SGH-D347/SGH-D357/SGH-D407/SGH-E315/SGH-E316/SGH-E317/SGH-T309/SGH-X427m/SGH-X507",
	//128, 160, -1, -2, -3, -4, -5, -6, -7, UNKNOW_KEY_VALUE},
	//{"SAMSUNG SGH-ZX10/SGH-ZX20",
	//176, 205, -1, -2, -3, -4, -5, -6, -7, UNKNOW_KEY_VALUE},
	{"Samsung D500(176*220)",//"SAMSUNG SGH-D500/D807/SCH-A950/SGH-P207/SGH-T719/T619/T709",
	176, 220, -1, -2, -3, -4, -5, -6, -7, UNKNOW_KEY_VALUE, 0, 31, "samsung-d500"},
	{"Samsung D600(240*320)",//"SAMSUNG SGH-D600/D800",
	240, 320, -1, -2, -3, -4, -5, -8, -7, UNKNOW_KEY_VALUE, 0, 32, "samsung-600"},

	//Siemens
	{"Siemens C65(130*130)",//"Siemens C65/CV65",
	130, 130, -59, -60, -61, -62, -26, -1, -4, UNKNOW_KEY_VALUE, 0, 33, "siemens-c65"},
	{"Siemens CX65(132*176)",
	132, 176, -59, -60, -61, -62, -26, -1, -4, UNKNOW_KEY_VALUE, 0, 34, "siemens-cx65"},

	//sagem
	{"Sagem myV55(128*142)",//"SAGEM myV55/myX5",
	128, 142, -1, -2, -3, -4, -5, -6, -7, -8, 0, 35, "sagem-myv55"},
	{"Sagem my600V(176*196)",
	176, 196, -1, -2, -3, -4, -5, -6, -7, -8, 0, 36, "sagem-my600v"},

	//sanyo
	{"Sanyo 8100(120*112)",
	120, 112, -1, -2, -3, -4, -5, -8, -7, UNKNOW_KEY_VALUE, 0, 37, "sanyo-8100"},
	{"Sanyo 2300(128*96)",//"SANYO 2300/4920/4930",
	128, 96, 1, 6, 2, 5, 8, 21, 22, UNKNOW_KEY_VALUE, 0, 38, "sanyo-2300"},
	{"Sanyo 5400(132*144)",//"SANYO 5400/5500/7300/8200",
	132, 144, -1, -2, -3, -4, -5, -8, -7, UNKNOW_KEY_VALUE, 0, 39, "sanyo-5400"},
	{"Sanyo 7400(176*220)",//"SANYO 7400/7500/8300",
	176, 220, -1, -2, -3, -4, -5, -8, -7, UNKNOW_KEY_VALUE, 0, 40, "sanyo-7400"},
	//{"SANYO 7400/7500/8300",
	//176, 220, -1, -2, -3, -4, -5, -8, -7, UNKNOW_KEY_VALUE},

	//Sharp
	{"Sharp GX10(120*130)",//"Sharp GX10/GX20/GX30",
	120, 130, 1, 6, 2, 5, 8, 21, 22, UNKNOW_KEY_VALUE, 0, 41, "sharp-gx10"},
	{"Sharp GX15(120*147)",
	120, 147, -1, -6, -2, -5, -20, -21, -22, -9, 0, 42, "sharp-gx15"},
	{"Sharp 705(240*294)",
	240, 294, -1, -6, -2, -5, -20, -21, -22, -9, 0, 43, "sharp-705"},
	{"Sharp 905(400*240)",
	400, 240, -1, -6, -2, -5, -20, -21, -22, -9, 0, 44, "sharp-905"},

	//LG
	{"LG C2000(128*160)",//"LG C2000/F7200",
	128, 160, -1, -2, -3, -4, -5, -202, -203, UNKNOW_KEY_VALUE, 0, 45, "LG-c2000"},
	{"LG VX8100(176*203)",
	176, 203, -1, -2, -3, -4, 8, -20, -21, UNKNOW_KEY_VALUE, 0, 47, "LG-vx8100"},
	{"LG CU400(176*220)",
	176, 220, -1, -2, -3, -4, -5, -6, -7, -8, 0, 46, "LG-cu400"},

	//Audiovox
	{"Audiovox CDM-8450(128*96)",
	128, 96, -1, -2, -3, -4, -5, -6, -7, -8, 0, 48, "audiovox-8450"},
	{"Audiovox CDM-8615(128*132)",//"Audiovox CDM-8615/PM-8920",
	128, 132, -1, -2, -3, -4, -5, -6, -7, -8, 0, 49, "audiovox-8615"},
	{"Audiovox CDM-8900(128*132)",
	128, 132, -10, -11, -12, -13, -5, -20, -21, UNKNOW_KEY_VALUE, 0, 50, "audiovox-8900"},
	{"Audiovox CDM-8910(128*145)",
	128, 145, -1, -2, -3, -4, -5, -6, -7, -8, 0, 51, "audiovox-8910"},

	//Kyocera
	{"Kyocera K10(104*68)",
	104, 68, -1, -2, -3, -4, -5, -5, -16, STD_KEY_CLEAR, 0, 52, "kyocera-k10"},
	{"Kyocera K24(128*148)",
	128, 148, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 0, 53, "kyocera-k24"},
	{"Kyocera KX21(160*116)",
	160, 116, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 0, 54, "kyocera-kx21"},
	{"Kyocera KX5(176*204)",
	176, 204, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 0, 55, "kyocera-kx5"},

	//HTC
	{"HTC Star Trek(240*266)",
	240, 266, -57377, -57378, -57379, -57380, 13, 57345, 57346, UNKNOW_KEY_VALUE, 0, 56, "HTC-generic"},

	// CW90
	{"J2ME Standard(320*240, CW90)",
	240, 320, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 90, 57, "j2me-generic"},
	{"J2ME Standard(480*272, CW90)",
	272, 480, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 90, 58, "j2me-generic"},
	{"Nokia(320*240, CW90)",
	240, 320, -1, -2, -3, -4, -5, -6, -7, -8, 90, 59, "nokia-generic"},
	{"Nokia(480*272, CW90)",
	272, 480, -1, -2, -3, -4, -5, -6, -7, -8, 90, 60, "nokia-generic"},
	{"Motorola(320*240), CW90",
	240, 320, -1, -6, -2, -5, -20, -21, -22, -9, 90, 61, "motorola-generic"},
	{"Motorola(480*272), CW90",
	272, 480, -1, -6, -2, -5, -20, -21, -22, -9, 90, 62, "motorola-generic"},	
	{"SonyEricsson (240*320)",
	240, 320, -1, -2, -3, -4, -5, -6, -7, -8, 0, 9, "se-generic"},
	{"SonyEricsson (320*240)",
	320, 240, -1, -2, -3, -4, -5, -6, -7, -8, 0, 10, "se-generic"},
	{"SonyEricsson (480*272)",
	480, 272, -1, -2, -3, -4, -5, -6, -7, -8, 0, 11, "se-generic"},
	{"SonyEricsson (320*240, CW90)",
	240, 320, -1, -2, -3, -4, -5, -6, -7, -8, 90, 63, "se-generic"},
	{"SonyEricsson (480*272, CW90)",
	272, 480, -1, -2, -3, -4, -5, -6, -7, -8, 90, 64, "se-generic"},
	{"J2ME Standard(352*416)",
	352, 416, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 0, 65, "j2me-generic"},
	{"J2ME Standard(352*416, CW90)",
	352, 416, -1, -2, -3, -4, -5, 21, 22, STD_KEY_CLEAR, 90, 66, "j2me-generic"},
	{"Nokia(352*416)",
	352, 416, -1, -2, -3, -4, -5, -6, -7, -8, 0, 67, "nokia-generic"},
	{"Nokia(352*416, CW90)",
	352, 416, -1, -2, -3, -4, -5, -6, -7, -8, 90, 68, "nokia-generic"},
	{"Motorola(352*416)",
	352, 416, -1, -6, -2, -5, -20, -21, -22, -9, 0, 69, "motorola-generic"},
	{"Motorola(352*416, CW90)",
	352, 416, -1, -6, -2, -5, -20, -21, -22, -9, 90, 70, "motorola-generic"},
	{"SonyEricsson (352*416)",
	352, 416, -1, -2, -3, -4, -5, -6, -7, -8, 0, 71, "se-generic"},
	{"SonyEricsson (352*416, CW90)",
	352, 416, -1, -2, -3, -4, -5, -6, -7, -8, 90, 72, "se-generic"},
};

static int DEVICE_COUNT = sizeof(_devices)/sizeof(DEVICE_INFO);
static DEVICE_INFO * PHONE = &_devices[19];
static int current_device = -1;

static int SelectDevice(char * device_name) {
	int n = 0;
	for (; n < DEVICE_COUNT; ++n)
	{
		if (stricmp(_devices[n].DEVICE_NAME, device_name) == 0) {
			PHONE = &_devices[n];
			return n;
		}
	}
	return -1;
}


int javacall_devemu_get_devices_number() {
    return DEVICE_COUNT;
}

char* javacall_devemu_get_device_name(int id) {
    if (id < 0 || id >= DEVICE_COUNT) {
        return NULL;
    }
    return _devices[id].DEVICE_NAME;
}

int javacall_devemu_get_device_width(int id) {
    if (id < 0 || id >= DEVICE_COUNT) {
        return 480;
    }
    return _devices[id].SCREEN_WIDTH;
}

int javacall_devemu_get_device_height(int id) {
    if (id < 0 || id >= DEVICE_COUNT) {
        return 272;
    }
    return _devices[id].SCREEN_HEIGHT;
}

int javacall_devemu_get_current_device() {
    return current_device;
}

void javacall_devemu_set_current_device(int id) {
    current_device = id;
}

int javacall_devemu_get_keycode(int id, javacall_key javacallkey) {
    //printf("javacall_devemu_get_keycode: %d, %d\n", id, javacallkey);
    if (id < 0 || id >= DEVICE_COUNT) {
        return javacallkey;
    }
    
    switch (javacallkey) {
    	case JAVACALL_KEY_UP:
    		return _devices[id].KEY_UP;
    	case JAVACALL_KEY_DOWN:
    		return _devices[id].KEY_DOWN;
    	case JAVACALL_KEY_LEFT:
    		return _devices[id].KEY_LEFT;
    	case JAVACALL_KEY_RIGHT:
    		return _devices[id].KEY_RIGHT;
    	case JAVACALL_KEY_SOFT1:
    		return _devices[id].KEY_SOFT1;
    	case JAVACALL_KEY_SOFT2:
    		return _devices[id].KEY_SOFT2;
    	case JAVACALL_KEY_CLEAR:
    		return _devices[id].KEY_CLEAR;
    	case JAVACALL_KEY_SELECT:
    		return _devices[id].KEY_SELECT;
    	default:
    		return javacallkey;
    }
}

int javacall_devemu_get_rotation(int id) {
    if (id < 0 || id >= DEVICE_COUNT) {
        return 0;
    }
    return _devices[id].ROTATE_ANGLE;
}

int javacall_devemu_get_displayID(int devid) {
    if (devid < 0 || devid >= DEVICE_COUNT) {
        return -1;
    }
    return _devices[devid].DISPLAY_ID;
}

int javacall_devemu_get_deviceID(int dispid) {
    	int n = 0;
    	for (; n < DEVICE_COUNT; ++n)
	{
		if (_devices[n].DISPLAY_ID == dispid) {
			return n;
		}
	}
	return -1;

}

char* javacall_devemu_get_device_pid(int id) {
    if (id < 0 || id >= DEVICE_COUNT) {
        return NULL;
    }
    
    return _devices[id].DEVICE_PROPERTY_ID;
}

