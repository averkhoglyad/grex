package net.averkhoglyad.grex.arrow.gui

import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import net.averkhoglyad.grex.arrow.gui.layout.MainLayout
import org.apache.logging.log4j.jul.Log4jBridgeHandler
import tornadofx.App
import tornadofx.FX
import tornadofx.addStageIcon
import java.awt.SplashScreen

class ArrowApp : App(MainLayout::class) {

    init {
        Log4jBridgeHandler.install(true, ".", true)

        FX.layoutDebuggerShortcut = KeyCodeCombination(KeyCode.F12)

        addStageIcon(Image(resources.stream("/img/arrow.png")))

//        GlyphFontRegistry.register(FontAwesome(resources.stream("/org/controlsfx/glyphfont/fontawesome-webfont.ttf")))
//        FX.dicontainer = createDIContainer()

        SplashScreen.getSplashScreen()?.close()
    }

}