package godopu.lab.termproject.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.sweetalert.SweetAlertDialog
import godopu.lab.termproject.R
import godopu.lab.termproject.databinding.ActivityServicelistBinding
import godopu.lab.termproject.model.Device
import java.util.*
import kotlin.collections.ArrayList

class ServiceListActivity : AppCompatActivity() {

    private var binding: ActivityServicelistBinding? = null
    private lateinit var recyclerView: RecyclerView
    private var adapter: ServiceListAdapter = ServiceListAdapter(this)
    private var layoutManager = LinearLayoutManager(this)
    private lateinit var dialog: SweetAlertDialog

    private fun selectItemEventListener(item: Device) {
        when (item.Name) {
            "LED" -> {
                dialog = SweetAlertDialog(this@ServiceListActivity, SweetAlertDialog.FINGER_TYPE)
                dialog.titleText = "Car-seat Occupation Request"
                dialog.contentText = "Please wait for occupation of car-seat"
                dialog.show()
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                        }
                    }
                }, 1000)
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            dialog.dismiss()
                        }
                    }
                }, 1700)
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            val intent = Intent(this@ServiceListActivity, LedActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                }, 2100)
            }
            "Display" -> {
                dialog = SweetAlertDialog(this@ServiceListActivity, SweetAlertDialog.FINGER_TYPE)
                dialog.titleText = "Car-seat Occupation Request"
                dialog.contentText = "Please wait for occupation of car-seat"
                dialog.show()
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                        }
                    }
                }, 1000)
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            dialog.dismiss()
                        }
                    }
                }, 1700)
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            val intent = Intent(this@ServiceListActivity, DisplayActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                }, 2100)
            }
            "Door" -> {
                dialog = SweetAlertDialog(this@ServiceListActivity, SweetAlertDialog.FINGER_TYPE)
                dialog.titleText = "Car-seat Occupation Request"
                dialog.contentText = "Please wait for occupation of car-seat"
                dialog.show()
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                        }
                    }
                }, 1000)
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            dialog.dismiss()
                        }
                    }
                }, 1700)
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            val intent = Intent(this@ServiceListActivity, DoorActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                }, 2100)
            }
            "Temp" -> {
                dialog = SweetAlertDialog(this@ServiceListActivity, SweetAlertDialog.FINGER_TYPE)
                dialog.titleText = "Car-seat Occupation Request"
                dialog.contentText = "Please wait for occupation of car-seat"
                dialog.show()
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                        }
                    }
                }, 1000)
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            dialog.dismiss()
                        }
                    }
                }, 1700)
                Timer().schedule(object : TimerTask(){
                    override fun run() {
                        this@ServiceListActivity.runOnUiThread{
                            val intent = Intent(this@ServiceListActivity, TempActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                }, 2100)
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

        this.adapter.addItem(Device("LED", "LED"))
        this.adapter.addItem(Device("Display", "Display"))
        this.adapter.addItem(Device("Door", "Display"))
        this.adapter.addItem(Device("Temp", "Display"))

        this.adapter.notifyDataSetChanged()
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
            holder.textView.text = mItems[position].Name
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

        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var imageView: ImageView = view.findViewById(R.id.cate_image)
            var textView: TextView = view.findViewById(R.id.cate_name)
            var constraint: ConstraintLayout = view.findViewById(R.id.constraint)
            var cardView: CardView = view.findViewById(R.id.card_view)
        }
    }
}

