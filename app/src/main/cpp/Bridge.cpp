#include <jni.h>
#include "Emulator.h"
#include "Bridge.h"

extern "C" {
using namespace emudroid;

Emulator *emu;

#ifndef BRIDGE_PACKAGE
#define BRIDGE_PACKAGE :-)
#endif

Bridge::Bridge(Emulator *emulator) {
    emu = emulator;
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_start(JNIEnv *env, jobject obj, jint gfx, jint sfx,
                      jint general) {
    return (jboolean) emu->start(gfx, sfx, general);
}


JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_readPalette(JNIEnv *env, jobject obj, jintArray result) {
    return (jboolean) emu->readPalette(env, result);
}


JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_loadGame(JNIEnv *env, jobject obj, jstring path,
                         jstring batteryPath, jstring batteryFullPath) {
    jboolean isCopy;
    jboolean isCopy2;
    jboolean isCopy3;
    const char *fname = env->GetStringUTFChars(path, &isCopy);
    const char *fbattery = env->GetStringUTFChars(batteryPath, &isCopy2);
    const char *fbatteryFullPath = env->GetStringUTFChars(batteryFullPath, &isCopy3);
    bool success = emu->loadGame(fname, fbattery, fbatteryFullPath);
    env->ReleaseStringUTFChars(path, fname);
    env->ReleaseStringUTFChars(batteryPath, fbattery);
    env->ReleaseStringUTFChars(batteryFullPath, fbatteryFullPath);
    return success;
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_setBaseDir(JNIEnv *env, jobject obj, jstring path) {
    jboolean isCopy;
    const char *fname = env->GetStringUTFChars(path, &isCopy);
    bool success = emu->setBaseDir(fname);
    env->ReleaseStringUTFChars(path, fname);
    return success;
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_enableCheat(JNIEnv *env, jobject obj, jstring gg,
                            jint type) {
    jboolean isCopy;
    const char *cheat = env->GetStringUTFChars(gg, &isCopy);
    bool success = emu->enableCheat(cheat, type);
    env->ReleaseStringUTFChars(gg, cheat);
    return success;
}


JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_enableRawCheat(JNIEnv *env, jobject obj, jint addr, jint val, jint comp) {
    jboolean isCopy;
    bool success = emu->enableRawCheat(addr, val, comp);
    return success;
}


JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_emulate(JNIEnv *env, jobject obj, jint keys,
                        jint turbos, jint numFramesToSkip) {
    int res = emu->emulateFrame(keys, turbos, numFramesToSkip);
    return res;
}

JNIEXPORT jboolean
JNICALL Java_com_park_fceux_JniBridge_render(JNIEnv *env, jobject obj, jobject bitmap) {
    return emu->render(env, bitmap, -1, -1, NULL);
}


JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_renderVP(JNIEnv *env, jobject obj, jobject bitmap, int w, int h) {
    return emu->render(env, bitmap, w, h, NULL);
}


JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_renderGL(JNIEnv *env, jobject obj) {
    return emu->renderGL();
}


JNIEXPORT jint JNICALL
Java_com_park_fceux_JniBridge_getHistoryItemCount(JNIEnv *env, jobject obj) {
    return emu->getHistoryItemCount();
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_loadHistoryState(JNIEnv *env, jobject obj, int pos) {
    return emu->loadHistoryState(pos);
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_renderHistory(JNIEnv *env, jobject obj, jobject bmp, int pos, int w, int h) {
    return emu->renderHistory(env, bmp, pos, w, h);
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_setViewPortSize(JNIEnv *env, jobject obj, jint w, jint h) {
    return emu->setViewPortSize(w, h);
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_fireZapper(JNIEnv *env, jobject obj, jint x, jint y) {
    return emu->fireZapper(x, y);
}


JNIEXPORT jint JNICALL
Java_com_park_fceux_JniBridge_readSfxBuffer(JNIEnv *env, jobject obj, jshortArray data) {
    return emu->readSfxBuffer(env, obj, data);
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_loadState(JNIEnv *env, jobject obj, jstring path, int slot) {
    jboolean isCopy;
    const char *fname = env->GetStringUTFChars(path, &isCopy);
    bool success = emu->loadState(fname, slot);
    env->ReleaseStringUTFChars(path, fname);
    return success;
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_saveState(JNIEnv *env, jobject obj, jstring path, int slot) {
    jboolean isCopy;
    const char *fname = env->GetStringUTFChars(path, &isCopy);
    bool success = emu->saveState(fname, slot);
    env->ReleaseStringUTFChars(path, fname);
    return success;
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_reset(JNIEnv *env, jobject obj) {
    return emu->reset();
}

JNIEXPORT jboolean JNICALL
Java_com_park_fceux_JniBridge_stop(JNIEnv *env, jobject obj) {
    return emu->stop();
}

}