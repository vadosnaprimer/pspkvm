#!/bin/sh
USAGE="`basename $0`: [-CTcdghjmnp] [-r rootdir] [-J JDK_DIR]"
HELP="Options:
-C               performs a clean before building
-c               build CLDC HI
-d               debug build
-e               erase build output / clean
-h               print this help information
-j               build javacall_impl
-m               build midp
-n              use NAMS
-p               build pcsl
-T               compile THUMB mode
-J JDK_DIR       specify the JDK directory (i.e. c:/jdk1.4)
-r rootdir       use <rootdir> as the root of the tree for building"


BUILD_TARGET=WIN32_JAVACALL
unset DEBUG_OPTION
unset MIDP_DEBUG_OPTION
unset CLEAN
unset BUILD_JAVACALL_IMPL
unset BUILD_PCSL
unset BUILD_CLDC
unset ERASE_BUILD_OUTPUT
unset BUILD_MIDP
USE_NAMS=false
ENABLE_THUMB=false
USE_NOKIA_UI=true
USE_JSR_75=true
USE_JSR_179=true

while getopts \?J:CTcdDehjmnpr: c
do
	case $c in
	C)	CLEAN="true";;
	J)	JDK_DIR=$OPTARG
		export JDK_DIR;;
	c)	BUILD_CLDC="true";;
	d)	DEBUG_OPTION="USE_DEBUG=true"
		MIDP_DEBUG_OPTION="USE_DEBUG=true";;
	D)	DEBUG_OPTION="USE_DEBUG=true"
		MIDP_DEBUG_OPTION="USE_CLDC_RELEASE=true";;
	e)	ERASE_BUILD_OUTPUT="true";CLEAN="true";;
	h)	echo "${USAGE}"
		echo "${HELP}"
		exit 2;;
	j)	BUILD_JAVACALL_IMPL="true";;
	m)	BUILD_MIDP="true";;
	n)    USE_NAMS=true;;
	p)	BUILD_PCSL="true";;
	r)	WS_ROOT=$OPTARG;;
	T)	ENABLE_THUMB=true;;
	\?)	echo "${USAGE}"
		echo "${HELP}"
		exit 2;;
	esac
done

echo
echo
echo "-------------- BUILD ${BUILD_TARGET}----------------"

if [ -z "${WS_ROOT}" ]
then
	WS_ROOT=`pwd`
	echo WS_ROOT not specified using ${WS_ROOT}
fi

if [ -z "${BUILD_JAVACALL_IMPL}" -a -z "${BUILD_PCSL}" -a -z "${BUILD_CLDC}" -a -z "${BUILD_MIDP}" ]
then
	BUILD_JAVACALL_IMPL=true
	BUILD_PCSL=true
	BUILD_CLDC=true
	BUILD_MIDP=true
fi

JAVACALL_DIR=${WS_ROOT}/javacall
PCSL_DIR=${WS_ROOT}/pcsl
CLDC_DIR=${WS_ROOT}/cldc
MIDP_DIR=${WS_ROOT}/midp
RESTRICTED_CRYPTO_DIR=${WS_ROOT}/restricted_crypto
TOOLS_DIR=${WS_ROOT}/tools
JSR_120_DIR=${WS_ROOT}/jsr120
NOKIA_UI_DIR=${WS_ROOT}/ext/nokia
JSR_179_DIR=${WS_ROOT}/jsr179
#PSPDEV_GNU_TOOLS_DIR=`psp-config -P`
PSPDEV_GNU_TOOLS_DIR=

#JAVACALL_OUTPUT_DIR=${JAVACALL_DIR}/configuration/phoneMEFeature/psp_mips/output
JAVACALL_OUTPUT_DIR=${JAVACALL_DIR}/configuration/phoneMEFeature/win32_x86/output
PCSL_OUTPUT_DIR=${PCSL_DIR}/output

if [ "${BUILD_JAVACALL_IMPL}" = "true" ]
then
	BUILDMODULE=${JAVACALL_DIR}/configuration/phoneMEFeature/win32_x86
	cd ${BUILDMODULE}
	if [ "${CLEAN}" = "true" ]; then
		echo "Cleaning:    ${BUILDMODULE}"
		rm -rf ${JAVACALL_OUTPUT_DIR}
	fi
	if [ "${ERASE_BUILD_OUTPUT}" != "true" ]; then
	    echo "Building:    ${BUILDMODULE}"
		make USE_NATIVE_AMS=${USE_NAMS} \
			JAVACALL_DIR=${JAVACALL_DIR}  \
			JAVACALL_OUTPUT_DIR=${JAVACALL_OUTPUT_DIR} \
			ENABLE_THUMB=${ENABLE_THUMB} \
			USE_JSR_75=${USE_JSR_75} \
			USE_JSR_179=${USE_JSR_179} \
			USE_PROPERTIES_FROM_FS=false \
			${DEBUG_OPTION}
	fi
	if [ $? != 0 ];then
			echo make failed for ${BUILD_TARGET} module ${BUILDMODULE}
			exit 1
	fi
