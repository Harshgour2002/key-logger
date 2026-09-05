package com.example.key_logging.security

import android.app.Activity
import com.example.key_logging.security.accessibility.AccessibilityShield

/**
 * Public API for the Security SDK.
 */
object SecuritySdk {

    /**
     * Enables the Accessibility Shield for the given Activity.
     * This protects sensitive text fields from malicious accessibility services.
     */
    @JvmStatic
    fun enableAccessibilityShield(activity: Activity) {
        AccessibilityShield.install(activity)
    }
}
