package com.example.killersudoku.domain.model

data class Hint(
    val position: GridPosition,
    val answer: Int?,
    val candidates: Set<Int>,
)
