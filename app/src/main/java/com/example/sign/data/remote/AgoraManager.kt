package com.example.sign.data.remote

import android.content.Context
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoEncoderConfiguration

class AgoraManager(private val context: Context) {
    private var rtcEngine: RtcEngine? = null
    
    // Replace with your actual Agora App ID from Agora Console
    private val appId = "f966399754444252901dfca303bfca65"

    fun init(handler: IRtcEngineEventHandler) {
        try {
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = appId
            config.mEventHandler = handler
            rtcEngine = RtcEngine.create(config)
            
            rtcEngine?.enableVideo()
            rtcEngine?.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun joinChannel(channelName: String, token: String? = null, uid: Int = 0) {
        rtcEngine?.joinChannel(token, channelName, null, uid)
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    fun destroy() {
        RtcEngine.destroy()
        rtcEngine = null
    }
}
