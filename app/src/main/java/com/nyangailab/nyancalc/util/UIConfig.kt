package com.nyangailab.nyancalc.util

import androidx.compose.ui.unit.dp

/**
 * UI configuration constants to centralize size and spacing coefficients.
 */
object UIConfig {
    // --- 高さ比率 ---
    /**
     * 上部表示部（M1, M2, 計算, 結果）の高さ割合（全体の何％か）
     * 例: 0.5f で50%（ここを変更すれば即反映）
     */
    const val DISPLAY_AREA_HEIGHT_PERCENT = 0.5f
    /**
     * ボタンエリアの高さ割合（全体の何％か）
     * 例: 0.5f で50%（ここを変更すれば即反映）
     */
    const val BUTTON_AREA_HEIGHT_PERCENT = 0.5f

    // --- 最小高さ ---
    /** 上部表示部全体の最小高さ */
    val MIN_DISPLAY_AREA_HEIGHT = 250.dp
    /** 各表示行の最小高さ */
    val MIN_DISPLAY_ROW_HEIGHT = 50.dp
    /** ボタンエリアの最小高さ */
    val MIN_BUTTON_AREA_HEIGHT = 200.dp

    // --- ボタンサイズ ---
    /** ボタンの最小幅・高さ（Google推奨タッチターゲット） */
    val MIN_BUTTON_WIDTH = 48.dp
    val MIN_BUTTON_HEIGHT = 48.dp

    // --- ボタン間余白 ---
    val BUTTON_SPACING_HORIZONTAL = 2.dp
    val BUTTON_SPACING_VERTICAL = 2.dp

    // --- フォントサイズ倍率 ---
    const val BUTTON_FONT_SCALE = 1.0f
    const val DISPLAY_FONT_SCALE = 1.0f

    // Button configuration
    const val BUTTON_ENLARGE_FACTOR = 1.2f
    /** ボタンサイズ全体に掛けるスケール: 初期値 1.0f */
    const val BUTTON_SIZE_SCALE = 1.0f
    /** ボタン行の最小高さ: 初期値 40.dp */
    val BUTTON_ROW_MIN_HEIGHT = 10.dp

    // Button layout scale configuration
    /** ボタン全体サイズに掛けるグローバル係数 (幅・高さに適用): 初期値 1.4f */
    const val BUTTON_GLOBAL_SCALE = 1.4f

    /** ボタン行高に掛ける係数 (非推奨、BUTTON_GLOBAL_SCALEに統合予定) */
    const val BUTTON_LAYOUT_HEIGHT_SCALE = 1.0f
    /** ボタン幅に掛ける係数 (非推奨、BUTTON_GLOBAL_SCALEに統合予定) */
    const val BUTTON_LAYOUT_WIDTH_SCALE = 1.0f

    // Display rows configuration
    /** M1/M2 表示部の高さ比 (画面高さに対する割合): 初期値 0.06f */
    const val MEMORY_ROW_HEIGHT_PERCENT = 0.06f
    /** M1/M2 表示部の最小高さ: 初期値 56.dp */
    val MIN_MEMORY_ROW_HEIGHT = 56.dp
    /** 計算表示部の高さ比 (画面高さに対する割合): 初期値 0.08f */
    const val CALC_ROW_HEIGHT_PERCENT = 0.08f
    /** 計算表示部の最小高さ: 初期値 64.dp */
    val MIN_CALC_ROW_HEIGHT = 64.dp
    /** 結果表示部の高さ比 (画面高さに対する割合): 初期値 0.08f */
    const val RESULT_ROW_HEIGHT_PERCENT = 0.08f
    /** 結果表示部の最小高さ: 初期値 64.dp */
    val MIN_RESULT_ROW_HEIGHT = 64.dp

    /** 表示部の上下余白: 初期値 8.dp */
    val DISPLAY_SPACING = 6.dp

    // Font scaling factors for display sections
    /** M1/M2 表示部のフォントスケール: 初期値 1.0f */
    const val MEMORY_FONT_SCALE = 1.0f
    /** 計算表示部のフォントスケール: 初期値 1.0f */
    const val CALC_FONT_SCALE = 1.0f
    /** 結果表示部のフォントスケール: 初期値 1.0f */
    const val RESULT_FONT_SCALE = 1.0f

    // Button area layout configuration
    /** ボタンエリア上下のスペーシング (Divider前後): 初期値 8.dp */
    val BUTTON_AREA_SPACING = 2.dp
    /** ボタンエリア全体の内部余白: 初期値 4.dp */
    val BUTTON_AREA_WRAPPER_PADDING = 2.dp

    // Note: CalculatorButtonsLayout 内で使用しているローカル rowMinHeight 変数と UIConfig.BUTTON_ROW_MIN_HEIGHT が競合します。
    // rowMinHeight の使用を廃止し、UIConfig の BUTTON_ROW_HEIGHT_PERCENT と BUTTON_ROW_MIN_HEIGHT を統一的に参照するようにしてください。
}
