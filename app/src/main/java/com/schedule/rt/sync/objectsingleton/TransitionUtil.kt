package com.schedule.rt.sync.objectsingleton

import android.content.Context
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.transition.MaterialSharedAxis

object TransitionUtil {

    private const val transitionDuration = 300L

    fun enterTransition(): MaterialSharedAxis {
        return MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            duration = transitionDuration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    fun returnTransition(): MaterialSharedAxis {
        return MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            duration = transitionDuration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    fun exitTransition(): MaterialSharedAxis {
        return MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            duration = transitionDuration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    fun reenterTransition(): MaterialSharedAxis {
        return MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            duration = transitionDuration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    fun slideUpTransition(view: View) {
        val parent = view.parent as? ViewGroup ?: return
        view.visibility = View.INVISIBLE

        val transition = com.google.android.material.transition.platform.MaterialSharedAxis(
            com.google.android.material.transition.platform.MaterialSharedAxis.Y, /* forward = */ true).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

        TransitionManager.beginDelayedTransition(parent, transition)
        view.visibility = View.VISIBLE
    }




    private const val sharedElementTransition = android.R.transition.move

    fun sharedElementEnterTransition(context: Context): androidx.transition.Transition? {
        val transitionInflater = androidx.transition.TransitionInflater.from(context)
            .inflateTransition(sharedElementTransition)

        if (transitionInflater is androidx.transition.Transition) {
            transitionInflater.duration = transitionDuration
            transitionInflater.interpolator = AccelerateDecelerateInterpolator()
        }

        return transitionInflater
    }

    fun sharedElementReturnTransition(context: Context): androidx.transition.Transition? {
        val transitionInflater = androidx.transition.TransitionInflater.from(context)
            .inflateTransition(sharedElementTransition)

        if (transitionInflater is androidx.transition.Transition) {
            transitionInflater.duration = transitionDuration
            transitionInflater.interpolator = AccelerateDecelerateInterpolator()
        }

        return transitionInflater
    }
}