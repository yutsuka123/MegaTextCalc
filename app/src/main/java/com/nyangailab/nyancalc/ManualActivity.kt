package com.nyangailab.nyancalc

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 取扱説明書を表示するアクティビティ
 */
class ManualActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ManualScreenActivity {
                finish() // 戻るボタンでアクティビティを終了
            }
        }
    }
}

@Composable
fun ManualScreenActivity(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.manual_title)) },
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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
        ) {
            // 取扱説明書の内容
            ManualContent()
        }
    }
}

@Composable
fun ManualContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "MegaTextCalc かんたん取扱説明書",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 1. 機能と使い方
        Text(
            text = "1. 機能と使い方",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "■ メモリ機能", fontWeight = FontWeight.Bold)
        Text(text = "計算結果をメモリ1・メモリ2欄に保存して活用できます。")
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "■ 効果音設定", fontWeight = FontWeight.Bold)
        Text(text = "設定画面で効果音のON/OFFを切り替えられます。ONにすると猫の鳴き声が流れます。")
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "■ 表示範囲設定", fontWeight = FontWeight.Bold)
        Text(text = "設定画面で計算結果の表示桁数を4種類から選択できます。")
        Spacer(modifier = Modifier.height(16.dp))

        // 2. 設定画面
        Text(
            text = "2. 設定画面",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "効果音 … ON/OFF切替（猫ボイス）")
        Spacer(modifier = Modifier.height(4.dp))

        Text(text = "表示範囲 … 以下から選択")
        Text(text = "　①-999999999.999999999～999999999.999999999")
        Text(text = "　②-999999.999～999999.999（デフォルト）")
        Text(text = "　③-9999999999.999～999999999.999")
        Text(text = "　④-99999.99～99999.99")
        Spacer(modifier = Modifier.height(16.dp))

        // 3. よくある質問
        Text(
            text = "3. よくある質問",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Q: 猫の声が大きすぎる", fontWeight = FontWeight.Bold)
        Text(text = "A: 本体の音量を下げるか、設定で効果音をOFFにしてください。")
        Spacer(modifier = Modifier.height(8.dp))

        // 4. 免責事項
        Text(
            text = "4. 免責事項（要約）",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "本アプリの計算結果は参考値としてご利用ください。重要な計算は他の手段でも確認することをおすすめします。")
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "詳細はメニューの「免責事項」ボタンでご確認ください。")
        Spacer(modifier = Modifier.height(16.dp))

        // 5. ご支援のお願い
        Text(
            text = "ご支援のお願い",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "アプリが気に入ったら、メニ��ーの開発者支援からご支援いただけると励みになります。")
        Spacer(modifier = Modifier.height(24.dp))
    }
}
