#include <kni.h>
#include <sni.h>
#include <midpMalloc.h>
#include <midpDataHash.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_installer_SuiteDownloadInfo_openDir) {
    int handle;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_PCSL_STRING(1, dirName)

    handle = (int)storage_open_file_iterator(&dirName);

    RELEASE_PCSL_STRING_PARAMETER

    KNI_EndHandles();
    KNI_ReturnInt(handle);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_midp_installer_SuiteDownloadInfo_nextFileInDir0) {
    int handle, f_errc;
    pcsl_string filename = PCSL_STRING_NULL;
    
    handle = KNI_GetParameterAsInt(2);
    
    KNI_StartHandles(2);
    KNI_DeclareHandle(ret);

    GET_PARAMETER_AS_PCSL_STRING(1, root)
    
    f_errc = storage_get_next_file_in_iterator(&root, (void*)handle, &filename);
    if (0 == f_errc) {
            midp_jstring_from_pcsl_string(KNIPASSARGS &filename, ret);
    } else {
            KNI_ReleaseHandle(ret);
    }
    
    RELEASE_PCSL_STRING_PARAMETER
    
    
    pcsl_string_free(&filename);
    KNI_EndHandlesAndReturnObject(ret);
}

extern unsigned short JIS_UNICODE[];

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_sun_midp_installer_SuiteDownloadInfo_convert2lable0) {
    pcsl_string to = PCSL_STRING_NULL;
    int i, len, chars = 0;
    
    KNI_StartHandles(2);
    KNI_DeclareHandle(ret);

    GET_PARAMETER_AS_PCSL_STRING(1, from)

    len = pcsl_string_length(&from);
    to.data = midpMalloc((len + 1)*sizeof(jchar));
    if (to.data) {
        to.flags = PCSL_STRING_IN_HEAP;
     
        for (i = 0; i < len; ) {
        	 if ((from.data[i] > 0 && from.data[i] < 128) || (i+1 == len)) {
        	     to.data[chars] = from.data[i];
        	     i+=1;
        	 } else {
        	     to.data[chars] = JIS_UNICODE[(from.data[i] << 8) | from.data[i+1]];
        	     i+=2;
        	 }
        	 chars++;
        }
        to.data[chars] = 0;
        to.length = chars + 1;
        
        midp_jstring_from_pcsl_string(KNIPASSARGS &to, ret);
        
        RELEASE_PCSL_STRING_PARAMETER
        
        
        pcsl_string_free(&to);
    }
    KNI_EndHandlesAndReturnObject(ret);
}

KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_installer_SuiteDownloadInfo_closeDir) {
    int handle;
    
    handle = KNI_GetParameterAsInt(1);
    storageCloseFileIterator(handle);
    KNI_ReturnVoid();
}

