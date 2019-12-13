package godopu.lab.termproject.controller

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import godopu.lab.termproject.R
import godopu.lab.termproject.controller.components.HttpResponseEventRouter
import godopu.lab.termproject.databinding.ActivityMainBinding
import godopu.lab.termproject.model.Content
import godopu.lab.termproject.model.HttpService
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var slidingUpPanel: SlidingUpPanelLayout
    private lateinit var collapsedBody: LinearLayoutCompat
    private lateinit var dragIcon: ImageView
    private var binding: ActivityMainBinding? = null

    private var vWidth = 0
    private var vHeight = 0
    private var pWidth = 0
    private var pHeight = 0

    private val adapter: ContentListAdapter = ContentListAdapter(this)
    private val layoutManager = LinearLayoutManager(this)

    private var seekbar: SeekBar? = null
    private var timer: Timer? = null

    private var service : HttpService? = null
    private var checker: Timer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding!!.title = "Video Content Delivery"
        binding!!.strPlayTitle = "Temp"
        binding!!.mEndTime = "0:00"
        binding!!.mStartTime = "0:00"
        init()
        this.vWidth = dragIcon.layoutParams.width
        this.vHeight = dragIcon.layoutParams.height
        this.pWidth = resources.displayMetrics.widthPixels
        this.pHeight = (pWidth * 3 / 4)
        this.slidingUpPanel.panelHeight = 0
        this.slidingUpPanel.setDragView(null)

        val serviceIntent = Intent(this, HttpService::class.java)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("pudroid", "Service Disconnected!!")
            service = null
        }

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i("pudroid", "Service Connected!!")
            service = (iBinder as HttpService.LocalBinder).getService()
            checker = Timer()
            checker!!.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    service!!.httpRequestWithHandler(this@MainActivity,
                        "GET",
                        "http://192.168.12.216:5000/update",
                        object : HttpResponseEventRouter {
                            override fun route(context: Context, code: Int, arg: String) {
                                val obj = JSONObject(arg)
                                if(obj["state"] == "/loading")
                                {
                                    val intent =
                                        Intent(
                                            this@MainActivity,
                                            LoadingActivity::class.java
                                        )
                                    startActivity(intent)
                                    overridePendingTransition(
                                        android.R.anim.slide_in_left,
                                        android.R.anim.slide_out_right
                                    )
                                    checker!!.cancel()
                                    finish()
                                }
                            }
                        })
                }
            }, 2000, 2000)
        }
    }

    override fun onDestroy() {
        if(timer != null) timer!!.cancel()
        val serviceIntent = Intent(this, HttpService::class.java)
        stopService(serviceIntent)
        super.onDestroy()
    }

    fun cancelClick(v: View) {
        this.stop()
        service!!.httpRequestWithHandler(
            this@MainActivity,
            "PUT",
            "http://192.168.12.216/cancel",
            null
        )

        this.slidingUpPanel.panelHeight = 0
        this.adapter.reset()
    }

    fun playClick(v: View) {
        if (timer == null) {
            play()
        } else {
            stop()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {

        this.seekbar = binding!!.seekbar
        val recyclerView = this.binding!!.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = this.adapter
        recyclerView.layoutManager = this.layoutManager

        this.adapter.addItem(Content("apple carplay", 189))
        this.adapter.addItem(Content("google android auto", 43))
        this.adapter.addItem(Content("volvo_IVI system", 167))
        this.adapter.addItem(Content("samsung_IVI system", 136))

        dragIcon = findViewById(R.id.drag_icon)
        collapsedBody = findViewById(R.id.collapsed_body)
        slidingUpPanel = findViewById(R.id.sliding_layout)
        this.slidingUpPanel.performClick()
        this.slidingUpPanel.setFadeOnClickListener(null)
        slidingUpPanel.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                if (slideOffset < 0.2) {
                    if (dragIcon.layoutParams.width != (vWidth + ((pWidth - vWidth)) * (slideOffset * 5)).toInt() ||
                        dragIcon.layoutParams.height != (vHeight + ((pHeight - vHeight)) * (slideOffset * 5)).toInt()
                    ) {
                        dragIcon.layoutParams.width =
                            (vWidth + ((pWidth - vWidth)) * (slideOffset * 5)).toInt()
                        dragIcon.layoutParams.height =
                            (vHeight + ((pHeight - vHeight)) * (slideOffset * 5)).toInt()
                        dragIcon.requestLayout()
                    }
                } else {
                    if (dragIcon.layoutParams.width < pWidth) {
                        dragIcon.layoutParams.width = pWidth
                        dragIcon.layoutParams.height = pHeight
                        dragIcon.requestLayout()
                    }
                }
            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) {
                when (newState) {
                    SlidingUpPanelLayout.PanelState.COLLAPSED -> {
                        dragIcon.layoutParams.width = vWidth
                        dragIcon.layoutParams.height = vHeight
                        collapsedBody.visibility = View.VISIBLE
                    }
                    SlidingUpPanelLayout.PanelState.EXPANDED -> {
                        dragIcon.layoutParams.width = pWidth
                        dragIcon.layoutParams.height = pHeight
                        collapsedBody.visibility = View.GONE
                        if (this@MainActivity.slidingUpPanel.panelHeight != this@MainActivity.vHeight)
                            this@MainActivity.slidingUpPanel.panelHeight =
                                this@MainActivity.vHeight
                    }
                }
                dragIcon.requestLayout()
            }
        })

        binding!!.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (this@MainActivity.seekbar!!.progress % 60 < 10)
                    binding!!.mStartTime =
                        "${this@MainActivity.seekbar!!.progress / 60}:0${this@MainActivity.seekbar!!.progress % 60}"
                else
                    binding!!.mStartTime =
                        "${this@MainActivity.seekbar!!.progress / 60}:${this@MainActivity.seekbar!!.progress % 60}"
                val obj = JSONObject().apply {
                    put("seek", p0!!.progress)
                }
                service!!.httpRequestWithHandler(
                    this@MainActivity,
                    "PUT",
                    "http://192.168.12.216:5000/seek",
                    obj.toString(),
                    null
                )
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
        })
    }

    private fun stop() {
        Log.e("pudroid", "pause")

        service!!.httpRequestWithHandler(
            this,
            "PUT",
            "http://192.168.12.216:5000/pause",
            object : HttpResponseEventRouter {
                override fun route(context: Context, code: Int, arg: String) {
                    this@MainActivity.binding!!.playBtn1.setImageResource(R.mipmap.button_play)
                    this@MainActivity.binding!!.playBtn2.setImageResource(R.mipmap.button_play)
                    if (this@MainActivity.timer != null) {
                        this@MainActivity.timer!!.cancel()
                        this@MainActivity.timer = null
                    }
                }
            }
        )
    }

    private fun play() {
        service!!.httpRequestWithHandler(
            this,
            "PUT",
            "http://192.168.12.216:5000/play",
            object : HttpResponseEventRouter {
                override fun route(context: Context, code: Int, arg: String) {
                    mHandler.post {
                        this@MainActivity
                            .binding!!.playBtn1.setImageResource(R.mipmap.button_pause)
                        this@MainActivity
                            .binding!!.playBtn2.setImageResource(R.mipmap.button_pause)
                        this@MainActivity.timer = Timer()
                        this@MainActivity.timer!!.scheduleAtFixedRate(object : TimerTask() {
                            override fun run() {
                                this@MainActivity.mHandler.post {
                                    if (this@MainActivity.seekbar!!.progress % 60 < 10)
                                        binding!!.mStartTime =
                                            "${this@MainActivity.seekbar!!.progress / 60}:0${this@MainActivity.seekbar!!.progress % 60}"
                                    else
                                        binding!!.mStartTime =
                                            "${this@MainActivity.seekbar!!.progress / 60}:${this@MainActivity.seekbar!!.progress % 60}"
                                    this@MainActivity.seekbar!!.progress += 1
                                }
                            }
                        }, 0, 1000)
                    }
                }
            }
        )
    }

    private val mHandler = Handler()
    fun playVideo(content: Content) {
        val obj = JSONObject()
        obj.put("title", content.Title)
        service!!.httpRequestWithHandler(
            this,
            "PUT",
            "http://192.168.12.216:5000/video",
            obj.toString(),
            null
        )

        this.stop()
        this.dragIcon.setImageResource(content.Img)
        this.seekbar!!.progress = 0
        binding!!.mStartTime = "0:00"
        binding!!.mEndTime = content.EndTime
        binding!!.strPlayTitle = content.Title
        this.seekbar!!.max = content.PlayTime

        slidingUpPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        this.play()
}

    override fun onResume() {
        super.onResume()
        Log.i("pudroid", "onResume - ${dragIcon.layoutParams.width}")
    }

    override fun onPause() {

        super.onPause()
    }

    override fun onPostResume() {
        super.onPostResume()
        Log.i("pudroid", "onPostResume - ${dragIcon.layoutParams.width}")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.i("pudroid", "onAttachedToWindow - ${dragIcon.layoutParams.width}")
    }

    private class ContentListAdapter : RecyclerView.Adapter<ContentListAdapter.ViewHolder> {
        private var context: MainActivity
        private var mItems: ArrayList<Content>
        private var lastPosition: Int = -1
        private var prePlayingItem: ImageView? = null

        constructor(context: MainActivity, mItems: ArrayList<Content>) {
            this.context = context
            this.mItems = mItems
        }

        constructor(context: MainActivity) {
            this.context = context
            this.mItems = ArrayList()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.item_content, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.titleTextView.text = "${this.mItems[position].Title}.mp4"
            holder.contentImg.setImageResource(this.mItems[position].Img)
            holder.stateImg.setImageResource(R.mipmap.video_logo)
            holder.constraint.setOnClickListener {
                context.runOnUiThread {
                    context.playVideo(mItems[position])
                    if (prePlayingItem != null)
                        prePlayingItem!!.setImageResource(R.mipmap.video_logo)
                    holder.stateImg.setImageResource(R.mipmap.playing)
                    prePlayingItem = holder.stateImg

                }
            }
            setAnimation(holder.cardView, position)
        }

        fun reset() {
            if (prePlayingItem != null) prePlayingItem!!.setImageResource(R.mipmap.video_logo)
        }

        private fun setAnimation(viewToAnimate: View, position: Int) {
            // 새로 보여지는 뷰라면 애니메이션을 해줍니다
            if (position > lastPosition) {
                val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
                viewToAnimate.startAnimation(animation)
                lastPosition = position
            }
        }

        override fun getItemCount(): Int {
            return mItems.size
        }

        fun addItem(content: Content) {
            this.mItems.add(content)
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val cardView: CardView = view.findViewById(R.id.card_view)
            val constraint: ConstraintLayout = view.findViewById(R.id.constraint)
            val titleTextView: TextView = view.findViewById(R.id.contentTitle)
            val contentImg: ImageView = view.findViewById(R.id.content_img)
            val stateImg: ImageView = view.findViewById(R.id.state_img)
        }
    }
}