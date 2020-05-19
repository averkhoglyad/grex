package io.averkhoglyad.grex.roo.gui

import io.averkhoglyad.grex.roo.gui.layout.MainLayout
import org.slf4j.bridge.SLF4JBridgeHandler
import tornadofx.*
import java.awt.SplashScreen

class RooApp : App(MainLayout::class) {

    init {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

//        addStageIcon(Image(resources.stream("/images/icon.png")))
//        GlyphFontRegistry.register(FontAwesome(resources.stream("/org/controlsfx/glyphfont/fontawesome-webfont.ttf")))
//        FX.dicontainer = createDIContainer()

        SplashScreen.getSplashScreen()?.close()
    }

}