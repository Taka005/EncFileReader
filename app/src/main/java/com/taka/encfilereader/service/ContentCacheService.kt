package com.taka.encfilereader.service

import android.util.LruCache
import com.jakewharton.disklrucache.DiskLruCache
import java.io.File
import java.security.MessageDigest

class ContentCacheService(cacheDir: File){
    private val diskCache = DiskLruCache.open(
        File(cacheDir, "content_cache"),
        1,
        1,
        100 * 1024 * 1024
    )

    private val memoryCache = object : LruCache<String, ByteArray>(50 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ByteArray): Int {
            return value.size
        }
    }

    val diskCacheSize: Int
        get() = diskCache.size().toInt()

    val memoryCacheSize: Int
        get() = memoryCache.size()

    private fun hashKey(key: String): String {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(key.toByteArray())
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun get(key: String): ByteArray?{
        val hashedKey = hashKey(key)

        memoryCache.get(hashedKey)?.let {
            return it
        }

        return try {
            diskCache.get(hashedKey)?.use { snapshot ->
                snapshot.getInputStream(0).readBytes()
            }?.also {
                memoryCache.put(hashedKey, it)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun save(
        key: String,
        data: ByteArray,
        isDiskCache: Boolean = true
    ){
        val hashedKey = hashKey(key)

        memoryCache.put(hashedKey, data)

        if(!isDiskCache) return

        try {
            diskCache.edit(hashedKey)?.apply {
                newOutputStream(0).use {it.write(data) }

                commit()
            }
            diskCache.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close(){
        try {
            diskCache.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}