package com.susion.rabbit.trace.frame

import android.view.Choreographer
import com.susion.rabbit.RabbitLog
import com.susion.rabbit.reflect.RabbitReflectHelper
import com.susion.rabbit.trace.core.UIThreadLooperMonitor
import java.lang.reflect.Method

/**
 * susionwang at 2019-10-18
 *
 * hook [Choreographer], 监控 doFrame() 回调。
 *
 * 向[Choreographer]中插入3种callback, 监控一帧不同类型的事件运行时间
 */
class FrameTracer : UIThreadLooperMonitor.LooperHandleEventListener {

    private val TAG = javaClass.simpleName

    /**
     * callback type copy from  Choreographer
     * */

    // Callback type: Input callback.  Runs first.
    val CALLBACK_INPUT = 0

    // Callback type: Animation callback.  Runs before traversals.
    val CALLBACK_ANIMATION = 1

    //Callback type: Traversal callback.  Handles layout and draw.  Runs
    //after all other asynchronous messages have been handled.
    val CALLBACK_TRAVERSAL = 2

    // Choreographer's field or method
    private var addTraversalQueue: Method? = null
    private var addInputQueue: Method? = null
    private var addAnimationQueue: Method? = null
    private var choreographer: Choreographer? = null
    private var callbackQueues: Array<Any>? = null
    private var callbackQueueLock: Any? = null
    private val METHOD_ADD_CALLBACK = "addCallbackLocked"

    // Choreographer中每种callback执行的时间
    private val frameListeners = ArrayList<FrameUpdateListener>()
    private var inpueEventCostTimeNs = 0L
    private var animationEventCostTimeNs = 0L
    private var traversalEventCostTimeNs = 0L
    private var actualExecuteDoFrame = false
    private var insertMonitorRunnable = false

    private var oneFrameStartTime = 0L
    private var oneFrameEndTime = 0L
    private var startMonitorDoFrame = false

    fun init() {
        choreographer = Choreographer.getInstance()
        callbackQueueLock = RabbitReflectHelper.reflectField<Any>(choreographer, "mLock")
        callbackQueues =
            RabbitReflectHelper.reflectField<Array<Any>>(choreographer, "mCallbackQueues")

        if (callbackQueues != null) {
            addInputQueue = RabbitReflectHelper.reflectMethod(
                callbackQueues!![CALLBACK_INPUT],
                METHOD_ADD_CALLBACK,
                Long::class.java,
                Any::class.java,
                Any::class.java
            )
            addAnimationQueue = RabbitReflectHelper.reflectMethod(
                callbackQueues!![CALLBACK_ANIMATION],
                METHOD_ADD_CALLBACK,
                Long::class.java,
                Any::class.java,
                Any::class.java
            )
            addTraversalQueue = RabbitReflectHelper.reflectMethod(
                callbackQueues!![CALLBACK_TRAVERSAL],
                METHOD_ADD_CALLBACK,
                Long::class.java,
                Any::class.java,
                Any::class.java
            )
        }
    }

    //主线程在处理消息的时候，才做 doFrame 监控
    override fun onStartHandleEvent() {
        startMonitorChoreographerDoFrame()
    }

    override fun onEndHandleEvent() {
        endMonitorChoreographerDoFrame()
    }

    private fun startMonitorChoreographerDoFrame() {
        startMonitorDoFrame = true
        actualExecuteDoFrame = false
        inpueEventCostTimeNs = 0
        animationEventCostTimeNs = 0
        traversalEventCostTimeNs = 0
        oneFrameStartTime = System.nanoTime()
        insertCallbackToInputQueue()
        RabbitLog.d("trace", "start time : oneFrameStartTime")
    }

    /**
     * 真正计算一帧时间的采集条件 : 主线程消息循环 && 执行了 Choreographer.doFrame()
     * */
    private fun endMonitorChoreographerDoFrame() {
        if (!startMonitorDoFrame || !actualExecuteDoFrame)return

        startMonitorDoFrame = false
        oneFrameEndTime = System.nanoTime()
        traversalEventCostTimeNs = System.nanoTime() - traversalEventCostTimeNs

        RabbitLog.d("trace", "enc time : oneFrameEndTime")

        val oneFrameCostNs = oneFrameEndTime - oneFrameStartTime
        val inputCost = inpueEventCostTimeNs
        val animationCost = animationEventCostTimeNs
        val traversalCost = traversalEventCostTimeNs

        frameListeners.forEach {
            it.doFrame(
                oneFrameCostNs,
                inputCost,
                animationCost,
                traversalCost
            )
        }
    }

    /**
     * 这里添加的callback， 如果不执行 Choreographer.doFrame(), 是不会被执行的。
     * */
    private fun insertCallbackToInputQueue() {
        //已经插过一遍 [时间监控消息] 了， 不继续插入
        if (insertMonitorRunnable){
            return
        }
        insertMonitorRunnable = true
        addCallbackToHead(CALLBACK_INPUT, Runnable {
            insertMonitorRunnable = false
            actualExecuteDoFrame = true
            inpueEventCostTimeNs = System.nanoTime()
        })

        addCallbackToHead(CALLBACK_ANIMATION, Runnable {
            inpueEventCostTimeNs = System.nanoTime() - inpueEventCostTimeNs
            animationEventCostTimeNs = System.nanoTime()
        })

        addCallbackToHead(CALLBACK_TRAVERSAL, Runnable {
            animationEventCostTimeNs = System.nanoTime() - animationEventCostTimeNs
            traversalEventCostTimeNs = System.nanoTime()
        })
    }

    /**
     * 把事件插入到队列的最前面
     * */
    @Synchronized
    private fun addCallbackToHead(type: Int, callback: Runnable) {
        if (callbackQueueLock == null || callbackQueues == null) return
        try {
            synchronized(callbackQueueLock!!) {
                var method: Method? = null

                when (type) {
                    CALLBACK_INPUT -> method = addInputQueue
                    CALLBACK_ANIMATION -> method = addAnimationQueue
                    CALLBACK_TRAVERSAL -> method = addTraversalQueue
                }

                method?.invoke(callbackQueues!![type], -1, callback, null)
            }
        } catch (e: Exception) {
            RabbitLog.e(TAG, e.toString())
        }
    }

    fun addFrameUpdateListener(listener: FrameUpdateListener) {
        frameListeners.add(listener)
    }

    fun removeFrameUpdateListener(listener: FrameUpdateListener) {
        frameListeners.remove(listener)
    }

    fun containFrameUpdateListener(listener: FrameUpdateListener): Boolean {
        return frameListeners.contains(listener)
    }

    interface FrameUpdateListener {
        fun doFrame(
            frameCostNs:Long,
            inputCostNs: Long,
            animationCostNs: Long,
            traversalCostNs: Long
        )
    }

}