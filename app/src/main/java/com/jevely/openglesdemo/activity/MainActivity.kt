package com.jevely.openglesdemo.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.jevely.openglesdemo.IDrawer
import com.jevely.openglesdemo.R
import com.jevely.openglesdemo.render.BitmapDrawer
import com.jevely.openglesdemo.render.SimpleRender

class MainActivity : AppCompatActivity() {

    private var drawer: IDrawer? = null
    private var surface: GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surface = findViewById(R.id.surface)
//        drawer = TriangleDrawer()
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.testimg)
        drawer = BitmapDrawer(bitmap)
        initRender(drawer)

        findViewById<Button>(R.id.bt).setOnClickListener {
            startActivity(Intent(this@MainActivity, MainActivity2::class.java))
        }

        findViewById<Button>(R.id.bt2).setOnClickListener {
            startActivity(Intent(this@MainActivity, MainActivity3::class.java))
        }

        findViewById<Button>(R.id.bt3).setOnClickListener {
            startActivity(Intent(this@MainActivity, MainActivity4::class.java))
        }
    }

    private fun initRender(drawer: IDrawer?) {
        surface?.setEGLContextClientVersion(2)
        val simpleRender = SimpleRender()
        simpleRender.addDrawer(drawer!!)
        surface?.setRenderer(simpleRender)
    }

    override fun onDestroy() {
        drawer?.release()
        super.onDestroy()
    }
}