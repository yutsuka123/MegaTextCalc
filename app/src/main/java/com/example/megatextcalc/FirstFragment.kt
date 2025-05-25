package com.example.megatextcalc

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.megatextcalc.databinding.FragmentFirstBinding
import java.text.DecimalFormat
import kotlin.math.roundToInt

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // 電卓の状態を管理する変数
    private var currentInput = ""
    private var firstOperand = 0.0
    private var operator = ""
    private var isOperatorClicked = false
    private var isCalculated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 数字ボタンのセットアップ
        setupNumberButtons()

        // 演算子ボタンのセットアップ
        setupOperatorButtons()

        // 小数点ボタンのセットアップ
        setupDecimalButton()

        // クリアボタンのセットアップ
        setupClearButton()

        // イコールボタンのセットアップ
        setupEqualButton()
    }

    private fun setupNumberButtons() {
        // 数字ボタンの設定
        val numberButtons = mapOf(
            binding.btn0 to "0",
            binding.btn1 to "1",
            binding.btn2 to "2",
            binding.btn3 to "3",
            binding.btn4 to "4",
            binding.btn5 to "5",
            binding.btn6 to "6",
            binding.btn7 to "7",
            binding.btn8 to "8",
            binding.btn9 to "9"
        )

        // 各数字ボタンにクリックリスナーを設定
        for ((button, digit) in numberButtons) {
            button.setOnClickListener {
                // 計算後に数字をタップした場合は表示をリセット
                if (isCalculated) {
                    resetCalculator()
                }

                // 演算子をタップした後なら入力をリセット
                if (isOperatorClicked) {
                    currentInput = ""
                    isOperatorClicked = false
                }

                // 入力を追加
                currentInput += digit
                updateDisplay()
            }
        }
    }

    private fun setupDecimalButton() {
        // 小数点ボタンの設定
        binding.btnDecimal.setOnClickListener {
            // 計算後に小数点をタップした場合は表示をリセット
            if (isCalculated) {
                resetCalculator()
                currentInput = "0."
            } else {
                // 演算子をタップした後なら入力をリセット
                if (isOperatorClicked) {
                    currentInput = "0."
                    isOperatorClicked = false
                } else if (currentInput.isEmpty()) {
                    // 何も入力されていない場合は0.を入力
                    currentInput = "0."
                } else if (!currentInput.contains(".")) {
                    // 既に小数点がある場合は追加しない
                    currentInput += "."
                }
            }
            updateDisplay()
        }
    }

    private fun setupOperatorButtons() {
        // 演算子ボタンの設定
        val operatorButtons = mapOf(
            binding.btnPlus to "+",
            binding.btnMinus to "-",
            binding.btnMultiply to "×",
            binding.btnDivide to "÷"
        )

        // 各演算子ボタンにクリックリスナーを設定
        for ((button, op) in operatorButtons) {
            button.setOnClickListener {
                if (currentInput.isNotEmpty() || operator.isNotEmpty()) {
                    // 既に演算子が選択されている場合は前の計算を実行
                    if (operator.isNotEmpty() && !isOperatorClicked && currentInput.isNotEmpty()) {
                        calculateResult()
                    }

                    // 新しい演算子を設定
                    if (currentInput.isNotEmpty()) {
                        firstOperand = currentInput.toDouble()
                    }
                    operator = op
                    isOperatorClicked = true
                    isCalculated = false
                    updateExpression()
                }
            }
        }
    }

    private fun setupClearButton() {
        // クリアボタンの設定
        binding.btnClear.setOnClickListener {
            resetCalculator()
            updateDisplay()
            updateExpression()
        }
    }

    private fun setupEqualButton() {
        // イコールボタンの設定
        binding.btnEqual.setOnClickListener {
            if (currentInput.isNotEmpty() && operator.isNotEmpty()) {
                calculateResult()
                isCalculated = true
            }
        }
    }

    private fun calculateResult() {
        // 計算処理
        val secondOperand = currentInput.toDouble()
        var result = 0.0

        when (operator) {
            "+" -> result = firstOperand + secondOperand
            "-" -> result = firstOperand - secondOperand
            "×" -> result = firstOperand * secondOperand
            "÷" -> {
                if (secondOperand != 0.0) {
                    result = firstOperand / secondOperand
                } else {
                    binding.textviewResult.text = getString(R.string.calc_error)
                    return
                }
            }
        }

        // 結果を表示
        val formatter = DecimalFormat("#.##########")
        val formattedResult: String

        // 整数の場合は小数点以下を表示しない
        if (result == result.roundToInt().toDouble()) {
            formattedResult = result.roundToInt().toString()
        } else {
            formattedResult = formatter.format(result)
        }

        // 結果を画面に表示し、次の計算のために状態を更新
        binding.textviewResult.text = formattedResult
        currentInput = formattedResult
        firstOperand = result
        updateExpression()
    }

    private fun updateDisplay() {
        // 結果表示の更新
        binding.textviewResult.text = if (currentInput.isEmpty()) "0" else currentInput
    }

    private fun updateExpression() {
        // 式の表示を更新
        val expression = if (operator.isEmpty()) {
            currentInput
        } else {
            "$firstOperand $operator ${if (isOperatorClicked) "" else currentInput}"
        }
        binding.textviewExpression.text = expression
    }

    private fun resetCalculator() {
        // 電卓の状態をリセット
        currentInput = ""
        firstOperand = 0.0
        operator = ""
        isOperatorClicked = false
        isCalculated = false
        binding.textviewResult.text = "0"
        binding.textviewExpression.text = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

