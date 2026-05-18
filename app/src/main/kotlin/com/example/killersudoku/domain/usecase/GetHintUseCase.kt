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
            ?.takeIf { it.isHintable(game) }
            ?: game.currentGrid.positions()
                .filter { it.isHintable(game) }
                .minWithOrNull(
                    compareBy<GridPosition> { candidatesFor(game, it).size }
                        .thenBy { it.row }
                        .thenBy { it.col },
                )
            ?: return null

        val answer = game.puzzle.solutionGrid.valueAt(position)
        return Hint(
            position = position,
            answer = answer,
            candidates = candidatesFor(game, position),
        )
    }

    private fun GridPosition.isHintable(game: Game): Boolean =
        !game.puzzle.isGiven(this) &&
            game.currentGrid.valueAt(this) != game.puzzle.solutionGrid.valueAt(this)

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
