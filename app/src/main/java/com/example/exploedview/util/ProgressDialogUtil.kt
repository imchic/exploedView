package com.example.exploedview.util

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.example.exploedview.R

object ProgressDialogUtil {

    fun showProgressDialog(activity: Activity): AlertDialog {
        val dialog = AlertDialog.Builder(activity)
            .setView(R.layout.progress_dialog)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        return dialog
    }

    fun dismissProgressDialog(dialog: AlertDialog?) {
        dialog?.dismiss()
    }
}