package com.taka.encfilereader.model

import kotlinx.serialization.json.Json
import java.text.Collator
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.comparisons.compareBy

class Manifest (val dirName: String){
    var originalDirName: String? = null
    private var key: SecretKeySpec? = null
    private var files: MutableList<FileMetaData> = mutableListOf()
    val fileCount: Int
        get() = files.size

    fun setBuffer(
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
            val metaData = Json.decodeFromString<ManifestMetaData>(String(decRawData))

            this.files = metaData.files.toMutableList()
            this.originalDirName = metaData.originalDirName

            this.sortFiles()
        }
    }

    fun getFileMetaData(index: Int): Result<FileMetaData>{
        val fileMetaData = this.files.getOrNull(index) ?: return Result.failure(
            IndexOutOfBoundsException("ファイルの指定が範囲外です")
        )

        return Result.success(fileMetaData)
    }

    fun getContentData(
        data: ByteArray,
        contentMetaData: ContentMetaData
    ): Result<ByteArray>{
        val iv = contentMetaData.iv.hexToByteArray()
        val tag = contentMetaData.tag.hexToByteArray()

        val decRawData = decryptData(data + tag, iv).getOrElse { error ->
            return Result.failure(error)
        }

        return Result.success(decRawData)
    }

    private fun decryptData(
        content: ByteArray,
        iv: ByteArray
    ): Result<ByteArray> {
        val currentKey = this.key ?: return Result.failure(IllegalArgumentException("鍵が設定されていません"))

        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, currentKey, GCMParameterSpec(128, iv))

            cipher.doFinal(content)
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
        val collator = Collator.getInstance(Locale.getDefault()).apply {
            strength = Collator.PRIMARY
        }

        val naturalOrderComparator = Comparator<String> { s1, s2 ->
            val parts1 = s1.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)".toRegex())
            val parts2 = s2.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)".toRegex())

            for (i in 0 until minOf(parts1.size, parts2.size)) {
                val p1 = parts1[i]
                val p2 = parts2[i]

                val res = if (p1.all { it.isDigit() } && p2.all { it.isDigit() }) {
                    p1.toBigInteger().compareTo(p2.toBigInteger())
                } else {
                    collator.compare(p1, p2)
                }

                if (res != 0) return@Comparator res
            }
            parts1.size.compareTo(parts2.size)
        }

        this.files.sortWith(Comparator { f1, f2 ->
            naturalOrderComparator.compare(f1.originalFileName, f2.originalFileName)
        })

        this.files.forEach { data ->
            data.contents.sortWith(Comparator { c1, c2 ->
                naturalOrderComparator.compare(c1.name, c2.name)
            })
        }
    }
}