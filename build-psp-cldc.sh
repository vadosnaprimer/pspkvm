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
unset CLEAN
unset BUILD_JAVACALL_IMPL
unset BUILD_PCSL
unset BUILD_CLDC
unset ERASE_BUILD_OUTPUT
unset BUILD_MIDP
USE_NAMS=false
ENABLE_THUMB=false
USE_NOKIA_UI=true

while getopts \?J:Ccdehjmnpr: c
do
	case $c in
	C)	CLEAN="true";;
	J)	JDK_DIR=$OPTARG
		export JDK_DIR;;
	c)	BUILD_CLDC="true";;
	d)	DEBUG_OPTION="USE_DEBUG=true";;
	e)	ERASE_BUILD_OUTPUT="true";CLEAN="true";;
	h)	echo "${USAGE}"
		echo "${HELP}"
		exit 2;;
	j)	BUILD_JAVACALL_IMPL="true";;
	m)	BUILD_MIDP="true";;
	n)    USE_NAMS=true;;
	p)	BUILD_PCSL="true";;
	r)	WS_ROOT=$OPTARG;;
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
TOOLS_DIR=${WS_ROOT}/tools
RESTRICTED_CRYPTO_DIR=${WS_ROOT}/restricted_crypto
JSR_135_DIR=${WS_ROOT}/jsr135
JPEG_DIR=${WS_ROOT}/jpeg
JSR_120_DIR=${WS_ROOT}/jsr120
NOKIA_UI_DIR=${WS_ROOT}/ext/nokia
PSPDEV_GNU_TOOLS_DIR=`psp-config -P`
#PSPDEV_GNU_TOOLS_DIR=

JAVACALL_OUTPUT_DIR=${JAVACALL_DIR}/configuration/phoneMEFeature/psp_mips/output
#JAVACALL_OUTPUT_DIR=${JAVACALL_DIR}/configuration/phoneMEFeature/cygwin_x86/output
PCSL_OUTPUT_DIR=${PCSL_DIR}/output

if [ "${BUILD_JAVACALL_IMPL}" = "true" ]
then
	BUILDMODULE=${JAVACALL_DIR}/configuration/phoneMEFeature/psp_mips
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
			USE_JSR_135=true \
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
			PCSL_PLATFORM=javacall_psp_gcc \
			PCSL_OUTPUT_DIR=${PCSL_OUTPUT_DIR} \
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
	BUILDMODULE=${CLDC_DIR}/build/javacall_psp_gcc
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
			PCSL_DIST_DIR=${PCSL_OUTPUT_DIR}/javacall_mips \
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
	BUILDMODULE=${MIDP_DIR}/build/javacall_psp
	cd ${BUILDMODULE}
	if [ "${CLEAN}" = "true" ]; then
		echo "Cleaning:    ${BUILDMODULE}"
		rm -rf output
	fi
	if [ "${ERASE_BUILD_OUTPUT}" != "true" ]; then
	    echo "Building:    ${BUILDMODULE}"
		make JDK_DIR=${JDK_DIR} \
			USE_NATIVE_AMS=${USE_NAMS} \
			JAVACALL_PLATFORM=psp_mips \
			JAVACALL_OUTPUT_DIR=${JAVACALL_OUTPUT_DIR} \
			PCSL_OUTPUT_DIR=${PCSL_OUTPUT_DIR} \
			CLDC_DIST_DIR=${CLDC_DIR}/build/javacall_psp_gcc/dist \
			TOOLS_DIR=${TOOLS_DIR} \
			RESTRICTED_CRYPTO_DIR=${RESTRICTED_CRYPTO_DIR} \
			USE_MULTIPLE_ISOLATES=true \
			skip_ams_executables=true \
			JPEG_DIR=${JPEG_DIR} \
			JSR_135_DIR=${JSR_135_DIR} \
			JSR_120_DIR=${JSR_120_DIR} \
			USE_NOKIA_UI=${USE_NOKIA_UI} \
			NOKIA_UI_DIR=${NOKIA_UI_DIR} \
			${DEBUG_OPTION}
		if [ $? != 0 ];then
			echo make failed for ${BUILD_TARGET} module ${BUILDMODULE}
			exit 1
		else
			echo Build phoneME libs done. Now enter "psp" directory and "make kxploit"
		fi
	fi
fi
