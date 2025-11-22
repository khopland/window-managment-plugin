package com.github.khopland.windowmanagmentplugin.toolWindow

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager


class SaveToolwindowsState : AnAction("Save Toolwindow Position") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ToolwindowConfigService.getInstance().captureCurrentToolwindowState(project)
    }
}

class RestoreToolwindowAction : AnAction("Restore Toolwindow Position") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ApplicationManager.getApplication().invokeLater {
            ToolwindowConfigService.getInstance().restoreSavedStripeState(project)
        }
    }
}

