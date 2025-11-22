package com.github.khopland.windowmanagmentplugin.toolWindow

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class StripeActionsTest : BasePlatformTestCase() {

    fun testSaveToolwindowsStateActionExists() {
        val actionManager = ActionManager.getInstance()
        val action = actionManager.getAction("WindowManagement.saveToolwindow")

        assertNotNull("SaveToolwindowsState action should be registered", action)
        assertTrue("Action should be instance of SaveToolwindowsState", action is SaveToolwindowsState)
    }

    fun testRestoreStripeActionExists() {
        val actionManager = ActionManager.getInstance()
        val action = actionManager.getAction("WindowManagement.RestoreStripe")

        assertNotNull("RestoreStripeAction should be registered", action)
        assertTrue("Action should be instance of RestoreStripeAction", action is RestoreStripeAction)
    }

    fun testSaveToolwindowsStateActionPerformed() {
        val action = SaveToolwindowsState()
        val service = ToolwindowConfigService.getInstance()

        // Clear state first
        service.loadState(ToolwindowState())

        // Create action event
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .build()
        val presentation = Presentation()
        val actionEvent = AnActionEvent.createEvent(
            action,
            dataContext,
            presentation,
            "",
            ActionUiKind.TOOLBAR,
            null
        )

        // Should not throw
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should not throw exception: ${e.message}")
        }

        // Verify state was captured
        val state = service.state
        assertNotNull(state)
    }

    fun testSaveToolwindowsStateActionWithNullProject() {
        val action = SaveToolwindowsState()

        // Create action event without project (empty data context)
        val dataContext = SimpleDataContext.builder().build()
        val presentation = Presentation()
        val actionEvent = AnActionEvent.createEvent(
            action,
            dataContext,
            presentation,
            "",
            ActionUiKind.TOOLBAR,
            null
        )

        // Should handle null project gracefully (action checks e.project ?: return)
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle null project gracefully: ${e.message}")
        }
    }

    fun testRestoreStripeActionPerformed() {
        val action = RestoreStripeAction()
        val service = ToolwindowConfigService.getInstance()

        // First capture some state
        service.captureCurrentToolwindowState(project)

        // Create action event
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .build()
        val presentation = Presentation()
        val actionEvent = AnActionEvent.createEvent(
            action,
            dataContext,
            presentation,
            "",
            ActionUiKind.TOOLBAR,
            null
        )

        // Should not throw
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should not throw exception: ${e.message}")
        }
    }

    fun testRestoreStripeActionWithNullProject() {
        val action = RestoreStripeAction()

        // Create action event without project (empty data context)
        val dataContext = SimpleDataContext.builder().build()
        val presentation = Presentation()
        val actionEvent = AnActionEvent.createEvent(
            action,
            dataContext,
            presentation,
            "",
            ActionUiKind.TOOLBAR,
            null
        )

        // Should handle null project gracefully (action checks e.project ?: return)
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle null project gracefully: ${e.message}")
        }
    }

    fun testRestoreStripeActionWithEmptyState() {
        val action = RestoreStripeAction()
        val service = ToolwindowConfigService.getInstance()

        // Clear state
        service.loadState(ToolwindowState())

        // Create action event
        val dataContext = SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .build()
        val presentation = Presentation()
        val actionEvent = AnActionEvent.createEvent(
            action,
            dataContext,
            presentation,
            "",
            ActionUiKind.TOOLBAR,
            null
        )

        // Should handle empty state gracefully
        try {
            action.actionPerformed(actionEvent)
        } catch (e: Exception) {
            fail("Action should handle empty state gracefully: ${e.message}")
        }
    }

    fun testActionText() {
        val saveAction = SaveToolwindowsState()
        val restoreAction = RestoreStripeAction()

        assertEquals("Save Toolwindows", saveAction.templateText)
        assertEquals("Restore Toolwindows", restoreAction.templateText)
    }
}

