package com.taka.encfilereader.manager

import com.taka.encfilereader.service.StorageService
class StorageManager{
    private var _storage: StorageService? = null
    private var _password: String? = null

    val storage: StorageService?
        get() = _storage

    val password: String?
        get() = _password

    fun setCredentials(baseUrl: String, password: String) {
        _storage = StorageService(baseUrl)
        _password = password
    }

    fun isInitialized(): Boolean = _storage != null && _password != null
}