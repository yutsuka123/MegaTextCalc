package com.nyangailab.nyancalc

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyangailab.nyancalc.util.PreferencesHelper

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onShowManual: () -> Unit,
    onShowAbout: () -> Unit,
    onShowPrivacyPolicy: () -> Unit  // プライバシーポリシー表示用の関数パラメータを追加
) {
    val context = LocalContext.current
    val prefsHelper = remember { PreferencesHelper(context) }
    var soundEnabled by remember { mutableStateOf(prefsHelper.isSoundEnabled) }

    // 新しい設定項目
    var charLimit by remember { mutableStateOf(prefsHelper.charLimit) }
    var numberRangeLevel by remember { mutableStateOf(prefsHelper.numberRangeLevel) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ヘッダー
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Text("←", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "設定",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // 設定内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // 音声設定
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "効果音",
                    fontSize = 18.sp
                )
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = {
                        soundEnabled = it
                        prefsHelper.isSoundEnabled = it
                    }
                )
            }

            Divider()

            // 文字数制限の設定
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "計算式の文字数制限",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // ラジオボタン：20文字
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = charLimit == 20,
                        onClick = {
                            charLimit = 20
                            prefsHelper.charLimit = 20
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("20文字")
                }

                // ラジオボタン：30文字
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = charLimit == 30,
                        onClick = {
                            charLimit = 30
                            prefsHelper.charLimit = 30
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("30文字（デフォルト）")
                }

                // ラジオボタン：40文字
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = charLimit == 40,
                        onClick = {
                            charLimit = 40
                            prefsHelper.charLimit = 40
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("40文字")
                }
            }

            Divider()

            // 数値範囲制限の設定
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "計算可能な数値範囲",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // ラジオボタン：レベル1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = numberRangeLevel == 1,
                        onClick = {
                            numberRangeLevel = 1
                            prefsHelper.numberRangeLevel = 1
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("-999999999.999999999～999999999.999999999")
                }

                // ラジオボタン：レベル2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = numberRangeLevel == 2,
                        onClick = {
                            numberRangeLevel = 2
                            prefsHelper.numberRangeLevel = 2
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("-999999999.999999～999999999.999999（デフォルト）")
                }

                // ラジオボタン：レベル3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = numberRangeLevel == 3,
                        onClick = {
                            numberRangeLevel = 3
                            prefsHelper.numberRangeLevel = 3
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("-999999.999～999999.999")
                }
            }

            Divider()

            // マニュアル
            Button(
                onClick = onShowManual,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("取扱説明書")
            }

            // 免責事項・アプリについて
            Button(
                onClick = onShowAbout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("このアプリについて・免責事項")
            }

            // プライバシーポリシーボタンを追加
            Button(
                onClick = onShowPrivacyPolicy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("プライバシーポリシー")
            }
        }
    }
}

@Composable
fun ManualScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ヘッダー
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Text("←", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "取扱説明書",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // マニュアル内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "メガ電卓の使い方",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "基本操作",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("・数字と演算子ボタン（+、-、*、/）を使って計算式を入力します。")
            Text("・「=」ボタンで計算を実行します。")
            Text("・計算結果は自動的にメモリに保存されます（M1とM2が交互に使われます）。")
            Text("・「C」ボタンで現在の計算をクリアします。")
            Text("・「AC」ボタンで計算とメモリをすべてクリアします。")
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "メモリ機能",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("・計算結果は自動的に交互にメモリ1とメモリ2に保存されます。")
            Text("・メモリ欄をタップすると、そのメモリの値を計算式に挿入できます。")
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "連続計算",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("・計算結果が表示された後に演算子（+、-、*、/）を押すと、その結果を使った新しい計算を始められます。")
            Text("・例：「5+3=8」の後に「+」を押すと「8+」から始まる新しい計算が可能です。")
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tip機能について",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("・アプリ開発者を応援するために、Tipを送ることができます。")
            Text("・Tip機能は1ヶ月に1回のみ利用可能です。")
            Text("・「このアプリについて」画面からTipを送ることができます。")
        }
    }
}
