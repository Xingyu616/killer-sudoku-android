package com.example.killersudoku.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.valueAt

@Composable
fun GameGrid(
    game: Game,
    selectedCell: GridPosition?,
    selectedCells: Set<GridPosition>,
    notes: Map<GridPosition, Set<Int>>,
    mistakes: Set<GridPosition>,
    onCellClick: (GridPosition) -> Unit,
    onCellsSelected: (Set<GridPosition>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridLineColor = MaterialTheme.colorScheme.onSurface
    val dragSelection = remember { mutableStateOf(emptySet<GridPosition>()) }
    val visibleSelection = selectedCells + dragSelection.value
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color.White)
            .border(2.dp, MaterialTheme.colorScheme.onSurface)
            .pointerInput(Unit) {
                fun Offset.toGridPosition(): GridPosition? {
                    if (x !in 0f..size.width.toFloat() || y !in 0f..size.height.toFloat()) return null
                    val col = (x / (size.width / 9f)).toInt().coerceIn(0, 8)
                    val row = (y / (size.height / 9f)).toInt().coerceIn(0, 8)
                    return GridPosition(row, col)
                }

                detectDragGestures(
                    onDragStart = { offset ->
                        dragSelection.value = offset.toGridPosition()?.let { setOf(it) }.orEmpty()
                    },
                    onDragEnd = {
                        if (dragSelection.value.isNotEmpty()) {
                            onCellsSelected(dragSelection.value)
                        }
                        dragSelection.value = emptySet()
                    },
                    onDragCancel = {
                        dragSelection.value = emptySet()
                    },
                    onDrag = { change, _ ->
                        change.position.toGridPosition()?.let { position ->
                            dragSelection.value = dragSelection.value + position
                        }
                    },
                )
            },
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
                            isGiven = game.puzzle.isGiven(position),
                            isSelected = position in visibleSelection || selectedCell == position,
                            isRelated = selectedCell?.let {
                                position !in visibleSelection &&
                                    (
                                        it.row == row ||
                                            it.col == col ||
                                            game.puzzle.cageFor(it)?.id == cage?.id
                                        )
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
                val stroke = if (line % 3 == 0) 3.2f else 1.0f
                drawLine(
                    color = gridLineColor.copy(alpha = if (line % 3 == 0) 0.88f else 0.42f),
                    start = Offset(cellWidth * line, 0f),
                    end = Offset(cellWidth * line, size.height),
                    strokeWidth = stroke,
                )
                drawLine(
                    color = gridLineColor.copy(alpha = if (line % 3 == 0) 0.88f else 0.42f),
                    start = Offset(0f, cellHeight * line),
                    end = Offset(size.width, cellHeight * line),
                    strokeWidth = stroke,
                )
            }

            val dash = PathEffect.dashPathEffect(floatArrayOf(5f, 3f), 0f)
            val inset = 4.dp.toPx()
            game.puzzle.cages.forEach { cage ->
                val cells = cage.cells.toSet()

                repeat(9) { row ->
                    val topCols = cells
                        .filter { it.row == row && GridPosition(row - 1, it.col) !in cells }
                        .map { it.col }
                        .sorted()
                    topCols.consecutiveRuns().forEach { run ->
                        val y = row * cellHeight + inset
                        drawLine(
                            color = Color.Black,
                            start = Offset(run.first * cellWidth + inset, y),
                            end = Offset((run.last + 1) * cellWidth - inset, y),
                            strokeWidth = 2.3f,
                            pathEffect = dash,
                        )
                        drawCornerCaps(
                            Offset(run.first * cellWidth + inset, y),
                            Offset((run.last + 1) * cellWidth - inset, y),
                        )
                    }

                    val bottomCols = cells
                        .filter { it.row == row && GridPosition(row + 1, it.col) !in cells }
                        .map { it.col }
                        .sorted()
                    bottomCols.consecutiveRuns().forEach { run ->
                        val y = (row + 1) * cellHeight - inset
                        drawLine(
                            color = Color.Black,
                            start = Offset(run.first * cellWidth + inset, y),
                            end = Offset((run.last + 1) * cellWidth - inset, y),
                            strokeWidth = 2.3f,
                            pathEffect = dash,
                        )
                        drawCornerCaps(
                            Offset(run.first * cellWidth + inset, y),
                            Offset((run.last + 1) * cellWidth - inset, y),
                        )
                    }
                }

                repeat(9) { col ->
                    val leftRows = cells
                        .filter { it.col == col && GridPosition(it.row, col - 1) !in cells }
                        .map { it.row }
                        .sorted()
                    leftRows.consecutiveRuns().forEach { run ->
                        val x = col * cellWidth + inset
                        drawLine(
                            color = Color.Black,
                            start = Offset(x, run.first * cellHeight + inset),
                            end = Offset(x, (run.last + 1) * cellHeight - inset),
                            strokeWidth = 2.3f,
                            pathEffect = dash,
                        )
                        drawCornerCaps(
                            Offset(x, run.first * cellHeight + inset),
                            Offset(x, (run.last + 1) * cellHeight - inset),
                        )
                    }

                    val rightRows = cells
                        .filter { it.col == col && GridPosition(it.row, col + 1) !in cells }
                        .map { it.row }
                        .sorted()
                    rightRows.consecutiveRuns().forEach { run ->
                        val x = (col + 1) * cellWidth - inset
                        drawLine(
                            color = Color.Black,
                            start = Offset(x, run.first * cellHeight + inset),
                            end = Offset(x, (run.last + 1) * cellHeight - inset),
                            strokeWidth = 2.3f,
                            pathEffect = dash,
                        )
                        drawCornerCaps(
                            Offset(x, run.first * cellHeight + inset),
                            Offset(x, (run.last + 1) * cellHeight - inset),
                        )
                    }
                }
            }
        }

        CageLabelLayer(game = game)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCornerCaps(
    start: Offset,
    end: Offset,
) {
    drawCircle(color = Color.Black, radius = 1.2f, center = start)
    drawCircle(color = Color.Black, radius = 1.2f, center = end)
}

