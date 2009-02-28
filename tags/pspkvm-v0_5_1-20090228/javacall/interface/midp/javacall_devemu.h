#ifndef __JAVACALL_DEVEMU_H
#define __JAVACALL_DEVEMU_H



#ifdef __cplusplus
extern "C" {
#endif

#include "javacall_defs.h"
#include "javacall_keypress.h"

int javacall_devemu_get_devices_number();
char* javacall_devemu_get_device_name(int id);
int javacall_devemu_get_device_width(int id);
int javacall_devemu_get_device_height(int id);
int javacall_devemu_get_current_device();
void javacall_devemu_set_current_device(int id);
int javacall_devemu_get_keycode(int id, javacall_key javacallkey);
int javacall_devemu_get_rotation(int id);
int javacall_devemu_get_displayID(int devid);
int javacall_devemu_get_deviceID(int dispid);
char* javacall_devemu_get_device_pid(int id);


#ifdef __cplusplus
}
#endif

#endif  /* JAVACALL_DEVEMU_H */
