package com.example.killersudoku.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.killersudoku.R
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.ui.component.GameGrid
import com.example.killersudoku.ui.component.NumberKeypad
import com.example.killersudoku.viewmodel.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onCellClick: (GridPosition) -> Unit,
    onCellsSelected: (Set<GridPosition>) -> Unit,
    onCombination: (String) -> Unit,
    onNumber: (Int) -> Unit,
    onErase: () -> Unit,
    onHint: () -> Unit,
    onCheck: () -> Unit,
    onSolve: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCompletionDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.showCompletionDialog && state.game?.isCompleted == true) {
        AlertDialog(
            onDismissRequest = onCompletionDismiss,
            title = { CompletionTitle() },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(
                            R.string.game_completion_body,
                            formatDuration(state.elapsedMillis),
                        ),
                    )
                    Text(
                        text = stringResource(R.string.game_completion_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    state.completionReward?.let { reward ->
                        Text(
                            text = stringResource(
                                R.string.game_completion_reward,
                                reward.coins.toString(),
                                reward.tier.title,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (reward.firstWinBonusCoins > 0) {
                            Text(
                                text = stringResource(
                                    R.string.game_first_win_bonus,
                                    reward.firstWinBonusCoins.toString(),
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onCompletionDismiss) {
                    Text(stringResource(R.string.action_close))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(state.game?.puzzle?.difficulty?.title ?: stringResource(R.string.game_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.action_home))
                    }
                },
                actions = {
                    val currentGame = state.game
                    if (currentGame != null && !currentGame.isCompleted) {
                        TextButton(onClick = if (state.isPaused) onResume else onPause) {
                            Text(stringResource(if (state.isPaused) R.string.action_resume else R.string.action_pause))
                        }
                    }
                    TextButton(onClick = onCheck, enabled = state.game != null) {
                        Text(stringResource(R.string.action_check))
                    }
                },
            )
        },
    ) { paddingValues ->
        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
            )
            return@Scaffold
        }

        val game = state.game
        if (game == null) {
            EmptyGame(onBack = onBack, modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = when {
                        game.isCompleted -> stringResource(R.string.game_completed)
                        state.isPaused -> stringResource(R.string.game_paused)
                        else -> stringResource(R.string.game_in_progress)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (game.isCompleted) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
                Text(
                    text = stringResource(R.string.game_timer, formatDuration(state.elapsedMillis)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.game_cages, game.puzzle.cages.size.toString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            GameGrid(
                game = game,
                selectedCell = state.selectedCell,
                selectedCells = state.selectedCells,
                notes = state.notes,
                mistakes = if (state.progress.errorHighlightEnabled) state.mistakes else emptySet(),
                boardTheme = state.progress.selectedTheme,
                onCellClick = onCellClick,
                onCellsSelected = onCellsSelected,
                modifier = Modifier.fillMaxWidth(),
            )

            NumberKeypad(
                cageCombinations = state.cageCombinations,
                cageSelectionTotal = state.selectedCageTotal,
                inactiveCombinations = state.selectedCageId?.let { state.inactiveCombinations[it] }.orEmpty(),
                inactiveNumbers = state.selectedCell?.let { state.inactiveNumbers[it] }.orEmpty(),
                canUndo = state.canUndo,
                onCombination = onCombination,
                onNumber = onNumber,
                onErase = onErase,
                onHint = onHint,
                onUndo = onUndo,
            )

            state.message?.let {
                Text(
                    text = stringResource(it.resId, *it.args.toTypedArray()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun EmptyGame(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.game_empty))
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.action_home))
        }
    }
}

@Composable
private fun CompletionTitle() {
    val transition = rememberInfiniteTransition(label = "completion")
    val scale = transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "completionScale",
    )
    Text(
        text = stringResource(R.string.game_completion_title),
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    )
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}
