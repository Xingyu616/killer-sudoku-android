package com.example.killersudoku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.killersudoku.R
import com.example.killersudoku.domain.model.BoardTheme
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.positions
import com.example.killersudoku.domain.model.valueAt
import com.example.killersudoku.domain.repository.GameRepository
import com.example.killersudoku.domain.usecase.GetHintUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
                    val selectedCell = it.selectedCell?.takeIf { selected ->
                        selected.row in 0..8 && selected.col in 0..8
                    }
                    val selectedCells = it.selectedCells.filterTo(mutableSetOf()) { selected ->
                        selected.row in 0..8 && selected.col in 0..8
                    }
                    it.copy(
                        isLoading = false,
                        game = nextGame,
                        selectedCell = selectedCell,
                        selectedCells = selectedCells,
                        notes = nextGame?.notes.orEmpty(),
                        cageCombinations = cageCombinationsForSelection(nextGame, selectedCell, selectedCells),
                        selectedCageTotal = selectedCageTotalFor(nextGame, selectedCells),
                        selectedCageId = if (selectedCells.size > 1) null else cageIdFor(nextGame, selectedCell),
                        elapsedMillis = nextGame?.currentElapsedMillis().orZero(),
                        isPaused = nextGame?.pausedAt != null,
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = redoStack.isNotEmpty(),
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.observeStats().collect { stats ->
                _uiState.update { it.copy(stats = stats) }
            }
        }
        viewModelScope.launch {
            repository.observePlayerProgress().collect { progress ->
                _uiState.update { it.copy(progress = progress) }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _uiState.update {
                    val game = it.game
                    if (game == null || game.isCompleted || game.pausedAt != null) {
                        it
                    } else {
                        it.copy(elapsedMillis = game.currentElapsedMillis())
                    }
                }
            }
        }
    }

    fun startNewGame(difficulty: Difficulty) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    message = null,
                    mistakes = emptySet(),
                    showCompletionDialog = false,
                )
            }
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
                    selectedCells = emptySet(),
                    notes = emptyMap(),
                    cageCombinations = emptyList(),
                    selectedCageTotal = null,
                    selectedCageId = null,
                    inactiveCombinations = emptyMap(),
                    inactiveNumbers = emptyMap(),
                    mistakes = emptySet(),
                    message = null,
                    completionReward = null,
                    dailyCheckInReward = null,
                    elapsedMillis = game.currentElapsedMillis(),
                    isPaused = false,
                    showCompletionDialog = false,
                    canUndo = false,
                    canRedo = false,
                )
            }
        }
    }

    fun selectCell(position: GridPosition) {
        val game = _uiState.value.game ?: return
        if (game.isCompleted) return
        if (game.puzzle.isGiven(position)) {
            _uiState.update {
                it.copy(
                    selectedCell = position,
                    selectedCells = emptySet(),
                    cageCombinations = cageCombinationsFor(game, position),
                    selectedCageTotal = null,
                    selectedCageId = cageIdFor(game, position),
                    message = UiMessage(R.string.message_given_cell),
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    selectedCell = position,
                    selectedCells = setOf(position),
                    cageCombinations = cageCombinationsFor(game, position),
                    selectedCageTotal = null,
                    selectedCageId = cageIdFor(game, position),
                    message = null,
                )
            }
        }
    }

    fun selectCells(positions: Set<GridPosition>) {
        val game = _uiState.value.game ?: return
        if (game.isCompleted || game.pausedAt != null) return
        val selected = positions
            .filter { it.row in 0..8 && it.col in 0..8 }
            .toSet()
        val editable = selected.filterNot { game.puzzle.isGiven(it) }
        val primary = editable.sortedWith(compareBy<GridPosition> { it.row }.thenBy { it.col }).firstOrNull()
        _uiState.update {
            it.copy(
                selectedCell = primary,
                selectedCells = selected,
                cageCombinations = emptyList(),
                selectedCageTotal = selectedCageTotalFor(game, selected),
                selectedCageId = null,
                message = null,
            )
        }
    }

    fun inputNumber(value: Int) {
        val state = _uiState.value
        val game = state.game ?: return
        if (game.isCompleted || game.pausedAt != null) return
        val positions = state.inputTargets(game)
        if (positions.isEmpty()) return

        val edited = positions.fold(game) { current, position ->
            current.withInputValue(position, value)
        }
            .cleanRelatedNotesIfEnabled(positions, value, state.progress.autoClearNotes)
            .copy(isCompleted = false, completedAt = null)
        val updated = edited.completedIfSolved()

        pushUndo(game)
        save(updated, captureReward = updated.isCompleted && !game.isCompleted)
        _uiState.update {
            it.copy(
                game = updated,
                mistakes = it.mistakes - positions,
                notes = updated.notes,
                cageCombinations = cageCombinationsForSelection(updated, it.selectedCell, it.selectedCells),
                selectedCageTotal = selectedCageTotalFor(updated, it.selectedCells),
                inactiveNumbers = positions.fold(it.inactiveNumbers) { current, position ->
                    current.toggle(position, value)
                },
                message = if (updated.isCompleted) UiMessage(R.string.message_completed) else null,
                elapsedMillis = updated.currentElapsedMillis(),
                isPaused = updated.pausedAt != null,
                showCompletionDialog = updated.isCompleted && !game.isCompleted,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun toggleCombination(combination: String) {
        val state = _uiState.value
        if (state.game?.isCompleted == true || state.game?.pausedAt != null) return
        val position = state.selectedCell ?: return
        val cageId = cageIdFor(state.game, position) ?: return
        _uiState.update {
            it.copy(inactiveCombinations = it.inactiveCombinations.toggle(cageId, combination))
        }
    }

    fun eraseSelected() {
        val state = _uiState.value
        val game = state.game ?: return
        if (game.isCompleted || game.pausedAt != null) return
        val positions = state.inputTargets(game).filter { position ->
            game.currentGrid.valueAt(position) != 0 || !game.notes[position].isNullOrEmpty()
        }.toSet()
        if (positions.isEmpty()) return

        val updated = positions.fold(game) { current, position ->
            current.withCell(position, 0).withNotes(current.notes - position)
        }
            .copy(isCompleted = false, completedAt = null)
        pushUndo(game)
        save(updated)
        _uiState.update {
            it.copy(
                game = updated,
                notes = updated.notes,
                cageCombinations = cageCombinationsForSelection(updated, it.selectedCell, it.selectedCells),
                selectedCageTotal = selectedCageTotalFor(updated, it.selectedCells),
                mistakes = it.mistakes - positions,
                inactiveNumbers = it.inactiveNumbers - positions,
                elapsedMillis = updated.currentElapsedMillis(),
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun requestHint() {
        val state = _uiState.value
        val game = state.game ?: return
        if (game.isCompleted || game.pausedAt != null) return
        val hint = getHint(game, state.selectedCell) ?: run {
            _uiState.update { it.copy(message = UiMessage(R.string.message_no_hint)) }
            return
        }
        viewModelScope.launch {
            val spend = repository.spendHintAccess() ?: run {
                _uiState.update { it.copy(message = UiMessage(R.string.message_hint_cost_short)) }
                return@launch
            }
            applyHint(game, hint.position, hint.answer ?: return@launch, spend.usedTicket)
        }
    }

    private fun applyHint(
        game: Game,
        position: GridPosition,
        answer: Int,
        usedTicket: Boolean,
    ) {
        val updated = game
            .withCell(position, answer)
            .copy(usedHint = true, isCompleted = false, completedAt = null)
            .completedIfSolved()
        pushUndo(game)
        save(updated, captureReward = updated.isCompleted && !game.isCompleted)
        _uiState.update {
            it.copy(
                game = updated,
                selectedCell = position,
                selectedCells = setOf(position),
                notes = updated.notes,
                cageCombinations = cageCombinationsFor(updated, position),
                selectedCageTotal = null,
                selectedCageId = cageIdFor(updated, position),
                message = UiMessage(
                    if (usedTicket) R.string.message_hint_ticket else R.string.message_hint_paid,
                    listOf(
                        (position.row + 1).toString(),
                        (position.col + 1).toString(),
                        answer.toString(),
                    ),
                ),
                elapsedMillis = updated.currentElapsedMillis(),
                showCompletionDialog = updated.isCompleted && !game.isCompleted,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun checkBoard() {
        val game = _uiState.value.game ?: return
        if (game.pausedAt != null) return
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
                message = if (mistakes.isEmpty()) {
                    UiMessage(R.string.message_no_mistakes)
                } else {
                    UiMessage(R.string.message_mistakes, listOf(mistakes.size.toString()))
                },
            )
        }
    }

    fun smartHint() = requestHint()

    fun purchaseHintTicket() {
        viewModelScope.launch {
            val purchased = repository.purchaseHintTicket()
            _uiState.update {
                it.copy(
                    message = UiMessage(
                        if (purchased) R.string.message_hint_ticket_bought else R.string.message_hint_ticket_short,
                    ),
                )
            }
        }
    }

    fun setBgmEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setBgmEnabled(enabled) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun setAutoClearNotes(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoClearNotes(enabled) }
    }

    fun setErrorHighlightEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setErrorHighlightEnabled(enabled) }
    }

    fun selectTheme(theme: BoardTheme) {
        viewModelScope.launch {
            val selected = repository.selectTheme(theme)
            _uiState.update {
                it.copy(
                    message = UiMessage(
                        if (selected) R.string.message_theme_selected else R.string.message_theme_short,
                    ),
                )
            }
        }
    }

    fun claimDailyCheckIn() {
        viewModelScope.launch {
            val reward = repository.claimDailyCheckIn()
            _uiState.update {
                it.copy(
                    dailyCheckInReward = reward,
                    message = reward?.let { result ->
                        UiMessage(
                            R.string.message_daily_check_in,
                            listOf(result.coins.toString(), (result.checkInStreak ?: 1).toString()),
                        )
                    } ?: UiMessage(R.string.message_daily_checked),
                )
            }
        }
    }

    fun pauseGame() {
        val game = _uiState.value.game ?: return
        val paused = game.pause()
        if (paused == game) return
        save(paused)
        _uiState.update {
            it.copy(
                game = paused,
                elapsedMillis = paused.elapsedMillis,
                isPaused = true,
                message = UiMessage(R.string.message_paused),
            )
        }
    }

    fun resumeGame() {
        val game = _uiState.value.game ?: return
        val resumed = game.resume()
        if (resumed == game) return
        save(resumed)
        _uiState.update {
            it.copy(
                game = resumed,
                elapsedMillis = resumed.currentElapsedMillis(),
                isPaused = false,
                message = null,
            )
        }
    }

    fun dismissCompletionDialog() {
        _uiState.update { it.copy(showCompletionDialog = false, completionReward = null) }
    }

    fun undo() {
        val current = _uiState.value.game ?: return
        if (current.pausedAt != null) return
        val previous = undoStack.removeLastOrNullCompat() ?: return
        redoStack.addLast(current)
        save(previous)
        _uiState.update {
            it.copy(
                game = previous,
                notes = previous.notes,
                cageCombinations = cageCombinationsForSelection(previous, it.selectedCell, it.selectedCells),
                selectedCageTotal = selectedCageTotalFor(previous, it.selectedCells),
                selectedCageId = if (it.selectedCells.size > 1) null else cageIdFor(previous, it.selectedCell),
                inactiveNumbers = emptyMap(),
                mistakes = emptySet(),
                message = null,
                elapsedMillis = previous.currentElapsedMillis(),
                isPaused = previous.pausedAt != null,
                showCompletionDialog = false,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    fun redo() {
        val current = _uiState.value.game ?: return
        if (current.pausedAt != null) return
        val next = redoStack.removeLastOrNullCompat() ?: return
        undoStack.addLast(current)
        save(next)
        _uiState.update {
            it.copy(
                game = next,
                notes = next.notes,
                cageCombinations = cageCombinationsForSelection(next, it.selectedCell, it.selectedCells),
                selectedCageTotal = selectedCageTotalFor(next, it.selectedCells),
                selectedCageId = if (it.selectedCells.size > 1) null else cageIdFor(next, it.selectedCell),
                inactiveNumbers = emptyMap(),
                mistakes = emptySet(),
                message = null,
                elapsedMillis = next.currentElapsedMillis(),
                isPaused = next.pausedAt != null,
                showCompletionDialog = next.isCompleted,
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

    private fun save(
        game: Game,
        captureReward: Boolean = false,
    ) {
        val requestId = ++latestSaveRequest
        viewModelScope.launch(Dispatchers.IO) {
            saveMutex.withLock {
                if (requestId == latestSaveRequest) {
                    repository.saveGame(game)
                    if (game.isCompleted) {
                        val reward = repository.recordCompletedGame(game)
                        if (captureReward && reward != null) {
                            _uiState.update { it.copy(completionReward = reward) }
                        }
                    }
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

    private fun cageCombinationsForSelection(
        game: Game?,
        position: GridPosition?,
        selectedCells: Set<GridPosition>,
    ): List<String> =
        if (selectedCells.size > 1) emptyList() else cageCombinationsFor(game, position)

    private fun selectedCageTotalFor(
        game: Game?,
        selectedCells: Set<GridPosition>,
    ): CageSelectionTotal? {
        if (game == null || selectedCells.size <= 1) return null
        val completeCages = game.puzzle.cages.filter { cage ->
            val cageCells = cage.cells.toSet()
            cageCells.isNotEmpty() && selectedCells.containsAll(cageCells)
        }
        if (completeCages.isEmpty()) return null

        val completeCageCells = completeCages
            .flatMap { cage -> cage.cells }
            .toSet()
        val extraValues = (selectedCells - completeCageCells)
            .map { position -> game.currentGrid.valueAt(position) }
            .filter { value -> value in 1..9 }
        return CageSelectionTotal(
            totalSum = completeCages.sumOf { cage -> cage.targetSum } + extraValues.sum(),
            completeCageCount = completeCages.size,
            extraFilledCount = extraValues.size,
        )
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

    private fun Game.withInputValue(position: GridPosition, value: Int): Game {
        val currentValue = currentGrid.valueAt(position)
        val currentNotes = notes[position].orEmpty()
        return when {
            currentValue == value -> withCell(position, 0).withNotes(notes - position)
            currentNotes.isNotEmpty() -> withPencilValue(position, value)
            currentValue != 0 -> withNotes(notes + (position to setOf(currentValue, value)))
                .withCell(position, 0)
            else -> withCell(position, value)
        }
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

    private fun Game.cleanRelatedNotesIfEnabled(
        positions: Set<GridPosition>,
        value: Int,
        enabled: Boolean,
    ): Game {
        if (!enabled) return this
        val solvedPositions = positions.filter { position ->
            currentGrid.valueAt(position) == value &&
                puzzle.solutionGrid.valueAt(position) == value
        }
        if (solvedPositions.isEmpty()) return this

        val affected = solvedPositions.flatMap { position ->
            val cageCells = puzzle.cageFor(position)?.cells.orEmpty()
            currentGrid.positions().filter { other ->
                other != position &&
                    (
                        other.row == position.row ||
                            other.col == position.col ||
                            other in cageCells
                        )
            }
        }.toSet()
        val cleanedNotes = notes.mapValues { (position, values) ->
            if (position in affected) values - value else values
        }
        return withNotes(cleanedNotes)
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

    private fun Long?.orZero(): Long = this ?: 0L

    private fun GameUiState.inputTargets(game: Game): Set<GridPosition> =
        (selectedCells.ifEmpty { selectedCell?.let { setOf(it) }.orEmpty() })
            .filterNot { game.puzzle.isGiven(it) }
            .toSet()
}
