package com.hotpodata.redchain.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hotpodata.redchain.R
import com.hotpodata.redchain.adapter.viewholder.RowColorViewHolder
import com.hotpodata.redchain.interfaces.ColorSelectedListener
import java.util.*

/**
 * Created by jdrotos on 9/17/15.
 */
class ColorCircleAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ROW_COLOR = 0;
    val ctx = context

    var rows: List<RowColor>;
    var selectedColor: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var colorSelectionLisetner: ColorSelectedListener? = null
        set


    init {
        var tmpRows = ArrayList<RowColor>()
        selectedColor = ctx.resources.getColor(R.color.primary)
        var colors = ctx.resources.getIntArray(R.array.material_colors)
        for (color in colors) {
            tmpRows.add(RowColor(color))
        }
        rows = tmpRows;
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        var data = rows.get(position)
        if (holder is RowColorViewHolder) {
            holder.mIcon.setCircleBgColor(data.color)
            if (data.color == selectedColor) {
                holder.mIcon.setImageResource(R.drawable.ic_action_checkmark)
            } else {
                holder.mIcon.setImageDrawable(null)
            }
            holder.itemView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    selectedColor = data.color
                    colorSelectionLisetner?.onColorSelected(selectedColor)
                }
            })
        }
    }


    override fun getItemCount(): Int {
        return rows.size;
    }

    override fun getItemViewType(position: Int): Int {
        if (rows.get(position) is RowColor) {
            return ROW_COLOR;
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var inflater = LayoutInflater.from(parent?.context);

        if (viewType == ROW_COLOR) {
            return RowColorViewHolder(inflater.inflate(R.layout.row_color, parent, false));
        }

        return null;
    }


    class RowColor(var color: Int)

}