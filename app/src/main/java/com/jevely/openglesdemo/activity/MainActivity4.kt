package com.jevely.openglesdemo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Surface
import android.view.SurfaceView
import com.jevely.openglesdemo.R
import com.jevely.openglesdemo.decoder.AudioDecoder
import com.jevely.openglesdemo.decoder.VideoDecoder
import com.jevely.openglesdemo.render.CustomGLRender
import com.jevely.openglesdemo.render.VideoDrawer
import java.util.concurrent.Executors

class MainActivity4 : AppCompatActivity() {

    private var path1 = ""
    private var path2 = ""

    private val threadPool = Executors.newFixedThreadPool(10)

    private var mRenderer = CustomGLRender()

    private var sfv: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
        sfv = findViewById(R.id.sfv)
        path1 = filesDir.absolutePath + "/小视频.mp4"
        path2 = filesDir.absolutePath + "/dynamic_8.mp4"
        initFirstVideo()
        initSecondVideo()
        setRenderSurface()
    }

    private fun initFirstVideo() {
        val drawer = VideoDrawer()
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initPlayer(path1, Surface(it), true)
        }
        mRenderer.addDrawer(drawer)
    }

    private fun initSecondVideo() {
        val drawer = VideoDrawer()
        drawer.setAlpha(0.5f)
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initPlayer(path2, Surface(it), false)
        }
        mRenderer.addDrawer(drawer)

        Handler().postDelayed({
            drawer.scale(0.5f, 0.5f)
        }, 1000)
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

    private fun setRenderSurface() {
        mRenderer.setSurface(sfv!!)
    }

}