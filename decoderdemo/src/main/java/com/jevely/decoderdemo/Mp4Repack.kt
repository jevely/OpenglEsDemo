package com.jevely.decoderdemo

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME
import android.util.Log
import java.nio.ByteBuffer

class Mp4Repack(private val context: Context,private val path: String) {
    private val TAG = "MP4Repack"

    //初始化音视频分离器
    private val mAExtractor: AudioExtractor = AudioExtractor(path)
    private val mVExtractor: VideoExtractor = VideoExtractor(path)

    //初始化封装器
    private val mMuxer: MediaMuxerTool = MediaMuxerTool(context)

    /**
     *启动重封装
     */
    fun start() {
        val audioFormat = mAExtractor.getFormat()
        val videoFormat = mVExtractor.getFormat()

        //判断是否有音频数据，没有音频数据则告诉封装器，忽略音频轨道
        if (audioFormat != null) {
            mMuxer.addAudioTrack(audioFormat)
        } else {
            mMuxer.setNoAudio()
        }
        //判断是否有视频数据，没有音频数据则告诉封装器，忽略视频轨道
        if (videoFormat != null) {
            mMuxer.addVideoTrack(videoFormat)
        } else {
            mMuxer.setNoVideo()
        }

        //启动线程
        Thread {
            val buffer = ByteBuffer.allocate(500 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()

            //音频数据分离和写入
            if (audioFormat != null) {
                var size = mAExtractor.readBuffer(buffer)
                while (size > 0) {
                    bufferInfo.set(
                        0, size, mAExtractor.getCurrentTimestamp(),
                        BUFFER_FLAG_KEY_FRAME
                    )

                    mMuxer.writeAudioData(buffer, bufferInfo)

                    size = mAExtractor.readBuffer(buffer)
                }
            }

            //视频数据分离和写入
            if (videoFormat != null) {
                var size = mVExtractor.readBuffer(buffer)
                while (size > 0) {
                    bufferInfo.set(
                        0, size, mVExtractor.getCurrentTimestamp(),
                        BUFFER_FLAG_KEY_FRAME
                    )

                    mMuxer.writeVideoData(buffer, bufferInfo)

                    size = mVExtractor.readBuffer(buffer)
                }
            }
            mAExtractor.stop()
            mVExtractor.stop()
            mMuxer.release()
            Log.i(TAG, "MP4 重打包完成")
        }.start()
    }
}