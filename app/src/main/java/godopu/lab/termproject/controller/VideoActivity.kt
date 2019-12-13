package godopu.lab.termproject.controller

import android.Manifest
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlaybackControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import godopu.lab.termproject.R
import godopu.lab.termproject.controller.components.VideoService
import godopu.lab.termproject.model.HttpService


class VideoActivity : AppCompatActivity() {

    private var service: HttpService? = null

    private val STATE_RESUME_WINDOW = "resumeWindow"
    private val STATE_RESUME_POSITION = "resumePosition"
    private val STATE_PLAYER_FULLSCREEN = "playerFullscreen"


    private var mExoPlayerView: PlayerView? = null
    private var mExoPlayerFullscreen = false
    private var mFullScreenIcon: ImageView? = null
    private var mFullScreenDialog: Dialog? = null
    private var videoService: VideoService? = null

    private val ServiceConnection = object:android.content.ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            videoService = (iBinder as VideoService.LocalBinder).getService()
            if(mExoPlayerView == null){
                mExoPlayerView = findViewById(R.id.exoplayer)
                initFullScreenDialog()
                initFullScreenButton()
                videoService!!.initExoPlayer(mExoPlayerView as PlayerView)
            }

            if(mExoPlayerFullscreen){
                openFullscreenDialog()
            }
        }
    }

    private fun initFullScreenButton(){
        val controlView = mExoPlayerView!!.findViewById<PlaybackControlView>(R.id.exo_controller)
        mFullScreenIcon = controlView!!.findViewById(R.id.exo_fullscreen_icon)
        val mFullScreenButton = controlView.findViewById<FrameLayout>(R.id.exo_fullscreen_button)

        mFullScreenButton.setOnClickListener(object:View.OnClickListener {
            override fun onClick(v: View?) {
                if(mExoPlayerFullscreen){
                    this@VideoActivity.runOnUiThread {
                        openFullscreenDialog()
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }else{
                    this@VideoActivity.runOnUiThread {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        closeFullscreenDialog()
                    }
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        setupPermissions()
        if (savedInstanceState != null) {
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen)
        super.onSaveInstanceState(outState)
    }

    private fun initFullScreenDialog(){
        mFullScreenDialog = object:Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen){
            override fun onBackPressed() {
                if(mExoPlayerFullscreen) closeFullscreenDialog()
                super.onBackPressed()
            }
        }
    }

    private fun openFullscreenDialog(){
        (mExoPlayerView!!.parent as ViewGroup).removeView(mExoPlayerView)
        mFullScreenDialog!!.addContentView(mExoPlayerView as View,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        mFullScreenIcon!!.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_fullscreen_skrink))
        mExoPlayerFullscreen = true
        mFullScreenDialog!!.show()
    }

    private fun closeFullscreenDialog(){
        (mExoPlayerView!!.parent as ViewGroup).removeView(mExoPlayerView)
        findViewById<FrameLayout>(R.id.main_media_frame)!!.addView(mExoPlayerView)
        mFullScreenIcon!!.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_fullscreen_expand))
        mExoPlayerFullscreen = false
        mFullScreenDialog!!.dismiss()
    }


    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            0
        )
    }

    private fun prepareExoPlayerFromFileUri(videoUrl: String): MediaSource {
        val uri = Uri.parse(videoUrl);
        return buildMediaSource(uri)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return ExtractorMediaSource.Factory(
            DefaultDataSourceFactory(this, applicationContext.applicationInfo.packageName)
        ).createMediaSource(uri)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("pudroid", "Service Disconnected!!")
            service = null
        }

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i("pudroid", "Service Connected!!")
            service = (iBinder as HttpService.LocalBinder).getService()
        }
    }

    override fun onResume() {
        super.onResume()
        if(service == null) {
            var serviceIntent = Intent(this, HttpService::class.java)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

            serviceIntent = Intent(this, VideoService::class.java)
            bindService(serviceIntent, ServiceConnection, Context.BIND_AUTO_CREATE)
        }else{
            if(mExoPlayerFullscreen){
                openFullscreenDialog()
            }
        }
    }

    override fun onPause() {
        if(videoService != null)
            videoService!!.pause()
        if(mFullScreenDialog != null)
            mFullScreenDialog!!.dismiss()
        super.onPause()
    }

    override fun onDestroy() {
        if(service != null)
        {
            var serviceIntent = Intent(this, HttpService::class.java)
            stopService(serviceIntent)

            serviceIntent = Intent(this, VideoService::class.java)
            stopService(serviceIntent)
        }

        super.onDestroy()
    }
}