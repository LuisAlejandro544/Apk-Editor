cmake_minimum_required(VERSION 3.22.1)

project("apk_native_engine" C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

# Original Lua C source files (excluding standalone CLI executables lua.c and luac.c)
set(LUA_SOURCES
    lua/lapi.c
    lua/lauxlib.c
    lua/lbaselib.c
    lua/lcode.c
    lua/lcorolib.c
    lua/lctype.c
    lua/ldblib.c
    lua/ldebug.c
    lua/ldo.c
    lua/ldump.c
    lua/lfunc.c
    lua/lgc.c
    lua/linit.c
    lua/liolib.c
    lua/llex.c
    lua/lmathlib.c
    lua/lmem.c
    lua/loadlib.c
    lua/lobject.c
    lua/lopcodes.c
    lua/loslib.c
    lua/lparser.c
    lua/lstate.c
    lua/lstring.c
    lua/lstrlib.c
    lua/ltable.c
    lua/ltablib.c
    lua/ltm.c
    lua/lundump.c
    lua/lutf8lib.c
    lua/lvm.c
    lua/lzio.c
)

add_library(
    apk_native_engine
    SHARED
    native_bridge.c
    cpp_engine.cpp
    rust_core/rust_core_abi.c
    ${LUA_SOURCES}
)

target_include_directories(
    apk_native_engine
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/lua
    ${CMAKE_CURRENT_SOURCE_DIR}/rust_core
)

find_library(log-lib log)

target_link_libraries(
    apk_native_engine
    ${log-lib}
    m
)
