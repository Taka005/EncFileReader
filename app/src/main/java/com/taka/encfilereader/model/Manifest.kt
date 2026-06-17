package com.taka.encfilereader.model

import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class Manifest (val path: String){
    var rawData: ByteArray? = null
    var salt: ByteArray? = null
    var iv: ByteArray? = null
    var tag: ByteArray? = null
    var key: SecretKeySpec? = null
    val files: MutableList<String> = mutableListOf()
    val fileCount: Int
        get() = files.size

    fun setBuffer(data: ByteArray): Unit{
        if (data.size < 44) throw IllegalArgumentException("データサイズが不足しています\n必要なサイズ: 44, 実際: ${data.size}")

        this.salt = data.sliceArray(0 until 16)
        this.iv = data.sliceArray(16 until 28)
        this.tag = data.sliceArray(28 until 44)
        this.rawData = data.sliceArray(44 until data.size)
    }

    fun decrypt(password: String): Unit{
        val currentSalt = salt ?: throw IllegalArgumentException("Saltが設定されていません")
        val currentIv = iv ?: throw IllegalArgumentException("IVが設定されていません")
        val currentTag = tag ?: throw IllegalArgumentException("Tagが設定されていません")
        val currentRawData = rawData ?: throw IllegalArgumentException("rawDataが設定されていません")

        val spec = PBEKeySpec(password.toCharArray(), currentSalt, 100000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        this.key = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, currentIv)
        cipher.init(Cipher.DECRYPT_MODE, this.key, gcmSpec)

        val decManifestRaw = String(cipher.doFinal(currentRawData + currentTag))
    }
}