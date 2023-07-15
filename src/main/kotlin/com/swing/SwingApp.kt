package com.swing

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.*

val mqtt = Mqtt("tcp://localhost:1883", "car-interface")
val cfgDirPath: Path = Paths.get(System.getProperty("user.dir"), "config")

fun main() {
    SwingUtilities.invokeLater { SwingApp }
}

object SwingApp : JFrame() {
    init {
        // create save directory if not exists
        Files.createDirectories(cfgDirPath)

        title = "Car control interface"
        defaultCloseOperation = EXIT_ON_CLOSE
//        preferredSize = Dimension(320, 240)
        contentPane = ContentPane()
        pack() // установка размеров фрейма
        isResizable = false
        isVisible = true

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                (contentPane as ContentPane).imageSettingsPane.saveParams()
                (contentPane as ContentPane).carMotionPane.saveParams()
                (contentPane as ContentPane).pidPane.saveParams()
                super.windowClosing(e)
            }
        })
    }
}