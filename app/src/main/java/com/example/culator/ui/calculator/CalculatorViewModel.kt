package com.example.culator.ui.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CalculatorViewModel : ViewModel() {

    var displayText by mutableStateOf("0")
        private set

    private var firstOperand: Double? = null
    private var pendingOperation: String? = null
    private var isNewInput = true

    private val secretCode = "79560"
    private val _navigateToSecret = MutableSharedFlow<Unit>()
    val navigateToSecret = _navigateToSecret.asSharedFlow()

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Operation -> enterOperation(action.operation)
            CalculatorAction.Calculate -> calculate()
            CalculatorAction.Clear -> clear()
            CalculatorAction.Decimal -> enterDecimal()
        }
    }

    private fun enterNumber(number: Int) {
        if (isNewInput || displayText == "0") {
            displayText = number.toString()
            isNewInput = false
        } else {
            displayText += number.toString()
        }

        if (displayText == secretCode) {
            viewModelScope.launch {
                _navigateToSecret.emit(Unit)
            }
        }
    }

    private fun enterOperation(operation: String) {
        firstOperand = displayText.toDoubleOrNull()
        pendingOperation = operation
        isNewInput = true
    }

    private fun calculate() {
        val secondOperand = displayText.toDoubleOrNull() ?: return
        val first = firstOperand ?: return
        val op = pendingOperation ?: return

        val result = when (op) {
            "+" -> first + secondOperand
            "-" -> first - secondOperand
            "*" -> first * secondOperand
            "/" -> if (secondOperand != 0.0) first / secondOperand else Double.NaN
            else -> return
        }

        displayText = if (result % 1 == 0.0) result.toLong().toString() else result.toString()
        firstOperand = null
        pendingOperation = null
        isNewInput = true
    }

    private fun clear() {
        displayText = "0"
        firstOperand = null
        pendingOperation = null
        isNewInput = true
    }

    private fun enterDecimal() {
        if (!displayText.contains(".")) {
            displayText += "."
            isNewInput = false
        }
    }
}

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    data class Operation(val operation: String) : CalculatorAction()
    object Clear : CalculatorAction()
    object Calculate : CalculatorAction()
    object Decimal : CalculatorAction()
}
