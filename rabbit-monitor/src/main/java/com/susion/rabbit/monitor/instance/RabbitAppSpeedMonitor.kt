package com.susion.rabbit.monitor.instance

import android.content.Context
import com.google.gson.Gson
import com.susion.rabbit.common.FileUtils
import com.susion.rabbit.entities.*
import com.susion.rabbit.monitor.RabbitMonitor
import com.susion.rabbit.monitor.core.RabbitMonitorProtocol
import com.susion.rabbit.storage.RabbitDbStorageManager
import com.susion.rabbit.tracer.RabbitTracerEventNotifier

/**
 * susionwang at 2019-11-14
 * 应用速度监控:
 * 1. 应用启动速度
 * 2. 页面启动速度、渲染速度
 */
internal class RabbitAppSpeedMonitor(override var isOpen: Boolean = false) : RabbitMonitorProtocol {

    private val TAG = javaClass.simpleName

    private val ASSERT_FILE_NAME = "rabbit_speed_monitor.json"

    private var currentPageName: String = ""
    private val pageApiStatusInfo = HashMap<String, RabbitPageApiInfo>()
    private var configInfo = RabbitAppSpeedMonitorConfig()
    private val monitorPageApiSet = HashSet<String>()
    private val apiSet = HashSet<String>()

    //避免重复统计测试时间
    private var appSpeedCanRecord = false
    private var pageSpeedCanRecord = false

    private var pageSpeedInfo: RabbitPageSpeedInfo =
        RabbitPageSpeedInfo()
    private var appSpeedInfo: RabbitAppStartSpeedInfo =
        RabbitAppStartSpeedInfo()

    //第一个对用户有效的页面 【闪屏页 or 首页】
    private var entryActivityName = ""

    init {
        loadConfig()
        monitorApplicationStart()
    }

    override fun open(context: Context) {
        isOpen = true
        monitorActivitySpeed()
        com.susion.rabbit.RabbitLog.d(TAG, "entryActivityName : $entryActivityName")
    }

    override fun close() {
        isOpen = false
        RabbitTracerEventNotifier.eventNotifier = RabbitTracerEventNotifier.FakeEventListener()
    }

    override fun getMonitorInfo() = RabbitMonitorProtocol.APP_SPEED

    /**
     * 目前定死从 asstets/page_api_list.json 文件中加载
     *
     * TODO:也可以扩展为网络配置
     * */
    private fun loadConfig() {
        try {
            val jsonStr = FileUtils.getAssetString(RabbitMonitor.application!!, ASSERT_FILE_NAME)
            if (jsonStr.isEmpty()) return

            configInfo = Gson().fromJson(jsonStr, RabbitAppSpeedMonitorConfig::class.java)

            configInfo.pageConfigList.forEach {
                monitorPageApiSet.add(it.pageSimpleName)
                it.apiList.forEach { simpleUrl ->
                    apiSet.add(simpleUrl)
                }
            }

            entryActivityName = configInfo.homeActivity

        } catch (e: Exception) {
            com.susion.rabbit.RabbitLog.d(TAG, "loadConfig failed ${e.message} ")
        }
    }

    /**
     * 一个请求结束
     * */
    fun markRequestFinish(requestUrl: String, costTime: Long = 0) {
        val curApiInfo = pageApiStatusInfo[currentPageName] ?: return
        curApiInfo.apiStatusList.forEach {
            if (requestUrl.contains(it.api)) {
                it.isFinish = true
                it.costTime = costTime
            }
        }
    }

