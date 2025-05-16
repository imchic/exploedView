package com.example.exploedview.map

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.exploedview.NaverMapActivity
import com.example.exploedview.R
import com.example.exploedview.db.BuildingInfo
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BuildingInfoAdapter(
    private val items: List<BuildingInfo>,
    private val viewModel: NaverMapViewModel, // ViewModel 주입
    private val onClick: (BuildingInfo, Int) -> Unit,
) : RecyclerView.Adapter<BuildingInfoAdapter.BuildingInfoItemViewHolder>() {

    inner class BuildingInfoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textView: TextView = itemView.findViewById(R.id.itemText)
        private val textView2: TextView = itemView.findViewById(R.id.itemSubText)
        private val textView3: TextView = itemView.findViewById(R.id.itemType)
        private val textView4: TextView = itemView.findViewById(R.id.itemDongCnt)
        private val textView5: TextView = itemView.findViewById(R.id.itemUnitCnt)

        private val delButton: TextView = itemView.findViewById(R.id.deleteButton)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(items[position], position)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: BuildingInfo) {
            textView.text = item.complexNm1
            textView2.text = item.address
            textView3.text = "타입: ${item.complexGbCd}"
            textView4.text = "동 수: ${item.dongCnt}"
            textView5.text = "세대수: ${viewModel.addCommaToNumber(item.unitCnt)}"

            delButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {

                    val activity = itemView.context as? NaverMapActivity
                    // AlertDialog를 사용하여 삭제 확인
                    val layoutInflater = LayoutInflater.from(itemView.context)
                    val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
                    val alertDialog = AlertDialog.Builder(itemView.context)
                        .setView(dialogView)
                        .setCancelable(true)
                        .show()

                    val tvTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
                    val tvMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
                    val btnCancel = dialogView.findViewById<Button>(R.id.dialog_cancel_button)
                    val btnConfirm = dialogView.findViewById<Button>(R.id.dialog_confirm_button)

                    tvTitle.text = "삭제"
                    tvMessage.text = "아이템과 마커를 삭제하시겠습니까?"

                    btnCancel.setOnClickListener {
                        alertDialog.dismiss()
                    }

                    btnConfirm.setOnClickListener {
                        // 삭제 처리
                        CoroutineScope(Dispatchers.Main).launch {
                            // ViewModel을 통해 데이터 삭제
                            viewModel.removeMenuItemAndMarker(position)

                            activity?.buildingInfoDao?.deleteBySeq(
                                items[position].seq ?: 0
                            )

                            // Toasty 메시지
                            Toasty.success(
                                itemView.context,
                                "아이템과 마커가 삭제되었습니다.",
                                Toasty.LENGTH_SHORT
                            ).show()
                            activity?.drawerLayout?.closeDrawers() // 드로어 닫기
                        }
                        alertDialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuildingInfoItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.drawer_list_item, parent, false)

        // 전체 삭제 버튼
        val btnDeleteAll: ImageView = parent.rootView.findViewById(R.id.btn_delete_all)

        btnDeleteAll.setOnClickListener {

            // AlertDialog를 사용하여 전체 삭제 확인
            val layoutInflater = LayoutInflater.from(parent.context)

            val dialogView = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
            val alertDialog = AlertDialog.Builder(parent.context)
                .setView(dialogView)
                .setCancelable(true)
                .show()

            val tvTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
            val tvMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
            val btnCancel = dialogView.findViewById<Button>(R.id.dialog_cancel_button)
            val btnConfirm = dialogView.findViewById<Button>(R.id.dialog_confirm_button)

            tvTitle.text = "전체 삭제"
            tvMessage.text = "모든 아이템과 마커를 삭제하시겠습니까?"

            btnCancel.setOnClickListener {
                alertDialog.dismiss()
            }

            btnConfirm.setOnClickListener {
                // 전체 삭제 처리
                val activity = parent.context as? NaverMapActivity
                CoroutineScope(Dispatchers.Main).launch {
                    // 전체 삭제 처리
                    viewModel.clearMenuItems()
                    viewModel.clearMarkers()
                    viewModel.clearDatabase(activity!!) // activity 전달
                    Toasty.success(
                        parent.context,
                        "모든 아이템과 마커가 삭제되었습니다.",
                        Toasty.LENGTH_SHORT
                    ).show()
                    activity.drawerLayout?.closeDrawers() // 드로어 닫기
                }
                alertDialog.dismiss()
            }
        }

        return BuildingInfoItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuildingInfoItemViewHolder, position: Int) {
        //holder.bind(items[position])
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size
}