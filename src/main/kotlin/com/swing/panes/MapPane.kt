package com.swing.panes

import org.json.JSONArray
import org.json.JSONObject
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import javax.swing.*

class MapPane: JPanel() {
    private var selectedPointIndex: Int = -1

    val pointTableModel = PointTableModel()

    init {
        background = Color.WHITE
        preferredSize = Dimension(600, 600)

        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                selectedPointIndex = findPointIndexNear(e.point)
                if (SwingUtilities.isRightMouseButton(e) && selectedPointIndex != -1) {
                    showEditMenu(e)
                }
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (selectedPointIndex != -1) {
                    pointTableModel.changePoint(selectedPointIndex, e.point)
                    repaint()
                }
            }
        })
    }

    fun loadPointsFromJson(file: File) {
        pointTableModel.points.clear()
        val json = FileReader(file).use { reader ->
            val jsonText = reader.readText()
            JSONArray(jsonText)
        }
        for (i in 0 until json.length()) {
            val arr = json.getJSONArray(i)
            val x = arr.getDouble(0)
            val y = arr.getDouble(1)
            pointTableModel.addPoint(Point(x.toInt(), y.toInt()))
        }
        repaint()
    }

    fun loadPointsFromCsv(file: File) {
        pointTableModel.points.clear()
        val csv = FileReader(file).use { reader ->
            reader.readLines()
        }
        for (line in csv) {
            val parts = line.split(",")
            val x = parts[0].toInt()
            val y = parts[1].toInt()
            pointTableModel.addPoint(Point(x, y))
        }
        repaint()
    }

    fun savePointsToJson(file: File) {
        val jsonArray = JSONArray()
        for (point in pointTableModel.points) {
            val jsonObject = JSONObject()
            jsonObject.put("x", point.x)
            jsonObject.put("y", point.y)
            jsonArray.put(jsonObject)
        }
        FileWriter(file).use { writer ->
            writer.write(jsonArray.toString(4))
        }
        JOptionPane.showMessageDialog(this, "Points saved to ${file.absolutePath}")
    }

    fun savePointsToCsv(file: File) {
        FileWriter(file).use { writer ->
            for (point in pointTableModel.points) {
                writer.write("${point.x},${point.y}\n")
            }
        }
        JOptionPane.showMessageDialog(this, "Points saved to ${file.absolutePath}")
    }

    fun addPoint(index: Int, point: Point) {
        pointTableModel.addPoint(index, point)
        repaint()
    }

    fun removePoint(index: Int) {
        if (index in pointTableModel.points.indices) {
            pointTableModel.removePoint(index)
            repaint()
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        for (point in pointTableModel.points) {
            g2.fillOval(point.x - 5, point.y - 5, 10, 10)
        }
    }


    private fun findPointNear(location: Point): Point? {
        val tolerance = 10
        return pointTableModel.points.find { it.distance(location.x.toDouble(), location.y.toDouble()) < tolerance }
    }

    private fun findPointIndexNear(location: Point): Int {
        val tolerance = 10
        return pointTableModel.points.indexOfFirst { it.distance(location.x.toDouble(), location.y.toDouble()) < tolerance }
    }

    private fun showEditMenu(e: MouseEvent) {
        val menu = JPopupMenu()
        val editItem = JMenuItem("Edit Coordinates")
        editItem.addActionListener {
            if (selectedPointIndex != -1) {
                val xInput = JOptionPane.showInputDialog(this, "New X Coordinate:", pointTableModel.points[selectedPointIndex].x)
                val yInput = JOptionPane.showInputDialog(this, "New Y Coordinate:", pointTableModel.points[selectedPointIndex].y)
                try {
                    val newX = xInput.toInt()
                    val newY = yInput.toInt()
                    pointTableModel.changePoint(selectedPointIndex, Point(newX, newY))
                    repaint()
                } catch (ex: NumberFormatException) {
                    JOptionPane.showMessageDialog(this, "Invalid input. Please enter integer values.")
                }
            }
        }
        menu.add(editItem)
        menu.show(this, e.x, e.y)
    }
}