package com.swing.panes

import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class PathManagerPane : JPanel() {
    private val mapPanel: MapPane
    private val fileChooser: JFileChooser
    private val fileWorker: JPanel
    private val pointTable: JTable

    init {
        layout = BorderLayout()

        // Initialize components
        mapPanel = MapPane()
        fileChooser = JFileChooser().apply {
            fileFilter = FileNameExtensionFilter("Load path file", "json", "csv")
        }

        // Initialize table for point management
        pointTable = JTable(mapPanel.pointTableModel).apply {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            preferredScrollableViewportSize = Dimension(100, 500)
        }

        fileWorker = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.CENTER_ALIGNMENT

            val buttonSize = Dimension(200, 40)

            val loadButton = JButton("Load points").apply {
                addActionListener { loadFile() }
                maximumSize = buttonSize
                alignmentX = Component.CENTER_ALIGNMENT
            }
            val saveButton = JButton("Save points").apply {
                addActionListener { savePoints() }
                maximumSize = buttonSize
                alignmentX = Component.CENTER_ALIGNMENT
            }
            val addButton = JButton("Add Point").apply {
                addActionListener { addPoint() }
                maximumSize = buttonSize
                alignmentX = Component.CENTER_ALIGNMENT
            }
            val deleteButton = JButton("Delete Point").apply {
                addActionListener { deletePoint() }
                maximumSize = buttonSize
                alignmentX = Component.CENTER_ALIGNMENT
            }

            val buttonsPane = JPanel().apply {
                layout = GridLayout(2, 2)
                add(loadButton)
                add(saveButton)
                add(addButton)
                add(deleteButton)
            }
            add(buttonsPane)
            add(JScrollPane(pointTable))
        }

        add(mapPanel, BorderLayout.CENTER)
        add(fileWorker, BorderLayout.EAST)
    }

    private fun loadFile() {
        val returnVal = fileChooser.showOpenDialog(this)
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            when (file.extension) {
                "json" -> mapPanel.loadPointsFromJson(file)
                "csv" -> mapPanel.loadPointsFromCsv(file)
                else -> JOptionPane.showMessageDialog(this, "Invalid file type")
            }
        }
    }

    private fun savePoints() {
        val fileName = JOptionPane.showInputDialog(
            this,
            "Enter the name of the new JSON or CSV file:",
            "Save Points",
            JOptionPane.PLAIN_MESSAGE)
        if (fileName != null && fileName.isNotBlank()) {
            if (fileName.endsWith("json")) {
                mapPanel.savePointsToJson(File(fileName))
            } else if (fileName.endsWith("csv")) {
                mapPanel.savePointsToCsv(File(fileName))
            } else {
                JOptionPane.showMessageDialog(this, "Invalid file type")
            }
        }
    }

    private fun addPoint() {
        val indexInput = JOptionPane.showInputDialog(this, "Enter Point Index:")
        val xInput = JOptionPane.showInputDialog(this, "Enter X Coordinate:")
        val yInput = JOptionPane.showInputDialog(this, "Enter Y Coordinate:")
        try {
            val index = indexInput.toInt()
            val x = xInput.toInt()
            val y = yInput.toInt()
            val newPoint = Point(x, y)
            mapPanel.addPoint(index, newPoint)
        } catch (ex: NumberFormatException) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter integer values.")
        }
    }

    private fun deletePoint() {
        val selectedRow = pointTable.selectedRow
        if (selectedRow != -1) {
            mapPanel.removePoint(selectedRow)
        } else {
            JOptionPane.showMessageDialog(this, "No point selected.")
        }
    }
}