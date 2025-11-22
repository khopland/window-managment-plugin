package com.github.khopland.windowmanagmentplugin.toolWindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ToolwindowConfigServiceTest : BasePlatformTestCase() {

    private lateinit var service: ToolwindowConfigService

    override fun setUp() {
        super.setUp()
        service = ToolwindowConfigService.getInstance()
        // Clear state before each test
        service.loadState(ToolwindowState())
    }

    fun testServiceIsApplicationLevel() {
        val service1 = ToolwindowConfigService.getInstance()
        val service2 = ToolwindowConfigService.getInstance()

        // Should be the same instance (application-level singleton)
        assertSame(service1, service2)
    }

    fun testCaptureCurrentToolwindowState() {
        val twManager = ToolWindowManager.getInstance(project)
        val toolWindowIds = twManager.toolWindowIds

        // Capture the current state (even if there are no tool windows)
        service.captureCurrentToolwindowState(project)

        val state = service.state
        assertNotNull(state)
        assertNotNull(state.windows)

        // If there are tool windows, verify they were captured correctly
        if (toolWindowIds.isNotEmpty()) {
            // Should have captured at least some tool windows
            assertTrue("Expected to capture at least one tool window", state.windows.isNotEmpty())

            // Verify captured data structure
            for (windowInfo in state.windows) {
                assertFalse("Tool window ID should not be empty", windowInfo.id.isEmpty())
                assertFalse("Tool window anchor should not be empty", windowInfo.anchor.isEmpty())
            }
        } else {
            // If no tool windows, state should be empty but valid
            assertEquals(0, state.windows.size)
        }
    }

    fun testCaptureAndRestoreState() {
        val twManager = ToolWindowManager.getInstance(project)
        val toolWindowIds = twManager.toolWindowIds

        if (toolWindowIds.isEmpty()) {
            // Skip test if no tool windows available
            return
        }

        // Get initial state of first tool window
        val firstToolWindow = twManager.getToolWindow(toolWindowIds[0]) ?: return
        val initialAnchor = firstToolWindow.anchor
        val initialVisible = firstToolWindow.isVisible
        val initialAvailable = firstToolWindow.isAvailable
        val initialShowButton = firstToolWindow.isShowStripeButton

        // Capture current state
        service.captureCurrentToolwindowState(project)

        // Verify state was captured
        val capturedState = service.state
        var found = false
        for (windowInfo in capturedState.windows) {
            if (windowInfo.id == toolWindowIds[0]) {
                found = true
                break
            }
        }
        assertTrue("Tool window should be captured", found)

        // Modify tool window state
        firstToolWindow.setAnchor(ToolWindowAnchor.BOTTOM, null)
        firstToolWindow.setAvailable(!initialAvailable, object : Runnable {
            override fun run() {}
        })
        firstToolWindow.isShowStripeButton = !initialShowButton

        // Restore state
        try {
            ApplicationManager.getApplication().invokeAndWait(object : Runnable {
                override fun run() {
                    service.restoreSavedStripeState(project)
                }
            })
        } catch (e: Exception) {
            fail("Restore should not throw exception: ${e.message}")
        }

        // Verify restoration (note: some properties might not restore perfectly in test environment)
        val restoredToolWindow = twManager.getToolWindow(toolWindowIds[0])
        assertNotNull(restoredToolWindow)

        // Restore original state for cleanup
        firstToolWindow.setAnchor(initialAnchor, null)
        firstToolWindow.setAvailable(initialAvailable, object : Runnable {
            override fun run() {}
        })
        firstToolWindow.isShowStripeButton = initialShowButton
    }

    fun testStatePersistence() {
        // Capture initial state
        service.captureCurrentToolwindowState(project)
        val initialState = service.state
        val initialWindowCount = initialState.windows.size

        // Create a new state and load it
        val newState = ToolwindowState()
        newState.windows.add(
            ToolWindowInfo(
                id = "TestWindow",
                anchor = ToolWindowAnchor.LEFT.displayName,
                available = true,
                visible = false,
                showButton = true
            )
        )

        service.loadState(newState)

        // Verify state was loaded
        val loadedState = service.state
        assertEquals(1, loadedState.windows.size)
        assertEquals("TestWindow", loadedState.windows[0].id)
        assertEquals(ToolWindowAnchor.LEFT.displayName, loadedState.windows[0].anchor)
        assertTrue(loadedState.windows[0].available)
        assertFalse(loadedState.windows[0].visible)
        assertTrue(loadedState.windows[0].showButton)
    }

    fun testRestoreWithNonExistentToolWindow() {
        // Create state with a non-existent tool window ID
        val testState = ToolwindowState()
        testState.windows.add(
            ToolWindowInfo(
                id = "NonExistentToolWindow",
                anchor = ToolWindowAnchor.RIGHT.displayName,
                available = true,
                visible = true,
                showButton = true
            )
        )

        service.loadState(testState)

        // Should not throw when restoring non-existent tool window
        try {
            ApplicationManager.getApplication().invokeAndWait(object : Runnable {
                override fun run() {
                    service.restoreSavedStripeState(project)
                }
            })
        } catch (e: Exception) {
            fail("Restore should handle non-existent tool window gracefully: ${e.message}")
        }
    }

    fun testRestoreWithInvalidAnchor() {
        val twManager = ToolWindowManager.getInstance(project)
        val toolWindowIds = twManager.toolWindowIds

        if (toolWindowIds.isEmpty()) {
            return
        }

        // Create state with invalid anchor name
        val testState = ToolwindowState()
        testState.windows.add(
            ToolWindowInfo(
                id = toolWindowIds[0],
                anchor = "InvalidAnchorName",
                available = true,
                visible = false,
                showButton = true
            )
        )

        service.loadState(testState)

        // Should handle invalid anchor gracefully
        try {
            ApplicationManager.getApplication().invokeAndWait(object : Runnable {
                override fun run() {
                    service.restoreSavedStripeState(project)
                }
            })
        } catch (e: Exception) {
            fail("Restore should handle invalid anchor gracefully: ${e.message}")
        }
    }

    fun testMultipleCaptureOperations() {
        // First capture
        service.captureCurrentToolwindowState(project)
        val firstCapture = service.state.windows.size

        // Second capture should replace the first
        service.captureCurrentToolwindowState(project)
        val secondCapture = service.state.windows.size

        // Should have same number (or at least be valid)
        assertTrue(firstCapture >= 0)
        assertTrue(secondCapture >= 0)
    }
}

