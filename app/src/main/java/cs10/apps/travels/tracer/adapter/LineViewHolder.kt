package cs10.apps.travels.tracer.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ItemLineBinding
import cs10.apps.travels.tracer.model.joins.RatedBusLine
import cs10.apps.travels.tracer.model.lines.CustomBusLine

class LineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val binding = ItemLineBinding.bind(view)

    fun render(item: RatedBusLine, onLineClickListener: (CustomBusLine) -> Unit){
        item.number?.let { n -> binding.title.text = "Linea $n" }
        item.name?.let { n -> binding.suggestedName.text = "- $n" }

        when(item.color){
            0 -> binding.card.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.bus))
            else -> binding.card.setCardBackgroundColor(item.color)
        }

        binding.rateText.isVisible = (item.avgUserRate > 0)
        binding.rateText.text = "${Utils.rateFormat(item.avgUserRate)} (${item.reviewsCount} reviews)"

        binding.card.setOnClickListener { onLineClickListener(item) }
    }
}