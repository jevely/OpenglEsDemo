package com.jevely.openglesdemo

import android.opengl.GLES20

object OpenglTool {

    fun createTextureIds(count: Int): IntArray {
        val texture = IntArray(count)
        GLES20.glGenTextures(count, texture, 0) //็ๆ็บน็
        return texture
    }

}