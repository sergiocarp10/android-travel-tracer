package cs10.apps.travels.tracer.pages.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.model.roca.HorarioTren

class TrainScheduleAdapter(
    var list: List<HorarioTren>,
    var current: Int?,
    private val onClickListener: (HorarioTren) -> Unit
) : RecyclerView.Adapter<TrainScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainScheduleViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TrainScheduleViewHolder(layoutInflater.inflate(R.layout.item_service, parent, false))
    }

    override fun onBindViewHolder(holder: TrainScheduleViewHolder, position: Int) {
        val item = list[position]

        holder.render(
            item,
            position == 0 || position == itemCount - 1,
            position == current,
            onClickListener
        )
    }

    override fun getItemCount() = list.size

    fun updateCurrent(value: Int){
        val prev = current
        current = value

        if (prev != null) notifyItemChanged(prev)
        notifyItemChanged(value)
    }
}