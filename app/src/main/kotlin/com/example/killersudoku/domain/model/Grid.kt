package com.example.killersudoku.domain.model

typealias Grid = List<List<Int>>

data class GridPosition(
    val row: Int,
    val col: Int,
)

fun emptyGrid(): Grid = List(9) { List(9) { 0 } }

fun Grid.valueAt(position: GridPosition): Int = this[position.row][position.col]

fun Grid.withValue(position: GridPosition, value: Int): Grid =
    mapIndexed { rowIndex, row ->
        if (rowIndex == position.row) {
            row.mapIndexed { colIndex, current ->
                if (colIndex == position.col) value else current
            }
        } else {
            row
        }
    }

fun Grid.isFilled(): Boolean = all { row -> row.all { it in 1..9 } }

fun Grid.positions(): List<GridPosition> =
    flatMapIndexed { rowIndex, row ->
        row.indices.map { colIndex -> GridPosition(rowIndex, colIndex) }
    }
