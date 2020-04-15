package godopu.lab.termproject.model

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import godopu.lab.termproject.controller.components.HttpResponseEventRouter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.HttpURLConnection
import java.net.URL

class HttpService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("pudroid", "Service start!!")
        return super.onStartCommand(intent, flags, startId)
    }

    inner class LocalBinder : Binder(){
        fun getService(): HttpService {
            return this@HttpService
        }
    }
    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        Log.i("pudroid", "Service destroy")
        super.onDestroy()
    }
    
    fun httpRequestWithHandler(
        context: Context,
        method: String,
        urlString: String,
        router: HttpResponseEventRouter?
    ) {
        object : Thread() {
            override fun run() {
                val url = URL(urlString)
                val urlConn = url.openConnection() as HttpURLConnection
                urlConn.requestMethod = method

                val reader = BufferedReader(InputStreamReader(urlConn.inputStream))

                val line = StringBuilder()
                while (true) {
                    val l = reader.readLine() ?: break
                    Log.i("pudroid", l)
                    line.append(l)
                }

                Log.i("pudroid", line.toString())
                router?.route(context, urlConn.responseCode, line.toString())
            }
        }.start()
    }

    fun httpRequestWithHandler(
        context: Context,
        method: String,
        urlString: String,
        payload: String,
        router: HttpResponseEventRouter?
    ) {
        object : Thread() {
            override fun run() {
                val url = URL(urlString)
                val urlConn = url.openConnection() as HttpURLConnection
                urlConn.setRequestProperty("content-type", "application/json")
                urlConn.requestMethod = method

                val writer = PrintStream(urlConn.outputStream)
                writer.print(payload)

                val reader = BufferedReader(InputStreamReader(urlConn.inputStream))

                val line = StringBuilder()
                while (true) {
                    val l = reader.readLine() ?: break
                    Log.i("pudroid", l)
                    line.append(l)
                }

                router?.route(context, urlConn.responseCode, line.toString())
            }
        }.start()
    }
}