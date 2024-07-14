package com.swing.windows

import com.swing.panes.MainContentPane
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.ImageIcon
import javax.swing.JFrame

object MainWindow : JFrame() {
    init {
        setIconImage()
        title = "Control Interface"
        defaultCloseOperation = EXIT_ON_CLOSE
        contentPane = MainContentPane()
        pack() // установка размеров фрейма
        setLocationRelativeTo(null)  // center the window
        isResizable = false
        isVisible = true

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                (contentPane as MainContentPane).imageSettingsPane.paramsManager.saveParams()
                (contentPane as MainContentPane).motionPane.paramsManager.saveParams()
                (contentPane as MainContentPane).steeringPidPane.paramsManager.saveParams()
                (contentPane as MainContentPane).throttlePidPane.paramsManager.saveParams()
                super.windowClosing(e)
            }
        })
    }

    private fun setIconImage() {
        val iconURL = this::class.java.getResource("/aqua.png")
        if (iconURL != null) {
            val icon = ImageIcon(iconURL)
            iconImage = icon.image
        } else {
            println("Icon image not found.")
        }
    }
}