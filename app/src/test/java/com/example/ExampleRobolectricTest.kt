package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.TapLockAccessibilityService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TapLock", appName)
    }

    @Test
    fun `accessibility service initial state check`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val isEnabled = TapLockAccessibilityService.isAccessibilityServiceEnabled(context)
        // In clean test environment it should be false initially
        assertFalse(isEnabled)
    }
}
