/*
 * PSP Software Development Kit - http://www.pspdev.org
 * -----------------------------------------------------------------------
 * Licensed under the BSD license, see LICENSE in PSPSDK root for details.
 *
 * pspusbcam.h - Prototypes for the sceUsbCam library
 *
 * Copyright (c) 2005 John Kelley <ps2dev@kelley.ca>
 *
 * $Id: pspgps.h
 */
#ifndef __PSPUSBGPS_H__
#define __PSPUSBGPS_H__

#ifdef __cplusplus
extern "C" {
#endif

#define PSP_USBGPS_PID				0x283
#define PSP_USBGPS_DRIVERNAME		"USBGps_Driver"


typedef struct
{
        short year;             //0
        short month;            //2
        short date;             //4
        short hour;             //6
        short minute;           //8
        short second;           //10
        float garbage1;         //12
        float hdop;             //16
        float garbage2;         //20
        float latitude;         //24
        float longitude;        //28
        float altitude;         //32
        float garbage3;         //36
        float speed;            //40
        float bearing;          //44
} gpsdata;

typedef struct
{
        unsigned char   id;
        unsigned char   elevation;
        short           azimuth;
        unsigned char   snr;
        unsigned char   good;
        short           garbage;
} satinfo;

typedef struct
{
        short satellites_in_view;
        short gargage;
        satinfo satinf[24];
} satdata;


// Unknow

int sceUsbGps_31F95CDE (void);									// ?
int sceUsbGps_69E4AAA8 (void);									// ?
int sceUsbGps_9D8F99E8 (u32 a0, u32 a1);						// ?
int sceUsbGpsReset (u32 a0);									// ?

// Init

int sceUsbGpsOpen (void);										// Open the GPS device
int sceUsbGpsClose (void);										// Close the GPS device

int sceUsbGpsSetInitDataLocation (long a0);						// ?
int sceUsbGpsGetInitDataLocation (long *a0);						// ?

// Data
int sceUsbGpsGetData (gpsdata* g, satdata* s);			// Get data from GPS (size of buffer = 0x30 u32 ?)

// State
int sceUsbGpsGetState (u32 *state);								// Get GPS device state

#ifdef __cplusplus
}
#endif

#endif
