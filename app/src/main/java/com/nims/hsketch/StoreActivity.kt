package com.nims.hsketch

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProrationMode.IMMEDIATE_WITH_TIME_PRORATION
import kotlinx.android.synthetic.main.activity_store.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoreActivity : AppCompatActivity() {
    private lateinit var bm                                : BillingModule
    private lateinit var mStore_Imageview_Back             : ImageView
    private lateinit var mStore_Storeitemview_Remove_Admob : StoreItemView
    private lateinit var mStore_Storeitemview_Donate       : StoreItemView

    private var mSkuDetails = listOf<SkuDetails>()
        set(value) {
            field = value
            setSkuDetailsView()
        }

    private fun setClickListeners() {
        with(this) { // 광고 제거 구매 버튼 클릭
            mStore_Storeitemview_Remove_Admob.setOnClickListener {
                mSkuDetails.find { it.sku == DM.mStoreItemIdRemoveAdmob }
                    ?.let { skuDetail -> bm.purchase(skuDetail) } ?: also {
                    Toast.makeText(this@StoreActivity, getString(R.string.store_find_failed), Toast.LENGTH_LONG).show()
                }
            }
            mStore_Storeitemview_Donate.setOnClickListener {
                mSkuDetails.find { it.sku == DM.mStoreItemIdDonateToDev }
                    ?.let { skuDetail -> bm.purchase(skuDetail) } ?: also {
                    Toast.makeText(this@StoreActivity, getString(R.string.store_find_failed), Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        init()
        bm = BillingModule(this, lifecycleScope, object : BillingModule.Callback {
            override fun onBillingModulesIsReady() {
                bm.querySkuDetail(
                    BillingClient.SkuType.INAPP,
                    DM.mStoreItemIdRemoveAdmob,
                    DM.mStoreItemIdDonateToDev
                ) { skuDetails ->
                    mSkuDetails = skuDetails
                }
            }

            override fun onSuccess(purchase: Purchase) {
                when (purchase.sku) {
                    DM.mStoreItemIdRemoveAdmob -> {
                        PreferencesManager().setBoolean(this@StoreActivity, DM.mPreferencesKeyRemoveAdmob, true)
                        DM.getInstance()    .showToast(this@StoreActivity, getString(R.string.store_remove_admob_success))
                    }
                    DM.mStoreItemIdDonateToDev -> {
                        DM.getInstance().showToast(this@StoreActivity, getString(R.string.donate_thankyou))
                    }
                }
            }

            override fun onFailure(errorCode: Int) {
                when (errorCode) {
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        PreferencesManager().setBoolean(this@StoreActivity, DM.mPreferencesKeyRemoveAdmob, true)
                        DM.getInstance()    .showToast(this@StoreActivity, getString(R.string.store_already_owned))
                    }
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        DM.getInstance().showToast(this@StoreActivity, getString(R.string.store_canceled))
                    }
                    else -> {
                        DM.getInstance().showToast(this@StoreActivity, "error: $errorCode")
                    }
                }
            }
        })
        setClickListeners()
    }

    private fun init(){
        //bind
        mStore_Storeitemview_Remove_Admob = findViewById(R.id.store_storeitemview_remove_admob)
        mStore_Storeitemview_Donate       = findViewById(R.id.store_storeitemview_donate)
        mStore_Imageview_Back             = findViewById(R.id.store_imageview_back)

        onActivityFinish()
    }

    override fun finish() {
        super.finish()
        DM.getInstance().finishActivity(this)
    }

    private fun onActivityFinish(){
        mStore_Imageview_Back.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        bm.onResume(BillingClient.SkuType.INAPP)
    }


    private fun setSkuDetailsView() {
        mStore_Storeitemview_Remove_Admob.setTitleText(mSkuDetails[0].title)
        mStore_Storeitemview_Remove_Admob.setPriceText(mSkuDetails[0].price)
        mStore_Storeitemview_Remove_Admob.setDescriptionText(mSkuDetails[0].description)
        mStore_Storeitemview_Donate      .setTitleText(mSkuDetails[1].title)
        mStore_Storeitemview_Donate      .setPriceText(mSkuDetails[1].price)
        mStore_Storeitemview_Donate      .setDescriptionText(mSkuDetails[1].description)
    }

    class BillingModule(
        private val activity        : Activity,
        private val lifeCycleScope  : LifecycleCoroutineScope,
        private val callback        : Callback
    ) {
        // '소비'되어야 하는 sku 들을 적어줍니다.
        private val consumableSkus = setOf(DM.mStoreItemIdDonateToDev, DM.mStoreItemIdRemoveAdmob)

        // 구매관련 업데이트 수신
        private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            when {
                billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null -> {
                    // 제대로 구매 완료, 구매 확인 처리를 해야합니다. 3일 이내 구매확인하지 않으면 자동으로 환불됩니다.
                    for (purchase in purchases) {
                        confirmPurchase(purchase)
                    }
                }
                else -> {
                    // 구매 실패
                    callback.onFailure(billingResult.responseCode)
                }
            }
        }

        private var billingClient: BillingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        init {
            billingClient.startConnection(object: BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // 여기서부터 billingClient 활성화 됨
                        callback.onBillingModulesIsReady()
                    } else {
                        callback.onFailure(billingResult.responseCode)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // GooglePlay와 연결이 끊어졌을때 재시도하는 로직이 들어갈 수 있음.
                    Log.e("BillingModule", "Disconnected.")
                }
            })
        }

        /**
         * 구매를 했지만 확인되지 않은 건에대해서 확인처리를 합니다.
         * @param type BillingClient.SkuType.INAPP 또는 BillingClient.SkuType.SUBS
         */
        fun onResume(type: String) {
            if (billingClient.isReady) {
                billingClient.queryPurchases(type).purchasesList?.let { purchaseList ->
                    for (purchase in purchaseList) {
                        if (!purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            confirmPurchase(purchase)
                        }
                    }
                }
            }
        }

        /**
         * 원하는 sku id를 가지고있는 상품 정보를 가져옵니다.
         * @param sku sku 목록
         * @param resultBlock sku 상품정보 콜백
         */
        fun querySkuDetail(
            type: String = BillingClient.SkuType.INAPP,
            vararg sku: String,
            resultBlock: (List<SkuDetails>) -> Unit = {}
        ) {
            SkuDetailsParams.newBuilder().apply {
                // 인앱, 정기결제 유형중에서 고름. (SkuType.INAPP, SkuType.SUBS)
                setSkusList(sku.asList()).setType(type)
                // 비동기적으로 상품정보를 가져옵니다.
                lifeCycleScope.launch(Dispatchers.IO) {
                    val skuDetailResult = billingClient.querySkuDetails(build())
                    withContext(Dispatchers.Main) {
                        resultBlock(skuDetailResult.skuDetailsList ?: emptyList())
                    }
                }
            }
        }

        /**
         * 구매 시작하기
         * @param skuDetail 구매하고자하는 항목. querySkuDetail()을 통해 획득한 SkuDetail
         * @param oldPurchase 이미 구독중일때, 현재 구독 구매 정보를 전달
         */
        fun purchase(
            skuDetail: SkuDetails,
            oldPurchase: Purchase? = null
        ) {
            val flowParams = BillingFlowParams.newBuilder().apply {
                setSkuDetails(skuDetail)
                if (oldPurchase != null) {
                    // # 구독을 위한 ProrationMode 문서: https://developer.android.com/reference/com/android/billingclient/api/BillingFlowParams.ProrationMode
                    setReplaceSkusProrationMode(IMMEDIATE_WITH_TIME_PRORATION)
                    setOldSku(oldPurchase.sku, oldPurchase.purchaseToken)
                }
            }.build()

            // 구매 절차를 시작, OK라면 제대로 된것입니다.
            val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
            if (responseCode != BillingClient.BillingResponseCode.OK) {
                callback.onFailure(responseCode)
            }
            // 이후 부터는 purchasesUpdatedListener를 거치게 됩니다.
        }

        /**
         * 구매 여부 체크, 소비성 구매가 아닌 항목에 한정.
         * @param sku
         */
        fun checkPurchased(
            sku: String,
            resultBlock: (purchased: Boolean) -> Unit
        ) {
            billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList?.let { purchaseList ->
                for (purchase in purchaseList) {
                    if (purchase.sku == sku && purchase.isPurchaseConfirmed()) {
                        return resultBlock(true)
                    }
                }
                return resultBlock(false)
            }
        }

        /**
         * 구독 여부 체크
         * @param sku
         * @return 구독하지 않았다면 null을 반환합니다.
         */
        fun checkSubscribed(resultBlock: (Purchase?) -> Unit) {
            billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList?.let { purchaseList ->
                for (purchase in purchaseList) {
                    if (purchase.isPurchaseConfirmed()) {
                        return resultBlock(purchase)
                    }
                }
                return resultBlock(null)
            }
        }

        /**
         * 구매 확인 처리
         * @param purchase 확인처리할 아이템의 구매정보
         */
        private fun confirmPurchase(purchase: Purchase) {
            when {
                consumableSkus.contains(purchase.sku) -> {
                    // 소비성 구매는 consume을 해주어야합니다.
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    lifeCycleScope.launch(Dispatchers.IO) {
                        val result = billingClient.consumePurchase(consumeParams)
                        withContext(Dispatchers.Main) {
                            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                callback.onSuccess(purchase)
                            }
                        }
                    }
                }
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged -> {
                    // 구매는 완료되었으나 확인이 되어있지 않다면 구매 확인 처리를 합니다.
                    val ackPurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                    lifeCycleScope.launch(Dispatchers.IO) {
                        val result = billingClient.acknowledgePurchase(ackPurchaseParams.build())
                        withContext(Dispatchers.Main) {
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                callback.onSuccess(purchase)
                            } else {
                                callback.onFailure(result.responseCode)
                            }
                        }
                    }
                }
            }
        }

        // 구매 확인 검사 Extension
        private fun Purchase.isPurchaseConfirmed(): Boolean {
            return this.isAcknowledged && this.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        interface Callback {
            fun onBillingModulesIsReady()
            fun onSuccess(purchase: Purchase)
            fun onFailure(errorCode: Int)
        }
    }
}
