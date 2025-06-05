package com.nyangailab.nyancalc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import com.nyangailab.nyancalc.util.PreferencesHelper
import com.nyangailab.nyancalc.util.SoundPlayer
import com.nyangailab.nyancalc.util.UIConfig
import java.util.*
import kotlin.math.abs // ★ abs をインポート
import kotlin.math.max
import kotlin.math.min

/**
 * メガ電卓のメイン画面（計算機能部分）
 * - UI改善 2025/06/05: ボタンサイズ拡大、重なり解消、相対レイアウト化
 * - UI改善 2025/06/05: 縦スクロール対応を追加
 * - 元の実装は OldLayoutCalculatorScreen.kt に保存
 */
@Composable // ★ @androidx.compose.runtime.Composable から変更
fun CalculatorScreen() {
    val context = LocalContext.current
    val prefsHelper = remember { PreferencesHelper(context) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val scale = screenHeight.value / 592f

    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var memory1 by remember { mutableStateOf("") }
    var memory2 by remember { mutableStateOf("") }
    var memoryToggle by remember { mutableStateOf(true) }
    var lastOperator by remember { mutableStateOf("") }
    var lastNumber by remember { mutableStateOf(0.0) }
    var newNumberInput by remember { mutableStateOf(true) }
    var lastResultCalculated by remember { mutableStateOf(false) }

    // 設定値から最大桁数を算出
    val maxAbsValue = maxOf(abs(prefsHelper.getNumberRangeMax().toDouble()), abs(prefsHelper.getNumberRangeMin().toDouble())) // ★ .toDouble() を追加
    val maxDigits = maxAbsValue.toBigDecimal().toPlainString().replace(".", "").length + prefsHelper.getDecimalPlaces() + 2 // 小数点+符号分

    val scrollState = rememberScrollState() // TODO: この変数は現在使用されていません

        Column(
            modifier = Modifier
                .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. 最上部余白（6%）
        Spacer(modifier = Modifier.weight(6f))
        // 2. 設定ボタンエリア（10%）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                .weight(10f),
                verticalAlignment = Alignment.CenterVertically
            ) {
            val logoExists = remember {
                context.resources.getIdentifier("rogo", "drawable", context.packageName) != 0
            }
            if (logoExists) {
                    Image(
                    painter = painterResource(id = R.drawable.rogo),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(32.dp).padding(start = 8.dp)
                )
            } else {
                Text(
                    text = "MegaTextCalc",
                    fontSize = (18f * scale).sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* 設定画面遷移 */ }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_preferences),
                    contentDescription = "Settings"
                )
            }
        }
        // 3. 上部表示部（35%）
        Column(
                    modifier = Modifier
                        .fillMaxWidth()
                .weight(35f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val displayFontSize = (18f * scale * 1.5f).sp
            val minFontSize = 8.sp
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AutoResizeText("M1:", maxDigits, displayFontSize, minFontSize, Modifier.weight(1f), maxLines = 2)
                AutoResizeText(memory1, maxDigits, displayFontSize, minFontSize, Modifier.weight(3f), maxLines = 2)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AutoResizeText("M2:", maxDigits, displayFontSize, minFontSize, Modifier.weight(1f), maxLines = 2)
                AutoResizeText(memory2, maxDigits, displayFontSize, minFontSize, Modifier.weight(3f), maxLines = 2)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AutoResizeText("計算", maxDigits, displayFontSize, minFontSize, Modifier.weight(1f), maxLines = 2)
                AutoResizeText(expression, maxDigits, displayFontSize, minFontSize, Modifier.weight(3f), maxLines = 2)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AutoResizeText("結果", maxDigits, displayFontSize, minFontSize, Modifier.weight(1f), maxLines = 2)
                AutoResizeText(result, maxDigits, displayFontSize, minFontSize, Modifier.weight(3f), maxLines = 2)
            }
        }
        // 4. 上部キー部と下部キー部の間（3%）
        Spacer(modifier = Modifier.weight(3f))
        // 5. ボタンエリア（40%）
        CalculatorButtonsLayout(
                modifier = Modifier
                    .fillMaxWidth()
                .weight(40f),
                    scale = scale,
            enlargeFactor = UIConfig.BUTTON_ENLARGE_FACTOR,
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
                            val eval = evaluateExpression(expression, prefsHelper)
                                        result = eval
                                        if (eval != "エラー") {
                                            if (memoryToggle) memory1 = eval else memory2 = eval
                                            memoryToggle = !memoryToggle
                                            lastResultCalculated = true
                            }
                            // コールバックチェーンで順次再生
                            SoundPlayer.playAsset(
                                context = context,
                                fileName = "equal.mp3",
                                exclusive = true,
                                onCompletion = {
                                    val catFile = SoundPlayer.getRandomCatSound(context)
                                    if (catFile.isNotEmpty()) {
                                        SoundPlayer.playAsset(
                                            context = context,
                                            fileName = catFile,
                                            exclusive = true,
                                            onCompletion = { SoundPlayer.playAnswerFlexible(context, eval) {} }
                                        )
                                    } else {
                                                SoundPlayer.playAnswerFlexible(context, eval) {}
                                    }
                                }
                            )
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
                            if (canAppendNumber("", buttonValue, maxDigits)) {
                                        expression = buttonValue
                                        lastResultCalculated = false
                                    }
                                } else {
                                    // 文字数制限（設定に基づく）を追加
                            if (expression.length < maxDigits && canAppendNumber(expression, buttonValue, maxDigits)) {
                                        expression += buttonValue
                                    }
                                }

                                // 音声再生
                                SoundPlayer.playAsset(context, "$buttonValue.mp3")
                            }
                        }
                    }
                )
        // 6. 最下部余白（6%）
        Spacer(modifier = Modifier.weight(6f))
    }
}

