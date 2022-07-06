package com.example.exploedview

import android.util.Log


class Utils(classStr: String) {

    private var classNm: String = classStr

    /* 로그 */
    fun logV(msg: String) {
        if (BuildConfig.DEBUG) Log.v(classNm, "상세 \uD83D\uDC49\uD83D\uDC49" + buildLogMsg(msg))
    }

    fun logD(msg: String) {
        if (BuildConfig.DEBUG) Log.d(classNm, "디버깅 \uD83D\uDC49\uD83D\uDC49" + buildLogMsg(msg))
    }

    fun logI(msg: String) {
        if (BuildConfig.DEBUG) Log.i(classNm, "정보 \uD83D\uDC49\uD83D\uDC49" + buildLogMsg(msg))
    }

    fun logW(msg: String) {
        if (BuildConfig.DEBUG) Log.w(classNm, "경고 \uD83D\uDC49\uD83D\uDC49" + buildLogMsg(msg))
    }

    fun logE(msg: String) {
        if (BuildConfig.DEBUG) Log.e(classNm, "오류 \uD83D\uDC49\uD83D\uDC49" + buildLogMsg(msg))
    }

    /**
     * 로그에 띄울 메세지를 만들어주는 메서드
     * @param logMsg
     * @return
     */

    private fun buildLogMsg(logMsg: String): String {
        val ste = Thread.currentThread().stackTrace[4]
        val sb = StringBuilder()
        sb.append("  [")
        sb.append(ste.fileName.replace(".java", "::"))
        sb.append("]")
        sb.append("[")
        sb.append(ste.lineNumber)
        sb.append("]")
        sb.append("[")
        sb.append(ste.methodName)
        sb.append("]")
        sb.append(" → $logMsg")
        return sb.toString()
    }
}