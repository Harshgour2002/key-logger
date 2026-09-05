package com.example.key_logging

import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.key_logging.security.SecuritySdk
import com.example.key_logging.security.accessibility.SanitizingAccessibilityNodeProvider
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityShieldTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAccessibilityShieldSanitization() {
        val activity = composeTestRule.activity
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        instrumentation.runOnMainSync {
            SecuritySdk.enableAccessibilityShield(activity)
            
            val editText = EditText(activity)
            editText.setText("SensitiveText123")
            activity.setContentView(editText)
            
            SecuritySdk.enableAccessibilityShield(activity)

            @Suppress("DEPRECATION")
            val info = AccessibilityNodeInfo.obtain()
            editText.onInitializeAccessibilityNodeInfo(info)

            // Verify node sanitization
            assertNull("Text should be null", info.text)
            assertTrue("Should be marked as password", info.isPassword)
            assertFalse("Should be marked as not editable", info.isEditable)
            
            @Suppress("DEPRECATION")
            info.recycle()
        }
    }

    @Test
    fun testEventBlocking() {
        val activity = composeTestRule.activity
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        instrumentation.runOnMainSync {
            SecuritySdk.enableAccessibilityShield(activity)
            val editText = EditText(activity)
            activity.setContentView(editText)
            SecuritySdk.enableAccessibilityShield(activity)
            
            // Try to send a text change event
            val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED)
            event.text.add("LeakThis")
            
            // We can't easily check if the system received it, 
            // but we can check if our delegate clears it if we call sendAccessibilityEventUnchecked
            editText.accessibilityDelegate?.sendAccessibilityEventUnchecked(editText, event)
            
            assertTrue("Event text should be cleared", event.text.isEmpty())
            assertNull("Before text should be null", event.beforeText)
            
            @Suppress("DEPRECATION")
            event.recycle()
        }
    }

    @Test
    fun testComposeAccessibilityShieldInstallation() {
        val activity = composeTestRule.activity
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        composeTestRule.waitForIdle()

        instrumentation.runOnMainSync {
            val decorView = activity.window.decorView
            decorView.createAccessibilityNodeInfo()
            
            val composeView = findViewByClassName(decorView, "AndroidComposeView")
                ?: findViewByClassName(decorView, "ComposeView")
            
            assertNotNull("Compose host view should be found", composeView)
            
            val provider = composeView?.accessibilityNodeProvider
            assertNotNull("AccessibilityNodeProvider should be present", provider)
            assertTrue("Provider should be our wrapped version",
                provider is SanitizingAccessibilityNodeProvider)
        }
    }

    private fun findViewByClassName(view: View, className: String): View? {
        if (view.javaClass.name.contains(className)) {
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findViewByClassName(view.getChildAt(i), className)
                if (found != null) return found
            }
        }
        return null
    }
}
