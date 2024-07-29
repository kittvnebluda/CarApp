package com.swing.panes

import java.awt.Point
import javax.swing.table.AbstractTableModel

class PointTableModel : AbstractTableModel() {
    private val columnNames = arrayOf("Index", "X", "Y")

    val points = mutableListOf<Point>()

    override fun getRowCount(): Int {
        return points.size
    }

    override fun getColumnCount(): Int {
        return columnNames.size
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val point = points[rowIndex]
        return when (columnIndex) {
            0 -> rowIndex
            1 -> point.x
            2 -> point.y
            else -> ""
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return columnIndex > 0 // Only allow editing of X and Y columns
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        if (rowIndex in points.indices) {
            val point = points[rowIndex]
            when (columnIndex) {
                1 -> point.x = aValue.toString().toIntOrNull() ?: point.x
                2 -> point.y = aValue.toString().toIntOrNull() ?: point.y
            }
            fireTableCellUpdated(rowIndex, columnIndex)
        }
    }

    override fun getColumnName(columnIndex: Int): String {
        return columnNames[columnIndex]
    }

    fun addPoint(point: Point) {
        points.add(point)
        fireTableRowsInserted(points.size - 1, points.size - 1)
    }

    fun addPoint(index: Int, point: Point) {
        points.add(index, point)
        fireTableRowsInserted(points.size - 1, points.size - 1)
    }

    fun removePoint(index: Int) {
        if (index in points.indices) {
            points.removeAt(index)
            fireTableRowsDeleted(index, index)
        }
    }

    fun changePoint(index: Int, point: Point) {
        if (index in points.indices) {
            points[index] = point
            fireTableRowsUpdated(index, index)
        }
    }
}