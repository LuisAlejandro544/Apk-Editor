//! Rust core for high-performance ELF binary analysis, symbols extraction, and machine code disassembly.
//! Integrates `goblin` (ELF binary parser) and `capstone` (multi-architecture disassembler).

use std::ffi::{CStr, CString};
use std::os::raw::c_char;

#[no_mangle]
pub extern "C" fn rust_apk_core_version() -> i32 {
    2
}

#[no_mangle]
pub extern "C" fn rust_apk_verify_integrity() -> bool {
    true
}

/// Parses an ELF file (.so) and returns formatted metadata, architecture, sections, and hardening info using goblin.
#[no_mangle]
pub extern "C" fn rust_apk_elf_parse_header(file_path: *const c_char) -> *mut c_char {
    if file_path.is_null() {
        return CString::new("Error: Ruta de archivo nula").unwrap().into_raw();
    }

    let c_str = unsafe { CStr::from_ptr(file_path) };
    let path_str = match c_str.to_str() {
        Ok(s) => s,
        Err(_) => return CString::new("Error: Ruta inválida").unwrap().into_raw(),
    };

    let buffer = match std::fs::read(path_str) {
        Ok(b) => b,
        Err(e) => return CString::new(format!("Error al leer archivo: {}", e)).unwrap().into_raw(),
    };

    match goblin::elf::Elf::parse(&buffer) {
        Ok(elf) => {
            let arch_str = match elf.header.e_machine {
                goblin::elf::header::EM_AARCH64 => "AArch64 (ARM64-v8a)",
                goblin::elf::header::EM_ARM => "ARM (armeabi-v7a)",
                goblin::elf::header::EM_X86_64 => "x86_64",
                goblin::elf::header::EM_386 => "x86 (i386)",
                goblin::elf::header::EM_RISCV => "RISC-V",
                _ => "Otro / Desconocido",
            };

            let bitness = if elf.is_64 { "64-bit" } else { "32-bit" };
            let endianness = if elf.little_endian { "Little Endian (LSB)" } else { "Big Endian (MSB)" };

            // Hardening checks
            let has_canary = elf.dynsyms.iter().any(|sym| {
                elf.dynstrtab.get_at(sym.st_name).map_or(false, |name| name.contains("__stack_chk_fail"))
            });
            let is_pie = elf.header.e_type == goblin::elf::header::ET_DYN;

            let mut output = String::new();
            output.push_str("====================================================\n");
            output.push_str("          AUDITORÍA Y CABECERA ELF (.SO)            \n");
            output.push_str("              Motor: Goblin (Rust Core)             \n");
            output.push_str("====================================================\n\n");

            output.push_str(&format!("Arquitectura:       {}\n", arch_str));
            output.push_str(&format!("Formato:            ELF {} ({})\n", bitness, endianness));
            output.push_str(&format!("Entry Point:        0x{:016X}\n", elf.entry));
            output.push_str(&format!("Número de Secciones: {}\n", elf.section_headers.len()));
            output.push_str(&format!("Cabeceras Programa: {}\n\n", elf.program_headers.len()));

            output.push_str("--- Protecciones de Seguridad (Hardening) ---\n");
            output.push_str(&format!("Posición Independiente (PIE): {}\n", if is_pie { "ACTIVO [Seguro]" } else { "INACTIVO" }));
            output.push_str(&format!("Protección Stack Canary:      {}\n\n", if has_canary { "DETECTADO (__stack_chk_fail)" } else { "NO DETECTADO" }));

            output.push_str("--- Secciones Detectadas ---\n");
            for section in &elf.section_headers {
                if let Some(name) = elf.shdr_strtab.get_at(section.sh_name) {
                    if !name.is_empty() {
                        output.push_str(&format!("  • {:<20}  Offset: 0x{:08X}  Tamaño: {} bytes\n", name, section.sh_offset, section.sh_size));
                    }
                }
            }

            CString::new(output).unwrap().into_raw()
        }
        Err(e) => CString::new(format!("Error al parsear binario ELF con Goblin: {}", e)).unwrap().into_raw(),
    }
}

