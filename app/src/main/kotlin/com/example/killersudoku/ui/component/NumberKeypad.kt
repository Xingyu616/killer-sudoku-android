package com.example.killersudoku.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.killersudoku.R
import com.example.killersudoku.viewmodel.CageSelectionTotal

@Composable
fun NumberKeypad(
    cageCombinations: List<String>,
    cageSelectionTotal: CageSelectionTotal?,
    inactiveCombinations: Set<String>,
    inactiveNumbers: Set<Int>,
    canUndo: Boolean,
    canRedo: Boolean,
    onCombination: (String) -> Unit,
    onNumber: (Int) -> Unit,
    onErase: () -> Unit,
    onHint: () -> Unit,
    onSolve: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(KeypadMode.COMBINATION) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(166.dp),
        color = Color(0xFF202936),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ModeRail(
                selectedMode = mode,
                onModeChange = { mode = it },
            )
            FeaturePanel(
                mode = mode,
                cageCombinations = cageCombinations,
                cageSelectionTotal = cageSelectionTotal,
                inactiveCombinations = inactiveCombinations,
                onCombination = onCombination,
                onCheck = onCheck,
                modifier = Modifier.weight(1f),
            )
            ActionColumn(
                canUndo = canUndo,
                canRedo = canRedo,
                onErase = onErase,
                onHint = onHint,
                onSolve = onSolve,
                onUndo = onUndo,
                onRedo = onRedo,
            )
            NumberGrid(
                inactiveNumbers = inactiveNumbers,
                onNumber = onNumber,
            )
        }
    }
}

private enum class KeypadMode {
    COMBINATION,
    MENU,
}

@Composable
private fun ModeRail(
    selectedMode: KeypadMode,
    onModeChange: (KeypadMode) -> Unit,
) {
    Column(
        modifier = Modifier.width(38.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ModeButton(
            label = stringResource(R.string.keypad_mode_comb),
            selected = selectedMode == KeypadMode.COMBINATION,
            onClick = { onModeChange(KeypadMode.COMBINATION) },
            modifier = Modifier.weight(1f),
        )
        ModeButton(
            label = stringResource(R.string.keypad_mode_menu),
            selected = selectedMode == KeypadMode.MENU,
            onClick = { onModeChange(KeypadMode.MENU) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (selected) Color(0xFF324156) else Color(0xFF1A222E))
            .border(1.dp, Color.White.copy(alpha = if (selected) 0.18f else 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.58f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ActionColumn(
    canUndo: Boolean,
    canRedo: Boolean,
    onErase: () -> Unit,
    onHint: () -> Unit,
    onSolve: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    Column(
        modifier = Modifier.width(54.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CompactAction(
                label = stringResource(R.string.keypad_undo),
                enabled = canUndo,
                onClick = onUndo,
                modifier = Modifier.weight(1f),
            )
            CompactAction(
                label = stringResource(R.string.keypad_redo),
                enabled = canRedo,
                onClick = onRedo,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CompactAction(
                label = stringResource(R.string.keypad_delete),
                enabled = true,
                onClick = onErase,
                modifier = Modifier.weight(1f),
            )
            CompactAction(
                label = stringResource(R.string.keypad_hint),
                enabled = true,
                onClick = onHint,
                modifier = Modifier.weight(1f),
            )
        }
        CompactAction(
            label = stringResource(R.string.keypad_smart_hint),
            enabled = true,
            onClick = onSolve,
            modifier = Modifier.fillMaxWidth(),
            height = 78,
        )
    }
}

@Composable
private fun CompactAction(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 32,
) {
    Box(
        modifier = modifier
            .height(height.dp)
            .background(if (enabled) Color(0xFF2B3544) else Color(0xFF1A202A))
            .border(1.dp, Color.White.copy(alpha = 0.08f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (enabled) Color.White.copy(alpha = 0.86f) else Color.White.copy(alpha = 0.28f),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FeaturePanel(
    mode: KeypadMode,
    cageCombinations: List<String>,
    cageSelectionTotal: CageSelectionTotal?,
    inactiveCombinations: Set<String>,
    onCombination: (String) -> Unit,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (mode) {
        KeypadMode.COMBINATION -> CandidatePanel(
            cageCombinations = cageCombinations,
            cageSelectionTotal = cageSelectionTotal,
            inactiveCombinations = inactiveCombinations,
            onCombination = onCombination,
            modifier = modifier,
        )

        KeypadMode.MENU -> MenuPanel(
            onCheck = onCheck,
            modifier = modifier,
        )
    }
}

@Composable
private fun CandidatePanel(
    cageCombinations: List<String>,
    cageSelectionTotal: CageSelectionTotal?,
    inactiveCombinations: Set<String>,
    onCombination: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (cageSelectionTotal != null) {
        CageTotalPanel(
            total = cageSelectionTotal,
            modifier = modifier.height(150.dp),
        )
    } else {
        LazyColumn(
            modifier = modifier.height(150.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (cageCombinations.isNotEmpty()) {
                items(cageCombinations) { combination ->
                    CombinationBox(
                        text = combination,
                        isInactive = combination in inactiveCombinations,
                        onClick = { onCombination(combination) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CageTotalPanel(
    total: CageSelectionTotal,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF263140))
            .border(1.dp, Color.White.copy(alpha = 0.08f))
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.keypad_cage_total, total.totalSum.toString()),
                color = Color(0xFFFFEE75),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(
                    R.string.keypad_cage_total_detail,
                    total.completeCageCount.toString(),
                    total.extraFilledCount.toString(),
                ),
                color = Color.White.copy(alpha = 0.62f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MenuPanel(
    onCheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(150.dp)
            .fillMaxWidth()
            .background(Color(0xFF263140))
            .border(1.dp, Color.White.copy(alpha = 0.08f))
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        MenuActionButton(
            label = stringResource(R.string.action_check),
            onClick = onCheck,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MenuActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(63.dp)
            .background(Color(0xFF2B3544))
            .border(1.dp, Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.86f),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CombinationBox(
    text: String,
    isInactive: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(31.dp)
            .background(if (isInactive) Color(0xFF171D27) else Color(0xFF263140))
            .clickable(enabled = text.isNotBlank(), onClick = onClick)
            .border(1.dp, Color.White.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (isInactive) Color.White.copy(alpha = 0.22f) else Color(0xFFFFEE75),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NumberGrid(
    inactiveNumbers: Set<Int>,
    onNumber: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.width(126.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                repeat(3) { col ->
                    val number = row * 3 + col + 1
                    NumberButton(
                        number = number,
                        isInactive = number in inactiveNumbers,
                        onClick = { onNumber(number) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    isInactive: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(if (isInactive) Color(0xFF171D27) else Color(0xFF253040))
            .border(1.dp, Color.White.copy(alpha = 0.10f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = if (isInactive) Color.White.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.82f),
            fontSize = 30.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
        )
    }
}
