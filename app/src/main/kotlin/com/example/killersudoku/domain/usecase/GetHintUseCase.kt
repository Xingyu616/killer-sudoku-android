package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.Hint
import com.example.killersudoku.domain.model.positions
import com.example.killersudoku.domain.model.valueAt
import javax.inject.Inject

class GetHintUseCase @Inject constructor() {
    operator fun invoke(game: Game, preferredPosition: GridPosition?): Hint? {
        val position = preferredPosition
            ?.takeIf { !game.puzzle.isGiven(it) && game.currentGrid.valueAt(it) == 0 }
            ?: game.currentGrid.positions().firstOrNull {
                !game.puzzle.isGiven(it) && game.currentGrid.valueAt(it) == 0
            }
            ?: return null

        val answer = game.puzzle.solutionGrid.valueAt(position)
        return Hint(
            position = position,
            answer = answer,
            candidates = candidatesFor(game, position),
            message = "第 ${position.row + 1} 行第 ${position.col + 1} 列可以填 $answer",
        )
    }

    private fun candidatesFor(game: Game, position: GridPosition): Set<Int> {
        val used = mutableSetOf<Int>()
        used += game.currentGrid[position.row].filter { it != 0 }
        used += (0..8).map { row -> game.currentGrid[row][position.col] }.filter { it != 0 }

        val rowStart = position.row / 3 * 3
        val colStart = position.col / 3 * 3
        used += (rowStart until rowStart + 3).flatMap { row ->
            (colStart until colStart + 3).map { col -> game.currentGrid[row][col] }
        }.filter { it != 0 }

        val cage = game.puzzle.cageFor(position)
        if (cage != null) {
            used += cage.cells.map { game.currentGrid.valueAt(it) }.filter { it != 0 }
        }

        return (1..9).filterNot { it in used }.toSet()
    }
}
