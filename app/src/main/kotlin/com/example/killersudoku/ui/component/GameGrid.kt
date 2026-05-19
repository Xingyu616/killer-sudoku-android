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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.killersudoku.domain.model.BoardTheme
import com.example.killersudoku.domain.model.Cage
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
    boardTheme: BoardTheme,
    onCellClick: (GridPosition) -> Unit,
    onCellsSelected: (Set<GridPosition>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = boardTheme.gridColors()
    val gridLineColor = colors.line
    val dragSelection = remember { mutableStateOf(emptySet<GridPosition>()) }
    val visibleSelection = selectedCells + dragSelection.value
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(colors.cell)
            .border(2.dp, colors.line)
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
                            colors = colors,
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
                val stroke = if (line % 3 == 0) 4.6f else 1.0f
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

            val cageInset = 4.dp.toPx()
            val cageStroke = Stroke(
                width = 2.3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f),
            )
            game.puzzle.cages.forEach { cage ->
                cage.boundaryPaths(
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    inset = cageInset,
                ).forEach { path ->
                    drawPath(
                        path = path,
                        color = colors.cageLine,
                        style = cageStroke,
                    )
                }
            }
        }

        CageLabelLayer(game = game, colors = colors)
    }
}

@Composable
private fun SudokuCell(
    value: Int,
    notes: Set<Int>,
    isGiven: Boolean,
    isSelected: Boolean,
    isRelated: Boolean,
    isError: Boolean,
    colors: GridColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        isError -> colors.error
        isSelected -> colors.selected
        isRelated -> colors.related
        else -> colors.cell
    }

    Box(
        modifier = modifier
            .background(background)
            .border(
                width = if (isSelected) 1.5.dp else 0.3.dp,
                color = if (isSelected) colors.selectionLine else colors.line.copy(alpha = 0.18f),
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
                    colors.givenText
                } else {
                    colors.userText
                },
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        } else if (notes.isNotEmpty()) {
            NoteGrid(
                notes = notes,
                color = colors.userText,
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

private fun Cage.boundaryPaths(
    cellWidth: Float,
    cellHeight: Float,
    inset: Float,
): List<Path> {
    val cageCells = cells.toSet()
    val edges = mutableListOf<BoundaryEdge>()

    cageCells.forEach { cell ->
        if (GridPosition(cell.row - 1, cell.col) !in cageCells) {
            edges += BoundaryEdge(
                start = GridCorner(cell.row, cell.col),
                end = GridCorner(cell.row, cell.col + 1),
            )
        }
        if (GridPosition(cell.row, cell.col + 1) !in cageCells) {
            edges += BoundaryEdge(
                start = GridCorner(cell.row, cell.col + 1),
                end = GridCorner(cell.row + 1, cell.col + 1),
            )
        }
        if (GridPosition(cell.row + 1, cell.col) !in cageCells) {
            edges += BoundaryEdge(
                start = GridCorner(cell.row + 1, cell.col + 1),
                end = GridCorner(cell.row + 1, cell.col),
            )
        }
        if (GridPosition(cell.row, cell.col - 1) !in cageCells) {
            edges += BoundaryEdge(
                start = GridCorner(cell.row + 1, cell.col),
                end = GridCorner(cell.row, cell.col),
            )
        }
    }

    return edges
        .orderedLoops()
        .mapNotNull { loop -> loop.toInsetPath(cellWidth, cellHeight, inset) }
}

private fun MutableList<BoundaryEdge>.orderedLoops(): List<List<BoundaryEdge>> {
    val loops = mutableListOf<List<BoundaryEdge>>()
    while (isNotEmpty()) {
        val loop = mutableListOf(removeAt(0))
        val startCorner = loop.first().start
        var currentCorner = loop.first().end
        while (currentCorner != startCorner && isNotEmpty()) {
            val nextIndex = indexOfFirst { it.start == currentCorner }
            if (nextIndex == -1) break
            val nextEdge = removeAt(nextIndex)
            loop += nextEdge
            currentCorner = nextEdge.end
        }
        loops += loop
    }
    return loops
}

private fun List<BoundaryEdge>.toInsetPath(
    cellWidth: Float,
    cellHeight: Float,
    inset: Float,
): Path? {
    if (isEmpty()) return null

    val points = indices.map { index ->
        val previous = this[(index - 1 + size) % size]
        val current = this[index]
        insetCornerPoint(
            corner = current.start,
            previous = previous.insetLine(cellWidth, cellHeight, inset),
            current = current.insetLine(cellWidth, cellHeight, inset),
            cellWidth = cellWidth,
            cellHeight = cellHeight,
        )
    }

    return Path().apply {
        val firstPoint = points.first()
        moveTo(firstPoint.x, firstPoint.y)
        points.drop(1).forEach { point ->
            lineTo(point.x, point.y)
        }
        close()
    }
}

private fun insetCornerPoint(
    corner: GridCorner,
    previous: InsetLine,
    current: InsetLine,
    cellWidth: Float,
    cellHeight: Float,
): Offset =
    when {
        previous is InsetLine.Horizontal && current is InsetLine.Vertical -> Offset(current.x, previous.y)
        previous is InsetLine.Vertical && current is InsetLine.Horizontal -> Offset(previous.x, current.y)
        previous is InsetLine.Horizontal && current is InsetLine.Horizontal -> {
            Offset(corner.x(cellWidth), current.y)
        }
        previous is InsetLine.Vertical && current is InsetLine.Vertical -> {
            Offset(current.x, corner.y(cellHeight))
        }
        else -> Offset(corner.x(cellWidth), corner.y(cellHeight))
    }

private data class BoundaryEdge(
    val start: GridCorner,
    val end: GridCorner,
) {
    fun insetLine(
        cellWidth: Float,
        cellHeight: Float,
        inset: Float,
    ): InsetLine =
        when {
            start.row == end.row && start.col < end.col -> InsetLine.Horizontal(start.y(cellHeight) + inset)
            start.col == end.col && start.row < end.row -> InsetLine.Vertical(start.x(cellWidth) - inset)
            start.row == end.row && start.col > end.col -> InsetLine.Horizontal(start.y(cellHeight) - inset)
            start.col == end.col && start.row > end.row -> InsetLine.Vertical(start.x(cellWidth) + inset)
            else -> error("Boundary edge must be horizontal or vertical.")
        }
}

private sealed interface InsetLine {
    data class Horizontal(val y: Float) : InsetLine
    data class Vertical(val x: Float) : InsetLine
}

private data class GridCorner(
    val row: Int,
    val col: Int,
) {
    fun x(cellWidth: Float): Float = col * cellWidth

    fun y(cellHeight: Float): Float = row * cellHeight
}

@Composable
private fun CageLabelLayer(
    game: Game,
    colors: GridColors,
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
                                color = colors.labelText,
                                modifier = Modifier
                                    .background(colors.cell)
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
    color: Color,
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
        NoteRow(text = firstRow, color = color)
        if (secondRow.isNotEmpty()) {
            NoteRow(text = secondRow, color = color)
        }
    }
}

@Composable
private fun NoteRow(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 9.sp,
        lineHeight = 10.sp,
        color = color,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
    )
}

