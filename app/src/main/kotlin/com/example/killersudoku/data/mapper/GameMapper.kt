package com.example.killersudoku.data.mapper

import com.example.killersudoku.data.local.entity.GameEntity
import com.example.killersudoku.domain.model.Cage
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.Grid
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.Puzzle

fun Game.toEntity(): GameEntity =
    GameEntity(
        id = id,
        puzzleId = puzzle.id,
        difficulty = puzzle.difficulty.name,
        initialGrid = puzzle.initialGrid.encodeGrid(),
        solutionGrid = puzzle.solutionGrid.encodeGrid(),
        currentGrid = currentGrid.encodeGrid(),
        cages = puzzle.cages.encodeCages(),
        notes = notes.encodeNotes(),
        startedAt = startedAt,
        lastModified = lastModified,
        isCompleted = isCompleted,
        completedAt = completedAt,
    )

fun GameEntity.toDomain(): Game {
    val puzzle = Puzzle(
        id = puzzleId,
        difficulty = Difficulty.valueOf(difficulty),
        initialGrid = initialGrid.decodeGrid(),
        solutionGrid = solutionGrid.decodeGrid(),
        cages = cages.decodeCages(),
        createdAt = startedAt,
    )
    return Game(
        id = id,
        puzzle = puzzle,
        currentGrid = currentGrid.decodeGrid(),
        notes = notes.decodeNotes(),
        startedAt = startedAt,
        lastModified = lastModified,
        isCompleted = isCompleted,
        completedAt = completedAt,
    )
}

fun Grid.encodeGrid(): String =
    flatten().joinToString(separator = "") { it.toString() }

fun String.decodeGrid(): Grid =
    chunked(9).map { row -> row.map { it.digitToInt() } }

private fun List<Cage>.encodeCages(): String =
    joinToString(separator = ";") { cage ->
        val cells = cage.cells.joinToString(separator = ",") { "${it.row}-${it.col}" }
        "${cage.id}:${cage.targetSum}:$cells"
    }

private fun String.decodeCages(): List<Cage> {
    if (isBlank()) return emptyList()
    return split(";").map { encoded ->
        val parts = encoded.split(":")
        Cage(
            id = parts[0].toInt(),
            targetSum = parts[1].toInt(),
            cells = parts[2].split(",").map { cell ->
                val (row, col) = cell.split("-")
                GridPosition(row.toInt(), col.toInt())
            },
        )
    }
}

private fun Map<GridPosition, Set<Int>>.encodeNotes(): String =
    entries
        .filter { it.value.isNotEmpty() }
        .sortedWith(compareBy<Map.Entry<GridPosition, Set<Int>>> { it.key.row }.thenBy { it.key.col })
        .joinToString(separator = ";") { (position, values) ->
            val notes = values.sorted().joinToString(separator = "")
            "${position.row}-${position.col}:$notes"
        }

private fun String.decodeNotes(): Map<GridPosition, Set<Int>> {
    if (isBlank()) return emptyMap()
    return split(";").associate { encoded ->
        val (cell, values) = encoded.split(":")
        val (row, col) = cell.split("-")
        GridPosition(row.toInt(), col.toInt()) to values.map { it.digitToInt() }.toSet()
    }
}
