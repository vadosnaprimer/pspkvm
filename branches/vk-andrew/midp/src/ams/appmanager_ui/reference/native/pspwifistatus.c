#include <kni.h>
#include <sni.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>
#include <string.h>

#ifdef PSP
#include <javacall_wifi_support.h>
#endif

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

NET_CONFIG_STR_RET(com_sun_midp_appmanager_PSPWifiStatus_getProfileName,
	javacall_pspNetGetProfileName)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_PSPWifiStatus_getBSSID,
	javacall_pspNetGetBSSIDStr)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_PSPWifiStatus_getSSID,
	javacall_pspNetGetSSIDStr)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_PSPWifiStatus_getIP,
	javacall_pspNetGetIP)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_PSPWifiStatus_getSubnetMask,
	javacall_pspNetGetSubnetMask)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_PSPWifiStatus_getGateway,
	javacall_pspNetGetGateway)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_PSPWifiStatus_getPrimaryDNS,
	javacall_pspNetGetPrimaryDNS)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_PSPWifiStatus_getSecondaryDNS,
	javacall_pspNetGetSecondaryDNS)
NET_CONFIG_STR_RET(com_sun_midp_appmanager_PSPWifiStatus_getProxyURL,
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

NET_CONFIG_UINT_RET(com_sun_midp_appmanager_PSPWifiStatus_getSecurityType,
	javacall_pspNetGetSecurityType)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_PSPWifiStatus_getSignalStrength,
	javacall_pspNetGetSignalStrength)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_PSPWifiStatus_getChannel,
	javacall_pspNetGetChannel)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_PSPWifiStatus_getPowerSave,
	javacall_pspNetGetPowerSave)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_PSPWifiStatus_getUseProxy,
	javacall_pspNetGetUseProxy)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_PSPWifiStatus_getProxyPort,
	javacall_pspNetGetProxyPort)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_PSPWifiStatus_getEAPType,
	javacall_pspNetGetEAPType)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_PSPWifiStatus_getStartBrowser,
	javacall_pspNetGetStartBrowser)
NET_CONFIG_UINT_RET(com_sun_midp_appmanager_PSPWifiStatus_getUseWiFiSP,
	javacall_pspNetGetUseWiFiSP)
