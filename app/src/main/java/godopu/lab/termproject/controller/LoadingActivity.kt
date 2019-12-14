package godopu.lab.termproject.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import godopu.lab.termproject.R

class LoadingActivity : AppCompatActivity() {

    private lateinit var bAnim: AlphaAnimation
    private lateinit var anim: Animation

    val mHandler = Handler()

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }else{
            val intent = Intent(this@LoadingActivity, ServiceListActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun makeRequest() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.INTERNET),
            0
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        startMyAnimation(R.id.illust_phone)
        mHandler.postDelayed(Runnable {
            startMyAnimation(R.id.illust_light)
            startMyAnimation(R.id.illust_char)
        }, 500)

        mHandler.postDelayed(Runnable {
            this@LoadingActivity.runOnUiThread {
                setupPermissions()
            }
        }, 1000)
    }

    private fun startMyAnimation(id: Int) {
        when (id) {
            R.id.illust_phone -> {
                anim =
                    AnimationUtils.loadAnimation(applicationContext, android.R.anim.slide_in_left)
                findViewById<View>(id).startAnimation(anim)
            }
            R.id.illust_light -> {
                bAnim = AlphaAnimation(0.0f, 1.0f)
                bAnim.duration = 400
                bAnim.fillAfter = true
                bAnim.repeatMode = AlphaAnimation.REVERSE
                bAnim.repeatCount = 2
                findViewById<View>(id).startAnimation(bAnim)
                findViewById<View>(id).visibility = View.VISIBLE
            }
            R.id.illust_char -> {
                bAnim = AlphaAnimation(0.0f, 1.0f)
                bAnim.duration = 300
                bAnim.fillAfter = true
                bAnim.repeatMode = AlphaAnimation.REVERSE

                findViewById<View>(id).startAnimation(bAnim)
                findViewById<View>(id).visibility = View.VISIBLE
            }
        }
    }

}
