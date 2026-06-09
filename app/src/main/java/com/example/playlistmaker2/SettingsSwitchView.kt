package com.example.playlistmaker2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Checkable
import android.widget.Switch
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class SettingsSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr), Checkable {

    private val trackBounds = RectF()
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val switchWidth = resources.getDimension(R.dimen.settings_switch_width)
    private val switchHeight = resources.getDimension(R.dimen.settings_switch_height)
    private val trackWidth = resources.getDimension(R.dimen.settings_switch_track_width)
    private val trackHeight = resources.getDimension(R.dimen.settings_switch_track_height)
    private val trackRadius = resources.getDimension(R.dimen.settings_switch_track_radius)
    private val trackLeft = resources.getDimension(R.dimen.settings_switch_track_margin_start)
    private val trackTop = resources.getDimension(R.dimen.settings_switch_track_margin_top)
    private val thumbSize = resources.getDimension(R.dimen.settings_switch_thumb_size)
    private val thumbCheckedLeft = resources.getDimension(R.dimen.settings_switch_thumb_margin_start)
    private val thumbUncheckedLeft = resources.getDimension(R.dimen.settings_switch_thumb_unchecked_margin_start)
    private val thumbTop = resources.getDimension(R.dimen.settings_switch_thumb_margin_top)
    private val thumbShadowRadius = resources.getDimension(R.dimen.settings_switch_thumb_shadow_radius)

    @ColorInt
    private val checkedTrackColor = ContextCompat.getColor(context, R.color.yp_switch_track_checked)

    @ColorInt
    private val checkedThumbColor = ContextCompat.getColor(context, R.color.yp_switch_thumb_checked)

    @ColorInt
    private val uncheckedTrackColor = ContextCompat.getColor(context, R.color.yp_switch_track_unchecked)

    @ColorInt
    private val uncheckedThumbColor = ContextCompat.getColor(context, R.color.yp_switch_thumb_unchecked)

    @ColorInt
    private val thumbShadowColor = ContextCompat.getColor(context, R.color.yp_switch_thumb_shadow)

    private var checked = true

    init {
        isClickable = true
        isFocusable = true
        minimumWidth = switchWidth.roundToInt()
        minimumHeight = switchHeight.roundToInt()
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        thumbPaint.setShadowLayer(thumbShadowRadius, 0f, 0f, thumbShadowColor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = resolveSize(switchWidth.roundToInt(), widthMeasureSpec)
        val measuredHeight = resolveSize(switchHeight.roundToInt(), heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        trackPaint.color = if (checked) checkedTrackColor else uncheckedTrackColor
        trackBounds.set(trackLeft, trackTop, trackLeft + trackWidth, trackTop + trackHeight)
        canvas.drawRoundRect(trackBounds, trackRadius, trackRadius, trackPaint)

        thumbPaint.color = if (checked) checkedThumbColor else uncheckedThumbColor
        val thumbLeft = if (checked) thumbCheckedLeft else thumbUncheckedLeft
        val thumbRadius = thumbSize / 2f
        canvas.drawCircle(thumbLeft + thumbRadius, thumbTop + thumbRadius, thumbRadius, thumbPaint)
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }

    override fun toggle() {
        setChecked(!checked)
    }

    override fun isChecked(): Boolean = checked

    override fun setChecked(checked: Boolean) {
        if (this.checked == checked) return
        this.checked = checked
        refreshDrawableState()
        invalidate()
    }

    @Suppress("DEPRECATION")
    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = Switch::class.java.name
        info.isCheckable = true
        info.isChecked = checked
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState(), checked)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        setChecked(state.checked)
    }

    private class SavedState : BaseSavedState {
        val checked: Boolean

        constructor(superState: Parcelable?, checked: Boolean) : super(superState) {
            this.checked = checked
        }

        private constructor(parcel: Parcel) : super(parcel) {
            checked = parcel.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (checked) 1 else 0)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}

