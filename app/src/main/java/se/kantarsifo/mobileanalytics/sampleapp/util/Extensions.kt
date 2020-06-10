package se.kantarsifo.mobileanalytics.sampleapp.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.CheckBox
import android.widget.EditText

fun EditText.afterTextChanged(action: () -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            action()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // no-op
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // no-op
        }

    })
}

fun CheckBox.onCheckedChanged(action: () -> Unit) {
    this.setOnCheckedChangeListener { _, _ -> action() }
}