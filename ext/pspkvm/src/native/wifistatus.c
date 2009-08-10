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

NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getProfileName,
	javacall_pspNetGetProfileName)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getBSSID,
	javacall_pspNetGetBSSIDStr)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getSSID,
	javacall_pspNetGetSSIDStr)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getIP,
	javacall_pspNetGetIP)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getSubnetMask,
	javacall_pspNetGetSubnetMask)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getGateway,
	javacall_pspNetGetGateway)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getPrimaryDNS,
	javacall_pspNetGetPrimaryDNS)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getSecondaryDNS,
	javacall_pspNetGetSecondaryDNS)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getProxyURL,
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

NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getSecurityType,
	javacall_pspNetGetSecurityType)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getSignalStrength,
	javacall_pspNetGetSignalStrength)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getChannel,
	javacall_pspNetGetChannel)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getPowerSave,
	javacall_pspNetGetPowerSave)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getUseProxy,
	javacall_pspNetGetUseProxy)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getProxyPort,
	javacall_pspNetGetProxyPort)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getEAPType,
	javacall_pspNetGetEAPType)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getStartBrowser,
	javacall_pspNetGetStartBrowser)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getUseWiFiSP,
	javacall_pspNetGetUseWiFiSP)
