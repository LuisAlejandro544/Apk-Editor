package com.example.data

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object BouncyCastleCryptoService {

  init {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(BouncyCastleProvider())
    }
  }

  fun calculateHash(bytes: ByteArray, algorithm: String): String {
    return try {
      val md = MessageDigest.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME)
      val digest = md.digest(bytes)
      digest.joinToString("") { "%02x".format(it) }
    } catch (_: Throwable) {
      try {
        val md = MessageDigest.getInstance(algorithm)
        val digest = md.digest(bytes)
        digest.joinToString("") { "%02x".format(it) }
      } catch (e: Throwable) {
        "Error: ${e.localizedMessage}"
      }
    }
  }

  fun inspectCertificate(bytes: ByteArray): String {
    return try {
      val cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME)
      val cert = cf.generateCertificate(ByteArrayInputStream(bytes)) as? X509Certificate
        ?: return "No es un certificado X.509 válido"

      buildString {
        appendLine("--- CERTIFICADO X.509 / FIRMA APK ---")
        appendLine("Sujeto: ${cert.subjectDN}")
        appendLine("Emisor: ${cert.issuerDN}")
        appendLine("Algoritmo de Firma: ${cert.sigAlgName}")
        appendLine("Número de Serie: ${cert.serialNumber}")
        appendLine("Válido desde: ${cert.notBefore}")
        appendLine("Válido hasta: ${cert.notAfter}")
        appendLine("Algoritmo de Clave Pública: ${cert.publicKey.algorithm}")
        appendLine("SHA-256 Huella: ${calculateHash(cert.encoded, "SHA-256")}")
        appendLine("SHA-1 Huella: ${calculateHash(cert.encoded, "SHA-1")}")
        appendLine("MD5 Huella: ${calculateHash(cert.encoded, "MD5")}")
      }
    } catch (e: Throwable) {
      "No se pudo parsear como certificado X.509: ${e.localizedMessage}"
    }
  }

  fun decryptAesCbc(
    cipherBytes: ByteArray,
    keyBytes: ByteArray,
    ivBytes: ByteArray? = null
  ): Result<ByteArray> {
    return try {
      val keySpec = SecretKeySpec(keyBytes, "AES")
      val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", BouncyCastleProvider.PROVIDER_NAME)
      if (ivBytes != null && ivBytes.isNotEmpty()) {
        val ivSpec = IvParameterSpec(ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
      } else {
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
      }
      val decrypted = cipher.doFinal(cipherBytes)
      Result.success(decrypted)
    } catch (e: Throwable) {
      Result.failure(e)
    }
  }

  fun decryptAesGcm(
    cipherBytes: ByteArray,
    keyBytes: ByteArray,
    ivBytes: ByteArray
  ): Result<ByteArray> {
    return try {
      val keySpec = SecretKeySpec(keyBytes, "AES")
      val cipher = Cipher.getInstance("AES/GCM/NoPadding", BouncyCastleProvider.PROVIDER_NAME)
      val ivSpec = IvParameterSpec(ivBytes)
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
      val decrypted = cipher.doFinal(cipherBytes)
      Result.success(decrypted)
    } catch (e: Throwable) {
      Result.failure(e)
    }
  }

  fun decryptXor(bytes: ByteArray, key: ByteArray): ByteArray {
    if (key.isEmpty()) return bytes
    val result = ByteArray(bytes.size)
    for (i in bytes.indices) {
      result[i] = (bytes[i].toInt() xor key[i % key.size].toInt()).toByte()
    }
    return result
  }
}
