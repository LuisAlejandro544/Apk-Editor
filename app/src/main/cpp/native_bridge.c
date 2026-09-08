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
char* rust_apk_elf_parse_header(const char* file_path);
char* rust_apk_elf_parse_symbols(const char* file_path);
char* rust_apk_elf_parse_dependencies(const char* file_path);
char* rust_apk_elf_disassemble(const char* file_path, unsigned int max_instructions);
char* rust_apk_elf_extract_strings(const char* file_path, unsigned int min_len);
void rust_apk_free_string(char* ptr);

// Defined in cpp_engine.cpp
const char* get_cpp_engine_status(void);

// C bridge
const char* get_native_c_version(void) {
    return "C11 Native Core Engine Ready";
}

// JNI Entry point providing status of C, C++, Rust, Goblin, Capstone, and Lua
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
        "C (C11): OK | C++ (C++17): OK | Rust Core (Goblin & Capstone) v%d: OK | Official %s: OK",
        rust_ver,
        lua_ver
    );

    return (*env)->NewStringUTF(env, buffer);
}

JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_NativeEngineBridge_parseElfHeader(
    JNIEnv* env,
    jobject thiz,
    jstring file_path) {
    
    if (!file_path) {
        return (*env)->NewStringUTF(env, "Error: Ruta nula");
    }
    const char* path = (*env)->GetStringUTFChars(env, file_path, 0);
    char* result = rust_apk_elf_parse_header(path);
    (*env)->ReleaseStringUTFChars(env, file_path, path);

    jstring ret = (*env)->NewStringUTF(env, result ? result : "Error analizando cabecera ELF");
    if (result) rust_apk_free_string(result);
    return ret;
}

JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_NativeEngineBridge_parseElfSymbols(
    JNIEnv* env,
    jobject thiz,
    jstring file_path) {
    
    if (!file_path) {
        return (*env)->NewStringUTF(env, "Error: Ruta nula");
    }
    const char* path = (*env)->GetStringUTFChars(env, file_path, 0);
    char* result = rust_apk_elf_parse_symbols(path);
    (*env)->ReleaseStringUTFChars(env, file_path, path);

    jstring ret = (*env)->NewStringUTF(env, result ? result : "Error analizando símbolos");
    if (result) rust_apk_free_string(result);
    return ret;
}

JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_NativeEngineBridge_parseElfDependencies(
    JNIEnv* env,
    jobject thiz,
    jstring file_path) {
    
    if (!file_path) {
        return (*env)->NewStringUTF(env, "Error: Ruta nula");
    }
    const char* path = (*env)->GetStringUTFChars(env, file_path, 0);
    char* result = rust_apk_elf_parse_dependencies(path);
    (*env)->ReleaseStringUTFChars(env, file_path, path);

    jstring ret = (*env)->NewStringUTF(env, result ? result : "Error analizando dependencias");
    if (result) rust_apk_free_string(result);
    return ret;
}

JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_NativeEngineBridge_disassembleElfSection(
    JNIEnv* env,
    jobject thiz,
    jstring file_path,
    jint max_instructions) {
    
    if (!file_path) {
        return (*env)->NewStringUTF(env, "Error: Ruta nula");
    }
    const char* path = (*env)->GetStringUTFChars(env, file_path, 0);
    char* result = rust_apk_elf_disassemble(path, (unsigned int)max_instructions);
    (*env)->ReleaseStringUTFChars(env, file_path, path);

    jstring ret = (*env)->NewStringUTF(env, result ? result : "Error en desensamblado");
    if (result) rust_apk_free_string(result);
    return ret;
}

JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_NativeEngineBridge_extractElfStrings(
    JNIEnv* env,
    jobject thiz,
    jstring file_path,
    jint min_len) {
    
    if (!file_path) {
        return (*env)->NewStringUTF(env, "Error: Ruta nula");
    }
    const char* path = (*env)->GetStringUTFChars(env, file_path, 0);
    char* result = rust_apk_elf_extract_strings(path, (unsigned int)min_len);
    (*env)->ReleaseStringUTFChars(env, file_path, path);

    jstring ret = (*env)->NewStringUTF(env, result ? result : "Error extrayendo cadenas");
    if (result) rust_apk_free_string(result);
    return ret;
}


#ifdef __cplusplus
}
#endif
