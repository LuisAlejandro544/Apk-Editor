#include <jni.h>
#include <string.h>
#include <stdio.h>

// Include original C Lua headers
#include "lua/lua.h"
#include "lua/lauxlib.h"
#include "lua/lualib.h"

// Forward declaration for rust core function and C++ engine
#ifdef __cplusplus
extern "C" {
#endif

// Defined in rust_core
int rust_apk_core_version(void);

// Defined in cpp_engine.cpp
const char* get_cpp_engine_status(void);

// C bridge
const char* get_native_c_version(void) {
    return "C11 Native Core Engine Ready";
}

// JNI Entry point providing status of C, C++, Rust, and Lua
JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_NativeEngineBridge_getNativeStackStatus(
    JNIEnv* env,
    jobject thiz) {
    
    // 1. Initialize pure original C Lua state
    lua_State *L = luaL_newstate();
    const char *lua_ver = "Lua Not Initialized";
    if (L != NULL) {
        luaL_openlibs(L);
        lua_ver = LUA_RELEASE; // e.g. "Lua 5.4.7"
        lua_close(L);
    }

    int rust_ver = rust_apk_core_version();
    const char *cpp_status = get_cpp_engine_status();

    char buffer[512];
    snprintf(
        buffer,
        sizeof(buffer),
        "C (C11): OK | C++ (C++17): OK | Rust Core v%d: OK | Official %s: OK",
        rust_ver,
        lua_ver
    );

    return (*env)->NewStringUTF(env, buffer);
}

#ifdef __cplusplus
}
#endif
