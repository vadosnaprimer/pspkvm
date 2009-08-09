#include <kni.h>
#include <sni.h>
#include <midpMalloc.h>
#include <midpDataHash.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>
#include <string.h>

#ifdef PSP
#include <javacall_network.h>
#include <javacall_wifi_support.h>
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
#ifdef PSP
    
    index = KNI_GetParameterAsInt(1);
    if (JAVACALL_OK == javacall_network_connect_profile(index)) {
    	 ret = 0;
    }
#endif    
    KNI_ReturnInt(ret);
}

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_appmanager_WifiSelector_getConnectState) {
    int state = -1;
#ifdef PSP

    if (JAVACALL_OK != javacall_network_connect_state(&state)) {
        state = -1;
    }
#endif
    KNI_ReturnInt(state);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_appmanager_WifiSelector_disconnect) {
#ifdef PSP
    javacall_network_disconnect();
#endif
}

/* Access to all of the status stuff added in wifi_support.c */

/* Macro--since the code for string returns is mostly boilerplate */
/* null returned if javacall call fails */
#ifdef PSP
#define NET_CONFIG_STR_RET(kdecl, natvcall) \
KNIEXPORT KNI_RETURNTYPE_OBJECT \
KNIDECL(kdecl) { \
    char* r; \
    int re; \
    KNI_StartHandles(1); \
    KNI_DeclareHandle(ret); \
    re = natvcall(&r); \
    if ((re==JAVACALL_OK) && (strlen(r) > 0)) { \
        KNI_NewStringUTF(r, ret); } \
    KNI_EndHandlesAndReturnObject(ret); }
#else
#define NET_CONFIG_STR_RET(kdecl, natvcall) \
KNIEXPORT KNI_RETURNTYPE_OBJECT \
KNIDECL(kdecl) { \
    KNI_StartHandles(1); \
    KNI_DeclareHandle(ret); \
    KNI_EndHandlesAndReturnObject(ret); }
#endif    

NET_CONFIG_STR_RET(com_sun_midp_appmanager_WifiSelector_getProfileName,
	javacall_pspNetGetProfileName)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_WifiSelector_getBSSID,
	javacall_pspNetGetBSSIDStr)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_WifiSelector_getSSID,
	javacall_pspNetGetSSIDStr)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_WifiSelector_getIP,
	javacall_pspNetGetIP)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_WifiSelector_getSubnetMask,
	javacall_pspNetGetSubnetMask)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_WifiSelector_getGateway,
	javacall_pspNetGetGateway)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_WifiSelector_getPrimaryDNS,
	javacall_pspNetGetPrimaryDNS)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_WifiSelector_getSecondaryDNS,
	javacall_pspNetGetSecondaryDNS)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_WifiSelector_getProxyURL,
	javacall_pspNetGetProxyURL)

/* Macro for uint returns (return as int) */
/* Note that int comes back -1 if call fails, correct value otherwise */
#ifdef PSP
#define NET_CONFIG_UINT_RET(kdecl, natvcall) \
KNIEXPORT KNI_RETURNTYPE_INT \
KNIDECL(kdecl) { \
    unsigned int i; \
    int r = -1; \
    if (natvcall(&i)==JAVACALL_OK) { \
    	 r=(int)i; } \
    KNI_ReturnInt(r); }
#else
#define NET_CONFIG_UINT_RET(kdecl, natvcall) \
KNIEXPORT KNI_RETURNTYPE_INT \
KNIDECL(kdecl) { \
    KNI_ReturnInt(-1); }
#endif

NET_CONFIG_UINT_RET(com_sun_midp_appmanager_WifiSelector_getSecurityType,
	javacall_pspNetGetSecurityType)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_WifiSelector_getSignalStrength,
	javacall_pspNetGetSignalStrength)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_WifiSelector_getChannel,
	javacall_pspNetGetChannel)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_WifiSelector_getPowerSave,
	javacall_pspNetGetPowerSave)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_WifiSelector_getUseProxy,
	javacall_pspNetGetUseProxy)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_WifiSelector_getProxyPort,
	javacall_pspNetGetProxyPort)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_WifiSelector_getEAPType,
	javacall_pspNetGetEAPType)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_WifiSelector_getStartBrowser,
	javacall_pspNetGetStartBrowser)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_WifiSelector_getUseWiFiSP,
	javacall_pspNetGetUseWiFiSP)
