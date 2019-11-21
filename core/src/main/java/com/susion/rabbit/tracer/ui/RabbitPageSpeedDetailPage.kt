package com.susion.rabbit.tracer.ui

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.susion.rabbit.R
import com.susion.rabbit.tracer.entities.RabbitPageSpeedInfo
import com.susion.rabbit.tracer.entities.RabbitPageSpeedUiInfo
import com.susion.rabbit.ui.page.RabbitBasePage
import com.susion.rabbit.utils.dp2px
import kotlinx.android.synthetic.main.rabbit_page_page_speed_detail.view.*
import java.util.ArrayList


/**
 * susionwang at 2019-10-29
 */
class RabbitPageSpeedDetailPage(context: Context) : RabbitBasePage(context) {


    override fun getLayoutResId() = R.layout.rabbit_page_page_speed_detail

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setTitle("页面测速详情")
        mRabbitSpeedDetailChart.apply {
            setBackgroundColor(Color.parseColor("#90caf9"))
            setTouchEnabled(true)
            description.isEnabled = false
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setPadding(dp2px(3f), dp2px(3f), dp2px(3f), dp2px(3f))
            axisRight.isEnabled = false
            xAxis.isEnabled = false
        }

    }

    override fun setEntryParams(blockInfo: Any) {
        if (blockInfo !is RabbitPageSpeedUiInfo) return

        mRabbitSpeedDetailTvPageName.text = blockInfo.pageName.split(".").lastOrNull() ?: ""

        mRabbitSpeedDetailTvCreateTime.text = "${(blockInfo.speedInfoList.map { it.pageCreateTime }.average()).toInt()} ms"
        mRabbitSpeedDetailTvInlateTime.text = "${(blockInfo.speedInfoList.map { it.pageInflateTime }.average()).toInt()} ms"
        mRabbitSpeedDetailTvFullRenderTime.text = "${(blockInfo.speedInfoList.map { it.fullRenderTime }.average()).toInt()} ms"

        renderChart(  blockInfo.speedInfoList.sortedBy { it.time })
    }

    private fun renderChart(speedInfoList: List<RabbitPageSpeedInfo>) {

        val createTimes = ArrayList<Entry>()
        speedInfoList.forEachIndexed { index, rabbitPageSpeedInfo ->
            createTimes.add(Entry(index.toFloat(), rabbitPageSpeedInfo.pageCreateTime.toFloat()))
        }

        val inflateTimes = ArrayList<Entry>()
        speedInfoList.forEachIndexed { index, rabbitPageSpeedInfo ->
            inflateTimes.add(Entry(index.toFloat(), rabbitPageSpeedInfo.pageInflateTime.toFloat()))
        }

        val renderTimes = ArrayList<Entry>()
        speedInfoList.forEachIndexed { index, rabbitPageSpeedInfo ->
            renderTimes.add(Entry(index.toFloat(), rabbitPageSpeedInfo.fullRenderTime.toFloat()))
        }

        val dataSets = ArrayList<ILineDataSet>()
        val onCreateDatas = LineDataSet(createTimes, "onCreate耗时").apply {
            setCircleColor(Color.parseColor("#00e676"))
            color = Color.parseColor("#00e676")
        }

        val inflateDatas = LineDataSet(inflateTimes, "inflate耗时").apply {
            setCircleColor(Color.parseColor("#ff4081"))
            color = Color.parseColor("#ff4081")
        }

        val fullDrawDatas = LineDataSet(renderTimes, "完全渲染耗时").apply {
            setCircleColor(Color.parseColor("#aa00ff"))
            color = Color.parseColor("#aa00ff")
        }

        dataSets.add(onCreateDatas)
        dataSets.add(inflateDatas)
        dataSets.add(fullDrawDatas)

        mRabbitSpeedDetailChart.data =  LineData(dataSets)

    }

}