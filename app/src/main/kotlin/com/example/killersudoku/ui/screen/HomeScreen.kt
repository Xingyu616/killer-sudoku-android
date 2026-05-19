package com.example.killersudoku.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.killersudoku.R
import com.example.killersudoku.domain.model.BoardTheme
import com.example.killersudoku.domain.model.DifficultyStats
import com.example.killersudoku.domain.model.HINT_TICKET_COST
import com.example.killersudoku.ui.component.GuideDialog
import com.example.killersudoku.viewmodel.GameUiState
import java.time.LocalDate

private enum class HomeDialog {
    GUIDE,
    RECORDS,
    BAG,
    SETTINGS,
}

@Composable
fun HomeScreen(
    state: GameUiState,
    onContinue: () -> Unit,
    onNewGame: () -> Unit,
    onDailyCheckIn: () -> Unit,
    onPurchaseHintTicket: () -> Unit,
    onBgmChanged: (Boolean) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onAutoClearNotesChanged: (Boolean) -> Unit,
    onErrorHighlightChanged: (Boolean) -> Unit,
    onSelectTheme: (BoardTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialog by rememberSaveable { mutableStateOf<HomeDialog?>(null) }
    val checkedToday = state.progress.lastCheckInDate == LocalDate.now().toString()

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopStatusBar(
                state = state,
                checkedToday = checkedToday,
                onDailyCheckIn = onDailyCheckIn,
            )

            MainMenu(
                state = state,
                onContinue = onContinue,
                onNewGame = onNewGame,
                modifier = Modifier.fillMaxWidth(),
            )

            QuickActions(
                bgmEnabled = state.progress.bgmEnabled,
                onGuide = { dialog = HomeDialog.GUIDE },
                onRecords = { dialog = HomeDialog.RECORDS },
                onBag = { dialog = HomeDialog.BAG },
                onSettings = { dialog = HomeDialog.SETTINGS },
                onBgm = { onBgmChanged(!state.progress.bgmEnabled) },
            )
        }
    }

    when (dialog) {
        HomeDialog.GUIDE -> GuideDialog(
            onDismiss = { dialog = null },
        )

        HomeDialog.RECORDS -> RecordsDialog(
            state = state,
            onDismiss = { dialog = null },
        )

        HomeDialog.BAG -> BagDialog(
            state = state,
            onPurchaseHintTicket = onPurchaseHintTicket,
            onSelectTheme = onSelectTheme,
            onDismiss = { dialog = null },
        )

        HomeDialog.SETTINGS -> SettingsDialog(
            state = state,
            onBgmChanged = onBgmChanged,
            onSoundChanged = onSoundChanged,
            onAutoClearNotesChanged = onAutoClearNotesChanged,
            onErrorHighlightChanged = onErrorHighlightChanged,
            onDismiss = { dialog = null },
        )

        null -> Unit
    }
}

@Composable
private fun TopStatusBar(
    state: GameUiState,
    checkedToday: Boolean,
    onDailyCheckIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(R.string.reward_coins, state.progress.coins.toString()),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    R.string.reward_inventory_summary,
                    state.progress.hintTickets.toString(),
                    state.progress.checkInStreak.toString(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = onDailyCheckIn,
            enabled = !checkedToday,
        ) {
            Text(stringResource(if (checkedToday) R.string.reward_checked_today else R.string.reward_check_in))
        }
    }
}

