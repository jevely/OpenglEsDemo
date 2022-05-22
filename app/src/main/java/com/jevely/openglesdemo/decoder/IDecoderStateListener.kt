package com.jevely.openglesdemo.decoder

interface IDecoderStateListener {
    fun decoderError(baseDecoder: BaseDecoder, s: String)
    fun decoderRunning(baseDecoder: BaseDecoder)
    fun decoderPause(baseDecoder: BaseDecoder)
    fun decoderFinish(baseDecoder: BaseDecoder)
    fun decoderDestroy(baseDecoder: BaseDecoder)
    fun decoderPrepare(baseDecoder: BaseDecoder)
}