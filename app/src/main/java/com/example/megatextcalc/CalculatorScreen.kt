package com.example.megatextcalc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement /* ここだけ1箇所 */
import androidx.compose.foundation.layout.RowScope /* weight を使うならこれ */
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment  /* 1箇所 */
import androidx.compose.ui.Modifier   /* 1箇所 */
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview /* 1箇所 */
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@androidx.compose.runtime.Composable
fun CalculatorScreen() {
    var expression by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var result by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        // 上半分: 計算式と結果表示
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 画面の上半分を占めるように
                .padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = expression,
                fontSize = 24.sp, // デカ文字対応
                modifier = Modifier.weight(1f) // 左側
            )
            Text(
                text = result,
                fontSize = 32.sp, // デカ文字対応、結果はより大きく
                modifier = Modifier.weight(1f) // 右側
                    .wrapContentWidth(Alignment.End) // 右寄せ
            )
        }

        // 下半分: 電卓ボタン
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 画面の下半分を占めるように
        ) {
            // ここに電卓ボタンのグリッドなどを配置します
            // 例:
            CalculatorButtonsLayout(
                onButtonPressed = { buttonValue ->
                    // ボタンが押されたときのロジック
                    // expression や result を更新
                    // TODO: 計算ロジックとリアルタイム計算を実装
                    if (buttonValue == "=") {
                        // TODO: 計算実行
                        // result = evaluateExpression(expression)
                    } else if (buttonValue == "C") {
                        expression = ""
                        result = ""
                    } else {
                        expression += buttonValue
                        // TODO: リアルタイム計算
                        // if (isValidPartialExpression(expression)) {
                        //     result = evaluateExpression(expression)
                        // }
                    }
                }
            )
        }
    }
}

@androidx.compose.runtime.Composable
fun CalculatorButtonsLayout(onButtonPressed: (String) -> Unit) {
    // 簡単なボタンの例
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
    ) {
        androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            CalculatorButton("7", onButtonPressed)
            CalculatorButton("8", onButtonPressed)
            CalculatorButton("9", onButtonPressed)
            CalculatorButton("/", onButtonPressed)
        }
        androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            CalculatorButton("4", onButtonPressed)
            CalculatorButton("5", onButtonPressed)
            CalculatorButton("6", onButtonPressed)
            CalculatorButton("*", onButtonPressed)
        }
        androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            CalculatorButton("1", onButtonPressed)
            CalculatorButton("2", onButtonPressed)
            CalculatorButton("3", onButtonPressed)
            CalculatorButton("-", onButtonPressed)
        }
        androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            CalculatorButton("C", onButtonPressed)
            CalculatorButton("0", onButtonPressed)
            CalculatorButton("=", onButtonPressed)
            CalculatorButton("+", onButtonPressed)
        }
    }
}

@androidx.compose.runtime.Composable
fun CalculatorButton(text: String, onClick: (String) -> Unit) {
    Button(
        onClick = { onClick(text) },
        modifier = Modifier
            .padding(4.dp)
            .sizeIn(minWidth = 64.dp, minHeight = 64.dp) // ボタンの最小サイズ
    ) {
        Text(text, fontSize = 20.sp) // デカ文字対応
    }
}

@Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun DefaultPreview() {
    CalculatorScreen()
}