private data class GridColors(
    val cell: Color,
    val related: Color,
    val selected: Color,
    val error: Color,
    val line: Color,
    val cageLine: Color,
    val selectionLine: Color,
    val givenText: Color,
    val userText: Color,
    val labelText: Color,
)

private fun BoardTheme.gridColors(): GridColors =
    when (this) {
        BoardTheme.DEFAULT -> GridColors(
            cell = Color.White,
            related = Color.White,
            selected = Color(0xFFF4C4F2),
            error = Color(0xFFFFECEC),
            line = Color.Black,
            cageLine = Color.Black,
            selectionLine = Color(0xFFAF4CA8),
            givenText = Color.Black,
            userText = Color(0xFF1D4ED8),
            labelText = Color.Black,
        )

        BoardTheme.EYE_CARE -> GridColors(
            cell = Color(0xFFF4FAEA),
            related = Color(0xFFEAF3DE),
            selected = Color(0xFFDCEFC9),
            error = Color(0xFFFFE4DA),
            line = Color(0xFF31422B),
            cageLine = Color(0xFF31422B),
            selectionLine = Color(0xFF5C8F3E),
            givenText = Color(0xFF172018),
            userText = Color(0xFF1F6D4C),
            labelText = Color(0xFF172018),
        )

        BoardTheme.NIGHT -> GridColors(
            cell = Color(0xFF1D2430),
            related = Color(0xFF243041),
            selected = Color(0xFF384563),
            error = Color(0xFF55313A),
            line = Color(0xFFE5EDF7),
            cageLine = Color(0xFFE5EDF7),
            selectionLine = Color(0xFF8DB6FF),
            givenText = Color(0xFFF6F8FB),
            userText = Color(0xFF8DB6FF),
            labelText = Color(0xFFF6F8FB),
        )

        BoardTheme.PAPER -> GridColors(
            cell = Color(0xFFFAF6EC),
            related = Color(0xFFF1E8D7),
            selected = Color(0xFFE8D7B9),
            error = Color(0xFFFFE0D7),
            line = Color(0xFF3F3428),
            cageLine = Color(0xFF3F3428),
            selectionLine = Color(0xFF9B6B3D),
            givenText = Color(0xFF241B14),
            userText = Color(0xFF245C7C),
            labelText = Color(0xFF241B14),
        )
    }
