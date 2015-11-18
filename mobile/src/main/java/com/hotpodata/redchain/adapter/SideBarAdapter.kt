package com.hotpodata.redchain.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hotpodata.redchain.R
import com.hotpodata.redchain.adapter.viewholder.*
import com.hotpodata.redchain.data.Chain

/**
 * Created by jdrotos on 11/7/15.
 */
class SideBarAdapter(ctx: Context, rows: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ROW_TYPE_HEADER = 0
    private val ROW_TYPE_ONE_LINE = 1
    private val ROW_TYPE_TWO_LINE = 2
    private val ROW_TYPE_DIV = 3
    private val ROW_TYPE_DIV_INSET = 4
    private val ROW_TYPE_SIDE_BAR_HEADING = 5
    private val ROW_TYPE_CHAIN = 6
    private val ROW_TYPE_NEW_CHAIN = 7

    private var mRows: List<Any>
    private var mColor: Int
    private var mContext: Context

    init {
        mRows = rows
        mColor = ctx.resources.getColor(R.color.primary)
        mContext = ctx
        notifyDataSetChanged()
    }

    public fun setRows(rows: List<Any>) {
        mRows = rows
        notifyDataSetChanged()
    }

    public fun setAccentColor(color: Int) {
        mColor = color;
        if (getItemCount() > 0 && getItemViewType(0) == ROW_TYPE_HEADER) {
            notifyItemChanged(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == ROW_TYPE_HEADER) {
            val v = inflater.inflate(R.layout.row_sidebar_section_header, parent, false)
            return SideBarSectionHeaderViewHolder(v)
        } else if (viewType == ROW_TYPE_ONE_LINE || viewType == ROW_TYPE_NEW_CHAIN) {
            val v = inflater.inflate(R.layout.row_text_one_line, parent, false)
            return RowTextOneLineViewHolder(v)
        } else if (viewType == ROW_TYPE_TWO_LINE) {
            val v = inflater.inflate(R.layout.row_text_two_line, parent, false)
            return RowTextTwoLineViewHolder(v)
        } else if (viewType == ROW_TYPE_DIV || viewType == ROW_TYPE_DIV_INSET) {
            val v = inflater.inflate(R.layout.row_div, parent, false)
            return RowDivViewHolder(v)
        } else if (viewType == ROW_TYPE_SIDE_BAR_HEADING) {
            val v = inflater.inflate(R.layout.row_sidebar_header, parent, false)
            return SideBarHeaderViewHolder(v)
        } else if (viewType == ROW_TYPE_CHAIN) {
            val v = inflater.inflate(R.layout.row_chain_two_line, parent, false)
            return RowChainViewHolder(v)
        }
        return null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = getItemViewType(position)
        val objData = mRows[position]
        when (type) {
            ROW_TYPE_SIDE_BAR_HEADING -> {
                val vh = holder as SideBarHeaderViewHolder
                val data = objData as SideBarHeading
                vh.mTitleTv.text = data.title
                vh.mSubTitleTv.text = data.subtitle
                vh.mContainer.setBackgroundColor(mColor)
            }
            ROW_TYPE_HEADER -> {
                val vh = holder as SideBarSectionHeaderViewHolder
                val data = objData as String
                vh.mTitleTv.setText(data)
            }
            ROW_TYPE_ONE_LINE, ROW_TYPE_NEW_CHAIN -> {
                val vh = holder as RowTextOneLineViewHolder
                val data = objData as SettingsRow
                vh.mTextOne.setText(data.title)
                vh.itemView.setOnClickListener(data.onClickListener)
                if (data.iconResId != -1) {
                    vh.mIcon.setImageResource(data.iconResId)
                    vh.mIcon.visibility = View.VISIBLE
                } else {
                    vh.mIcon.setImageDrawable(null)
                    vh.mIcon.visibility = View.GONE
                }
            }
            ROW_TYPE_TWO_LINE -> {
                val vh = holder as RowTextTwoLineViewHolder
                val data = objData as SettingsRow
                vh.mTextOne.setText(data.title)
                vh.mTextTwo.setText(data.subTitle)
                vh.itemView.setOnClickListener(data.onClickListener)
                if (data.iconResId != -1) {
                    vh.mIcon.setImageResource(data.iconResId)
                    vh.mIcon.visibility = View.VISIBLE
                } else {
                    vh.mIcon.setImageDrawable(null)
                    vh.mIcon.visibility = View.GONE
                }
            }
            ROW_TYPE_DIV_INSET, ROW_TYPE_DIV -> {
                val vh = holder as RowDivViewHolder
                val data = objData as Div
                if (data.isInset) {
                    vh.mSpacer.setVisibility(View.VISIBLE)
                } else {
                    vh.mSpacer.setVisibility(View.GONE)
                }
            }

            ROW_TYPE_CHAIN -> {
                val vh = holder as RowChainViewHolder
                val data = objData as RowChain
                vh.itemView.setOnClickListener(data.onClickListener)
                vh.mTextOne.text = data.chain.title

                vh.mIcon.setCircleBgColor(data.chain.color)
                if (data.isSelected) {
                    vh.mTextOne.setTypeface(null, Typeface.BOLD)
                    vh.mTextOne.setTextColor(mColor)
                    vh.mTextTwo.setTypeface(null, Typeface.BOLD)
                    vh.mTextTwo.setTextColor(mColor)
                } else {
                    vh.mTextOne.setTypeface(null, Typeface.NORMAL)
                    vh.mTextOne.setTextColor(mContext.resources.getColor(R.color.settings_row_title_color))
                    vh.mTextTwo.setTypeface(null, Typeface.NORMAL)
                    vh.mTextTwo.setTextColor(mContext.resources.getColor(R.color.settings_row_subtitle_color))
                }

                if (data.chain.chainContainsToday()) {
                    vh.mIcon.setImageResource(R.drawable.ic_action_checkmark)
                    vh.mIcon.setColorFilter(Color.WHITE)
                } else {
                    vh.mIcon.setImageDrawable(null)
                }
                if (data.chain.chainLength > 0) {
                    vh.mTextTwo.text = mContext.resources.getString(R.string.day_num, data.chain.chainLength)
                    vh.mTextTwo.visibility = View.VISIBLE
                } else {
                    vh.mTextTwo.visibility = View.GONE
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return mRows!!.size
    }

    override fun getItemViewType(position: Int): Int {
        val data = mRows[position]
        if (data is String) {
            return ROW_TYPE_HEADER
        } else if (data is RowCreateChain) {
            return ROW_TYPE_NEW_CHAIN
        } else if (data is SettingsRow) {
            if (TextUtils.isEmpty((data as SettingsRow).subTitle)) {
                return ROW_TYPE_ONE_LINE
            } else {
                return ROW_TYPE_TWO_LINE
            }
        } else if (data is Div) {
            if ((data as Div).isInset) {
                return ROW_TYPE_DIV_INSET
            } else {
                return ROW_TYPE_DIV
            }
        } else if (data is SideBarHeading) {
            return ROW_TYPE_SIDE_BAR_HEADING
        } else if (data is RowChain) {
            return ROW_TYPE_CHAIN

        } else {
            return super.getItemViewType(position)
        }
    }

    open class SettingsRow {
        var title: String? = null
            private set
        var subTitle: String? = null
            private set
        var onClickListener: View.OnClickListener? = null
            private set
        var iconResId = -1
            private set

        constructor(title: String, subTitle: String, onClickListener: View.OnClickListener) {
            this.title = title
            this.subTitle = subTitle
            this.onClickListener = onClickListener
        }

        constructor(title: String, subTitle: String, onClickListener: View.OnClickListener, iconResId: Int) {
            this.title = title
            this.subTitle = subTitle
            this.onClickListener = onClickListener
            this.iconResId = iconResId
        }
    }

    class Div(val isInset: Boolean)

    class SideBarHeading(val title: String, val subtitle: String?)

    class RowChain(val chain: Chain, val isSelected: Boolean, var onClickListener: View.OnClickListener?)

    class RowCreateChain(title: String, subTitle: String, onClickListener: View.OnClickListener, iconResId: Int) : SettingsRow(title, subTitle, onClickListener, iconResId)
}