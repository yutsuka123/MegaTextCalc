package com.nyangailab.nyancalc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nyangailab.nyancalc.billing.BillingClientHelper
import com.nyangailab.nyancalc.util.PreferencesHelper

/**
 * 設定画面を表示するアクティビティ
 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var billingClientHelper: BillingClientHelper

    // プライバシーポリシーのURL
    private val privacyPolicyUrl = "https://docs.google.com/document/d/1ilzylPZeh-GL4fBtdT_ErSbDxrcuDbcy7ngdEHHBH4k/edit?usp=sharing"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 課金クライアントの初期化
        billingClientHelper = BillingClientHelper(this)
        billingClientHelper.initialize { isSuccess ->
            // 初期化結果をログ出力するなどの処理があれば追加
        }

        setContent {
            SettingsScreenActivity(
                onBackClick = { finish() },
                onSupportDeveloperClick = { billingClientHelper.showDonationOptions(this) },
                onPrivacyPolicyClick = { openPrivacyPolicy() }
            )
        }
    }

    // プライバシーポリシーを開く関数
    private fun openPrivacyPolicy() {
        // プライバシーポリシーのダイアログを表示
        PrivacyPolicyDialogFragment().show(supportFragmentManager, "privacy_policy")
    }

    override fun onDestroy() {
        // アクティビティ終了時に課金クライアント接続を終了
        billingClientHelper.endConnection()
        super.onDestroy()
    }
}

@Composable
fun SettingsScreenActivity(
    onBackClick: () -> Unit,
    onSupportDeveloperClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    val context = LocalContext.current
    val prefsHelper = remember { PreferencesHelper(context) }

    // 設定値を読み込む
    var soundEnabled by remember { mutableStateOf(prefsHelper.isSoundEnabled) }
    var displayRangeIndex by remember { mutableStateOf(prefsHelper.numberRangeLevel - 1) }

    val displayRangeOptions = listOf(
        "-999999999.999999999～999999999.999999999",
        "-999999999.999999～999999999.999999（デフォルト）",
        "-999999.999～999999.999"
    )
    val displayRangeDescriptions = listOf(
        "大きな数値と高精度な小数部が必要な計算向け",
        "一般的な計算に最適なバランス設定",
        "シンプルで見やすい表示が好みの方向け"
    )

    // 設定変更時に保存する
    fun saveSoundSetting(enabled: Boolean) {
        prefsHelper.isSoundEnabled = enabled
    }

    fun saveRangeSetting(index: Int) {
        prefsHelper.numberRangeLevel = index + 1 // インデックスは0始まり、レベルは1始まりなので調整
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 音声設定
            Text(
                text = stringResource(id = R.string.sound_settings),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "猫ボイスを有効にする",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = {
                        soundEnabled = it
                        saveSoundSetting(it) // 設定変更時に即座に保存
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 表示範囲設定
            Text(
                text = stringResource(id = R.string.display_settings),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Text("表示範囲の選択")
            Spacer(modifier = Modifier.height(8.dp))

            displayRangeOptions.forEachIndexed { index, option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = displayRangeIndex == index,
                        onClick = {
                            displayRangeIndex = index
                            saveRangeSetting(index) // 設定変更時に即座に保存
                        }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = "　${index + 1} ${option}")
                        Text(
                            text = displayRangeDescriptions[index],
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 開発者支援セクションの追加
            Text(
                text = stringResource(id = R.string.donate_title),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.donate_description),
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSupportDeveloperClick,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(text = stringResource(id = R.string.donate_title))
            }
            Spacer(modifier = Modifier.height(32.dp))

            // プライバシーポリシーセクション
            Text(
                text = "プライバシーポリシー",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "当アプリのプライバシーポリシーを確認できます",
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onPrivacyPolicyClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "プライバシーポリシーを確認する")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "※設定はすぐに反映されます",
                style = MaterialTheme.typography.caption
            )
        }
    }
}
