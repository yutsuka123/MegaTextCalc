// このファイルには元の実装が保存されています。
// UI改修で問題が発生した場合に参照・復元できるよう保管しています。
// 元々のCalculatorScreen.ktの内容

package com.nyangailab.nyancalc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.nyangailab.nyancalc.util.PreferencesHelper
import com.nyangailab.nyancalc.util.SoundPlayer
import java.util.*
import kotlin.math.max
import kotlin.math.min

@androidx.compose.runtime.Composable
fun OldCalculatorScreen() {
    val context = LocalContext.current
    val prefsHelper = remember { PreferencesHelper(context) }

    // 画面のサイズを取得
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // 画面サイズに応じたスケール係数を計算（基準：Pixel 9 Pro XL）
    val baseWidth = 480f // Pixel 9 Pro XLの幅（仮の値）
    val baseHeight = 960f // Pixel 9 Pro XLの高さ（仮の値）
    val widthScale = configuration.screenWidthDp / baseWidth
    val heightScale = configuration.screenHeightDp / baseHeight
    val scale = min(widthScale, heightScale) // 小さい方のスケールを採用

    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var memory1 by remember { mutableStateOf("") }
    var memory2 by remember { mutableStateOf("") }
    var memoryToggle by remember { mutableStateOf(true) } // true: M1, false: M2
    var lastOperator by remember { mutableStateOf("") }
    var lastNumber by remember { mutableStateOf(0.0) }
    var newNumberInput by remember { mutableStateOf(true) }
    var lastResultCalculated by remember { mutableStateOf(false) } // 計算結果が出たかどうかのフラグ

    // 設定から文字数制限と数値範囲を取得
    val charLimit = prefsHelper.charLimit
    val minValue = prefsHelper.getNumberRangeMin()
    val maxValue = prefsHelper.getNumberRangeMax()
    val decimalPlaces = prefsHelper.getDecimalPlaces()

    // 範囲チェック関数 - 設定に基づいた範囲チェック
    fun isInValidRange(value: Double): Boolean {
        return value >= minValue && value <= maxValue
    }

    // 入力値が範囲内か判定（小数点対応）
    fun canAppendNumber(current: String, append: String): Boolean {
        // 既存の式が演算子で終わっている場合は常に許可
        if (current.isEmpty() || current.endsWith("+") || current.endsWith("-") || current.endsWith("*") || current.endsWith("/")) return true

        // 直近の数値部分を抽出
        val lastNumber = current.split(Regex("[+\\-*/]"), 0).lastOrNull() ?: ""

        // 追加後の数値を作成
        val newNumber = if (lastNumber == "0" && append != ".") append else lastNumber + append

        // 小数点のみは許可
        if (newNumber == "." || newNumber == "-." || newNumber == "") return true

        // 小数点以下の桁数チェック（設定に基づく）
        if (newNumber.contains(".")) {
            val decimalPart = newNumber.substringAfter(".")
            if (decimalPart.length > decimalPlaces) return false
        }

        // 先頭0の制御
        val normalized = if (newNumber.startsWith("0") && !newNumber.startsWith("0.")) newNumber.trimStart('0') else newNumber

        // 範囲チェック - 設定に基づいて数値の範囲をチェック
        try {
            val value = normalized.toDouble()
            return value >= minValue && value <= maxValue
        } catch (e: NumberFormatException) {
            return false
        }
    }

    // 結果を適切にフォーマット - 小数点以下の桁数を設定に基づいて調整
    fun formatResult(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            // 整数の場合は小数点以下を表示しない
            String.format("%,d", value.toInt())
        } else {
            // 小数点以下がある場合は設定された桁数まで表示（末尾の0は削除）
            String.format("%,.${decimalPlaces}f", value).trimEnd('0').trimEnd('.')
        }
    }

    // 改善した計算ロジック - 四則演算の優先順位に対応
    fun evaluateExpression(expr: String): String {
        try {
            // 複雑な計算式を評価するためのヘルパー関数
            fun evaluateExprWithPriority(expression: String): Double {
                // 1. 数値と演算子を抽出
                val tokens = mutableListOf<String>()
                var currentNumber = ""

                for (char in expression) {
                    if (char.isDigit() || char == '.') {
                        currentNumber += char
                    } else if (char in setOf('+', '-', '*', '/')) {
                        if (currentNumber.isNotEmpty()) {
                            tokens.add(currentNumber)
                            currentNumber = ""
                        }
                        tokens.add(char.toString())
                    }
                }

                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber)
                }

                // 2. 掛け算と割り算を先に計算
                var i = 0
                while (i < tokens.size) {
                    if (i + 2 < tokens.size && tokens[i + 1] in setOf("*", "/")) {
                        val left = tokens[i].toDouble()
                        val operator = tokens[i + 1]
                        val right = tokens[i + 2].toDouble()

                        val result = when (operator) {
                            "*" -> left * right
                            "/" -> {
                                if (right == 0.0) throw ArithmeticException("除算エラー")
                                left / right
                            }
                            else -> throw IllegalArgumentException("不明な演算子")
                        }

                        // 計算結果で置き換える
                        tokens[i] = result.toString()
                        // 使用済みの演算子と右オペランドを削除
                        tokens.removeAt(i + 1)
                        tokens.removeAt(i + 1)
                        // インデックスは進めない（同じ位置に新しい値がある）
                    } else {
                        // 掛け算/割り算でなければ次へ
                        i++
                    }
                }

                // 3. 足し算と引き算を計算
                i = 0
                while (tokens.size >= 3 && i + 2 < tokens.size) {
                    if (tokens[i + 1] in setOf("+", "-")) {
                        val left = tokens[i].toDouble()
                        val operator = tokens[i + 1]
                        val right = tokens[i + 2].toDouble()

                        val result = when (operator) {
                            "+" -> left + right
                            "-" -> left - right
                            else -> throw IllegalArgumentException("不明な演算子")
                        }

                        // 計算結果で置き換える
                        tokens[i] = result.toString()
                        // 使用済みの演算子と右オペランドを削除
                        tokens.removeAt(i + 1)
                        tokens.removeAt(i + 1)
                        // インデックスは進めない（同じ位置に新しい値がある）
                    } else {
                        // 想定外のケース
                        i++
                    }
                }

                // 最終結果
                return tokens[0].toDouble()
            }

            // 式を評価
            if (expr.contains("+") || expr.contains("-") || expr.contains("*") || expr.contains("/")) {
                val resultValue = evaluateExprWithPriority(expr)

                // 結果が範囲内かチェック
                if (isInValidRange(resultValue)) {
                    return formatResult(resultValue)
                }
                return "エラー" // 範囲外
            }

            // 単独の数値の場合
            val value = expr.toDouble()
            if (isInValidRange(value)) {
                return formatResult(value)
            }
            return "エラー" // 範囲外
        } catch (e: Exception) {
            return "エラー" // 構文エラーなど
        }
    }

    // 文字サイズを動的に計算する関数（スケーリング対応）
    fun calculateTextSize(text: String, isResult: Boolean, scale: Float): TextUnit {
        // ベースサイズにスケールを適用
        val baseSize = if (isResult) (36 * scale).sp else (30 * scale).sp
        val minSize = if (isResult) (20 * scale).sp else (16 * scale).sp

        // 改行を含むかチェック
        val hasNewLine = text.contains("\n")

        // 文字数に基づいたサイズ調整
        return when {
            text.length > 25 || hasNewLine -> minSize
            text.length > 20 -> (baseSize.value * 0.6f).sp
            text.length > 15 -> (baseSize.value * 0.8f).sp
            else -> baseSize
        }
    }

    // 文字列を必要に応じて改行する関数
    fun formatWithLineBreak(text: String, maxCharsPerLine: Int): String {
        if (text.length <= maxCharsPerLine) return text

        // 適切な位置で改行を入れる
        val breakPoint = maxCharsPerLine
        return text.substring(0, breakPoint) + "\n" + text.substring(breakPoint)
    }

    // 相対レイアウト用のパディングサイズを計算
    val basePadding = 8.dp * scale

    // メインのレイアウト - 相対サイズに変更
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(basePadding)
    ) {
        // 1行目: ロゴ＋アプリ名 (設定ボタンは削除 - MainActivityで対応)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.rogo),
                contentDescription = "ロゴ",
                modifier = Modifier.size((36 * scale).dp)
            )
            Spacer(modifier = Modifier.width((4 * scale).dp))
            Text("電卓", fontSize = (20 * scale).sp)
        }

        Spacer(modifier = Modifier.height((8 * scale).dp))

        // 2行目: メモリ1（高さを画面サイズに合わせて調整）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height((58 * scale).dp) // 高さをスケーリング
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .background(Color(0xFFF8F8F8))
                .padding(horizontal = (8 * scale).dp)
                .clickable(enabled = memory1.isNotEmpty()) {
                    // 演算子で終わっていたら追加、そうでなければ上書き
                    if (expression.isNotEmpty() && (expression.endsWith("+") || expression.endsWith("-") ||
                       expression.endsWith("*") || expression.endsWith("/"))) {
                        // カンマを除去してから追加
                        expression += memory1.replace(",", "")
                    } else {
                        // カンマを除去してから代入
                        expression = memory1.replace(",", "")
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "M1:",
                fontSize = (17 * scale).sp,
                color = Color.Gray,
                modifier = Modifier.width((28 * scale).dp)
            )

            // スケーリングを適用した動的文字サイズ
            val fontSize = if (memory1.length > 15) {
                // 文字数が多い場合は縮小
                val reductionFactor = when {
                    memory1.length > 25 -> 0.6f
                    memory1.length > 20 -> 0.7f
                    else -> 0.8f
                }
                ((19 * scale) * reductionFactor).sp
            } else {
                (19 * scale).sp // ベースサイズにスケール適用
            }

            Text(
                memory1,
                fontSize = fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 3行目: メモリ2（高さをスケーリング）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height((58 * scale).dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .background(Color(0xFFF8F8F8))
                .padding(horizontal = (8 * scale).dp)
                .clickable(enabled = memory2.isNotEmpty()) {
                    // 演算子で終わっていたら追加、そうでなければ上書き
                    if (expression.isNotEmpty() && (expression.endsWith("+") || expression.endsWith("-") ||
                       expression.endsWith("*") || expression.endsWith("/"))) {
                        // カンマを除去してから追加
                        expression += memory2.replace(",", "")
                    } else {
                        // カンマを除去してから代入
                        expression = memory2.replace(",", "")
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "M2:",
                fontSize = (17 * scale).sp,
                color = Color.Gray,
                modifier = Modifier.width((28 * scale).dp)
            )

            // スケーリングを適用した動的文字サイズ
            val fontSize = if (memory2.length > 15) {
                // 文字数が多い場合は縮小
                val reductionFactor = when {
                    memory2.length > 25 -> 0.6f
                    memory2.length > 20 -> 0.7f
                    else -> 0.8f
                }
                ((19 * scale) * reductionFactor).sp
            } else {
                (19 * scale).sp // ベースサイズにスケール適用
            }

            Text(
                memory2,
                fontSize = fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height((8 * scale).dp))

        // 4行目: 計算行（高さをスケーリング）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height((108 * scale).dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .background(Color.White)
                .padding(horizontal = (8 * scale).dp, vertical = (8 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "計算",
                fontSize = (17 * scale).sp,
                color = Color.Gray,
                modifier = Modifier.width((32 * scale).dp)
            )

            // スケーリングを考慮した改行ロジック
            val maxCharsPerLine = (20 / scale).toInt().coerceAtLeast(10) // 最小10文字は確保
            val formattedExpression = if (expression.length > maxCharsPerLine) {
                expression.substring(0, maxCharsPerLine) + "\n" + expression.substring(maxCharsPerLine)
            } else {
                expression
            }

            // 文字サイズをスケーリング
            val isTwoLine = formattedExpression.contains("\n")
            // 基本サイズ(1行表示時) - スケーリング適用
            val baseSize = if (formattedExpression.length <= 10) 36f * scale
                          else if (formattedExpression.length <= 15) 32f * scale
                          else if (formattedExpression.length <= 20) 28f * scale
                          else 24f * scale

            // 2行表示時は90%に縮小
            val fontSize = (if (isTwoLine) baseSize * 0.9f else baseSize).sp

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isTwoLine) {
                    // 2行表示の場合
                    Text(
                        text = formattedExpression,
                        fontSize = fontSize,
                        maxLines = 2,
                        overflow = TextOverflow.Visible,
                        lineHeight = (36 * scale).sp // 行間もスケーリング
                    )
                } else {
                    // 1行表示の場合
                    Text(
                        text = formattedExpression,
                        fontSize = fontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Visible
                    )
                }
            }
        }

        // 5行目: 結果行（高さをスケーリング）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height((108 * scale).dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .background(Color.White)
                .padding(horizontal = (8 * scale).dp, vertical = (8 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "結果",
                fontSize = (17 * scale).sp,
                color = Color.Gray,
                modifier = Modifier.width((32 * scale).dp)
            )

            // スケーリングを考慮した改行ロジック
            val maxCharsPerLine = (20 / scale).toInt().coerceAtLeast(10) // 最小10文字は確保
            val formattedResult = if (result.length > maxCharsPerLine) {
                result.substring(0, maxCharsPerLine) + "\n" + result.substring(maxCharsPerLine)
            } else {
                result
            }

            // 文字サイズをスケーリング
            val isTwoLine = formattedResult.contains("\n")
            // 基本サイズ(1行表示時) - スケーリング適用
            val baseSize = if (formattedResult.length <= 10) 38f * scale
                          else if (formattedResult.length <= 15) 34f * scale
                          else if (formattedResult.length <= 20) 30f * scale
                          else 26f * scale

            // 2行表示時は90%に縮小
            val fontSize = (if (isTwoLine) baseSize * 0.9f else baseSize).sp

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isTwoLine) {
                    // 2行表示の場合
                    Text(
                        text = formattedResult,
                        fontSize = fontSize,
                        maxLines = 2,
                        overflow = TextOverflow.Visible,
                        lineHeight = (36 * scale).sp // 行間もスケーリング
                    )
                } else {
                    // 1行表示の場合
                    Text(
                        text = formattedResult,
                        fontSize = fontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Visible
                    )
                }
            }
        }

        // 下部スペース確保（表示比率を維持）
        Spacer(modifier = Modifier.weight(0.05f))

        // ボタン部
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.95f)
                .padding(bottom = (2 * scale).dp),
            contentAlignment = Alignment.Center
        ) {
            // スケールを渡して電卓ボタンレイアウトを表示
            CalculatorButtonsLayout(
                scale = scale,
                onButtonPressed = { buttonValue ->
                    when (buttonValue) {
                        "AC" -> {
                            // ACボタンの処理: メモリ1、メモリ2もクリア
                            expression = ""
                            result = ""
                            memory1 = ""
                            memory2 = ""
                            lastOperator = ""
                            lastNumber = 0.0
                            newNumberInput = true
                            lastResultCalculated = false

                            // 音声再生
                            SoundPlayer.playAsset(context, "C.mp3")
                        }
                        "C" -> {
                            // 計算欄だけをクリア、結果は残す
                            expression = ""
                            lastOperator = ""
                            lastNumber = 0.0
                            newNumberInput = true
                            // lastResultCalculatedフラグは変更しない（結果をそのまま）

                            // 音声再生
                            SoundPlayer.playAsset(context, "C.mp3")
                        }
                        "." -> {
                            // 小数点処理: 既に小数点があれば何もしない
                            val lastNumber = expression.split(Regex("[+\\-*/]")).last()
                            if (!lastNumber.contains(".")) {
                                // 空または最後の文字が数値でない場合は「0.」を追加
                                if (lastNumber.isEmpty() || expression.isEmpty() ||
                                    expression.endsWith("+") || expression.endsWith("-") ||
                                    expression.endsWith("*") || expression.endsWith("/")) {
                                    expression += "0."

                                    // 「0」と「.」の両方を順番に読み上げ
                                    SoundPlayer.playAsset(context, "0.mp3")
                                    // 少し遅延を入れて次の音声を再生
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        SoundPlayer.playAsset(context, "dot.mp3")
                                    }, 300) // 300ミリ秒の遅延
                                } else {
                                    expression += "."

                                    // 通常の小数点の場合は「.」のみ読み上げ
                                    SoundPlayer.playAsset(context, "dot.mp3")
                                }
                            }
                        }
                        "=" -> {
                            if (expression.isNotEmpty()) {
                                // イコールの音声を数字と同じロジックで直接指定
                                SoundPlayer.playAsset(context, "equal.mp3", onCompletion = {
                                    val eval = evaluateExpression(expression)
                                    result = eval
                                    if (eval != "エラー") {
                                        if (memoryToggle) memory1 = eval else memory2 = eval
                                        memoryToggle = !memoryToggle
                                        lastResultCalculated = true
                                        // 猫の音声も直接指定でテスト
                                        SoundPlayer.playAsset(context, "cat1a.mp3", onCompletion = {
                                            SoundPlayer.playAnswerFlexible(context, eval) {}
                                        })
                                    }
                                })
                            }
                        }
                        "+", "-", "*", "/" -> {
                            if (lastResultCalculated) {
                                val cleanResult = result.replace(",", "")
                                expression = cleanResult + buttonValue
                                lastResultCalculated = false
                            } else if (expression.isNotEmpty() && !expression.endsWith("+") &&
                                !expression.endsWith("-") && !expression.endsWith("*") &&
                                !expression.endsWith("/")) {
                                expression += buttonValue
                                newNumberInput = true
                            } else if (expression.isNotEmpty() && (expression.endsWith("+") ||
                                      expression.endsWith("-") || expression.endsWith("*") ||
                                      expression.endsWith("/"))) {
                                expression = expression.substring(0, expression.length - 1) + buttonValue
                            }
                            // 演算子音声再生も直接指定
                            when (buttonValue) {
                                "+" -> SoundPlayer.playAsset(context, "+.mp3")
                                "-" -> SoundPlayer.playAsset(context, "-.mp3")
                                "*" -> SoundPlayer.playAsset(context, "multiply.mp3")
                                "/" -> SoundPlayer.playAsset(context, "÷.mp3")
                            }
                        }
                        "←" -> {
                            // バックスペースボタンの処理（一番右の文字を削除）
                            if (expression.isNotEmpty()) {
                                expression = expression.substring(0, expression.length - 1)
                            }
                        }
                        else -> {
                            // 数字ボタンの処理
                            // 結果表示後に数字を押したら式をクリア
                            if (lastResultCalculated) {
                                if (canAppendNumber("", buttonValue)) {
                                    expression = buttonValue
                                    lastResultCalculated = false
                                }
                            } else {
                                // 文字数制限（設定に基づく）を追加
                                if (expression.length < charLimit && canAppendNumber(expression, buttonValue)) {
                                    expression += buttonValue
                                }
                            }

                            // 音声再生
                            SoundPlayer.playAsset(context, "$buttonValue.mp3")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CalculatorButtonsLayout(scale: Float, onButtonPressed: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((4 * scale).dp)
    ) {
        // ボタン配置（相対サイズに変更）
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            CalculatorButton("AC", scale, onButtonPressed)
            CalculatorButton("←", scale, onButtonPressed)
            CalculatorButton("C", scale, onButtonPressed)
            CalculatorButton("/", scale, onButtonPressed)
        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            CalculatorButton("7", scale, onButtonPressed)
            CalculatorButton("8", scale, onButtonPressed)
            CalculatorButton("9", scale, onButtonPressed)
            CalculatorButton("*", scale, onButtonPressed)
        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            CalculatorButton("4", scale, onButtonPressed)
            CalculatorButton("5", scale, onButtonPressed)
            CalculatorButton("6", scale, onButtonPressed)
            CalculatorButton("-", scale, onButtonPressed)
        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            CalculatorButton("1", scale, onButtonPressed)
            CalculatorButton("2", scale, onButtonPressed)
            CalculatorButton("3", scale, onButtonPressed)
            CalculatorButton("+", scale, onButtonPressed)
        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            CalculatorButton("0", scale, onButtonPressed)
            CalculatorButton(".", scale, onButtonPressed)
            CalculatorButton("=", scale, onButtonPressed, isDoubleWidth = true)
        }

        // ダミーボタン行を追加（高さ他のボタンの1/3）
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            // 透明のダミーボタン（押下時の処理なし）
            DummyButton(scale)
            DummyButton(scale)
            DummyButton(scale, isDoubleWidth = true)
        }
    }
}

