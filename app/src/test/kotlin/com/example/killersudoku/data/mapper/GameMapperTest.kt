package com.example.killersudoku.data.mapper

import com.example.killersudoku.domain.model.Cage
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.Puzzle
import com.example.killersudoku.domain.model.emptyGrid
import org.junit.Assert.assertEquals
import org.junit.Test

class GameMapperTest {
    @Test
    fun preservesNotesWhenMappingThroughEntity() {
        val position = GridPosition(2, 3)
        val game = Game(
            id = 7L,
            puzzle = Puzzle(
                id = "test",
                difficulty = Difficulty.EASY,
                initialGrid = emptyGrid(),
                solutionGrid = emptyGrid(),
                cages = listOf(Cage(id = 1, cells = listOf(position), targetSum = 4)),
                createdAt = 1L,
            ),
            currentGrid = emptyGrid(),
            notes = mapOf(position to setOf(1, 4, 7)),
            startedAt = 1L,
            lastModified = 2L,
        )

        val restored = game.toEntity().toDomain()

        assertEquals(game.notes, restored.notes)
    }
}
