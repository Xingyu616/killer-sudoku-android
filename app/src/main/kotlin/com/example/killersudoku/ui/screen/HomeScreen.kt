package com.example.killersudoku.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                text = "杀手数独",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = state.game?.let {
                    "最近一局：${it.puzzle.difficulty.title}${if (it.isCompleted) "，已完成" else "，进行中"}"
                } ?: "选择难度开始一局新的挑战",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onNewGame,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("新游戏")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onContinue,
                enabled = state.game != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("继续游戏")
            }
        }
    }
}
