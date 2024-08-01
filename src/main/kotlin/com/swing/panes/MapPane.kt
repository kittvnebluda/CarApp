package com.swing.panes

import org.json.JSONArray
import org.json.JSONObject
import java.awt.*
import java.awt.event.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import javax.swing.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MapPane: JPanel() {
    private var selectedPointIndex: Int = -1
    private val panelWidth = 600
    private val panelHeight = 600
    private var translation = Point(panelWidth / 2, panelHeight / 2)
    private var rotation = -PI / 2
    private var lastMousePosition: Point? = null

    val pointTableModel = PointTableModel()

    init {
        background = Color.WHITE
        preferredSize = Dimension(panelWidth, panelHeight)
        bindKeyActions()

        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                selectedPointIndex = findPointIndexNear(e.point)
                if (SwingUtilities.isRightMouseButton(e) && selectedPointIndex != -1) {
                    showEditMenu(e)
                }
                lastMousePosition = e.point
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                if (SwingUtilities.isLeftMouseButton(e) && selectedPointIndex != -1) {
                    pointTableModel.changePoint(selectedPointIndex, fromMapFrame(e.point))
                    repaint()
                } else if (SwingUtilities.isMiddleMouseButton(e)) {
                    val deltaPos = e.point - lastMousePosition!!
                    translation += deltaPos
                    lastMousePosition = e.point
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

        g2.translate(translation.x, translation.y)
        g2.rotate(rotation)

        for (point in pointTableModel.points) {
            g2.fillOval(point.x - 5, point.y - 5, 10, 10)
        }
    }

    private fun fromMapFrame(point: Point): Point {
        point.translate(-translation.x, -translation.y)
        val cosTheta = cos(rotation)
        val sinTheta = -sin(rotation)
        val newX = point.x * cosTheta - point.y * sinTheta
        val newY = point.x * sinTheta + point.y * cosTheta
        return Point(newX.toInt(), newY.toInt())
    }

    private fun findPointIndexNear(location: Point): Int {
        val tolerance = 10
        val locReal = fromMapFrame(location)
        return pointTableModel.points.indexOfFirst {
            it.distance(locReal.x.toDouble(), locReal.y.toDouble()) < tolerance
        }
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

    private fun bindKeyActions() {
        val inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        val actionMap = actionMap

        // Bind Q key
        inputMap.put(KeyStroke.getKeyStroke('Q', 0), "rotateCounterClockwise")
        actionMap.put("rotateCounterClockwise", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                rotation -= 0.01
                repaint()
            }
        })

        // Bind E key
        inputMap.put(KeyStroke.getKeyStroke('E', 0), "rotateClockwise")
        actionMap.put("rotateClockwise", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                rotation += 0.01
                repaint()
            }
        })

        // Bind W key
        inputMap.put(KeyStroke.getKeyStroke('W', 0), "panUp")
        actionMap.put("panUp", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                translation.y -= 1
                repaint()
            }
        })

        // Bind S key
        inputMap.put(KeyStroke.getKeyStroke('S', 0), "panDown")
        actionMap.put("panDown", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                translation.y += 1
                repaint()
            }
        })

        // Bind A key
        inputMap.put(KeyStroke.getKeyStroke('A', 0), "panLeft")
        actionMap.put("panLeft", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                translation.x -= 1
                repaint()
            }
        })

        // Bind D key
        inputMap.put(KeyStroke.getKeyStroke('D', 0), "panRight")
        actionMap.put("panRight", object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                translation.x += 1
                repaint()
            }
        })

        // Add alternative bindings for lowercase
        inputMap.put(KeyStroke.getKeyStroke('q', 0), "rotateCounterClockwise")
        inputMap.put(KeyStroke.getKeyStroke('e', 0), "rotateClockwise")
        inputMap.put(KeyStroke.getKeyStroke('w', 0), "panUp")
        inputMap.put(KeyStroke.getKeyStroke('s', 0), "panDown")
        inputMap.put(KeyStroke.getKeyStroke('a', 0), "panLeft")
        inputMap.put(KeyStroke.getKeyStroke('d', 0), "panRight")
    }
}

private operator fun Point.plusAssign(point: Point) {
    x += point.x
    y += point.y
}

private operator fun Point.minus(translation: Point): Point {
    return Point(x - translation.x, y - translation.y)
}

private operator fun Int.times(point: Point): Point {
    return Point(this * point.x, this * point.y)
}