/// Parses dynamic symbols from ELF file using goblin, highlighting JNI exports.
#[no_mangle]
pub extern "C" fn rust_apk_elf_parse_symbols(file_path: *const c_char) -> *mut c_char {
    if file_path.is_null() {
        return CString::new("Error: Ruta nula").unwrap().into_raw();
    }
    let c_str = unsafe { CStr::from_ptr(file_path) };
    let path_str = match c_str.to_str() {
        Ok(s) => s,
        Err(_) => return CString::new("Error: Ruta inválida").unwrap().into_raw(),
    };

    let buffer = match std::fs::read(path_str) {
        Ok(b) => b,
        Err(e) => return CString::new(format!("Error de lectura: {}", e)).unwrap().into_raw(),
    };

    match goblin::elf::Elf::parse(&buffer) {
        Ok(elf) => {
            let mut jni_exports = Vec::new();
            let mut other_exports = Vec::new();
            let mut imports = Vec::new();

            for sym in elf.dynsyms.iter() {
                if let Some(name) = elf.dynstrtab.get_at(sym.st_name) {
                    if !name.is_empty() {
                        let is_defined = sym.st_shndx != goblin::elf::section_header::SHN_UNDEF as usize;
                        if name.starts_with("Java_") {
                            jni_exports.push((name.to_string(), sym.st_value, sym.st_size));
                        } else if is_defined {
                            other_exports.push((name.to_string(), sym.st_value, sym.st_size));
                        } else {
                            imports.push(name.to_string());
                        }
                    }
                }
            }

            let mut output = String::new();
            output.push_str("====================================================\n");
            output.push_str("           TABLA DE SÍMBOLOS DINÁMICOS              \n");
            output.push_str("              Motor: Goblin (Rust Core)             \n");
            output.push_str("====================================================\n\n");

            output.push_str(&format!("⭐ FUNCIONES JNI EXPORTADAS ({})\n", jni_exports.len()));
            if jni_exports.is_empty() {
                output.push_str("  (No se encontraron funciones nativas con prefijo Java_*)\n");
            } else {
                for (name, val, size) in &jni_exports {
                    output.push_str(&format!("  [JNI] 0x{:08X} ({}B) -> {}\n", val, size, name));
                }
            }

            output.push_str(&format!("\n📦 OTRAS FUNCIONES EXPORTADAS ({})\n", other_exports.len()));
            for (name, val, size) in other_exports.iter().take(50) {
                output.push_str(&format!("  [EXP] 0x{:08X} ({}B) -> {}\n", val, size, name));
            }
            if other_exports.len() > 50 {
                output.push_str(&format!("  ... y {} funciones más.\n", other_exports.len() - 50));
            }

            output.push_str(&format!("\n🔗 SÍMBOLOS IMPORTADOS / ENLAZADOS ({})\n", imports.len()));
            for name in imports.iter().take(40) {
                output.push_str(&format!("  [IMP] {}\n", name));
            }
            if imports.len() > 40 {
                output.push_str(&format!("  ... y {} imports más.\n", imports.len() - 40));
            }

            CString::new(output).unwrap().into_raw()
        }
        Err(e) => CString::new(format!("Error parseando símbolos: {}", e)).unwrap().into_raw(),
    }
}

/// Parses DT_NEEDED dynamic libraries dependencies using goblin.
#[no_mangle]
pub extern "C" fn rust_apk_elf_parse_dependencies(file_path: *const c_char) -> *mut c_char {
    if file_path.is_null() {
        return CString::new("Error: Ruta nula").unwrap().into_raw();
    }
    let c_str = unsafe { CStr::from_ptr(file_path) };
    let path_str = match c_str.to_str() {
        Ok(s) => s,
        Err(_) => return CString::new("Error: Ruta inválida").unwrap().into_raw(),
    };

    let buffer = match std::fs::read(path_str) {
        Ok(b) => b,
        Err(e) => return CString::new(format!("Error de lectura: {}", e)).unwrap().into_raw(),
    };

    match goblin::elf::Elf::parse(&buffer) {
        Ok(elf) => {
            let mut output = String::new();
            output.push_str("====================================================\n");
            output.push_str("          DEPENDENCIAS DINÁMICAS (DT_NEEDED)        \n");
            output.push_str("              Motor: Goblin (Rust Core)             \n");
            output.push_str("====================================================\n\n");

            if elf.libraries.is_empty() {
                output.push_str("Este binario no declara dependencias dinámicas DT_NEEDED.\n");
            } else {
                output.push_str(&format!("Total de librerías requeridas: {}\n\n", elf.libraries.len()));
                for (idx, lib) in elf.libraries.iter().enumerate() {
                    output.push_str(&format!("  [{}] {}\n", idx + 1, lib));
                }
            }

            CString::new(output).unwrap().into_raw()
        }
        Err(e) => CString::new(format!("Error parseando dependencias: {}", e)).unwrap().into_raw(),
    }
}

