package godopu.lab.termproject.controller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import godopu.lab.termproject.R
import godopu.lab.termproject.controller.components.HttpResponseEventRouter
import godopu.lab.termproject.model.HttpService
import org.json.JSONObject
import java.util.*

class LoadingActivity : AppCompatActivity() {

    private lateinit var bAnim: AlphaAnimation
    private lateinit var anim: Animation
    private var service: HttpService? = null

    private var checker: Timer? = null


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

    val mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        startMyAnimation(R.id.illust_phone)
        mHandler.postDelayed(Runnable {
            startMyAnimation(R.id.illust_light)
            startMyAnimation(R.id.illust_char)
        }, 500)

        if (service == null) {
            val serviceIntent = Intent(this, HttpService::class.java)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        }



    }

    override fun onDestroy() {
        if (service != null) {
            val serviceIntent = Intent(this, HttpService::class.java)
            stopService(serviceIntent)

        }

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        checker = Timer()
        checker!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                service!!.httpRequestWithHandler(this@LoadingActivity,
                    "GET",
                    "http://192.168.12.216:5000/update",
                    object : HttpResponseEventRouter {
                        override fun route(context: Context, code: Int, arg: String) {
                            val obj = JSONObject(arg)
                            if(obj["state"] == "/video")
                            {
                                val intent =
                                    Intent(
                                        this@LoadingActivity,
                                        MainActivity::class.java
                                    )
                                startActivity(intent)
                                overridePendingTransition(
                                    android.R.anim.slide_in_left,
                                    android.R.anim.slide_out_right
                                )
                                checker!!.cancel()
                            }
                        }
                    })
            }
        }, 2000, 2000)
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
