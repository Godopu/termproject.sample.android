package godopu.lab.termproject.controller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import godopu.lab.termproject.R
import godopu.lab.termproject.databinding.ActivityTestBinding
import godopu.lab.termproject.model.HttpService
import godopu.lab.termproject.model.ObserverWithHttp

class TestActivity : AppCompatActivity() {

    private var service: HttpService? = null
    private var binding : ActivityTestBinding? = null
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("pudroid", "Service Disconnected!!")
            service = null
        }

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i("pudroid", "Service Connected!!")
            service = (iBinder as HttpService.LocalBinder).getService()
            service!!.registerObserver(this@TestActivity, this@TestActivity.mHandler)

        }
    }

    class PuObserverHandler(_activity : TestActivity) : Handler(){
        private val activity : TestActivity = _activity
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val value = msg.obj as String
            when(msg.what){
                ObserverWithHttp.INIT->{
                    Log.i("pudroid", "INIT $value")
                    if(activity.binding!!.status != value)
                        activity.binding!!.status = value
                }
                ObserverWithHttp.CHANGED->{
                    Log.i("pudroid", "CHANGED $value")
                    if(activity.binding!!.status != value)
                        activity.binding!!.status = value
                }
            }
        }
    }

    private val mHandler = PuObserverHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test)
        binding!!.status = "loading..."
        if (service == null) {
            val serviceIntent = Intent(this, HttpService::class.java)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        if (service != null) {
            this.service!!.deregisterObserver()
            val serviceIntent = Intent(this, HttpService::class.java)
            stopService(serviceIntent)

        }

        super.onDestroy()
    }
}
