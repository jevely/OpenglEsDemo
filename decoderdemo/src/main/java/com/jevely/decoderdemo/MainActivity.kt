package com.jevely.decoderdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.SurfaceView
import java.io.File
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    lateinit var surfaceview: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surfaceview = findViewById(R.id.surfaceview)
        initPlayer()
    }

    private fun initPlayer() {
        val path = filesDir.absolutePath + "/小视频.mp4"

        //创建线程池
        val threadPool = Executors.newFixedThreadPool(2)

        //创建视频解码器
        val videoDecoder = VideoDecoder(path, surfaceview, null)
        threadPool.execute(videoDecoder)
        //创建音频解码器
        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        videoDecoder.setStateListener(object : IDecoderStateListener {
            override fun decoderError(baseDecoder: BaseDecoder, s: String) {
                Log.d("LJW", "报错了:$s")
            }

            override fun decoderRunning(baseDecoder: BaseDecoder) {

            }

            override fun decoderPause(baseDecoder: BaseDecoder) {

            }

            override fun decoderFinish(baseDecoder: BaseDecoder) {

            }

            override fun decoderDestroy(baseDecoder: BaseDecoder) {

            }

            override fun decoderPrepare(baseDecoder: BaseDecoder) {

            }

        })

        //开启播放
        videoDecoder.goOn()
        audioDecoder.goOn()
    }
}