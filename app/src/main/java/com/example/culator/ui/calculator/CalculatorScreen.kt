package com.example.culator.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onNavigateToSecret: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = Unit) {
        viewModel.navigateToSecret.collect {
            onNavigateToSecret()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = viewModel.displayText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            textAlign = TextAlign.End,
            color = Color.White,
            fontSize = 80.sp,
            fontWeight = FontWeight.Light,
            maxLines = 1
        )

        val buttons = listOf(
            listOf("C", "±", "%", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { char ->
                    val modifier = if (char == "0") {
                        Modifier.weight(2f)
                    } else {
                        Modifier.weight(1f)
                    }

                    CalculatorButton(
                        symbol = char,
                        modifier = modifier.aspectRatio(if (char == "0") 2f else 1f),
                        color = getButtonColor(char),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            when (char) {
                                "C" -> viewModel.onAction(CalculatorAction.Clear)
                                "=" -> viewModel.onAction(CalculatorAction.Calculate)
                                "." -> viewModel.onAction(CalculatorAction.Decimal)
                                "+", "-", "*", "/" -> viewModel.onAction(CalculatorAction.Operation(char))
                                else -> char.toIntOrNull()?.let {
                                    viewModel.onAction(CalculatorAction.Number(it))
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    modifier: Modifier = Modifier,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() }
            .then(modifier)
    ) {
        Text(
            text = symbol,
            fontSize = 32.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getButtonColor(symbol: String): Color {
    return when (symbol) {
        "C", "±", "%" -> Color(0xFFA5A5A5)
        "/", "*", "-", "+", "=" -> Color(0xFFFF9F0A)
        else -> Color(0xFF333333)
    }
}
