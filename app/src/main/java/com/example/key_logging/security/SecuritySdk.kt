package com.example.key_logging.security

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider
import com.example.key_logging.R


object SecuritySdk {

    @JvmStatic
    fun enableAccessibilityShield(activity: Activity) {
        val decorView = try { activity.window.decorView } catch (e: Exception) { return }

        // Trigger 1: Protect current focus immediately
        install(decorView.findFocus())

        // Trigger 2: O(1) Lazy discovery via global focus changes
        decorView.viewTreeObserver.addOnGlobalFocusChangeListener { _, newFocus ->
            install(newFocus)
        }

        // Trigger 3: Supplemental discovery via event bubbling
        install(decorView)
    }

    private fun install(view: View?) {
        if (view == null || view.getTag(R.id.accessibility_shield_installed) == true) return

        val originalDelegate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.accessibilityDelegate
        } else null

        if (originalDelegate is ShieldDelegate) return

        view.accessibilityDelegate = ShieldDelegate(originalDelegate)
        view.setTag(R.id.accessibility_shield_installed, true)
    }

    private fun sanitize(info: AccessibilityNodeInfo?) {
        if (info == null) return

        // 1. Enhanced Sensitivity Heuristic
        val className = info.className ?: ""
        val isInputBuffer = info.isEditable || info.isPassword ||
                className.contains("EditText", ignoreCase = true) ||
                className.contains("TextField", ignoreCase = true)

        if (!isInputBuffer) return

        // 2. SECURITY BOUNDARY: Mask and Nullify
        info.isPassword = true
        info.text = null
        info.contentDescription = null

        // 3. Action Scrubbing (Prevent Copy/SetText)
        info.removeAction(AccessibilityNodeInfo.ACTION_COPY)
        info.removeAction(AccessibilityNodeInfo.ACTION_CUT)
        info.removeAction(AccessibilityNodeInfo.ACTION_PASTE)
        info.removeAction(AccessibilityNodeInfo.ACTION_SET_TEXT)

        // 4. API-specific text channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) info.hintText = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) info.error = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.tooltipText = null
            info.paneTitle = null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) info.stateDescription = null
        
        // 5. Metadata Cleansing
        try { info.extras?.clear() } catch (ignored: Exception) {}

        // 6. Obfuscate state
        info.isEditable = false
    }

    private class ShieldDelegate(
        private val original: View.AccessibilityDelegate?
    ) : View.AccessibilityDelegate() {

        private var isCapturingProvider = false

        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
            original?.onInitializeAccessibilityNodeInfo(host, info) ?: super.onInitializeAccessibilityNodeInfo(host, info)
            sanitize(info)
        }

        override fun getAccessibilityNodeProvider(host: View): AccessibilityNodeProvider? {
            if (isCapturingProvider) return null // Prevent recursion

            isCapturingProvider = true
            val provider = try {
                original?.getAccessibilityNodeProvider(host) ?: host.accessibilityNodeProvider
            } finally {
                isCapturingProvider = false
            }

            if (provider == null || provider is SanitizingProvider) return provider
            return SanitizingProvider(provider)
        }

        override fun onRequestSendAccessibilityEvent(host: ViewGroup, child: View, event: AccessibilityEvent): Boolean {
            // Lazy Discovery: When a view/virtual node acts, ensure the host is shielded
            install(child)

            // Wipe textual data from the event itself (Supplementary)
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
                event.text?.clear()
                event.contentDescription = null
            }

            return original?.onRequestSendAccessibilityEvent(host, child, event) ?:
            super.onRequestSendAccessibilityEvent(host, child, event)
        }
    }

    private class SanitizingProvider(private val original: AccessibilityNodeProvider) : AccessibilityNodeProvider() {
        override fun createAccessibilityNodeInfo(virtualViewId: Int): AccessibilityNodeInfo? =
            original.createAccessibilityNodeInfo(virtualViewId)?.also { sanitize(it) }

        override fun findFocus(focus: Int): AccessibilityNodeInfo? =
            original.findFocus(focus)?.also { sanitize(it) }

        override fun findAccessibilityNodeInfosByText(text: String?, id: Int): List<AccessibilityNodeInfo>? =
            original.findAccessibilityNodeInfosByText(text, id)?.onEach { sanitize(it) }

        override fun performAction(id: Int, action: Int, args: Bundle?): Boolean =
            original.performAction(id, action, args)

        override fun addExtraDataToAccessibilityNodeInfo(id: Int, info: AccessibilityNodeInfo, key: String?, args: Bundle?) {
            original.addExtraDataToAccessibilityNodeInfo(id, info, key, args)
            sanitize(info)
        }
    }
}