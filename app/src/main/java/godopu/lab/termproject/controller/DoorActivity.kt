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
import godopu.lab.termproject.databinding.ActivityDoorBinding
import godopu.lab.termproject.model.HttpService
import godopu.lab.termproject.model.ObserverWithHttp
import org.json.JSONObject

class DoorActivity : AppCompatActivity() {

    private var observer : ObserverWithHttp ?= null
    private var service: HttpService? = null
    private var binding : ActivityDoorBinding? = null
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("pudroid", "Service Disconnected!!")
            service = null
        }

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i("pudroid", "Service Connected!!")
            service = (iBinder as HttpService.LocalBinder).getService()
            if(service != null)
                registerObserver(this@DoorActivity, service as HttpService, this@DoorActivity.mHandler, "door-latest")
        }
    }

    fun registerObserver(_context : Context, _service : HttpService, _mHandler : Handler, _resName : String){
        if(observer != null) observer!!.destroy()
        observer = ObserverWithHttp(_context, _service, _mHandler, _resName)
    }

    fun deregisterObserver(){
        if(observer != null) observer!!.destroy()
    }

    class PuObserverHandler(_activity : DoorActivity) : Handler(){
        private val activity : DoorActivity = _activity
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val value = (msg.obj as JSONObject)["state"] as String

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_door)
        binding!!.status = "loading..."
        if (service == null) {
            val serviceIntent = Intent(this, HttpService::class.java)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        if (service != null) {
            deregisterObserver()
            val serviceIntent = Intent(this, HttpService::class.java)
            stopService(serviceIntent)
        }

        super.onDestroy()
    }
}
