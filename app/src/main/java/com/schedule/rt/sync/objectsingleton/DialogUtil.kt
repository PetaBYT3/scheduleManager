package com.schedule.rt.sync.objectsingleton

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.BottomSheet1etBinding
import com.schedule.rt.sync.databinding.BottomSheet2etBinding
import com.schedule.rt.sync.databinding.BottomSheetConfirmationBinding
import com.schedule.rt.sync.databinding.BottomSheetScheduleBinding


object DialogUtil {

    fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()


    fun showPopUp(
        activity: Activity,
        message: String,
        bottomSheetView: View,
        duration: Long = 2000L
    ) {
        val inflater = LayoutInflater.from(activity)
        val popupView = inflater.inflate(R.layout.pop_up, null)
        val tvPopUp = popupView.findViewById<TextView>(R.id.tvPopUp)
        tvPopUp.text = message

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        ).apply {
            elevation = 20f
            isTouchable = false
            isFocusable = false
            isOutsideTouchable = true
            setBackgroundDrawable(null)
        }

        // Ukur view dulu untuk mendapatkan dimensi toast
        popupView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        val popupHeight = popupView.measuredHeight

        // Hitung posisi BottomSheet di layar
        val location = IntArray(2)
        bottomSheetView.getLocationOnScreen(location)
        val bottomSheetTopY = location[1] // posisi Y teratas dari BottomSheet

        // Tempatkan popup tepat di atas BottomSheet
        val x = (activity.window.decorView.width - popupView.measuredWidth) / 2
        val y = bottomSheetTopY - popupHeight - 16.dpToPx(activity) // kasih jarak 16dp

        popupWindow.showAtLocation(activity.window.decorView.rootView, Gravity.TOP or Gravity.START, x, y)

        Handler(Looper.getMainLooper()).postDelayed({
            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
        }, duration)
    }

    fun showToast(
        context: Activity,
        message: String,
        icon: Int
    ) {
        val inflater = context.layoutInflater
        val layout = inflater.inflate(R.layout.toast, null)

        val tvToast = layout.findViewById<TextView>(R.id.tvToast)
        val ivToast = layout.findViewById<ImageView>(R.id.ivToast)

        tvToast.text = message
        ivToast.setImageResource(icon)

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout

        toast.show()
    }

    fun showBottomSheetConfirmation(
        context: Context,
        tittle: String,
        ivYes: Int,
        tvYes: String,
        onBottomSheetCreated: (BottomSheetConfirmationBinding, BottomSheetDialog) -> Unit,
    ) : BottomSheetDialog {
        val bottomSheetBinding = BottomSheetConfirmationBinding.inflate(LayoutInflater.from(context))
        val bottomSheet = BottomSheetDialog(context)
        bottomSheet.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.tvTittleBottomSheet.text = tittle

        bottomSheetBinding.ivYes.setImageResource(ivYes)
        bottomSheetBinding.tvYes.text = tvYes

        bottomSheet.show()

        onBottomSheetCreated(bottomSheetBinding, bottomSheet)
        return bottomSheet
    }

    fun showBottomSheet1Et(
        context: Context,
        tittle: String,
        etFirstHint: String,
        ivYes: Int,
        tvYes: String,
        onBottomSheetCreated: (BottomSheet1etBinding, BottomSheetDialog) -> Unit,
    ) : BottomSheetDialog {
        val bottomSheetBinding = BottomSheet1etBinding.inflate(LayoutInflater.from(context))
        val bottomSheet = BottomSheetDialog(context)
        bottomSheet.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.tvTittle.text = tittle

        bottomSheetBinding.tiFirst.hint = etFirstHint

        bottomSheetBinding.ivYes.setImageResource(ivYes)
        bottomSheetBinding.tvYes.text = tvYes

        bottomSheet.show()

        onBottomSheetCreated(bottomSheetBinding, bottomSheet)
        return bottomSheet
    }

    fun showBottomSheet2Et(
        context: Context,
        tittle: String,
        etFirstHint: String,
        etSecondHint: String,
        ivYes: Int,
        tvYes: String,
        onBottomSheetCreated: (BottomSheet2etBinding, BottomSheetDialog) -> Unit,
    ) : BottomSheetDialog {
        val bottomSheetBinding = BottomSheet2etBinding.inflate(LayoutInflater.from(context))
        val bottomSheet = BottomSheetDialog(context)
        bottomSheet.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.tvTittle.text = tittle

        bottomSheetBinding.tiFirst.hint = etFirstHint
        bottomSheetBinding.tiSecond.hint = etSecondHint

        bottomSheetBinding.ivYes.setImageResource(ivYes)
        bottomSheetBinding.tvYes.text = tvYes

        bottomSheet.show()

        onBottomSheetCreated(bottomSheetBinding, bottomSheet)
        return bottomSheet
    }

    fun showBottomSheetSchedule(
        context: Context,
        tittle: String,
        iconYes: Int,
        tvYes: String,
        onBottomSheetCreated: (BottomSheetScheduleBinding, BottomSheetDialog) -> Unit,
    ) : BottomSheetDialog {
        val bottomSheetBinding = BottomSheetScheduleBinding.inflate(LayoutInflater.from(context))
        val bottomSheet = BottomSheetDialog(context)
        bottomSheet.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.toolBar.setNavigationOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheetBinding.toolBar.title = tittle

        val tpStart = bottomSheetBinding.tpStart
        tpStart.setIs24HourView(true)
        tpStart.hour = 6
        tpStart.minute = 0


        val btnYes = bottomSheetBinding.btnYes
        btnYes.setCompoundDrawablesRelativeWithIntrinsicBounds(iconYes, 0, 0, 0)
        btnYes.text = tvYes

        bottomSheet.show()

        onBottomSheetCreated(bottomSheetBinding, bottomSheet)
        return bottomSheet
    }
}