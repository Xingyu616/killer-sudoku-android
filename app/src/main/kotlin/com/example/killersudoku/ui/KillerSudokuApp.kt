package com.example.killersudoku.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.killersudoku.ui.screen.DifficultyScreen
import com.example.killersudoku.ui.screen.GameScreen
import com.example.killersudoku.ui.screen.HomeScreen
import com.example.killersudoku.viewmodel.GameViewModel

private enum class AppScreen {
    HOME,
    DIFFICULTY,
    GAME,
}

@Composable
fun KillerSudokuApp(
    viewModel: GameViewModel = hiltViewModel(),
) {
    var screen by rememberSaveable { mutableStateOf(AppScreen.HOME.name) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (AppScreen.valueOf(screen)) {
        AppScreen.HOME -> HomeScreen(
            state = state,
            onContinue = { screen = AppScreen.GAME.name },
            onNewGame = { screen = AppScreen.DIFFICULTY.name },
        )

        AppScreen.DIFFICULTY -> DifficultyScreen(
            onBack = { screen = AppScreen.HOME.name },
            onDifficultySelected = {
                viewModel.startNewGame(it)
                screen = AppScreen.GAME.name
            },
        )

        AppScreen.GAME -> GameScreen(
            state = state,
            onBack = { screen = AppScreen.HOME.name },
            onCellClick = viewModel::selectCell,
            onNumber = viewModel::inputNumber,
            onErase = viewModel::eraseSelected,
            onToggleNote = viewModel::toggleNoteMode,
            onHint = viewModel::requestHint,
            onCheck = viewModel::checkBoard,
            onSolve = viewModel::solve,
            onUndo = viewModel::undo,
            onRedo = viewModel::redo,
        )
    }
}
