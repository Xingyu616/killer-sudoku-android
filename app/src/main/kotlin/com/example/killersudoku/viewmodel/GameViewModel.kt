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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val getHint: GetHintUseCase,
    private val solvePuzzle: SolvePuzzleUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<Game>()
    private val redoStack = ArrayDeque<Game>()
    private val saveMutex = Mutex()
    @Volatile
    private var latestSaveRequest = 0L
    @Volatile
    private var completedSaveRequest = 0L

    init {
        viewModelScope.launch {
            repository.observeLatestGame().collect { game ->
                _uiState.update {
                    val currentGame = it.game
                    val shouldKeepCurrentGame = currentGame != null &&
                        game != null &&
                        currentGame.id == game.id &&
                        (completedSaveRequest < latestSaveRequest || currentGame.lastModified >= game.lastModified)
                    val nextGame = if (shouldKeepCurrentGame) currentGame else game
                    it.copy(
                        isLoading = false,
                        game = nextGame,
                        selectedCell = it.selectedCell?.takeIf { selected ->
                            nextGame?.puzzle?.isGiven(selected) == false
                        },
                        notes = nextGame?.notes.orEmpty(),
                        cageCombinations = cageCombinationsFor(nextGame, it.selectedCell),
                        selectedCageId = cageIdFor(nextGame, it.selectedCell),
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
                    cageCombinations = emptyList(),
                    selectedCageId = null,
                    inactiveCombinations = emptyMap(),
                    inactiveNumbers = emptyMap(),
                    mistakes = emptySet(),
                    message = null,
                    canUndo = false,
                    canRedo = false,
                )
            }
        }
    }

    fun selectCell(position: GridPosition) {
        val game = _uiState.value.game ?: return
        if (game.puzzle.isGiven(position)) {
            _uiState.update {
                it.copy(
                    selectedCell = position,
                    cageCombinations = cageCombinationsFor(game, position),
                    selectedCageId = cageIdFor(game, position),
                    message = "这是题目给出的数字",
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedCell = position,
                    cageCombinations = cageCombinationsFor(game, position),
                    selectedCageId = cageIdFor(game, position),
                    message = null,
                )
            }
        }
    }

    fun inputNumber(value: Int) {
        val state = _uiState.value
        val game = state.game ?: return
        val position = state.selectedCell ?: return
        if (game.puzzle.isGiven(position)) return

        val currentValue = game.currentGrid.valueAt(position)
        val currentNotes = game.notes[position].orEmpty()
        val updated = when {
            currentValue == value -> game.withCell(position, 0).withNotes(game.notes - position)
            currentNotes.isNotEmpty() -> game.withPencilValue(position, value)
            currentValue != 0 -> game.withNotes(game.notes + (position to setOf(currentValue, value)))
                .withCell(position, 0)
            else -> game.withCell(position, value).completedIfSolved()
        }.copy(isCompleted = false, completedAt = null).completedIfSolved()
        pushUndo(game)
        save(updated)
        _uiState.update {
            it.copy(
                game = updated,
                mistakes = it.mistakes - position,
                notes = updated.notes,
                cageCombinations = cageCombinationsFor(updated, position),
                inactiveNumbers = it.inactiveNumbers.toggle(position, value),
                message = if (updated.isCompleted) "完成！这一局很漂亮。" else null,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun toggleCombination(combination: String) {
        val position = _uiState.value.selectedCell ?: return
        val cageId = cageIdFor(_uiState.value.game, position) ?: return
        _uiState.update {
            it.copy(inactiveCombinations = it.inactiveCombinations.toggle(cageId, combination))
        }
    }

    fun eraseSelected() {
        val state = _uiState.value
        val game = state.game ?: return
        val position = state.selectedCell ?: return
        if (game.puzzle.isGiven(position)) return
        if (game.currentGrid.valueAt(position) == 0 && game.notes[position].isNullOrEmpty()) return

        val updated = game.withCell(position, 0)
            .withNotes(game.notes - position)
            .copy(isCompleted = false, completedAt = null)
        pushUndo(game)
        save(updated)
        _uiState.update {
            it.copy(
                game = updated,
                notes = updated.notes,
                cageCombinations = cageCombinationsFor(updated, position),
                mistakes = it.mistakes - position,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun requestHint() {
        val state = _uiState.value
        val game = state.game ?: return
        val hint = getHint(game, state.selectedCell) ?: run {
            _uiState.update { it.copy(message = "没有可提示的空格") }
            return
        }
        val updated = game.withNotes(game.notes + (hint.position to hint.candidates))
        pushUndo(game)
        save(updated)
        _uiState.update {
            it.copy(
                game = updated,
                selectedCell = hint.position,
                notes = updated.notes,
                cageCombinations = cageCombinationsFor(updated, hint.position),
                selectedCageId = cageIdFor(updated, hint.position),
                message = hint.message,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
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
                notes = solved.notes,
                cageCombinations = emptyList(),
                selectedCageId = null,
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
                notes = previous.notes,
                cageCombinations = cageCombinationsFor(previous, it.selectedCell),
                selectedCageId = cageIdFor(previous, it.selectedCell),
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
                notes = next.notes,
                cageCombinations = cageCombinationsFor(next, it.selectedCell),
                selectedCageId = cageIdFor(next, it.selectedCell),
                mistakes = emptySet(),
                message = null,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
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
        val requestId = ++latestSaveRequest
        viewModelScope.launch(Dispatchers.IO) {
            saveMutex.withLock {
                if (requestId == latestSaveRequest) {
                    repository.saveGame(game)
                    completedSaveRequest = requestId
                }
            }
        }
    }

    private fun cageCombinationsFor(game: Game?, position: GridPosition?): List<String> {
        if (game == null || position == null) return emptyList()
        val cage = game.puzzle.cageFor(position) ?: return emptyList()
        return combinationsFor(cage.targetSum, cage.cells.size)
            .map { values -> values.joinToString(separator = "") }
    }

    private fun cageIdFor(game: Game?, position: GridPosition?): Int? =
        if (game == null || position == null) null else game.puzzle.cageFor(position)?.id

    private fun combinationsFor(targetSum: Int, size: Int): List<List<Int>> {
        val results = mutableListOf<List<Int>>()

        fun search(start: Int, remainingSize: Int, remainingSum: Int, current: List<Int>) {
            if (remainingSize == 0) {
                if (remainingSum == 0) results += current
                return
            }
            for (value in start..9) {
                if (value > remainingSum) break
                search(value + 1, remainingSize - 1, remainingSum - value, current + value)
            }
        }

        search(start = 1, remainingSize = size, remainingSum = targetSum, current = emptyList())
        return results
    }

    private fun Game.withPencilValue(position: GridPosition, value: Int): Game {
        val baseNotes = notes[position].orEmpty()
        val currentValue = currentGrid.valueAt(position).takeIf { it != 0 }
        val current = if (currentValue == null) baseNotes else baseNotes + currentValue
        val next = if (value in current) current - value else current + value
        return when (next.size) {
            0 -> withCell(position, 0).withNotes(notes - position)
            1 -> withNotes(notes - position).withCell(position, next.single())
            else -> withCell(position, 0).withNotes(notes + (position to next))
        }
    }

    private fun <K, T> Map<K, Set<T>>.toggle(key: K, value: T): Map<K, Set<T>> {
        val current = this[key].orEmpty()
        val next = if (value in current) current - value else current + value
        return if (next.isEmpty()) {
            this - key
        } else {
            this + (key to next)
        }
    }

    private fun ArrayDeque<Game>.removeLastOrNullCompat(): Game? =
        if (isEmpty()) null else removeLast()
}
