package com.nyangailab.nyancalc.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.billingclient.api.*
import com.nyangailab.nyancalc.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Google Playの課金処理を管理するヘルパークラス
 */
class BillingClientHelper(private val context: Context) {

    private val TAG = "BillingClientHelper"

    // 開発者支援商品ID
    companion object {
        const val TIP_SMALL = "tip_120"
        const val TIP_MEDIUM = "tip_480"
        const val TIP_LARGE = "tip_1000"
    }

    // 課金完了リスナー
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "User canceled the purchase")
        } else {
            Log.e(TAG, "Purchase failed: ${billingResult.responseCode}")
        }
    }

    // 課金クライアント
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    // 接続状態
    private var isConnected = false

    // 初期化完了後に実行するコールバック
    private var onBillingSetupFinished: ((isSuccess: Boolean) -> Unit)? = null

    /**
     * 課金クライアントの初期化
     */
    fun initialize(onSetupFinished: (isSuccess: Boolean) -> Unit) {
        onBillingSetupFinished = onSetupFinished

        if (!isConnected) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    // Billing接続結果のログ出力
                    Log.d("Billing", "Connected: ${billingResult.responseCode}")

                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        isConnected = true
                        onBillingSetupFinished?.invoke(true)
                    } else {
                        onBillingSetupFinished?.invoke(false)
                        Log.e(TAG, "Billing setup failed: ${billingResult.responseCode}")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    isConnected = false
                    Log.d("Billing", "Disconnected")
                }
            })
        } else {
            onBillingSetupFinished?.invoke(true)
        }
    }

    /**
     * 商品情報の取得
     */
    suspend fun queryTipProducts(): List<ProductDetails> {
        if (!isConnected) {
            Log.d("Billing", "Not connected, can't query products")
            return emptyList()
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TIP_SMALL)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TIP_MEDIUM)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TIP_LARGE)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }

        // 商品情報のログ出力
        Log.d("Billing", "SKU count = ${result.productDetailsList?.size ?: 0}")
        result.productDetailsList?.forEach {
            Log.d("Billing", "Product: ${it.productId} - ${it.title}")
        }

        return result.productDetailsList ?: emptyList()
    }

    /**
     * 購入フローの開始
     */
    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * 購入処理の完了
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // 支払い確認（非消費型でもコンシューム処理が必要）
            CoroutineScope(Dispatchers.Main).launch {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                withContext(Dispatchers.IO) {
                    billingClient.consumePurchase(consumeParams)
                }

                // UI スレッドでトースト表示
                Toast.makeText(context, context.getString(R.string.thank_you), Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 開発者支援オプションを表示
     */
    fun showDonationOptions(activity: Activity) {
        // 接続していない場合は処理しない
        if (!isConnected) {
            Log.e("Billing", "Not connected to billing service")
            Toast.makeText(
                activity,
                "課金サービスに接続できません。エミュレータではなく実機での動作確認をお試しください。",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val productDetails = queryTipProducts()
                Log.d("Billing", "Retrieved ${productDetails.size} products")

                // 商品が取得できた場合のみダイアログを表示
                if (productDetails.isNotEmpty()) {
                    showImprovedTipDialog(activity, productDetails)
                } else {
                    Toast.makeText(
                        activity,
                        "開発者支援アイテムを読み込めません。エミュレータではなく実機で確認してください。",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("Billing", "No products available")
                }
            } catch (e: Exception) {
                Log.e("Billing", "Error querying products: ${e.message}", e)
                Toast.makeText(activity, "エラーが発生しました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 開発者支援ダイアログを表示（改良版）
     */
    private fun showImprovedTipDialog(activity: Activity, productDetails: List<ProductDetails>) {
        // 商品情報の並び替え（金額順）
        val sortedProducts = productDetails.sortedBy { it.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0 }

        // カスタムレイアウトを使用したダイアログを作成
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_donation, null)

        // ダイアログを作成
        val dialog = AlertDialog.Builder(activity, R.style.CompactDialog)
            .setTitle(R.string.donate_title)
            .setView(dialogView)
            .setNegativeButton(R.string.close, null)
            .create()

        // ダイアログの説明文を設定（短く表示）
        val descriptionText = dialogView.findViewById<TextView>(R.id.donationDescription)
        descriptionText.text = activity.getString(R.string.donate_description)
        descriptionText.maxLines = Integer.MAX_VALUE
        descriptionText.ellipsize = null

        // 支援ボタンの設定
        setupDonationButton(dialogView, R.id.btnSmallDonation, sortedProducts, 0, activity, dialog)
        setupDonationButton(dialogView, R.id.btnMediumDonation, sortedProducts, 1, activity, dialog)
        setupDonationButton(dialogView, R.id.btnLargeDonation, sortedProducts, 2, activity, dialog)

        // ダイアログを表示
        dialog.show()

        // ダイアログウィンドウの位置を上に調整
        dialog.window?.let { window ->
            // 高さを制限
            val params = window.attributes
            params.height = (activity.resources.displayMetrics.heightPixels * 0.75).toInt()
            // 上に移動
            params.y = -120
            window.attributes = params
        }
    }

    /**
     * 支援ボタンの設定
     */
    private fun setupDonationButton(
        dialogView: View,
        buttonId: Int,
        products: List<ProductDetails>,
        index: Int,
        activity: Activity,
        dialog: AlertDialog
    ) {
        val donationButton = dialogView.findViewById<Button>(buttonId)

        if (index < products.size) {
            val product = products[index]
            val price = product.oneTimePurchaseOfferDetails?.formattedPrice ?: "???"

            // ボタンのテキスト設定（商品情報を表示）
            when (product.productId) {
                TIP_SMALL -> donationButton.text = "小額支援 $price"
                TIP_MEDIUM -> donationButton.text = "中額支援 $price"
                TIP_LARGE -> donationButton.text = "大口支援 $price"
                else -> donationButton.text = "$price を支援する"
            }

            // クリックリスナー設定
            donationButton.setOnClickListener {
                dialog.dismiss()
                launchBillingFlow(activity, product)
            }

            donationButton.visibility = View.VISIBLE
        } else {
            donationButton.visibility = View.GONE
        }
    }

    /**
     * 課金クライアントの終了
     */
    fun endConnection() {
        billingClient.endConnection()
    }
}
