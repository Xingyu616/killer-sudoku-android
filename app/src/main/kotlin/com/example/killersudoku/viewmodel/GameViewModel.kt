package com.example.killersudoku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.valueAt
import com.example.killersudoku.domain.repository.GameRepository
import com.example.killersudoku.domain.usecase.GetHintUseCase
import com.example.killersudoku.domain.usecase.SolvePuzzleUseCase
import com.example.killersudoku.domain.usecase.ValidateMoveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val validateMove: ValidateMoveUseCase,
    private val getHint: GetHintUseCase,
    private val solvePuzzle: SolvePuzzleUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<Game>()
    private val redoStack = ArrayDeque<Game>()

    init {
        viewModelScope.launch {
            repository.observeLatestGame().collect { game ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        game = game,
                        selectedCell = it.selectedCell?.takeIf { selected ->
                            game?.puzzle?.isGiven(selected) == false
                        },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty(),
                    )
                }
            }
        }
    }

    fun startNewGame(difficulty: Difficulty) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, mistakes = emptySet()) }
            undoStack.clear()
            redoStack.clear()
            val game = withContext(Dispatchers.Default) {
                repository.createGame(difficulty)
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    game = game,
                    selectedCell = null,
                    notes = emptyMap(),
                    canUndo = false,
                    canRedo = false,
                )
            }
        }
    }

    fun selectCell(position: GridPosition) {
        val game = _uiState.value.game ?: return
        if (game.puzzle.isGiven(position)) {
            _uiState.update { it.copy(selectedCell = position, message = "这是题目给出的数字") }
        } else {
            _uiState.update { it.copy(selectedCell = position, message = null) }
        }
    }

    fun inputNumber(value: Int) {
        val state = _uiState.value
        val game = state.game ?: return
        val position = state.selectedCell ?: return
        if (game.puzzle.isGiven(position)) return

        if (state.noteMode) {
            toggleNote(position, value)
            return
        }

        val result = validateMove(game, position, value)
        if (!result.isValid) {
            _uiState.update {
                it.copy(
                    mistakes = it.mistakes + position,
                    message = result.reason,
                )
            }
            return
        }

        val updated = game.withCell(position, value).completedIfSolved()
        pushUndo(game)
        save(updated)
        _uiState.update {
            it.copy(
                game = updated,
                mistakes = it.mistakes - position,
                notes = it.notes - position,
                message = if (updated.isCompleted) "完成！这一局很漂亮。" else null,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun eraseSelected() {
        val state = _uiState.value
        val game = state.game ?: return
        val position = state.selectedCell ?: return
        if (game.puzzle.isGiven(position) || game.currentGrid.valueAt(position) == 0) return

        val updated = game.withCell(position, 0).copy(isCompleted = false, completedAt = null)
        pushUndo(game)
        save(updated)
        _uiState.update {
            it.copy(
                game = updated,
                mistakes = it.mistakes - position,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun toggleNoteMode() {
        _uiState.update { it.copy(noteMode = !it.noteMode) }
    }

    fun requestHint() {
        val state = _uiState.value
        val game = state.game ?: return
        val hint = getHint(game, state.selectedCell) ?: run {
            _uiState.update { it.copy(message = "没有可提示的空格") }
            return
        }
        _uiState.update {
            it.copy(
                selectedCell = hint.position,
                notes = it.notes + (hint.position to hint.candidates),
                message = hint.message,
            )
        }
    }

    fun checkBoard() {
        val game = _uiState.value.game ?: return
        val mistakes = game.currentGrid
            .flatMapIndexed { row, values ->
                values.mapIndexedNotNull { col, value ->
                    val position = GridPosition(row, col)
                    if (value != 0 && value != game.puzzle.solutionGrid.valueAt(position)) position else null
                }
            }
            .toSet()

        _uiState.update {
            it.copy(
                mistakes = mistakes,
                message = if (mistakes.isEmpty()) "目前没有发现错误" else "有 ${mistakes.size} 个格子需要再看看",
            )
        }
    }

    fun solve() {
        val game = _uiState.value.game ?: return
        pushUndo(game)
        val solved = solvePuzzle(game)
        save(solved)
        _uiState.update {
            it.copy(
                game = solved,
                mistakes = emptySet(),
                message = "已显示完整解答",
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun undo() {
        val current = _uiState.value.game ?: return
        val previous = undoStack.removeLastOrNullCompat() ?: return
        redoStack.addLast(current)
        save(previous)
        _uiState.update {
            it.copy(
                game = previous,
                mistakes = emptySet(),
                message = null,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun redo() {
        val current = _uiState.value.game ?: return
        val next = redoStack.removeLastOrNullCompat() ?: return
        undoStack.addLast(current)
        save(next)
        _uiState.update {
            it.copy(
                game = next,
                mistakes = emptySet(),
                message = null,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    private fun toggleNote(position: GridPosition, value: Int) {
        _uiState.update {
            val current = it.notes[position].orEmpty()
            val next = if (value in current) current - value else current + value
            it.copy(notes = it.notes + (position to next), message = null)
        }
    }

    private fun pushUndo(game: Game) {
        undoStack.addLast(game)
        redoStack.clear()
        while (undoStack.size > 80) {
            undoStack.removeFirst()
        }
    }

    private fun save(game: Game) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveGame(game)
        }
    }

    private fun ArrayDeque<Game>.removeLastOrNullCompat(): Game? =
        if (isEmpty()) null else removeLast()
}
