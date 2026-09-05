#include <jni.h>
#include <android/log.h>

#define LOG_TAG "SecuritySDK_Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_example_key_1logging_MainActivity_nativeProtectActivity(JNIEnv *env, jobject thiz, jobject activity) {
    if (activity == nullptr) return;

    // 1. FLAG_SECURE still handled in native as it's a simple window flag
    jclass activityClass = env->GetObjectClass(activity);
    jmethodID getWindowMethod = env->GetMethodID(activityClass, "getWindow", "()Landroid/view/Window;");
    jobject window = env->CallObjectMethod(activity, getWindowMethod);

    if (window != nullptr) {
        jclass windowClass = env->GetObjectClass(window);
        jmethodID addFlagsMethod = env->GetMethodID(windowClass, "addFlags", "(I)V");
        env->CallVoidMethod(window, addFlagsMethod, 0x00002000); // FLAG_SECURE
    }

    // 2. Delegate everything else to the Java/Kotlin Security SDK
    jclass sdkClass = env->FindClass("com/example/key_logging/security/SecuritySdk");
    if (sdkClass == nullptr) {
        LOGE("Failed to find SecuritySdk class");
        return;
    }

    jmethodID enableShieldMethod = env->GetStaticMethodID(sdkClass, "enableAccessibilityShield", "(Landroid/app/Activity;)V");
    if (enableShieldMethod == nullptr) {
        LOGE("Failed to find enableAccessibilityShield method");
        return;
    }

    env->CallStaticVoidMethod(sdkClass, enableShieldMethod, activity);
    LOGI("Accessibility Shield initiated from native layer");
}
