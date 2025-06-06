package com.nyangailab.nyancalc.util

import android.content.Context
import android.content.SharedPreferences
import java.util.*

/**
 * アプリの設定を保存・取得するヘルパークラス
 */
class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 音声設定を保存・取得
    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true) // デフォルトはON
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    // 最後にTipを送信した日付を記録
    var lastTipDate: Long
        get() = prefs.getLong(KEY_LAST_TIP_DATE, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_TIP_DATE, value).apply()

    // 文字数制限の設定（デフォルト: 30文字）
    var charLimit: Int
        get() = prefs.getInt(KEY_CHAR_LIMIT, 30)
        set(value) = prefs.edit().putInt(KEY_CHAR_LIMIT, value).apply()

    // 数値範囲の設定（デフォルト: レベル2）
    var numberRangeLevel: Int
        get() = prefs.getInt(KEY_NUMBER_RANGE, 2)
        set(value) = prefs.edit().putInt(KEY_NUMBER_RANGE, value).apply()

    // すべての音OFF設定
    var isAllSoundOff: Boolean
        get() = prefs.getBoolean("all_sound_off", false)
        set(value) = prefs.edit().putBoolean("all_sound_off", value).apply()

    // 数値範囲レベルに基づいて、制限値を取得する
    fun getNumberRangeMin(): Double {
        return when (numberRangeLevel) {
            1 -> -999_999_999.999999999
            2 -> -999_999_999.999999
            3 -> -999_999.999
            else -> -999_999_999.999999 // デフォルトはレベル2
        }
    }

    fun getNumberRangeMax(): Double {
        return when (numberRangeLevel) {
            1 -> 999_999_999.999999999
            2 -> 999_999_999.999999
            3 -> 999_999.999
            else -> 999_999_999.999999 // デフォルトはレベル2
        }
    }

    fun getDecimalPlaces(): Int {
        return when (numberRangeLevel) {
            1 -> 9
            2 -> 6
            3 -> 3
            else -> 6 // デフォルトはレベル2
        }
    }

    // Tip送信が可能かどうかを確認（1ヶ月に1回まで）
    fun canSendTip(): Boolean {
        val lastDate = lastTipDate
        if (lastDate == 0L) return true // 一度も送っていない場合は許可

        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // 前回の日付を設定
        calendar.timeInMillis = lastDate
        val lastMonth = calendar.get(Calendar.MONTH)
        val lastYear = calendar.get(Calendar.YEAR)

        // 現在の日付を取得
        calendar.timeInMillis = currentTime
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // 年が変わっているか、月が変わっている場合は送信可能
        return currentYear > lastYear || (currentYear == lastYear && currentMonth > lastMonth)
    }

    // 最後にTipを送信した日付を現在に更新
    fun updateLastTipDate() {
        lastTipDate = System.currentTimeMillis()
    }

    companion object {
        private const val PREFS_NAME = "MegaTextCalcPrefs"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_LAST_TIP_DATE = "last_tip_date"
        private const val KEY_CHAR_LIMIT = "char_limit"
        private const val KEY_NUMBER_RANGE = "number_range"
    }
}
