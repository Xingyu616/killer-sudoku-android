package com.example.killersudoku.domain.model

data class Cage(
    val id: Int,
    val cells: List<GridPosition>,
    val targetSum: Int,
) {
    fun contains(position: GridPosition): Boolean = position in cells

    fun isTopLeft(position: GridPosition): Boolean =
        cells.minWith(compareBy<GridPosition> { it.row }.thenBy { it.col }) == position
}