    private fun monitorActivitySpeed() {
        RabbitTracerEventNotifier.eventNotifier = object : RabbitTracerEventNotifier.TracerEvent {

            override fun applicationCreateTime(attachBaseContextTime: Long, createEndTime: Long) {
                appSpeedCanRecord = true
                appSpeedInfo.createStartTime = attachBaseContextTime
                appSpeedInfo.createEndTime = createEndTime
                appSpeedInfo.fullShowCostTime = 0
                if (entryActivityName.isEmpty()) {
                    appSpeedCanRecord = false
                    RabbitDbStorageManager.save(appSpeedInfo)
                }
            }

            override fun activityCreateStart(activity: Any, time: Long) {
                currentPageName = activity.javaClass.simpleName
                pageSpeedCanRecord = true
                pageSpeedInfo = RabbitPageSpeedInfo()
                pageSpeedInfo.pageName = activity.javaClass.name
                pageSpeedInfo.createStartTime = time
                resetPageApiRequestStatus(activity.javaClass.simpleName)
            }

            override fun activityCreateEnd(activity: Any, time: Long) {
                pageSpeedInfo.createEndTime = time
            }

            override fun activityResumeEnd(activity: Any, time: Long) {
                pageSpeedInfo.resumeEndTime = time
            }

            override fun activityDrawFinish(activity: Any, time: Long) {
                saveAppPageSpeedInfoToLocal(time)
                if (entryActivityName.isNotEmpty() && appSpeedCanRecord) {
                    saveApplicationStartInfoToLocal(time, activity.javaClass.simpleName)
                }
            }

            //页面测试信息
            private fun saveAppPageSpeedInfoToLocal(drawFinishTime: Long) {
                if (!pageSpeedCanRecord) return

                if (pageSpeedInfo.inflateFinishTime == 0L) {
                    pageSpeedInfo.time = System.currentTimeMillis()
                    pageSpeedInfo.inflateFinishTime = drawFinishTime
                    com.susion.rabbit.RabbitLog.d(
                        TAG,
                        "$currentPageName page inflateFinishTime ---> cost time : ${drawFinishTime - pageSpeedInfo.createStartTime}"
                    )
                }

                val apiStatus = pageApiStatusInfo[currentPageName]
                if (apiStatus != null) {
                    if (apiStatus.allApiRequestFinish()) {
                        com.susion.rabbit.RabbitLog.d(
                            TAG,
                            "$currentPageName page finish all request ---> cost time : ${drawFinishTime - pageSpeedInfo.createStartTime}"
                        )
                        pageSpeedCanRecord = false
                        pageSpeedInfo.fullDrawFinishTime = drawFinishTime
                        pageSpeedInfo.apiRequestCostString = Gson().toJson(apiStatus).toString()
                        RabbitDbStorageManager.save(pageSpeedInfo)
                    }
                } else {
                    pageSpeedCanRecord = false
                    pageSpeedInfo.fullDrawFinishTime = drawFinishTime
                    RabbitDbStorageManager.save(pageSpeedInfo)
                }
            }
        }

    }

    private fun resetPageApiRequestStatus(acSimpleName: String) {
        if (!monitorPageApiSet.contains(acSimpleName)) return
        var pageApiInfo = pageApiStatusInfo[acSimpleName]
        if (pageApiInfo == null) {
            pageApiInfo = RabbitPageApiInfo()
            for (apiConfigInfo in configInfo.pageConfigList) {
                if (apiConfigInfo.pageSimpleName == acSimpleName) {
                    apiConfigInfo.apiList.forEach { apiUrl ->
                        pageApiInfo.apiStatusList.add(
                            RabbitApiInfo(
                                apiUrl,
                                false
                            )
                        )
                    }
                    break
                }
            }
            pageApiStatusInfo[acSimpleName] = pageApiInfo
        } else {
            pageApiInfo.apiStatusList.forEach {
                it.isFinish = false
            }
        }
    }

    private fun monitorApplicationStart() {
        RabbitTracerEventNotifier.eventNotifier = object : RabbitTracerEventNotifier.TracerEvent {
            override fun applicationCreateTime(attachBaseContextTime: Long, createEndTime: Long) {
                appSpeedInfo.createStartTime = attachBaseContextTime
                appSpeedInfo.createEndTime = createEndTime
                if (!RabbitMonitor.isAutoOpen(this@RabbitAppSpeedMonitor)) {
                    RabbitDbStorageManager.save(appSpeedInfo)
                    com.susion.rabbit.RabbitLog.d(TAG, "monitorApplicationStart")
                }
            }
        }
    }

    //应用启动测速信息记录
    private fun saveApplicationStartInfoToLocal(pageDrawFinishTime: Long, pageName: String) {
        if (!appSpeedCanRecord || pageName != entryActivityName) return

        val apiStatus = pageApiStatusInfo[entryActivityName]
        if (apiStatus != null) {
            if (apiStatus.allApiRequestFinish()) {
                appSpeedCanRecord = false
                appSpeedInfo.fullShowCostTime = pageDrawFinishTime - appSpeedInfo.createStartTime
                RabbitDbStorageManager.save(appSpeedInfo)
                com.susion.rabbit.RabbitLog.d(TAG, "saveApplicationStartInfoToLocal")
            }
        } else {
            appSpeedCanRecord = false
            appSpeedInfo.fullShowCostTime = pageDrawFinishTime - appSpeedInfo.createStartTime
            RabbitDbStorageManager.save(appSpeedInfo)
            com.susion.rabbit.RabbitLog.d(TAG, "saveApplicationStartInfoToLocal")
        }

    }

    //是否监控这个请求
    fun monitorRequest(requestUrl: String): Boolean {
        for (api in apiSet) {
            if (requestUrl.contains(api)) {
                return true
            }
        }
        return false
    }

}