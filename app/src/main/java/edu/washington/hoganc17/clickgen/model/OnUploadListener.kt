package edu.washington.hoganc17.clickgen.model

import java.io.InputStream

interface OnUploadListener {
    fun onFileUploaded(songStream: InputStream, clickStream: InputStream)

    fun onFileUploaded(mixedStream: InputStream, title: String?)

    fun onFileUploaded(trio: AudioTrio)
}