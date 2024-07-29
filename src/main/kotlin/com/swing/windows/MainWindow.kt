package com.swing.windows

import com.swing.panes.MainContentPane
import com.swing.panes.PathManagerPane
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JTabbedPane

object MainWindow : JFrame() {
    private val mainContentPane = MainContentPane()
    private val pathManagerPane = PathManagerPane()
    init {
        val tabbedPane = JTabbedPane().apply {
            addTab("MQTT", mainContentPane)
            setMnemonicAt(0, KeyEvent.VK_1)
            addTab("Path Manager", pathManagerPane)
            setMnemonicAt(1, KeyEvent.VK_2)
        }

        setIconImage()
        title = "Control Interface"
        defaultCloseOperation = EXIT_ON_CLOSE
        contentPane.add(tabbedPane)
        pack() // установка размеров фрейма
        setLocationRelativeTo(null)  // center the window
        isResizable = false
        isVisible = true

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                mainContentPane.imageSettingsPane.paramsManager.saveParams()
                mainContentPane.motionPane.paramsManager.saveParams()
                mainContentPane.steeringPidPane.paramsManager.saveParams()
                mainContentPane.throttlePidPane.paramsManager.saveParams()
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