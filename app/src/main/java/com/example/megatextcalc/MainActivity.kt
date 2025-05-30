package com.example.megatextcalc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.megatextcalc.billing.BillingClientHelper
import com.example.megatextcalc.databinding.ActivityMainBinding
import com.example.megatextcalc.ui.AboutDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var billingHelper: BillingClientHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Billing初期化
        billingHelper = BillingClientHelper(this)
        binding.btnMenu.isEnabled = false
        billingHelper.initialize { isSuccess ->
            binding.btnMenu.isEnabled = isSuccess
        }

        // メニューボタンでボトムシート表示
        binding.btnMenu.setOnClickListener {
            showMenuBottomSheet()
        }
    }

    private fun showMenuBottomSheet() {
        val sheet = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_menu, null)
        sheet.setContentView(view)
        // 寄付ボタン
        view.findViewById<View>(R.id.btnDonate).setOnClickListener {
            sheet.dismiss()
            showDonateDialog()
        }
        // 免責事項ボタン
        view.findViewById<View>(R.id.btnDisclaimer).setOnClickListener {
            sheet.dismiss()
            AboutDialogFragment().show(supportFragmentManager, AboutDialogFragment.TAG)
        }
        // 閉じるボタン
        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            sheet.dismiss()
        }
        sheet.show()
    }

    private fun showDonateDialog() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_donate, null)
        dialog.setContentView(view)
        // 価格ボタン
        view.findViewById<View>(R.id.btnSmallTip).setOnClickListener {
            launchTipPurchase(BillingClientHelper.TIP_SMALL)
            dialog.dismiss()
        }
        view.findViewById<View>(R.id.btnMediumTip).setOnClickListener {
            launchTipPurchase(BillingClientHelper.TIP_MEDIUM)
            dialog.dismiss()
        }
        view.findViewById<View>(R.id.btnLargeTip).setOnClickListener {
            launchTipPurchase(BillingClientHelper.TIP_LARGE)
            dialog.dismiss()
        }
        view.findViewById<View>(R.id.btnCancelDonate).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun launchTipPurchase(productId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val products = billingHelper.queryTipProducts()
            val product = products.find { it.productId == productId }
            if (product != null) {
                billingHelper.launchBillingFlow(this@MainActivity, product)
            } else {
                Toast.makeText(this@MainActivity, "商品情報の取得に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_menu -> {
                showMenuBottomSheet()
                true
            }
            R.id.action_disclaimer -> {
                AboutDialogFragment().show(supportFragmentManager, AboutDialogFragment.TAG)
                true
            }
            R.id.action_tip -> {
                showDonateDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}