private fun List<Int>.consecutiveRuns(): List<IntRange> {
    if (isEmpty()) return emptyList()
    val runs = mutableListOf<IntRange>()
    var start = first()
    var previous = first()
    drop(1).forEach { value ->
        if (value == previous + 1) {
            previous = value
        } else {
            runs += start..previous
            start = value
            previous = value
        }
    }
    runs += start..previous
    return runs
}

@Composable
private fun SudokuCell(
    value: Int,
    notes: Set<Int>,
    isGiven: Boolean,
    isSelected: Boolean,
    isRelated: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        isError -> Color(0xFFFFECEC)
        isSelected -> Color(0xFFF4C4F2)
        isRelated -> Color.White
        else -> Color.White
    }

    Box(
        modifier = modifier
            .background(background)
            .border(
                width = if (isSelected) 1.5.dp else 0.3.dp,
                color = if (isSelected) Color(0xFFAF4CA8) else Color.Black.copy(alpha = 0.18f),
            )
            .clickable(onClick = onClick)
            .padding(2.dp),
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isGiven) FontWeight.Bold else FontWeight.Normal,
                color = if (isGiven) {
                    Color.Black
                } else {
                    Color(0xFF1D4ED8)
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
                    .padding(
                        start = 3.dp,
                        top = 3.dp,
                        end = 3.dp,
                        bottom = 3.dp,
                    ),
            )
        }
    }
}

@Composable
private fun CageLabelLayer(
    game: Game,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        repeat(9) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                repeat(9) { col ->
                    val position = GridPosition(row, col)
                    val cage = game.puzzle.cageFor(position)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = 3.dp, top = 2.dp),
                        contentAlignment = Alignment.TopStart,
                    ) {
                        if (cage?.isTopLeft(position) == true) {
                            Text(
                                text = cage.targetSum.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 7.sp,
                                lineHeight = 7.sp,
                                color = Color.Black,
                                modifier = Modifier
                                    .background(Color.White)
                                    .padding(horizontal = 1.dp, vertical = 0.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteGrid(
    notes: Set<Int>,
    modifier: Modifier = Modifier,
) {
    val values = notes.sorted()
    val firstRow = values.take(5).joinToString(separator = "")
    val secondRow = values.drop(5).take(4).joinToString(separator = "")
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NoteRow(text = firstRow)
        if (secondRow.isNotEmpty()) {
            NoteRow(text = secondRow)
        }
    }
}

@Composable
private fun NoteRow(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 9.sp,
        lineHeight = 10.sp,
        color = Color(0xFF1D4ED8),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
    )
}
