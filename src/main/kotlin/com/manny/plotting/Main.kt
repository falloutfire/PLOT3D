package com.manny.plotting

import com.manny.plotting.Controller.MainLayoutController
import com.manny.plotting.Controller.RootLayoutController
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.io.IOException

class Main: Application() {

    var primaryStage: Stage? = null
    var rootLoader: RootLayoutController? = null
    var mainLoader: MainLayoutController? = null
    private var rootPane: BorderPane? = null

    override fun start(primaryStage: Stage?) {
        this.primaryStage = primaryStage
        this.primaryStage?.sizeToScene()
        this.primaryStage?.title = "Моделирование"
        this.primaryStage?.isResizable = false
        initRootLayout()
        showMainLayout()
    }

    @Throws(IOException::class)
    private fun showMainLayout() {
        val loader = FXMLLoader()
        loader.location = Main::class.java.getResource("View/MainLayout.fxml")

        val mainPane = loader.load<AnchorPane>()
        rootPane?.center = mainPane

        mainLoader = loader.getController<MainLayoutController>()
        mainLoader?.setMainApp(this)
    }

    @Throws(IOException::class)
    private fun initRootLayout() {
        val loader = FXMLLoader()
        loader.location = Main::class.java.getResource("View/RootLayout.fxml")
        rootPane = loader.load() as BorderPane

        val scene = Scene(rootPane)
        primaryStage?.scene = scene
        primaryStage?.show()

        rootLoader = loader.getController()
        rootLoader?.setMainApp(this)
    }
}

fun main() {
    Application.launch(Main::class.java)
}