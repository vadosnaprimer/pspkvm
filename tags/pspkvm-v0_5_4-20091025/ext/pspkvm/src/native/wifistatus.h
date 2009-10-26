#ifndef _wifi_status_h
#define _wifi_status_h

#ifdef __cplusplus
extern "C" {
#endif

#include <pspnet_apctl.h>

/*
	Header for wifistatus.c--defines a struct and the field values
	for calling into sceNetApctlGetInfo(...). Much is borrowed
	from pspZorba and Insert_Witty_Name via
	
		http://forums.ps2dev.org/viewtopic.php?t=10277 .
	
	NB: Defines may eventually collide if this winds up in the SDK;
	hasn't as of this writing, however.
*/

/* Field selectors and corresponding return types for sceNetApctlGetInfo */
#define PSP_CONSTATE_PROFILE_NAME       0   /*char [64] */ 
#define PSP_CONSTATE_BSSID              1   /*u8 [6] */ 
#define PSP_CONSTATE_SSID               2   /*char [32] */ 
#define PSP_CONSTATE_SSID_LENGTH        3   /*u32*/ 
#define PSP_CONSTATE_SECURITY_TYPE      4   /*u32*/ 
#define PSP_CONSTATE_STRENGTH           5   /*u8*/ 
#define PSP_CONSTATE_CHANNEL            6   /*u8*/ 
#define PSP_CONSTATE_POWER_SAVE         7   /*u8*/ 
#define PSP_CONSTATE_IP                 8   /*char[16] */ 
#define PSP_CONSTATE_SUBNETMASK         9   /*char[16]*/ 
#define PSP_CONSTATE_GATEWAY           10   /*char[16]*/ 
#define PSP_CONSTATE_PRIMDNS           11   /*char[16] */ 
#define PSP_CONSTATE_SECDNS            12   /*char[16] */ 
#define PSP_CONSTATE_USEPROXY          13   /*u32*/ 
#define PSP_CONSTATE_PROXYURL          14   /*char[128] */ 
#define PSP_CONSTATE_PROXYPORT         15   /*u16*/ 
#define PSP_CONSTATE_8021_EAP_TYPE     16   /*u32*/ 
#define PSP_CONSTATE_STARTBROWSER      17   /*u32*/ 
#define PSP_CONSTATE_WIFISP            18   /*u32*/ 

/* Helpful struct for getting all this info */

typedef struct pspNetConnectionState 
{ 
    char name[64+1];          /*name of the configuration used*/ 
    unsigned char bssid[6];   /*mac address of the access point*/
		char bssid_str[18];				/*string version of MAC*/ 
    char ssid[32+1];          /*ssid*/ 

    unsigned int ssidLength;  /*ssid string length*/ 
    unsigned int securityType;/*(0 for none, 1 for WEP, 2 for WPA). */ 

    unsigned char strength;   /*signal strength in %*/ 

    unsigned char channel;    /*channel*/ 
    unsigned char powerSave;  /*1 on, 0 off*/ 

    char ip[16+1];            /*PSP's ip*/ 
    char subNetMask[16+1];    /*subnet mask*/ 
    char gateway[16+1];       /*gateway*/ 
    char primaryDns[16+1];    /*primary DNS*/ 
    char secondaryDns[16+1];  /*secondary DNS*/ 

    unsigned int useProxy;    /*should I use a Proxy : 1 => yes   0=> no*/ 
    char proxyUrl[128+1];     /*proxy url*/ 
    unsigned short proxyPort; /*proxy port*/ 

    unsigned int eapType;     /*(0 is none, 1 is EAP-MD5)*/ 
    unsigned int startBrowser;/*should a browser be started*/ 
    unsigned int wifisp;      /* Set to 1 to allow connections to Wifi service providers (WISP) */ 
} pspNetConnectionState;

#ifdef __cplusplus
}
#endif


#endif // #ifndef _wifi_status_h
