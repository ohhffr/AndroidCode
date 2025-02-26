package com.example.englishgptapplication.ui.util

import android.media.MediaPlayer
import android.media.PlaybackParams
import java.io.IOException

//AudioPlayer 类封装了 Android 系统的 MediaPlayer，提供了音频播放、暂停、设置播放速度、循环播放等功能，同时处理了播放过程中的异常情况
class AudioPlayer {
    val mediaPlayer = MediaPlayer()
    private var currentUrl: String? = null
    private var isPrepared = false
    private var pendingSpeed: Float? = null // 保存待处理的播放速度

    fun isPlaying(): Boolean = mediaPlayer.isPlaying

    fun playLoop(url: String) {
        // 如果当前正在播放的音频就是指定的音频，则不做处理
        if (url == currentUrl && isPlaying()) return

        mediaPlayer.reset()
        try {
            // 设置音频数据源为指定的 URL
            mediaPlayer.setDataSource(url)
            mediaPlayer.isLooping = true   // 设置音频为循环播放模式
            mediaPlayer.prepareAsync()// 异步准备音频，避免阻塞主线程
            mediaPlayer.setOnPreparedListener { mp ->
                isPrepared = true // 确保标记为已准备
                // 如果有待处理的播放速度，应用该速度并清空待处理速度
                pendingSpeed?.let { speed ->
                    applyPlaybackSpeed(speed)
                    pendingSpeed = null
                }
                mp.start()
                currentUrl = url
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun play(url: String) {
        if (url == currentUrl && isPlaying()) return

        mediaPlayer.reset()
        isPrepared = false
        pendingSpeed = null // 重置待处理速度
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mp ->
                isPrepared = true
                // 应用待处理的播放速度
                pendingSpeed?.let { speed ->
                    applyPlaybackSpeed(speed)
                    pendingSpeed = null
                }
                mp.start()
                currentUrl = url
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    // 设置音频的播放速度
    fun setPlaybackSpeed(speed: Float) {
        if (isPrepared) {
            applyPlaybackSpeed(speed)
        } else {
            // 保存速度设置，待准备完成后应用
            pendingSpeed = speed
        }
    }
    // 应用指定的播放速度
    private fun applyPlaybackSpeed(speed: Float) {
        try {
            // 创建 PlaybackParams 对象并设置播放速度
            val params = PlaybackParams().apply {
                this.speed = speed
            }
            // 将设置好的播放速度应用到 MediaPlayer
            mediaPlayer.playbackParams = params
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun getCurrentPosition(): Int = mediaPlayer.currentPosition

    fun start() {
        if (isPrepared && !isPlaying()) mediaPlayer.start()
    }

    fun pause() {
        if (isPlaying()) mediaPlayer.pause()
    }

    fun release() {
        mediaPlayer.release()
        pendingSpeed = null
    }
}