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

    fun setBuffer(data: ByteArray,password: String): Unit{
        require(data.size >= 44) { "データサイズが不足しています。サイズ: ${data.size}" }

        val salt: ByteArray = data.sliceArray(0 until 16)
        val iv: ByteArray = data.sliceArray(16 until 28)
        val tag: ByteArray = data.sliceArray(28 until 44)
        val rawData: ByteArray = data.sliceArray(44 until data.size)

        val decRawData = decryptData(rawData + tag, salt, iv, password)

        this.files = Json.decodeFromString<MutableList<FileMetaData>>(decRawData)

        sortFile()
    }

    fun decryptData(content: ByteArray, salt: ByteArray, iv: ByteArray, password: String): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 100000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        this.key = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, this.key, GCMParameterSpec(128, iv))

        return String(cipher.doFinal(content))
    }

    fun sortFile(): Unit{
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