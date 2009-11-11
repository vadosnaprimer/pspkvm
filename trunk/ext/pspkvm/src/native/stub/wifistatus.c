#include <kni.h>
#include <sni.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>
#include <string.h>
#include <stdio.h>

/* Macro--since the code for string returns is mostly boilerplate */
/* null returned*/
#define NET_CONFIG_STR_RET(kdecl) \
KNIEXPORT KNI_RETURNTYPE_OBJECT \
KNIDECL(kdecl) { \
    char* r; \
    int re; \
    KNI_StartHandles(1); \
    KNI_DeclareHandle(ret); \
    KNI_EndHandlesAndReturnObject(ret); }

NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getProfileName)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getBSSID)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getSSID)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getIP)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getSubnetMask)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getGateway)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getPrimaryDNS)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getSecondaryDNS)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getProxyURL)

/* Macro for uint returns (return as int) */
/* Note that int comes back -1 if call fails, correct value otherwise */
#define NET_CONFIG_UINT_RET(kdecl) \
KNIEXPORT KNI_RETURNTYPE_INT \
KNIDECL(kdecl) { \
    KNI_ReturnInt(-1); }

NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getSecurityType)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getSignalStrength)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getChannel)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getPowerSave)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getUseProxy)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getProxyPort)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getEAPType)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getStartBrowser)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getUseWiFiSP)


KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_pspkvm_system_WifiStatus_isPowerOn) {
	KNI_ReturnBoolean(KNI_TRUE); }

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_pspkvm_system_WifiStatus_isSwitchOn) {
	KNI_ReturnBoolean(KNI_TRUE); }

KNIEXPORT KNI_RETURNTYPE_OBJECT 
KNIDECL(com_pspkvm_system_WifiStatus_getMACAddr) {
	KNI_StartHandles(1);
	KNI_DeclareHandle(ret);
	// 6 x 2 digits + 5 separators + terminator
	char retstr[18];
	// Native function requests 8, uses 6; no one knows why:
	unsigned char mac_addr[8];
	sprintf(retstr, "%02x:%02x:%02x:%02x:%02x:%02x",
		0,0,0,0,0,0);
	KNI_NewStringUTF(retstr, ret);
	KNI_EndHandlesAndReturnObject(ret); }
