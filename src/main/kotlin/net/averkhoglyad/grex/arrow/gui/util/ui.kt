package net.averkhoglyad.grex.arrow.gui.util

import javafx.event.Event
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.controlsfx.control.StatusBar
import tornadofx.*

fun Stage.consumeCloseRequest(op: (Stage) -> Unit) {
    setOnCloseRequest {
        it.consume()
        op(this@consumeCloseRequest)
    }
}

fun Stage.requestClose() {
    Event.fireEvent(this, WindowEvent(this, WindowEvent.WINDOW_CLOSE_REQUEST))
}

fun TabPane.tab(component: UIComponent, op: Tab.() -> Unit = {}) = tab(text = component.title) {
    textProperty().bind(component.titleProperty)
    graphicProperty().bind(component.iconProperty)
    this += component
    op()
}

//fun StatusBar.bindStatus(text: String, status: TaskStatus) {
//    this.text = text
//    progressProperty().bind(status.progress)
//}
//
//fun StatusBar.clearStatus() {
//    text = ""
//    progressProperty().unbind()
//    progress = 0.0
//}
