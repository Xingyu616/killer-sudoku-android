package com.example.killersudoku.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.killersudoku.R
import com.example.killersudoku.ui.screen.DifficultyScreen
import com.example.killersudoku.ui.screen.GameScreen
import com.example.killersudoku.ui.screen.HomeScreen
import com.example.killersudoku.ui.theme.KillerSudokuTheme
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
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val currentScreen = AppScreen.valueOf(screen)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.pauseGame()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler {
        when (currentScreen) {
            AppScreen.HOME -> showExitDialog = true
            AppScreen.DIFFICULTY -> screen = AppScreen.HOME.name
            AppScreen.GAME -> {
                viewModel.pauseGame()
                screen = AppScreen.HOME.name
            }
        }
    }

    KillerSudokuTheme(boardTheme = state.progress.selectedTheme) {
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text(stringResource(R.string.exit_game_title)) },
                text = { Text(stringResource(R.string.exit_game_body)) },
                confirmButton = {
                    TextButton(onClick = { context.findActivity()?.finish() }) {
                        Text(stringResource(R.string.action_exit))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                },
            )
        }

        when (currentScreen) {
            AppScreen.HOME -> HomeScreen(
                state = state,
                onContinue = {
                    viewModel.resumeGame()
                    screen = AppScreen.GAME.name
                },
                onNewGame = { screen = AppScreen.DIFFICULTY.name },
                onDailyCheckIn = viewModel::claimDailyCheckIn,
                onPurchaseHintTicket = viewModel::purchaseHintTicket,
                onBgmChanged = viewModel::setBgmEnabled,
                onSoundChanged = viewModel::setSoundEnabled,
                onAutoClearNotesChanged = viewModel::setAutoClearNotes,
                onErrorHighlightChanged = viewModel::setErrorHighlightEnabled,
                onSelectTheme = viewModel::selectTheme,
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
                onBack = {
                    viewModel.pauseGame()
                    screen = AppScreen.HOME.name
                },
                onCellClick = viewModel::selectCell,
                onCellsSelected = viewModel::selectCells,
                onCombination = viewModel::toggleCombination,
                onNumber = viewModel::inputNumber,
                onErase = viewModel::eraseSelected,
                onHint = viewModel::requestHint,
                onCheck = viewModel::checkBoard,
                onSolve = viewModel::smartHint,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onPause = viewModel::pauseGame,
                onResume = viewModel::resumeGame,
                onCompletionDismiss = viewModel::dismissCompletionDialog,
            )
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
