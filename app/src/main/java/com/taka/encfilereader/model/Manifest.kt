package com.taka.encfilereader.model

import kotlinx.serialization.json.Json
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.comparisons.compareBy

open class Manifest (val path: String){
    var key: SecretKeySpec? = null
    var files: MutableList<FileMetaData> = mutableListOf()
    open val fileCount: Int
        get() = files.size

    fun setBuffer(data: ByteArray,password: String): Result<Unit>{
        if (data.size < 44) return Result.failure(IllegalArgumentException("データサイズ不足: ${data.size}"))

        val salt: ByteArray = data.sliceArray(0 until 16)
        val iv: ByteArray = data.sliceArray(16 until 28)
        val tag: ByteArray = data.sliceArray(28 until 44)
        val rawData: ByteArray = data.sliceArray(44 until data.size)

        this.key = createKey(salt,password).getOrElse { error ->
            return Result.failure(error)
        }

        val decRawData = decryptData(rawData + tag, salt, iv, password).getOrElse { error ->
            return Result.failure(error)
        }

        return runCatching {
            this.files = Json.decodeFromString<MutableList<FileMetaData>>(decRawData)

            sortFiles()
        }
    }

    fun getContent(data: ByteArray){

    }

    fun decryptData(content: ByteArray, salt: ByteArray, iv: ByteArray, password: String): Result<String> {
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, this.key, GCMParameterSpec(128, iv))

            String(cipher.doFinal(content))
        }
    }

    fun createKey(salt: ByteArray,password: String): Result<SecretKeySpec>{
        return runCatching {
            val spec = PBEKeySpec(password.toCharArray(), salt, 100000, 256)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")

            SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        }

    }

    fun sortFiles(): Unit{
        this.files.sortWith(
            compareBy { it.originalFileName }
        )

        this.files.forEach { data ->
            data.contents.sortWith(
                compareBy({ it.name })
            )
        }
    }
}