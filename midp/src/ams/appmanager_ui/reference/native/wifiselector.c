#include <kni.h>
#include <sni.h>
#include <midpMalloc.h>
#include <midpDataHash.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>

#ifdef PSP
#include <javacall_network.h>
#endif

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_midp_appmanager_WifiSelector_lookupWifiProfile) {
    int index;
    
    index = KNI_GetParameterAsInt(1);
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(ret);

#ifdef PSP
    int len;
    char* profile = javacall_network_get_profile(index);
    if (profile && (len = strlen(profile)) > 0) {
        KNI_NewStringUTF(profile, ret);
    }
#endif

    KNI_EndHandlesAndReturnObject(ret);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_appmanager_WifiSelector_connect) {
    int index;
    int ret = -1;
    
    index = KNI_GetParameterAsInt(1);
    if (JAVACALL_OK == javacall_network_connect_profile(index)) {
    	 ret = 0;
    }
    
    KNI_ReturnInt(ret);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_appmanager_WifiSelector_getConnectState) {
    int state;
    if (JAVACALL_OK != javacall_network_connect_state(&state)) {
        state = -1;
    }
    KNI_ReturnInt(state);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_appmanager_WifiSelector_disconnect) {
    javacall_network_disconnect();
}

