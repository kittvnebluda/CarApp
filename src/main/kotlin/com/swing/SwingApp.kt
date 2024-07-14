package com.swing

import com.swing.net.Mqtt
import com.swing.windows.LoginWindow
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.SwingUtilities

const val clientId = "car-interface"

val cfgDirPath: Path = Paths.get(System.getProperty("user.dir"), "config")

lateinit var mqtt: Mqtt

fun main() {
    Files.createDirectories(cfgDirPath)
    SwingUtilities.invokeLater { LoginWindow }
}
