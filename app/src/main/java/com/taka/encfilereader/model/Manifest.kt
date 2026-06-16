package com.taka.encfilereader.model

import javax.crypto.spec.SecretKeySpec

class Manifest (val path: String){
    val rawData: ByteArray? = null
    val salt: ByteArray? = null
    val iv: ByteArray? = null
    val key: SecretKeySpec? = null
    val files: Array<FileMetaData>? = null
}