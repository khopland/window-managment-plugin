package com.github.khopland.windowmanagmentplugin.toolWindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager

data class ToolWindowInfo(
    var id: String = "",
    var anchor: String = "",       // ToolWindowAnchor.name()
    var available: Boolean = false,
    var visible: Boolean = false,
    var showButton: Boolean = false,
)

data class ToolwindowState(
    var windows: MutableList<ToolWindowInfo> = mutableListOf()
)


@Service(Service.Level.APP)
@State(name = "ToolwindowConfig", storages = [Storage("toolwindow-config.xml")])
class ToolwindowConfigService : PersistentStateComponent<ToolwindowState> {

    private var internalState = ToolwindowState()

    override fun getState(): ToolwindowState = internalState

    override fun loadState(state: ToolwindowState) {
        this.internalState = state
    }

    companion object {
        fun getInstance(): ToolwindowConfigService = ApplicationManager.getApplication().service()
    }

    fun captureCurrentToolwindowState(project: Project) {
        val twManager = ToolWindowManager.getInstance(project)
        val ids = twManager.toolWindowIds // array of IDs
        val list = mutableListOf<ToolWindowInfo>()
        for (id in ids) {
            val tw = twManager.getToolWindow(id) ?: continue
            val anchor = tw.anchor // ToolWindowAnchor
            list.add(
                ToolWindowInfo(
                    id = id,
                    anchor = anchor.displayName,
                    available = tw.isAvailable,
                    visible = tw.isVisible,
                    showButton = tw.isShowStripeButton,
                )
            )

        }
        internalState.windows = list
    }

    fun restoreSavedStripeState(project: Project) {
        val twManager = ToolWindowManager.getInstance(project)
        for (info in internalState.windows) {
            val tw = twManager.getToolWindow(info.id) ?: continue
            try {
                val desiredAnchor = ToolWindowAnchor.fromText(info.anchor)
                tw.setAnchor(desiredAnchor) {}
            } catch (e: IllegalArgumentException) {
                thisLogger().warn("Failed to restore anchor for tool window ${info.id}: ${e.message}")
            } catch (e: Exception) {
                thisLogger().warn("Failed to set anchor for tool window ${info.id}: ${e.message}")
            }

            tw.setAvailable(info.available) {}
            if (info.visible) {
                tw.show {}
            } else {
                tw.hide {}
            }
            tw.isShowStripeButton = info.showButton
        }
    }
}