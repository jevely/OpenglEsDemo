package com.jevely.openglesdemo.egl;

/**
 * 渲染状态
 */
public enum RenderState {
    NO_SURFACE, //没有有效的surface
    FRESH_SURFACE, //持有一个未初始化的新的surface
    SURFACE_CHANGE, // surface尺寸变化
    RENDERING, //初始化完毕，可以开始渲染
    SURFACE_DESTROY, //surface销毁
    STOP //停止绘制
}