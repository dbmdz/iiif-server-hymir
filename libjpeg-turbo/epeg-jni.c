#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <stdbool.h>

#include "jni.h"
#include "Epeg.h"

/*
 * Class:     de_digitalcollections_iiif_image_backend_impl_repository_jpegtran_v2_EpegScaler
 * Method:    downScaleJpegImage
 * Signature: ([BIII)[B
 */
JNIEXPORT jbyteArray JNICALL
Java_de_digitalcollections_iiif_image_backend_impl_repository_jpegtran_v2_EpegScaler_downScaleJpegImage(
        JNIEnv* env, jclass cls, jbyteArray inData, jint width, jint height, jint quality) {

    // Acquire JVM input lock
    int in_size = (*env)->GetArrayLength(env, inData);
    unsigned char* in_buf = (*env)->GetPrimitiveArrayCritical(env, inData, 0);
    unsigned char* dest_buf = NULL;
    int dest_size = -1;

    Epeg_Image* im = epeg_memory_open(in_buf, in_size);
    epeg_decode_size_set(im, width, height);
    epeg_quality_set(im, quality);
    epeg_memory_output_set(im, &dest_buf, &dest_size);
    epeg_encode(im);
    epeg_close(im);
    // Release JVM input lock
    (*env)->ReleasePrimitiveArrayCritical(env, inData, in_buf, 0);
    in_buf = NULL;

    // Acquire JVM input lock
    jbyteArray outArray = (*env)->NewByteArray(env, dest_size);
    void *data = (*env)->GetPrimitiveArrayCritical(env, (jarray)outArray, 0);
    memcpy(data, dest_buf, dest_size);
    // Acquire JVM output lock
    (*env)->ReleasePrimitiveArrayCritical(env, outArray, data, 0);
    free(dest_buf);
    return outArray;
}
