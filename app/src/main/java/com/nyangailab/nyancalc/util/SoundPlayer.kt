package com.nyangailab.nyancalc.util

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.util.ArrayDeque

/**
 * 音声再生を管理するシンプルなプレーヤークラス
 * デバッグ機能強化版
 *
 * 修正履歴:
 * - ファイル名変換の修正: =.mp3→equal.mp3, *.mp3→multiply.mp3 の対応を強化
 * - 猫の音声再生ロジックを最適化: MainActivityでの猫音声再生との競合を解消
 */
object SoundPlayer {
    private const val TAG = "SoundPlayerDebug" // ログ用タグ
    private var mediaPlayer: MediaPlayer? = null
    private val playQueue = ArrayDeque<PlayRequest>()
    private var isPlaying = false

    // デバッグモードを無効化（ポップアップ非表示）
    private const val DEBUG_MODE = false

    data class PlayRequest(
        val context: Context,
        val fileName: String,
        val onCompletion: (() -> Unit)?
    )

    private fun playNextInQueue() {
        if (playQueue.isNotEmpty()) {
            val req = playQueue.removeFirst()
            playAsset(
                context = req.context,
                fileName = req.fileName,
                onCompletion = req.onCompletion,
                exclusive = false
            )
        } else {
            isPlaying = false
        }
    }

    /**
     * 音声アセットを再生する
     * @param context コンテキスト
     * @param fileName 再生するファイル名
     * @param onCompletion 再生完了時のコールバック
     * @param exclusive 排他制御フラグ
     */
    fun playAsset(
        context: Context,
        fileName: String,
        onCompletion: (() -> Unit)? = null,
        exclusive: Boolean = false
    ) {
        val preferencesHelper = PreferencesHelper(context)
        if (preferencesHelper.isAllSoundOff) {
            onCompletion?.invoke()
            if (exclusive) processQueue()
            return
        }
        if (exclusive) {
            playQueue.add(PlayRequest(context, fileName, onCompletion))
            processQueue()
        } else {
            // 短い効果音は即時多重再生
            try {
                val assetManager = context.assets
                val mp = MediaPlayer()
                val afd = assetManager.openFd("sound/$fileName")
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                mp.setOnCompletionListener {
                    onCompletion?.invoke()
                    mp.release()
                }
                mp.setOnErrorListener { _, _, _ ->
                    onCompletion?.invoke()
                    mp.release()
                    true
                }
                mp.prepareAsync()
                mp.setOnPreparedListener { mp.start() }
            } catch (e: Exception) {
                onCompletion?.invoke()
            }
        }
    }

    private fun processQueue() {
        if (isPlaying || playQueue.isEmpty()) return
        isPlaying = true
        val req = playQueue.removeFirst()
        try {
            val assetManager = req.context.assets
            val mp = MediaPlayer()
            val afd = assetManager.openFd("sound/${req.fileName}")
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.setOnCompletionListener {
                req.onCompletion?.invoke()
                mp.release()
                isPlaying = false
                processQueue()
            }
            mp.setOnErrorListener { _, _, _ ->
                req.onCompletion?.invoke()
                mp.release()
                isPlaying = false
                processQueue()
                true
            }
            mp.prepareAsync()
            mp.setOnPreparedListener { mp.start() }
        } catch (e: Exception) {
            req.onCompletion?.invoke()
            isPlaying = false
            processQueue()
        }
    }

