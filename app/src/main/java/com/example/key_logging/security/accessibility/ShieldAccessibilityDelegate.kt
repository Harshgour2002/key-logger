package com.example.key_logging.security.accessibility

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider

/**
 * An AccessibilityDelegate that installs our sanitizing provider wrapper and sanitizes events.
 */
class ShieldAccessibilityDelegate(
    private val originalDelegate: View.AccessibilityDelegate?
) : View.AccessibilityDelegate() {

    companion object {
        @Suppress("unused")
        private const val TAG = "ShieldDelegate"
    }

    override fun getAccessibilityNodeProvider(host: View): AccessibilityNodeProvider? {
        // 1. Safely retrieve the real provider without triggering recursion
        val provider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val currentDelegate = host.accessibilityDelegate
            host.accessibilityDelegate = null
            val p = host.accessibilityNodeProvider ?: originalDelegate?.getAccessibilityNodeProvider(host)
            host.accessibilityDelegate = currentDelegate
            p
        } else {
            originalDelegate?.getAccessibilityNodeProvider(host) ?: super.getAccessibilityNodeProvider(host)
        }

        if (provider == null || provider is SanitizingAccessibilityNodeProvider) return provider
        
        // 2. Return a sanitizing wrapper. 
        return SanitizingAccessibilityNodeProvider(provider)
    }

    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
        if (originalDelegate != null) {
            originalDelegate.onInitializeAccessibilityNodeInfo(host, info)
        } else {
            super.onInitializeAccessibilityNodeInfo(host, info)
        }
        
        if (SensitiveNodeDetector.isSensitive(info)) {
            AccessibilityNodeSanitizer.sanitize(info)
        }
    }

    override fun sendAccessibilityEvent(host: View, eventType: Int) {
        // Block sensitive triggers at the source
        if (isSensitiveEvent(eventType)) {
            return
        }
        
        if (originalDelegate != null) {
            originalDelegate.sendAccessibilityEvent(host, eventType)
        } else {
            super.sendAccessibilityEvent(host, eventType)
        }
    }

    override fun sendAccessibilityEventUnchecked(host: View, event: AccessibilityEvent) {
        if (isSensitiveEvent(event.eventType)) {
            return
        }
        
        sanitizeEvent(event)
        
        if (originalDelegate != null) {
            originalDelegate.sendAccessibilityEventUnchecked(host, event)
        } else {
            super.sendAccessibilityEventUnchecked(host, event)
        }
    }
    
    override fun dispatchPopulateAccessibilityEvent(host: View, event: AccessibilityEvent): Boolean {
        sanitizeEvent(event)
        return if (originalDelegate != null) {
            originalDelegate.dispatchPopulateAccessibilityEvent(host, event)
        } else {
            super.dispatchPopulateAccessibilityEvent(host, event)
        }
    }

    override fun onRequestSendAccessibilityEvent(host: ViewGroup, child: View, event: AccessibilityEvent): Boolean {
        if (isSensitiveEvent(event.eventType)) {
            return false
        }
        
        sanitizeEvent(event)

        return if (originalDelegate != null) {
            originalDelegate.onRequestSendAccessibilityEvent(host, child, event)
        } else {
            super.onRequestSendAccessibilityEvent(host, child, event)
        }
    }

    override fun performAccessibilityAction(host: View, action: Int, args: Bundle?): Boolean {
        return if (originalDelegate != null) {
            originalDelegate.performAccessibilityAction(host, action, args)
        } else {
            super.performAccessibilityAction(host, action, args)
        }
    }

    private fun isSensitiveEvent(eventType: Int): Boolean {
        return eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
               eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED ||
               eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
               eventType == AccessibilityEvent.TYPE_VIEW_CLICKED
    }

    private fun sanitizeEvent(event: AccessibilityEvent) {
        try {
            if (isSensitiveEvent(event.eventType) || event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                event.text.clear()
                event.beforeText = null
                event.contentDescription = null
                
                for (i in 0 until event.recordCount) {
                    val record = event.getRecord(i)
                    record.text.clear()
                    record.beforeText = null
                    record.contentDescription = null
                }
            }
        } catch (ignored: Exception) {
        }
    }
}
