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
import godopu.lab.termproject.controller.components.HttpResponseEventRouter
import godopu.lab.termproject.databinding.ActivityLedBinding
import godopu.lab.termproject.model.HttpService
import godopu.lab.termproject.model.ObserverWithHttp
import org.json.JSONObject

class LedActivity : AppCompatActivity() {

    private var service: HttpService? = null
    private var binding : ActivityLedBinding? = null
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("pudroid", "Service Disconnected!!")
            service = null
        }

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i("pudroid", "Service Connected!!")
            service = (iBinder as HttpService.LocalBinder).getService()
            service!!.registerObserver(this@LedActivity, this@LedActivity.mHandler, "led-latest")
            initComponent()
        }
    }

    class PuObserverHandler(_activity : LedActivity) : Handler(){
        private val activity : LedActivity = _activity
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_led)
        binding!!.status = "loading..."
        if (service == null) {
            val serviceIntent = Intent(this, HttpService::class.java)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun initComponent(){
        this.binding!!.toggleBtn!!.setOnClickListener {
            if(binding!!.toggleBtn.text == "on"){
                binding!!.toggleBtn.text = "off"
                val obj = JSONObject()
                obj.put("state", "off")
                service!!.httpRequestWithHandler(
                    this,
                    "PUT",
                    "http://${this.getString(R.string.ip_adr)}:${this.getString(R.string.port)}/led",
                    obj.toString(),
                    object : HttpResponseEventRouter{
                        override fun route(context: Context, code: Int, arg: String) {
                            this@LedActivity.runOnUiThread {
                                when(code){
                                    200->{
                                        binding!!.toggleBtn.text = "off"
                                    }
                                }

                            }
                        }
                    }
                )
            }else{
                binding!!.toggleBtn.text = "on"
                val obj = JSONObject()
                obj.put("state", "on")
                service!!.httpRequestWithHandler(
                    this,
                    "PUT",
                    "http://${this.getString(R.string.ip_adr)}:${this.getString(R.string.port)}/led",
                    obj.toString(),
                    object : HttpResponseEventRouter{
                        override fun route(context: Context, code: Int, arg: String) {
                            this@LedActivity.runOnUiThread {
                                when(code){
                                    200->{
                                        binding!!.toggleBtn.text = "on"
                                    }
                                }

                            }
                        }
                    }
                )
            }

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
