package com.example.key_logging.security.accessibility

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.example.key_logging.R

/**
 * Core engine for installing Accessibility Keylogger Protection.
 */
object AccessibilityShield {

    private const val TAG = "AccessibilityShield"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun install(activity: Activity) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { install(activity) }
            return
        }

        val decorView = try {
            activity.window.decorView
        } catch (e: Exception) {
            return
        }

        installRecursively(decorView)
        
        // Dynamic detection of new views
        val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            installRecursively(decorView)
        }
        
        decorView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        
        // Also use hierarchy change listener for more immediate detection
        setupHierarchyListenerRecursively(decorView)
        
        decorView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                try {
                    v.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
                } catch (ignored: Exception) {}
                v.removeOnAttachStateChangeListener(this)
            }
        })
    }

    private fun setupHierarchyListenerRecursively(view: View) {
        if (view is ViewGroup) {
            // Note: This replaces existing hierarchy listener if any. 
            // In production, we might want to chain them.
            view.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    if (child != null) {
                        installRecursively(child)
                        setupHierarchyListenerRecursively(child)
                    }
                }
                override fun onChildViewRemoved(parent: View?, child: View?) {}
            })
            
            for (i in 0 until view.childCount) {
                setupHierarchyListenerRecursively(view.getChildAt(i))
            }
        }
    }

    private fun installRecursively(view: View) {
        installOnView(view)

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                installRecursively(view.getChildAt(i))
            }
        }
    }

    private fun installOnView(view: View) {
        val currentDelegate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.accessibilityDelegate
        } else {
            null
        }

        if (currentDelegate is ShieldAccessibilityDelegate) {
            return
        }

        // Install our delegate
        view.accessibilityDelegate = ShieldAccessibilityDelegate(currentDelegate)
        view.setTag(R.id.accessibility_shield_installed, true)
        
        if (view.javaClass.name.contains("AndroidComposeView")) {
            Log.d(TAG, "Shield installed on Compose Host")
        }
    }
}
