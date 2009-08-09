#include "wifi_support_int.h"
#include "javacall_wifi_support.h"
#include <string.h>
#include <stdio.h>

// Pretty place to put all this stuff, for each call.
struct pspNetConnectionState crt_state;

// The call implementations--header/interface for all of these is javacall_wifi_support.h
// These provide pretty wrapping of calls to sceNetApctlGetInfo, for displaying/diagnosing
// the PSP's wireless network status.

// Note that for unsigned char, unsigned short, we just return as unsigned int--
// makes for a more uniform interface at the javacall and KNI_Return... levels.

javacall_result javacall_pspNetGetProfileName(char** r) {
	*r = crt_state.name;
	crt_state.name[64]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_PROFILE_NAME, crt_state.name)==0 ?
		JAVACALL_OK : JAVACALL_FAIL ; }
	
javacall_result javacall_pspNetGetBSSIDStr(char** r) {
	*r = crt_state.bssid_str;
	strcpy(crt_state.bssid_str, "");
	int re = sceNetApctlGetInfo(PSP_CONSTATE_BSSID, crt_state.bssid);
	if (re!=0) {
		return JAVACALL_FAIL; }
	sprintf(crt_state.bssid_str, "%02x:%02x:%02x:%02x:%02x:%02x",
		crt_state.bssid[0], crt_state.bssid[1],
		crt_state.bssid[2], crt_state.bssid[3], 
		crt_state.bssid[4], crt_state.bssid[5]);
	return JAVACALL_OK; } 

javacall_result javacall_pspNetGetSSIDStr(char** r) {
	*r = crt_state.ssid;
	strcpy(crt_state.ssid, "");	
	int re = sceNetApctlGetInfo(PSP_CONSTATE_SSID, crt_state.ssid);
	if (re!=0) {
		return JAVACALL_FAIL; }
	re = sceNetApctlGetInfo(PSP_CONSTATE_SSID_LENGTH, &crt_state.ssidLength);
	if (re!=0) {
		return JAVACALL_FAIL; }
	crt_state.ssid[crt_state.ssidLength+1]=0;
	return JAVACALL_OK; }

javacall_result javacall_pspNetGetSecurityType(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_SECURITY_TYPE, &crt_state.securityType);
	if (re!=0) {
		return JAVACALL_FAIL; }
	*r = crt_state.securityType;
	return JAVACALL_OK; }	

javacall_result javacall_pspNetGetSignalStrength(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_STRENGTH, &crt_state.strength);
	if (re!=0) {
		return JAVACALL_FAIL; }
	*r = (unsigned int)(crt_state.strength);
	return JAVACALL_OK; }	
	
javacall_result javacall_pspNetGetChannel(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_CHANNEL, &crt_state.channel);
	if (re!=0) {
		return JAVACALL_FAIL; }
	*r = (unsigned int)(crt_state.channel);
	return JAVACALL_OK; }	

javacall_result javacall_pspNetGetPowerSave(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_POWER_SAVE, &crt_state.powerSave);
	if (re!=0) {
		return JAVACALL_FAIL; }
	*r = (unsigned int)(crt_state.powerSave);
	return JAVACALL_OK; }	
	
javacall_result javacall_pspNetGetIP(char** r) {
	*r = crt_state.ip;
	crt_state.ip[16]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_IP, crt_state.ip)==0 ?
		JAVACALL_OK : JAVACALL_FAIL ; }

javacall_result javacall_pspNetGetSubnetMask(char** r) {
	*r = crt_state.subNetMask;
	crt_state.subNetMask[16]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_SUBNETMASK, crt_state.subNetMask)==0 ?
		JAVACALL_OK : JAVACALL_FAIL ; }

javacall_result javacall_pspNetGetGateway(char** r) {
	*r = crt_state.gateway;
	crt_state.gateway[16]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_GATEWAY, crt_state.gateway)==0 ?
		JAVACALL_OK : JAVACALL_FAIL ; }

javacall_result javacall_pspNetGetPrimaryDNS(char** r) {
	*r = crt_state.primaryDns;
	crt_state.primaryDns[16]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_PRIMDNS, crt_state.primaryDns)==0 ?
		JAVACALL_OK : JAVACALL_FAIL ; }

javacall_result javacall_pspNetGetSecondaryDNS(char** r) {
	*r = crt_state.secondaryDns;
	crt_state.secondaryDns[16]=0;	
	return sceNetApctlGetInfo(PSP_CONSTATE_SECDNS, crt_state.secondaryDns)==0 ?
		JAVACALL_OK : JAVACALL_FAIL ; }

javacall_result javacall_pspNetGetUseProxy(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_USEPROXY, &crt_state.useProxy);
	if (re!=0) {
		return JAVACALL_FAIL; }
	*r = crt_state.useProxy;
	return JAVACALL_OK; }	

javacall_result javacall_pspNetGetProxyURL(char** r) {
	*r = crt_state.proxyUrl;
	crt_state.proxyUrl[128]=0;
	return sceNetApctlGetInfo(PSP_CONSTATE_PROXYURL, crt_state.proxyUrl)==0 ?
		JAVACALL_OK : JAVACALL_FAIL ; }

javacall_result javacall_pspNetGetProxyPort(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_PROXYPORT, &crt_state.proxyPort);
	if (re!=0) {
		return JAVACALL_FAIL; }
	*r = (unsigned int)(crt_state.proxyPort);
	return JAVACALL_OK; }	

javacall_result javacall_pspNetGetEAPType(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_8021_EAP_TYPE, &crt_state.eapType);
	if (re!=0) {
		return JAVACALL_FAIL; }
	*r = crt_state.eapType;
	return JAVACALL_OK; }	

javacall_result javacall_pspNetGetStartBrowser(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_STARTBROWSER, &crt_state.startBrowser);
	if (re!=0) {
		return JAVACALL_FAIL; }
	*r = crt_state.startBrowser;
	return JAVACALL_OK; }	

javacall_result javacall_pspNetGetUseWiFiSP(unsigned int* r) {
	int re = sceNetApctlGetInfo(PSP_CONSTATE_WIFISP, &crt_state.wifisp);
	if (re!=0) {
		return JAVACALL_FAIL; }
	*r = crt_state.wifisp;
	return JAVACALL_OK; }	
	
