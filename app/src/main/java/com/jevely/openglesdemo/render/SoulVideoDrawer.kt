package com.jevely.openglesdemo.render

import android.graphics.SurfaceTexture
import android.opengl.GLES11
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import com.jevely.openglesdemo.IDrawer
import com.jevely.openglesdemo.fbo.OpenGLFBOTool
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 视频
 */
class SoulVideoDrawer : IDrawer {

    private val mDefVertexCoors = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

    // 顶点坐标
    private var mVertexCoors = mDefVertexCoors

    // 纹理坐标
    private val mTextureCoors = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    //纹理ID
    private var mTextureId: Int = -1

    //OpenGL程序ID
    private var mProgram: Int = -1

    // 顶点坐标接收者
    private var mVertexPosHandler: Int = -1

    // 纹理坐标接收者
    private var mTexturePosHandler: Int = -1

    // 纹理接收者
    private var mTextureHandler: Int = -1

    private var mSurfaceTexture: SurfaceTexture? = null

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer

    private var mWorldWidth: Int = -1
    private var mWorldHeight: Int = -1
    private var mVideoWidth: Int = -1
    private var mVideoHeight: Int = -1

    //坐标变换矩阵
    private var mMatrix: FloatArray? = null

    //矩阵变换接收者
    private var mVertexMatrixHandler: Int = -1

    // 半透值接收者
    private var mAlphaHandler: Int = -1

    private var mAlpha = 1f

    private var mWidthRatio = 1f
    private var mHeightRatio = 1f

    private var mSftCb: ((SurfaceTexture) -> Unit)? = null

    //-------------灵魂出窍相关的变量--------------

    /**上下颠倒的顶点矩阵*/
    private val mReserveVertexCoors = floatArrayOf(
        -1f, 1f,
        1f, 1f,
        -1f, -1f,
        1f, -1f
    )

    // 灵魂帧缓冲
    private var mSoulFrameBuffer: Int = -1

    // 灵魂纹理ID
    private var mSoulTextureId: Int = -1

    // 灵魂纹理接收者
    private var mSoulTextureHandler: Int = -1

    // 灵魂缩放进度接收者
    private var mProgressHandler: Int = -1

    // 是否更新FBO纹理
    private var mDrawFbo: Int = 1

    // 更新FBO标记接收者
    private var mDrawFobHandler: Int = -1

    // 一帧灵魂的时间
    private var mModifyTime: Long = -1

    init {
        initPos()
    }

    private fun initPos() {
        val bb = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
        bb.order(ByteOrder.nativeOrder())
        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        mVertexBuffer = bb.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)

