package com.example.killersudoku.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.killersudoku.domain.model.Cage
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.valueAt

@Composable
fun GameGrid(
    game: Game,
    selectedCell: GridPosition?,
    notes: Map<GridPosition, Set<Int>>,
    mistakes: Set<GridPosition>,
    onCellClick: (GridPosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridLineColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.onSurface),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            repeat(9) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    repeat(9) { col ->
                        val position = GridPosition(row, col)
                        val cage = game.puzzle.cageFor(position)
                        SudokuCell(
                            value = game.currentGrid.valueAt(position),
                            notes = notes[position].orEmpty(),
                            cage = cage,
                            isCageLabel = cage?.isTopLeft(position) == true,
                            isGiven = game.puzzle.isGiven(position),
                            isSelected = selectedCell == position,
                            isRelated = selectedCell?.let {
                                it.row == row ||
                                    it.col == col ||
                                    game.puzzle.cageFor(it)?.id == cage?.id
                            } == true,
                            isError = position in mistakes,
                            onClick = { onCellClick(position) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / 9f
            val cellHeight = size.height / 9f
            for (line in 1..8) {
                val stroke = if (line % 3 == 0) 3.5f else 1.1f
                drawLine(
                    color = gridLineColor,
                    start = Offset(cellWidth * line, 0f),
                    end = Offset(cellWidth * line, size.height),
                    strokeWidth = stroke,
                )
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, cellHeight * line),
                    end = Offset(size.width, cellHeight * line),
                    strokeWidth = stroke,
                )
            }
        }
    }
}

@Composable
private fun SudokuCell(
    value: Int,
    notes: Set<Int>,
    cage: Cage?,
    isCageLabel: Boolean,
    isGiven: Boolean,
    isSelected: Boolean,
    isRelated: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        isError -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.20f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        isRelated -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else -> cageColor(cage?.id ?: 0)
    }

    Box(
        modifier = modifier
            .background(background)
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.18f),
            )
            .clickable(onClick = onClick)
            .padding(2.dp),
    ) {
        if (isCageLabel && cage != null) {
            Text(
                text = cage.targetSum.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }

        if (value != 0) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isGiven) FontWeight.Bold else FontWeight.Normal,
                color = if (isGiven) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        } else if (notes.isNotEmpty()) {
            NoteGrid(
                notes = notes,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun NoteGrid(
    notes: Set<Int>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(3) { col ->
                    val number = row * 3 + col + 1
                    Text(
                        text = if (number in notes) number.toString() else "",
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private fun cageColor(id: Int): Color {
    val colors = listOf(
        Color(0xFFFFF7D7),
        Color(0xFFE9F6EA),
        Color(0xFFE8F1FA),
        Color(0xFFFFE9DF),
        Color(0xFFF0E8FA),
        Color(0xFFEAF7F6),
    )
    return colors[id % colors.size].copy(alpha = 0.72f)
}
