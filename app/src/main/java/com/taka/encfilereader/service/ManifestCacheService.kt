package com.taka.encfilereader.service

import com.jakewharton.disklrucache.DiskLruCache
import java.io.File

class ManifestCacheService(private val cacheDir: File) {
    private val maxDiskCache = 100 * 1024 * 1024
    private var diskCache = openDiskCache()

    private fun openDiskCache(): DiskLruCache {
        return DiskLruCache.open(
            File(cacheDir, "manifest_cache"),
            1,
            1,
            maxDiskCache.toLong()
        )
    }

    fun get(dirName: String): ByteArray? {
        return try {
            diskCache.get(dirName)?.use { snapshot ->
                snapshot.getInputStream(0).readBytes()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun save(dirName: String, data: ByteArray) {
        try {
            diskCache.edit(dirName)?.apply {
                newOutputStream(0).use { it.write(data) }
                commit()
            }
            diskCache.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun clearAll() {
        try {
            diskCache.delete()
            diskCache = openDiskCache()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            diskCache.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}