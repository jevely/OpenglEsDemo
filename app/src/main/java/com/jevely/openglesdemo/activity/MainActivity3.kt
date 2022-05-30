package com.jevely.openglesdemo.activity

import android.os.Bundle
import android.os.Handler
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.jevely.openglesdemo.DefGLSurfaceVIew
import com.jevely.openglesdemo.R
import com.jevely.openglesdemo.decoder.AudioDecoder
import com.jevely.openglesdemo.decoder.VideoDecoder
import com.jevely.openglesdemo.render.SimpleRender
import com.jevely.openglesdemo.render.VideoDrawer
import java.util.concurrent.Executors

class MainActivity3 : AppCompatActivity() {

    private lateinit var gl_surface: DefGLSurfaceVIew
    private var path1 = ""
    private var path2 = ""

    private val render = SimpleRender()

    private val threadPool = Executors.newFixedThreadPool(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        gl_surface = findViewById(R.id.gl_surface)
        path1 = filesDir.absolutePath + "/小视频.mp4"
        path2 = filesDir.absolutePath + "/dynamic_8.mp4"

        initFirstVideo()
        initSecondVideo()
        initRender()
    }

    private fun initFirstVideo() {
        val drawer = VideoDrawer()
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initPlayer(path1, Surface(it), true)
        }

        render.addDrawer(drawer)
        gl_surface.addDrawer(drawer)

        Handler().postDelayed(Runnable { drawer.scale(0.5f,0.5f) },1000)
    }

    private fun initSecondVideo() {
        val drawer = VideoDrawer()
        drawer.setAlpha(0.5f)
        drawer.setVideoSize(720, 1280)
        drawer.getSurfaceTexture {
            initPlayer(path2, Surface(it), false)
        }
        render.addDrawer(drawer)
    }

    private fun initPlayer(path: String, sf: Surface, withSound: Boolean) {
        val videoDecoder = VideoDecoder(path, null, sf)
        threadPool.execute(videoDecoder)
        videoDecoder.goOn()

        if (withSound) {
            val audioDecoder = AudioDecoder(path)
            threadPool.execute(audioDecoder)
            audioDecoder.goOn()
        }
    }

    private fun initRender() {
        gl_surface.setEGLContextClientVersion(2)
        gl_surface.setRenderer(render)
    }

}