@Composable
private fun MainMenu(
    state: GameUiState,
    onContinue: () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Killer Sudoku",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.height(10.dp))
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onNewGame,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.home_new_game))
        }
        OutlinedButton(
            onClick = onContinue,
            enabled = state.game != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.home_continue_game))
        }
        state.dailyCheckInReward?.let { reward ->
            Text(
                text = stringResource(R.string.reward_check_in_result, reward.coins.toString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun QuickActions(
    bgmEnabled: Boolean,
    onGuide: () -> Unit,
    onRecords: () -> Unit,
    onBag: () -> Unit,
    onSettings: () -> Unit,
    onBgm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MenuButton(
            label = stringResource(R.string.home_guide),
            onClick = onGuide,
            modifier = Modifier.weight(1f),
        )
        MenuButton(
            label = stringResource(R.string.home_records),
            onClick = onRecords,
            modifier = Modifier.weight(1f),
        )
        MenuButton(
            label = stringResource(R.string.home_bag),
            onClick = onBag,
            modifier = Modifier.weight(1f),
        )
        MenuButton(
            label = stringResource(R.string.home_settings),
            onClick = onSettings,
            modifier = Modifier.weight(1f),
        )
        MenuButton(
            label = stringResource(if (bgmEnabled) R.string.home_bgm_on else R.string.home_bgm_off),
            onClick = onBgm,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MenuButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RecordsDialog(
    state: GameUiState,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_records)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val latest = state.stats.latestCompleted
                Text(
                    text = latest?.let {
                        "${stringResource(R.string.home_recent_result)}：${it.difficulty.title} · ${formatDuration(it.elapsedMillis)}"
                    } ?: stringResource(R.string.home_no_results),
                    style = MaterialTheme.typography.bodyMedium,
                )
                state.stats.difficultyStats.forEach { stats ->
                    DifficultyStatRow(stats = stats)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
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

@Composable
private fun BagDialog(
    state: GameUiState,
    onPurchaseHintTicket: () -> Unit,
    onSelectTheme: (BoardTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_bag)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.reward_coins, state.progress.coins.toString()))
                Text(stringResource(R.string.reward_hint_tickets, state.progress.hintTickets.toString()))
                Button(onClick = onPurchaseHintTicket) {
                    Text(stringResource(R.string.reward_buy_hint_ticket, HINT_TICKET_COST.toString()))
                }
                Text(
                    text = stringResource(R.string.theme_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                BoardTheme.entries.forEach { theme ->
                    ThemeRow(
                        theme = theme,
                        selected = state.progress.selectedTheme == theme,
                        unlocked = theme in state.progress.unlockedThemes,
                        onSelectTheme = onSelectTheme,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}

@Composable
private fun ThemeRow(
    theme: BoardTheme,
    selected: Boolean,
    unlocked: Boolean,
    onSelectTheme: (BoardTheme) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(theme.title)
            Text(
                text = if (unlocked) {
                    stringResource(R.string.theme_unlocked)
                } else {
                    stringResource(R.string.theme_unlock_cost, theme.unlockCost.toString())
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = { onSelectTheme(theme) }) {
            Text(
                when {
                    selected -> stringResource(R.string.theme_selected)
                    unlocked -> stringResource(R.string.theme_select)
                    else -> stringResource(R.string.theme_unlock)
                },
            )
        }
    }
}

@Composable
private fun SettingsDialog(
    state: GameUiState,
    onBgmChanged: (Boolean) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onAutoClearNotesChanged: (Boolean) -> Unit,
    onErrorHighlightChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingSwitchRow(
                    label = stringResource(R.string.home_bgm),
                    checked = state.progress.bgmEnabled,
                    onCheckedChange = onBgmChanged,
                )
                SettingSwitchRow(
                    label = stringResource(R.string.settings_sound),
                    checked = state.progress.soundEnabled,
                    onCheckedChange = onSoundChanged,
                )
                SettingSwitchRow(
                    label = stringResource(R.string.settings_auto_clear_notes),
                    checked = state.progress.autoClearNotes,
                    onCheckedChange = onAutoClearNotesChanged,
                )
                SettingSwitchRow(
                    label = stringResource(R.string.settings_error_highlight),
                    checked = state.progress.errorHighlightEnabled,
                    onCheckedChange = onErrorHighlightChanged,
                )
                Text(
                    text = stringResource(R.string.home_settings_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
    )
}

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}
