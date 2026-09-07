#!/usr/bin/env bash
# ==============================================================================
# Script de compilación de APK Debug con generación forzada de firma desde cero
# Proyecto: APK Extractor Mobile
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="${SCRIPT_DIR}"
cd "${ROOT_DIR}"

echo "======================================================================"
echo "  APK EXTRACTOR - COMPILACIÓN DEBUG CON FIRMA GENERADA DESDE CERO"
echo "======================================================================"
echo "Directorio raíz: ${ROOT_DIR}"

KEYSTORE_FILE="${ROOT_DIR}/debug.keystore"
KEYSTORE_ALIAS="androiddebugkey"
KEYSTORE_PASS="android"
KEY_PASS="android"
DNAME="CN=Android Debug,O=Android,C=US"

# 1. Obligar a generar la firma desde cero
echo ""
echo "--> Paso 1: Verificando y forzando la generación de la firma desde cero..."
if [ -f "${KEYSTORE_FILE}" ]; then
  echo "    [!] Se detectó un archivo previo de firma: ${KEYSTORE_FILE}."
  echo "    [x] Eliminando firma previa para obligar generación limpia desde cero..."
  rm -f "${KEYSTORE_FILE}"
fi

echo "    [+] Generando nuevo keystore de depuración (RSA 2048, validez 10000 días)..."
if command -v keytool &> /dev/null; then
  keytool -genkeypair \
    -v \
    -keystore "${KEYSTORE_FILE}" \
    -storepass "${KEYSTORE_PASS}" \
    -alias "${KEYSTORE_ALIAS}" \
    -keypass "${KEY_PASS}" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "${DNAME}"
else
  echo "ERROR: 'keytool' no está instalado en el PATH. Asegúrate de tener JDK 17 o 21 configurado."
  exit 1
fi

if [ ! -f "${KEYSTORE_FILE}" ]; then
  echo "ERROR: Falló la generación del archivo de firma: ${KEYSTORE_FILE}"
  exit 1
fi

echo "    [✓] Firma generada exitosamente en: ${KEYSTORE_FILE}"
echo "    [i] Huella digital del certificado de depuración recién generado:"
keytool -list -v -keystore "${KEYSTORE_FILE}" -storepass "${KEYSTORE_PASS}" -alias "${KEYSTORE_ALIAS}" | grep -E "SHA1|SHA256" || true

# 2. Localizar ejecutable de Gradle
echo ""
echo "--> Paso 2: Localizando ejecutable de Gradle..."
GRADLE_EXEC=""
if command -v gradle &> /dev/null; then
  GRADLE_EXEC="gradle"
elif [ -x "${ROOT_DIR}/gradlew" ]; then
  GRADLE_EXEC="${ROOT_DIR}/gradlew"
elif [ -f "${ROOT_DIR}/gradlew" ]; then
  chmod +x "${ROOT_DIR}/gradlew"
  GRADLE_EXEC="${ROOT_DIR}/gradlew"
else
  echo "ERROR: No se encontró ni 'gradle' ni './gradlew' en el sistema."
  exit 1
fi

echo "    [✓] Usando Gradle: ${GRADLE_EXEC}"

# 3. Compilar APK Debug SIN CACHÉ
echo ""
echo "--> Paso 3: Compilando APK Debug (sin caché, sin daemon para aislamiento total)..."
"${GRADLE_EXEC}" :app:assembleDebug \
  --no-build-cache \
  --no-daemon \
  --stacktrace

# 4. Validar que el APK Debug se haya generado correctamente
echo ""
echo "--> Paso 4: Verificando la presencia y firma del APK Debug resultante..."
OUTPUT_APK_DIR="${ROOT_DIR}/app/build/outputs/apk/debug"
APK_FILE=$(find "${OUTPUT_APK_DIR}" -name "*.apk" 2>/dev/null | head -n 1 || true)

if [ -z "${APK_FILE}" ] || [ ! -f "${APK_FILE}" ]; then
  echo "ERROR: No se encontró ningún archivo APK en ${OUTPUT_APK_DIR}"
  exit 1
fi

APK_SIZE=$(stat -c%s "${APK_FILE}" 2>/dev/null || stat -f%z "${APK_FILE}" 2>/dev/null || echo "0")
echo "    [✓] APK Debug generado exitosamente: ${APK_FILE}"
echo "    [i] Tamaño del APK: ${APK_SIZE} bytes"

echo ""
echo "======================================================================"
echo "  COMPILACIÓN Y FIRMA FINALIZADA CON ÉXITO"
echo "  APK listo: ${APK_FILE}"
echo "======================================================================"
