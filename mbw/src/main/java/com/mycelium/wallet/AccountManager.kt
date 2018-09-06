package com.mycelium.wallet

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.google.common.collect.ImmutableMap
import com.mycelium.wallet.event.AccountChanged
import com.mycelium.wallet.event.AccountListChanged
import com.mycelium.wallet.event.ExtraAccountsChanged
import com.mycelium.wallet.event.SelectedAccountChanged
import com.mycelium.wapi.wallet.AccountProvider
import com.mycelium.wapi.wallet.WalletAccount
import com.mycelium.wapi.wallet.btc.WalletBtcAccount
import com.mycelium.wapi.wallet.btc.WalletBtcAccount.Type
import com.mycelium.wapi.wallet.btc.WalletBtcAccount.Type.*
import com.mycelium.wapi.wallet.WalletManager
import com.squareup.otto.Subscribe
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.HashMap

object AccountManager : AccountProvider {
    val accounts: HashMap<UUID, WalletBtcAccount> = hashMapOf()
    private val accountsSemaphore = Semaphore(100)
    private val archivedAccounts: HashMap<UUID, WalletBtcAccount> = hashMapOf()
    private val archivedAccountsSemaphore = Semaphore(100)
    private val masterSeedAccounts: HashMap<UUID, WalletBtcAccount> = hashMapOf()
    private val masterSeedAccountsSemaphore = Semaphore(100)

    init {
        Handler(Looper.getMainLooper()).post {
            MbwManager.getInstance(WalletApplication.getInstance()).eventBus.register(this)
        }
        FillAccountsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    class FillAccountsTask : AsyncTask<Void, Void, Void>() {
        val mbwManager = MbwManager.getInstance(WalletApplication.getInstance())!!
        override fun doInBackground(vararg params: Void): Void? {
            val walletManager: WalletManager = mbwManager.getWalletManager(false)
            accountsSemaphore.acquire(100)
            accounts.clear()
            accounts.putAll(walletManager.activeAccounts)
            accountsSemaphore.release(100)
            archivedAccountsSemaphore.acquire(100)
            archivedAccounts.clear()
            archivedAccounts.putAll(walletManager.archivedAccounts)
            archivedAccountsSemaphore.release(100)
            masterSeedAccountsSemaphore.acquire(100)
            masterSeedAccounts.clear()
            masterSeedAccounts.putAll(walletManager.activeMasterseedAccounts)
            masterSeedAccountsSemaphore.release(100)
            return null
        }

        override fun onPostExecute(result: Void?) {
            mbwManager.eventBus.post(AccountListChanged())
        }
    }

    override fun getAccounts(): ImmutableMap<UUID, WalletBtcAccount> = ImmutableMap.copyOf<UUID, WalletBtcAccount>(accounts)

    fun getBTCSingleAddressAccounts(): ImmutableMap<UUID, WalletBtcAccount> =
            getFilteredAccounts(accountsSemaphore, accounts) {
                it.value.type == BTCSINGLEADDRESS && !Utils.checkIsLinked(it.value, accounts.values)
            }

    fun getBTCBip44Accounts() = getAccountsByType(BTCBIP44)

    fun getBCHSingleAddressAccounts() = getAccountsByType(BCHSINGLEADDRESS)

    fun getBCHBip44Accounts() = getAccountsByType(BCHBIP44)

    fun getCoinapultAccounts() = getAccountsByType(COINAPULT)

    fun getColuAccounts() = getAccountsByType(COLU)

    fun getDashAccounts() = getAccountsByType(DASH)

    fun getActiveAccounts(): ImmutableMap<UUID, WalletBtcAccount> =
            getFilteredAccounts(accountsSemaphore, accounts) {
                it.value.isVisible
            }

    private fun getAccountsByType(coinType: Type): ImmutableMap<UUID, WalletBtcAccount> =
            getFilteredAccounts(accountsSemaphore, accounts) {
                it.value.type == coinType && it.value.isVisible
            }

    fun getBTCMasterSeedAccounts(): ImmutableMap<UUID, WalletBtcAccount> =
            getFilteredAccounts(masterSeedAccountsSemaphore, masterSeedAccounts) {
                it.value.type == BTCBIP44 && it.value.isVisible
            }

    fun getArchivedAccounts(): ImmutableMap<UUID, WalletBtcAccount> =
            getFilteredAccounts(archivedAccountsSemaphore, archivedAccounts) {
                it.value.isVisible
            }

    override fun getAccount(uuid: UUID?): WalletBtcAccount? = accounts[uuid]

    override fun hasAccount(uuid: UUID?): Boolean = accounts.containsKey(uuid)

    private fun HashMap<UUID, WalletBtcAccount>.putAll(from: List<WalletAccount<*,*>>) {
        val result: MutableMap<UUID, WalletBtcAccount> = mutableMapOf()
        for (walletAccount in from) {
            result[walletAccount.id] = walletAccount as WalletBtcAccount
        }
        putAll(result)
    }

    @Subscribe
    fun accountChanged(event: AccountChanged) {
        FillAccountsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @Subscribe
    fun selectedAccountChanged(event: SelectedAccountChanged) {
        FillAccountsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @Subscribe
    fun extraAccountsChanged(event: ExtraAccountsChanged) {
        FillAccountsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun getFilteredAccounts(s: Semaphore, accounts: Map<UUID, WalletBtcAccount>,
                                    filter: (mapEntry: Map.Entry<UUID, WalletBtcAccount>) -> Boolean): ImmutableMap<UUID, WalletBtcAccount> {
        s.acquire()
        val filteredAccounts = ImmutableMap.copyOf<UUID, WalletBtcAccount>(accounts.filter { filter(it) })
        s.release()
        return filteredAccounts
    }
}

