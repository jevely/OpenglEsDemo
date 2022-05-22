package com.jevely.openglesdemo

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import com.jevely.openglesdemo.decoder.AudioDecoder
import com.jevely.openglesdemo.decoder.VideoDecoder
import java.util.concurrent.Executors

class MainActivity2 : AppCompatActivity() {

    private var drawer: IDrawer? = null
    private var surface: GLSurfaceView? = null

    private var path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        surface = findViewById(R.id.surface)
        path = filesDir.absolutePath + "/小视频.mp4"
        initRender()
    }

    private fun initRender() {
        drawer = VideoDrawer()
        //设置视频宽高
        (drawer as VideoDrawer).setVideoSize(1920, 1080)
        (drawer as VideoDrawer)?.getSurfaceTexture {
            //使用SurfaceTexture初始化一个Surface，并传递给MediaCodec使用
            initPlayer(Surface(it))
        }
        surface?.setEGLContextClientVersion(2)
        surface?.setRenderer(SimpleRender(drawer!!))
    }

    private fun initPlayer(sf: Surface) {
        val threadPool = Executors.newFixedThreadPool(10)

        val videoDecoder = VideoDecoder(path, null, sf)
        threadPool.execute(videoDecoder)

        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        videoDecoder.goOn()
        audioDecoder.goOn()
    }

}