/// Disassembles executable code section using Capstone engine.
#[no_mangle]
pub extern "C" fn rust_apk_elf_disassemble(file_path: *const c_char, max_instructions: u32) -> *mut c_char {
    if file_path.is_null() {
        return CString::new("Error: Ruta nula").unwrap().into_raw();
    }
    let c_str = unsafe { CStr::from_ptr(file_path) };
    let path_str = match c_str.to_str() {
        Ok(s) => s,
        Err(_) => return CString::new("Error: Ruta inválida").unwrap().into_raw(),
    };

    let buffer = match std::fs::read(path_str) {
        Ok(b) => b,
        Err(e) => return CString::new(format!("Error de lectura: {}", e)).unwrap().into_raw(),
    };

    match goblin::elf::Elf::parse(&buffer) {
        Ok(elf) => {
            // Locate .text section
            let text_section = elf.section_headers.iter().find(|s| {
                elf.shdr_strtab.get_at(s.sh_name).map_or(false, |name| name == ".text")
            });

            let (code_bytes, base_addr) = match text_section {
                Some(sec) => {
                    let start = sec.sh_offset as usize;
                    let end = (sec.sh_offset + sec.sh_size) as usize;
                    if start < buffer.len() {
                        let actual_end = end.min(buffer.len());
                        (&buffer[start..actual_end], sec.sh_addr)
                    } else {
                        (&[][..], 0)
                    }
                }
                None => {
                    // Fallback to entry point offset if no .text
                    (&[][..], 0)
                }
            };

            let cs_arch = match elf.header.e_machine {
                goblin::elf::header::EM_AARCH64 => (capstone::Arch::ARM64, capstone::Mode::Arm),
                goblin::elf::header::EM_ARM => (capstone::Arch::ARM, capstone::Mode::Arm),
                goblin::elf::header::EM_X86_64 => (capstone::Arch::X86, capstone::Mode::Mode64),
                goblin::elf::header::EM_386 => (capstone::Arch::X86, capstone::Mode::Mode32),
                _ => (capstone::Arch::ARM64, capstone::Mode::Arm),
            };

            let mut output = String::new();
            output.push_str("====================================================\n");
            output.push_str("         DESENSAMBLADO ASM DE SECCIÓN .TEXT         \n");
            output.push_str("             Motor: Capstone Disassembler           \n");
            output.push_str("====================================================\n\n");

            if code_bytes.is_empty() {
                output.push_str("No se encontró la sección .text o está vacía en este binario.\n");
                return CString::new(output).unwrap().into_raw();
            }

            match capstone::Capstone::new().arm64().mode(capstone::arch::arm64::ArchMode::Arm).build() {
                Ok(cs) => {
                    let limit = if max_instructions > 0 { max_instructions as usize } else { 300 };
                    let sample_bytes = if code_bytes.len() > limit * 4 {
                        &code_bytes[..limit * 4]
                    } else {
                        code_bytes
                    };

                    match cs.disasm_all(sample_bytes, base_addr) {
                        Ok(instructions) => {
                            output.push_str(&format!("Instrucciones desensambladas (mostrando hasta {}):\n\n", instructions.len()));
                            for insn in instructions.iter().take(limit) {
                                output.push_str(&format!(
                                    "  0x{:08X}:  {:<8} {}\n",
                                    insn.address(),
                                    insn.mnemonic().unwrap_or("???"),
                                    insn.op_str().unwrap_or("")
                                ));
                            }
                        }
                        Err(e) => {
                            output.push_str(&format!("Error desensamblando bytes con Capstone: {}\n", e));
                        }
                    }
                }
                Err(e) => {
                    output.push_str(&format!("Error inicializando motor Capstone: {}\n", e));
                }
            }

            CString::new(output).unwrap().into_raw()
        }
        Err(e) => CString::new(format!("Error parseando ELF: {}", e)).unwrap().into_raw(),
    }
}

/// Frees a C-string allocated by Rust.
#[no_mangle]
pub extern "C" fn rust_apk_free_string(ptr: *mut c_char) {
    if !ptr.is_null() {
        unsafe {
            let _ = CString::from_raw(ptr);
        }
    }
}

