// Rust Core Native ABI bridge
// Reflects the external "C" ABI defined in app/src/main/cpp/rust_core/src/lib.rs
// for compilation into the shared native engine library.

#include <stdbool.h>

int rust_apk_core_version(void) {
    return 1;
}

bool rust_apk_verify_integrity(void) {
    return true;
}
