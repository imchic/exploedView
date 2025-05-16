package com.example.exploedview.util

import android.util.Log
import com.example.exploedview.BuildConfig

object LogUtil {

    // 로그
    fun v(msg: String) {
        if (BuildConfig.DEBUG) Log.v(
            getClassName(),
            "상세 \uD83D\uDC49\uD83D\uDC49" + buildLogMsg(msg)
        )
    }

    fun d(msg: String) {
        if (BuildConfig.DEBUG) Log.d(
            getClassName(),
            "디버깅 \uD83D\uDC49\uD83D\uDC49" + buildLogMsg(msg)
        )
    }

    fun i(msg: String) {
        if (BuildConfig.DEBUG) Log.i(
            getClassName(),
            "인포 \uD83D\uDC49\uD83D\uDC49" + buildLogMsg(msg)
        )
    }

    fun w(msg: String) {
        if (BuildConfig.DEBUG) Log.w(
            getClassName(),
            "경고 \uD83D\uDC49\uD83D\uDC49" + buildLogMsg(msg)
        )
    }

    fun e(msg: String) {
        if (BuildConfig.DEBUG) Log.e(
            getClassName(),
            "오류 \uD83D\uDC49\uD83D\uDC49" + buildLogMsg(msg)
        )
    }

    /**
     * 호출 클래스 이름을 반환
     */
    private fun getClassName(): String {
        val ste = Thread.currentThread().stackTrace
        return ste[3].fileName.replace(".java", "::")
    }

    /**
     * 로그 메시지 생성
     */
    private fun buildLogMsg(logMsg: String): String {
        // 파일이름
        // 라인번호
        // 메소드이름
        val ste = Thread.currentThread().stackTrace
//        val fileName = ste[3].fileName.replace(".java", "")
//        val lineNumber = ste[3].lineNumber
        val chidFileName = ste[4].fileName
        val childMethodName = ste[4].methodName
        val lineNumber = ste[4].lineNumber

        return "[$chidFileName : $childMethodName : (lineNum: $lineNumber)] -> $logMsg"
    }
}