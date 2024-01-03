package com.test.customsnackbar

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar


object CustomSnackBar {

    fun showSnackBar(view: View, message: String) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view

        // Set text color
        val textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text) as? TextView
        textView?.setTextColor(ContextCompat.getColor(view.context, android.R.color.white))
        textView?.gravity=Gravity.CENTER

        // Set position
        val params = snackBarView.layoutParams as? FrameLayout.LayoutParams
        params?.gravity = Gravity.BOTTOM
        snackBarView.layoutParams = params

        snackBar.show()
    }
}