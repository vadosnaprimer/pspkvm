#include <kni.h>
#include <sni.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>
#include <string.h>
#include <stdio.h>

#include "wifistatus.h"

// Native support for the com.pspkvm.system.WifiStatus extension.

// Pretty place to put all this stuff, for each call.
// Note that while this may not seem especially multithread-
// compatible, the underlying states are global anyway.
struct pspNetConnectionState crt_state;

// Note that for unsigned char, unsigned short, we just return as unsigned int--
// makes for a simpler, more uniform interface at the KNI_Return... level.
// Note also: for *ALL* of the pspNet calls, a zero return indicates okay.
// The KNI_ calls, on the other hand, if they return an int, return -1 for error,
// as all the valid values are positive. String KNI_ calls return null on error.

#define PSPCALL_OK 0

inline int pspNetGetProfileName(char** r) {
	*r = crt_state.name;
	crt_state.name[64]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_PROFILE_NAME, crt_state.name); }
	
inline int pspNetGetBSSIDStr(char** r) {
	*r = crt_state.bssid_str;
	strcpy(crt_state.bssid_str, "");
	int re = sceNetApctlGetInfo(PSP_CONSTATE_BSSID, crt_state.bssid);
	if (re!=0) {
		return re; }
	sprintf(crt_state.bssid_str, "%02x:%02x:%02x:%02x:%02x:%02x",
		crt_state.bssid[0], crt_state.bssid[1],
		crt_state.bssid[2], crt_state.bssid[3], 
		crt_state.bssid[4], crt_state.bssid[5]);
	return PSPCALL_OK; } 

inline int pspNetGetSSIDStr(char** r) {
	*r = crt_state.ssid;
	strcpy(crt_state.ssid, "");	
	int re = sceNetApctlGetInfo(PSP_CONSTATE_SSID, crt_state.ssid);
	if (re!=0) {
		return re; }
	re = sceNetApctlGetInfo(PSP_CONSTATE_SSID_LENGTH, &crt_state.ssidLength);
	if (re!=0) {
		return re; }
	crt_state.ssid[crt_state.ssidLength+1]=0;
	return PSPCALL_OK; }

inline int pspNetGetSecurityType(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_SECURITY_TYPE, &crt_state.securityType);
	if (re!=0) {
		return re; }
	*r = crt_state.securityType;
	return PSPCALL_OK; }	

inline int pspNetGetSignalStrength(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_STRENGTH, &crt_state.strength);
	if (re!=0) {
		return re; }
	*r = (unsigned int)(crt_state.strength);
	return PSPCALL_OK; }	
	
inline int pspNetGetChannel(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_CHANNEL, &crt_state.channel);
	if (re!=0) {
		return re; }
	*r = (unsigned int)(crt_state.channel);
	return PSPCALL_OK; }	

inline int pspNetGetPowerSave(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_POWER_SAVE, &crt_state.powerSave);
	if (re!=0) {
		return re; }
	*r = (unsigned int)(crt_state.powerSave);
	return PSPCALL_OK; }	
	
inline int pspNetGetIP(char** r) {
	*r = crt_state.ip;
	crt_state.ip[16]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_IP, crt_state.ip); }

inline int pspNetGetSubnetMask(char** r) {
	*r = crt_state.subNetMask;
	crt_state.subNetMask[16]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_SUBNETMASK, crt_state.subNetMask); }

inline int pspNetGetGateway(char** r) {
	*r = crt_state.gateway;
	crt_state.gateway[16]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_GATEWAY, crt_state.gateway); }

inline int pspNetGetPrimaryDNS(char** r) {
	*r = crt_state.primaryDns;
	crt_state.primaryDns[16]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_PRIMDNS, crt_state.primaryDns); }

inline int pspNetGetSecondaryDNS(char** r) {
	*r = crt_state.secondaryDns;
	crt_state.secondaryDns[16]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_SECDNS, crt_state.secondaryDns); }

inline int pspNetGetUseProxy(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_USEPROXY, &crt_state.useProxy);
	if (re!=0) {
		return re; }
	*r = crt_state.useProxy;
	return PSPCALL_OK; }	

inline int pspNetGetProxyURL(char** r) {
	*r = crt_state.proxyUrl;
	crt_state.proxyUrl[128]=0;
	return sceNetApctlGetInfo(PSP_CONSTATE_PROXYURL, crt_state.proxyUrl); }

inline int pspNetGetProxyPort(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_PROXYPORT, &crt_state.proxyPort);
	if (re!=0) {
		return re; }
	*r = (unsigned int)(crt_state.proxyPort);
	return PSPCALL_OK; }	

inline int pspNetGetEAPType(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_8021_EAP_TYPE, &crt_state.eapType);
	if (re!=0) {
		return re; }
	*r = crt_state.eapType;
	return PSPCALL_OK; }	

inline int pspNetGetStartBrowser(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_STARTBROWSER, &crt_state.startBrowser);
	if (re!=0) {
		return re; }
	*r = crt_state.startBrowser;
	return PSPCALL_OK; }	

inline int pspNetGetUseWiFiSP(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_WIFISP, &crt_state.wifisp);
	if (re!=0) {
		return re; }
	*r = crt_state.wifisp;
	return PSPCALL_OK; }	
	
/* Macro--since the code for string returns is mostly boilerplate */
/* null returned if javacall call fails */
#define NET_CONFIG_STR_RET(kdecl, natvcall) \
KNIEXPORT KNI_RETURNTYPE_OBJECT \
KNIDECL(kdecl) { \
    char* r; \
    int re; \
    KNI_StartHandles(1); \
    KNI_DeclareHandle(ret); \
    re = natvcall(&r); \
    if ((re==PSPCALL_OK) && (strlen(r) > 0)) { \
        KNI_NewStringUTF(r, ret); } \
    KNI_EndHandlesAndReturnObject(ret); }

NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getProfileName,
	pspNetGetProfileName)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getBSSID,
	pspNetGetBSSIDStr)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getSSID,
	pspNetGetSSIDStr)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getIP,
	pspNetGetIP)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getSubnetMask,
	pspNetGetSubnetMask)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getGateway,
	pspNetGetGateway)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getPrimaryDNS,
	pspNetGetPrimaryDNS)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getSecondaryDNS,
	pspNetGetSecondaryDNS)
NET_CONFIG_STR_RET(com_pspkvm_system_WifiStatus_getProxyURL,
	pspNetGetProxyURL)

/* Macro for uint returns (return as int) */
/* Note that int comes back -1 if call fails, correct value otherwise */
#define NET_CONFIG_UINT_RET(kdecl, natvcall) \
KNIEXPORT KNI_RETURNTYPE_INT \
KNIDECL(kdecl) { \
    unsigned int i; \
    int r = -1; \
    if (natvcall(&i)==PSPCALL_OK) { \
    	 r=(int)i; } \
    KNI_ReturnInt(r); }

NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getSecurityType,
	pspNetGetSecurityType)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getSignalStrength,
	pspNetGetSignalStrength)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getChannel,
	pspNetGetChannel)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getPowerSave,
	pspNetGetPowerSave)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getUseProxy,
	pspNetGetUseProxy)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getProxyPort,
	pspNetGetProxyPort)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getEAPType,
	pspNetGetEAPType)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getStartBrowser,
	pspNetGetStartBrowser)
NET_CONFIG_UINT_RET(com_pspkvm_system_WifiStatus_getUseWiFiSP,
	pspNetGetUseWiFiSP)
