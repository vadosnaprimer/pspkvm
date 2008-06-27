#include <kni.h>
#include <sni.h>
#include <midpMalloc.h>
#include <midpDataHash.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>

#include "javacall_devemu.h"
#include "javacall_keymap.h"

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_installer_DeviceDesc_getDevicesNumber0) {
    KNI_ReturnInt(javacall_devemu_get_devices_number());
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_midp_installer_DeviceDesc_getDeviceName0) {
    int index;    
    index = KNI_GetParameterAsInt(1);

    KNI_StartHandles(1);
    KNI_DeclareHandle(ret);

    int len;
    char* profile = javacall_devemu_get_device_name(index);
    if (profile && (len = strlen(profile)) > 0) {
        KNI_NewStringUTF(profile, ret);
    }

    KNI_EndHandlesAndReturnObject(ret);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_installer_DeviceDesc_getDeviceWidth0) {
    int index;
    
    index = KNI_GetParameterAsInt(1);

    KNI_ReturnInt(javacall_devemu_get_device_width(index));
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_installer_DeviceDesc_getDeviceHeight0) {
    int index;
    
    index = KNI_GetParameterAsInt(1);
    KNI_ReturnInt(javacall_devemu_get_device_height(index));
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_installer_DeviceDesc_setCurrentDevice0) {
    int index;
    
    index = KNI_GetParameterAsInt(1);
    javacall_devemu_set_current_device(index);
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_installer_DeviceDesc_getDeviceKeyCode0) {
    int index, key;
    
    index = KNI_GetParameterAsInt(1);
    key = KNI_GetParameterAsInt(2);
    KNI_ReturnInt(javacall_devemu_get_keycode(index, (javacall_key)key));
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_installer_DeviceDesc_getCurrentDevice0) {
    KNI_ReturnInt(javacall_devemu_get_current_device());
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_installer_DeviceDesc_setDefaultKeymap0) {
    javacall_keymap_setDefaultKeymap();
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_installer_DeviceDesc_resetKeymap0) {
    javacall_keymap_resetKeymap();
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_installer_DeviceDesc_setKeymap0) {
    int iJavakey, iNativekey;
    
    iJavakey = KNI_GetParameterAsInt(1);
    iNativekey = KNI_GetParameterAsInt(2);   
    
    javacall_keymap_set(iJavakey, iNativekey);
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_installer_DeviceDesc_getDefaultKeymap0) {
    int iJavakey;
    
    iJavakey = KNI_GetParameterAsInt(1);
    KNI_ReturnInt(javacall_keymap_getDefaultKey(iJavakey));
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_installer_DeviceDesc_getJavaKeyNumber0) {
    KNI_ReturnInt(javacall_keymap_javakey_number());
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_installer_DeviceDesc_devIdToDispId) {
    KNI_ReturnInt(javacall_devemu_get_displayID(KNI_GetParameterAsInt(1)));
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_installer_DeviceDesc_dispIdToDevId) {
    KNI_ReturnInt(javacall_devemu_get_deviceID(KNI_GetParameterAsInt(1)));
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_midp_installer_DeviceDesc_getDevicePropid0) {
    int index;    
    index = KNI_GetParameterAsInt(1);

    KNI_StartHandles(1);
    KNI_DeclareHandle(ret);

    int len;
    char* profile = javacall_devemu_get_device_pid(index);
    if (profile && (len = strlen(profile)) > 0) {
        KNI_NewStringUTF(profile, ret);
    }

    KNI_EndHandlesAndReturnObject(ret);
}

