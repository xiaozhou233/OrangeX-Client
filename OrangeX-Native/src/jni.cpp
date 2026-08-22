#include <jni.h>
#include <jvmti.h>
#include <windows.h>
#include <cstdio>
#include <cstring>

#include <cn_xiaozhou233_orangex_nativebridge_NativeBridge.h>

struct JvmEnv {
    JavaVM *jvm = nullptr;
    jvmtiEnv *jvmti = nullptr;
} jvmEnv;

// ── 缓存 NativeMessageHandler.onNativeMessage 方法 ID ──
static jclass g_handlerClass = nullptr;
static jmethodID g_onNativeMessage = nullptr;

// ── 被替换后的 nUpdate ──
static void JNICALL windows_display_update(JNIEnv *env, jclass) {
    MSG msg;
    while (PeekMessageW(&msg, nullptr, 0, 0, PM_REMOVE)) {
        if (msg.message == WM_QUIT) return;

        // 拦截键盘 + 鼠标消息
        if (msg.message == WM_KEYDOWN || msg.message == WM_KEYUP ||
            msg.message == WM_SYSKEYDOWN || msg.message == WM_SYSKEYUP ||
            msg.message == WM_MOUSEMOVE ||
            msg.message == WM_LBUTTONDOWN || msg.message == WM_LBUTTONUP ||
            msg.message == WM_RBUTTONDOWN || msg.message == WM_RBUTTONUP ||
            msg.message == WM_MBUTTONDOWN || msg.message == WM_MBUTTONUP ||
            msg.message == WM_MOUSEWHEEL ||
            msg.message == WM_XBUTTONDOWN || msg.message == WM_XBUTTONUP) {
            if (g_onNativeMessage) {
                jboolean handled = env->CallStaticBooleanMethod(
                    g_handlerClass, g_onNativeMessage,
                    (jint)msg.message, (jlong)msg.wParam, (jlong)msg.lParam);
                if (handled) continue; // Java 已处理，不 Dispatch
            }
        }
        TranslateMessage(&msg);
        DispatchMessageW(&msg);
    }
}

// ── 扫描已加载类，替换 nUpdate ──
JNIEXPORT void JNICALL Java_cn_xiaozhou233_orangex_nativebridge_NativeBridge_installNUpdateHook
  (JNIEnv *env, jclass) {
    if (!jvmEnv.jvmti) {
        printf("[installNUpdateHook] jvmti not available\n");
        return;
    }

    jint class_count = 0;
    jclass *classes = nullptr;
    jclass windows_display = nullptr;

    jvmtiError err = jvmEnv.jvmti->GetLoadedClasses(&class_count, &classes);
    if (err != JVMTI_ERROR_NONE || !classes) {
        printf("[installNUpdateHook] GetLoadedClasses failed: %d\n", err);
        return;
    }

    for (jint i = 0; i < class_count; i++) {
        char *sig = nullptr;
        if (jvmEnv.jvmti->GetClassSignature(classes[i], &sig, nullptr) == JVMTI_ERROR_NONE && sig) {
            if (strcmp(sig, "Lorg/lwjgl/opengl/WindowsDisplay;") == 0) {
                windows_display = classes[i];
                jvmEnv.jvmti->Deallocate((unsigned char*)sig);
                break;
            }
            jvmEnv.jvmti->Deallocate((unsigned char*)sig);
        }
    }
    jvmEnv.jvmti->Deallocate((unsigned char*)classes);

    if (!windows_display) {
        printf("[installNUpdateHook] WindowsDisplay not found\n");
        return;
    }

    JNINativeMethod method;
    method.name = const_cast<char*>("nUpdate");
    method.signature = const_cast<char*>("()V");
    method.fnPtr = (void*)windows_display_update;

    if (env->RegisterNatives(windows_display, &method, 1) == JNI_OK) {
        printf("[installNUpdateHook] nUpdate replaced successfully\n");
    } else {
        printf("[installNUpdateHook] RegisterNatives failed\n");
    }
}

// ── 初始化 ──
JNIEXPORT jboolean JNICALL Java_cn_xiaozhou233_orangex_nativebridge_NativeBridge_init
  (JNIEnv *env, jclass bridgeClass) {
    env->GetJavaVM(&jvmEnv.jvm);
    if (!jvmEnv.jvm) {
        printf("[init] Failed to get JavaVM\n");
        return JNI_FALSE;
    }

    jvmEnv.jvm->GetEnv((void**)&jvmEnv.jvmti, JVMTI_VERSION_1_2);
    if (!jvmEnv.jvmti) {
        printf("[init] Failed to get jvmtiEnv\n");
        return JNI_FALSE;
    }

    // 缓存 NativeMessageHandler 类和 onNativeMessage 方法
    jclass handlerClass = env->FindClass("cn/xiaozhou233/orangex/nativebridge/NativeMessageHandler");
    if (!handlerClass) {
        printf("[init] Failed to find NativeMessageHandler class\n");
        return JNI_FALSE;
    }
    g_handlerClass = (jclass)env->NewGlobalRef(handlerClass);
    g_onNativeMessage = env->GetStaticMethodID(handlerClass, "onNativeMessage", "(IJJ)Z");
    if (!g_onNativeMessage) {
        printf("[init] Failed to find onNativeMessage method\n");
    }

    printf("[init] OrangeX Native Bridge ready\n");
    return JNI_TRUE;
}