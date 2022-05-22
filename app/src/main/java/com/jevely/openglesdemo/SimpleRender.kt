package com.jevely.openglesdemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.jevely.openglesdemo.IDrawer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleRender(private val iDrawer: IDrawer) : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        val textureID = createTextureIds(1)[0]
        iDrawer.setTextureID(textureID)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        //设置OpenGL窗口坐标
        iDrawer.setWorldSize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        iDrawer.draw()
    }

    fun createTextureIds(count: Int): IntArray {
        val texture = IntArray(count)
        GLES20.glGenTextures(count, texture, 0) //生成纹理
        return texture
    }
}