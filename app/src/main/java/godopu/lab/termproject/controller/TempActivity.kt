package godopu.lab.termproject.controller

import android.app.ActivityManager
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
import godopu.lab.termproject.databinding.ActivityTempBinding
import godopu.lab.termproject.model.HttpService
import godopu.lab.termproject.model.ObserverWithHttp
import godopu.lab.termproject.model.TempService
import org.json.JSONObject

class TempActivity : AppCompatActivity() {

    private var observer : ObserverWithHttp?= null
    private var service: HttpService? = null
    private var binding : ActivityTempBinding? = null
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("pudroid", "Service Disconnected!!")
            service = null
        }

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i("pudroid", "Service Connected!!")
            service = (iBinder as HttpService.LocalBinder).getService()
            if(service != null)
                registerObserver(this@TempActivity, service as HttpService, this@TempActivity.mHandler, "temp-latest")
        }
    }

    fun registerObserver(_context : Context, _service : HttpService, _mHandler : Handler, _resName : String){
        if(observer != null) observer!!.destroy()
        observer = ObserverWithHttp(_context, _service, _mHandler, _resName)
    }

    fun deregisterObserver(){
        if(observer != null) observer!!.destroy()
    }

    class PuObserverHandler(_activity : TempActivity) : Handler(){
        private val activity : TempActivity = _activity
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val obj = msg.obj as JSONObject
            val temp = obj["temp"]
            val humi = obj["humi"]

            when(msg.what){
                ObserverWithHttp.INIT->{
                    activity.binding!!.temp = "Temp - $temp"
                    activity.binding!!.humi = "Humi - $humi"
                }
                ObserverWithHttp.CHANGED->{
                    activity.binding!!.temp = "Temp - $temp"
                    activity.binding!!.humi = "Humi - $humi"
                }
            }
        }
    }

    private val mHandler = PuObserverHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_temp)
        binding!!.temp = "loading..."
        binding!!.humi = "loading..."

        if(intent.getBooleanExtra("isNoti", false)){
            val serviceIntent = Intent(this, TempService::class.java)
            stopService(serviceIntent)
            binding!!.serviceBtn.text = "Service Not Running"
        }
        else{
            if(TempService.running){
                binding!!.serviceBtn.text = "Service Running"
            }else{
                binding!!.serviceBtn.text = "Service Not Running"
            }
        }

        if (service == null) {
            val serviceIntent = Intent(this, HttpService::class.java)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        }

        binding!!.serviceBtn.setOnClickListener {
            if(!TempService.running)
                this@TempActivity.startCheck()
            else
                this@TempActivity.stopCheck()
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

    private fun startCheck(){
        val serviceIntent = Intent(this, TempService::class.java)
        startService(serviceIntent)
        binding!!.serviceBtn.text = "Service Running"
    }

    fun stopCheck(){
        val serviceIntent = Intent(this, TempService::class.java)
        stopService(serviceIntent)
        binding!!.serviceBtn.text = "Service Not Running"
    }
}
