package godopu.lab.termproject.model

import android.content.Context
import android.os.Handler
import godopu.lab.termproject.R
import godopu.lab.termproject.controller.components.HttpResponseEventRouter
import org.json.JSONObject
import java.util.*

class ObserverWithHttp(_context: Context, _service: HttpService, _mHandler: Handler) {
    companion object{
        const val INIT = 0
        const val CHANGED = 1
    }

    private val context = _context
    private val service = _service
    private val mHandler = _mHandler
    private var latest = ""
    private val router = object : HttpResponseEventRouter {
        override fun route(context: Context, code: Int, arg: String) {
            when (code) {
                200 -> {
                    val obj = JSONObject(arg)

                    if(obj["state"] != latest)
                    {
                        if(latest == "") mHandler.sendMessage(mHandler.obtainMessage(INIT, obj["state"]))
                        else mHandler.sendMessage(mHandler.obtainMessage(CHANGED, obj["state"]))
                        latest = obj["state"] as String
                    }
                }
            }
        }
    }
    private val task = object : TimerTask(){
        override fun run() {
            service.httpRequestWithHandler(context, "GET", "http://${context.getString(R.string.ip_adr)}:${context.getString(R.string.port)}/door-latest", router)
        }
    }

    init{
        Timer().scheduleAtFixedRate(task, 1000, 1000)
    }

    fun destroy(){
        task.cancel()
    }

}