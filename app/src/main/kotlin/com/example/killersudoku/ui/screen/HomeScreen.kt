package com.example.killersudoku.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.killersudoku.R
import com.example.killersudoku.domain.model.DifficultyStats
import com.example.killersudoku.viewmodel.GameUiState

@Composable
fun HomeScreen(
    state: GameUiState,
    onContinue: () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Killer Sudoku",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = state.game?.let {
                    stringResource(
                        R.string.home_latest_game,
                        it.puzzle.difficulty.title,
                        stringResource(
                            if (it.isCompleted) {
                                R.string.home_completed_suffix
                            } else {
                                R.string.home_in_progress_suffix
                            },
                        ),
                    )
                } ?: stringResource(R.string.home_start_prompt),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatsPanel(state = state)
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onNewGame,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.home_new_game))
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onContinue,
                enabled = state.game != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.home_continue_game))
            }
        }
    }
}

@Composable
private fun StatsPanel(
    state: GameUiState,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.home_recent_result),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            val latest = state.stats.latestCompleted
            Text(
                text = latest?.let {
                    "${it.difficulty.title} · ${formatDuration(it.elapsedMillis)}"
                } ?: stringResource(R.string.home_no_results),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.home_best_times),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            state.stats.difficultyStats.forEach { stats ->
                DifficultyStatRow(stats = stats)
            }
        }
    }
}

@Composable
private fun DifficultyStatRow(stats: DifficultyStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stats.difficulty.title,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stats.bestTimeMillis?.let {
                "${formatDuration(it)} · ${stringResource(R.string.home_games_count, stats.completedGames.toString())}"
            } ?: stringResource(R.string.home_no_results),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}
