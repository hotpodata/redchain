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
import com.hotpodata.redchain.R
import com.hotpodata.redchain.adapter.viewholder.ChainLinkVh
import com.hotpodata.redchain.adapter.viewholder.ChainTodayVh
import com.hotpodata.redchain.adapter.viewholder.RowFirstDayMessageVh
import com.hotpodata.redchain.adapter.viewholder.VertLineVh
import com.hotpodata.redchain.data.Chain
import com.hotpodata.redchain.interfaces.ChainUpdateListener
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

/**
 * Created by jdrotos on 9/17/15.
 */
public class ChainAdapter(context: Context, argChain: Chain) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ctx = context
    val CHAIN_LINK = 0;
    val VERTICAL_LINE = 1;
    val CHAIN_TODAY = 2;
    val CHAIN_FIRST_DAY_MESSAGE = 3;

    val rowHeadingTypeface: Typeface
    val rowSubHeadTypeface: Typeface

    var chain: Chain
    var rows = ArrayList<Row>()

    val dtformat1 = DateTimeFormat.forPattern("EEEE")
    val dtformat2 = DateTimeFormat.shortDate()
    val dtformat3 = DateTimeFormat.shortTime()

    val interpo: Interpolator;

    val rowMaxTitleSize: Int
    val rowMinTitleSize: Int
    val rowMaxLineSize: Int
    val rowMinLineSize: Int

    var chainUpdateListener: ChainUpdateListener? = null

    init {
        chain = argChain
        rowHeadingTypeface = Typeface.createFromAsset(context.assets, "fonts/Roboto-Thin.ttf");
        rowSubHeadTypeface = Typeface.createFromAsset(context.assets, "fonts/Roboto-Medium.ttf");

        rowMaxTitleSize = context.resources.getDimensionPixelSize(R.dimen.row_title_max)
        rowMinTitleSize = context.resources.getDimensionPixelSize(R.dimen.row_title_min)
        rowMaxLineSize = context.resources.getDimensionPixelSize(R.dimen.row_vert_line_max_height)
        rowMinLineSize = context.resources.getDimensionPixelSize(R.dimen.row_vert_line_min_height)
        interpo = DecelerateInterpolator(10f);
        buildRows()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is ChainTodayVh) {
            var headingTypeface = rowHeadingTypeface
            if (holder.tv1.typeface != headingTypeface) {
                holder.tv1.typeface = headingTypeface
            }
            if (holder.tv2.typeface != headingTypeface) {
                holder.tv2.typeface = headingTypeface
            }
            if (holder.tv3.typeface != headingTypeface) {
                holder.tv3.typeface = headingTypeface
            }
            if (holder.tv4.typeface != headingTypeface) {
                holder.tv4.typeface = headingTypeface
            }
            if (chain.chainContainsToday()) {
                holder.xview.setOnClickListener(null)
                holder.xview.isClickable = false
                holder.xview.boxToXPercentage = 1f
                holder.xview.setColors(chain.color, ctx.resources.getColor(R.color.material_grey))
                holder.tv3.text = ctx.resources.getString(R.string.day_num, chain.chainLength)
                holder.tv4.text = chain.chainLink(0)?.toString(dtformat3)
                holder.tv2.visibility = View.GONE
                holder.afterChecked.visibility = View.VISIBLE
            } else {
                holder.tv2.visibility = View.VISIBLE
                holder.afterChecked.visibility = View.GONE
                holder.xview.isClickable = true
                holder.xview.setColors(chain.color, ctx.resources.getColor(R.color.material_grey))
                holder.xview.boxToXPercentage = 0f
                holder.xview.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {

                        //update data
                        chain.addNowToChain();
                        chainUpdateListener?.onChainUpdated(chain)

                        //start anim
                        var start = if (holder.xview.boxToXPercentage == 1f) 1f else 0f;
                        var end = if (holder.xview.boxToXPercentage == 1f) 0f else 1f;

                        holder.xview.boxToXPercentage = start;
                        var animator = ValueAnimator.ofFloat(start, end)
                        animator.addUpdateListener { anim ->
                            holder.xview.boxToXPercentage = anim.animatedFraction
                        }
                        animator.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                buildRows()
                                //                                if(rows.size == 1){
                                //                                    //If this is the first day, we add the blurb, so rebuild the rows
                                //                                    buildRows()
                                //                                }else{
                                //                                    //Otherwise we just update our today row
                                //                                    notifyItemChanged(0)
                                //                                }
                            }

                            override fun onAnimationCancel(animation: Animator?) {

                            }

                            override fun onAnimationStart(animation: Animator?) {

                            }
                        })
                        animator.interpolator = AccelerateDecelerateInterpolator()
                        animator.setDuration(700)
                        animator.start()
                    }
                })
            }
        }

        if (holder is ChainLinkVh) {
            var titleHeight = Math.max(rowMinTitleSize, rowMaxTitleSize - position).toFloat()//rowMinTitleSize + (floatDepth * (rowMaxTitleSize - rowMinTitleSize))

            var data = rows.get(position) as RowChainLink
            var headingTypeface = rowHeadingTypeface

            holder.itemView.setOnClickListener(null)
            holder.xview.setBox(false)
            holder.xview.setColors(chain.color, ctx.resources.getColor(R.color.material_grey))

            if (holder.tv1.typeface != headingTypeface) {
                holder.tv1.typeface = headingTypeface
            }
            if (holder.tv1.textSize != titleHeight) {
                holder.tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleHeight)
            }
            if (holder.tv2.typeface != rowSubHeadTypeface) {
                holder.tv2.typeface = rowSubHeadTypeface
            }

            var dateStr = ctx.getText(R.string.today)
            if (data.dateTime.toLocalDate().plusDays(1).isEqual(LocalDate.now())) {
                dateStr = ctx.getText(R.string.yesterday)
            } else if (Days.daysBetween(data.dateTime.toLocalDate(), LocalDate.now()).days < 7) {
                dateStr = data.dateTime.toString(dtformat1)
            } else {
                dateStr = data.dateTime.toString(dtformat2)
            }

            var timeStr = data.dateTime.toString(dtformat3)

            var depth = chain.chainLength - chain.chainDepth(data.dateTime)
            holder.tv1.text = ctx.resources.getString(R.string.day_num, depth)
            holder.tv2.text = "$dateStr\n$timeStr";
        }
        if (holder is VertLineVh) {
            var lineHeight = Math.max(rowMinLineSize, rowMaxLineSize - position).toFloat() //rowMinLineSize + (floatDepth * (rowMaxLineSize - rowMinLineSize))
            var data = rows.get(position) as RowChainLine
            if (holder.vertLine.layoutParams != null && holder.vertLine.layoutParams.height != lineHeight.toInt()) {
                holder.vertLine.layoutParams.height = lineHeight.toInt()
                holder.vertLine.layoutParams = holder.vertLine.layoutParams
            }
            holder.vertLine.setBackgroundColor(chain.color)
            holder.vertLine.visibility = if (data.invisible) View.INVISIBLE else View.VISIBLE;
        }
        if (holder is RowFirstDayMessageVh) {
            //nothing to do
        }
    }

    override fun getItemCount(): Int {
        return rows.size;
    }

    override fun getItemViewType(position: Int): Int {
        if (rows.get(position) is RowChainLine) {
            return VERTICAL_LINE;
        } else if (rows.get(position) is RowChainLink) {
            return CHAIN_LINK;
        } else if (rows.get(position) is RowChainToday) {
            return CHAIN_TODAY;
        } else if (rows.get(position) is RowFirstDayMessage) {
            return CHAIN_FIRST_DAY_MESSAGE
        }
        return -1;
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        var inflater = LayoutInflater.from(parent?.getContext());

        if (viewType == CHAIN_LINK) {
            var chainView: View? = inflater.inflate(R.layout.card_chain_link_center, parent, false);
            var vh = ChainLinkVh(chainView);
            return vh;
        }
        if (viewType == VERTICAL_LINE) {
            var lineView = inflater.inflate(R.layout.row_vertical_line_center, parent, false);
            return VertLineVh(lineView);
        }
        if (viewType == CHAIN_TODAY) {
            var chainView = inflater.inflate(R.layout.row_chain_today, parent, false);
            var vh = ChainTodayVh(chainView);
            return vh;
        }
        if (viewType == CHAIN_FIRST_DAY_MESSAGE) {
            var messageView = inflater.inflate(R.layout.row_first_day_message, parent, false)
            var vh = RowFirstDayMessageVh(messageView)
            return vh
        }

        return null;
    }

    public fun updateChain(chn: Chain) {
        chain = chn
        buildRows()

        //NOTE: buildRows() makes some assumptions, when we set a new chain, we should update all rows
        notifyDataSetChanged()
    }

    public fun buildRows() {
        var freshRows = ArrayList<Row>()
        if (chain.chainLength > 0) {
            var dateTimes = ArrayList<LocalDateTime>(chain.dateTimes);
            for (dt in dateTimes) {

                //TODAY has it's own special thing going on
                if (dt.toLocalDate().isBefore(LocalDate.now())) {
                    if (freshRows.size > 0) {
                        freshRows.add(RowChainLine(false))
                    }
                    freshRows.add(RowChainLink(dt))
                }

            }
            if (freshRows.size > 0) {
                freshRows.add(RowChainLine(true))
            }
        }

        if (freshRows.size > 0) {
            freshRows.add(0, RowChainLine(false))
        }
        freshRows.add(0, RowChainToday());
        if (freshRows.size == 1 && chain.chainContainsToday()) {
            freshRows.add(RowFirstDayMessage())
        }

        var oldRows = rows
        rows = freshRows

        if (oldRows.size == 1 && rows.size == 2) {
            notifyItemChanged(0)
            notifyItemInserted(1)
        } else if (oldRows.size > 1 && rows.size == oldRows.size) {
            notifyItemChanged(0)
        } else {
            notifyDataSetChanged()
        }
    }

    open class Row() {

    }

    class RowChainLink(dt: LocalDateTime) : Row() {
        val dateTime = dt
    }

    class RowChainLine(invis: Boolean) : Row() {
        val invisible = invis
    }

    class RowChainToday() : Row() {

    }

    class RowFirstDayMessage() : Row() {

    }
}