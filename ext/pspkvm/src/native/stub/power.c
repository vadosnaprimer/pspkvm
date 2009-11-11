#include <kni.h>
#include <sni.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>
#include <string.h>


KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getBatteryLifePercent) {
	KNI_ReturnInt(100); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getBatteryLifeTime) {
	KNI_ReturnInt(100); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getBatteryTemp) {
	KNI_ReturnInt(0); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getBatteryVolt) {
	KNI_ReturnInt(0); }

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_pspkvm_system_Power_isPowerOnline) {
	KNI_ReturnBoolean(KNI_TRUE); }

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_pspkvm_system_Power_isBatteryExist) {
	KNI_ReturnBoolean(KNI_TRUE); }

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_pspkvm_system_Power_isBatteryCharging) {
	KNI_ReturnBoolean(KNI_FALSE); }

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_pspkvm_system_Power_isLowBattery) {
	KNI_ReturnBoolean(KNI_FALSE); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getCpuClockFrequency) {
	KNI_ReturnInt(222); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getBusClockFrequency) {
	KNI_ReturnInt(133); }
