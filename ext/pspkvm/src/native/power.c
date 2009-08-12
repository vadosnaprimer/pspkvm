#include <kni.h>
#include <sni.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>
#include <string.h>

// Native support for the PSPKVM extension
// class com.pspkvm.system.Power.
// All calls (as of this writing) go through the
// PSP SDK's psppower.h header.

#include <psppower.h>

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getBatteryLifePercent) {
	KNI_ReturnInt(scePowerGetBatteryLifePercent()); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getBatteryLifeTime) {
	KNI_ReturnInt(scePowerGetBatteryLifeTime()); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getBatteryTemp) {
	KNI_ReturnInt(scePowerGetBatteryTemp()); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getBatteryVolt) {
	KNI_ReturnInt(scePowerGetBatteryVolt()); }

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_pspkvm_system_Power_isPowerOnline) {
	KNI_ReturnBoolean(scePowerIsPowerOnline()==0 ? KNI_FALSE : KNI_TRUE); }

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_pspkvm_system_Power_isBatteryExist) {
	KNI_ReturnBoolean(scePowerIsBatteryExist()==0 ? KNI_FALSE : KNI_TRUE); }

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_pspkvm_system_Power_isBatteryCharging) {
	KNI_ReturnBoolean(scePowerGetBatteryChargingStatus()==0 ? KNI_FALSE : KNI_TRUE); }

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_pspkvm_system_Power_isLowBattery) {
	KNI_ReturnBoolean(scePowerIsLowBattery()==0 ? KNI_FALSE : KNI_TRUE); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getCpuClockFrequency) {
	KNI_ReturnInt(scePowerGetCpuClockFrequency()); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_system_Power_getBusClockFrequency) {
	KNI_ReturnInt(scePowerGetBusClockFrequency()); }
