package com.stylingandroid.mandelbrot

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class MandelbrotLifecycleObserver(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    imageView: MandelbrotView
) : CoroutineScope, LifecycleObserver {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val renderer: MandelbrotRenderer = MandelbrotRenderer(imageView, coroutineContext)
    private val viewportDelegate: ViewportDelegate

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        viewportDelegate = ViewportDelegate(context, imageView, renderer)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Suppress("Unused")
    fun onDestroy() {
        job.cancel()
        renderer.destroy()
    }
}