        val cc = ByteBuffer.allocateDirect(mTextureCoors.size * 4)
        cc.order(ByteOrder.nativeOrder())
        mTextureBuffer = cc.asFloatBuffer()
        mTextureBuffer.put(mTextureCoors)
        mTextureBuffer.position(0)
    }

    override fun setTextureID(id: Int) {
        mTextureId = id
        mSurfaceTexture = SurfaceTexture(id).apply {
            mSftCb?.invoke(this)
        }
    }

    override fun setVideoSize(videoW: Int, videoH: Int) {
        mVideoWidth = videoW
        mVideoHeight = videoH
    }

    override fun setWorldSize(worldW: Int, worldH: Int) {
        mWorldWidth = worldW
        mWorldHeight = worldH
    }

    override fun draw() {
        if (mTextureId != -1) {
            //【新增1: 初始化矩阵方法】
            initDefMatrix()
            //【步骤2: 创建、编译并启动OpenGL着色器】
            createGLPrg()
            // -------【步骤2:新增FBO部分】-----
            //【步骤2.1: 更新灵魂纹理】
            updateFBO()
            //【步骤2.2: 激活灵魂纹理单元】
            activateSoulTexture()
            // ---------------------------
            //【步骤3: 激活并绑定纹理单元】
            activateTexture()
            //【步骤4: 绑定图片到纹理单元】
            updateTexture()
            //【步骤3: 开始渲染绘制】
            doDraw()
        }
    }

    private fun initDefMatrix() {
        if (mMatrix != null) return
        if (mVideoWidth != -1 && mVideoHeight != -1 &&
            mWorldWidth != -1 && mWorldHeight != -1
        ) {
            mMatrix = FloatArray(16)
            val prjMatrix = FloatArray(16)
            val originRatio = mVideoWidth / mVideoHeight.toFloat()
            val worldRatio = mWorldWidth / mWorldHeight.toFloat()
            if (mWorldWidth > mWorldHeight) {
                if (originRatio > worldRatio) {
                    mHeightRatio = originRatio / worldRatio
                    Matrix.orthoM(
                        prjMatrix, 0,
                        -mWidthRatio, mWidthRatio,
                        -mHeightRatio, mHeightRatio,
                        -1f, 3f
                    )
                } else {// 原始比例小于窗口比例，缩放高度度会导致高度超出，因此，高度以窗口为准，缩放宽度
                    mWidthRatio = worldRatio / originRatio
                    Matrix.orthoM(
                        prjMatrix, 0,
                        -mWidthRatio, mWidthRatio,
                        -mHeightRatio, mHeightRatio,
                        -1f, 3f
                    )
                }
            } else {
                if (originRatio > worldRatio) {
                    mHeightRatio = originRatio / worldRatio
                    Matrix.orthoM(
                        prjMatrix, 0,
                        -mWidthRatio, mWidthRatio,
                        -mHeightRatio, mHeightRatio,
                        3f, 5f
                    )
                } else {// 原始比例小于窗口比例，缩放高度会导致高度超出，因此，高度以窗口为准，缩放宽度
                    mWidthRatio = worldRatio / originRatio
                    Matrix.orthoM(
                        prjMatrix, 0,
                        -mWidthRatio, mWidthRatio,
                        -mHeightRatio, mHeightRatio,
                        -1f, 3f
                    )
                }
            }

            //设置相机位置
            val viewMatrix = FloatArray(16)
            Matrix.setLookAtM(
                viewMatrix, 0,
                0f, 0f, 5.0f,
                0f, 0f, 0f,
                0f, 1.0f, 0f
            )
            //计算变换矩阵
            Matrix.multiplyMM(mMatrix, 0, prjMatrix, 0, viewMatrix, 0)
        }
    }

    // 平移
    fun translate(dx: Float, dy: Float) {
        Matrix.translateM(mMatrix, 0, dx * mWidthRatio * 2, -dy * mHeightRatio * 2, 0f)
    }

    private fun createGLPrg() {
        if (mProgram == -1) {
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader())

            //创建OpenGL ES程序，注意：需要在OpenGL渲染线程中创建，否则无法渲染
            mProgram = GLES20.glCreateProgram()
            //将顶点着色器加入到程序
            GLES20.glAttachShader(mProgram, vertexShader)
            //将片元着色器加入到程序中
            GLES20.glAttachShader(mProgram, fragmentShader)
            //连接到着色器程序
            GLES20.glLinkProgram(mProgram)

            mVertexPosHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mTexturePosHandler = GLES20.glGetAttribLocation(mProgram, "aCoordinate")
            //【注3：新增获取纹理接收者】
            mTextureHandler = GLES20.glGetUniformLocation(mProgram, "uTexture")
            //【新增2: 获取顶点着色器中的矩阵变量】
            mVertexMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMatrix")
            mAlphaHandler = GLES20.glGetAttribLocation(mProgram, "alpha")

            mSoulTextureHandler = GLES20.glGetUniformLocation(mProgram, "uSoulTexture")
            mProgressHandler = GLES20.glGetUniformLocation(mProgram, "progress")
            mDrawFobHandler = GLES20.glGetUniformLocation(mProgram, "drawFbo")
        }
        //使用OpenGL程序
        GLES20.glUseProgram(mProgram)
    }

    private fun updateFBO() {
        //【1，创建FBO纹理】
        if (mSoulTextureId == -1) {
            mSoulTextureId = OpenGLFBOTool.createFBOTexture(mVideoWidth, mVideoHeight)
        }
        // 【2，创建FBO】
        if (mSoulFrameBuffer == -1) {
            mSoulFrameBuffer = OpenGLFBOTool.createFrameBuffer()
        }
        // 【3，渲染到FBO】
        if (System.currentTimeMillis() - mModifyTime > 500) {
            mModifyTime = System.currentTimeMillis()
            // 绑定FBO
            OpenGLFBOTool.bindFBO(mSoulFrameBuffer, mSoulTextureId)
            // 配置FBO窗口
            configFboViewport()

//--------执行正常画面渲染，画面将渲染到FBO上--------------

            // 激活默认的纹理
            activateDefTexture()
            // 更新纹理
            updateTexture()
            // 绘制到FBO
            doDraw()

//---------------------------------------------------

            // 解绑FBO
            OpenGLFBOTool.unbindFBO()
            // 恢复默认绘制窗口
            configDefViewport()
        }
    }

    /**
     * 配置FBO窗口
     */
    private fun configFboViewport() {
        mDrawFbo = 1
        // 将变换矩阵回复为单位矩阵（将画面拉升到整个窗口大小，设置窗口比例和FBO纹理比例一致，画面刚好可以正常绘制到FBO纹理上）
        Matrix.setIdentityM(mMatrix, 0)
        // 设置颠倒的顶点坐标
        mVertexCoors = mReserveVertexCoors
        //重新初始化顶点坐标
        initPos()
        GLES20.glViewport(0, 0, mVideoWidth, mVideoHeight)
        //设置一个颜色状态
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        //使能颜色状态的值来清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    /**
     * 配置默认显示的窗口
     */
    private fun configDefViewport() {
        mDrawFbo = 0
        mMatrix = null
        // 恢复顶点坐标
        mVertexCoors = mDefVertexCoors
        initPos()
        initDefMatrix()
        // 恢复窗口
        GLES20.glViewport(0, 0, mWorldWidth, mWorldHeight)
    }

    private fun activateDefTexture() {
        activateTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId, 0, mTextureHandler)
    }

    private fun activateSoulTexture() {
        activateTexture(GLES11.GL_TEXTURE_2D, mSoulTextureId, 1, mSoulTextureHandler)
    }

    private fun activateTexture(type: Int, textureId: Int, index: Int, textureHandler: Int) {
        //激活指定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index)
        //绑定纹理ID到纹理单元
        GLES20.glBindTexture(type, textureId)
        //将激活的纹理单元传递到着色器里面
        GLES20.glUniform1i(textureHandler, index)
        //配置边缘过渡参数
        GLES20.glTexParameterf(type, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(type, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        //根据type创建顶点着色器或者片元着色器
        val shader = GLES20.glCreateShader(type)
        //将资源加入到着色器中，并编译
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        return shader
    }

    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                "precision mediump float;" +
                //【新增4: 矩阵变量】
                "uniform mat4 uMatrix;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "attribute float alpha;" +
                "varying float inAlpha;" +
                "void main() {" +
                //【新增5: 坐标变换】
                "    gl_Position = uMatrix*aPosition;" +
                "    vCoordinate = aCoordinate;" +
                "    inAlpha = alpha;" +
                "}"
    }

    private fun getFragmentShader(): String {
        //一定要加换行"\n"，否则会和下一行的precision混在一起，导致编译出错
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "varying vec2 vCoordinate;" +
                "varying float inAlpha;" +
                "uniform samplerExternalOES uTexture;" +
                "uniform float progress;" +
                "uniform int drawFbo;" +
                "uniform sampler2D uSoulTexture;" +
                "void main() {" +
                // 透明度[0,0.4]
                "float alpha = 0.6 * (1.0 - progress);" +
                // 缩放比例[1.0,1.5]
                "float scale = 1.0 + (1.5 - 1.0) * progress;" +

                // 放大纹理坐标
                "float soulX = 0.5 + (vCoordinate.x - 0.5) / scale;\n" +
                "float soulY = 0.5 + (vCoordinate.y - 0.5) / scale;\n" +
                "vec2 soulTextureCoords = vec2(soulX, soulY);" +
                // 获取对应放大纹理坐标下的像素(颜色值rgba)
                "vec4 soulMask = texture2D(uSoulTexture, soulTextureCoords);" +

                "vec4 color = texture2D(uTexture, vCoordinate);" +

                "if (drawFbo == 0) {" +
                // 颜色混合 默认颜色混合方程式 = mask * (1.0-alpha) + weakMask * alpha
                "    gl_FragColor = color * (1.0 - alpha) + soulMask * alpha;" +
                "} else {" +
                "   gl_FragColor = vec4(color.r, color.g, color.b, inAlpha);" +
                "}" +
                "}"
    }

    private fun activateTexture() {
        //激活指定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //绑定纹理ID到纹理单元
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
        //将激活的纹理单元传递到着色器里面
        GLES20.glUniform1i(mTextureHandler, 0)
        //配置边缘过渡参数
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
    }

    private fun updateTexture() {
        mSurfaceTexture?.updateTexImage()
    }

    private fun doDraw() {
        //启用顶点的句柄
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        GLES20.glEnableVertexAttribArray(mTexturePosHandler)
        // 【新增3: 将变换矩阵传递给顶点着色器】
        GLES20.glUniformMatrix4fv(mVertexMatrixHandler, 1, false, mMatrix, 0)
        //设置着色器参数
        GLES20.glVertexAttribPointer(mVertexPosHandler, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        GLES20.glVertexAttribPointer(
            mTexturePosHandler,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            mTextureBuffer
        )
        GLES20.glVertexAttrib1f(mAlphaHandler, mAlpha)
        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    override fun release() {
        releaseFBO()
        GLES20.glDisableVertexAttribArray(mVertexPosHandler)
        GLES20.glDisableVertexAttribArray(mTexturePosHandler)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }

    private fun releaseFBO() {
        val fbs = IntArray(1)
        fbs[0] = mSoulFrameBuffer
        val texts = IntArray(1)
        texts[0] = mSoulTextureId

        OpenGLFBOTool.deleteFBO(fbs, texts)
    }

    override fun setAlpha(alpha: Float) {
        mAlpha = alpha
    }

    fun getSurfaceTexture(cb: (st: SurfaceTexture) -> Unit) {
        mSftCb = cb
    }

    /**
     * 缩放
     */
    fun scale(sx: Float, sy: Float) {
        Matrix.scaleM(mMatrix, 0, sx, sy, 1f)
        mWidthRatio /= sx
        mHeightRatio /= sy
    }

}