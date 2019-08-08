package com.devheart.geckoviewbrowser

import android.content.Context
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

fun View.hideKeyboard() {
    val imm = (context.getSystemService(Context.INPUT_METHOD_SERVICE) ?: return)
            as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun AppCompatActivity.showDialog(items: List<Spanned>) {
    AlertDialog.Builder(this)
        .setTitle(getString(R.string.trackers_blocked))
        .setItems(items.toTypedArray(), null)
        .create()
        .show()
}

const val SEARCH_URI_BASE = "https://duckduckgo.com/?q="
const val INITIAL_URL = "https://www.mozilla.org"