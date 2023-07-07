package com.swing

import java.awt.Dimension
import javax.swing.*

fun main() {
    SwingUtilities.invokeLater { SwingApp }
}

object SwingApp : JFrame() {
    init {
        title = "Car control interface"

        defaultCloseOperation = EXIT_ON_CLOSE

        preferredSize = Dimension(320, 240)

        contentPane = ContentPane()

        isResizable = false

        pack() // установка размеров фрейма

        isVisible = true
    }
}