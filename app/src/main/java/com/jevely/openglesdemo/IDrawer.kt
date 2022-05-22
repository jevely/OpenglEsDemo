package com.jevely.openglesdemo

interface IDrawer {
    //设置视频的原始宽高
    fun setVideoSize(videoW: Int, videoH: Int)
    //设置OpenGL窗口宽高
    fun setWorldSize(worldW: Int, worldH: Int)
    fun draw()
    fun setTextureID(id : Int)
    fun release()
}