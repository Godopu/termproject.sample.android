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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import godopu.lab.termproject.R
import godopu.lab.termproject.databinding.ActivityDoorBinding
import godopu.lab.termproject.databinding.ActivityServicelistBinding
import godopu.lab.termproject.model.Device
import godopu.lab.termproject.model.HttpService
import godopu.lab.termproject.model.ObserverWithHttp
import org.json.JSONObject

class ServiceListActivity : AppCompatActivity() {

    private var service: HttpService? = null
    private var binding: ActivityServicelistBinding? = null
    private lateinit var recyclerView: RecyclerView
    private var adapter: ServiceListAdapter = ServiceListAdapter(this)
    private var layoutManager = LinearLayoutManager(this)
    private lateinit var dialog: SweetAlertDialog
    private lateinit var serviceIntent: Intent

    private fun selectItemEventListener(item: Device) {
        when (item.Name) {
            "Car-seat" -> {
                dialog = SweetAlertDialog(this@ServiceListActivity, SweetAlertDialog.NORMAL_TYPE)
                dialog.titleText = "Car-seat Occupation Request"
                dialog.contentText = "Please wait for occupation of car-seat"
                dialog.show()
                dialog!!.dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_servicelist)


        this.recyclerView = findViewById(R.id.recycler_view)
        this.recyclerView.setHasFixedSize(true)

        this.recyclerView.adapter = this.adapter
        this.recyclerView.layoutManager = this.layoutManager

        this.adapter.addItem(Device("User", "User"))
        this.adapter.addItem(Device("Display", "Display"))
        this.adapter.addItem(Device("Car-seat", "Car-seat"))
        this.adapter.addItem(Device("Sensor", "Sensor"))
        this.adapter.addItem(Device("Heated-Seat", "Heated-Seat"))

        this.adapter.notifyDataSetChanged()
    }


    override fun onPause() {
        if (service != null) {
            this.service!!.deregisterObserver()
            val serviceIntent = Intent(this, HttpService::class.java)
            stopService(serviceIntent)

        }

        super.onPause()
    }

    private class ServiceListAdapter : RecyclerView.Adapter<ServiceListAdapter.ViewHolder> {
        private var context: ServiceListActivity
        private var mItems: ArrayList<Device>
        private var lastPosition: Int = -1

        constructor(context: ServiceListActivity, mItems: ArrayList<Device>) {
            this.context = context
            this.mItems = mItems
        }

        constructor(context: ServiceListActivity) {
            this.context = context
            this.mItems = ArrayList()
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.imageView.setImageResource(mItems[position].Image)
            holder.textView.text = mItems[position].Category
            holder.constraint.setOnClickListener {
                this.context.selectItemEventListener(mItems[position])
            }
            setAnimation(holder.cardView, position)
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

        fun addItem(category: Device) {
            mItems.add(category)
        }

        fun addItem(category: Device, idx : Int) {
            mItems.add(idx, category)
        }

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var imageView: ImageView = view.findViewById(R.id.cate_image)
            var textView: TextView = view.findViewById(R.id.cate_name)
            var constraint: ConstraintLayout = view.findViewById(R.id.constraint)
            var cardView: CardView = view.findViewById(R.id.card_view)
        }
    }
}

