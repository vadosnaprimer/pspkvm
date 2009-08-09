#ifndef _javacall_wifi_support_h
#define _javacall_wifi_support_h

#ifdef __cplusplus
extern "C" {
#endif


#include "javacall_defs.h"

/* External interface to the support routines */

/*
	All return 0 on success, nonzero on fail. Pass in pointers. Storage
	is already allocated (non-re-entrant--one buffer only) for 
	all string values */

javacall_result javacall_pspNetGetProfileName(char** r);
	
javacall_result javacall_pspNetGetBSSIDStr(char** r);

javacall_result javacall_pspNetGetSSIDStr(char** r);

javacall_result javacall_pspNetGetSecurityType(unsigned int* r);

javacall_result javacall_pspNetGetSignalStrength(unsigned int* r);
	
javacall_result javacall_pspNetGetChannel(unsigned int* r);

javacall_result javacall_pspNetGetPowerSave(unsigned int* r);
	
javacall_result javacall_pspNetGetIP(char** r);

javacall_result javacall_pspNetGetSubnetMask(char** r);

javacall_result javacall_pspNetGetGateway(char** r);

javacall_result javacall_pspNetGetPrimaryDNS(char** r);

javacall_result javacall_pspNetGetSecondaryDNS(char** r);

javacall_result javacall_pspNetGetUseProxy(unsigned int* r);

javacall_result javacall_pspNetGetProxyURL(char** r);

javacall_result javacall_pspNetGetProxyPort(unsigned int* r);

javacall_result javacall_pspNetGetEAPType(unsigned int* r);

javacall_result javacall_pspNetGetStartBrowser(unsigned int* r);

javacall_result javacall_pspNetGetUseWiFiSP(unsigned int* r);

#ifdef __cplusplus
}
#endif

#endif /* #ifndef _javacall_wifi_support_h */
