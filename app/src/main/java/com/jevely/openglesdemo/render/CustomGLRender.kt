package com.jevely.openglesdemo.render

import android.opengl.GLES20
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.jevely.openglesdemo.IDrawer
import com.jevely.openglesdemo.OpenglTool
import com.jevely.openglesdemo.egl.EGLSurfaceHolder
import com.jevely.openglesdemo.egl.EGL_RECORDABLE_ANDROID
import com.jevely.openglesdemo.egl.RenderState
import java.lang.ref.WeakReference

class CustomGLRender : SurfaceHolder.Callback {

    //OpenGL渲染线程
    private val mThread = RenderThread()

    //页面上的SurfaceView弱引用
    private var mSurfaceView: WeakReference<SurfaceView>? = null

    //所有的绘制器
    private val mDrawers = mutableListOf<IDrawer>()

    init {
        //启动渲染线程
        mThread.start()
    }

    /**
     * 设置SurfaceView
     */
    fun setSurface(surface: SurfaceView) {
        mSurfaceView = WeakReference(surface)
        surface.holder.addCallback(this)

        surface.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener{
            override fun onViewDetachedFromWindow(v: View?) {
                mThread.onSurfaceStop()
            }

            override fun onViewAttachedToWindow(v: View?) {
            }
        })
    }

    /**
     * 添加绘制器
     */
    fun addDrawer(drawer: IDrawer) {
        mDrawers.add(drawer)
    }

//    fun setRenderMode(mode: RenderMode) {
//        mThread.setRenderMode(mode)
//    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mThread.onSurfaceCreate()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        mThread.onSurfaceChange(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mThread.onSurfaceDestroy()
    }

    inner class RenderThread: Thread() {

        // 渲染状态
        private var mState = RenderState.NO_SURFACE

        private var mEGLSurface: EGLSurfaceHolder? = null

        // 是否绑定了EGLSurface
        private var mHaveBindEGLContext = false

        //是否已经新建过EGL上下文，用于判断是否需要生产新的纹理ID
        private var mNeverCreateEglContext = true

        private var mWidth = 0
        private var mHeight = 0

        private val mWaitLock = Object()

//        private var mRenderMode = RenderMode.RENDER_WHEN_DIRTY

        //------------第1部分：线程等待与解锁-----------------

        private fun holdOn() {
            synchronized(mWaitLock) {
                mWaitLock.wait()
            }
        }

        private fun notifyGo() {
            synchronized(mWaitLock) {
                mWaitLock.notify()
            }
        }

//        fun setRenderMode(mode: RenderMode) {
//            mRenderMode = mode
//        }

        //------------第2部分：Surface声明周期转发函数------------

        fun onSurfaceCreate() {
            mState = RenderState.FRESH_SURFACE
            notifyGo()
        }

        fun onSurfaceChange(width: Int, height: Int) {
            mWidth = width
            mHeight = height
            mState = RenderState.SURFACE_CHANGE
            notifyGo()
        }

        fun onSurfaceDestroy() {
            mState = RenderState.SURFACE_DESTROY
            notifyGo()
        }

        fun onSurfaceStop() {
            mState = RenderState.STOP
            notifyGo()
        }

        //------------第3部分：OpenGL渲染循环------------

        override fun run() {
            // 【1】初始化EGL
            initEGL()
            while (true) {
                when (mState) {
                    RenderState.FRESH_SURFACE -> {
                        //【2】使用surface初始化EGLSurface，并绑定上下文
                        createEGLSurfaceFirst()
                        holdOn()
                    }
                    RenderState.SURFACE_CHANGE -> {
                        createEGLSurfaceFirst()
                        //【3】初始化OpenGL世界坐标系宽高
                        GLES20.glViewport(0, 0, mWidth, mHeight)
                        configWordSize()
                        mState = RenderState.RENDERING
                    }
                    RenderState.RENDERING -> {
                        //【4】进入循环渲染
                        render()
                    }
                    RenderState.SURFACE_DESTROY -> {
                        //【5】销毁EGLSurface，并解绑上下文
                        destroyEGLSurface()
                        mState = RenderState.NO_SURFACE
                    }
                    RenderState.STOP -> {
                        //【6】释放所有资源
                        releaseEGL()
                        return
                    }
                    else -> {
                        holdOn()
                    }
                }
                sleep(20)
            }
        }

        //------------第4部分：EGL相关操作------------

        private fun initEGL() {
            mEGLSurface = EGLSurfaceHolder()
            mEGLSurface?.init(null, EGL_RECORDABLE_ANDROID)
        }

        private fun createEGLSurfaceFirst() {
            if (!mHaveBindEGLContext) {
                mHaveBindEGLContext = true
                createEGLSurface()
                if (mNeverCreateEglContext) {
                    mNeverCreateEglContext = false
                    generateTextureID()
                }
            }
        }

        private fun createEGLSurface() {
            mEGLSurface?.createEGLSurface(mSurfaceView?.get()?.holder?.surface)
            mEGLSurface?.makeCurrent()
        }

        private fun destroyEGLSurface() {
            mEGLSurface?.destroyEGLSurface()
            mHaveBindEGLContext = false
        }

        private fun releaseEGL() {
            mEGLSurface?.release()
        }

        //------------第5部分：OpenGL ES相关操作-------------

        private fun generateTextureID() {
            val textureIds = OpenglTool.createTextureIds(mDrawers.size)
            for ((idx, drawer) in mDrawers.withIndex()) {
                drawer.setTextureID(textureIds[idx])
            }
        }

        private fun configWordSize() {
            mDrawers.forEach { it.setWorldSize(mWidth, mHeight) }
        }

        private fun render() {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            mDrawers.forEach { it.draw() }
            mEGLSurface?.swapBuffers()
        }

    }

}