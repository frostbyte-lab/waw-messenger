package com.waw.messenger.linked

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.MetricAffectingSpan
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.waw.messenger.R

object FaText {
    private class TypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
        override fun updateDrawState(textPaint: android.text.TextPaint) { textPaint.typeface = typeface }
        override fun updateMeasureState(textPaint: android.text.TextPaint) { textPaint.typeface = typeface }
    }

    fun set(textView: TextView, context: Context, icon: String, label: String) {
        val value = "$icon  $label"
        val styled = SpannableString(value)
        ResourcesCompat.getFont(context, R.font.fa_solid_900)?.let { font ->
            styled.setSpan(TypefaceSpan(font), 0, icon.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        textView.text = styled
    }
}
