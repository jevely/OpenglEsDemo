package com.jevely.openglesdemo.activity

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.jevely.openglesdemo.IDrawer
import com.jevely.openglesdemo.R
import com.jevely.openglesdemo.decoder.AudioDecoder
import com.jevely.openglesdemo.decoder.VideoDecoder
import com.jevely.openglesdemo.render.SimpleRender
import com.jevely.openglesdemo.render.SoulVideo2Drawer
import com.jevely.openglesdemo.render.SoulVideoDrawer
import java.util.concurrent.Executors

class MainActivity5 : AppCompatActivity() {

    private var path = ""
    lateinit var drawer: IDrawer
    private var gl_surface : GLSurfaceView ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main5)
        path = filesDir.absolutePath + "/小视频.mp4"
        gl_surface = findViewById(R.id.sfv)
        initRender()
    }

    private fun initRender() {
        // 使用“灵魂出窍”渲染器
        drawer = SoulVideoDrawer()
        drawer.setVideoSize(1920, 1080)
        (drawer as SoulVideoDrawer).getSurfaceTexture {
            initPlayer(Surface(it))
        }
        gl_surface?.setEGLContextClientVersion(2)
        val render = SimpleRender()
        render.addDrawer(drawer)
        gl_surface?.setRenderer(render)
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