package com.jevely.openglesdemo.decoder

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class MediaMuxerTool(private val context: Context) {

    private val TAG = "MMuxer"

    private var mPath: String

    private var mMediaMuxer: MediaMuxer? = null

    private var mVideoTrackIndex = -1
    private var mAudioTrackIndex = -1

    private var mIsAudioTrackAdd = false
    private var mIsVideoTrackAdd = false

    private var mIsStart = false

    init {
        val fileName = "LVideo_" + SimpleDateFormat("yyyyMM_dd-HHmmss").format(Date()) + ".mp4"
        val filePath = context.filesDir.absolutePath
        mPath = filePath + fileName
        mMediaMuxer = MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    fun addVideoTrack(mediaFormat: MediaFormat) {
        if (mMediaMuxer != null) {
            mVideoTrackIndex = try {
                mMediaMuxer!!.addTrack(mediaFormat)
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
            mIsVideoTrackAdd = true
            startMuxer()
        }
    }

    fun addAudioTrack(mediaFormat: MediaFormat) {
        if (mMediaMuxer != null) {
            mAudioTrackIndex = try {
                mMediaMuxer!!.addTrack(mediaFormat)
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
            mIsAudioTrackAdd = true
            startMuxer()
        }
    }

    /**
     *忽略音频轨道
     */
    fun setNoAudio() {
        if (mIsAudioTrackAdd) return
        mIsAudioTrackAdd = true
        startMuxer()
    }

    /**
     *忽略视频轨道
     */
    fun setNoVideo() {
        if (mIsVideoTrackAdd) return
        mIsVideoTrackAdd = true
        startMuxer()
    }

    private fun startMuxer() {
        if (mIsAudioTrackAdd && mIsVideoTrackAdd) {
            mMediaMuxer?.start()
            mIsStart = true
            Log.i(TAG, "启动混合器，等待数据输入...")
        }
    }

    fun writeVideoData(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (mIsStart) {
            mMediaMuxer?.writeSampleData(mVideoTrackIndex, byteBuffer, bufferInfo)
        }
    }

    fun writeAudioData(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (mIsStart) {
            mMediaMuxer?.writeSampleData(mAudioTrackIndex, byteBuffer, bufferInfo)
        }
    }

    fun release() {
        mIsAudioTrackAdd = false
        mIsVideoTrackAdd = false
        try {
            mMediaMuxer?.stop()
            mMediaMuxer?.release()
            mMediaMuxer = null
            Log.i(TAG, "混合器退出...")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}