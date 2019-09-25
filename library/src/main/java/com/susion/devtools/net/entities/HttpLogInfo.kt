package com.susion.devtools.net.entities

import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

/**
 * susionwang at 2019-09-24
 *
 * http/https response info
 */
class HttpLogInfo(
    var host: String = "",
    var path: String = "",
    var getRequestParams: HashMap<String, String> = HashMap(),
    var responseStr: String = "",
    var tookTime: Long = 0L,
    var size: String = "",
    var requestType: String = RequestType.GET,
    var responseContentType: String = ResponseContentType.GSON,
    var time:Long = System.currentTimeMillis()
) : Serializable {

    object RequestType {
        val GET = "get"
        val POST = "post"
        fun isGet(type:String):Boolean = type == GET || type == GET.toLowerCase()
    }

    object ResponseContentType {
        val GSON = "gson"
    }

    fun isValid(): Boolean {
        return host.isNotEmpty() && path.isNotEmpty() && responseStr.isNotEmpty()
    }

    override fun toString(): String {
        return "$host$path$getRequestParams$responseStr$tookTime"
    }

}