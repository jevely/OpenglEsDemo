package com.jevely.openglesdemo.egl

import android.opengl.EGLContext
import android.opengl.EGLSurface

class EGLSurfaceHolder {

    private val TAG = "EGLSurfaceHolder"

    private lateinit var mEGLCore: EGLCore

    private var mEGLSurface: EGLSurface? = null

    fun init(shareContext: EGLContext? = null, flags: Int) {
        mEGLCore = EGLCore()
        mEGLCore.init(shareContext, flags)
    }

    fun createEGLSurface(surface: Any?, width: Int = -1, height: Int = -1) {
        mEGLSurface = if (surface != null) {
            mEGLCore.createWindowSurface(surface)
        } else {
            mEGLCore.createOffscreenSurface(width, height)
        }
    }

    fun makeCurrent() {
        if (mEGLSurface != null) {
            mEGLCore.makeCurrent(mEGLSurface!!)
        }
    }

    fun swapBuffers() {
        if (mEGLSurface != null) {
            mEGLCore.swapBuffers(mEGLSurface!!)
        }
    }

    fun destroyEGLSurface() {
        if (mEGLSurface != null) {
            mEGLCore.destroySurface(mEGLSurface!!)
            mEGLSurface = null
        }
    }

    fun release() {
        mEGLCore.release()
    }

}