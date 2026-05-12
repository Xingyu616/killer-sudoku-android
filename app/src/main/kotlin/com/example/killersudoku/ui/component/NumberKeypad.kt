package com.example.killersudoku.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberKeypad(
    noteMode: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onNumber: (Int) -> Unit,
    onErase: () -> Unit,
    onToggleNote: () -> Unit,
    onHint: () -> Unit,
    onSolve: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(3) { col ->
                    val number = row * 3 + col + 1
                    Button(
                        onClick = { onNumber(number) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                    ) {
                        Text(number.toString())
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = noteMode,
                onClick = onToggleNote,
                label = { Text("笔记") },
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = onErase,
                modifier = Modifier.weight(1f),
            ) {
                Text("删除")
            }
            OutlinedButton(
                onClick = onHint,
                modifier = Modifier.weight(1f),
            ) {
                Text("提示")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.weight(1f),
            ) {
                Text("撤销")
            }
            OutlinedButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.weight(1f),
            ) {
                Text("重做")
            }
            OutlinedButton(
                onClick = onSolve,
                modifier = Modifier.weight(1f),
            ) {
                Text("答案")
            }
        }
    }
}
