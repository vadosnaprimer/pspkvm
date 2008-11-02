#include <kni.h>
#include <commonKNIMacros.h>
#include <ROMStructs.h>
#include <jvm.h>

#ifdef DEBUG
#include <stdlib.h>
#include <stdio.h>
#endif

#include <zlib.h>
#include <assert.h>

static z_stream strm;

KNIEXPORT KNI_RETURNTYPE_VOID
Java_javax_microedition_m3g_util_Inflater_initNativeInflater() {
    strm.zalloc = Z_NULL;
    strm.zfree = Z_NULL;
    strm.opaque = Z_NULL;
    strm.avail_in = 0;
    strm.next_in = Z_NULL;
    inflateInit(&strm);
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_javax_microedition_m3g_util_Inflater_inflate() {
    int offset;
    int inLength, outLength;
    char *buf_in;
    char *buf_out;

    int ret;

    KNI_StartHandles(2);
    KNI_DeclareHandle(inBuf);
    KNI_DeclareHandle(outBuf);

    KNI_GetParameterAsObject(1, inBuf);
    KNI_GetParameterAsObject(4, outBuf);

    offset = KNI_GetParameterAsInt(2);
    inLength = KNI_GetParameterAsInt(3);

    outLength = (int)KNI_GetArrayLength(outBuf);

    buf_in = malloc(inLength);
    buf_out = malloc(outLength);

    KNI_GetRawArrayRegion(inBuf, 0, inLength, (jbyte*)buf_in);

    strm.avail_in = inLength;
    strm.next_in = (unsigned char*)buf_in;

    strm.avail_out = outLength;
    strm.next_out = (unsigned char*)buf_out;

    ret = inflate(&strm, Z_FINISH);
    assert(ret != Z_STREAM_ERROR);
    assert(ret != Z_STREAM_END);

#ifdef DEBUG
    printf("nativeInflater->inflate = %d (avail.out %d)\n", ret, strm.avail_out);
#endif

    KNI_SetRawArrayRegion(outBuf, 0, outLength, (jbyte*)buf_out);
    KNI_EndHandles();

    free(buf_in);
    free(buf_out);

    KNI_ReturnInt(outLength);

}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_javax_microedition_m3g_util_Inflater_destroyNativeInflater() {
    inflateEnd(&strm);
    KNI_ReturnVoid();
}
