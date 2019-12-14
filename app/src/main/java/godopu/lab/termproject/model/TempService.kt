package godopu.lab.termproject.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import godopu.lab.termproject.R
import godopu.lab.termproject.controller.TempActivity
import org.json.JSONObject


class TempService : Service() {

    companion object {
        var running: Boolean = false
    }

    private var service: HttpService? = null
    private var observer: ObserverWithHttp? = null
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("pudroid", "Service Disconnected!!")
            service = null
        }

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i("pudroid", "Service Connected!!")
            service = (iBinder as HttpService.LocalBinder).getService()
            if (service != null)
                registerObserver(
                    this@TempService,
                    service as HttpService,
                    this@TempService.mHandler,
                    "temp-latest"
                )
        }
    }
    val NOTIFICATION_CHANNEL_ID = "10001"

    fun sendNoti(){
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent =
            Intent(this, TempActivity::class.java)
        notificationIntent.putExtra("isNoti", true)

        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            FLAG_UPDATE_CURRENT
        )

        val builder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.mipmap.ic_launcher_round
                    )
                ) //BitMap 이미지 요구
                .setContentTitle("상태바 드래그시 보이는 타이틀")
                .setContentText("상태바 드래그시 보이는 서브타이틀")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setSmallIcon(R.drawable.ic_launcher_foreground)

            val channelName: CharSequence = "노티페케이션 채널"
            val description = "오레오 이상을 위한 것임"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)

            channel.description = description
            notificationManager.createNotificationChannel(channel)
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher)
        }

        notificationManager.notify(1234, builder.build())
    }

    private var notied = false

    class PuObserverHandler(_service: TempService) : Handler() {
        private val service: TempService = _service
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val value = (msg.obj as JSONObject)["temp"] as Int
            if (value > 20) {
                if(msg.what != ObserverWithHttp.INIT && !service.notied){
                    service.notied = true
                    service.sendNoti()
                }
            }
        }
    }

    val mHandler = PuObserverHandler(this)


    fun registerObserver(
        _context: Context,
        _service: HttpService,
        _mHandler: Handler,
        _resName: String
    ) {
        if (observer != null) observer!!.destroy()
        observer = ObserverWithHttp(_context, _service, _mHandler, _resName)
    }

    fun deregisterObserver() {
        if (observer != null) observer!!.destroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("pudroid", "Temp Service start!!")
        running = true
        if (service == null) {
            val serviceIntent = Intent(this, HttpService::class.java)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        }

        return super.onStartCommand(intent, flags, startId)
    }

    inner class LocalBinder : Binder() {
        fun getService(): TempService {
            return this@TempService
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        Log.i("pudroid", "Bind Service")

        running = true
        notied = false
        if (service == null) {
            val serviceIntent = Intent(this, HttpService::class.java)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        }
        return binder
    }

    override fun onDestroy() {
        Log.i("pudroid", "Service destroy")
        running = false
        deregisterObserver()
        super.onDestroy()
    }
}