fi

if [ "${BUILD_PCSL}" = "true" ]
then
	BUILDMODULE=${PCSL_DIR}
	cd ${BUILDMODULE}
	if [ "${CLEAN}" = "true" ]; then
		echo "Cleaning:    ${BUILDMODULE}"
		rm -rf ${PCSL_OUTPUT_DIR}
	fi
	if [ "${ERASE_BUILD_OUTPUT}" != "true" ]; then
	    echo "Building:    ${BUILDMODULE}"
		make JAVACALL_OUTPUT_DIR=${JAVACALL_OUTPUT_DIR} \
			PCSL_PLATFORM=javacall_i386_vc \
			PCSL_OUTPUT_DIR=${PCSL_OUTPUT_DIR} \
			ENABLE_THUMB=${ENABLE_THUMB} \
			GNU_TOOLS_DIR=${PSPDEV_GNU_TOOLS_DIR} \
			${DEBUG_OPTION}
	fi
	if [ $? != 0 ];then
			echo make failed for ${BUILD_TARGET} module ${BUILDMODULE}
			exit 1
	fi
fi

if [ "${BUILD_CLDC}" = "true" ]
then
	BUILDMODULE=${CLDC_DIR}/build/javacall_i386_vc
	cd ${BUILDMODULE}
	if [ "${CLEAN}" = "true" ]; then
		echo "Cleaning:    ${BUILDMODULE}"
		rm -rf dist loopgen romgen target tools ../classes ../tmpclasses ../classes.zip
	fi
	if [ "${ERASE_BUILD_OUTPUT}" != "true" ]; then
	    echo "Building:    ${BUILDMODULE}"
		make JDK_DIR=${JDK_DIR} \
			ENABLE_ISOLATES=true \
			ENABLE_PCSL=true \
			PCSL_OUTPUT_DIR=${PCSL_OUTPUT_DIR} \
			PCSL_DIST_DIR=${PCSL_OUTPUT_DIR}/javacall_i386 \
			JAVACALL_OUTPUT_DIR=${JAVACALL_OUTPUT_DIR} \
			JVMWorkSpace=${CLDC_DIR} \
			${DEBUG_OPTION}
	fi
	if [ $? != 0 ];then
			echo make failed for ${BUILD_TARGET} module ${BUILDMODULE}
			exit 1
	fi
fi

if [ "${BUILD_MIDP}" = "true" ]
then
	BUILDMODULE=${MIDP_DIR}/build/javacall
	cd ${BUILDMODULE}
	if [ "${CLEAN}" = "true" ]; then
		echo "Cleaning:    ${BUILDMODULE}"
		rm -rf output
	fi
	if [ "${ERASE_BUILD_OUTPUT}" != "true" ]; then
	    echo "Building:    ${BUILDMODULE}"
		make JDK_DIR=${JDK_DIR} \
			USE_NATIVE_AMS=${USE_NAMS} \
			ENABLE_THUMB=${ENABLE_THUMB} \
			JAVACALL_PLATFORM=win32_i386_vc \
			JAVACALL_OUTPUT_DIR=${JAVACALL_OUTPUT_DIR} \
			PCSL_OUTPUT_DIR=${PCSL_OUTPUT_DIR} \
			CLDC_DIST_DIR=${CLDC_DIR}/build/javacall_i386_vc/dist \
			TOOLS_DIR=${TOOLS_DIR} \
			RESTRICTED_CRYPTO_DIR=${RESTRICTED_CRYPTO_DIR} \
			USE_JSR_75=${USE_JSR_75} \
			JSR_75_DIR=${WS_ROOT}/jsr75 \
			JSR_120_DIR=${JSR_120_DIR} \
			USE_JSR_179=${USE_JSR_179} \
			JSR_179_DIR=${JSR_179_DIR} \
			USE_NOKIA_UI=${USE_NOKIA_UI} \
			NOKIA_UI_DIR=${NOKIA_UI_DIR} \
			USE_MULTIPLE_ISOLATES=true \
			skip_ams_executables=false \
			SOFT_KEYBOARD_ENABLED=true \
			${MIDP_DEBUG_OPTION}
		if [ $? != 0 ];then
			echo make failed for ${BUILD_TARGET} module ${BUILDMODULE}
			exit 1
		else
			echo "Done! Go to midp/build/javacall/output, run runPhoneME.bat"
		fi
	fi
fi