package com.taka.encfilereader.model

import kotlinx.serialization.json.Json
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.comparisons.compareBy

open class Manifest (val dirName: String){
    var originalDirName: String? = null;
    var key: SecretKeySpec? = null
    private var files: MutableList<FileMetaData> = mutableListOf()
    open val fileCount: Int
        get() = files.size

    open fun setBuffer(
        data: ByteArray,
        password: String
    ): Result<Unit>{
        if (data.size < 44) return Result.failure(IllegalArgumentException("データサイズ不足: ${data.size}"))

        val salt: ByteArray = data.sliceArray(0 until 16)
        val iv: ByteArray = data.sliceArray(16 until 28)
        val tag: ByteArray = data.sliceArray(28 until 44)
        val rawData: ByteArray = data.sliceArray(44 until data.size)

        this.key = createKey(salt,password).getOrElse { error ->
            return Result.failure(error)
        }

        val decRawData = decryptData(rawData + tag, iv).getOrElse { error ->
            return Result.failure(error)
        }

        return runCatching {
            val metaData = Json.decodeFromString<ManifestMetaData>(decRawData)

            this.files = metaData.files.toMutableList();
            this.originalDirName = metaData.originalDirName

            this.sortFiles()
        }
    }

    open fun getContent(
        data: ByteArray,
        fileIndex: Int,
        contentIndex: Int
    ): Result<ByteArray>{
        if(this.fileCount == 0) return Result.failure(IllegalArgumentException("ファイルデータが存在しません"))

        val fileData = this.files.getOrNull(fileIndex) ?: return Result.failure(
            IndexOutOfBoundsException("ファイルの指定が範囲外です")
        )

        val contentData = fileData.contents.getOrNull(fileIndex) ?: return Result.failure(
            IndexOutOfBoundsException("コンテンツの指定が範囲外です")
        )

        val iv = contentData.iv.hexToByteArray()
        val tag = contentData.tag.hexToByteArray()

        val decRawData = decryptData(data + tag, iv).getOrElse { error ->
            return Result.failure(error)
        }

        return Result.success(decRawData.toByteArray())
    }

    private fun decryptData(
        content: ByteArray,
        iv: ByteArray
    ): Result<String> {
        val currentKey = this.key ?: return Result.failure(IllegalArgumentException("鍵が設定されていません"))

        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, currentKey, GCMParameterSpec(128, iv))

            String(cipher.doFinal(content))
        }
    }

    private fun createKey(
        salt: ByteArray,
        password: String
    ): Result<SecretKeySpec>{
        return runCatching {
            val spec = PBEKeySpec(password.toCharArray(), salt, 100000, 256)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")

            SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        }

    }

    fun sortFiles(){
        this.files.sortWith(
            compareBy { it.originalFileName }
        )

        this.files.forEach { data ->
            data.contents.sortWith(
                compareBy { it.name }
            )
        }
    }
}