    /**
     * メディアプレーヤーを解放する
     */
    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "プレーヤー解放エラー", e)
        }
    }

    /**
     * ランダムな猫の音声を再生する
     */
    private fun playRandomCatSound(context: Context, onCompletion: (() -> Unit)? = null) {
        try {
            val assetManager = context.assets
            val allFiles = assetManager.list("sound") ?: emptyArray()
            val catFiles = allFiles.filter { it.startsWith("cat") }

            if (catFiles.isEmpty()) {
                // showErrorToast(context, "猫の音声ファイルがありません") // エラーポップアップ表示をコメントアウト
                Log.e(TAG, "猫の音声ファイルがありません")
                onCompletion?.invoke()
                return
            }

            val randomCatFile = catFiles.random()
            // showDebugToast(context, "猫音声: $randomCatFile") // ポップアップ表示をコメントアウト
            Log.d(TAG, "猫音声: $randomCatFile")

            // 別のメディアプレーヤーで猫の音を再生
            val catPlayer = MediaPlayer()
            try {
                val afd = assetManager.openFd("sound/$randomCatFile")
                catPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                catPlayer.setOnCompletionListener {
                    it.release()
                    onCompletion?.invoke()
                }

                catPlayer.setOnErrorListener { _, _, _ ->
                    catPlayer.release()
                    onCompletion?.invoke()
                    true
                }

                catPlayer.prepare()
                catPlayer.start()

            } catch (e: Exception) {
                catPlayer.release()
                // showErrorToast(context, "猫音声再生失敗: $randomCatFile") // エラーポップアップ表示をコメントアウト
                Log.e(TAG, "猫音声再生失敗: $randomCatFile", e)
                onCompletion?.invoke()
            }

        } catch (e: Exception) {
            // showErrorToast(context, "猫音声エラー: ${e.message}") // エラーポップアップ表示をコメントアウト
            Log.e(TAG, "猫音声エラー", e)
            onCompletion?.invoke()
        }
    }

    /**
     * デバッグ用のトースト表示
     */
    private fun showDebugToast(context: Context, message: String) {
        if (DEBUG_MODE) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "🔊 $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * エラー用のトースト表示
     */
    private fun showErrorToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "❌ $message", Toast.LENGTH_LONG).show()
        }
    }

    // 以下の計算結果読み上げ関連の機能はそのまま保持
    fun getRandomCatSound(context: Context): String {
        try {
            val catFiles = context.assets.list("sound")?.filter { it.startsWith("cat") } ?: listOf()
            return if (catFiles.isNotEmpty()) catFiles.random() else ""
        } catch (e: IOException) {
            Log.e(TAG, "Error getting cat sound files", e)
            return ""
        }
    }

    private fun splitAnswer(answer: Int): List<String> {
        val parts = mutableListOf<String>()
        var remain = answer
        val units = listOf(10000, 1000, 100, 10, 1)
        for (unit in units) {
            val value = (remain / unit) * unit
            if (value > 0) {
                parts.add("$value.mp3")
                remain -= value
            }
        }
        return parts
    }

    fun playAnswer(context: Context, answer: Int, onFinish: () -> Unit) {
        val files = splitAnswer(answer)
        fun playNext(index: Int) {
            if (index >= files.size) {
                onFinish()
            } else {
                // ファイルが存在しない場合はスキップ
                try {
                    context.assets.openFd("sound/${files[index]}").close()
                    playAsset(context, files[index], onCompletion = { playNext(index + 1) }, exclusive = true)
                } catch (e: IOException) {
                    Log.w(TAG, "skip missing file: ${files[index]}")
                    playNext(index + 1)
                }
            }
        }
        if (files.isEmpty()) {
            // 0の場合
            try {
                context.assets.openFd("sound/0.mp3").close()
                playAsset(context, "0.mp3", onCompletion = { onFinish() })
            } catch (e: IOException) {
                onFinish()
            }
        } else {
            playNext(0)
        }
    }

    fun playAnswerFlexible(context: Context, answerStr: String, onFinish: () -> Unit) {
        val files = mutableListOf<String>()
        val isNegative = answerStr.startsWith("-")
        val absValueStr = answerStr.replace(",", "").removePrefix("-")
        if (isNegative) files.add("minus.mp3")
        val parts = absValueStr.split(".", limit = 2)
        val intPartStr = parts.getOrNull(0) ?: "0"
        val decimalPart = parts.getOrNull(1)

        // 整数部の各桁を1文字ずつmp3ファイルに
        intPartStr.forEach { c ->
            if (c.isDigit()) files.add("${c}.mp3")
        }

        // 小数点対応（最大6桁）
        if (!decimalPart.isNullOrEmpty()) {
            files.add("dot.mp3")
            decimalPart.take(6).forEach { c ->
                if (c.isDigit()) files.add("${c}.mp3")
            }
        }

        // デバッグ用ログ
        Log.d(TAG, "playAnswerFlexible files: $files")

        // filesが空の場合は必ず0.mp3を追加
        if (files.isEmpty()) {
            files.add("0.mp3")
        }

        fun playNext(index: Int) {
            if (index >= files.size) {
                onFinish()
                playNextInQueue()
            } else {
                playAsset(
                    context = context,
                    fileName = files[index],
                    exclusive = true,
                    onCompletion = { playNext(index + 1) }
                )
            }
        }
        playNext(0)
    }

    // 1000万、100万、200万以降のルールに従いファイル名を追加
    private fun addLargeUnitFiles(files: MutableList<String>, value: Long) {
        var remain = value
        if (remain >= 10_000_000) {
            val senman = remain / 10_000_000
            if (senman == 1L) {
                files.add("10million.mp3")
            } else {
                files.add("${senman}.mp3")
                files.add("10million_combine.mp3")
            }
            remain %= 10_000_000
        }
        if (remain >= 1_000_000) {
            val hyakuman = remain / 1_000_000
            if (hyakuman == 1L) {
                files.add("million.mp3")
            } else {
                files.add("${hyakuman}.mp3")
                files.add("million.mp3")
            }
            remain %= 1_000_000
        }
        // それ以下はsplitAnswerで既存通り
        if (remain > 0) {
            files.addAll(splitAnswer(remain.toInt()))
        }
    }
}
