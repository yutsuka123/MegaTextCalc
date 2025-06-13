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
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 8.dp)
                    )
                } else {
                    Text(
                        text = "MegaTextCalc",
                        fontSize = (18f * scale).sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 右端メニューボタン（≡）
                IconButton(
                    onClick = {
                        // MainActivity の BottomSheetMenu を表示
                        (context as? MainActivity)?.showMenuBottomSheet()
                    },
                    modifier = Modifier
                        .size(32.dp)
                ) {
                    Text("≡", fontSize = 18.sp, color = Color.Black)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Column の高さを4等分し、他領域に影響しない
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutoResizeText(
                    "M1:",
                    maxDigits,
                    displayFontSize,
                    minFontSize,
                    Modifier.weight(1f),
                    maxLines = 2
                )
                AutoResizeText(
                    formatNumberForDisplay(memory1),
                    maxDigits,
                    displayFontSize,
                    minFontSize,
                    Modifier
                        .weight(3f)
                        .clickable(enabled = memory1.isNotEmpty()) {
                            if (expression.isEmpty() || expression == "0" ||
                                expression.lastOrNull() in listOf('+', '-', '*', '/')) {
                                expression = if (expression.isEmpty() || expression == "0") memory1 else expression + memory1
                            }
                        },
                    maxLines = 2
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Column の高さを4等分し、他領域に影響しない
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutoResizeText(
                    "M2:",
                    maxDigits,
                    displayFontSize,
                    minFontSize,
                    Modifier.weight(1f),
                    maxLines = 2
                )
                AutoResizeText(
                    formatNumberForDisplay(memory2),
                    maxDigits,
                    displayFontSize,
                    minFontSize,
                    Modifier
                        .weight(3f)
                        .clickable(enabled = memory2.isNotEmpty()) {
                            if (expression.isEmpty() || expression == "0" ||
                                expression.lastOrNull() in listOf('+', '-', '*', '/')) {
                                expression = if (expression.isEmpty() || expression == "0") memory2 else expression + memory2
                            }
                        },
                    maxLines = 2
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Column の高さを4等分し、他領域に影響しない
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutoResizeText(
                    "計算",
                    maxDigits,
                    displayFontSize,
                    minFontSize,
                    Modifier.weight(1f),
                    maxLines = 2
                )
                AutoResizeText(
                    formatNumberForDisplay(expression),
                    maxDigits,
                    displayFontSize,
                    minFontSize,
                    Modifier.weight(3f),
                    maxLines = 2
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Column の高さを4等分し、他領域に影響しない
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutoResizeText(
                    "結果",
                    maxDigits,
                    displayFontSize,
                    minFontSize,
                    Modifier.weight(1f),
                    maxLines = 2
                )
                AutoResizeText(
                    formatNumberForDisplay(result),
                    maxDigits,
                    displayFontSize,
                    minFontSize,
                    Modifier.weight(3f),
                    maxLines = 2
                )
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
                                    // 1) 式を即時評価し、UI とメモリを更新
                                    val eval = evaluateExpression(expression, prefsHelper.getNumberRangeMin().toDouble(), prefsHelper.getNumberRangeMax().toDouble(), prefsHelper.getDecimalPlaces())
                                    result = eval

                                    if (eval != "エラー") {
                                        if (memoryToggle) memory1 = eval else memory2 = eval
                                        memoryToggle = !memoryToggle
                                        lastResultCalculated = true

                                        // 2) サウンド連鎖: イコール(専有) → 猫(専有) → 答え
                                        SoundPlayer.playAsset(
                                            context = context,
                                            fileName = "equal.mp3",
                                            exclusive = true,
                                            onCompletion = {
                                                val prefs = PreferencesHelper(context)
                                                val catFile = if (prefs.isSoundEnabled) SoundPlayer.getRandomCatSound(context) else ""
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
                                    } else {
                                        // エラー時は効果音のみ
                                        SoundPlayer.playAsset(context, "equal.mp3")
                                    }
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
                                val charLimit = prefsHelper.charLimit // 計算範囲ではなく、式の文字数制限を取得
                                // 数字ボタンの処理
                                // 結果表示後に数字を押したら式をクリア
                                if (lastResultCalculated) {
                                    if (canAppendNumber("", buttonValue, charLimit)) {
                                        expression = buttonValue
                                        lastResultCalculated = false
                                    }
                                } else {
                                    // 文字数制限（設定に基づく）を追加
                                    if (expression.length < charLimit && canAppendNumber(expression, buttonValue, charLimit)) {
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
 * 表示部のテキストを自動で縮小し、2行まで見切れずに表示するためのコンポーザブル。
 * 設計意図：
 *   - 計算式や結果が長い場合でも2行に収めて見切れを防ぐ。
 *   - これをしないと長い計算式や結果が...で省略されてしまうため必須。
 *   - フォントサイズは2行に収まるまで段階的に小さくする。
 * デグレ防止：
 *   - レイアウト変更やフォントサイズ調整時に2行目が重なったり見切れたりするバグを防ぐ。
 *   - 2行表示・縮小ロジックを必ず維持すること。
 */
@Composable
fun AutoResizeText(
    text: String,
    charLimit: Int,
    baseFontSize: TextUnit,
    minFontSize: TextUnit,
    modifier: Modifier = Modifier,
    maxLines: Int = 2
) {
    // 2行収め用フォントサイズ計算:
    // 60dp 高の行に lineHeight = font*1.2 × 2 が収まるよう上限を設定
    val maxFontByHeight = 22f // 行高 ≈52dp に 2行収める上限を22sp程度に設定

    val computedFontSize = if (text.length > charLimit) {
        val ratio = charLimit.toFloat() / text.length
        (baseFontSize.value * ratio).coerceAtLeast(minFontSize.value)
            .coerceAtMost(maxFontByHeight)
    } else {
        baseFontSize.value.coerceAtMost(maxFontByHeight)
    }.sp

    Text(
        text = text,
        fontSize = computedFontSize,
        maxLines = maxLines,
        softWrap = true,
        overflow = TextOverflow.Clip,
        lineHeight = (computedFontSize.value * 1.2f).sp,
        modifier = modifier.padding(horizontal = 2.dp)
    )
}

/**
 * 四則演算式を安全にパース・計算し、カンマ区切りで表示する関数。
 * 設計意図：
 *   - ユーザーが入力した計算式（例：10000+0.3333や3,500+2,500など）を正しく評価し、
 *     結果を常に3桁区切りで表示する。
 *   - カンマ（,）は計算前にすべて除去する。
 *   - 小数点や負号、演算子の優先順位も正しく処理。
 * デグレ防止：
 *   - 以前の実装で「10000+0.3333→0.3333」など誤った結果になるバグがあったため、
 *     このRPNパーサーで必ず正しい計算結果を保証する。
 *   - 計算式評価ロジックを変更する場合は必ずテストケース（1000+0.333, 1+2*3, (1+2)*3等）で正しい結果を確認すること。
 *   - 結果は常にカンマ区切り・指数表現禁止で返す。
 */
fun evaluateExpression(expr: String, minValue: Double, maxValue: Double, decimalPlaces: Int): String {
    // カンマを除去してから解析
    val cleanExpr = expr.replace(",", "")

    // トークン化（数値, 演算子, 括弧）
    // Kotlin の通常の文字列リテラルでは "\\-" のように二重にエスケープが必要になるため、
    // 可読性と安全性を考慮して raw 文字列（""" ～ """) を使用する。
    val tokens = Regex("""([+\-*/()])""")
        .replace(cleanExpr, " $1 ")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }

    // 中置→後置変換用スタック
    val output = mutableListOf<String>()
    val opStack = mutableListOf<String>()
    val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)

    try {
        // Shunting-yard
        tokens.forEach { tok ->
            when {
                tok.toBigDecimalOrNull() != null -> output.add(tok)
                tok == "(" -> opStack.add(tok)
                tok == ")" -> {
                    while (opStack.isNotEmpty() && opStack.last() != "(") {
                        output.add(opStack.removeAt(opStack.lastIndex))
                    }
                    if (opStack.isEmpty() || opStack.removeAt(opStack.lastIndex) != "(") {
                        throw IllegalArgumentException("括弧の対応が取れていません")
                    }
                }
                tok in precedence -> {
                    while (opStack.isNotEmpty() && opStack.last() in precedence &&
                        precedence[opStack.last()]!! >= precedence[tok]!!) {
                        output.add(opStack.removeAt(opStack.lastIndex))
                    }
                    opStack.add(tok)
                }
                else -> throw IllegalArgumentException("不正なトークン: $tok")
            }
        }
        while (opStack.isNotEmpty()) {
            val op = opStack.removeAt(opStack.lastIndex)
            if (op in listOf("(", ")")) throw IllegalArgumentException("括弧の対応が取れていません")
            output.add(op)
        }

        // RPN 評価（BigDecimal で高精度計算）
        val evalStack = mutableListOf<java.math.BigDecimal>()
        val mc = java.math.MathContext.DECIMAL64
        output.forEach { tok ->
            when {
                tok.toBigDecimalOrNull() != null -> evalStack.add(tok.toBigDecimal())
                tok in precedence -> {
                    if (evalStack.size < 2) throw IllegalArgumentException("式が不正です")
                    val b = evalStack.removeAt(evalStack.lastIndex)
                    val a = evalStack.removeAt(evalStack.lastIndex)
                    val res = when (tok) {
                        "+" -> a.add(b, mc)
                        "-" -> a.subtract(b, mc)
                        "*" -> a.multiply(b, mc)
                        "/" -> a.divide(b, mc)
                        else -> throw IllegalStateException()
                    }
                    evalStack.add(res)
                }
            }
        }

        if (evalStack.size != 1) return "エラー"

        // 計算結果
        var result = evalStack.first()

        // 1) 小数部の丸め（四捨五入）
        result = try {
            result.setScale(decimalPlaces, java.math.RoundingMode.HALF_UP)
        } catch (e: Exception) {
            result
        }

        // 2) 範囲チェック
        val doubleVal = try { result.toDouble() } catch (e: Exception) { Double.NaN }
        if (doubleVal.isNaN() || doubleVal < minValue || doubleVal > maxValue) {
            return "エラー"
        }

        // 3) 表示フォーマット
        val resultStr = result.stripTrailingZeros().toPlainString()
        return formatNumberForDisplay(resultStr)
    } catch (e: Exception) {
        return "エラー"
    }
}

fun formatNumberForDisplay(raw: String): String {
    return try {
        // カンマを除去して数値化 → 文字列化で指数表記を防止
        val plain = java.math.BigDecimal(raw.replace(",", "")).stripTrailingZeros().toPlainString()

        // 整数か小数かで処理分岐
        return if (!plain.contains('.')) {
            // 整数のみ → 3桁区切り
            "% ,d".format(plain.toLong())
        } else {
            val (intStr, decStr) = plain.split('.')
            val formattedInt = "% ,d".format(intStr.toLong())
            val trimmedDec = decStr.trimEnd('0')
            if (trimmedDec.isEmpty()) formattedInt else "$formattedInt.$trimmedDec"
        }
    } catch (e: Exception) {
        // 何らかの変換失敗時は入力をそのまま返す
        raw
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

