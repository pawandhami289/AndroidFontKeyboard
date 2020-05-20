package com.highstarapp.fontkeyboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.billingclient.api.*

class InAppPurchase : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private val skuList = listOf("test_product_one", "test_product_two")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_purchase)

        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    logger("Setup Billing Done")
                   //loadAllSKUs()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                logger("Failed")

            }
        })

    }

    private fun loadAllSKUs() = if (billingClient.isReady) {
        try{
            val params = SkuDetailsParams
                .newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
            billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
                // Process the result.
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                    for (skuDetails in skuDetailsList) {
                        if (skuDetails.sku == "test_product_one")
                        /*buttonBuyProduct.setOnClickListener {*/
                            billingClient.launchBillingFlow(this, BillingFlowParams
                                .newBuilder()
                                .setSkuDetails(skuDetails)
                                .build())
                        //}
                    }
                }
                logger(skuDetailsList[0].description)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    } else {
        println("Billing Client not ready")
    }


    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
            logger(debugMessage)
            logger(responseCode)
        }
    }


    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                acknowledgePurchase(purchase.purchaseToken)

            }
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            logger("User Cancelled")
            logger(billingResult?.debugMessage.toString())


        } else {
            logger(billingResult?.debugMessage.toString())
            // Handle any other error codes.
        }
    }




}
