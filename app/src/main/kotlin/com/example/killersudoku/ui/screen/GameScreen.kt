package com.example.killersudoku.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onCombination: (String) -> Unit,
    onNumber: (Int) -> Unit,
    onErase: () -> Unit,
    onHint: () -> Unit,
    onCheck: () -> Unit,
    onSolve: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(state.game?.puzzle?.difficulty?.title ?: "游戏") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("首页")
                    }
                },
                actions = {
                    TextButton(onClick = onCheck, enabled = state.game != null) {
                        Text("检查")
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
                    text = if (game.isCompleted) "已完成" else "进行中",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (game.isCompleted) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
                Text(
                    text = "笼区 ${game.puzzle.cages.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            GameGrid(
                game = game,
                selectedCell = state.selectedCell,
                notes = state.notes,
                mistakes = state.mistakes,
                onCellClick = onCellClick,
                modifier = Modifier.fillMaxWidth(),
            )

            NumberKeypad(
                cageCombinations = state.cageCombinations,
                inactiveCombinations = state.selectedCageId?.let { state.inactiveCombinations[it] }.orEmpty(),
                inactiveNumbers = state.selectedCell?.let { state.inactiveNumbers[it] }.orEmpty(),
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                onCombination = onCombination,
                onNumber = onNumber,
                onErase = onErase,
                onHint = onHint,
                onSolve = onSolve,
                onUndo = onUndo,
                onRedo = onRedo,
            )

            state.message?.let {
                Text(
                    text = it,
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
        Text("还没有游戏")
        TextButton(onClick = onBack) {
            Text("返回首页")
        }
    }
}
