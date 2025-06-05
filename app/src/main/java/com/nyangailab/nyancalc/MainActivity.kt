package com.nyangailab.nyancalc

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nyangailab.nyancalc.billing.BillingClientHelper
import com.nyangailab.nyancalc.ui.AboutDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var billingHelper: BillingClientHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 
        // 最下行表示問題対策：WindowInsetsとシステムバー設定
        // これによりナビゲーションバーとの重複を防ぎ、電卓ボタンの適切な表示を確保
        //
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 先にBillingを初期化
        billingHelper = BillingClientHelper(this)

        // 初期化後にUIを設定
        setContent {
            MegaTextCalcApp(
                onShowAbout = { showAboutDialog() },
                billingClientHelper = billingHelper
            )
        }

        // Billing接続を確立（UIの表示と並行して実行可能）
        billingHelper.initialize { isSuccess ->
            // Billing初期化成功後の処理があれば記述
        }
    }

    private fun showAboutDialog() {
        val dialogFragment = AboutDialogFragment.newInstance(billingHelper)
        dialogFragment.show(supportFragmentManager, "AboutDialog")
    }

    // メニューボタンでボトムシート表示
    fun showMenuBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_menu, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // バージョン情報をセット
        val versionInfo = bottomSheetView.findViewById<android.widget.TextView>(R.id.tvVersionInfo)
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            versionInfo.text = "Version: $versionName"
        } catch (e: Exception) {
            versionInfo.text = "Version: 不明"
        }

        // 設定ボタンのリスナー設定
        bottomSheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSettings).setOnClickListener {
            // 設定画面を表示
            val intent = android.content.Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }

        // 取扱説明書ボタンのリスナー設定
        bottomSheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnManual).setOnClickListener {
            // 取扱説明書表示の処理
            // 別アクティビティで取扱説明書画面を表示
            val intent = android.content.Intent(this, ManualActivity::class.java)
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }

        // 免責事項ボタンのリスナー設定
        bottomSheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDisclaimer).setOnClickListener {
            DisclaimerDialogFragment().show(supportFragmentManager, "disclaimer")
            bottomSheetDialog.dismiss() // ダイアログ表示後にボトムシートを閉じる
        }

        // プライバシーポリシーボタンのリスナー設定
        bottomSheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPrivacyPolicy).setOnClickListener {
            PrivacyPolicyDialogFragment().show(supportFragmentManager, "privacy_policy")
            bottomSheetDialog.dismiss() // ダイアログ表示後にボトムシートを閉じる
        }

        // バージョン情報ボタンのリスナー設定
        bottomSheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnVersionInfo).setOnClickListener {
            try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val versionName = packageInfo.versionName
                val message = "アプリバージョン: $versionName\n" +
                        "最終更新: 2025年6月5日"

                val dialog = AlertDialog.Builder(this)
                    .setTitle("バージョン情報")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .create()
                dialog.show()
            } catch (e: Exception) {
                Toast.makeText(this, "バージョン情報を取得できませんでした", Toast.LENGTH_SHORT).show()
            }
        }

        // 寄付ボタンのリスナー設定
        bottomSheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDonate).setOnClickListener {
            billingHelper.showDonationOptions(this)
            bottomSheetDialog.dismiss()
        }

        // 閉じるボタンのリスナー設定
        bottomSheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClose).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showMenuBottomSheet()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

@Composable
fun MegaTextCalcApp(
    onShowAbout: () -> Unit = {},
    billingClientHelper: BillingClientHelper? = null
) {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    val context = LocalContext.current

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            if (showSplash) {
                // スプラッシュスクリーン表示
                SplashScreen(
                    onSplashComplete = {
                        showSplash = false
                    }
                )
            } else {
                NavHost(navController = navController, startDestination = "calculator") {
                    composable("calculator") {
                        val activity = LocalContext.current as? MainActivity
                        Box(modifier = Modifier.fillMaxSize()) {
                            CalculatorScreen()

                            // 右上のメニューボタン
                            Button(
                                onClick = { activity?.showMenuBottomSheet() },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color.LightGray,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(40.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Text("≡", fontSize = 20.sp)
                            }
                        }
                    }
                    composable("settings") {
                        SettingsScreen(
                            onBackClick = { navController.popBackStack() },
                            onShowManual = { navController.navigate("manual") },
                            onShowAbout = onShowAbout,
                            onShowPrivacyPolicy = {
                                // プライバシーポリシーを外部ブラウザで開く
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/1ilzylPZeh-GL4fBtdT_ErSbDxrcuDbcy7ngdEHHBH4k/edit?usp=sharing"))
                                context.startActivity(intent)
                            }
                        )
                    }
                    composable("manual") {
                        ManualScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val context = LocalContext.current

    // スプラッシュ画面の表示とディレイ
    LaunchedEffect(key1 = true) {
        delay(2000)  // 2秒後に移動
        onSplashComplete()
    }

    // スプラッシュ画面のUI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // splashscreen.pngを表示
        Image(
            painter = painterResource(id = R.drawable.splashscreen),
            contentDescription = "Splash Screen",
            modifier = Modifier.fillMaxWidth(0.7f)  // 画面の70%幅で表示
        )
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MegaTextCalcApp()
}