/**
 * 計算機のボタンレイアウト - UI改善版（2025/06/05）
 * - weightベースの相対レイアウトに変更
 * - テキスト自動調整機能強化
 */
@Composable
fun CalculatorButtonsLayout(
    modifier: Modifier = Modifier,
    scale: Float,
    enlargeFactor: Float, // TODO: この引数は現在使用されていません
    onButtonPressed: (String) -> Unit
) {
    val buttonRows = listOf(
        listOf("AC", "←", "C", "/"),
        listOf("7", "8", "9", "*"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=", "")
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        buttonRows.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { label ->
                    if (label.isNotEmpty()) {
                        CalculatorButton(
                            text = label,
                            scale = scale,
                            enlargeFactor = enlargeFactor,
                            onClick = onButtonPressed,
            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .aspectRatio(2.2f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 計算ボタン（UI改善版 2025/06/05）
 */
@Composable
fun CalculatorButton(
    text: String,
    scale: Float,
    enlargeFactor: Float,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
     Button(
         onClick = { onClick(text) },
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = UIConfig.BUTTON_SPACING_HORIZONTAL * UIConfig.BUTTON_GLOBAL_SCALE,
            vertical = UIConfig.BUTTON_SPACING_VERTICAL * UIConfig.BUTTON_GLOBAL_SCALE
        ),
         colors = ButtonDefaults.buttonColors(
             backgroundColor = Color.Black,
             contentColor = Color.White
         ),
        shape = RoundedCornerShape(4.dp)
    ) {
             Text(
                 text = text,
            fontSize = (22f * scale).sp,
            maxLines = 1
        )
     }
 }

 @Preview(showBackground = true)
 @Composable
 fun CalculatorPreview() {
     CalculatorScreen()
 }

/**
 * 四則演算の簡易評価関数（例：1+2*3 → 7）
 * 必要に応じて拡張してください
 */
fun evaluateExpression(expr: String, prefsHelper: PreferencesHelper): String { // ★ prefsHelper を引数に追加
    return try {
        val cleanExpr = expr.replace("×", "*").replace("÷", "/")
        val result = object : Any() {
            var pos = -1
            var ch = 0
            fun nextChar() { ch = if (++pos < cleanExpr.length) cleanExpr[pos].code else -1 } // ★ toInt() を code に変更
            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar() // ★ toInt() を code に変更
                if (ch == charToEat) { nextChar(); return true }
                return false
            }
            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < cleanExpr.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm() // ★ toInt() を code に変更
                        eat('-'.code) -> x -= parseTerm() // ★ toInt() を code に変更
                        else -> return x
                    }
                }
            }
            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor() // ★ toInt() を code に変更
                        eat('/'.code) -> { // ★ toInt() を code に変更
                            val divisor = parseFactor()
                            if (divisor == 0.0) throw ArithmeticException("除算エラー")
                            x /= divisor
                        }
                        else -> return x
                    }
                }
            }
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // ★ toInt() を code に変更
                if (eat('-'.code)) return -parseFactor() // ★ toInt() を code に変更
                var x: Double
                val startPos = pos
                if (eat('('.code)) { // ★ toInt() を code に変更
                    x = parseExpression()
                    eat(')'.code) // ★ toInt() を code に変更
                } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) { // ★ toInt() を code に変更
                    while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar() // ★ toInt() を code に変更
                    x = cleanExpr.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
        // カンマ区切り・指数表現禁止
        val formatted = if (result % 1.0 == 0.0) "% ,d".format(result.toLong())
                        else "% ,.${prefsHelper.getDecimalPlaces()}f".format(result).trimEnd('0').trimEnd('.')
        formatted
    } catch (e: Exception) { // TODO: この例外変数は現在使用されていません
        "エラー"
    }
}

/**
 * 入力値が有効かどうかを判定する関数
 * - 例：最大10桁、小数点は1つまで
 */
fun canAppendNumber(current: String, append: String, charLimit: Int): Boolean {
    if ((current + append).length > charLimit) return false
    if (append == "." && current.contains(".")) return false
    return append.all { it.isDigit() || it == '.' }
}

@Composable
fun AutoResizeText(
    text: String,
    charLimit: Int,
    baseFontSize: TextUnit,
    minFontSize: TextUnit,
    modifier: Modifier = Modifier,
    maxLines: Int = 2 // 2行まで許容
) {
    val fontSize = if (text.length > charLimit) {
        val ratio = charLimit.toFloat() / text.length
        (baseFontSize.value * ratio).coerceAtLeast(minFontSize.value).sp
    } else {
        baseFontSize
    }
    Text(
        text = text,
        fontSize = fontSize,
        maxLines = maxLines,
        softWrap = true,
        modifier = modifier.padding(horizontal = 2.dp)
    )
 }