@Composable
fun CalculatorButton(text: String, scale: Float, onClick: (String) -> Unit, isDoubleWidth: Boolean = false) {
    // ボタンサイズをスケーリング（画面サイズに対応）
    val baseSize = 74.dp * 0.9f
    val buttonWidth = if (isDoubleWidth) baseSize * scale * 2 else baseSize * scale
    val buttonHeight = baseSize * scale
    val fontSize = (28 * 0.9f * scale).sp

    Button(
        onClick = { onClick(text) },
        modifier = Modifier
            .padding((4 * scale).dp)
            .size(width = buttonWidth, height = buttonHeight),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Black,
            contentColor = Color.White
        )
    ) {
        // 長いテキストは2行で表示し、...表示をしない
        val lines = if (text.length > 10) {
            val midPoint = text.length / 2
            listOf(text.substring(0, midPoint), text.substring(midPoint))
        } else {
            listOf(text)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            lines.forEach { line ->
                Text(
                    text = line,
                    // 2行表示の場合は文字サイズを90%に縮小
                    fontSize = if (lines.size > 1) fontSize * 0.9f else fontSize,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DummyButton(scale: Float, isDoubleWidth: Boolean = false) {
    // ベースサイズにスケールを適用
    val baseSize = 74.dp * 0.9f
    val buttonWidth = if (isDoubleWidth) baseSize * scale * 2 else baseSize * scale
    val buttonHeight = baseSize * scale / 3 // 高さを1/3に

    Box(
        modifier = Modifier
            .padding((4 * scale).dp)
            .size(width = buttonWidth, height = buttonHeight)
            .background(Color.Transparent)
    )
}

@Preview(showBackground = true)
@Composable
fun OldCalculatorPreview() {
    OldCalculatorScreen()
}
