package com.hotpodata.redchain.adapter

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.AdapterView
import com.hotpodata.redchain.R
import com.hotpodata.redchain.adapter.viewholder.ChainLinkVh
import com.hotpodata.redchain.adapter.viewholder.ChainTodayVh
import com.hotpodata.redchain.adapter.viewholder.RowColorViewHolder
import com.hotpodata.redchain.adapter.viewholder.VertLineVh
import com.hotpodata.redchain.data.Chain
import com.hotpodata.redchain.interfaces.ChainUpdateListener
import com.hotpodata.redchain.interfaces.ColorSelectedListener
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
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
        var inflater = LayoutInflater.from(parent?.getContext());

        if (viewType == ROW_COLOR) {
            var chainView: View? = inflater.inflate(R.layout.row_color, parent, false);
            var vh = RowColorViewHolder(chainView);
            return vh;
        }

        return null;
    }


    class RowColor(var color: Int)

}